package org.tron.core.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.protobuf.ByteString;
import com.typesafe.config.ConfigObject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.common.utils.Commons;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockBalanceTraceCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.core.db.accountstate.AccountStateCallBackUtils;
import org.tron.core.db2.common.Key;
import org.tron.core.db2.common.WrappedByteArray;
import org.tron.core.exception.BadItemException;
import org.tron.protos.Protocol;
import org.tron.protos.contract.BalanceContract.TransactionBalanceTrace;
import org.tron.protos.contract.BalanceContract.TransactionBalanceTrace.Operation;

@Slf4j(topic = "DB")
@Component
public class AccountStore extends TronStoreWithRevoking<AccountCapsule> {

  private static Map<String, byte[]> assertsAddress = new HashMap<>(); // key = name , value = address

  @Autowired
  private AccountStateCallBackUtils accountStateCallBackUtils;

  @Autowired
  private BalanceTraceStore balanceTraceStore;

  @Autowired
  private AccountTraceStore accountTraceStore;

  @Autowired
  private DynamicPropertiesStore dynamicPropertiesStore;

  private Cache<WrappedByteArray, Protocol.Account> accountCache = Caffeine.newBuilder()
      .expireAfterAccess(7, TimeUnit.DAYS)
      .expireAfterWrite(7, TimeUnit.DAYS)
      .build();

  @Autowired
  private AccountStore(@Value("account") String dbName) {
    super(dbName);
  }

  public static void setAccount(com.typesafe.config.Config config) {
    List list = config.getObjectList("genesis.block.assets");
    for (int i = 0; i < list.size(); i++) {
      ConfigObject obj = (ConfigObject) list.get(i);
      String accountName = obj.get("accountName").unwrapped().toString();
      byte[] address = Commons.decodeFromBase58Check(obj.get("address").unwrapped().toString());
      assertsAddress.put(accountName, address);
    }
  }

  @Override
  public AccountCapsule get(byte[] key) {
    return getUnchecked(key);
  }

  @Override
  public AccountCapsule getUnchecked(byte[] key) {
    if (isSync()) {
      Protocol.Account account = accountCache.getIfPresent(WrappedByteArray.of(key));
      if (account != null) {
        return new AccountCapsule(account);
      }
    } else {
      accountCache.invalidateAll();
    }

    byte[] value = revokingDB.getUnchecked(key);
    if (ArrayUtils.isEmpty(value)) {
      return null;
    }

    AccountCapsule accountCapsule = new AccountCapsule(value);
    if (isSync()) {
      accountCache.put(WrappedByteArray.of(key), accountCapsule.getInstance());
    }
    return accountCapsule;
  }

  @Override
  public void put(byte[] key, AccountCapsule item) {
    if (Objects.isNull(key) || Objects.isNull(item)) {
      return;
    }

    AccountCapsule old = get(key);
    if (old == null) {
      if (item.getBalance() != 0) {
        recordBalance(item, item.getBalance());
        BlockCapsule.BlockId blockId = balanceTraceStore.getCurrentBlockId();
        if (blockId != null) {
          accountTraceStore.recordBalanceWithBlock(key, blockId.getNum(), item.getBalance());
        }
      }
    } else if (old.getBalance() != item.getBalance()){
      recordBalance(item, item.getBalance() - old.getBalance());
      BlockCapsule.BlockId blockId = balanceTraceStore.getCurrentBlockId();
      if (blockId != null) {
        accountTraceStore.recordBalanceWithBlock(key, blockId.getNum(), item.getBalance());
      }
    }

    super.put(key, item);
    if (isSync()) {
      accountCache.put(WrappedByteArray.of(key), item.getInstance());
    }
    accountStateCallBackUtils.accountCallBack(key, item);
  }

  @Override
  public void delete(byte[] key) {
    AccountCapsule old = get(key);
    if (old != null) {
      recordBalance(old, -old.getBalance());
    }

    BlockCapsule.BlockId blockId = balanceTraceStore.getCurrentBlockId();
    if (blockId != null) {
      accountTraceStore.recordBalanceWithBlock(key, blockId.getNum(), 0);
    }

    if (isSync()) {
      accountCache.invalidate(WrappedByteArray.of(key));
    }
    super.delete(key);
  }


  /**
   * Max TRX account.
   */
  public AccountCapsule getSun() {
    return getUnchecked(assertsAddress.get("Sun"));
  }

  /**
   * Min TRX account.
   */
  public AccountCapsule getBlackhole() {
    return getUnchecked(assertsAddress.get("Blackhole"));
  }

  /**
   * Get foundation account info.
   */
  public AccountCapsule getZion() {
    return getUnchecked(assertsAddress.get("Zion"));
  }

  @Override
  public void close() {
    super.close();
  }

  // do somethings
  // check old balance and new balance, if equals, do nothing, then get balance trace from balancetraceStore
  private void recordBalance(AccountCapsule accountCapsule, long diff) {
    TransactionBalanceTrace transactionBalanceTrace = balanceTraceStore.getCurrentTransactionBalanceTrace();

    if (transactionBalanceTrace == null) {
      return;
    }

    long operationIdentifier;
    OptionalLong max = transactionBalanceTrace.getOperationList().stream()
        .mapToLong(Operation::getOperationIdentifier)
        .max();
    if (max.isPresent()) {
      operationIdentifier = max.getAsLong() + 1;
    } else {
      operationIdentifier = 0;
    }

    ByteString address = accountCapsule.getAddress();
    TransactionBalanceTrace.Operation operation = Operation.newBuilder()
        .setAddress(address)
        .setAmount(diff)
        .setOperationIdentifier(operationIdentifier)
        .build();
    transactionBalanceTrace = transactionBalanceTrace.toBuilder()
        .addOperation(operation)
        .build();
    balanceTraceStore.setCurrentTransactionBalanceTrace(transactionBalanceTrace);
  }

  public boolean isSync() {
    long timestamp = dynamicPropertiesStore.getLatestBlockHeaderTimestamp();
    return System.currentTimeMillis() - timestamp >= 3600_000L;
  }
}

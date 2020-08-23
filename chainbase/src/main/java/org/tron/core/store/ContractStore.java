package org.tron.core.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Streams;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.core.Is;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.ContractCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.core.db2.common.WrappedByteArray;
import org.tron.protos.Protocol;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;

@Slf4j(topic = "DB")
@Component
public class ContractStore extends TronStoreWithRevoking<ContractCapsule> {

  @Autowired
  private AccountStore accountStore;

  private Cache<WrappedByteArray, SmartContract> contractCache = Caffeine.newBuilder()
      .expireAfterAccess(7, TimeUnit.DAYS)
      .expireAfterWrite(7, TimeUnit.DAYS)
      .build();


  @Autowired
  private ContractStore(@Value("contract") String dbName) {
    super(dbName);
  }

  @Override
  public ContractCapsule get(byte[] key) {
    return getUnchecked(key);
  }

  @Override
  public ContractCapsule getUnchecked(byte[] key) {
    if (accountStore.isSync()) {
      SmartContract contract = contractCache.getIfPresent(WrappedByteArray.of(key));
      if (contract != null) {
        return new ContractCapsule(contract);
      }
    } else {
      contractCache.invalidateAll();
    }

    byte[] value = revokingDB.getUnchecked(key);
    if (ArrayUtils.isEmpty(value)) {
      return null;
    }

    ContractCapsule contractCapsule = new ContractCapsule(value);
    if (accountStore.isSync()) {
      contractCache.put(WrappedByteArray.of(key), contractCapsule.getInstance());
    }
    return contractCapsule;
  }

  @Override
  public void put(byte[] key, ContractCapsule item) {
    if (Objects.isNull(key) || Objects.isNull(item)) {
      return;
    }

    revokingDB.put(key, item.getData());
    if (accountStore.isSync()) {
      contractCache.put(WrappedByteArray.of(key), item.getInstance());
    }
  }

  @Override
  public void delete(byte[] key) {
    revokingDB.delete(key);
    if (accountStore.isSync()) {
      contractCache.invalidate(WrappedByteArray.of(key));
    }
  }

  /**
   * get total transaction.
   */
  public long getTotalContracts() {
    return Streams.stream(revokingDB.iterator()).count();
  }

  /**
   * find a transaction  by it's id.
   */
  public byte[] findContractByHash(byte[] trxHash) {
    return revokingDB.getUnchecked(trxHash);
  }

  /**
   *
   * @param contractAddress
   * @return
   */
  public SmartContract.ABI getABI(byte[] contractAddress) {
    ContractCapsule contractCapsule = getUnchecked(contractAddress);
    if (contractCapsule == null) {
      return null;
    }

    SmartContract smartContract = contractCapsule.getInstance();
    if (smartContract == null) {
      return null;
    }

    return smartContract.getAbi();
  }

}

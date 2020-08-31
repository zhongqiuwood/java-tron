package org.tron.core.db;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.primitives.Bytes;
import com.google.common.reflect.TypeToken;
import com.google.protobuf.GeneratedMessageV3;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteOptions;
import org.rocksdb.DirectComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.storage.leveldb.LevelDbDataSourceImpl;
import org.tron.common.storage.rocksdb.RocksDbDataSourceImpl;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.StorageUtils;
import org.tron.core.capsule.ProtoCapsule;
import org.tron.core.db2.common.DB;
import org.tron.core.db2.common.IRevokingDB;
import org.tron.core.db2.common.LevelDB;
import org.tron.core.db2.common.RocksDB;
import org.tron.core.db2.common.WrappedByteArray;
import org.tron.core.db2.core.Chainbase;
import org.tron.core.db2.core.ITronChainBase;
import org.tron.core.db2.core.RevokingDBWithCachingOldValue;
import org.tron.core.db2.core.SnapshotRoot;
import org.tron.core.exception.BadItemException;
import org.tron.core.exception.ItemNotFoundException;
import org.tron.core.store.AccountTraceStore;
import org.tron.core.store.BalanceTraceStore;
import org.tron.core.store.MarketAccountStore;
import org.tron.core.store.MarketOrderStore;
import org.tron.core.store.MarketPairPriceToOrderStore;
import org.tron.core.store.MarketPairToPriceStore;
import org.tron.core.store.TransactionHistoryStore;
import org.tron.core.store.TransactionRetStore;
import org.tron.core.store.TreeBlockIndexStore;
import org.tron.protos.Protocol;
import org.tron.protos.contract.Common;


@Slf4j(topic = "DB")
public abstract class TronStoreWithRevoking<T extends ProtoCapsule<U>, U extends GeneratedMessageV3> implements ITronChainBase<T> {

  @Getter // only for unit test
  protected IRevokingDB revokingDB;

  private Cache<WrappedByteArray, U> cache = Caffeine.newBuilder().build();

  private TypeToken<T> token = new TypeToken<T>(getClass()) {
  };

  private TypeToken<U> uTypeToken = new TypeToken<U>(getClass()) {
  };

  @Autowired
  private RevokingDatabase revokingDatabase;

  protected TronStoreWithRevoking(String dbName) {
    int dbVersion = CommonParameter.getInstance().getStorage().getDbVersion();
    String dbEngine = CommonParameter.getInstance().getStorage().getDbEngine();
    if (dbVersion == 1) {
      this.revokingDB = new RevokingDBWithCachingOldValue(dbName,
          getOptionsByDbNameForLevelDB(dbName));
    } else if (dbVersion == 2) {
      if ("LEVELDB".equals(dbEngine.toUpperCase())) {
        this.revokingDB = new Chainbase(new SnapshotRoot(
            new LevelDB(
                new LevelDbDataSourceImpl(StorageUtils.getOutputDirectoryByDbName(dbName),
                    dbName,
                    getOptionsByDbNameForLevelDB(dbName),
                    new WriteOptions().sync(CommonParameter.getInstance()
                        .getStorage().isDbSync())))));
      } else if ("ROCKSDB".equals(dbEngine.toUpperCase())) {
        String parentPath = Paths
            .get(StorageUtils.getOutputDirectoryByDbName(dbName), CommonParameter
                .getInstance().getStorage().getDbDirectory()).toString();

        this.revokingDB = new Chainbase(new SnapshotRoot(
            new RocksDB(
                new RocksDbDataSourceImpl(parentPath,
                    dbName, CommonParameter.getInstance()
                    .getRocksDBCustomSettings(), getDirectComparator()))));
      }
    } else {
      throw new RuntimeException("db version is error.");
    }
  }

  protected org.iq80.leveldb.Options getOptionsByDbNameForLevelDB(String dbName) {
    return StorageUtils.getOptionsByDbName(dbName);
  }

  protected DirectComparator getDirectComparator() {
    return null;
  }

  protected TronStoreWithRevoking(DB<byte[], byte[]> db) {
    int dbVersion = CommonParameter.getInstance().getStorage().getDbVersion();
    if (dbVersion == 2) {
      this.revokingDB = new Chainbase(new SnapshotRoot(db));
    } else {
      throw new RuntimeException("db version is only 2.(" + dbVersion + ")");
    }
  }

  // only for test
  protected TronStoreWithRevoking(String dbName, RevokingDatabase revokingDatabase) {
    this.revokingDB = new RevokingDBWithCachingOldValue(dbName,
        (AbstractRevokingStore) revokingDatabase);
  }

  // only for test
  protected TronStoreWithRevoking(String dbName, Options options,
      RevokingDatabase revokingDatabase) {
    this.revokingDB = new RevokingDBWithCachingOldValue(dbName, options,
        (AbstractRevokingStore) revokingDatabase);
  }

  @Override
  public String getDbName() {
    return null;
  }

  @PostConstruct
  private void init() {
    initCache();
    revokingDatabase.add(revokingDB);
  }

  private void initCache() {
    if (!isCached()) {
      return;
    }

    for (Map.Entry<byte[], byte[]> e : revokingDB) {
      try {
        cache.put(WrappedByteArray.of(e.getKey()), of(e.getValue()).getInstance());
      } catch (BadItemException ex) {
        ex.printStackTrace();
      }
    }
  }

  private boolean isCached() {
    return CommonParameter.getInstance().isReplay() && !STORES.contains(getClass());
  }

  @Override
  public void put(byte[] key, T item) {
    if (Objects.isNull(key) || Objects.isNull(item)) {
      return;
    }

    if (isCached()) {
      U u = item.getInstance();
      cache.put(WrappedByteArray.of(key), u);
    } else {
      revokingDB.put(key, item.getData());
    }
  }

  @Override
  public void delete(byte[] key) {
    revokingDB.delete(key);
    if (isCached()) {
      cache.invalidate(WrappedByteArray.of(key));
    }
  }

  @Override
  public T get(byte[] key) throws ItemNotFoundException, BadItemException {
    if (isCached()) {
      U u = cache.getIfPresent(WrappedByteArray.of(key));
      if (u != null) {
        return of(u);
      }
    }

    return of(revokingDB.get(key));
  }

  @Override
  public T getUnchecked(byte[] key) {
    try {
      if (isCached()) {
        U u = cache.getIfPresent(WrappedByteArray.of(key));
        if (u != null) {
          return of(u);
        }
      }

      byte[] value = revokingDB.getUnchecked(key);
      if (value == null) {
        return null;
      }
      return of(value);
    } catch (BadItemException e) {
      return null;
    }
  }

  public byte[] getBytes(byte[] key) {
    if (isCached()) {
      U u = cache.getIfPresent(WrappedByteArray.of(key));
      if (u != null) {
        return u.toByteArray();
      }
    }

    return revokingDB.getUnchecked(key);
  }

  public T of(byte[] value) throws BadItemException {
    try {
      Constructor constructor = token.getRawType().getConstructor(byte[].class);
      @SuppressWarnings("unchecked")
      T t = (T) constructor.newInstance((Object) value);
      return t;
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      throw new BadItemException(e.getMessage());
    }
  }

  public T of(U value) throws BadItemException {
    try {
      Constructor constructor = token.getRawType().getConstructor(uTypeToken.getRawType());
      @SuppressWarnings("unchecked")
      T t = (T) constructor.newInstance(value);
      return t;
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      throw new BadItemException(e.getMessage());
    }
  }

  @Override
  public boolean has(byte[] key) {
    if (isCached()) {
      return cache.getIfPresent(WrappedByteArray.of(key)) != null;
    }

    return revokingDB.has(key);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void close() {
    revokingDB.close();
  }

  @Override
  public void reset() {
    revokingDB.reset();
  }

  @Override
  public Iterator<Map.Entry<byte[], T>> iterator() {
    if (isCached()) {
      return Iterators.transform(cache.asMap().entrySet().stream()
          .sorted((e1, e2) -> ByteUtil.compare(e1.getKey().getBytes(), e2.getKey().getBytes()))
          .iterator(), e -> {
        try {
          return Maps.immutableEntry(e.getKey().getBytes(), of(e.getValue()));
        } catch (BadItemException e1) {
          throw new RuntimeException(e1);
        }
      });
    }

    return Iterators.transform(revokingDB.iterator(), e -> {
      try {
        return Maps.immutableEntry(e.getKey(), of(e.getValue()));
      } catch (BadItemException e1) {
        throw new RuntimeException(e1);
      }
    });
  }

  public long size() {
    if (isCached()) {
      return cache.asMap().size();
    }

    return Streams.stream(revokingDB.iterator()).count();
  }

  public void setCursor(Chainbase.Cursor cursor) {
    revokingDB.setCursor(cursor);
  }

  public void flush() {
    for (Map.Entry<WrappedByteArray, U> e : cache.asMap().entrySet()) {
      if (e.getValue() instanceof Common.ByteArray) {
        revokingDB.put(e.getKey().getBytes(), ((Common.ByteArray) e.getValue()).getData().toByteArray());
      } else {
        revokingDB.put(e.getKey().getBytes(), e.getValue().toByteArray());
      }
    }
  }

  private static final Set<Type> STORES = new HashSet<>(ImmutableList.of(
      BlockStore.class,
      BlockIndexStore.class,
      RecentBlockStore.class,
      AccountTraceStore.class,
      BalanceTraceStore.class,
      TransactionStore.class,
      TransactionRetStore.class,
      TransactionHistoryStore.class,
      MarketAccountStore.class,
      MarketOrderStore.class,
      MarketPairPriceToOrderStore.class,
      MarketPairToPriceStore.class,
      TreeBlockIndexStore.class
  ));
}

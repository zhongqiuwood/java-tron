package org.tron.core.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.StorageRowCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.core.db2.common.WrappedByteArray;
import org.tron.protos.Protocol;

@Slf4j(topic = "DB")
@Component
public class StorageRowStore extends TronStoreWithRevoking<StorageRowCapsule> {

  @Autowired
  private AccountStore accountStore;

  private Cache<WrappedByteArray, WrappedByteArray> storageRowCache = Caffeine.newBuilder()
      .expireAfterAccess(7, TimeUnit.DAYS)
      .expireAfterWrite(7, TimeUnit.DAYS)
      .build();

  @Autowired
  private StorageRowStore(@Value("storage-row") String dbName) {
    super(dbName);
  }

  @Override
  public StorageRowCapsule get(byte[] key) {
    StorageRowCapsule row = getUnchecked(key);
    row.setRowKey(key);
    return row;
  }

  @Override
  public StorageRowCapsule getUnchecked(byte[] key) {
    if (accountStore.isSync()) {
      WrappedByteArray value = storageRowCache.getIfPresent(WrappedByteArray.of(key));
      if (value != null) {
        return new StorageRowCapsule(WrappedByteArray.copyOf(value.getBytes()).getBytes());
      }
    } else {
      storageRowCache.invalidateAll();
    }

    StorageRowCapsule storageRowCapsule = super.getUnchecked(key);

    if (accountStore.isSync()) {
      storageRowCache.put(WrappedByteArray.of(key), WrappedByteArray.copyOf(storageRowCapsule.getInstance()));
    }
    return storageRowCapsule;
  }

  @Override
  public void put(byte[] key, StorageRowCapsule item) {
    if (Objects.isNull(key) || Objects.isNull(item)) {
      return;
    }

    super.put(key, item);
    if (accountStore.isSync()) {
      storageRowCache.put(WrappedByteArray.of(key), WrappedByteArray.copyOf(item.getInstance()));
    }
  }

  @Override
  public void delete(byte[] key) {
    super.delete(key);

    if (accountStore.isSync()) {
      storageRowCache.invalidate(WrappedByteArray.of(key));
    }
  }

}

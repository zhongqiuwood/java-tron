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
import org.tron.protos.contract.Common;

@Slf4j(topic = "DB")
@Component
public class StorageRowStore extends TronStoreWithRevoking<StorageRowCapsule, Common.ByteArray> {

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
}

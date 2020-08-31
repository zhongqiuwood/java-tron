package org.tron.core.store;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.protos.contract.Common;

@Component
public class NullifierStore extends TronStoreWithRevoking<BytesCapsule, Common.ByteArray> {

  @Autowired
  public NullifierStore(@Value("nullifier") String dbName) {
    super(dbName);
  }

  public void put(BytesCapsule bytesCapsule) {
    put(bytesCapsule.getData(), new BytesCapsule(bytesCapsule.getData()));
  }

  @Override
  public BytesCapsule get(byte[] key) {
    return getUnchecked(key);
  }
}
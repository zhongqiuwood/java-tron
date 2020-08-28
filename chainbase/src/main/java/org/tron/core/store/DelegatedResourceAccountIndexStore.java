package org.tron.core.store;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.DelegatedResourceAccountIndexCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.protos.Protocol;

@Component
public class DelegatedResourceAccountIndexStore extends
    TronStoreWithRevoking<DelegatedResourceAccountIndexCapsule, Protocol.DelegatedResourceAccountIndex> {

  @Autowired
  public DelegatedResourceAccountIndexStore(@Value("DelegatedResourceAccountIndex") String dbName) {
    super(dbName);
  }

  @Override
  public DelegatedResourceAccountIndexCapsule get(byte[] key) {

    byte[] value = revokingDB.getUnchecked(key);
    return ArrayUtils.isEmpty(value) ? null : new DelegatedResourceAccountIndexCapsule(value);
  }

}
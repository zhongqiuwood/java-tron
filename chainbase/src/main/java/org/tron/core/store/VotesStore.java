package org.tron.core.store;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.VotesCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.protos.Protocol;

@Component
public class VotesStore extends TronStoreWithRevoking<VotesCapsule, Protocol.Votes> {

  @Autowired
  public VotesStore(@Value("votes") String dbName) {
    super(dbName);
  }

  @Override
  public VotesCapsule get(byte[] key) {
    return getUnchecked(key);
  }
}
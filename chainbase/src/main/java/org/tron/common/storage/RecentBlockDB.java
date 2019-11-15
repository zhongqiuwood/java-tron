package org.tron.common.storage;

import com.google.common.collect.Streams;
import org.springframework.stereotype.Component;
import org.tron.common.utils.ByteArray;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.db.BlockIndexStore;
import org.tron.core.db2.common.Key;
import org.tron.core.db2.common.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class RecentBlockDB {

  private Map<Key, Value> recentBlocks = new HashMap<>();

  public void put(byte[] blockHash) {
    byte[] bytes = ByteArray.subArray(blockHash, 5, 8);
    bytes[0] &= 1;
    recentBlocks.put(Key.of(bytes), Value.of(null, blockHash));
  }

  public byte[] getPrevBlockHash(byte[] blockHash) {
    byte[] bytes = ByteArray.subArray(blockHash, 5, 8);
    bytes[0] &= 1;
    Key key = Key.of(bytes);
    Value value = recentBlocks.get(key);
    byte[] prevHash = value == null ? null : value.getBytes();
    if (!Arrays.equals(blockHash, prevHash)) {
      return prevHash;
    }

    return null;
  }

  public byte[] getPrevBlockHash(long num) {
    byte[] bytes = ByteArray.subArray(ByteArray.fromLong(num), 5, 8);
    bytes[0] &= 1;
    Key key = Key.of(bytes);
    Value value = recentBlocks.get(key);
    byte[] prevHash = value == null ? null : value.getBytes();
    if (prevHash != null && !Arrays.equals(ByteArray.fromLong(num), ByteArray.subArray(prevHash,0, 8))) {
      return prevHash;
    }

    return null;
  }

  public long getPrevBlockNum(long num) {
    byte[] bytes = ByteArray.subArray(ByteArray.fromLong(num), 5, 8);
    bytes[0] &= 1;
    Key key = Key.of(bytes);
    Value value = recentBlocks.get(key);
    byte[] prevHash = value == null ? null : value.getBytes();
    if (prevHash != null && !Arrays.equals(ByteArray.fromLong(num), ByteArray.subArray(prevHash,0, 8))) {
      return ByteArray.toLong(ByteArray.subArray(prevHash,0, 8));
    }

    return -1;
  }

  public boolean hashPrevHash(byte[] blockHash) {
    return getPrevBlockHash(blockHash) != null;
  }

  public void load(BlockIndexStore blockIndexStore) {
    Streams.stream(blockIndexStore).forEach(e -> put(e.getValue().getData()));
  }
}

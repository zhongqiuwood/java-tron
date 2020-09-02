package org.tron.program;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.RocksDB;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.db.BlockStore;
import org.tron.core.store.DynamicPropertiesStore;

@Slf4j(topic = "db")
public class CurrentBlock {

  static {
    RocksDB.loadLibrary();
  }

  public void print() {
    BlockStore blockStore = new BlockStore("block");
    DynamicPropertiesStore dynamicPropertiesStore = new DynamicPropertiesStore("properties");
    List<BlockCapsule> blockCapsules = blockStore.getBlockByLatestNum(1);
    if (CollectionUtils.isEmpty(blockCapsules)) {
      System.out.println("current block number (block/properties): " + 0
          + ", " + dynamicPropertiesStore.getLatestBlockHeaderNumber());
      return;
    }

    long latestNumber = blockCapsules.get(0).getNum();
    System.out.println("current block number (block/properties): " + latestNumber
        + ", " + dynamicPropertiesStore.getLatestBlockHeaderNumber());
    System.exit(0);
  }
}

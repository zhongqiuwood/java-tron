package org.tron.program;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.BlockIndexStore;
import org.tron.core.db.BlockStore;
import org.tron.core.db.TransactionStore;
import org.tron.core.exception.BadItemException;
import org.tron.core.exception.ItemNotFoundException;
import org.tron.core.store.DynamicPropertiesStore;

@Component
@Slf4j(topic = "DB")
public class PruneBlock {

  public static final long MIN = (1<<16) + 100;

  @Autowired
  BlockStore blockStore;
  @Autowired
  BlockIndexStore blockIndexStore;
  @Autowired
  TransactionStore transactionStore;
  @Autowired
  DynamicPropertiesStore dynamicPropertiesStore;

  public void pruneAll() {
    long blockNumber = dynamicPropertiesStore.getLatestBlockHeaderNumber();
    long min = blockNumber - MIN;

    blockStore.forEach(e -> {
      BlockCapsule block = e.getValue();
      long number = block.getNum();
      if (number < min) {
        delete(block);
      }
    });

  }

  public void prune(BlockCapsule block) {
    long blockNumber = block.getNum();
    long min = blockNumber - MIN;
    if (min <=0) {
      return;
    }

    try {
      BlockCapsule.BlockId blockId = blockIndexStore.get(min);
      BlockCapsule minBlock = blockStore.get(blockId.getBytes());
      delete(minBlock);
      logger.info("prune block {}, {} done", min, blockId);
    } catch (ItemNotFoundException | BadItemException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public void delete(BlockCapsule block) {
    if (block == null) {
      return;
    }

    for (TransactionCapsule transaction : block.getTransactions()) {
      Sha256Hash transactionId = transaction.getTransactionId();
      transactionStore.delete(transactionId.getBytes());
    }

    blockStore.delete(block.getBlockId().getBytes());

    blockIndexStore.delete(Longs.toByteArray(block.getNum()));
  }

}

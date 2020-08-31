package org.tron.program;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.config.args.Args;
import org.tron.core.db.BlockStore;
import org.tron.core.db.Manager;
import org.tron.core.store.AccountStore;


@Component
@Slf4j(topic = "db")
public class ReplayBlock {

  @Autowired
  private BlockStore blockStore;

  @Autowired
  private Manager manager;

  @Autowired
  private AccountStore accountStore;

  public void execute() {
    if (!Args.getInstance().isReplay()) {
      return;
    }

    logger.info("*************** replay start ***************");

    List<BlockCapsule> blockCapsules = blockStore.getBlockByLatestNum(1);
    if (CollectionUtils.isEmpty(blockCapsules)) {
      logger.info("*************** there is not blocks in block store. exit. ***************");
      return;
    }

    long latestNumber = blockCapsules.get(0).getNum();
    for (Map.Entry<byte[], BlockCapsule> entry : blockStore) {
      logger.info("block: {}", entry.getValue().getNum());
      try {
        manager.pushVerifiedBlock(entry.getValue());
      } catch (Exception e) {
        logger.error("Failed to replay block {}, {}.", entry.getValue(), e);
      }
    }

    Args.getInstance().setReplay(false);
    flush();
    logger.info("*************** replay end ***************");
  }

  public void flush() {
    accountStore.flush();
  }
}

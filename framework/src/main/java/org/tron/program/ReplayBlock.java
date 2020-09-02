package org.tron.program;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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
import org.tron.core.store.DynamicPropertiesStore;


@Component
@Slf4j(topic = "db")
public class ReplayBlock {

  @Autowired
  private BlockStore blockStore;

  @Autowired
  private Manager manager;

  @Autowired
  private AccountStore accountStore;

  @Autowired
  private DynamicPropertiesStore dynamicPropertiesStore;

  private Boolean[] processBar = new Boolean[100];

  @PostConstruct
  private void init() {
    Arrays.fill(processBar, Boolean.FALSE);
  }

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
    long currentNumber = dynamicPropertiesStore.getLatestBlockHeaderNumber();
    if (currentNumber >= latestNumber) {
      logger.info("*************** there is not blocks that needed replay. ***************");
      return;
    }

    logger.info("process block {}", currentNumber / latestNumber);

    currentNumber += 1;
    for (; currentNumber <= latestNumber; currentNumber++) {
      List<BlockCapsule> capsules = blockStore.getLimitNumber(currentNumber, 1);
      if (CollectionUtils.isEmpty(capsules)) {
        break;
      }


    }
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

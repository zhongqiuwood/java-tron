package org.tron.common.logsfilter.capsule;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.utils.StringUtil;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.WitnessCapsule;
import org.tron.core.config.Parameter.ChainConstant;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.core.store.WitnessStore;

public class BlockLogTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  private BlockLogTrigger blockLogTrigger;

  public BlockLogTriggerCapsule(BlockCapsule block) {
    blockLogTrigger = new BlockLogTrigger();
    blockLogTrigger.setBlockHash(block.getBlockId().toString());
    blockLogTrigger.setTimeStamp(block.getTimeStamp());
    blockLogTrigger.setBlockNumber(block.getNum());
    blockLogTrigger.setTransactionSize(block.getTransactions().size());
    block.getTransactions().forEach(trx ->
        blockLogTrigger.getTransactionList().add(trx.getTransactionId().toString())
    );
  }

  public void setLatestSolidifiedBlockNumber(long latestSolidifiedBlockNumber) {
    blockLogTrigger.setLatestSolidifiedBlockNumber(latestSolidifiedBlockNumber);
  }

  public void setWitnessPlay(BlockCapsule block, DynamicPropertiesStore dynamicPropertiesStore) {
    blockLogTrigger.setWitnessAddress(StringUtil
        .encode58Check((block.getWitnessAddress().toByteArray())));
    blockLogTrigger.setWitnessPayPerBlock(dynamicPropertiesStore
        .getWitnessPayPerBlock());
    blockLogTrigger.setWitnessMap(block.getWitnessMap());
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postBlockTrigger(blockLogTrigger);
  }
}

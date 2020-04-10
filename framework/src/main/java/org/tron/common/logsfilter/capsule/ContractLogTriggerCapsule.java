package org.tron.common.logsfilter.capsule;

import static org.tron.common.logsfilter.EventPluginLoader.matchFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.ContractEventTrigger;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;
import org.tron.common.logsfilter.trigger.SolidityLogTrigger;

public class ContractLogTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  private ContractLogTrigger contractLogTrigger;

  @Autowired
  private Map<Long, List<SolidityLogTrigger>> contractLogTriggerSet;

  public ContractLogTriggerCapsule(ContractLogTrigger contractLogTrigger) {
    this.contractLogTrigger = contractLogTrigger;
  }

  public void setLatestSolidifiedBlockNumber(long latestSolidifiedBlockNumber) {
    contractLogTrigger.setLatestSolidifiedBlockNumber(latestSolidifiedBlockNumber);
  }

  @Override
  public void processTrigger() {
    if (matchFilter(contractLogTrigger)) {
      EventPluginLoader.getInstance().postContractLogTrigger(contractLogTrigger);
      contractLogTriggerSet.computeIfAbsent(contractLogTrigger
          .getBlockNumber(), listBlk -> new ArrayList<>())
          .add(new SolidityLogTrigger(contractLogTrigger));
    }
  }
}

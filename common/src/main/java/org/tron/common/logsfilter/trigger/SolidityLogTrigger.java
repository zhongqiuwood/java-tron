package org.tron.common.logsfilter.trigger;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class SolidityLogTrigger extends ContractTrigger {

  /**
   * topic list produced by the smart contract LOG function
   */
  @Getter
  @Setter
  private List<String> topicList;

  /**
   * data produced by the smart contract LOG function
   */
  @Getter
  @Setter
  private String data;

  public SolidityLogTrigger() {
    super();
    setTriggerName(Trigger.SOLIDITYLOG_TRIGGER_NAME);
  }
  public SolidityLogTrigger(ContractLogTrigger contractLogTrigger) {
    super(contractLogTrigger);
    setTriggerName(Trigger.SOLIDITYLOG_TRIGGER_NAME);
    this.topicList = contractLogTrigger.getTopicList();
    this.data = contractLogTrigger.getData();
  }
}

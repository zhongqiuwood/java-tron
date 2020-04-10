package org.tron.common.logsfilter.capsule;

import lombok.Getter;
import lombok.Setter;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.SolidityLogTrigger;

public class SolidityLogCapsule extends TriggerCapsule {
  @Getter
  @Setter
  private SolidityLogTrigger solidityLogTrigger;

  public SolidityLogCapsule(SolidityLogTrigger solidityLogTrigger) {
    this.solidityLogTrigger = solidityLogTrigger;
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postSolidityLogTrigger(solidityLogTrigger);
  }
}

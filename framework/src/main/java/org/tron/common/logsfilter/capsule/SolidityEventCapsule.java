package org.tron.common.logsfilter.capsule;

import lombok.Getter;
import lombok.Setter;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.SolidityEventTrigger;
import org.tron.common.logsfilter.trigger.SolidityLogTrigger;

public class SolidityEventCapsule extends TriggerCapsule {
  @Getter
  @Setter
  private SolidityEventTrigger solidityEventTrigger;

  public SolidityEventCapsule(SolidityEventTrigger solidityEventTrigger) {
    this.solidityEventTrigger = solidityEventTrigger;
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postSolidityEventTrigger(solidityEventTrigger);
  }
}

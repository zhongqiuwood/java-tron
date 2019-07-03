package org.tron.core.agent.invariant;

import lombok.Getter;
import lombok.Setter;

public class InvariantInfo {

  @Getter
  @Setter
  private String nodeID;

  @Getter
  @Setter
  private long time;

  @Getter
  @Setter
  private RuntimeInfo runtime;

  @Getter
  @Setter
  private ConfigInfo configInfo;

}

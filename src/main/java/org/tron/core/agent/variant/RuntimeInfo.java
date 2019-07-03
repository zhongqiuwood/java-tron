package org.tron.core.agent.variant;

import lombok.Getter;
import lombok.Setter;

public class RuntimeInfo {

  @Getter
  @Setter
  private int coreNum;

  @Getter
  @Setter
  private long memSize;

  @Getter
  @Setter
  private long jvmMemSize;

  @Getter
  @Setter
  private boolean isGcUseCms;

}

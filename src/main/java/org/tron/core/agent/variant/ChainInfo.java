package org.tron.core.agent.variant;

import lombok.Getter;
import lombok.Setter;

public class ChainInfo {

  @Getter
  @Setter
  private long headBlockNum;

  @Getter
  @Setter
  private long solidBlockNum;

  @Getter
  @Setter
  private String headBlockID;

  @Getter
  @Setter
  private String solidBlockID;

}

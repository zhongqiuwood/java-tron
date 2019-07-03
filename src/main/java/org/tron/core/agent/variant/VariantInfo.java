package org.tron.core.agent.variant;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class VariantInfo {

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
  private ChainInfo chain;

  @Getter
  @Setter
  private List<PeerInfo> peers;

  @Getter
  @Setter
  private List<WitnessInfo> witnesses;

}

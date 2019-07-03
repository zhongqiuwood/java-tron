package org.tron.core.agent.variant;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;

public class WitnessInfo {

  @Getter
  @Setter
  private String witnessAddress;

  @Getter
  @Setter
  private String nodeID;

  @Getter
  @Setter
  private long blockCount;

  @Getter
  @Setter
  private long delay1000;

  @Getter
  @Setter
  private long delay2000;

  @Getter
  @Setter
  private long delay2500;

  @Getter
  @Setter
  private long delay3000;
}

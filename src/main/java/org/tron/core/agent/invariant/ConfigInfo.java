package org.tron.core.agent.invariant;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class ConfigInfo {

  @Getter
  @Setter
  private String tronVersion;

  @Getter
  @Setter
  private String witness;

  @Getter
  @Setter
  private String externalIP;

  @Getter
  @Setter
  private String internalIP;

  @Getter
  @Setter
  private boolean discoverEnable;

  @Getter
  @Setter
  private int port;

  @Getter
  @Setter
  private List<String> activeNodes;

  @Getter
  @Setter
  private List<String> passiveNodes;

  @Getter
  @Setter
  private List<String> fastForwardNodes;

  @Getter
  @Setter
  private int seedNodeSize;

  @Getter
  @Setter
  private int maxConnect;

  @Getter
  @Setter
  private int maxConnectWithSameIP;

  @Getter
  @Setter
  private double connectFactor;

  @Getter
  @Setter
  private double activeConnectFactor;

  @Getter
  @Setter
  private int backupPort;

  @Getter
  @Setter
  private int backupPriority;

  @Getter
  @Setter
  private List<String> backupMembers;

  @Getter
  @Setter
  private int databaseVersion;

  @Getter
  @Setter
  private String databaseEngine;

  @Getter
  @Setter
  private boolean supportConstant;

  @Getter
  @Setter
  private double minTimeRatio;

  @Getter
  @Setter
  private double maxTimeRatio;

  @Getter
  @Setter
  private long allowCreationOfContracts;

  @Getter
  @Setter
  private long allowAdaptiveEnergy;


}

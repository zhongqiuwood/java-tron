package org.tron.core.agent.variant;

import lombok.Getter;
import lombok.Setter;
import org.tron.protos.Protocol.ReasonCode;

public class PeerInfo {

  @Getter
  @Setter
  private String nodeID;

  @Getter
  @Setter
  private String ip;

  @Getter
  @Setter
  private int port;

  @Getter
  @Setter
  private boolean isActive;

  @Getter
  @Setter
  private long onlineTime;

  @Getter
  @Setter
  private long pingCount;

  @Getter
  @Setter
  private long pingMax;

  @Getter
  @Setter
  private long pingAvg;

  @Getter
  @Setter
  private long pingMin;

  @Getter
  @Setter
  private long pingLast;

  @Getter
  @Setter
  private int disconnectCount;

  @Getter
  @Setter
  private ReasonCode remoteDisconnectReason;

  @Getter
  @Setter
  private ReasonCode localDisconnectReason;

  @Getter
  @Setter
  private boolean syncFromPeer;

  @Getter
  @Setter
  private boolean syncFromUs;

  @Getter
  @Setter
  private long remainNum;

}

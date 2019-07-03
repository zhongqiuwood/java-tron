package org.tron.core.agent.variant;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.config.args.Args;
import org.tron.core.net.TronNetDelegate;
import org.tron.core.net.service.FastForwardService;

@Slf4j(topic = "data")
@Component
public class VariantInfoGet {

  private Args args = Args.getInstance();

  @Autowired
  private TronNetDelegate tronNetDelegate;

  @Autowired
  private FastForwardService fastForwardService;


  public VariantInfo getVariantInfo() {
    VariantInfo info = new VariantInfo();
    info.setNodeID(args.getP2pNodeId());
    info.setTime(System.currentTimeMillis());
    info.setRuntime(getRuntimeInfo());
    info.setChain(getChainInfo());
    info.setPeers(getPeerInfo());
    info.setWitnesses(getWitnessInfo());
    return info;
  }

  private RuntimeInfo getRuntimeInfo() {
    RuntimeInfo info = new RuntimeInfo();
    return info;
  }

  private ChainInfo getChainInfo() {
    ChainInfo info = new ChainInfo();
    info.setHeadBlockID(Hex.encodeHexString(tronNetDelegate.getHeadBlockId().getBytes()));
    info.setHeadBlockNum(tronNetDelegate.getHeadBlockId().getNum());
    info.setSolidBlockID(Hex.encodeHexString(tronNetDelegate.getSolidBlockId().getBytes()));
    info.setSolidBlockNum(tronNetDelegate.getSolidBlockId().getNum());
    return info;
  }

  private List<PeerInfo> getPeerInfo() {
    List<PeerInfo> peers = new ArrayList<>();
    tronNetDelegate.getActivePeer().forEach(peer -> {
      PeerInfo info = new PeerInfo();
      info.setNodeID(peer.getNode().getHexId());
      info.setIp(peer.getNode().getHost());
      info.setPort(peer.getNode().getPort());
      info.setActive(peer.isActive());
      info.setOnlineTime(System.currentTimeMillis() - peer.getStartTime());
      info.setPingCount(peer.getNodeStatistics().pingMessageLatency.getCount());
      info.setPingMax(peer.getNodeStatistics().pingMessageLatency.getMax());
      info.setPingAvg(peer.getNodeStatistics().pingMessageLatency.getAvrg());
      info.setPingMin(peer.getNodeStatistics().pingMessageLatency.getMin());
      info.setPingLast(peer.getNodeStatistics().pingMessageLatency.getLast());
      info.setDisconnectCount(peer.getNodeStatistics().getDisconnectTimes());
      info.setRemoteDisconnectReason(peer.getNodeStatistics().getTronLastRemoteDisconnectReason());
      info.setLocalDisconnectReason(peer.getNodeStatistics().getTronLastLocalDisconnectReason());
      info.setSyncFromPeer(peer.isNeedSyncFromPeer());
      info.setSyncFromUs(peer.isNeedSyncFromUs());
      info.setRemainNum(peer.getRemainNum());
      peers.add(info);
    });
    return peers;
  }

  private List<WitnessInfo> getWitnessInfo() {
    List<WitnessInfo> witnesses = new ArrayList<>();
    fastForwardService.getWitnessInfo().forEach((address, witness) -> {
      WitnessInfo info = new WitnessInfo();
      info.setWitnessAddress(address);
      info.setNodeID(witness.getNodeID());
      info.setBlockCount(witness.getBlockCount().get());
      info.setDelay1000(witness.getDelay1000().get());
      info.setDelay2000(witness.getDelay2000().get());
      info.setDelay2500(witness.getDelay2500().get());
      info.setDelay3000(witness.getDelay3000().get());
      witnesses.add(info);
    });
    return witnesses;
  }

}

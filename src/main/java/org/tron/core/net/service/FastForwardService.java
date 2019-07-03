package org.tron.core.net.service;

import com.google.protobuf.ByteString;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.config.args.Args;
import org.tron.core.net.TronNetDelegate;
import org.tron.core.net.message.BlockMessage;
import org.tron.core.net.peer.Item;
import org.tron.core.net.peer.PeerConnection;
import org.tron.protos.Protocol.Inventory.InventoryType;

@Slf4j(topic = "net")
@Component
public class FastForwardService {

  @Autowired
  private TronNetDelegate tronNetDelegate;

  private boolean fastForward = Args.getInstance().isFastForward();

  private ConcurrentHashMap<String, WitnessInfo> witnesses = new ConcurrentHashMap<>();

  private HashSet<InetAddress> fastForwardNodes = new HashSet<>();

  public void init() {
    Args.getInstance().getFastForwardNodes().forEach(node ->
        fastForwardNodes.add(new InetSocketAddress(node.getHost(), node.getPort()).getAddress()));
  }

  public void broadcast(BlockMessage msg) {
    Item item = new Item(msg.getBlockId(), InventoryType.BLOCK);
    List<PeerConnection> peers = tronNetDelegate.getActivePeer().stream()
        .filter(peer -> !peer.isNeedSyncFromPeer() && !peer.isNeedSyncFromUs())
        .filter(peer -> peer.getAdvInvReceive().getIfPresent(item) == null
            && peer.getAdvInvSpread().getIfPresent(item) == null)
        .collect(Collectors.toList());

    if (!fastForward) {
      peers = peers.stream().filter(peer -> peer.isFastForwardPeer()).collect(Collectors.toList());
    }

    peers.forEach(peer -> {
      peer.sendMessage(msg);
      peer.getAdvInvSpread().put(item, System.currentTimeMillis());
      peer.setFastForwardBlock(msg.getBlockId());
    });
  }

  public void processBlockMsg(BlockMessage msg, PeerConnection peer, long delay) {

    InetAddress address = peer.getInetAddress();
    if (fastForwardNodes.contains(address)) {
      return;
    }

    String witnessAddress = Hex.encodeHexString(msg.getBlockId().getBytes());
    WitnessInfo witnessInfo = witnesses.get(witnessAddress);
    if (witnessInfo == null) {
      witnessInfo = new WitnessInfo();
      witnesses.put(witnessAddress, witnessInfo);
    }

    witnessInfo.setNodeID(peer.getNode().getHexId());

    witnessInfo.getBlockCount().incrementAndGet();

    if (delay > 3000) {
      witnessInfo.getDelay3000().incrementAndGet();
    } else if (delay > 2500) {
      witnessInfo.getDelay2500().incrementAndGet();
    } else if (delay > 2000) {
      witnessInfo.getDelay2000().incrementAndGet();
    } else if (delay > 1000) {
      witnessInfo.getDelay1000().incrementAndGet();
    }

    tronNetDelegate.trustNode(peer);
  }

  public Map<String, WitnessInfo> getWitnessInfo() {
    witnesses.keySet().forEach(witness -> {
      if (!tronNetDelegate.getActiveWitnesses().contains(ByteString.copyFrom(witness.getBytes()))) {
        witnesses.remove(witness);
      }
    });
    return witnesses;
  }


  public class WitnessInfo {

    @Getter
    @Setter
    private AtomicLong blockCount = new AtomicLong();

    @Getter
    @Setter
    private AtomicLong delay1000 = new AtomicLong();

    @Getter
    @Setter
    private AtomicLong delay2000 = new AtomicLong();

    @Getter
    @Setter
    private AtomicLong delay2500 = new AtomicLong();

    @Getter
    @Setter
    private AtomicLong delay3000 = new AtomicLong();

    @Getter
    @Setter
    private String nodeID;
  }
}

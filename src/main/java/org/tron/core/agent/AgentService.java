package org.tron.core.agent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.agent.invariant.ConfigInfo;
import org.tron.core.agent.invariant.InvariantInfo;
import org.tron.core.agent.invariant.InvariantInfoGet;
import org.tron.core.agent.variant.ChainInfo;
import org.tron.core.agent.variant.VariantInfo;
import org.tron.core.agent.variant.VariantInfoGet;
import org.tron.core.config.args.Args;

@Slf4j(topic = "data")
@Component
public class AgentService {

  @Autowired
  private InvariantInfoGet invariantInfoGet;

  @Autowired
  private VariantInfoGet variantInfoGet;

  private Args args = Args.getInstance();

  private ScheduledExecutorService logExecutor = Executors.newSingleThreadScheduledExecutor();

  public void start() {
    loggerInvariantInfo();
    logExecutor.scheduleWithFixedDelay(() -> loggerVariantInfo(), 10, 10, TimeUnit.SECONDS);
  }

  public void stop() {
    logExecutor.shutdown();
  }

  private void loggerInvariantInfo() {
    InvariantInfo info = invariantInfoGet.getInvariantInfo();
    StringBuilder builder = new StringBuilder();
    builder.append("\n******************** runtime ********************");
    builder
        .append("\njavaVersion: ").append(info.getRuntime().getJavaVersion())
        .append("\ncoreNum: ").append(info.getRuntime().getCoreNum())
        .append("\nmemSize: ").append(info.getRuntime().getMemSize())
        .append("\njvmMemSize: ").append(info.getRuntime().getJvmMemSize())
        .append("\n");

    ConfigInfo configInfo = info.getConfigInfo();
    builder.append("\n******************** config ********************");
    builder
        .append("\ntronVersion: ").append(configInfo.getTronVersion())
        .append("\nwitness: ").append(configInfo.getWitness())
        .append("\nexternalIP: ").append(configInfo.getExternalIP())
        .append("\ninternalIP: ").append(configInfo.getInternalIP())
        .append("\nport: ").append(configInfo.getPort())
        .append("\nactiveNodes: ").append(configInfo.getActiveNodes())
        .append("\npassiveNodes: ").append(configInfo.getPassiveNodes())
        .append("\nfastForwardNodes: ").append(configInfo.getFastForwardNodes())
        .append("\nseedNodeSize: ").append(configInfo.getSeedNodeSize())
        .append("\nmaxConnect: ").append(configInfo.getMaxConnect())
        .append("\nmaxConnectWithSameIP: ").append(configInfo.getMaxConnectWithSameIP())
        .append("\nconnectFactor: ").append(configInfo.getConnectFactor())
        .append("\nactiveConnectFactor: ").append(configInfo.getActiveConnectFactor())
        .append("\nbackupPort: ").append(configInfo.getBackupPort())
        .append("\nbackupPriority: ").append(configInfo.getBackupPriority())
        .append("\nbackupMembers: ").append(configInfo.getBackupMembers())
        .append("\ndatabaseVersion: ").append(configInfo.getDatabaseVersion())
        .append("\ndatabaseEngine: ").append(configInfo.getDatabaseEngine())
        .append("\nsupportConstant: ").append(configInfo.isSupportConstant())
        .append("\nminTimeRatio: ").append(configInfo.getMinTimeRatio())
        .append("\nmaxTimeRatio: ").append(configInfo.getMaxTimeRatio())
        .append("\nallowCreationOfContracts: ").append(configInfo.getAllowCreationOfContracts())
        .append("\nallowAdaptiveEnergy: ").append(configInfo.getAllowAdaptiveEnergy())
        .append("\n");

    logger.info(builder.toString());
  }

  private void loggerVariantInfo() {
    VariantInfo info = variantInfoGet.getVariantInfo();
    StringBuilder builder = new StringBuilder();
    ChainInfo chainInfo = info.getChain();
    builder.append("\n******************** chain ********************");
    builder
        .append("\nhead block:  ").append(chainInfo.getHeadBlockNum()).append(" ")
        .append(chainInfo.getHeadBlockID())
        .append("\nsolid block: ").append(chainInfo.getSolidBlockNum()).append(" ")
        .append(chainInfo.getSolidBlockID())
        .append("\n");

    builder.append("\n******************** peers ********************");
    info.getPeers().forEach(peer ->
      builder
          .append("\nip/port: ").append(peer.getIp()).append(":").append(peer.getPort())
          .append("\nonline time: ").append(peer.getOnlineTime() / 1000).append("s")
          .append("\nping count: ").append(peer.getPingCount()).append(", delay max-avg-min-last: ")
          .append(peer.getPingMax()).append(" ").append(peer.getPingAvg()).append(" ")
          .append(peer.getPingMin()).append(" ").append(peer.getPingLast())
          .append("\nsync from peer-us: ").append(peer.isSyncFromPeer()).append(" ")
          .append(peer.isSyncFromUs()).append(", remain: ").append(peer.getRemainNum())
          .append("\ndisconnect count: ").append(peer.getDisconnectCount()).append(", reason remote-local: ")
          .append(peer.getRemoteDisconnectReason()).append(" ").append(peer.getLocalDisconnectReason())
          .append("\n"));

    if (args.isFastForward()) {
      builder.append("\n******************** witness ********************");
      info.getWitnesses().forEach(witness ->
      builder
          .append("\nwitness: ").append(witness.getWitnessAddress())
          .append("\nblock count: ").append(witness.getBlockCount())
          .append(", delay 1000-2000-25000-3000: ")
          .append(witness.getDelay1000()).append(" ")
          .append(witness.getDelay2000()).append(" ")
          .append(witness.getDelay2500()).append(" ")
          .append(witness.getDelay3000())
          .append("\n"));
    }

    logger.info(builder.toString());
  }

}

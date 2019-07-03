package org.tron.core.agent.invariant;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;
import org.tron.common.overlay.discover.node.Node;
import org.tron.core.config.args.Args;
import org.tron.program.Version;

@Slf4j(topic = "agent")
@Component
public class InvariantInfoGet {

  private MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
  private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
  private OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory
      .getOperatingSystemMXBean();
  private Args args = Args.getInstance();

  public InvariantInfo getInvariantInfo() {
    InvariantInfo info = new InvariantInfo();
    info.setNodeID(args.getP2pNodeId());
    info.setTime(System.currentTimeMillis());
    info.setRuntime(getRuntimeInfo());
    info.setConfigInfo(getConfigInfo());
    return info;
  }

  private RuntimeInfo getRuntimeInfo() {
    RuntimeInfo info = new RuntimeInfo();
    info.setJavaVersion(runtimeMXBean.getSystemProperties().get("java.version"));
    info.setCoreNum(Runtime.getRuntime().availableProcessors());
    info.setMemSize(operatingSystemMXBean.getTotalPhysicalMemorySize());
    info.setGcUseCms(isGcUserCms());
    info.setJvmMemSize(memoryMXBean.getHeapMemoryUsage().getMax());
    return info;
  }

  private ConfigInfo getConfigInfo() {
    ConfigInfo info = new ConfigInfo();
    info.setTronVersion(Version.getVersion());
    info.setExternalIP(args.getNodeExternalIp());
    info.setInternalIP(args.getNodeDiscoveryBindIp());
    info.setWitness(Hex.encodeHexString(args.getLocalWitnesses().getWitnessAccountAddress()));
    info.setDiscoverEnable(args.isNodeDiscoveryEnable());
    info.setPort(args.getNodeListenPort());
    info.setActiveNodes(getNodes(args.getActiveNodes()));
    info.setPassiveNodes(getNodes(args.getPassiveNodes()));
    info.setFastForwardNodes(getNodes(args.getFastForwardNodes()));
    info.setSeedNodeSize(args.getSeedNodes().size());
    info.setMaxConnect(args.getNodeMaxActiveNodes());
    info.setMaxConnectWithSameIP(args.getNodeMaxActiveNodesWithSameIp());
    info.setConnectFactor(args.getConnectFactor());
    info.setActiveConnectFactor(args.getActiveConnectFactor());
    info.setBackupPort(args.getBackupPort());
    info.setBackupPriority(args.getBackupPriority());
    info.setBackupMembers(args.getBackupMembers());
    info.setDatabaseVersion(args.getStorage().getDbVersion());
    info.setDatabaseEngine(args.getStorage().getDbEngine());
    info.setSupportConstant(args.isSupportConstant());
    info.setMinTimeRatio(args.getMinTimeRatio());
    info.setMaxTimeRatio(args.getMaxTimeRatio());
    info.setAllowCreationOfContracts(args.getAllowCreationOfContracts());
    info.setAllowAdaptiveEnergy(args.getAllowAdaptiveEnergy());
    return info;
  }

  private boolean isGcUserCms() {
    try {
      String[] cmd = { "sh", "-c", "ps -ef|grep UseConcMarkSweepGC|grep -v grep" };
      Process p = Runtime.getRuntime().exec(cmd);
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      boolean flag = br.read() > 0;
      if(br!=null){
        br.close();
      }
      p.destroy();
      return flag;
    } catch (Exception e) {
      logger.error("{}", e);
      return false;
    }
  }

  private List<String> getNodes (List<Node> nodes) {
    ArrayList<String> list = new ArrayList<>();
    nodes.forEach(node -> list.add(node.getHost() + ":" + node.getPort()));
    return list;
  }

}

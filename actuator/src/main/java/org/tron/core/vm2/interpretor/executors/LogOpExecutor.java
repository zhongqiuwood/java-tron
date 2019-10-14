package org.tron.core.vm2.interpretor.executors;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.tron.common.runtime.vm.DataWord;
import org.tron.common.runtime.vm.LogInfo;
import org.tron.core.vm.program.Stack;
import org.tron.core.vm2.ContractContext;
import org.tron.core.vm2.interpretor.Costs;
import org.tron.core.vm2.interpretor.Op;

public class LogOpExecutor extends OpExecutor {

  private static LogOpExecutor INSTANCE = new LogOpExecutor();

  private LogOpExecutor() {
  }

  public static LogOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {
    int nTopics = op.val() - Op.LOG0.val();
    Stack stack = context.getStack();
    DataWord address = context.getContractAddress();
    DataWord memStart = stack.pop();
    DataWord dataSize = stack.pop();
    //is it nessary?
/*    BigInteger dataCost = dataSize.value()
        .multiply(BigInteger.valueOf(Costs.LOG_DATA_ENERGY));

    if (context.getContractBase().getEnergyLimitLeft().value().compareTo(dataCost) < 0) {
      throw new org.tron.common.runtime.vm.program.Program.OutOfEnergyException(
          "Not enough energy for '%s' operation executing: opEnergy[%d], programEnergy[%d]",
          op.name(),
          dataCost.longValueExact(),
          context.getContractBase().getEnergyLimitLeft().longValueSafe());
    }*/
    BigInteger memNeeded = memNeeded(memStart, dataSize);
    long energyCost = Costs.LOG_ENERGY
        + Costs.LOG_TOPIC_ENERGY * nTopics
        + Costs.LOG_DATA_ENERGY * dataSize.longValue()
        + calcMemEnergy(context.getMemory().size(),
        memNeeded, 0, op);

    context.spendEnergy(energyCost, op.name());
    checkMemorySize(op, memNeeded);

    List<DataWord> topics = new ArrayList<>();
    for (int i = 0; i < nTopics; ++i) {
      DataWord topic = stack.pop();
      topics.add(topic);
    }

    byte[] data = context.memoryChunk(memStart.intValueSafe(), dataSize.intValueSafe());

    LogInfo logInfo =
        new LogInfo(address.getLast20Bytes(), topics, data);

    context.getContractBase().getProgramResult().addLogInfo(logInfo);
    context.step();

  }
}

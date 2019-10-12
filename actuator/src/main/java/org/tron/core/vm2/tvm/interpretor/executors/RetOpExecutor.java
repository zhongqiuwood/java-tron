package org.tron.core.vm2.tvm.interpretor.executors;


import org.tron.common.runtime.vm.DataWord;
import org.tron.core.vm2.tvm.ContractContext;
import org.tron.core.vm2.tvm.interpretor.Costs;
import org.tron.core.vm2.tvm.interpretor.Op;

public class RetOpExecutor extends OpExecutor {

  private static RetOpExecutor INSTANCE = new RetOpExecutor();

  private RetOpExecutor() {
  }

  public static RetOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {

    DataWord offset = context.stackPop();
    DataWord size = context.stackPop();
    long energyCost = Costs.STOP + calcMemEnergy(context.getMemory().size(),
        memNeeded(offset, size), 0, op);
    context.spendEnergy(energyCost, op.name());
    byte[] hReturn = context.memoryChunk(offset.intValueSafe(), size.intValueSafe());
    context.getContractBase().getProgramResult().setHReturn(hReturn);
    context.step();
    context.stop();

    if (op == Op.REVERT) {
      context.getContractBase().getProgramResult().setRevert();
    }

  }
}

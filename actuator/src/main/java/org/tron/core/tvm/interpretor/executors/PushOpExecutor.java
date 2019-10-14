package org.tron.core.tvm.interpretor.executors;


import org.tron.core.tvm.ContractContext;
import org.tron.core.tvm.interpretor.Op;
import org.tron.core.tvm.interpretor.Op.Tier;

public class PushOpExecutor extends OpExecutor {

  private static PushOpExecutor INSTANCE = new PushOpExecutor();

  private PushOpExecutor() {
  }

  public static PushOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {
    context.spendEnergy(Tier.VeryLowTier.asInt(), op.name());

    context.step();
    int nPush = op.val() - Op.PUSH1.val() + 1;
    byte[] data = context.sweep(nPush);

    context.stackPush(data);

  }
}

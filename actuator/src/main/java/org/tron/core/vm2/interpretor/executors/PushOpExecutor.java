package org.tron.core.vm2.interpretor.executors;


import org.tron.core.vm2.ContractContext;
import org.tron.core.vm2.interpretor.Op;
import org.tron.core.vm2.interpretor.Op.Tier;

public class PushOpExecutor implements OpExecutor {

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

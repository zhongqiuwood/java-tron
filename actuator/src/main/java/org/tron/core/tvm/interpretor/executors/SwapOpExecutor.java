package org.tron.core.tvm.interpretor.executors;


import org.tron.core.tvm.ContractContext;
import org.tron.core.tvm.interpretor.Op;
import org.tron.core.tvm.interpretor.Op.Tier;
import org.tron.core.vm.program.Stack;

public class SwapOpExecutor extends OpExecutor {

  private static SwapOpExecutor INSTANCE = new SwapOpExecutor();

  private SwapOpExecutor() {
  }

  public static SwapOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {
    context.spendEnergy(Tier.VeryLowTier.asInt(), op.name());

    Stack stack = context.getStack();
    int n = op.val() - Op.SWAP1.val() + 2;
    stack.swap(stack.size() - 1, stack.size() - n);
    context.step();
  }
}

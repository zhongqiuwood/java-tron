package org.tron.core.tvm.interpretor.executors;


import static org.tron.core.tvm.interpretor.Op.DUP1;

import org.tron.common.runtime.vm.DataWord;
import org.tron.core.tvm.ContractContext;
import org.tron.core.tvm.interpretor.Op;
import org.tron.core.tvm.interpretor.Op.Tier;

public class DupOpExecutor extends OpExecutor {

  private static DupOpExecutor INSTANCE = new DupOpExecutor();

  private DupOpExecutor() {
  }

  public static DupOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {
    context.spendEnergy(Tier.VeryLowTier.asInt(), op.name());

    int n = op.val() - DUP1.val() + 1;
    DataWord word1 = context.getStack().get(context.getStack().size() - n);
    context.stackPush(word1.clone());
    context.step();

  }
}

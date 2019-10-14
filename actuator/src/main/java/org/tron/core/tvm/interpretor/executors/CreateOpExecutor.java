package org.tron.core.tvm.interpretor.executors;


import org.tron.common.runtime.vm.DataWord;
import org.tron.core.tvm.ContractContext;
import org.tron.core.tvm.interpretor.Costs;
import org.tron.core.tvm.interpretor.Op;

public class CreateOpExecutor extends OpExecutor {

  private static CreateOpExecutor INSTANCE = new CreateOpExecutor();

  private CreateOpExecutor() {
  }

  public static CreateOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {
    DataWord value = context.stackPop();
    DataWord inOffset = context.stackPop();
    DataWord inSize = context.stackPop();
    DataWord salt = null;
    boolean isCreate2 = false;

    long energyCost =
        Costs.CREATE + calcMemEnergy(context.getMemory().size(), memNeeded(inOffset, inSize), 0,
            op);

    if (op == Op.CREATE2) {
      isCreate2 = true;
      salt = context.stackPop();
      energyCost += DataWord.sizeInWords(inSize.intValueSafe()) * Costs.SHA3_WORD;
    }

    context.spendEnergy(energyCost, op.name());
    context.createContract(value, inOffset, inSize, salt, isCreate2);
    context.step();

  }
}

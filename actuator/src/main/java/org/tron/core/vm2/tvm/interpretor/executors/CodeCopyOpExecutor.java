package org.tron.core.vm2.tvm.interpretor.executors;

import static org.tron.common.utils.ByteUtil.EMPTY_BYTE_ARRAY;

import org.tron.common.runtime.vm.DataWord;
import org.tron.core.vm2.tvm.ContractContext;
import org.tron.core.vm2.tvm.interpretor.Op;

public class CodeCopyOpExecutor extends OpExecutor {

  private static CodeCopyOpExecutor INSTANCE = new CodeCopyOpExecutor();

  private CodeCopyOpExecutor() {
  }

  public static CodeCopyOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {

    byte[] fullCode = EMPTY_BYTE_ARRAY;
    if (op == Op.CODECOPY) {
      fullCode = context.getContractBase().getCode();
    } else {
      DataWord address = context.stackPop();
      fullCode = context.getCodeAt(address);
    }

    DataWord memOffsetd = context.stackPop();
    DataWord codeOffsetd = context.stackPop();
    DataWord lengthDatad = context.stackPop();

    context.spendEnergy(
        calcMemEnergy(context.getMemory().size(),
            memNeeded(memOffsetd, lengthDatad),
            lengthDatad.longValueSafe(), op),
        op.name()
    );
    int memOffset = memOffsetd.intValueSafe();
    int codeOffset = codeOffsetd.intValueSafe();
    int lengthData = lengthDatad.intValueSafe();

    int sizeToBeCopied =
        (long) codeOffset + lengthData > fullCode.length
            ? (fullCode.length < codeOffset ? 0 : fullCode.length - codeOffset)
            : lengthData;

    byte[] codeCopy = new byte[lengthData];

    if (codeOffset < fullCode.length) {
      System.arraycopy(fullCode, codeOffset, codeCopy, 0, sizeToBeCopied);
    }

    context.memorySave(memOffset, codeCopy);
    context.step();

  }
}

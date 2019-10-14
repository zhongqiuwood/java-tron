package org.tron.core.vm2.interpretor.executors;


import java.math.BigInteger;
import org.tron.common.runtime.vm.DataWord;
import org.tron.core.vm.MessageCall;
import org.tron.core.vm.PrecompiledContracts;
import org.tron.core.vm.program.Program;
import org.tron.core.vm2.ContractContext;
import org.tron.core.vm2.interpretor.Costs;
import org.tron.core.vm2.interpretor.Op;

public class CallOpExecutor extends OpExecutor {

  private static CallOpExecutor INSTANCE = new CallOpExecutor();

  private CallOpExecutor() {
  }

  public static CallOpExecutor getInstance() {
    return INSTANCE;
  }


  @Override
  public void exec(Op op, ContractContext context) {
    long oldMemSize = context.getMemory().size();
    long energyCost = Costs.CALL;
    DataWord callEnergyWord = context.stackPop();
    DataWord codeAddress = context.stackPop();
    DataWord value;
    if (op == Op.CALL || op == Op.CALLCODE || op == Op.CALLTOKEN) {
      value = context.stackPop();
    } else {
      value = DataWord.ZERO.clone();
    }
    if (op == Op.CALL || op == Op.CALLTOKEN) {
      if (context.isDeadAccount(codeAddress) && !value.isZero()) {
        energyCost += Costs.NEW_ACCT_CALL;
      }
    }
    if (!value.isZero()) {
      energyCost += Costs.VT_CALL;
    }
    DataWord tokenId = new DataWord(0);

    boolean isTokenTransferMsg = false;
    if (op == Op.CALLTOKEN) {
      tokenId = context.stackPop();
      isTokenTransferMsg = true;
    }

    DataWord inDataOffs = context.stackPop();
    DataWord inDataSize = context.stackPop();
    DataWord outDataOffs = context.stackPop();
    DataWord outDataSize = context.stackPop();

    BigInteger in = memNeeded(inDataOffs, inDataSize); // in offset+size
    BigInteger out = memNeeded(outDataOffs, outDataSize);// out offset+size

    energyCost += calcMemEnergy(oldMemSize, in.max(out), 0, op);

    context.memoryExpand(outDataOffs, outDataSize);
    DataWord getEnergyLimitLeft = context.getContractBase().getEnergyLimitLeft().clone();
    getEnergyLimitLeft.sub(new DataWord(energyCost));

    DataWord adjustedCallEnergy = context.getCallEnergy(callEnergyWord, getEnergyLimitLeft);

    energyCost += adjustedCallEnergy.longValueSafe();
    context.spendEnergy(energyCost, op.name());

    if (context.getContractBase().isStatic()
        && (op == Op.CALL || op == Op.CALLTOKEN) && !value.isZero()) {
      throw new Program.StaticCallModificationException();
    }

    if (!value.isZero()) {
      adjustedCallEnergy.add(new DataWord(Costs.STIPEND_CALL));
    }

    MessageCall msg = new MessageCall(
        op, adjustedCallEnergy, codeAddress, value, inDataOffs, inDataSize,
        outDataOffs, outDataSize, tokenId, isTokenTransferMsg);

    PrecompiledContracts.PrecompiledContract contract =
        PrecompiledContracts.getContractForAddress(codeAddress);

    if (op != Op.CALLCODE && op != Op.DELEGATECALL) {
      context.getContractBase().getProgramResult()
          .addTouchAccount(codeAddress.getLast20Bytes());
    }

    if (contract != null) {
      context.callToPrecompiledAddress(msg, contract);
    } else {
      context.callToAddress(msg);
    }

    context.step();


  }
}

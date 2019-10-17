package org.tron.core.vm2;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.vm.config.VMConfig;
import org.tron.core.vm.program.Program;
import org.tron.core.vm2.interpretor.Op;


@Slf4j(topic = "VM2")
public class Interpreter2 {
  private static final BigInteger _32_ = BigInteger.valueOf(32);
  public static final String ADDRESS_LOG = "address: ";
  private static final String ENERGY_LOG_FORMATE = "{} Op:[{}]  Energy:[{}] Deep:[{}] Hint:[{}]";


  private static class InterpreterInstance {
    private static final Interpreter2 instance = new Interpreter2();
  }

  public static Interpreter2 getInstance() {
    return InterpreterInstance.instance;
  }


  public void play(ContractContext env) {
    if (isNotEmpty(env.getContractBase().getOps())) {
      while (!env.isStopped()) {
        this.step(env);
      }
    }
  }


  public void step(ContractContext context) {

    if (VMConfig.vmTrace()) {
      context.saveOpTrace();
      String hint = new StringBuilder().append("exec:")
          .append(Op.code(context.getCurrentOp()).name())
          .append(" stack:").append(context.getStack().size())
          .append(" mem:").append(context.getMemory().size())
          .append(" pc:").append(context.getPC())
          .append(" stacktop:").append(context.getStack().safepeek())
          .append(" energy:")
          .append(context.getContractBase().getProgramResult().getEnergyUsed()).toString();
      context.getContractBase().addOpHistory(hint);
    }

    try {
      Op op = Op.code(context.getCurrentOp());
      if (op == null) {
        throw Program.Exception
            .invalidOpCode(context.getCurrentOp());
      }
      context.setLastOp(op.val());
      context.verifyStackSize(op.require());
      //check not exceeding stack limits
      context.verifyStackOverflow(op.require(), op.ret());
      //spend energy
      //checkcpu limit
      context.checkCPUTimeLimit(op.name());
      //step
      op.getOpExecutor().exec(op, context);
      context.setPreviouslyExecutedOp(op.val());


    } catch (RuntimeException e) {
      logger.info("VM halted: [{}]", e.getMessage());
      if (!(e instanceof Program.TransferException)) {
        context.spendAllEnergy();
      }
      context.resetFutureRefund();
      context.stop();
      throw e;
    }
  }



}

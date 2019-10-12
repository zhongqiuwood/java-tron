package org.tron.core.vm2.tvm;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.vm.program.Program;
import org.tron.core.vm2.tvm.interpretor.Op;


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


  public void step(ContractContext env) {
    try {
      Op op = Op.code(env.getCurrentOp());
      if (op == null) {
        throw Program.Exception
            .invalidOpCode(env.getCurrentOp());
      }
      env.setLastOp(op.val());
      env.verifyStackSize(op.require());
      //check not exceeding stack limits
      env.verifyStackOverflow(op.require(), op.ret());
      //spend energy
      //checkcpu limit
      env.checkCPUTimeLimit(op.name());
      //step
      op.getOpExecutor().exec(op,env);
      env.setPreviouslyExecutedOp(op.val());
      String hint =
          "exec:" + op.name() + " stack:" + env.getStack().size() + " mem:" + env.getMemory().size()
              + " pc:" + env.getPC() + " stacktop:" + env.getStack().safepeek() + " ene:" + env
              .getContractBase().getProgramResult().getEnergyUsed();
      env.getContractBase().addOpHistory(hint);

    } catch (RuntimeException e) {
      logger.info("VM halted: [{}]", e.getMessage());
      if (!(e instanceof Program.TransferException)) {
        env.spendAllEnergy();
      }
      env.resetFutureRefund();
      env.stop();
      throw e;
    }
  }



}

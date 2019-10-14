package org.tron.core.tvm;

import org.tron.common.runtime.vm.program.InternalTransaction;
import org.tron.common.storage.Deposit;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.vm2.config.VMConfig;

public class VMFactory {

  private VMFactory() {
  }

  public static IVM createTVM(
      VMConfig config, TransactionCapsule trx, BlockCapsule block, Deposit deposit) {
    TVM tvm = new TVM(trx, block, deposit);
    tvm.setVmConfig(config);
    tvm.setStatic(false);
    return tvm;
  }

  public static IVM createTVMForWallet(
      VMConfig config, TransactionCapsule trx, BlockCapsule block, Deposit deposit) {
    TVM tvm = new TVM(trx, block, deposit);
    tvm.setVmConfig(config);
    tvm.setExecutorType(InternalTransaction.ExecutorType.ET_PRE_TYPE);
    tvm.setStatic(true);
    return tvm;
  }
}

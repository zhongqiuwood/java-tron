package org.tron.core.vm2;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.logsfilter.trigger.ContractTrigger;
import org.tron.common.runtime.InternalTransaction;
import org.tron.common.runtime.ProgramResult;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DBConfig;
import org.tron.core.Constant;
import org.tron.core.actuator.Actuator2;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.ContractCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.EnergyProcessor;
import org.tron.core.db.TransactionContext;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.TypeMismatchNamingException;
import org.tron.core.exception.VMIllegalException;
import org.tron.core.store.AccountStore;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.core.store.StoreFactory;
import org.tron.core.vm.LogInfoTriggerParser;
import org.tron.core.vm.config.VMConfig;
import org.tron.core.vm.program.Program;
import org.tron.core.vm.repository.Repository;
import org.tron.core.vm.repository.RepositoryImpl;
import org.tron.protos.Protocol;
import org.tron.protos.contract.SmartContractOuterClass.CreateSmartContract;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
import org.tron.protos.contract.SmartContractOuterClass.TriggerSmartContract;

@Slf4j(topic = "VM2")
public class TVM implements Actuator2 {


  private EnergyProcessor energyProcessor;

  private BlockCapsule blockCap;

  private TransactionCapsule trx;

  private InternalTransaction.TrxType trxType;

  private Repository repository;

  private LogInfoTriggerParser logInfoTriggerParser;

  private ProgramResult result;
  @Setter
  private boolean isStatic;

  @Setter
  private boolean enableEventLinstener;

  @Setter
  private InternalTransaction.ExecutorType executorType;

  public TVM(boolean isStatic) {
    this.isStatic = isStatic;
    this.repository = RepositoryImpl.createRoot(StoreFactory.getInstance());
  }

  private EnergyProcessor initEnergyProcessor() throws ContractValidateException {
    StoreFactory storeFactory = StoreFactory.getInstance();
    try {
      return new EnergyProcessor(storeFactory.getStore(DynamicPropertiesStore.class),
          storeFactory.getStore(
              AccountStore.class));
    } catch (TypeMismatchNamingException e) {
      throw new ContractValidateException("initEnergyProcessor error");
    }
  }

  @Override
  public void validate(TransactionContext context) throws ContractValidateException {

    enableEventLinstener = context.isEventPluginLoaded();

    this.trx = context.getTrxCap();
    this.blockCap = context.getBlockCap();
    this.energyProcessor = initEnergyProcessor();

    if (Objects.nonNull(blockCap)) {
      this.executorType = InternalTransaction.ExecutorType.ET_NORMAL_TYPE;
    } else {
      this.blockCap = new BlockCapsule(Protocol.Block.newBuilder().build());
      this.executorType = InternalTransaction.ExecutorType.ET_PRE_TYPE;

    }

    Protocol.Transaction.Contract.ContractType contractType
        = this.trx.getInstance().getRawData().getContract(0).getType();
    switch (contractType.getNumber()) {
      case Protocol.Transaction.Contract.ContractType.TriggerSmartContract_VALUE:
        trxType = InternalTransaction.TrxType.TRX_CONTRACT_CALL_TYPE;
        break;
      default:
        trxType = InternalTransaction.TrxType.TRX_CONTRACT_CREATION_TYPE;
    }
  }


  @Override
  public void execute(TransactionContext context) {
    //Validate and getBaseProgram
    ContractBase program = null;
    try {
      program = preValidateAndGetBaseProgram(isStatic);
      //setup program environment and play
      ContractContext env = ContractContext
          .createEnvironment(repository, program).setEnableInterpreter2(true)
          .execute();
      //process result
      processResult(env, isStatic);
      context.setProgramResult(env.getContractBase().getProgramResult());

    } catch (ContractValidateException e) {
      e.printStackTrace();
    } catch (VMIllegalException e) {
      e.printStackTrace();
    }

  }

  private void processResult(ContractContext context, boolean isStatic) {
    ContractBase program = context.getContractBase();
    result =  program.getProgramResult();
    // for static call don't processResult
    if (isStatic) {
      return;
    }
    //
    if (result.getException() != null || result.isRevert()) {
      result.getDeleteAccounts().clear();
      result.getLogInfoList().clear();
      result.resetFutureRefund();
      result.rejectInternalTransactions();

      if (result.getException() != null) {
        if (!(result.getException()
            instanceof Program.TransferException)) {
          context.spendAllEnergy();
        }
      } else {
        result.setRuntimeError("REVERT opcode executed");
      }
    } else {
      repository.commit();

      if (logInfoTriggerParser != null) {
        List<ContractTrigger> triggers = logInfoTriggerParser
            .parseLogInfos(program.getProgramResult().getLogInfoList(), this.repository);
        program.getProgramResult().setTriggerList(triggers);
      }
    }
    //trace.setBill(result.getEnergyUsed());

  }

  private void loadEventPlugin(ContractBase program) {
    if (enableEventLinstener && isCheckTransaction()) {
      logInfoTriggerParser = new LogInfoTriggerParser(blockCap.getNum(), blockCap.getTimeStamp(),
              program.getRootTransactionId(), program.getCallerAddress());
    }
  }


  ContractBase preValidateAndGetBaseProgram(boolean isStatic)
      throws ContractValidateException, VMIllegalException {
    ContractBase program = new ContractBase();
    program.setTrxType(trxType);
    if (trxType == InternalTransaction.TrxType.TRX_CONTRACT_CREATION_TYPE) {
      CreateSmartContract contract =
          ContractCapsule.getSmartContractFromTransaction(trx.getInstance());
      if (contract == null) {
        throw new ContractValidateException("Cannot get CreateSmartContract from transaction");
      }

      SmartContract newSmartContract = contract.getNewContract();
      if (!contract.getOwnerAddress().equals(newSmartContract.getOriginAddress())) {
        logger.info("OwnerAddress not equals OriginAddress");
        throw new VMIllegalException("OwnerAddress is not equals OriginAddress");
      }

      byte[] contractName = newSmartContract.getName().getBytes();

      if (contractName.length > VMConstant.CONTRACT_NAME_LENGTH) {
        throw new ContractValidateException("contractName's length cannot be greater than 32");
      }

      long percent = contract.getNewContract().getConsumeUserResourcePercent();
      if (percent < 0 || percent > VMConstant.ONE_HUNDRED) {
        throw new ContractValidateException("percent must be >= 0 and <= 100");
      }
      AccountCapsule creator =
          this.repository.getAccount(newSmartContract.getOriginAddress().toByteArray());
      byte[] callerAddress = contract.getOwnerAddress().toByteArray();


      program.setCallValue(newSmartContract.getCallValue());
      program.setTokenId(contract.getTokenId());
      program.setTokenValue(contract.getCallTokenValue());
      program.setCreator(creator);
      program.setCallerAddress(callerAddress);
      program.setOps(newSmartContract.getBytecode().toByteArray());
      program.setOrigin(contract.getOwnerAddress().toByteArray());
      program.setMsgData(ByteUtil.EMPTY_BYTE_ARRAY);


    } else { // TRX_CONTRACT_CALL_TYPE
      TriggerSmartContract contract =
          ContractCapsule.getTriggerContractFromTransaction(trx.getInstance());
      if (contract.getContractAddress() == null) {
        throw new ContractValidateException("Cannot get contract address from TriggerContract");
      }
      byte[] contractAddress = contract.getContractAddress().toByteArray();

      ContractCapsule deployedContract = this.repository.getContract(contractAddress);
      if (null == deployedContract) {
        logger.info("No contract or not a smart contract");
        throw new ContractValidateException("No contract or not a smart contract");
      }
      AccountCapsule creator = this.repository
              .getAccount(deployedContract.getInstance().getOriginAddress().toByteArray());
      byte[] callerAddress = contract.getOwnerAddress().toByteArray();
      AccountCapsule caller = this.repository.getAccount(callerAddress);


      program.setCallValue(contract.getCallValue());
      program.setTokenId(contract.getTokenId());
      program.setTokenValue(contract.getCallTokenValue());
      program.setCreator(creator);
      program.setCallerAddress(callerAddress);
      program.setCaller(caller);
      program.setContractAddress(contractAddress);
      program.setOps(repository.getCode(contractAddress));
      program.setOrigin(contract.getOwnerAddress().toByteArray());
      program.setMsgData(contract.getData().toByteArray());


    }
    //setBlockInfo
    setBlockInfo(program);

    //calculateEnergyLimit
    long energylimt = calculateEnergyLimit(
        program.getCreator(), program.getCaller(), program.getContractAddress(), isStatic,
        program.getCallValue());
    program.setEnergyLimit(energylimt);
    //maxCpuTime
    long maxCpuTimeOfOneTx = repository.getDynamicPropertiesStore()
        .getMaxCpuTimeOfOneTx()
            * VMConstant.ONE_THOUSAND;
    long thisTxCPULimitInUs = (long) (maxCpuTimeOfOneTx * getCpuLimitInUsRatio());
    long vmStartInUs = System.nanoTime() / VMConstant.ONE_THOUSAND;
    long vmShouldEndInUs = vmStartInUs + thisTxCPULimitInUs;
    program.setVmStartInUs(vmStartInUs);
    program.setVmShouldEndInUs(vmShouldEndInUs);
    program.setStatic(isStatic);

    //set rootTransaction

    byte[] txId = new TransactionCapsule(trx.getInstance()).getTransactionId().getBytes();
    program.setRootTransactionId(txId);
    program.setInternalTransaction(new InternalTransaction(trx.getInstance(), trxType));

    //load eventPlugin
    loadEventPlugin(program);



    return program;
  }

  private void setBlockInfo(ContractBase contractBase) {
    Protocol.Block block = blockCap.getInstance();
    byte[] lastHash = block.getBlockHeader().getRawDataOrBuilder().getParentHash().toByteArray();
    byte[] coinbase = block.getBlockHeader().getRawDataOrBuilder().getWitnessAddress()
            .toByteArray();
    long timestamp = block.getBlockHeader().getRawDataOrBuilder().getTimestamp() / 1000;
    long number = block.getBlockHeader().getRawDataOrBuilder().getNumber();
    contractBase.getBlockInfo().setCoinbase(coinbase);
    contractBase.getBlockInfo().setLastHash(lastHash);
    contractBase.getBlockInfo().setNumber(number);
    contractBase.getBlockInfo().setTimestamp(timestamp);


  }


  private long calculateEnergyLimit(AccountCapsule creator, AccountCapsule caller,
      byte[] contractAddress, boolean isStatic, long callValue)
      throws ContractValidateException {
    long energyLimit = 0;
    long rawfeeLimit = trx.getInstance().getRawData().getFeeLimit();
    if (rawfeeLimit < 0 || rawfeeLimit > VMConfig.MAX_FEE_LIMIT) {
      logger.info("invalid feeLimit {}", rawfeeLimit);
      throw new ContractValidateException(
          "feeLimit must be >= 0 and <= " + VMConfig.MAX_FEE_LIMIT);
    }
    if (trxType == InternalTransaction.TrxType.TRX_CONTRACT_CREATION_TYPE) {
      energyLimit = getAccountEnergyLimitWithFixRatio(creator, rawfeeLimit, callValue);
    } else { // TRX_CONTRACT_CALL_TYPE
      if (isStatic) {
        energyLimit = Constant.ENERGY_LIMIT_IN_CONSTANT_TX;
      } else {
        energyLimit = getTotalEnergyLimit(creator, caller, contractAddress, rawfeeLimit, callValue);
      }
    }
    return energyLimit;
  }


  private long getAccountEnergyLimitWithFixRatio(AccountCapsule account, long feeLimit,
                                                 long callValue) {

    long sunPerEnergy = Constant.SUN_PER_ENERGY;
    if (repository.getDynamicPropertiesStore().getEnergyFee() > 0) {
      sunPerEnergy = repository.getDynamicPropertiesStore().getEnergyFee();
    }

    long leftFrozenEnergy = energyProcessor.getAccountLeftEnergyFromFreeze(account);

    long energyFromBalance = max(account.getBalance() - callValue, 0) / sunPerEnergy;
    long availableEnergy = Math.addExact(leftFrozenEnergy, energyFromBalance);

    long energyFromFeeLimit = feeLimit / sunPerEnergy;
    return min(availableEnergy, energyFromFeeLimit);

  }


  private long getTotalEnergyLimitWithFixRatio(AccountCapsule creator,
      AccountCapsule caller, byte[] contractAddress, long feeLimit, long callValue)
      throws ContractValidateException {

    long callerEnergyLimit = getAccountEnergyLimitWithFixRatio(caller, feeLimit, callValue);
    if (Arrays.equals(creator.getAddress().toByteArray(), caller.getAddress().toByteArray())) {
      // when the creator calls his own contract, this logic will be used.
      // so, the creator must use a BIG feeLimit to call his own contract,
      // which will cost the feeLimit TRX when the creator's frozen energy is 0.
      return callerEnergyLimit;
    }

    long creatorEnergyLimit = 0;
    ContractCapsule contractCapsule = this.repository
            .getContract(contractAddress);
    long consumeUserResourcePercent = contractCapsule.getConsumeUserResourcePercent();

    long originEnergyLimit = contractCapsule.getOriginEnergyLimit();
    if (originEnergyLimit < 0) {
      throw new ContractValidateException("originEnergyLimit can't be < 0");
    }

    if (consumeUserResourcePercent <= 0) {
      creatorEnergyLimit = min(energyProcessor.getAccountLeftEnergyFromFreeze(creator),
              originEnergyLimit);
    } else {
      if (consumeUserResourcePercent < Constant.ONE_HUNDRED) {
        creatorEnergyLimit = min(
            BigInteger.valueOf(callerEnergyLimit).multiply(
                BigInteger.valueOf(Constant.ONE_HUNDRED - consumeUserResourcePercent))
                        .divide(BigInteger.valueOf(consumeUserResourcePercent)).longValueExact(),
                min(energyProcessor.getAccountLeftEnergyFromFreeze(creator), originEnergyLimit)
        );
      }
    }
    return Math.addExact(callerEnergyLimit, creatorEnergyLimit);
  }


  private long getTotalEnergyLimit(AccountCapsule creator, AccountCapsule caller,
                                   byte[] contractAddress, long feeLimit, long callValue)
          throws ContractValidateException {
    if (Objects.isNull(creator)) {
      return getAccountEnergyLimitWithFixRatio(caller, feeLimit, callValue);
    }
    return getTotalEnergyLimitWithFixRatio(creator, caller, contractAddress, feeLimit, callValue);

  }

  private double getCpuLimitInUsRatio() {
    double cpuLimitRatio;

    if (InternalTransaction.ExecutorType.ET_PRE_TYPE == executorType) {
      cpuLimitRatio = 1.0;
      return cpuLimitRatio;
    }


    // self witness generates block
    if (this.blockCap != null && blockCap.generatedByMyself
        && this.blockCap.getInstance().getBlockHeader().getWitnessSignature().isEmpty()) {
      cpuLimitRatio = 1.0;
    } else {
      // self witness or other witness or fullnode verifies block
      if (trx.getInstance().getRet(0).getContractRet()
          == Protocol.Transaction.Result.contractResult.OUT_OF_TIME) {
        cpuLimitRatio = DBConfig.getMinTimeRatio();
      } else {
        cpuLimitRatio = DBConfig.getMaxTimeRatio();
      }
    }
    return cpuLimitRatio;
  }

  private boolean isCheckTransaction() {
    return this.blockCap != null && !this.blockCap.getInstance().getBlockHeader()
            .getWitnessSignature().isEmpty();
  }


}

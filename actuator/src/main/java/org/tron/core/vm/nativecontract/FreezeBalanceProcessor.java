package org.tron.core.vm.nativecontract;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.utils.DecodeUtil;
import org.tron.common.utils.StringUtil;
import org.tron.core.actuator.ActuatorConstant;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.DelegatedResourceAccountIndexCapsule;
import org.tron.core.capsule.DelegatedResourceCapsule;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.core.vm.nativecontract.param.FreezeBalanceParam;
import org.tron.core.vm.repository.Repository;
import org.tron.protos.Protocol;
import org.tron.protos.contract.BalanceContract;
import org.tron.protos.contract.Common;

import java.util.Arrays;
import java.util.List;

import static org.tron.core.config.Parameter.ChainConstant.FROZEN_PERIOD;
import static org.tron.core.config.Parameter.ChainConstant.TRX_PRECISION;


@Slf4j(topic = "nativecontract")
public class FreezeBalanceProcessor implements IContractProcessor{
    @Getter
    private Repository repository;

    public FreezeBalanceProcessor(Repository repository){
        this.repository = repository;
    }

    @Override
    public boolean execute(Object contract) {
        FreezeBalanceParam freezeBalanceParam = (FreezeBalanceParam)contract;
        byte[] ownerAddress = freezeBalanceParam.getOwnerAddress();
        long frozenDuration = freezeBalanceParam.getFrozenDuration();
        long frozenBalance = freezeBalanceParam.getFrozenBalance();
        byte[] receiverAddress = freezeBalanceParam.getReceiverAddress();
        Common.ResourceCode resource = freezeBalanceParam.getResource();

        DynamicPropertiesStore dynamicStore = repository.getDynamicPropertiesStore();
        AccountCapsule accountCapsule = repository.getAccount(ownerAddress);

        long now = dynamicStore.getLatestBlockHeaderTimestamp();
        long duration = frozenDuration * FROZEN_PERIOD;

        long newBalance = accountCapsule.getBalance() - frozenBalance;

        long expireTime = now + duration;

        switch (resource) {
            case BANDWIDTH:
                if (!ArrayUtils.isEmpty(receiverAddress)
                        && dynamicStore.supportDR()) {
                    delegateResource(ownerAddress, receiverAddress, true,
                            frozenBalance, expireTime);
                    accountCapsule.addDelegatedFrozenBalanceForBandwidth(frozenBalance);
                } else {
                    long newFrozenBalanceForBandwidth =
                            frozenBalance + accountCapsule.getFrozenBalance();
                    accountCapsule.setFrozenForBandwidth(newFrozenBalanceForBandwidth, expireTime);
                }
                repository
                        .addTotalNetWeight(frozenBalance / TRX_PRECISION);
                break;
            case ENERGY:
                if (!ArrayUtils.isEmpty(receiverAddress)
                        && dynamicStore.supportDR()) {
                    delegateResource(ownerAddress, receiverAddress, false,
                            frozenBalance, expireTime);
                    accountCapsule.addDelegatedFrozenBalanceForEnergy(frozenBalance);
                } else {
                    long newFrozenBalanceForEnergy =
                            frozenBalance + accountCapsule.getAccountResource()
                                    .getFrozenBalanceForEnergy()
                                    .getFrozenBalance();
                    accountCapsule.setFrozenForEnergy(newFrozenBalanceForEnergy, expireTime);
                }
                repository
                        .addTotalEnergyWeight(frozenBalance / TRX_PRECISION);
                break;
            default:
                logger.debug("Resource Code Error.");
        }

        accountCapsule.setBalance(newBalance);
        repository.updateAccount(accountCapsule.createDbKey(), accountCapsule);

        return true;
    }

    @Override
    public boolean validate(Object contract) throws ContractValidateException {
        if (contract == null) {
            throw new ContractValidateException(ActuatorConstant.CONTRACT_NOT_EXIST);
        }
        if (repository == null) {
            throw new ContractValidateException(ActuatorConstant.STORE_NOT_EXIST);
        }

        DynamicPropertiesStore dynamicStore = repository.getDynamicPropertiesStore();
        if (!(contract instanceof FreezeBalanceParam)) {
            throw new ContractValidateException(
                    "contract type error,expected type [FreezeBalanceContract],real type[" + contract
                            .getClass() + "]");
        }
        FreezeBalanceParam freezeBalanceParam = (FreezeBalanceParam)contract;
        byte[] ownerAddress = freezeBalanceParam.getOwnerAddress();
        long frozenDuration = freezeBalanceParam.getFrozenDuration();
        long frozenBalance = freezeBalanceParam.getFrozenBalance();
        byte[] receiverAddress = freezeBalanceParam.getReceiverAddress();
        Common.ResourceCode resource = freezeBalanceParam.getResource();

        if (!DecodeUtil.addressValid(ownerAddress)) {
            throw new ContractValidateException("Invalid address");
        }

        AccountCapsule accountCapsule = repository.getAccount(ownerAddress);
        if (accountCapsule == null) {
            String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);
            throw new ContractValidateException(
                    "Account[" + readableOwnerAddress + "] not exists");
        }

        if (frozenBalance <= 0) {
            throw new ContractValidateException("frozenBalance must be positive");
        }
        if (frozenBalance < TRX_PRECISION) {
            throw new ContractValidateException("frozenBalance must be more than 1TRX");
        }

        int frozenCount = accountCapsule.getFrozenCount();
        if (!(frozenCount == 0 || frozenCount == 1)) {
            throw new ContractValidateException("frozenCount must be 0 or 1");
        }
        if (frozenBalance > accountCapsule.getBalance()) {
            throw new ContractValidateException("frozenBalance must be less than accountBalance");
        }

//    long maxFrozenNumber = dbManager.getDynamicPropertiesStore().getMaxFrozenNumber();
//    if (accountCapsule.getFrozenCount() >= maxFrozenNumber) {
//      throw new ContractValidateException("max frozen number is: " + maxFrozenNumber);
//    }

        long minFrozenTime = dynamicStore.getMinFrozenTime();
        long maxFrozenTime = dynamicStore.getMaxFrozenTime();

        boolean needCheckFrozeTime = CommonParameter.getInstance()
                .getCheckFrozenTime() == 1;//for test
        if (needCheckFrozeTime && !(frozenDuration >= minFrozenTime
                && frozenDuration <= maxFrozenTime)) {
            throw new ContractValidateException(
                    "frozenDuration must be less than " + maxFrozenTime + " days "
                            + "and more than " + minFrozenTime + " days");
        }

        switch (resource) {
            case BANDWIDTH:
                break;
            case ENERGY:
                break;
            default:
                throw new ContractValidateException(
                        "ResourceCode error,valid ResourceCode[BANDWIDTH、ENERGY]");
        }

        //todo：need version control and config for delegating resource
        //If the receiver is included in the contract, the receiver will receive the resource.
        if (!ArrayUtils.isEmpty(receiverAddress) && dynamicStore.supportDR()) {
            if (Arrays.equals(receiverAddress, ownerAddress)) {
                throw new ContractValidateException(
                        "receiverAddress must not be the same as ownerAddress");
            }

            if (!DecodeUtil.addressValid(receiverAddress)) {
                throw new ContractValidateException("Invalid receiverAddress");
            }

            AccountCapsule receiverCapsule = repository.getAccount(receiverAddress);
            if (receiverCapsule == null) {
                String readableOwnerAddress = StringUtil.createReadableString(receiverAddress);
                throw new ContractValidateException(
                        "Account[" + readableOwnerAddress + "] not exists");
            }

            if (dynamicStore.getAllowTvmConstantinople() == 1
                    && receiverCapsule.getType() == Protocol.AccountType.Contract) {
                throw new ContractValidateException(
                        "Do not allow delegate resources to contract addresses");

            }

        }

        return true;
    }

    private void delegateResource(byte[] ownerAddress, byte[] receiverAddress, boolean isBandwidth,
                                  long balance, long expireTime) {
        byte[] key = DelegatedResourceCapsule.createDbKey(ownerAddress, receiverAddress);
        //modify DelegatedResourceStore
        DelegatedResourceCapsule delegatedResourceCapsule = repository.getDelegatedResource(key);
        if (delegatedResourceCapsule != null) {
            if (isBandwidth) {
                delegatedResourceCapsule.addFrozenBalanceForBandwidth(balance, expireTime);
            } else {
                delegatedResourceCapsule.addFrozenBalanceForEnergy(balance, expireTime);
            }
        } else {
            delegatedResourceCapsule = new DelegatedResourceCapsule(
                    ByteString.copyFrom(ownerAddress),
                    ByteString.copyFrom(receiverAddress));
            if (isBandwidth) {
                delegatedResourceCapsule.setFrozenBalanceForBandwidth(balance, expireTime);
            } else {
                delegatedResourceCapsule.setFrozenBalanceForEnergy(balance, expireTime);
            }

        }
        repository.updateDelegatedResource(key, delegatedResourceCapsule);

        //modify DelegatedResourceAccountIndexStore
        {
            DelegatedResourceAccountIndexCapsule delegatedResourceAccountIndexCapsule = repository
                    .getDelegatedResourceAccountIndex(ownerAddress);
            if (delegatedResourceAccountIndexCapsule == null) {
                delegatedResourceAccountIndexCapsule = new DelegatedResourceAccountIndexCapsule(
                        ByteString.copyFrom(ownerAddress));
            }
            List<ByteString> toAccountsList = delegatedResourceAccountIndexCapsule.getToAccountsList();
            if (!toAccountsList.contains(ByteString.copyFrom(receiverAddress))) {
                delegatedResourceAccountIndexCapsule.addToAccount(ByteString.copyFrom(receiverAddress));
            }
            repository.updateDelegatedResourceAccountIndex(ownerAddress, delegatedResourceAccountIndexCapsule);
        }

        {
            DelegatedResourceAccountIndexCapsule delegatedResourceAccountIndexCapsule = repository
                    .getDelegatedResourceAccountIndex(receiverAddress);
            if (delegatedResourceAccountIndexCapsule == null) {
                delegatedResourceAccountIndexCapsule = new DelegatedResourceAccountIndexCapsule(
                        ByteString.copyFrom(receiverAddress));
            }
            List<ByteString> fromAccountsList = delegatedResourceAccountIndexCapsule
                    .getFromAccountsList();
            if (!fromAccountsList.contains(ByteString.copyFrom(ownerAddress))) {
                delegatedResourceAccountIndexCapsule.addFromAccount(ByteString.copyFrom(ownerAddress));
            }
            repository.updateDelegatedResourceAccountIndex(receiverAddress, delegatedResourceAccountIndexCapsule);
        }

        //modify AccountStore
        AccountCapsule receiverCapsule = repository.getAccount(receiverAddress);
        if (isBandwidth) {
            receiverCapsule.addAcquiredDelegatedFrozenBalanceForBandwidth(balance);
        } else {
            receiverCapsule.addAcquiredDelegatedFrozenBalanceForEnergy(balance);
        }

        repository.updateAccount(receiverCapsule.createDbKey(), receiverCapsule);
    }
}

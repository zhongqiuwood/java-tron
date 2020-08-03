package org.tron.core.vm.nativecontract;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.tron.common.utils.DecodeUtil;
import org.tron.common.utils.StringUtil;
import org.tron.core.actuator.ActuatorConstant;
import org.tron.core.capsule.*;
import org.tron.core.db.DelegationService;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.store.*;
import org.tron.core.vm.nativecontract.param.UnfreezeBalanceParam;
import org.tron.core.vm.repository.Repository;
import org.tron.protos.Protocol;
import org.tron.protos.contract.Common;

import java.util.*;

import static org.tron.core.config.Parameter.ChainConstant.TRX_PRECISION;

@Slf4j(topic = "nativecontract")
public class UnfreezeBalanceProcessor  implements IContractProcessor{
    @Getter
    private Repository repository;

    public UnfreezeBalanceProcessor(Repository repository){
        this.repository = repository;
    }

    @Override
    public boolean execute(Object contract) {
        UnfreezeBalanceParam unfreezeBalanceParam = (UnfreezeBalanceParam)contract;
        byte[] ownerAddress = unfreezeBalanceParam.getOwnerAddress();
        byte[] receiverAddress = unfreezeBalanceParam.getReceiverAddress();
        Common.ResourceCode resource = unfreezeBalanceParam.getResource();

        DynamicPropertiesStore dynamicStore = repository.getDynamicPropertiesStore();

        ContractService contractService = new ContractService(repository);
        contractService.withdrawReward(ownerAddress);

        AccountCapsule accountCapsule = repository.getAccount(ownerAddress);
        long oldBalance = accountCapsule.getBalance();

        long unfreezeBalance = 0L;

        //If the receiver is not included in the contract, unfreeze frozen balance for this account.
        //otherwise,unfreeze delegated frozen balance provided this account.
        if (!ArrayUtils.isEmpty(receiverAddress) && dynamicStore.supportDR()) {
            byte[] key = DelegatedResourceCapsule
                    .createDbKey(ownerAddress, receiverAddress);
            DelegatedResourceCapsule delegatedResourceCapsule = repository.getDelegatedResource(key);

            switch (resource) {
                case BANDWIDTH:
                    unfreezeBalance = delegatedResourceCapsule.getFrozenBalanceForBandwidth();
                    delegatedResourceCapsule.setFrozenBalanceForBandwidth(0, 0);
                    accountCapsule.addDelegatedFrozenBalanceForBandwidth(-unfreezeBalance);
                    break;
                case ENERGY:
                    unfreezeBalance = delegatedResourceCapsule.getFrozenBalanceForEnergy();
                    delegatedResourceCapsule.setFrozenBalanceForEnergy(0, 0);
                    accountCapsule.addDelegatedFrozenBalanceForEnergy(-unfreezeBalance);
                    break;
                default:
                    //this should never happen
                    break;
            }

            AccountCapsule receiverCapsule = repository.getAccount(receiverAddress);
            if (dynamicStore.getAllowTvmConstantinople() == 0 ||
                    (receiverCapsule != null)) {
                switch (resource) {
                    case BANDWIDTH:
                        if (dynamicStore.getAllowTvmSolidity059() == 1
                                && receiverCapsule.getAcquiredDelegatedFrozenBalanceForBandwidth()
                                < unfreezeBalance) {
                            receiverCapsule.setAcquiredDelegatedFrozenBalanceForBandwidth(0);
                        } else {
                            receiverCapsule.addAcquiredDelegatedFrozenBalanceForBandwidth(-unfreezeBalance);
                        }
                        break;
                    case ENERGY:
                        if (dynamicStore.getAllowTvmSolidity059() == 1
                                && receiverCapsule.getAcquiredDelegatedFrozenBalanceForEnergy() < unfreezeBalance) {
                            receiverCapsule.setAcquiredDelegatedFrozenBalanceForEnergy(0);
                        } else {
                            receiverCapsule.addAcquiredDelegatedFrozenBalanceForEnergy(-unfreezeBalance);
                        }
                        break;
                    default:
                        //this should never happen
                        break;
                }
                repository.updateAccount(receiverCapsule.createDbKey(), receiverCapsule);
            }

            accountCapsule.setBalance(oldBalance + unfreezeBalance);

            if (delegatedResourceCapsule.getFrozenBalanceForBandwidth() == 0
                    && delegatedResourceCapsule.getFrozenBalanceForEnergy() == 0) {

                //modify DelegatedResourceAccountIndexStore
                {
                    DelegatedResourceAccountIndexCapsule delegatedResourceAccountIndexCapsule = repository
                            .getDelegatedResourceAccountIndex(ownerAddress);
                    if (delegatedResourceAccountIndexCapsule != null) {
                        List<ByteString> toAccountsList = new ArrayList<>(delegatedResourceAccountIndexCapsule
                                .getToAccountsList());
                        toAccountsList.remove(ByteString.copyFrom(receiverAddress));
                        delegatedResourceAccountIndexCapsule.setAllToAccounts(toAccountsList);
                        repository
                                .updateDelegatedResourceAccountIndex(ownerAddress, delegatedResourceAccountIndexCapsule);
                    }
                }

                {
                    DelegatedResourceAccountIndexCapsule delegatedResourceAccountIndexCapsule = repository
                            .getDelegatedResourceAccountIndex(receiverAddress);
                    if (delegatedResourceAccountIndexCapsule != null) {
                        List<ByteString> fromAccountsList = new ArrayList<>(delegatedResourceAccountIndexCapsule
                                .getFromAccountsList());
                        fromAccountsList.remove(ByteString.copyFrom(ownerAddress));
                        delegatedResourceAccountIndexCapsule.setAllFromAccounts(fromAccountsList);
                        repository
                                .updateDelegatedResourceAccountIndex(receiverAddress, delegatedResourceAccountIndexCapsule);
                    }
                }

            }
            repository.updateDelegatedResource(key, delegatedResourceCapsule);
        } else {
            switch (resource) {
                case BANDWIDTH:

                    List<Protocol.Account.Frozen> frozenList = Lists.newArrayList();
                    frozenList.addAll(accountCapsule.getFrozenList());
                    Iterator<Protocol.Account.Frozen> iterator = frozenList.iterator();
                    long now = dynamicStore.getLatestBlockHeaderTimestamp();
                    while (iterator.hasNext()) {
                        Protocol.Account.Frozen next = iterator.next();
                        if (next.getExpireTime() <= now) {
                            unfreezeBalance += next.getFrozenBalance();
                            iterator.remove();
                        }
                    }

                    accountCapsule.setInstance(accountCapsule.getInstance().toBuilder()
                            .setBalance(oldBalance + unfreezeBalance)
                            .clearFrozen().addAllFrozen(frozenList).build());

                    break;
                case ENERGY:
                    unfreezeBalance = accountCapsule.getAccountResource().getFrozenBalanceForEnergy()
                            .getFrozenBalance();

                    Protocol.Account.AccountResource newAccountResource = accountCapsule.getAccountResource().toBuilder()
                            .clearFrozenBalanceForEnergy().build();
                    accountCapsule.setInstance(accountCapsule.getInstance().toBuilder()
                            .setBalance(oldBalance + unfreezeBalance)
                            .setAccountResource(newAccountResource).build());

                    break;
                default:
                    //this should never happen
                    break;
            }

        }

        switch (resource) {
            case BANDWIDTH:
                dynamicStore
                        .addTotalNetWeight(-unfreezeBalance / TRX_PRECISION);
                break;
            case ENERGY:
                dynamicStore
                        .addTotalEnergyWeight(-unfreezeBalance / TRX_PRECISION);
                break;
            default:
                //this should never happen
                break;
        }

        VotesCapsule votesCapsule = repository.getVotesCapsule(ownerAddress);
        if (votesCapsule == null) {
            votesCapsule = new VotesCapsule(ByteString.copyFrom(ownerAddress),
                    accountCapsule.getVotesList());
        }
        accountCapsule.clearVotes();
        votesCapsule.clearNewVotes();

        repository.updateAccount(ownerAddress, accountCapsule);

        repository.updateVotesCapsule(ownerAddress, votesCapsule);

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
        if (!(contract instanceof UnfreezeBalanceParam)) {
            throw new ContractValidateException(
                    "contract type error, expected type [UnfreezeBalanceContract], real type[" + contract
                            .getClass() + "]");
        }
        UnfreezeBalanceParam unfreezeBalanceParam = (UnfreezeBalanceParam)contract;
        byte[] ownerAddress = unfreezeBalanceParam.getOwnerAddress();
        byte[] receiverAddress = unfreezeBalanceParam.getReceiverAddress();
        Common.ResourceCode resource = unfreezeBalanceParam.getResource();

        if (!DecodeUtil.addressValid(ownerAddress)) {
            throw new ContractValidateException("Invalid address");
        }

        AccountCapsule accountCapsule = repository.getAccount(ownerAddress);
        if (accountCapsule == null) {
            String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);
            throw new ContractValidateException(
                    "Account[" + readableOwnerAddress + "] does not exist");
        }
        long now = dynamicStore.getLatestBlockHeaderTimestamp();
        //If the receiver is not included in the contract, unfreeze frozen balance for this account.
        //otherwise,unfreeze delegated frozen balance provided this account.
        if (!ArrayUtils.isEmpty(receiverAddress) && dynamicStore.supportDR()) {
            if (Arrays.equals(receiverAddress, ownerAddress)) {
                throw new ContractValidateException(
                        "receiverAddress must not be the same as ownerAddress");
            }

            if (!DecodeUtil.addressValid(receiverAddress)) {
                throw new ContractValidateException("Invalid receiverAddress");
            }

            AccountCapsule receiverCapsule = repository.getAccount(receiverAddress);
            if (dynamicStore.getAllowTvmConstantinople() == 0
                    && receiverCapsule == null) {
                String readableReceiverAddress = StringUtil.createReadableString(receiverAddress);
                throw new ContractValidateException(
                        "Receiver Account[" + readableReceiverAddress + "] does not exist");
            }

            byte[] key = DelegatedResourceCapsule
                    .createDbKey(ownerAddress, receiverAddress);
            DelegatedResourceCapsule delegatedResourceCapsule = repository.getDelegatedResource(key);
            if (delegatedResourceCapsule == null) {
                throw new ContractValidateException(
                        "delegated Resource does not exist");
            }

            switch (resource) {
                case BANDWIDTH:
                    if (delegatedResourceCapsule.getFrozenBalanceForBandwidth() <= 0) {
                        throw new ContractValidateException("no delegatedFrozenBalance(BANDWIDTH)");
                    }

                    if (dynamicStore.getAllowTvmConstantinople() == 0) {
                        if (receiverCapsule.getAcquiredDelegatedFrozenBalanceForBandwidth()
                                < delegatedResourceCapsule.getFrozenBalanceForBandwidth()) {
                            throw new ContractValidateException(
                                    "AcquiredDelegatedFrozenBalanceForBandwidth[" + receiverCapsule
                                            .getAcquiredDelegatedFrozenBalanceForBandwidth() + "] < delegatedBandwidth["
                                            + delegatedResourceCapsule.getFrozenBalanceForBandwidth()
                                            + "]");
                        }
                    } else {
                        if (dynamicStore.getAllowTvmSolidity059() != 1
                                && receiverCapsule != null
                                && receiverCapsule.getType() != Protocol.AccountType.Contract
                                && receiverCapsule.getAcquiredDelegatedFrozenBalanceForBandwidth()
                                < delegatedResourceCapsule.getFrozenBalanceForBandwidth()) {
                            throw new ContractValidateException(
                                    "AcquiredDelegatedFrozenBalanceForBandwidth[" + receiverCapsule
                                            .getAcquiredDelegatedFrozenBalanceForBandwidth() + "] < delegatedBandwidth["
                                            + delegatedResourceCapsule.getFrozenBalanceForBandwidth()
                                            + "]");
                        }
                    }

                    if (delegatedResourceCapsule.getExpireTimeForBandwidth() > now) {
                        throw new ContractValidateException("It's not time to unfreeze.");
                    }
                    break;
                case ENERGY:
                    if (delegatedResourceCapsule.getFrozenBalanceForEnergy() <= 0) {
                        throw new ContractValidateException("no delegateFrozenBalance(Energy)");
                    }
                    if (dynamicStore.getAllowTvmConstantinople() == 0) {
                        if (receiverCapsule.getAcquiredDelegatedFrozenBalanceForEnergy()
                                < delegatedResourceCapsule.getFrozenBalanceForEnergy()) {
                            throw new ContractValidateException(
                                    "AcquiredDelegatedFrozenBalanceForEnergy[" + receiverCapsule
                                            .getAcquiredDelegatedFrozenBalanceForEnergy() + "] < delegatedEnergy["
                                            + delegatedResourceCapsule.getFrozenBalanceForEnergy() +
                                            "]");
                        }
                    } else {
                        if (dynamicStore.getAllowTvmSolidity059() != 1
                                && receiverCapsule != null
                                && receiverCapsule.getType() != Protocol.AccountType.Contract
                                && receiverCapsule.getAcquiredDelegatedFrozenBalanceForEnergy()
                                < delegatedResourceCapsule.getFrozenBalanceForEnergy()) {
                            throw new ContractValidateException(
                                    "AcquiredDelegatedFrozenBalanceForEnergy[" + receiverCapsule
                                            .getAcquiredDelegatedFrozenBalanceForEnergy() + "] < delegatedEnergy["
                                            + delegatedResourceCapsule.getFrozenBalanceForEnergy() +
                                            "]");
                        }
                    }

                    if (delegatedResourceCapsule.getExpireTimeForEnergy(dynamicStore) > now) {
                        throw new ContractValidateException("It's not time to unfreeze.");
                    }
                    break;
                default:
                    throw new ContractValidateException(
                            "ResourceCode error.valid ResourceCode[BANDWIDTH、Energy]");
            }

        } else {
            switch (resource) {
                case BANDWIDTH:
                    if (accountCapsule.getFrozenCount() <= 0) {
                        throw new ContractValidateException("no frozenBalance(BANDWIDTH)");
                    }

                    long allowedUnfreezeCount = accountCapsule.getFrozenList().stream()
                            .filter(frozen -> frozen.getExpireTime() <= now).count();
                    if (allowedUnfreezeCount <= 0) {
                        throw new ContractValidateException("It's not time to unfreeze(BANDWIDTH).");
                    }
                    break;
                case ENERGY:
                    Protocol.Account.Frozen frozenBalanceForEnergy = accountCapsule.getAccountResource()
                            .getFrozenBalanceForEnergy();
                    if (frozenBalanceForEnergy.getFrozenBalance() <= 0) {
                        throw new ContractValidateException("no frozenBalance(Energy)");
                    }
                    if (frozenBalanceForEnergy.getExpireTime() > now) {
                        throw new ContractValidateException("It's not time to unfreeze(Energy).");
                    }

                    break;
                default:
                    throw new ContractValidateException(
                            "ResourceCode error.valid ResourceCode[BANDWIDTH、Energy]");
            }

        }

        return true;
    }

}

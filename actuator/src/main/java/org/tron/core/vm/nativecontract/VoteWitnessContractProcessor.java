package org.tron.core.vm.nativecontract;

import com.google.common.math.LongMath;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.DecodeUtil;
import org.tron.common.utils.StringUtil;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.VotesCapsule;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.store.WitnessStore;
import org.tron.core.vm.repository.RepositoryImpl;
import org.tron.protos.Protocol;

import java.util.Iterator;
import java.util.Objects;

import static org.tron.core.config.Parameter.ChainConstant.MAX_VOTE_NUMBER;
import static org.tron.core.config.Parameter.ChainConstant.TRX_PRECISION;
import static org.tron.core.vm.nativecontract.ContractProcessorConstant.*;

@Slf4j(topic = "Processor")
public class VoteWitnessContractProcessor implements IContractProcessor {

    private RepositoryImpl repository;

    public VoteWitnessContractProcessor(RepositoryImpl repository) {
        this.repository = repository;
    }

    @Override
    public boolean execute(Object contract) throws ContractExeException {
        VoteWinessCapusle voteWinessCapusle = (VoteWinessCapusle) contract;
        if (Objects.isNull(voteWinessCapusle)) {
            throw new RuntimeException(CONTRACT_NULL);
        }
        try {
            countVoteAccount(voteWinessCapusle);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            throw new ContractExeException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean validate(Object contract) throws ContractValidateException {
        VoteWinessCapusle voteWinessCapusle = (VoteWinessCapusle) contract;
        if (repository == null) {
            throw new ContractValidateException(ContractProcessorConstant.STORE_NOT_EXIST);
        }
        byte[] ownerAddress = voteWinessCapusle.getAddress().toByteArray();
        if (!DecodeUtil.addressValid(ownerAddress)) {
            throw new ContractValidateException("Invalid address");
        }
        AccountCapsule accountCapsule = repository.getAccount(ownerAddress);
        String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);
        if (accountCapsule == null) {
            throw new ContractValidateException(
                    ACCOUNT_EXCEPTION_STR + readableOwnerAddress + NOT_EXIST_STR);
        }
        if (voteWinessCapusle.getVotesCount() == 0) {
            throw new ContractValidateException(
                    "VoteNumber must more than 0");
        }
        if (voteWinessCapusle.getVotesCount() > MAX_VOTE_NUMBER) {
            throw new ContractValidateException(
                    "VoteNumber more than maxVoteNumber " + MAX_VOTE_NUMBER);
        }
        WitnessStore witnessStore = repository.getWitnessStore();
        try {
            Iterator<Protocol.Vote> iterator = voteWinessCapusle.getVotesList().iterator();
            Long sum = 0L;
            while (iterator.hasNext()) {
                Protocol.Vote vote = iterator.next();
                byte[] witnessCandidate = vote.getVoteAddress().toByteArray();
                if (!DecodeUtil.addressValid(witnessCandidate)) {
                    throw new ContractValidateException("Invalid vote address!");
                }
                if (vote.getVoteCount() <= 0) {
                    throw new ContractValidateException("vote count must be greater than 0");
                }
                String readableWitnessAddress = StringUtil.createReadableString(vote.getVoteAddress());
                if (repository.getAccount(witnessCandidate) == null) {
                    throw new ContractValidateException(
                            ACCOUNT_EXCEPTION_STR + readableWitnessAddress + NOT_EXIST_STR);
                }
                if (!witnessStore.has(witnessCandidate)) {
                    throw new ContractValidateException(
                            WITNESS_EXCEPTION_STR + readableWitnessAddress + NOT_EXIST_STR);
                }
                sum = LongMath.checkedAdd(sum, vote.getVoteCount());
            }

            long tronPower = accountCapsule.getTronPower();

            // trx -> drop. The vote count is based on TRX
            sum = LongMath
                    .checkedMultiply(sum, TRX_PRECISION);
            if (sum > tronPower) {
                throw new ContractValidateException(
                        "The total number of votes[" + sum + "] is greater than the tronPower[" + tronPower
                                + "]");
            }
        } catch (ArithmeticException e) {
            logger.debug(e.getMessage(), e);
            throw new ContractValidateException(e.getMessage());
        }

        return true;
    }

    private void countVoteAccount(VoteWinessCapusle voteWinessCapusle) {

        byte[] ownerAddress = voteWinessCapusle.getAddress().toByteArray();
        AccountCapsule accountCapsule = repository.getAccount(ownerAddress);
        VotesCapsule votesCapsule;

        ContractService contractService = new ContractService(repository);
        contractService.withdrawReward(ownerAddress);

        if (repository.getVotesCapsule(ownerAddress) == null) {
            votesCapsule = new VotesCapsule(voteWinessCapusle.getAddress(),
                    accountCapsule.getVotesList());
        } else {
            votesCapsule = repository.getVotesCapsule(ownerAddress);
        }

        accountCapsule.clearVotes();
        votesCapsule.clearNewVotes();

        voteWinessCapusle.getVotesList().forEach(vote -> {
            logger.debug("countVoteAccount, address[{}]",
                    ByteArray.toHexString(vote.getVoteAddress().toByteArray()));

            votesCapsule.addNewVotes(vote.getVoteAddress(), vote.getVoteCount());
            accountCapsule.addVotes(vote.getVoteAddress(), vote.getVoteCount());
        });
        repository.putAccountValue(accountCapsule.createDbKey(), accountCapsule);
        repository.updateVotesCapsule(ownerAddress, votesCapsule);
        repository.commit();
    }

    @Override
    public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
        return null;
    }

    @Override
    public long calcFee() {
        return 0;
    }
}

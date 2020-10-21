package org.tron.core.vm.nativecontract;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.store.DelegationStore;
import org.tron.core.vm.repository.Repository;
import org.tron.protos.Protocol;

@Slf4j(topic = "contractService")
public class ContractService {
  private ContractService() {
  }

  public static ContractService getInstance() {
    return ContractService.Singleton.INSTANCE.getInstance();
  }

  private enum Singleton {
    INSTANCE;
    private ContractService instance;

    Singleton() {
      instance = new ContractService();
    }

    public ContractService getInstance() {
      return instance;
    }
  }

  public void withdrawReward(byte[] address, Repository repository) {
    if (!repository.getDynamicPropertiesStore().allowChangeDelegation()) {
      return;
    }
    AccountCapsule accountCapsule = repository.getAccount(address);
    long beginCycle = repository.getBeginCycle(address);
    long endCycle = repository.getEndCycle(address);
    long currentCycle = repository.getDynamicPropertiesStore().getCurrentCycleNumber();
    long reward = 0;
    if (beginCycle > currentCycle || accountCapsule == null) {
      return;
    }
    if (beginCycle == currentCycle) {
      AccountCapsule account = repository.getAccountVote(beginCycle, address);
      if (account != null) {
        return;
      }
    }
    //withdraw the latest cycle reward
    if (beginCycle + 1 == endCycle && beginCycle < currentCycle) {
      AccountCapsule account = repository.getAccountVote(beginCycle, address);
      if (account != null) {
        reward = computeReward(beginCycle, account, repository);
        adjustAllowance(address, reward, repository);
        reward = 0;
        logger.info("latest cycle reward {},{}", beginCycle, account.getVotesList());
      }
      beginCycle += 1;
    }
    endCycle = currentCycle;
    if (CollectionUtils.isEmpty(accountCapsule.getVotesList())) {
      repository.updateRemark(address, endCycle);
      repository.updateBeginCycle(address, endCycle + 1);
      return;
    }
    if (beginCycle < endCycle) {
      for (long cycle = beginCycle; cycle < endCycle; cycle++) {
        reward += computeReward(cycle, accountCapsule, repository);
      }
      adjustAllowance(address, reward, repository);

    }
    repository.updateBeginCycle(address, endCycle);
    repository.updateEndCycle(address, endCycle + 1);
    repository.updateAccountVote(address, endCycle, accountCapsule);
    logger.info("adjust {} allowance {}, now currentCycle {}, beginCycle {}, endCycle {}, "
            + "account vote {},", Hex.toHexString(address), reward, currentCycle,
        beginCycle, endCycle, accountCapsule.getVotesList());
  }

  private long computeReward(long cycle, AccountCapsule accountCapsule, Repository repository) {
    long reward = 0;
    logger.info("[timeoutTest]cycle={}, votesList size={}", cycle, accountCapsule.getVotesList().size());
    for (Protocol.Vote vote : accountCapsule.getVotesList()) {
      byte[] srAddress = vote.getVoteAddress().toByteArray();
      long totalReward = repository.getDelegationStore().getReward(cycle,
          srAddress);
      long totalVote = repository.getDelegationStore().getWitnessVote(cycle, srAddress);
      if (totalVote == DelegationStore.REMARK || totalVote == 0) {
        continue;
      }
      long userVote = vote.getVoteCount();
      double voteRate = (double) userVote / totalVote;
      reward += voteRate * totalReward;
      logger.info("[timeoutTest]computeReward {} {} {} {},{},{},{}", cycle,
          Hex.toHexString(accountCapsule.getAddress().toByteArray()), Hex.toHexString(srAddress),
          userVote, totalVote, totalReward, reward);
    }
    return reward;
  }

  public void adjustAllowance(byte[] address, long amount, Repository repository) {
    if (amount <= 0) {
      return;
    }
    AccountCapsule accountCapsule = repository.getAccount(address);
    long allowance = accountCapsule.getAllowance();
    accountCapsule.setAllowance(allowance + amount);
    repository.putAccountValue(accountCapsule.createDbKey(), accountCapsule);
  }

  public long queryReward(byte[] address, Repository repository) {
    long start = System.nanoTime();
    long result = queryRewardPack(address, repository);
    long end = System.nanoTime();
    logger.info("[timeoutTest]ContractService.queryReward spendTime:{} ns, result:{}", end - start, result);
    return result;
  }

  public long queryRewardPack(byte[] address, Repository repository) {
    if (!repository.getDynamicPropertiesStore().allowChangeDelegation()) {
      logger.info("[timeoutTest]return at (1)");
      return 0;
    }
    AccountCapsule accountCapsule = repository.getAccount(address);
    long beginCycle = repository.getBeginCycle(address);
    long endCycle = repository.getEndCycle(address);
    long currentCycle = repository.getDynamicPropertiesStore().getCurrentCycleNumber();
    logger.info("[timeoutTest]beginCycle={}, endCycle={}, currentCycle={}", beginCycle, endCycle, currentCycle);
    long reward = 0;
    if (accountCapsule == null) {
      logger.info("[timeoutTest]return at (2)");
      return 0;
    }
    if (beginCycle > currentCycle) {
      logger.info("[timeoutTest]return at (3)");
      return accountCapsule.getAllowance();
    }
    //withdraw the latest cycle reward
    if (beginCycle + 1 == endCycle && beginCycle < currentCycle) {
      AccountCapsule account = repository.getAccountVote(beginCycle, address);
      if (account != null) {
        reward = computeReward(beginCycle, account, repository);
        logger.info("[timeoutTest]account != null, reward={}", reward);
      }
      beginCycle += 1;
    }
    endCycle = currentCycle;
    if (CollectionUtils.isEmpty(accountCapsule.getVotesList())) {
      logger.info("[timeoutTest]return at (4)");
      return reward + accountCapsule.getAllowance();
    }
    logger.info("[timeoutTest]cycle循环from {} to {}", beginCycle, endCycle);
    if (beginCycle < endCycle) {
      for (long cycle = beginCycle; cycle < endCycle; cycle++) {
        reward += computeReward(cycle, accountCapsule, repository);
      }
    }
    logger.info("[timeoutTest]return at (5)");
    return reward + accountCapsule.getAllowance();
  }
}

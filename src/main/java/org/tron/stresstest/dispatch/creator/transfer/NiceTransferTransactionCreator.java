package org.tron.stresstest.dispatch.creator.transfer;

import com.google.protobuf.ByteString;
import lombok.Setter;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.TransactionUtils;
import org.tron.core.Wallet;
import org.tron.stresstest.dispatch.GoodCaseTransactonCreator;
import org.tron.stresstest.dispatch.TransactionFactory;
import org.tron.stresstest.dispatch.creator.CreatorCounter;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;

@Setter
public class NiceTransferTransactionCreator extends AbstractTransferTransactionCreator implements GoodCaseTransactonCreator {

  private String ownerAddress = "THRR7uvFbRLfNzpKPXEyQa8KCJqi59V59e";
  private String toAddress = "TBLZaw93rsnLJ1SWTvoPkr7GVg5ixn2Jv1";
  private long amount = 1L;
  private String privateKey = "9F0B8537C77A0BC91BE874950E2591EDD669DD30390AEAEFC65852F8D5164908";

  @Override
  protected Protocol.Transaction create() {

    TransactionFactory.context.getBean(CreatorCounter.class).put(this.getClass().getName());

    Contract.TransferContract contract = Contract.TransferContract.newBuilder()
        .setOwnerAddress(ByteString.copyFrom(Wallet.decodeFromBase58Check(ownerAddress)))
        .setToAddress(ByteString.copyFrom(Wallet.decodeFromBase58Check(toAddress)))
        .setAmount(amount)
        .build();
    Protocol.Transaction transaction = createTransaction(contract, ContractType.TransferContract);
    transaction = TransactionUtils.setDelaySeconds(transaction, 1000);

    transaction = sign(transaction, ECKey.fromPrivate(ByteArray.fromHexString(privateKey)));
    return transaction;
  }
}

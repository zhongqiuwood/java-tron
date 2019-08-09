package org.tron.stresstest.dispatch.creator.contract;

import lombok.Setter;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.core.Wallet;
import org.tron.protos.Contract.CreateSmartContract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.stresstest.dispatch.AbstractTransactionCreator;
import org.tron.stresstest.dispatch.GoodCaseTransactonCreator;
import org.tron.stresstest.dispatch.TransactionFactory;
import org.tron.stresstest.dispatch.creator.CreatorCounter;

@Setter
public class DeployContractTransactionCreator extends AbstractTransactionCreator implements
    GoodCaseTransactonCreator {

  private String contractName = "trc20Contract";
  private String ownerAddress = commonOwnerAddress;
  private String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"stop\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"burn\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stopped\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"start\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"_addressFounder\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
  private String code = "60c0604052600660808190527f54726f6e6978000000000000000000000000000000000000000000000000000060a090815261003e916000919061013c565b506040805180820190915260038082527f545258000000000000000000000000000000000000000000000000000000000060209092019182526100839160019161013c565b506006600281905560006005558054600160a860020a03191690553480156100aa57600080fd5b50604051602080610a8383398101604081815291516006805461010060a860020a031916336101000217905567016345785d8a00006005819055600160a060020a03821660008181526003602090815286822084905592855294519294909390927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a3506101d7565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061017d57805160ff19168380011785556101aa565b828001600101855582156101aa579182015b828111156101aa57825182559160200191906001019061018f565b506101b69291506101ba565b5090565b6101d491905b808211156101b657600081556001016101c0565b90565b61089d806101e66000396000f3006080604052600436106100cf5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166306fdde0381146100d457806307da68f51461015e578063095ea7b31461017557806318160ddd146101ad57806323b872dd146101d4578063313ce567146101fe57806342966c681461021357806370a082311461022b57806375f12b211461024c57806395d89b4114610261578063a9059cbb14610276578063be9a65551461029a578063c47f0027146102af578063dd62ed3e14610308575b600080fd5b3480156100e057600080fd5b506100e961032f565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561012357818101518382015260200161010b565b50505050905090810190601f1680156101505780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561016a57600080fd5b506101736103bd565b005b34801561018157600080fd5b50610199600160a060020a03600435166024356103e5565b604080519115158252519081900360200190f35b3480156101b957600080fd5b506101c2610465565b60408051918252519081900360200190f35b3480156101e057600080fd5b50610199600160a060020a036004358116906024351660443561046b565b34801561020a57600080fd5b506101c2610588565b34801561021f57600080fd5b5061017360043561058e565b34801561023757600080fd5b506101c2600160a060020a0360043516610625565b34801561025857600080fd5b50610199610637565b34801561026d57600080fd5b506100e9610640565b34801561028257600080fd5b50610199600160a060020a036004351660243561069a565b3480156102a657600080fd5b50610173610764565b3480156102bb57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101739436949293602493928401919081908401838280828437509497506107899650505050505050565b34801561031457600080fd5b506101c2600160a060020a03600435811690602435166107b9565b6000805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156103b55780601f1061038a576101008083540402835291602001916103b5565b820191906000526020600020905b81548152906001019060200180831161039857829003601f168201915b505050505081565b6006546101009004600160a060020a031633146103d657fe5b6006805460ff19166001179055565b60065460009060ff16156103f557fe5b3315156103fe57fe5b336000818152600460209081526040808320600160a060020a03881680855290835292819020869055805186815290519293927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925929181900390910190a350600192915050565b60055481565b60065460009060ff161561047b57fe5b33151561048457fe5b600160a060020a0384166000908152600360205260409020548211156104a957600080fd5b600160a060020a03831660009081526003602052604090205482810110156104d057600080fd5b600160a060020a038416600090815260046020908152604080832033845290915290205482111561050057600080fd5b600160a060020a03808416600081815260036020908152604080832080548801905593881680835284832080548890039055600482528483203384528252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060019392505050565b60025481565b336000908152600360205260409020548111156105aa57600080fd5b336000818152600360209081526040808320805486900390558280527f3617319a054d772f909f7c479a2cebe5066e836a939412e32403c99029b92eff805486019055805185815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350565b60036020526000908152604090205481565b60065460ff1681565b60018054604080516020600284861615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156103b55780601f1061038a576101008083540402835291602001916103b5565b60065460009060ff16156106aa57fe5b3315156106b357fe5b336000908152600360205260409020548211156106cf57600080fd5b600160a060020a03831660009081526003602052604090205482810110156106f657600080fd5b33600081815260036020908152604080832080548790039055600160a060020a03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350600192915050565b6006546101009004600160a060020a0316331461077d57fe5b6006805460ff19169055565b6006546101009004600160a060020a031633146107a257fe5b80516107b59060009060208401906107d6565b5050565b600460209081526000928352604080842090915290825290205481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061081757805160ff1916838001178555610844565b82800160010185558215610844579182015b82811115610844578251825591602001919060010190610829565b50610850929150610854565b5090565b61086e91905b80821115610850576000815560010161085a565b905600a165627a7a723058202b251653003f1859e00d30411fa90d271d66cfc390ba62946fe50260d824d6c50029";
  private long value = 0L;
  private long consumeUserResourcePercent = 100L;
  private String libraryAddressPair = null;
  private long feeLimit = 10000000L;
  private String privateKey = commonOwnerPrivateKey;

  @Override
  protected Protocol.Transaction create() {
    byte[] ownerAddressBytes = Wallet.decodeFromBase58Check(ownerAddress);

    TransactionFactory.context.getBean(CreatorCounter.class).put(this.getClass().getName());

    CreateSmartContract contract = org.tron.stresstest.dispatch.CreateSmartContract
        .createContractDeployContract(
            contractName,
            ownerAddressBytes,
            abi,
            code,
            value,
            consumeUserResourcePercent,
            libraryAddressPair
        );

    Protocol.Transaction transaction = createTransaction(contract,
        ContractType.CreateSmartContract);

    transaction = transaction.toBuilder()
        .setRawData(transaction.getRawData().toBuilder().setFeeLimit(feeLimit).build()).build();

    transaction = sign(transaction, ECKey.fromPrivate(ByteArray.fromHexString(privateKey)));
    return transaction;
  }
}

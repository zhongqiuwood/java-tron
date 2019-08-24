package org.tron.stresstest.dispatch.creator.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.core.Wallet;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.protos.Contract.TriggerSmartContract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.Protocol.Transaction.Result.code;
import org.tron.stresstest.AbiUtil2;
import org.tron.stresstest.dispatch.AbstractTransactionCreator;
import org.tron.stresstest.dispatch.GoodCaseTransactonCreator;
import org.tron.stresstest.dispatch.TransactionFactory;
import org.tron.stresstest.dispatch.creator.CreatorCounter;
import org.tron.stresstest.exception.EncodingException;

@Setter
public class multiValidateSignContractCreator3 extends AbstractTransactionCreator implements
    GoodCaseTransactonCreator {

  private String ownerAddress = triggerOwnerAddress;
  private String contractAddress = commonContractAddress9;
  private long callValue = 0L;
  private String methodSign = "testArray(bytes32,bytes[],address[])";
  private boolean hex = false;
  //  private String param = "\"" + commonContractAddress2 + "\",1002136,1";
  private String param = "\"0x182797a55fb8c343f5a9217ebf14f7449f1c0bb4c77279905705b9174c391d3a\",[\"3d60b61fdeb8cb95bbcff7762361e4aa1c91396915c5d1c9d3f755550aec77f125d80d68b69597a2c89e23819cab4ed7b685fc9faad16efb5431e38ee8176c6f00\",\"6c7977febeae8a1f17465e1d8f71806e5be8605350a6a070c642c3c96a5cfe0a4324221d476664ecd3cb353f519abbe191f7440ba98f4d215e3a18ab7b5d763300\",\"09b601844c41a65985026ebcdb712456ec2109e042d20d8ef96537e21324d84c0e59380dc0ef29b29c36232befccdd75c62f6963e331e01f23890d7aca19bd1a01\",\"372a2ba086c061c718b81d64006d978ff0aaf9230e4d8a25adfadcf4647c066d5a59b9faaacd3d63d53d1fc257f985690e719fd8c5b8112c85fef42ba7687ff701\"],[\"TLhZoLmiSYcgeoK267HcDLRu2aaLL6L9So\",\"TAuQgjMSnfGbMc6S9yXrNdQ1yhxRbJzrAU\",\"TReGUiaaFD6PdFZKCCEXcfVfiteTmsZ4tS\",\"TJv6H5gVpBKqKfXHdsZq6AYxTtJg4LRsyC\"]";
  private long feeLimit = 1000000000L;
  private String privateKey = triggerOwnerKey;
  List<Object> signatures = new ArrayList<>();
  List<Object> addresses = new ArrayList<>();
  public static AtomicLong queryCount = new AtomicLong();

  private List<String> ownerAddressList = new CopyOnWriteArrayList<>();


  @Override
  protected Protocol.Transaction create() {
    byte[] ownerAddressBytes = Wallet.decodeFromBase58Check(ownerAddress);

    TransactionFactory.context.getBean(CreatorCounter.class).put(this.getClass().getName());

    TriggerSmartContract contract = null;
//    String txid = "93967a1bb6590308bae2c0a8d82bdc2ed07ef39b675d8ddf958762f09bbf9657";
//    byte[] hash = Hash.sha3(txid.getBytes());
//    for (int i = 0; i < 4; i++) {
//      ECKey key = new ECKey();
//      byte[] sign = key.sign(hash).toByteArray();
//      signatures.add(Hex.toHexString(sign));
//      addresses.add(Wallet.encode58Check(key.getAddress()));
//    }
//    addresses.set(1, Wallet.encode58Check(new ECKey().getAddress()));
//    addresses.set(2, Wallet.encode58Check(new ECKey().getAddress()));
////    addresses.set(27, Wallet.encode58Check(new ECKey().getAddress()));
////    addresses.set(31, Wallet.encode58Check(new ECKey().getAddress()));
//    List<Object> parameters = Arrays.asList("0x" + Hex.toHexString(hash), signatures, addresses);
//    param = parametersString(parameters);

    try {
      contract = triggerCallContract(
          ownerAddressBytes, Wallet.decodeFromBase58Check(contractAddress), callValue,
          Hex.decode(AbiUtil2.parseMethod(methodSign, param, hex)));
    } catch (EncodingException e) {
      e.printStackTrace();
    }

    Protocol.Transaction transaction = createTransaction(contract,
        ContractType.TriggerSmartContract);

    transaction = transaction.toBuilder()
        .setRawData(transaction.getRawData().toBuilder().setFeeLimit(feeLimit).build()).build();
    TransactionResultCapsule ret = new TransactionResultCapsule();

    ret.setStatus(0, code.SUCESS);
    transaction = transaction.toBuilder().addRet(ret.getInstance())
        .build();
    transaction = sign(transaction, ECKey.fromPrivate(ByteArray.fromHexString(privateKey)));
    return transaction;
  }

  public static String parametersString(List<Object> parameters) {
    String[] inputArr = new String[parameters.size()];
    int i = 0;
    for (int j = 0; j < parameters.size(); j++) {
      Object parameter = parameters.get(j);
      if (parameter instanceof List) {
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < ((List) parameter).size(); k++) {
          Object item = ((List) parameter).get(k);
          if (sb.length() != 0) {
            sb.append(",");
          }
          sb.append("\"").append(item).append("\"");
        }
        inputArr[i++] = "[" + sb.toString() + "]";
      } else {
        inputArr[i++] =
            (parameter instanceof String) ? ("\"" + parameter + "\"") : ("" + parameter);
      }
    }
    String input = StringUtils.join(inputArr, ',');
    return input;
  }


  public static String bytes32ToString(byte[] bytes) {
    if (bytes == null) {
      return "null";
    }
    int iMax = bytes.length - 1;
    if (iMax == -1) {
      return "";
    }

    StringBuilder b = new StringBuilder();
    for (int i = 0; ; i++) {
      b.append(bytes[i]);
      if (i == iMax) {
        return b.toString();
      }
    }
  }
}

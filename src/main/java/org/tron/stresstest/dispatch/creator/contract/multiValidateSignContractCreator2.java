package org.tron.stresstest.dispatch.creator.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
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
public class multiValidateSignContractCreator2 extends AbstractTransactionCreator implements
    GoodCaseTransactonCreator {

  private String ownerAddress = triggerOwnerAddress;
  private String contractAddress = commonContractAddress8;
  private long callValue = 0L;
  private String methodSign = "testArray(bytes32,bytes[],address[])";
  private boolean hex = false;
  //  private String param = "\"" + commonContractAddress2 + "\",1002136,1";
  private String param = null;
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
    String txid = "6232d88558c9d95c012d0c5fbdddcef07eea8cb45934599eecdd54b920ff76fc";
    byte[] hash = Hash.sha3(txid.getBytes());
    for (int i = 0; i < 4; i++) {
      ECKey key = new ECKey();
      byte[] sign = key.sign(hash).toByteArray();
      if (i == 0) {
        signatures.add(
            Hex.toHexString(key.sign("dgjjsldgjljvjjfdshkh1hgsk0807779".getBytes()).toByteArray()));
      } else {
        signatures.add(Hex.toHexString(sign));
      }
      addresses.add(Wallet.encode58Check(key.getAddress()));
    }
    List<Object> parameters = Arrays.asList("0x" + Hex.toHexString(hash), signatures, addresses);
    param = parametersString(parameters);
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

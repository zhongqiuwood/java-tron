package org.tron.stresstest.dispatch.creator.contract;

import static org.tron.stresstest.AbiUtil2.parseMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import org.tron.stresstest.dispatch.AbstractTransactionCreator;
import org.tron.stresstest.dispatch.GoodCaseTransactonCreator;
import org.tron.stresstest.dispatch.TransactionFactory;
import org.tron.stresstest.dispatch.creator.CreatorCounter;

@Slf4j
@Setter
public class multiValidateSignContractCreator extends AbstractTransactionCreator implements
    GoodCaseTransactonCreator {

  private String ownerAddress = triggerOwnerAddress;
  private String contractAddress = commonContractAddress7;
  private long callValue = 0L;
  private String methodSign = "testArray(bytes32,bytes[],address[])";
  private boolean hex = false;
  //  private String param = "\"" + commonContractAddress2 + "\",1002136,1";
  private String param = "\"0x43767d75401bbab8890ff3add59b4685d2adc6108ac8bdf3ee3f7109d339db40\",[\"57fbd59c0a7392d7f1114e726792a3cfbf154eeb8474274ca7a7d21316d3563f4a765cbc9d402de1b3b2791659fd23eec2de0a0296e5ea6584bb1381c7a865d000\",\"889b793caa641adb23d668242849a6b0d6451f59cf2984e98d6cf02837aaffae566f6774a4085083916aa8474fb799029e64242ed0c3a08d140efcb76c108cbf01\",\"dbdd0e362f210cd8de8f3245dc6869b9ff6d4975687b8e21945301121d2feb81333124bf1711776201dff1166325e2b4dcd6141766158e3aa696560c5bb955e901\",\"020a188b4303c37189fc5ba1b5ce1bf527c675bb5cf1e35a7bcb8153e8acaeff6703c13d4af4464157e39a834d1086d749ba23b9e903b5f3eab941d7f5c117c800\"],[\"TNj2iY579YcAxr8aBHek7HGDw8BFRnUUS4\",\"TDhBjj1Cs25LRLdyC5eGy6RDMFQ9S6EtBP\",\"THMmBQzjvdt494YapNCnKfCM96z1ZpCmS6\",\"TGvMq135HdbhFAYisVuvS7MT7qWj2wUT2h\"]";
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

//    synchronized (this) {
//      String txid = "a8e1e40136c41ea4452ee85b80e8b73dd14611137e7ea521a4844df84a12f10b";
//      byte[] hash = Hash.sha3(txid.getBytes());
//      for (int i = 0; i < 4; i++) {
//        ECKey key = new ECKey();
//        byte[] sign = key.sign(hash).toByteArray();
//        signatures.add(Hex.toHexString(sign));
//        addresses.add(Wallet.encode58Check(key.getAddress()));
//      }
//      List<Object> parameters = Arrays.asList("0x" + Hex.toHexString(hash), signatures, addresses);
//      param = parametersString(parameters);}

    try {

      contract = triggerCallContract(
          ownerAddressBytes, Wallet.decodeFromBase58Check(contractAddress), callValue,
          Hex.decode(parseMethod(methodSign, param, hex)));
    } catch (Exception e) {
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


  public String parametersString(List<Object> parameters) {
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

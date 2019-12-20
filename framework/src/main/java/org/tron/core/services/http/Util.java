package org.tron.core.services.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.StringUtil;
import org.pf4j.util.StringUtils;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.api.GrpcAPI.EasyTransferResponse;
import org.tron.api.GrpcAPI.TransactionApprovedList;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.GrpcAPI.TransactionList;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.DecodeUtil;
import org.tron.common.utils.Hash;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Wallet;
import org.tron.core.actuator.TransactionFactory;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.args.Args;
import org.tron.core.services.http.JsonFormat.ParseException;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.contract.SmartContractOuterClass.CreateSmartContract;

@Slf4j(topic = "API")
public class Util {

  public static final String VISIBLE = "visible";
  public static final String PERMISSION_ID = "Permission_id";
  public static final String TRANSACTION = "transaction";
  public static final String VALUE = "value";
  public static final String CONTRACT_TYPE = "contractType";

  public static String printTransactionFee(String transactionFee) {
    JSONObject jsonObject = new JSONObject();
    JSONObject receipt = JSONObject.parseObject(transactionFee);
    jsonObject.put("Receipt", receipt.get("receipt"));
    return jsonObject.toJSONString();
  }

  public static String printErrorMsg(Exception e) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Error", e.getClass() + " : " + e.getMessage());
    return jsonObject.toJSONString();
  }

  public static String printBlock(Block block, boolean selfType) {
    return printBlockToJSON(block, selfType).toJSONString();
  }

  public static JSONObject printBlockToJSON(Block block, boolean selfType) {
    BlockCapsule blockCapsule = new BlockCapsule(block);
    String blockID = ByteArray.toHexString(blockCapsule.getBlockId().getBytes());
    JSONObject jsonObject = JSONObject.parseObject(JsonFormat.printToString(block, selfType));
    jsonObject.put("blockID", blockID);
    if (!blockCapsule.getTransactions().isEmpty()) {
      jsonObject.put("transactions", printTransactionListToJSON(blockCapsule.getTransactions(),
          selfType));
    }
    return jsonObject;
  }

  public static String printTransactionList(TransactionList list, boolean selfType) {
    List<Transaction> transactions = list.getTransactionList();
    JSONObject jsonObject = JSONObject.parseObject(JsonFormat.printToString(list, selfType));
    JSONArray jsonArray = new JSONArray();
    transactions.stream().forEach(transaction -> jsonArray
        .add(printTransactionToJSON(transaction, selfType))
    );
    jsonObject.put(TRANSACTION, jsonArray);

    return jsonObject.toJSONString();
  }

  public static JSONArray printTransactionListToJSON(List<TransactionCapsule> list,
      boolean selfType) {
    JSONArray transactions = new JSONArray();
    list.stream().forEach(transactionCapsule -> transactions
        .add(printTransactionToJSON(transactionCapsule.getInstance(), selfType)));
    return transactions;
  }

  public static String printEasyTransferResponse(EasyTransferResponse response, boolean selfType) {
    JSONObject jsonResponse = JSONObject.parseObject(JsonFormat.printToString(response, selfType));
    jsonResponse.put(TRANSACTION, printTransactionToJSON(response.getTransaction(), selfType));
    return jsonResponse.toJSONString();
  }

  public static String printTransaction(Transaction transaction, boolean selfType) {
    return printTransactionToJSON(transaction, selfType).toJSONString();
  }

  public static String printTransaction(Transaction transaction) {
    return printTransactionToJSON(transaction, true).toJSONString();
  }

  public static String printTransactionExtention(TransactionExtention transactionExtention,
      boolean selfType) {
    String string = JsonFormat.printToString(transactionExtention, selfType);
    JSONObject jsonObject = JSONObject.parseObject(string);
    if (transactionExtention.getResult().getResult()) {
      JSONObject transactionObject = printTransactionToJSON(
          transactionExtention.getTransaction(), selfType);
      transactionObject.put(VISIBLE, selfType);
      jsonObject.put(TRANSACTION, transactionObject);
    }
    return jsonObject.toJSONString();
  }

  public static String printTransactionSignWeight(TransactionSignWeight transactionSignWeight,
      boolean selfType) {
    String string = JsonFormat.printToString(transactionSignWeight, selfType);
    JSONObject jsonObject = JSONObject.parseObject(string);
    JSONObject jsonObjectExt = jsonObject.getJSONObject(TRANSACTION);
    jsonObjectExt
        .put(TRANSACTION,
            printTransactionToJSON(transactionSignWeight.getTransaction().getTransaction(),
                selfType));
    jsonObject.put(TRANSACTION, jsonObjectExt);
    return jsonObject.toJSONString();
  }

  public static String printTransactionApprovedList(
      TransactionApprovedList transactionApprovedList, boolean selfType) {
    String string = JsonFormat.printToString(transactionApprovedList, selfType);
    JSONObject jsonObject = JSONObject.parseObject(string);
    JSONObject jsonObjectExt = jsonObject.getJSONObject(TRANSACTION);
    jsonObjectExt.put(TRANSACTION,
        printTransactionToJSON(transactionApprovedList.getTransaction().getTransaction(),
            selfType));
    jsonObject.put(TRANSACTION, jsonObjectExt);
    return jsonObject.toJSONString();
  }

  public static byte[] generateContractAddress(Transaction trx, byte[] ownerAddress) {
    // get tx hash
    byte[] txRawDataHash = Sha256Hash.of(trx.getRawData().toByteArray()).getBytes();

    // combine
    byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
    System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
    System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

    return Hash.sha3omit12(combined);
  }

  public static JSONObject printTransactionToJSON(Transaction transaction, boolean selfType) {
    JSONObject jsonTransaction = JSONObject.parseObject(JsonFormat.printToString(transaction,
        selfType));
    JSONArray contracts = new JSONArray();
    transaction.getRawData().getContractList().stream().forEach(contract -> {
      try {
        JSONObject contractJson = null;
        Any contractParameter = contract.getParameter();
        switch (contract.getType()) {
          case CreateSmartContract:
            CreateSmartContract deployContract = contractParameter
                .unpack(CreateSmartContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(deployContract,
                selfType));
            byte[] ownerAddress = deployContract.getOwnerAddress().toByteArray();
            byte[] contractAddress = generateContractAddress(transaction, ownerAddress);
            jsonTransaction.put("contract_address", ByteArray.toHexString(contractAddress));
            break;
          default:
            Class clazz = TransactionFactory.getContract(contract.getType());
            if (clazz != null) {
              contractJson = JSONObject
                  .parseObject(JsonFormat.printToString(contractParameter.unpack(clazz), selfType));
            }
            break;
        }

        JSONObject parameter = new JSONObject();
        parameter.put(VALUE, contractJson);
        parameter.put("type_url", contract.getParameterOrBuilder().getTypeUrl());
        JSONObject jsonContract = new JSONObject();
        jsonContract.put("parameter", parameter);
        jsonContract.put("type", contract.getType());
        if (contract.getPermissionId() > 0) {
          jsonContract.put(PERMISSION_ID, contract.getPermissionId());
        }
        contracts.add(jsonContract);
      } catch (InvalidProtocolBufferException e) {
        logger.debug("InvalidProtocolBufferException: {}", e.getMessage());
      }
    });

    JSONObject rawData = JSONObject.parseObject(jsonTransaction.get("raw_data").toString());
    rawData.put("contract", contracts);
    jsonTransaction.put("raw_data", rawData);
    String rawDataHex = ByteArray.toHexString(transaction.getRawData().toByteArray());
    jsonTransaction.put("raw_data_hex", rawDataHex);
    String txID = ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    jsonTransaction.put("txID", txID);
    return jsonTransaction;
  }

  public static void checkBodySize(String body) throws Exception {
    CommonParameter parameter = Args.getInstance();
    if (body.getBytes().length > parameter.getMaxMessageSize()) {
      throw new Exception("body size is too big, the limit is " + parameter.getMaxMessageSize());
    }
  }
  public static boolean getVisible(final HttpServletRequest request) {
    boolean visible = false;
    if (StringUtil.isNotBlank(request.getParameter(VISIBLE))) {
      visible = Boolean.valueOf(request.getParameter(VISIBLE));
    }
    return visible;
  }

  public static String getContractType(final String input) {
    String contractType = null;
    JSONObject jsonObject = JSON.parseObject(input);
    if (jsonObject.containsKey(CONTRACT_TYPE)) {
      contractType = jsonObject.getString(CONTRACT_TYPE);
    }
    return contractType;
  }

  public static boolean getVisibleOnlyForSign(JSONObject jsonObject) {
    boolean visible = false;
    if (jsonObject.containsKey(VISIBLE)) {
      visible = jsonObject.getBoolean(VISIBLE);
    } else if (jsonObject.getJSONObject(TRANSACTION).containsKey(VISIBLE)) {
      visible = jsonObject.getJSONObject(TRANSACTION).getBoolean(VISIBLE);
    }
    return visible;
  }

  public static String parseMethod(String methodSign, String input) {
    byte[] selector = new byte[4];
    System.arraycopy(Hash.sha3(methodSign.getBytes()), 0, selector, 0, 4);
    //System.out.println(methodSign + ":" + Hex.toHexString(selector));
    if (StringUtils.isNullOrEmpty(input)) {
      return Hex.toHexString(selector);
    }

    return Hex.toHexString(selector) + input;
  }

  public static long getJsonLongValue(final JSONObject jsonObject, final String key) {
    return getJsonLongValue(jsonObject, key, false);
  }

  public static long getJsonLongValue(JSONObject jsonObject, String key, boolean required) {
    BigDecimal bigDecimal = jsonObject.getBigDecimal(key);
    if (required && bigDecimal == null) {
      throw new InvalidParameterException("key [" + key + "] does not exist");
    }
    return (bigDecimal == null) ? 0L : bigDecimal.longValueExact();
  }

  public static String getMemo(byte[] memo) {
    int index = memo.length;
    for (; index > 0; --index) {
      if (memo[index - 1] != 0) {
        break;
      }
    }

    byte[] inputCheck = new byte[index];
    System.arraycopy(memo, 0, inputCheck, 0, index);
    return new String(inputCheck, Charset.forName("UTF-8"));
  }
}

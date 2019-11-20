package org.tron.core.services.http;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.common.utils.ByteArray;
import org.tron.core.Wallet;
import org.tron.core.db.Manager;
import org.tron.protos.Contract.AccountPermissionUpdateContract;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;


@Component
@Slf4j(topic = "API")
public class ConstructorParameterServlet extends HttpServlet {

  @Autowired
  private Wallet wallet;
  @Autowired
  private Manager dbmanager;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      String input = request.getParameter("value");
      if (visible) {
        input = Util.getHexAddress(input);
      }

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("value", input);
      BytesMessage.Builder build = BytesMessage.newBuilder();
      JsonFormat.merge(jsonObject.toJSONString(), build, visible);
      SmartContract smartContract = wallet.getContract(build.build());
      String deployedBytecode = ByteArray.toHexString(smartContract.getBytecode().toByteArray());
      String runtimeBytecode = ByteArray.toHexString(dbmanager.getCodeStore().
          get(smartContract.getContractAddress().toByteArray()).getData());
      String[] resultArray = deployedBytecode.split(runtimeBytecode);
      String result = "parse failed";
      if(resultArray.length==2){
        result = resultArray[1];
      }
      JSONObject jsonResult = new JSONObject();
      jsonResult.put("constructor_param",result);
      response.getWriter().println(jsonResult.toJSONString());
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(Util.printErrorMsg(e));
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);
      boolean visible = Util.getVisiblePost(input);
      if (visible) {
        JSONObject jsonObject = JSONObject.parseObject(input);
        String value = jsonObject.getString("value");
        jsonObject.put("value", Util.getHexAddress(value));
        input = jsonObject.toJSONString();
      }

      BytesMessage.Builder build = BytesMessage.newBuilder();
      JsonFormat.merge(input, build, visible);
      SmartContract smartContract = wallet.getContract(build.build());
      JSONObject jsonSmartContract = JSONObject
          .parseObject(JsonFormat.printToString(smartContract, visible));
      response.getWriter().println(jsonSmartContract.toJSONString());
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(Util.printErrorMsg(e));
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }
}

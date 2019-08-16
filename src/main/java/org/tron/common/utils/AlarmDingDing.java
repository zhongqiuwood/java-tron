package org.tron.common.utils;

import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class AlarmDingDing {
  private static final String http_url = "https://oapi.dingtalk.com/robot/send?"
      + "access_token=5bdf7396e96f427dad3241a0d07766b2482fb55424e32ff2e1d9fb95a2f1768b";

  public static boolean post(final String url, final JSONObject requestBody) {
    boolean bRet = false;
    //create default httpClient instance.
    CloseableHttpClient httpclient = HttpClients.createDefault();
    //create httppost
    HttpPost httppost = new HttpPost(url);
    httppost.setHeader("Content-type", "application/json; charset=utf-8");
    httppost.setHeader("Connection", "Close");
    //add parameters
    if (requestBody != null) {
      StringEntity entity = new StringEntity(requestBody.toString(), Charset.forName("UTF-8"));
      entity.setContentEncoding("UTF-8");
      entity.setContentType("application/json");
      httppost.setEntity(entity);
    }

    try {
      CloseableHttpResponse response = httpclient.execute(httppost);
      try {
        bRet = verificationResult(response);
      } finally {
        response.close();
      }
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      //close connection,release resource
      try {
        httpclient.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return bRet;
  }

  /**
   * constructor.
   */
  public static boolean verificationResult(HttpResponse response) {
    if (response.getStatusLine().getStatusCode() != 200) {
      return false;
    }
    return true;
  }

  public static boolean postDingDing( final JSONObject requestBody ) {
    //return post(http_url, requestBody);
    return true;
  }
}

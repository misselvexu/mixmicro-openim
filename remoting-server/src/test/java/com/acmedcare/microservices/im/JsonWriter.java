package com.acmedcare.microservices.im;


import com.alibaba.fastjson.JSONObject;
import java.io.UnsupportedEncodingException;
import org.springframework.util.Base64Utils;

/**
 * com.acmedcare.microservices.im
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 19/09/2018.
 */
public class JsonWriter {
  public static void main(String[] args) throws UnsupportedEncodingException {

    String str = "测试代码code";

    byte[] strBytes = str.getBytes("UTF-8");

    String base64Str = Base64Utils.encodeToString(strBytes);

    System.out.println("标准BASE64 = " + base64Str);

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("str",strBytes);

    String json = jsonObject.toJSONString();

    System.out.println("FastJson输出: " + json);


  }
}

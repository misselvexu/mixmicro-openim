package com.acmedcare.tiffany.framework;

import com.acmedcare.framework.kits.jre.http.HttpRequest;

/**
 * com.acmedcare.tiffany.framework
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
public class MasterRequestTest {

  public static void main(String[] args) {
    String url = "http://127.0.0.1:13110/master/available-cluster-servers";
    HttpRequest request = HttpRequest.get(url);
    if (request.ok()) {
      String body = request.body("UTF-8");
      System.out.println(body);
    }
  }
}

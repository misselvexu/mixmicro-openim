package com.acmedcare.framework.remoting.mq.client;

import com.acmedcare.framework.remoting.mq.client.biz.request.AuthRequest;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import com.acmedcare.nas.client.NasClient;
import com.acmedcare.nas.client.NasClientFactory;
import java.util.List;
import java.util.Random;

/**
 * Biz Executor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public abstract class BizExecutor {

  /** 请求超时时间 */
  protected static long requestTimeout =
      Long.parseLong(System.getProperty("tiffany.quantum.request.timeout", "5000"));

  protected AcmedcareMQRemoting remoting;

  protected NasClient nasClient;

  public BizExecutor(AcmedcareMQRemoting remoting) {
    this.remoting = remoting;

    try {
      if (AcmedcareMQRemoting.getNasProperties() != null) {
        this.nasClient =
            NasClientFactory.createNewNasClient(AcmedcareMQRemoting.getNasProperties());
        System.out.println(this.nasClient);
      }
    } catch (Exception e) {
      e.printStackTrace();
      AcmedcareLogger.e(null, e, "Acmedcare Nas Init failed");
    }
  }

  public NasClient nasClient() {
    return this.nasClient;
  }

  protected String remotingAddress() {
    if (this.remoting != null) {
      if (this.remoting.getCurrentRemotingAddress() != null) {
        return this.remoting.getCurrentRemotingAddress();
      } else {
        if (AcmedcareMQRemoting.getAddresses() != null) {
          List<String> addresses = AcmedcareMQRemoting.getAddresses();
          if (addresses.size() > 0) {
            this.remoting.setCurrentRemotingAddress(
                addresses.get(new Random(addresses.size()).nextInt()));
            return this.remoting.getCurrentRemotingAddress();
          }
        }
        AcmedcareLogger.w(
            this.getClass().getSimpleName(), "No found remoting address for current request;");
        return null;
      }
    } else {
      throw new BizException("BizExecutor not init-ed with framework.");
    }
  }

  /**
   * 授权 Api
   *
   * @throws BizException exception
   */
  protected abstract void auth(AuthRequest request, AuthRequest.AuthCallback authCallback)
      throws BizException;
}

package com.acmedcare.framework.newim.server.endpoint.ws;

import com.acmedcare.framework.boot.web.socket.annotation.OnClose;
import com.acmedcare.framework.boot.web.socket.annotation.OnError;
import com.acmedcare.framework.boot.web.socket.annotation.OnEvent;
import com.acmedcare.framework.boot.web.socket.annotation.OnMessage;
import com.acmedcare.framework.boot.web.socket.annotation.OnOpen;
import com.acmedcare.framework.boot.web.socket.annotation.ServerEndpoint;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Default Wss Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Component
@ServerEndpoint(prefix = "newim-ws")
public class DefaultWssEndpoint {

  @OnOpen
  public void onOpen(WssSession session, HttpHeaders headers) throws IOException {
    System.out.println("new connection");
    if (headers.contains("passport")) {
      System.out.println(headers.get("passport"));
    } else {
      session.sendText("no passport");
      session.close();
    }
  }

  @OnClose
  public void onClose(WssSession session) throws IOException {
    System.out.println("one connection closed");
  }

  @OnError
  public void onError(WssSession session, Throwable throwable) {
    throwable.printStackTrace();
  }

  @OnMessage
  public void onMessage(WssSession session, String message) {
    System.out.println(message);
    session.sendText("{\"name\":\"..\"}");
  }

  @OnEvent
  public void onEvent(WssSession session, Object evt) {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
      switch (idleStateEvent.state()) {
        case READER_IDLE:
          System.out.println("read idle");
          break;
        case WRITER_IDLE:
          System.out.println("write idle");
          break;
        case ALL_IDLE:
          System.out.println("all idle");
          break;
        default:
          break;
      }
    }
  }
}

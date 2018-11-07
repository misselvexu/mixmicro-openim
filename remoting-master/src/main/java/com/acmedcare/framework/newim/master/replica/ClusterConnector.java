package com.acmedcare.framework.newim.master.replica;

import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import io.netty.channel.Channel;
import lombok.Builder;

/**
 * Cluster Connector For {@link
 * com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient} and {@link
 * com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 07/11/2018.
 */
public class ClusterConnector {

  /**
   * Server Acceptor
   *
   * <p>
   */
  public static class ClusterServer {

    private NettyServerConfig serverConfig;
    private volatile NettyRemotingSocketServer server;

    @Builder
    public ClusterServer(NettyServerConfig serverConfig) {
      this.serverConfig = serverConfig;
    }

    public NettyRemotingSocketServer newServer() {
      if (server != null) {
        return server;
      }
      // new
      server =
          new NettyRemotingSocketServer(
              serverConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String address, Channel channel) {}

                @Override
                public void onChannelClose(String address, Channel channel) {}

                @Override
                public void onChannelException(String address, Channel channel) {}

                @Override
                public void onChannelIdle(String address, Channel channel) {}
              });

      return server;
    }
  }

  /**
   * Connector Client
   *
   * <p>
   */
  public static class ClusterClient {}
}

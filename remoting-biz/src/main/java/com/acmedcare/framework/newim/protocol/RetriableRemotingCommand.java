package com.acmedcare.framework.newim.protocol;

import com.acmedcare.framework.newim.protocol.Command.Retriable;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Retriable Remoting Command
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Getter
@Setter
@Builder
public class RetriableRemotingCommand {

  private Retriable retriable;
  private CommandCustomHeader header;
  private byte[] body;
  private NettyRemotingSocketClient client;
  private String targetServerAddress;
}

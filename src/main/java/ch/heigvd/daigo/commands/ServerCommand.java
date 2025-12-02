package ch.heigvd.daigo.commands;

import java.util.concurrent.Callable;

import ch.heigvd.daigo.server.Server;
import picocli.CommandLine;

@CommandLine.Command(name = "server", description = "Start the server part of the network game.")
public class ServerCommand implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "1919")
  protected int port;

  @Override
  public Integer call() {
      Server server = new Server(port);
      server.launch();
      return 0;
  }
}

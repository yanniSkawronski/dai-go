package ch.heigvd.daigo.commands;

import java.util.concurrent.Callable;

import ch.heigvd.daigo.goprogs.GoServer;
import picocli.CommandLine;

@CommandLine.Command(name = "server", description = "Start the server part of the network game.")
public class Server implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "6433")
  protected int port;

  @Override
  public Integer call() {
      GoServer goServer = new GoServer(port);
      goServer.launch();
      return 0;
  }
}

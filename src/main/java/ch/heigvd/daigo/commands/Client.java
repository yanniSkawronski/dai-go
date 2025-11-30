package ch.heigvd.daigo.commands;

import java.util.concurrent.Callable;

import ch.heigvd.daigo.goprogs.NiceGoClient;
import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Start the client part of the network game.")
public class Client implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-H", "--host"},
      description = "Host to connect to.",
      required = true)
  protected String host;

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "6433")
  protected int port;

  @Override
  public Integer call() {
      NiceGoClient niceGoClient = new NiceGoClient(host, port);
      niceGoClient.launch();
      return 0;
  }
}

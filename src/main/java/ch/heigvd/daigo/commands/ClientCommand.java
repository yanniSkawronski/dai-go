package ch.heigvd.daigo.commands;

import java.util.concurrent.Callable;

import ch.heigvd.daigo.client.NiceClient;
import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Start the client part of the network game.")
public class ClientCommand implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-H", "--host"},
      description = "Host to connect to.",
      required = true)
  protected String host;

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "1919")
  protected int port;

  @Override
  public Integer call() {
      NiceClient niceClient = new NiceClient(host, port);
      niceClient.launch();
      return 0;
  }
}

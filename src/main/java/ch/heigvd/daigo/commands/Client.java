package ch.heigvd.daigo.commands;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import ch.heigvd.daigo.utils.ClientRequest;
import ch.heigvd.daigo.utils.ServerRequest;
import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Start the client part of the network game.")
public class Client implements Callable<Integer> {

    private enum ResponseCommand {
        OK,
        ERROR,
        GAMES,
    }

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
      System.out.println("[Client] Connecting to " + host + ":" + port + "...");

      try (Socket socket = new Socket(host, port);
           Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
           BufferedReader in = new BufferedReader(reader);
           Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
           BufferedWriter out = new BufferedWriter(writer);
           Reader systemInReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
           BufferedReader bsir = new BufferedReader(systemInReader)) {
          System.out.println("[Client] Connected to " + host + ":" + port);
          System.out.println();

          // Display help message
          help();

          // Run REPL until user quits
          while (!socket.isClosed()) {
              // Display prompt
              System.out.print("> ");

              // Read user input
              String userInput = bsir.readLine();

              try {
                  // Split user input to parse command (also known as message)
                  String[] userInputParts = userInput.split(" ");
                  System.out.println(userInputParts[0]);
                  ClientRequest command = ClientRequest.valueOf(userInputParts[0].toUpperCase());

                  // Prepare request
                  String request = command.name();

                  switch (command) {
                      case HELO, JOIN -> {
                          if (userInputParts.length != 2) {
                              System.out.println("[Client] Invalid arguments.");
                              continue;
                          }
                          request = " " + userInputParts[1];
                      }
                      case CREATE, LIST, PASS, FORFEIT ->  {
                          if (userInputParts.length != 1) {
                              System.out.println("ERROR, try again");
                              continue;
                          }
                      }
                      case STONE ->   {
                          request = command + " " + userInputParts[1] + " " + userInputParts[2];
                      }
                      case EXIT ->   {
                          request = command.toString();
                          socket.close();
                      }
                  }

                  if (request != null) {
                      // Send request to server
                      out.write(request + "\n");
                      out.flush();
                  }
              } catch (Exception e) {
                  System.out.println("Invalid command. Please try again.");
                  continue;
              }

              // Read response from server and parse it
              String serverResponse = in.readLine();

              // If serverResponse is null, the server has disconnected
              if (serverResponse == null) {
                  socket.close();
                  continue;
              }

              // Split response to parse message (also known as command)
              String[] serverResponseParts = serverResponse.split(" ", 2);

              ResponseCommand message = null;
              try {
                  message = ResponseCommand.valueOf(serverResponseParts[0]);
              } catch (IllegalArgumentException e) {
                  // Do nothing
              }

              // Handle response from server
              switch (message) {
                  case OK -> {
                      // As we know from the server implementation, the message is always the second part
                      String helloMessage = serverResponseParts[1];
                      System.out.println(helloMessage);
                  }

                  case ERROR -> {
                      if (serverResponseParts.length < 2) {
                          System.out.println("Invalid message. Please try again.");
                          break;
                      }

                      String invalidMessage = serverResponseParts[1];
                      System.out.println(invalidMessage);
                  }
                  case GAMES -> {
                      System.out.println(serverResponseParts[1]);
                  }
                  case null, default ->
                          System.out.println("Invalid/unknown command sent by server, ignore.");
              }
          }

          System.out.println("[Client] Closing connection and quitting...");
      } catch (Exception e) {
          System.out.println("[Client] Exception: " + e);
      }
      return 0;
  }

    private static void help() {
//        System.out.println("Usage:");
//        System.out.println("  " + ClientRequest.HELLO + " <your name> - Say hello with a name.");
//        System.out.println("  " + ClientRequest.HELLO_WITHOUT_NAME + " - Say hello without a name.");
//        System.out.println("  " + ClientRequest.INVALID + " - Send an invalid command to the server.");
//        System.out.println("  " + ClientRequest.QUIT + " - Close the connection to the server.");
//        System.out.println("  " + ClientRequest.HELP + " - Display this help message.");
    }
}

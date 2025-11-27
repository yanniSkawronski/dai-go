package ch.heigvd.daigo.commands;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import ch.heigvd.daigo.utils.ClientRequestCommand;
import ch.heigvd.daigo.utils.Player;
import ch.heigvd.daigo.utils.ServerRequestCommand;
import picocli.CommandLine;

import static ch.heigvd.daigo.utils.ClientRequestCommand.*;

@CommandLine.Command(name = "client", description = "Start the client part of the network game.")
public class Client implements Callable<Integer> {

    private enum ResponseCommand {
        OK,
        ERROR,
        GAME,
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

  private Player current;

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
              try {
                  System.out.print("> ");
                  out.write(validatedRequestToSend(bsir.readLine()) + "\n");
                  out.flush();
              } catch (Exception e) {
                  System.out.println("Invalid command. Please try again.");
                  continue;
              }

              // Read response from server and parse it
              String serverResponse = in.readLine();

              // If serverResponse is null, the server has disconnected
              if (serverResponse == null) {
//                  socket.close();
                  continue;
              }

              // Split response to parse message (also known as command)
              String[] serverResponseParts = serverResponse.split(" ");

              ResponseCommand message = null;
              try {
                  message = ResponseCommand.valueOf(serverResponseParts[0]);
              } catch (IllegalArgumentException e) {
                  // Do nothing
              }

              //Handling the server request

              int errno;

              switch (message) {
                  case OK -> {
                      System.out.println("Server is happy !");
                  }

                  case ERROR -> {
                      errno = Integer.parseInt(serverResponseParts[1]);
                  }
                  case GAME -> {
                      boolean victory =  Boolean.parseBoolean(serverResponseParts[1]);
                      System.out.println(victory ? "Gagné !" : "Perdu.");
                      socket.close();
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

  private static void handleServerRequest(String serverRequest) throws IllegalArgumentException {
      String[] serverRequestParts = serverRequest.split(" ");
      ServerRequestCommand command = ServerRequestCommand.valueOf(serverRequestParts[0]);
      switch (command) {
          case JOINED -> {
              System.out.println("[Client] Player " + serverRequestParts[1] + " joined your game!");
          }
          case PLAY -> {
              System.out.println("Allez joue maintenant là !");
          }
          case PLAYER_STONE -> {
              System.out.println("Player played stone on " + serverRequestParts[1] + " " + serverRequestParts[2]);
          }
          case PLAYER_PASS -> {
              System.out.println("Player passed");
          }
          case PLAYER_FORFEIT -> {
              System.out.println("Player forfeited");
          }
          case PLAYER_DISCONNECT -> {
              System.out.println("Player disconnected");
          }
      }
  }

  private static String validatedRequestToSend(String userInput) throws IllegalArgumentException {
      String[] userInputParts = userInput.split(" ");
      System.out.println(userInputParts[0]);
      ClientRequestCommand command = ClientRequestCommand.valueOf(userInputParts[0].toUpperCase());

      switch (command) {
          case HELO -> {
              return HELO.name() + userInputParts[1];
          }
          case CREATE -> {
              return CREATE.name();
          }
          case LIST -> {
              return LIST.name();
          }
          case JOIN -> {
              return JOIN.name() +  userInputParts[1];
          }
          case STONE -> {
              return STONE.name() + userInputParts[1] + userInputParts[2];
          }
          case PASS -> {
              return PASS.name();
          }
          case FORFEIT ->  {
              return FORFEIT.name();
          }
          default -> {
              throw new IllegalArgumentException();
          }
      }
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

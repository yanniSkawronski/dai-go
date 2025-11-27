package ch.heigvd.daigo.commands;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import ch.heigvd.daigo.utils.ClientRequestCommand;
import ch.heigvd.daigo.utils.Player;
import picocli.CommandLine;

@CommandLine.Command(name = "server", description = "Start the server part of the network game.")
public class Server implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "6433")
  protected int port;

    private List<Player> players = new ArrayList<>();



  @Override
  public Integer call() {
      try (ServerSocket serverSocket = new ServerSocket(port)) {
          System.out.println("[Server] Listening on port " + port);

          while (!serverSocket.isClosed()) {
              try (Socket socket = serverSocket.accept();
                   Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
                   BufferedReader in = new BufferedReader(reader);
                   Writer writer =
                           new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                   BufferedWriter out = new BufferedWriter(writer)) {
                  System.out.println(
                          "[Server] New client connected from "
                                  + socket.getInetAddress().getHostAddress()
                                  + ":"
                                  + socket.getPort());

                  // Run REPL until client disconnects
                  while (!socket.isClosed()) {
                      // Read response from client
                      String clientRequest = in.readLine();
                      System.out.println("[Server] " + clientRequest);

                      // If clientRequest is null, the client has disconnected
                      // The server can close the connection and wait for a new client
                      if (clientRequest == null) {
                          socket.close();
                          continue;
                      }

                      String response = "DDD";

                      // Split user input to parse command (also known as message)

//                      switch (command) {
//                          case JOINED -> {
//                              System.out.println("[Server] Joined game");
//                          }
//                          case PLAY -> {
//                              System.out.println("[Server] Playing game");
//                          }
//                          case PLAYER_STONE -> {
//                              System.out.println("[Server] Player stone game");
//                          }
//                          case PLAYER_PASS -> {
//                              System.out.println("[Server] Player pass game");
//                          }
//                          case PLAYER_FORFEIT -> {
//                              System.out.println("[Server] Player forfeit game");
//                          }
//                          case PLAYER_DISCONNECT -> {
//                              System.out.println("[Server] Player disconnect game");
//                          }
//                          case GAME -> {
//                              System.out.println("[Server] Giving game results");
//                          }
//                          default -> {System.out.println("Invalid/unknown command sent by server, ignore.");}
//                      }

                      // Send response to client
                      out.write(response + "\n");
                      out.flush();
                  }

                  System.out.println("[Server] Closing connection");
              } catch (IOException e) {
                  System.out.println("[Server] IO exception: " + e);
              }
          }
      } catch (IOException e) {
          System.out.println("[Server] IO exception: " + e);
      }
      return 0;
  }

  public void handleClientRequest(String clientRequest) throws IllegalArgumentException {
      String[] clientRequestParts = clientRequest.split(" ", 2);

      ClientRequestCommand command = ClientRequestCommand.valueOf(clientRequestParts[0]);

      String response = command.name();

      switch (command) {
          case HELO -> {
              String playerName = clientRequestParts[1];
              if(!new_player(playerName)) {
                  System.out.println("[Server] Player " + playerName + " already exists");
              }
          }
          case CREATE -> {
              System.out.println("[Server] create");
          }
          case LIST -> {
              System.out.println("[Server] list");
          }
          case JOIN -> {
              System.out.println("[Server] join");
          }
          case STONE -> {
              System.out.println("[Server] stone");
          }
          case PASS -> {
              System.out.println("[Server] pass");
          }
          case FORFEIT -> {
              System.out.println("[Server] forfeit");
          }
          case EXIT -> {
              System.out.println("[Server] exit");
          }
      }
  }

  Optional<Player> findPlayer(String playerName) {
    for (Player player : players) {
        if(player.get_name().equals(playerName))
          return Optional.of(player);
    }
    return Optional.empty();
  }

  private boolean new_player(String playerName) {
      for (Player player : players) {
          if (player.get_name().equals(playerName))
              return false;
      }
      players.add(new Player(playerName));
      return true;
  }

  private void create_game(String playerName) {
      for (Player player : players) {
          if (player.get_name().equals(playerName)) {
              player.create_game();
          }
      }
  }

  private List<Player> available_players() {
      List<Player> available_players = new ArrayList<>();
      for (Player player : players) {
          if(player.available())
              available_players.add(player);
      }
      return available_players;
  }

  private void join_player(String requestPlayerName, String playerNameToJoin) {
      Optional<Player> requestPlayer = findPlayer(requestPlayerName),
              playerToJoin = findPlayer(playerNameToJoin);

      if(requestPlayer.isPresent() &&  playerToJoin.isPresent()
      && requestPlayer.get().available() && playerToJoin.get().available()) {
          requestPlayer.get().set_opponent(playerToJoin.get());
      }
  }
}

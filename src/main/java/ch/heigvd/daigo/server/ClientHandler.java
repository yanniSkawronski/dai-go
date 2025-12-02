package ch.heigvd.daigo.server;

import ch.heigvd.daigo.client.ClientRequest;
import ch.heigvd.go.Board;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles a single client using the DAIGO protocol
 */
class ClientHandler implements Runnable {
    private static final CopyOnWriteArrayList<Game> availableGames = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<String> clients = new CopyOnWriteArrayList<>();

    private final Socket clientSocket;

    private String name;
    Game game = null;

    public ClientHandler(Socket s) {
        this.clientSocket = s;
    }

    /**
     * Add a client to the list of clients, making sure there are no 2 clients with the same name
     * @param name Name of the client
     * @return false if name is taken else true
     */
    private synchronized static boolean add_client(String name) {
        for (String client : clients)
            if (name.equals(client))
                return false;
        clients.add(name);
        return true;
    }

    /**
     * handle HELO message
     * @param userInputs message split by spaces
     * @return response
     */
    String hello(String[] userInputs) {
        if (userInputs.length != 2)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name != null)
            return ServerError.ALREADY_IDENTIFIED.response();

        if (!add_client(userInputs[1]))
            return ServerError.NAME_TAKEN.response();

        this.name = userInputs[1];

        System.out.println("Client Hello " + this.name);
        return ServerReply.OK.toString();
    }

    /**
     * handle CREATE message
     * @param userInputs message split by spaces
     * @return response
     */
    String create(String[] userInputs) {
        if (userInputs.length != 1)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name == null)
            return ServerError.UNIDENTIFIED_CLIENT.response();
        if (this.game != null) {
            if (this.game.isFinished()) {
                this.game = null;
            } else {
                return ServerError.CLIENT_INGAME.response();
            }
        }

        this.game = new Game(this.name);
        availableGames.add(this.game);

        return ServerReply.OK.toString();
    }

    /**
     * handle LIST message
     * @param userInputs message split by spaces
     * @return response
     */
    String list(String[] userInputs) {
        if (userInputs.length != 1)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name == null)
            return ServerError.UNIDENTIFIED_CLIENT.response();

        StringBuilder sb = new StringBuilder(ServerReply.GAMES.toString());
        for (Game game : availableGames) {
            sb.append(" ").append(game.getHostName());
        }
        return sb.toString();
    }

    /**
     * handle JOIN message
     * @param userInputs message split by spaces
     * @return response
     */
    String join(String[] userInputs) {
        if (userInputs.length != 2)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name == null)
            return ServerError.UNIDENTIFIED_CLIENT.response();
        if (this.game != null) {
            if (this.game.isFinished()) {
                this.game = null;
            } else {
                return ServerError.CLIENT_INGAME.response();
            }
        }

        Game new_game = null;
        for (Game game : availableGames) {
            if (game.getHostName().equals(userInputs[1])) {
                new_game = game;
                break;
            }
        }
        if (new_game == null || !availableGames.remove(new_game))
            return ServerError.GAME_NOT_FOUND.response();

        this.game = new_game;

        this.game.joinGame(this.name);

        return ServerReply.OK.toString();
    }

    /**
     * handle PLAY message
     * @param userInputs message split by spaces
     * @return response
     */
    String play(String[] userInputs) {
        if (userInputs.length != 1)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name == null)
            return ServerError.UNIDENTIFIED_CLIENT.response();
        if (this.game == null)
            return ServerError.CLIENT_NOT_INGAME.response();

        GameStatus status = this.game.status(this.name);

        return status.toString();
    }

    /**
     * handle STONE message
     * @param userInputs message split by spaces
     * @return response
     */
    String stone(String[] userInputs) {
        if (userInputs.length != 3)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name == null)
            return ServerError.UNIDENTIFIED_CLIENT.response();
        if (this.game == null)
            return ServerError.CLIENT_NOT_INGAME.response();

        int x, y;
        try {
            x = Integer.parseInt(userInputs[1]);
            y = Integer.parseInt(userInputs[2]);
        } catch (NumberFormatException e) {
            return ServerError.UNKNOWN_MESSAGE.response();
        }
        Optional<ServerError> res = this.game.stone(this.name, x, y);

        return res.map(ServerError::response)
                .orElse(ServerReply.OK.toString());
    }

    /**
     * handle PASS message
     * @param userInputs message split by spaces
     * @return response
     */
    String pass(String[] userInputs) {
        if (userInputs.length != 1)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name == null)
            return ServerError.UNIDENTIFIED_CLIENT.response();
        if (this.game == null)
            return ServerError.CLIENT_NOT_INGAME.response();

        Optional<ServerError> res = this.game.pass(this.name);

        return res.map(ServerError::response)
                .orElse(ServerReply.OK.toString());
    }

    /**
     * handle FORFEIT message
     * @param userInputs message split by spaces
     * @return response
     */
    String forfeit(String[] userInputs) {
        if (userInputs.length != 1)
            return ServerError.UNKNOWN_MESSAGE.response();
        if (this.name == null)
            return ServerError.UNIDENTIFIED_CLIENT.response();
        if (this.game == null)
            return ServerError.CLIENT_NOT_INGAME.response();

        Optional<ServerError> res = this.game.forfeit(this.name);

        if (res.isEmpty()) {
            this.game = null;
        }

        return res.map(ServerError::response)
                .orElse(ServerReply.OK.toString());
    }

    /**
     * handle disconnect, remove player from game and from clients list
     */
    void disconnect() {
        if (this.game != null)
            this.game.disconnect(this.name);
        this.game = null;
        if (this.name != null) {
            clients.removeIf(name -> name.equals(this.name));
        }
    }

    @Override
    public void run() {
        try (clientSocket;
             InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(isr);
             OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw);) {

            String userInput;

            while (!clientSocket.isClosed()) {

                userInput = in.readLine();
                if (userInput == null) {
                    this.disconnect();
                    clientSocket.close();
                    continue;
                }

                String[] userInputs = userInput.split(" ");
                String response;
                try {
                    response = switch (ClientRequest.valueOf(userInputs[0])) {
                        case HELO -> hello(userInputs);
                        case CREATE -> create(userInputs);
                        case LIST -> list(userInputs);
                        case JOIN -> join(userInputs);
                        case PLAY -> play(userInputs);
                        case STONE -> stone(userInputs);
                        case PASS -> pass(userInputs);
                        case FORFEIT -> forfeit(userInputs);
                    };
                } catch (IllegalArgumentException e) {
                    response = ServerError.UNKNOWN_MESSAGE.response();
                }
                out.write(response + '\n');
                out.flush();
            }

        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
        System.out.println("Closing thread...");
    }
}

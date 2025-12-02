package ch.heigvd.daigo.server;

import ch.heigvd.daigo.client.ClientRequest;
import ch.heigvd.go.Board;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

class ClientHandler implements Runnable {
    private static final CopyOnWriteArrayList<Game> availableGames = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<String> clients = new CopyOnWriteArrayList<>();

    private final Socket clientSocket;

    private String name;
    Game game = null;

    public ClientHandler(Socket s) {
        this.clientSocket = s;
    }

    private synchronized static boolean add_client(String name) {
        for (String client : clients)
            if (name.equals(client))
                return false;
        clients.add(name);
        return true;
    }

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

    void disconnect() {
        if (this.game != null)
            this.game.disconnect(this.name);
        this.game = null;
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

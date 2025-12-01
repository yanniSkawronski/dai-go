package ch.heigvd.daigo.goprogs;

import ch.heigvd.go.Board;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.Random;

public class GoServer {
    private final int PORT;

    public GoServer(int port) {
        this.PORT = port;
    }

    public void launch() {
        System.out.println("heiGO Server starting...");

        try(ServerSocket serverSocket = new ServerSocket(PORT);
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); ) {
            while(!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection !");

                executor.submit(new ClientHandler(clientSocket));

            }
        } catch (IOException e) { System.out.println("Error : " + e); }
    }

    private class ClientHandler implements Runnable {
        private static int maxId = 0;
        private static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

        static synchronized int getId() { return ++maxId; }

        private final Socket clientSocket;
        private final int id;

        private String name = "";
        boolean hasIdentified = false;
        private int colour = 0; // 1 for black, -1 for white
        private ClientHandler opponent = null;
        private Board currentGame = null;
        boolean hasCreatedGame = false;
        boolean hasForfeited = false;
        boolean hasDisconnected = false;

        boolean mustPlay() {
            if(currentGame==null)
                return false;
            int boardPlayer = this.currentGame.blackToPlay() ? 1 : -1;
            return !currentGame.isFinished() && this.colour==boardPlayer;
        }
        boolean isInGame() {return hasCreatedGame || this.currentGame!=null;}

        public ClientHandler(Socket s) {
            this.clientSocket = s;
            clients.add(this);
            this.id = getId();
        }

        String hello(String[] userInputs) {
            if(userInputs.length!=2)
                return "ERROR -1";
            if(hasIdentified)
                return "ERROR 7";

            for(ClientHandler client : clients)
                if(client.hasIdentified && userInputs[1].equalsIgnoreCase(client.name))
                    return "ERROR 8";

            name = userInputs[1];
            hasIdentified = true;

            System.out.println("New player: " + name);
            return "OK";
        }

        String create(String[] userInputs) {
            if(userInputs.length!=1)
                return "ERROR -1";
            if(!hasIdentified)
                return "ERROR 1";
            if(isInGame())
                return "ERROR 3";

            hasCreatedGame = true;
            return "OK";
        }

        String list(String[] userInputs) {
            if(userInputs.length!=1)
                return "ERROR -1";
            if(!hasIdentified)
                return "ERROR 1";

            StringBuilder sb = new StringBuilder("GAMES");
            for(ClientHandler client : clients)
                if(client.hasIdentified && client.hasCreatedGame)
                    sb.append(" ").append(client.name);
            return sb.toString();
        }

        String join(String[] userInputs) {
            if(userInputs.length!=2)
                return "ERROR -1";
            if(!hasIdentified)
                return "ERROR 1";
            if(isInGame())
                return "ERROR 3";

            for(ClientHandler client : clients)
                if(client.hasIdentified && client.hasCreatedGame
                        && this != client
                        && userInputs[1].equalsIgnoreCase(client.name)) {
                    opponent = client;
                    client.opponent = this;
                    currentGame = new Board();
                    opponent.currentGame = currentGame;
                    hasCreatedGame = false;
                    opponent.hasCreatedGame = false;
                    hasForfeited = false;
                    opponent.hasForfeited = false;

                    // assign the colours
                    Random random = new Random();
                    int choice = random.nextInt(2);

                    if(choice==0) {
                        colour = 1;
                        opponent.colour = -1;
                    } else {
                        colour = -1;
                        opponent.colour = 1;
                    }

                    System.out.println("New game: " + name + " vs " + opponent.name);
                    return "OK";
                }

            return "ERROR 6";
        }

        String play(String[] userInputs) {
            if(userInputs.length!=1)
                return "ERROR -1";
            if(!hasIdentified)
                return "ERROR 1";
            if(!isInGame())
                return "ERROR 2";
            if(currentGame==null)
                return "WAIT";
            if(opponent.hasForfeited) {
                currentGame = null;
                opponent = null;
                return "FORFEIT";
            }
            if(opponent.hasDisconnected) {
                currentGame = null;
                opponent = null;
                return "DISCONNECTED";
            }
            if(currentGame.isFinished()) {
                int winner = currentGame.winner();
                currentGame = null;
                opponent = null;
                if(winner==colour)
                    return "RESULT 1";
                else if(winner== -1*colour)
                    return "RESULT -1";
                else
                    return "RESULT 0";
            }
            if(!mustPlay())
                return "WAIT " + opponent.name;

            // if this is reached, then it's the player's turn
            // we get the previous move.

            int X = currentGame.getPreviousMove()[0];
            int Y = currentGame.getPreviousMove()[1];

            if(X==-2)
                return "START " + opponent.name;
            if(X==-1)
                return "PASS";

            return "STONE " + X + " " + Y;
        }

        String stone(String[] userInputs) {
            if(userInputs.length!=3)
                return "ERROR -1";
            if(!hasIdentified)
                return "ERROR 1";
            if(!isInGame())
                return "ERROR 2";
            if(!mustPlay())
                return "ERROR 4";
            int X,Y;
            try {
                X = Integer.parseInt(userInputs[1]);
                Y = Integer.parseInt(userInputs[2]);
            } catch (NumberFormatException e) {
                return "ERROR -1";
            }
            if(!currentGame.playStone(X,Y))
                return "ERROR 5";

            return "OK";
        }

        String pass(String[] userInputs) {
            if(userInputs.length!=1)
                return "ERROR -1";
            if(!hasIdentified)
                return "ERROR 1";
            if(!isInGame())
                return "ERROR 2";
            if(!mustPlay())
                return "ERROR 4";
            if(!currentGame.pass())
                return "UNKNOWN ERROR: CANNOT PASS";
            if(currentGame.isFinished())
                System.out.println("Game " + name + " vs " + opponent.name + " has ended.");
            return "OK";
        }

        String forfeit(String[] userInputs) {
            if(userInputs.length!=1)
                return "ERROR -1";
            if(!hasIdentified)
                return "ERROR 1";
            if(!isInGame())
                return "ERROR 2";
            if(!mustPlay())
                return "ERROR 4";

            System.out.println("Game " + name + " vs " + opponent.name + " has ended by forfeit.");
            hasForfeited = true;
            opponent = null;
            currentGame = null;

            return "OK";
        }

        @Override
        public void run() {
            try(clientSocket;
                InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(isr);
                OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
                BufferedWriter out = new BufferedWriter(osw);) {

                String userInput;

                while(!hasDisconnected) {

                    userInput = in.readLine();
                    if(userInput==null)
                        userInput = "DISCONNECT";

                    String serverOutput = "ERROR -1";
                    // IMPORTANT
                    // Do not include de \n at the end of serverOutput,
                    // it will be added later

                    String[] userInputs = userInput.split(" ");
                    switch(userInputs[0]) {

                        case "HELLO" -> serverOutput = hello(userInputs);

                        case "CREATE" -> serverOutput = create(userInputs);

                        case "LIST" -> serverOutput = list(userInputs);

                        case "JOIN" -> serverOutput = join(userInputs);

                        case "PLAY" -> serverOutput = play(userInputs);

                        case "STONE" -> serverOutput = stone(userInputs);

                        case "PASS" -> serverOutput = pass(userInputs);

                        case "FORFEIT" -> serverOutput = forfeit(userInputs);

                        case "DISCONNECT" -> {
                            System.out.println(name +" has disconnected.");
                            if(currentGame!=null)
                                System.out.println("Game " + name + " vs " + opponent.name + " has ended.");
                            hasDisconnected = true;
                            hasIdentified = false;
                            hasCreatedGame = false;
                            currentGame = null;
                            opponent = null;
                        }

                    }
                    if(!hasDisconnected) {
                        out.write(serverOutput + "\n");
                        out.flush();
                    }
                }

            } catch (IOException e) { System.out.println("Error : " + e); }
            System.out.println("Closing thread...");
        }
    }
}

package ch.heigvd.daigo.client;

import ch.heigvd.daigo.server.ServerReply;
import ch.heigvd.go.Board;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NiceClient {
    private final String host;
    private final int PORT;

    private boolean hasCreatedGame;
    private String opponentName;
    private Board board;
    private int colour;
    private boolean sleepWarned;
    private boolean mustQuit = false;

    private ServerCommunicator communicator = null;
    private Scanner keyboard = null;

    public NiceClient(String host, int port) {
        this.host = host;
        this.PORT = port;
        hasCreatedGame = false;
        opponentName = null;
        board = null;
        colour = 0;
        sleepWarned = false;
    }

    private boolean isInGame() {return hasCreatedGame || board!=null;}

    private String sendToServer(String userInput, BufferedWriter out, BufferedReader in) throws IOException {
        out.write(userInput + "\n");
        out.flush();

        String ret = in.readLine();
        if(ret==null)
            throw new RuntimeException("Server has unexpectedly disconnected.");
        return ret;
    }

    private void waitOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            if(!sleepWarned) {
                System.out.println(" Warning: cannot sleep. ");
                sleepWarned = true;
            }
        }
    }

    /**
     * Set up the username
     * @throws IOException
     */
    private void setUsername() throws IOException {
        System.out.println("Enter a username:");
        String keyboardInput = keyboard.nextLine();
        ServerOutput output = communicator.send(ClientRequest.HELO, keyboardInput);

        while(output.reply != ServerReply.OK) {
            switch(output.args[0]) {
                case "-1" -> System.out.println("Enter 1 word:");
                case "8" -> System.out.println("Username already taken, pick another one:");
                default -> System.out.println("setUsername : Server throw unexpected error");
            }
            keyboardInput = keyboard.nextLine();
            output = communicator.send(ClientRequest.HELO, keyboardInput);
        }
        System.out.println("Welcome "+keyboardInput+"!\n");
    }

    /**
     * Display the list of available game
     * @throws IOException
     */
    private void list() throws IOException {
        ServerOutput output = communicator.send(ClientRequest.LIST);

        switch(output.reply) {
            case GAMES -> {
                int nGames = output.args.length;
                if(nGames==0)
                    System.out.println("No available game\n");
                else {
                    System.out.print("Available games ("+nGames+"): "+ output.args[0]);
                    for(int i = 1; i<nGames; ++i)
                        System.out.print(", "+ output.args[i+1]);
                    System.out.print("\n\n");
                }
            }
            case ERROR -> {
                if(output.args[0].equals("1"))
                    System.out.println("User isn't identified\n");
                else
                    System.out.println("list : Server throw unexpected error\n");
            }
            default -> System.out.println("list : Server throw unexpected error\n");
        }
    }

    /**
     * Creates a game
     * @throws IOException
     */
    private void create() throws IOException {
        ServerOutput output = communicator.send(ClientRequest.CREATE);

        switch(output.reply) {
            case OK -> {
                hasCreatedGame = true;
                System.out.println("Game created\nWaiting for another player");
            }
            case ERROR -> {
                switch(output.args[0]) {
                        case "1" -> System.out.println("User isn't identified\n");
                        case "3" -> System.out.println("User is already in a game\n");
                        default -> System.out.println("create : Server throw unexpected error\n");
                }
            }
            default -> System.out.println("list : Server throw unexpected error\n");
        }
    }

    /**
     * Joins an existing game, ask for what colour the player is
     * @throws IOException
     */
    private void join() throws IOException {
        System.out.println("Which game?");
        String keyboardInput = keyboard.nextLine();
        ServerOutput output = communicator.send(ClientRequest.JOIN, keyboardInput);
        switch(output.reply) {

            case OK -> {
                opponentName = keyboardInput;
                System.out.println("Game started with "+opponentName);
                board = new Board();
                output = communicator.send(ClientRequest.PLAY);
                switch(output.reply) {
                    case START -> {
                        System.out.println("You play as black\n");
                        colour = 1;
                    }
                    case WAIT -> {
                        System.out.println("You play as white\n");
                        System.out.println(board);
                        System.out.println("\n\n\nWaiting for "+opponentName);
                        colour = -1;
                    }
                    default -> System.out.println("join : Server throw unexpected error\n");
                }
            }
            case ERROR -> {
                switch(output.args[0]) {
                    case "6" -> System.out.println("There's no such game\n");
                    case "1" -> System.out.println("User isn't identified\n");
                    case "3" -> System.out.println("User is already in a game\n");
                    default -> System.out.println("join : Server throw unexpected error\n");
                }
            }
            default -> System.out.println("join : Server throw unexpected error\n");
        }
    }

    /**
     * CAll this method when the user had created a game and is waiting for
     * somebody to join.
     * @throws IOException
     */
    private void waitingForJoin() throws IOException {
        do {
            waitOneSecond();
            ServerOutput output = communicator.send(ClientRequest.PLAY);

            if(output.reply==ServerReply.WAIT && output.args.length==0) {
                //nobody joined
                System.out.print(".");
            } else if(output.reply==ServerReply.WAIT || output.reply==ServerReply.START) {
                //somebody joined
                opponentName = output.args[0];
                System.out.println("\nGame started with "+opponentName);
                hasCreatedGame = false;
                board = new Board();
                if(output.reply==ServerReply.START) {
                    System.out.println("You play as black\n");
                    colour = 1;
                } else {
                    System.out.println("You play as white\n");
                    System.out.println(board);
                    System.out.println("\n\n\nWaiting for "+opponentName);
                    colour = -1;
                }
            } else
                System.out.println("waitingForJoin : Server throw unexpected error\n");

        } while(hasCreatedGame);
    }

    /**
     * Prompt the user for a move and send it to the server
     * @throws IOException
     */
    private void askMove() throws IOException {
        System.out.println("\n"+board);

        ServerOutput output;
        boolean validMove;
        String keyboardInput;
        int X = -1,Y = -1;
        do{
            validMove = true;
            System.out.println("X,Y - Play a stone at column X, line Y");
            System.out.println("pass - Pass");
            System.out.println("forfeit - Forfeit the game");
            System.out.println("quit - Quit the client");
            System.out.println("Enter your move:");
            keyboardInput = keyboard.nextLine();

            if(keyboardInput.equalsIgnoreCase("pass")) {
                if(!board.pass())
                    throw new RuntimeException("Unable to pass");
                output = communicator.send(ClientRequest.PASS);
                if(output.reply!=ServerReply.OK)
                    throw new RuntimeException("askMove : Server throw unexpected error\n");
            } else if(keyboardInput.equalsIgnoreCase("forfeit")) {
                System.out.println("You lost the game by forfeit.\n");
                opponentName = null;
                board = null;
                output = communicator.send(ClientRequest.FORFEIT);
                if(output.reply!=ServerReply.OK)
                    throw new RuntimeException("askMove : Server throw unexpected error\n");
            } else if(keyboardInput.equalsIgnoreCase("quit")) {
                mustQuit = true;
            } else {

                String[] coords = keyboardInput.replace(" ", "").split(",");

                if(coords.length==2) {
                    try {
                        X = Integer.parseInt(coords[0]);
                        Y = Integer.parseInt(coords[1]);
                    } catch (NumberFormatException e) {
                        X = -1;
                        Y = -1;
                    }
                } else {
                    X = -1;
                    Y = -1;
                }

                if (!board.playStone(X, Y)) {
                    System.out.println("\nThe move is illegal\n");
                    validMove = false;
                } else {
                    output = communicator.send(ClientRequest.STONE, X, Y);
                    if(output.reply!=ServerReply.OK)
                        throw new RuntimeException("askMove : Server throw unexpected error\n");
                }
            }
        } while(!validMove);

        if(!mustQuit && board!=null && !board.isFinished()) {
            System.out.println("\n"+board);
            System.out.println("\n\n\n\nWaiting for " + opponentName);
        }
    }

    /**
     * Call this method when the user is in a game, and
     * waits for the move of their opponent.
     * @return the server's reply
     * @throws IOException
     */
    private ServerOutput getOpponentMove() throws IOException {
        ServerOutput output = communicator.send(ClientRequest.PLAY);

        //first we wait for the move
        while(output.reply==ServerReply.WAIT) {
            waitOneSecond();
            System.out.print(".");
            output = communicator.send(ClientRequest.PLAY);
        }

        return output;
    }

    public void launch() {
        System.out.println("Client starting...");

        try (Socket socket = new Socket(host, PORT);
             OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw);
             InputStreamReader isr = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(isr);
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);) {

            String strServerOutput, keyboardInput;
            String[] strServerOutputs;

            communicator = new ServerCommunicator(in, out);
            keyboard = scanner;

            setUsername();

            while (!mustQuit) {

                if(!isInGame()) { // The player has no current game

                    //print list of games
                    list();

                    do {
                        System.out.println("1 - Refresh list");
                        System.out.println("2 - Create a game");
                        System.out.println("3 - Join a game");
                        System.out.println("4 - Quit");
                        System.out.println("Choose an option:");
                        keyboardInput = keyboard.nextLine();
                    } while(!keyboardInput.matches("[1234]"));

                    switch(keyboardInput) {
                        case "1" -> {}
                        case "2" -> create();
                        case "3" -> join();
                        case "4" -> mustQuit = true;
                    }

                } else if(hasCreatedGame) { // The player is waiting for somebody to join

                    waitingForJoin();

                } else { // The player is in a game with an opponent

                    ServerOutput output = getOpponentMove();
                    int serverResult = -9; //-9 means we never asked the server about the result

                    switch(output.reply) {
                        case START -> askMove();

                        case STONE -> {
                            int X = Integer.parseInt(output.args[0]);
                            int Y = Integer.parseInt(output.args[1]);
                            if(!board.playStone(X,Y))
                                throw new RuntimeException("Illegal move received");

                            askMove();
                        }

                        case PASS -> {
                            if(!board.pass())
                                throw new RuntimeException("Illegal pass received");

                            askMove();
                        }

                        case RESULT -> {
                            if(!board.pass())
                                throw new RuntimeException("Illegal pass received");
                            //Warning: at this point, for the server, the client has no game
                            serverResult = Integer.parseInt(output.args[0]);
                            //check if game is indeed over
                            if(!board.isFinished())
                                throw new RuntimeException("Local game isn't finished");
                        }

                        case FORFEIT -> {
                            System.out.println("\n"+opponentName+" has forfeited the game, you won!\n");
                            board = null;
                            opponentName = null;
                            // the player is no longer in game
                        }
                        case DISCONNECT -> {
                            System.out.println("\n"+opponentName+" has disconnected, you won!\n");
                            board = null;
                            opponentName = null;
                            // the player is no longer in game
                        }
                        default -> System.out.println("Server sent an unknown response : " + output);
                    }

                    // check if game is finished
                    if(board!=null && board.isFinished()) {
                        //partie finie, afficher message de fin
                        // on récupère le résultat du serveur
                        //seulement si c'est pas déjà fait
                        if(serverResult==-9) {
                            output = communicator.send(ClientRequest.PLAY);
                            if(output.reply!=ServerReply.RESULT)
                                throw new RuntimeException("No end result from server");
                            serverResult = Integer.parseInt(output.args[0]);
                        }
                        if(board.winner()!=colour*serverResult)
                            throw new RuntimeException("Local and server result don't match");
                        System.out.println("\n"+board);
                        if(board.winner()==colour)
                            System.out.println("You won! :D\n");
                        else if(board.winner()== -1*colour)
                            System.out.println("You lost :(\n");

                        board = null;
                        opponentName = null;
                        // the player is no longer in game
                    }

                } // end if

            } // end while

        } catch (IOException | RuntimeException e) {
            System.out.println("Error: " + e);
        }

        System.out.println(" Client stopping...");

    }
}

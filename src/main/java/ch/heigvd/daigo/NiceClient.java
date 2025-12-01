package ch.heigvd.daigo;

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
        if(userInput.charAt(userInput.length()-1)=='\n')
            out.write(userInput);
        else
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

    public void launch() {
        System.out.println("Nice Go Client starting...");

        try (Socket socket = new Socket(host, PORT);
             OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw);
             InputStreamReader isr = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(isr);
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);) {

            String serverOutput, keyboardInput;
            String[] serverOutputs;

            // ask for the username
            System.out.println("Enter your username:");
            keyboardInput = scanner.nextLine();
            serverOutput = sendToServer("HELLO "+keyboardInput, out, in);
            while(!serverOutput.equals("OK")) {
                switch(serverOutput) {
                    case "ERROR -1" -> System.out.println("Enter 1 word:");
                    case "ERROR 8" -> System.out.println("Username already taken, pick another one:");
                    default -> throw new RuntimeException("AA "+serverOutput);
                }
                keyboardInput = scanner.nextLine();
                serverOutput = sendToServer("HELLO "+keyboardInput, out, in);
            }
            System.out.println("Welcome to the Nice Go Client, " + keyboardInput + "!\n");

            // The user had given a username.

            boolean mustQuit = false;
            while (!mustQuit) {

                if(!isInGame()) { // The player has no current game

                    System.out.println("You're in no game currently");

                    serverOutput = sendToServer("LIST", out, in);
                    if(serverOutput.startsWith("ERROR"))
                        throw new RuntimeException("BB "+serverOutput);

                    serverOutputs = serverOutput.split(" ");
                    int nGames = serverOutputs.length-1;
                    if(nGames==0)
                        System.out.println("No available games\n");
                    else {
                        System.out.print("Available games ("+nGames+"): "+ serverOutputs[1]);
                        for(int i = 1; i<nGames; ++i)
                            System.out.print(", "+ serverOutputs[i+1]);
                        System.out.print("\n\n");
                    }

                    do {
                        System.out.println("1 - Refresh list");
                        System.out.println("2 - Create a game");
                        System.out.println("3 - Join a game");
                        System.out.println("4 - Quit");
                        System.out.println("Choose an option:");
                        keyboardInput = scanner.nextLine();
                    } while(!keyboardInput.matches("[1234]"));

                    switch(keyboardInput) {

                        case "1" -> {}
                        case "2" -> {
                            serverOutput = sendToServer("CREATE", out, in);
                            if(!serverOutput.equals("OK"))
                                throw new RuntimeException("CC "+serverOutput);
                            hasCreatedGame = true;
                            System.out.print("Game created\nWaiting for another player\n");
                        }
                        case "3" -> {
                            System.out.println("Which game?");
                            keyboardInput = scanner.nextLine();

                            serverOutput = sendToServer("JOIN "+keyboardInput, out, in);
                            switch(serverOutput) {
                                case "ERROR 6" -> System.out.println("There's no such game\n");
                                case "OK" -> {
                                    opponentName = keyboardInput;
                                    System.out.println("Game started with "+opponentName);
                                    board = new Board();
                                    serverOutput = sendToServer("PLAY", out, in);
                                    if(serverOutput.startsWith("START")) {
                                        System.out.println("You play as black\n");
                                        colour = 1;
                                    } else if(serverOutput.startsWith("WAIT")) {
                                        System.out.println("You play as white\n");
                                        System.out.println(board);
                                        System.out.println("\n\n\nWaiting for "+opponentName);
                                        colour = -1;
                                    } else
                                        throw new RuntimeException("DD "+serverOutput);
                                }
                                default -> throw new RuntimeException("EE "+serverOutput);
                            }

                        }
                        case "4" -> {
                            mustQuit = true;
                        }

                    }

                } else if(hasCreatedGame) { // The player is waiting for somebody to join

                    do {
                        waitOneSecond();
                        serverOutput = sendToServer("PLAY", out, in);
                        serverOutputs = serverOutput.split(" ");
                        if(serverOutput.equals("WAIT"))
                            System.out.print(".");
                        else if(serverOutputs[0].equals("WAIT") || serverOutputs[0].equals("START")) {
                            //somebody joined
                            opponentName = serverOutputs[1];
                            System.out.println("\nGame started with "+opponentName);
                            hasCreatedGame = false;
                            board = new Board();

                            if(serverOutputs[0].equals("START")) {
                                System.out.println("You play as black\n");
                                colour = 1;
                            } else {
                                System.out.println("You play as white\n");
                                System.out.println(board);
                                System.out.println("\n\n\nWaiting for "+opponentName);
                                colour = -1;
                            }
                        } else
                            throw new RuntimeException("FF "+serverOutput);

                    } while(hasCreatedGame);

                } else { // The player is in a game with an opponent

                    do {
                        waitOneSecond();
                        serverOutput = sendToServer("PLAY", out, in);
                        serverOutputs = serverOutput.split(" ");
                        int serverResult = -9;
                        boolean askMove = false;
                        switch(serverOutputs[0]) {

                            case "WAIT" -> System.out.print(".");

                            case "START" -> askMove = true;

                            case "STONE" -> {
                                int X = Integer.parseInt(serverOutputs[1]);
                                int Y = Integer.parseInt(serverOutputs[2]);
                                if(!board.playStone(X,Y))
                                    throw new RuntimeException("Illegal move received");

                                askMove = true;
                            }

                            case "PASS" -> {
                                if(!board.pass())
                                    throw new RuntimeException("Illegal pass received");

                                askMove = true;
                            }

                            case "RESULT" -> {
                                if(!board.pass())
                                    throw new RuntimeException("Illegal pass received");
                                //Warning: at this point, for the server, the client has no game
                                serverResult = Integer.parseInt(serverOutputs[1]);
                                //check if game is indeed over
                                if(!board.isFinished())
                                    throw new RuntimeException("Local game isn't finished");
                            }

                            case "FORFEIT" -> {
                                System.out.println("\n"+opponentName+" has forfeited the game, you won!\n");
                                board = null;
                                opponentName = null;
                                // the player is no longer in game
                            }
                            case "DISCONNECT" -> {
                                System.out.println("\n"+opponentName+" has disconnected, you won!\n");
                                board = null;
                                opponentName = null;
                                // the player is no longer in game
                            }
                            default -> throw new RuntimeException("GG "+serverOutput);

                        }

                        if(askMove) {
                            System.out.println("\n"+board);

                            boolean validMove, playStone;
                            int X = -1,Y = -1;
                            do{
                                validMove = true;
                                playStone = true;
                                System.out.println("X,Y - Play a stone at column X, line Y");
                                System.out.println("pass - Pass");
                                System.out.println("forfeit - Forfeit the game");
                                System.out.println("Enter your move:");
                                keyboardInput = scanner.nextLine();

                                if(keyboardInput.equalsIgnoreCase("pass")) {
                                    playStone = false;
                                    if(!board.pass())
                                        throw new RuntimeException("Unable to pass");
                                    serverOutput = sendToServer("PASS", out, in);
                                    if(!serverOutput.equals("OK"))
                                        throw new RuntimeException("HH "+serverOutput);
                                } else if(keyboardInput.equalsIgnoreCase("forfeit")) {
                                    playStone = false;
                                    System.out.println("You lost the game by forfeit.\n");
                                    opponentName = null;
                                    board = null;
                                    serverOutput = sendToServer("FORFEIT", out, in);
                                    if(!serverOutput.equals("OK"))
                                        throw new RuntimeException("II "+serverOutput);
                                } else {

                                    String[] coords = keyboardInput.replace(" ", "").split(",");

                                    try {
                                        X = Integer.parseInt(coords[0]);
                                        Y = Integer.parseInt(coords[1]);
                                    } catch (NumberFormatException e) {
                                        X = -1;
                                        Y = -1;
                                    }

                                    if (!board.playStone(X, Y)) {
                                        System.out.println("\nThe move is illegal\n");
                                        validMove = false;
                                    }
                                }
                            } while(!validMove);

                            //envoyer le coup au serveur
                            if(playStone) {
                                serverOutput = sendToServer("STONE "+X+" "+Y, out, in);
                                if(!serverOutput.equals("OK"))
                                    throw new RuntimeException("JJ "+serverOutput);
                            }

                            if(board!=null && !board.isFinished()) {
                                System.out.println("\n"+board);
                                System.out.println("\n\n\nWaiting for " + opponentName);
                            }

                        }

                        if(board!=null && board.isFinished()) {
                            //partie finie, afficher message de fin
                            // on récupère le résultat du serveur
                            //seulement si c'est pas déjà fait
                            if(serverResult==-9) {
                                serverOutput = sendToServer("PLAY", out,in);
                                serverOutputs = serverOutput.split(" ");
                                if(!serverOutputs[0].equals("RESULT"))
                                    throw new RuntimeException("No end result from server");
                                serverResult = Integer.parseInt(serverOutputs[1]);
                            }
                            if(board.winner()!=colour*serverResult)
                                throw new RuntimeException("Local and server result don't match");
                            System.out.println(board);
                            if(board.winner()==colour)
                                System.out.println("You won! :D\n");
                            else if(board.winner()== -1*colour)
                                System.out.println("You lost :(\n");

                            board = null;
                            opponentName = null;
                            // the player is no longer in game
                        }

                    } while(isInGame());

                } // end if

            } // end while

        } catch (IOException | RuntimeException e) {
            System.out.println("Error: " + e);
        }

        System.out.println("Nice Go client stopping...");

    }
}

package ch.heigvd.daigo.server;

import ch.heigvd.go.Board;

import java.util.Optional;
import java.util.Random;

class Game {
    private String black;
    private String white;
    private final Board board;
    private boolean started = false;
    private boolean forfeited = false;

    public Game(String hostPlayer) {
        this.black = hostPlayer;
        this.board = new Board();
    }

    public synchronized String getHostName() {
        return black;
    }

    public synchronized boolean isFinished() {
        return (this.board.isFinished() || this.forfeited);
    }

    synchronized void joinGame(String guestPlayer) {
        Random random = new Random();
        boolean isBlack = random.nextBoolean();

        this.started = true;

        if (isBlack) {
            this.white = this.black;
            this.black = guestPlayer;
        }
        else {
            this.white = guestPlayer;
        }
    }

    private boolean checkTurn(boolean isBlack) {
        return isBlack == board.blackToPlay();
    }

    private String opponentName(boolean isBlack) {
        return isBlack ? white : black;
    }

    private synchronized boolean amIBlack(String name) {
        return name.equals(this.black);
    }

    synchronized GameStatus status(String playerName) {
        boolean isBlack = amIBlack(playerName);
        if (!started) {
            return new GameStatus();
        }

        String otherPlayer = opponentName(isBlack);
        if (otherPlayer == null) {
            if (forfeited) {
                return new GameStatus(ServerReply.FORFEIT);
            } else {
                return new GameStatus(ServerReply.DISCONNECT);
            }
        }

        int winner = this.board.winner();
        if (board.isFinished()) {
            return new GameStatus(
                isBlack ? winner : (-1 * winner)
            );
        }

        if (checkTurn(isBlack)) {
            return new GameStatus(opponentName(isBlack), false);
        }

        int x = this.board.getPreviousMove()[0];
        int y = this.board.getPreviousMove()[1];

        if (x == -2) {
            return new GameStatus(opponentName(isBlack), true);
        } else if (x == -1) {
            return new GameStatus(ServerReply.PASS);
        } else {
            return new GameStatus(x, y);
        }
    }

    synchronized Optional<ServerError> stone(String playerName, int x, int y) {
        boolean isBlack = amIBlack(playerName);
        if (checkTurn(isBlack)) {
            return Optional.of(ServerError.NOT_CLIENTS_TURN);
        }

        if (!this.board.playStone(x, y)) {
            return Optional.of(ServerError.INVALID_MOVE);
        }

        return Optional.empty();
    }

    synchronized Optional<ServerError> pass(String playerName) {
        boolean isBlack = amIBlack(playerName);
        if (checkTurn(isBlack)) {
            return Optional.of(ServerError.NOT_CLIENTS_TURN);
        }

        if (!this.board.pass()) {
            return Optional.of(ServerError.INVALID_MOVE);
        }

        return Optional.empty();
    }

    synchronized Optional<ServerError> forfeit(String playerName) {
        boolean isBlack = amIBlack(playerName);
        if (checkTurn(isBlack)) {
            return Optional.of(ServerError.NOT_CLIENTS_TURN);
        }

        if (this.forfeited || !this.board.resign()) {
            return Optional.of(ServerError.INVALID_MOVE);
        }

        this.forfeited = true;

        if (isBlack) {
            this.black = null;
        } else {
            this.white = null;
        }

        return Optional.empty();
    }

    synchronized void disconnect(String playerName) {
        boolean isBlack = amIBlack(playerName);
        if (isBlack) {
            this.black = null;
        } else {
            this.white = null;
        }
    }
}

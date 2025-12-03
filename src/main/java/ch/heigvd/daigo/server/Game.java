package ch.heigvd.daigo.server;

import ch.heigvd.go.Board;

import java.util.Optional;
import java.util.Random;

/**
 * A thread safe Game of Go with 2 players
 */
class Game {
    private String black;
    private String white;
    private final Board board;
    private boolean started = false;
    private boolean forfeited = false;

    /**
     * @param hostPlayer the player which created the game
     */
    public Game(String hostPlayer) {
        this.black = hostPlayer;
        this.board = new Board();
    }

    /**
     * @return Returns the hosts name if the game has not yet started, otherwise returns the black players name.
     */
    public synchronized String getHostName() {
        return black;
    }

    /**
     * @return is the game finished (win or forfeit)
     */
    public synchronized boolean isFinished() {
        return (this.board.isFinished() || this.forfeited || (
                this.started && (this.white == null || this.black == null)
            )
        );
    }

    /**
     * A player joins a game which is not yet started. The black player is determined randomly and players can start playing.
     * @param guestPlayer player which joins the game
     * @throws IllegalStateException if the game has already started and is full
     */
    synchronized void joinGame(String guestPlayer) {
        if (this.started) {
            throw new IllegalStateException("Game has already started");
        }
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

    /**
     * Check if it is the players turn
     * @param isBlack is player black
     * @return true if it is the players turn
     */
    private boolean checkTurn(boolean isBlack) {
        return isBlack == board.blackToPlay();
    }

    /**
     * @param isBlack is player black
     * @return The opponents name
     */
    private String opponentName(boolean isBlack) {
        return isBlack ? white : black;
    }

    /**
     * Check which player is the player with the corresponding name
     * @param name Name of the player
     * @return true if the player is the black player
     * @throws IllegalArgumentException if the name is none of the 2 players, this should not happen
     */
    private boolean amIBlack(String name) {
        if (name.equals(this.black))
            return true;
        else if (name.equals(this.white)) {
            return false;
        }
        //this should not happen, so we throw anyway
        throw new IllegalArgumentException("Invalid player name! Something is very wrong");
    }

    /**
     * Get the status of a game from the point of view of a player
     * @param playerName Name of the player
     * @return a GameStatus of the game for the player asking
     */
    synchronized GameStatus status(String playerName) {
        if (!started) {
            return new GameStatus();
        }
        if (forfeited) {
            return new GameStatus(ServerReply.FORFEIT);
        }

        boolean isBlack = amIBlack(playerName);

        String otherPlayer = opponentName(isBlack);
        if (otherPlayer == null) {
            return new GameStatus(ServerReply.DISCONNECT);
        }

        int winner = this.board.winner();
        if (board.isFinished()) {
            return new GameStatus(
                isBlack ? winner : (-1 * winner)
            );
        }

        if (!checkTurn(isBlack)) {
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

    /**
     * Can the player make a move, aka is it his turn and is the game running
     * @param playerName Name of the player
     * @return GAME_NOT_STARTED or GAME_FINISHED or NOT_CLIENTS_TURN or empty if he can play
     */
    private Optional<ServerError> canPlay(String playerName) {
        if (!started) {
            return Optional.of(ServerError.GAME_NOT_STARTED);
        }
        if (isFinished()) {
            return Optional.of(ServerError.GAME_FINISHED);
        }
        if (!checkTurn(amIBlack(playerName))) {
            return Optional.of(ServerError.NOT_CLIENTS_TURN);
        }

        return Optional.empty();
    }

    /**
     * Play a stone
     * @param playerName Name of the player
     * @param x horizontal position
     * @param y vertical position
     * @return Optional empty if stone was successfull otherwise corresponding ServerError
     */
    synchronized Optional<ServerError> stone(String playerName, int x, int y) {
        Optional<ServerError> res = canPlay(playerName);
        if (res.isPresent()) {
            return res;
        }
        if (!this.board.playStone(x, y)) {
            return Optional.of(ServerError.INVALID_MOVE);
        }

        return Optional.empty();
    }

    /**
     * Pass your turn
     * @param playerName Name of the player
     * @return Optional empty if pass was successfull otherwise corresponding ServerError
     */
    synchronized Optional<ServerError> pass(String playerName) {
        Optional<ServerError> res = canPlay(playerName);
        if (res.isPresent()) {
            return res;
        }

        if (!this.board.pass()) {
            return Optional.of(ServerError.INVALID_MOVE);
        }

        return Optional.empty();
    }

    /**
     * Forfeit the game
     * @param playerName Name of the player
     * @return Optional empty if pass was successfull otherwise corresponding ServerError
     */
    synchronized Optional<ServerError> forfeit(String playerName) {
        Optional<ServerError> res = canPlay(playerName);
        if (res.isPresent()) {
            return res;
        }

        if (this.forfeited || !this.board.resign()) {
            return Optional.of(ServerError.INVALID_MOVE);
        }

        this.forfeited = true;

        return Optional.empty();
    }

    /**
     * Disconnect the player
     * @param playerName Name of the player
     */
    synchronized void disconnect(String playerName) {
        if (!started || isFinished()) {
            return;
        }

        boolean isBlack = amIBlack(playerName);
        if (isBlack) {
            this.black = null;
        } else {
            this.white = null;
        }
    }
}

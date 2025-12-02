package ch.heigvd.daigo.server;

/**
 * Represents the state of a game from the point of view of a player.
 * Contains a corresponding server reply to a PLAY message in DAIGO protocol.
 */
class GameStatus {
    private final ServerReply reply;
    private String opponent;
    private int winner;
    private int lastStoneX;
    private int lastStoneY;


    /**
     * default state, the game has not started, player must WAIT
     */
    public GameStatus() {
        this.reply = ServerReply.WAIT;
    }

    /**
     * The player must WAIT or START playing
     * @param opponent the player opponents name
     * @param isStart must the player start or wait
     */
    public GameStatus(String opponent, boolean isStart) {
        this.opponent = opponent;
        if (isStart) {
            this.reply = ServerReply.START;
        } else {
            this.reply = ServerReply.WAIT;
        }
    }

    /**
     * Simply response with no parameters
     * @param reply One of PASS, FORFEIT or DISCONNECT
     * @throws IllegalArgumentException upon invalid reply
     */
    public GameStatus(ServerReply reply) {
        if (reply != ServerReply.PASS && reply != ServerReply.FORFEIT && reply != ServerReply.DISCONNECT) {
            throw new IllegalArgumentException("Invalid ServerReply in GameStatus");
        }
        this.reply = reply;
    }

    /**
     * Game is finished, reply will be RESULT with winner
     * @param winner -1 for white, 1 for black or 0 for draw
     */
    public GameStatus(int winner) {
        this.winner = winner;
        this.reply = ServerReply.RESULT;
    }

    /**
     * A stone was played, reply is STONE
     * @param lastStoneX x, horizontal position
     * @param lastStoneY y, vertical position
     */
    public GameStatus(int lastStoneX, int lastStoneY) {
        this.reply = ServerReply.STONE;
        this.lastStoneX = lastStoneX;
        this.lastStoneY = lastStoneY;
    }

    /**
     * @return returns the Game state as a valid response
     * to a PLAY message in the DAIGO protocol
     */
    @Override
    public String toString() {
        return switch (this.reply) {
            case WAIT -> ServerReply.WAIT.toString() + (this.opponent == null ? "" : " " + this.opponent);
            case START -> ServerReply.START.toString() + " " + this.opponent;
            case STONE -> ServerReply.STONE.toString() + " " + lastStoneX + " " + lastStoneY;
            case PASS, FORFEIT, DISCONNECT -> reply.toString();
            case RESULT -> ServerReply.RESULT.toString() + " " + this.winner;
            default -> {
                throw new IllegalArgumentException("Invalid ServerReply in GameStatus");
            }
        };
    }
}

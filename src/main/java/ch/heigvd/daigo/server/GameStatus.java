package ch.heigvd.daigo.server;

class GameStatus {
    private final ServerReply reply;
    private String opponent;
    private int winner;
    private int lastStoneX;
    private int lastStoneY;


    public GameStatus() {
        this.reply = ServerReply.WAIT;
    }

    public GameStatus(String opponent, boolean isStart) {
        this.opponent = opponent;
        if (isStart) {
            this.reply = ServerReply.START;
        } else {
            this.reply = ServerReply.WAIT;
        }
    }

    public GameStatus(ServerReply reply) {
        if (reply != ServerReply.PASS && reply != ServerReply.FORFEIT && reply != ServerReply.DISCONNECT) {
            throw new IllegalArgumentException("Invalid ServerReply in GameStatus");
        }
        this.reply = reply;
    }

    public GameStatus(int winner) {
        this.winner = winner;
        this.reply = ServerReply.RESULT;
    }

    public GameStatus(int lastStoneX, int lastStoneY) {
        this.reply = ServerReply.STONE;
        this.lastStoneX = lastStoneX;
        this.lastStoneY = lastStoneY;
    }

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

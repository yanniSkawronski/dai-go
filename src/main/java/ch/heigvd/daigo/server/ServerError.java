package ch.heigvd.daigo.server;

public enum ServerError {
    UNKNOWN_MESSAGE (-1),
    UNIDENTIFIED_CLIENT(1),
    CLIENT_NOT_INGAME(2),
    CLIENT_INGAME(3),
    NOT_CLIENTS_TURN(4),
    INVALID_MOVE(5),
    GAME_NOT_FOUND(6),
    ALREADY_IDENTIFIED(7),
    NAME_TAKEN(8),

    INVALID_ERROR(0);

    private final int code;

    ServerError(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static ServerError fromCode(int code) {
        return switch (code) {
            case -1 -> UNKNOWN_MESSAGE;
            case 1 -> UNIDENTIFIED_CLIENT;
            case 2 -> CLIENT_NOT_INGAME;
            case 3 -> CLIENT_INGAME;
            case 4 -> NOT_CLIENTS_TURN;
            case 5 -> INVALID_MOVE;
            case 6 -> GAME_NOT_FOUND;
            case 7 -> ALREADY_IDENTIFIED;
            case 8 -> NAME_TAKEN;
            default -> INVALID_ERROR;
        };
    }

    public String response() {
        return "ERROR " + this.code;
    }
}

package ch.heigvd.daigo.server;

/**
 * One of the possible Errors in the DAIGO protocol
 * with an additional INVALID_ERROR
 */
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
    GAME_NOT_STARTED(9),
    GAME_FINISHED(10),

    INVALID_ERROR(0);

    private final int code;

    ServerError(int code) {
        this.code = code;
    }

    /**
     * @return error code corresponding to the error
     */
    public int code() { return code; }

    /**
     * Decodes an error code into a ServerError
     * @param code error code
     * @return Error corresponding to the error code or INVALID_ERROR
     */
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
            case 9 -> GAME_NOT_STARTED;
            case 10 -> GAME_FINISHED;
            default -> INVALID_ERROR;
        };
    }

    /**
     * Returns the error as a valid DAIGO protocol response
     * @return response message
     */
    public String response() {
        return "ERROR " + this.code;
    }
}

package ch.heigvd.daigo.server;

/**
 * One of the possible server responses in the DAIGO protocol
 */
public enum ServerReply {
    OK,
    GAMES,
    WAIT,
    START,
    STONE,
    PASS,
    FORFEIT,
    DISCONNECT,
    RESULT,
    ERROR
}


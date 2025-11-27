package ch.heigvd.go;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        board = new Board();
    }

    @org.junit.jupiter.api.Test
    void blackToPlay() {
        assertTrue(board.blackToPlay());
        board.playStone(5, 5);
        assertFalse(board.blackToPlay());
        board.playStone(4, 5);
        assertTrue(board.blackToPlay());
    }

    @org.junit.jupiter.api.Test
    void isFinished() {
        assertFalse(board.isFinished());
        board.playStone(5, 5);
        board.pass();
        assertFalse(board.isFinished());
        board.playStone(4, 5);
        assertFalse(board.isFinished());
        board.pass();
        assertFalse(board.isFinished());
        board.pass();
        assertTrue(board.isFinished());
    }

    @org.junit.jupiter.api.Test
    void winner() {
        assertEquals(0, board.winner());
        assertTrue(board.playStone(3, 5));
        board.pass();
        board.pass();
        assertEquals(1, board.winner());
    }

    @org.junit.jupiter.api.Test
    void playStone() {
        assertTrue(board.playStone(5, 5));
        assertFalse(board.playStone(5, 5)); // white
        assertTrue(board.playStone(5, 6)); // white
        assertTrue(board.playStone(6, 6));
        assertTrue(board.playStone(1, 1)); // white
        assertTrue(board.playStone(4, 6));
        assertTrue(board.playStone(1, 2)); // white
        assertTrue(board.playStone(5, 7));

        // check if captured
    }

    @org.junit.jupiter.api.Test
    void pass() {
        assertTrue(board.pass());
        assertTrue(board.pass());
        assertTrue(board.isFinished());
        assertFalse(board.pass());
    }

    @org.junit.jupiter.api.Test
    void resign() {
        assertTrue(board.resign());
        assertEquals(-1, board.winner());
        assertFalse(board.resign());
    }
}
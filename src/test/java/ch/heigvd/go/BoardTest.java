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
        fail("Not yet implemented");
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        fail("Not yet implemented");
    }

    @org.junit.jupiter.api.Test
    void playStone() {
        assertTrue(board.playStone(5, 5));
        assertFalse(board.playStone(5, 5));
        fail("Needs to be expanded test");
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
        fail("Not yet implemented");
    }
}
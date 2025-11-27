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
        assertEquals(Board.Stone.BLACK, board.getStone(5, 5));

        assertTrue(board.playStone(5, 6)); // white
        assertEquals(Board.Stone.WHITE, board.getStone(5, 6));

        board.playStone(6, 6);
        board.pass(); // white
        board.playStone(4, 6);
        board.pass(); // white
        board.playStone(5, 7);

        // check if captured
        assertEquals(Board.Stone.BLANK, board.getStone(5, 6));

        board.playStone(5, 4); // white
        board.pass();
        board.playStone(4, 5); // white
        board.pass();
        board.playStone(6, 5); // white
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

    @org.junit.jupiter.api.Test
    void captureStone() {
        board.playStone(5, 5);

        board.playStone(5, 6); // white

        board.playStone(6, 6);
        board.pass(); // white
        board.playStone(4, 6);
        board.pass(); // white
        board.playStone(5, 7); // black captures white on 5, 6

        // check if captured
        assertEquals(Board.Stone.BLANK, board.getStone(5, 6));

        assertFalse(board.playStone(5, 6)); // check if unable to play if dies

        board.playStone(5, 4); // white
        board.pass();
        board.playStone(4, 5); // white
        board.pass();
        board.playStone(6, 5); // white
        board.pass();
        assertEquals(Board.Stone.BLACK, board.getStone(5, 5));

        assertTrue(board.playStone(5, 6)); // white recaptures

        assertEquals(Board.Stone.WHITE, board.getStone(5, 6));
        assertEquals(Board.Stone.BLANK, board.getStone(5, 5));

        assertFalse(board.playStone(5, 5)); // check if black unable to play if return to previous state

        board.playStone(1, 1);
        board.playStone(1, 2); // white

        assertTrue(board.playStone(5, 5)); // check if black able to play if moves happened
    }

    @org.junit.jupiter.api.Test
    void captureMultipleStones() {
        board.playStone(5, 5);
        board.playStone(6, 6); // white
        board.playStone(6, 5);
        board.playStone(5, 6); // white
        board.playStone(4, 6);
        board.pass();
        board.playStone(7, 6);
        board.pass();
        board.playStone(5, 7);
        board.pass();

        assertEquals(Board.Stone.WHITE, board.getStone(5, 6));
        assertEquals(Board.Stone.WHITE, board.getStone(6, 6));

        board.playStone(6, 7); // black captures

        assertEquals(Board.Stone.BLANK, board.getStone(5, 6));
        assertEquals(Board.Stone.BLANK, board.getStone(6, 6));

        assertTrue(board.playStone(5, 6)); //white ok
        board.pass();
        assertFalse(board.playStone(6, 6)); //white not ok
    }
}
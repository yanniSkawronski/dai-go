package ch.heigvd.go;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Describes a Go board in a particular position.
 */

public class Board {
    // black = 1, white = -1
    private final int[][] current;
    private final int[][] previous;
    private int[] previousMove;
    private boolean blackToPlay;

    /**
     * The additional points for white.
     * Can be negative.
     */
    public final int bonusPoint;
    /**
     * Name of the black player
     */
    public final String black;
    /**
     * Name of the white player
     */
    public final String white;
    private int consecutivePasses;

    private boolean isFinished;
    private int winner;
    private boolean wonByResignation;
    private int blackScore;

    /**
     *
     * @return true if it's black's turn,
     * false if it's white's.
     */
    public boolean blackToPlay(){
        return blackToPlay;
    };

    /**
     *
     * @return true if the game is finished,
     * false otherwise.
     */
    public boolean isFinished(){
        return isFinished;
    }

    /**
     * Returns the winner
     * @return 1 for black, -1 for white, 0 for no winner
     * (or game unfinished)
     */
    public int winner(){
        return winner;
    }

    /**
     * Creates a new game. Black will be the first to play.
     * @param size size of the board (from 1 to 99).
     * @param bonusPoint bonus points for white.
     *                  Recommended value for even game is 7.
     */
    public Board(int size, int bonusPoint) throws IllegalArgumentException {
        if(size<1 || size>99)
            throw new IllegalArgumentException("Board size must be from 1 to 99\n");
        this.bonusPoint = bonusPoint;
        current = new int[size][size];
        previous = new int[size][size];
        previousMove = null;
        blackToPlay = true;
        consecutivePasses = 0;

        isFinished = false;
        winner = 0;
        wonByResignation = false;
        blackScore = 0;

        black = "Black";
        white = "White";
    }

    /**
     * Creates a new game, with board size 9
     * and bonus point for white 7.
     * Black will be the first to play.
     */
    public Board() {
        this(9, 7);
    }

    private char visualiser(int x, int y) {
        if(Arrays.equals(new int[]{x, y}, previousMove))
            return blackToPlay ? 'W' : 'B';
        int colour = current[x][y];
        if(colour==0 || colour==11)
            return ' ';
        return colour > 0 ? 'b' : 'w';
    }

    private String oneLine(int i) {
        int size = current.length;
        StringBuilder sb = new StringBuilder();
        sb.append(visualiser(i,0));
        for(int j = 1; j<size; ++j) {
            sb.append(" — ");
            sb.append(visualiser(i,j));
        }
        return sb.toString();
    }
    private String sepLine() {
        return "|" + "   |".repeat(current.length-1);
    }

    private String twoChar(int i) {
        return i<10 ? " "+i : ""+i;
    }
    private String topLine() {
        int size = current.length;
        StringBuilder sb = new StringBuilder(" ");
        for(int j = 0; j<size; ++j)
            sb.append("  ").append(twoChar(j+1));
        return sb.toString();
    }

    public String toString() {

        int size = current.length;
        StringBuilder sb = new StringBuilder(topLine());
        sb.append("\n 1  ").append(oneLine(0));
        for(int j = 1; j<size; ++j) {
            sb.append("\n    ");
            sb.append(sepLine()).append('\n');
            sb.append(twoChar(j+1)).append("  ");
            sb.append(oneLine(j));
        }

        sb.append('\n');
        if(consecutivePasses>0)
            sb.append(blackToPlay ? white : black).append(" passed\n");
        if(isFinished) {
            sb.append("Game over!\n");
            if(winner==0) {
                sb.append("Nobody won!\n");
            } else {
                sb.append(winner>0? black : white).append(" won by ");
                sb.append(wonByResignation? "resignation\n" : Math.abs(blackScore)+" points\n" );
            }

        } else
            sb.append(blackToPlay ? black : white).append(" to play\n");

        return sb.toString();
    }

    private List<int[]> neighbours(int x, int y) {
        List<int[]> listRet = new ArrayList<int[]>();
        if(x>0)
            listRet.add(new int[]{x-1, y});
        if(x<current.length-1)
            listRet.add(new int[]{x+1, y});
        if(y>0)
            listRet.add(new int[]{x, y-1});
        if(y<current.length-1)
            listRet.add(new int[]{x, y+1});

        return listRet;
    }

    private static boolean containCorrect(List<int[]> list, int[] element) {
        // the .contains() method is stupid so I made my own
        for(int[] point : list)
            if(Arrays.equals(element, point))
                return true;
        return false;
    }

    private boolean canBreathe(int x, int y, List<int[]> visited) {
        visited.add(new int[]{x, y});
        int colour = current[x][y];
        if(colour==0)
            return true;

        boolean ret = false;
        for(int[] point : neighbours(x, y)) {
            if(!containCorrect(visited, point) && current[point[0]][point[1]] != -1*colour)
                ret = canBreathe(point[0], point[1], visited) || ret;
        }
        return ret;
    }
    private boolean canBreathe(int x, int y) {
        List<int[]> visited = new ArrayList<int[]>();
        return canBreathe(x, y, visited);
    }

    private void deleteGroup(int x, int y, int colour, List<int[]> deleted) {
        if(current[x][y]!=colour)
            return;
        current[x][y] = 0;
        deleted.add(new int[]{x, y});
        for(int[] point : neighbours(x, y)) {
            deleteGroup(point[0], point[1], colour, deleted);
        }
    }
    private List<int[]> deleteGroup(int x, int y) {
        List<int[]> deleted = new ArrayList<int[]>();
        if(current[x][y]!=0)
            deleteGroup(x, y, current[x][y], deleted);
        return deleted;
    }

    private void assignTerritory(int x, int y) {
        //determine to whom belongs the emplacement x,y
        //1, -1 : belongs certainly to black, white
        //2, -2 : might belong to black, white
        //0 : undecided
        //11 : belongs certainly to nobody
        //note : even number means it's uncertain
        int i = current[x][y];
        if(i%2!=0)
            return;
        boolean nextToBlack = false, nextToWhite = false, nextToNeutral = false;
        for(int[] point : neighbours(x, y)) {
            int j = current[point[0]][point[1]];
            nextToNeutral = j==11 || nextToNeutral;
            nextToWhite = j<0 || nextToWhite;
            nextToBlack = (j>0 && j<10) || nextToBlack;
        }
        if(nextToNeutral || (nextToBlack && nextToWhite))
            current[x][y] = 11;
        else if(nextToBlack)
            current[x][y] = 2;
        else if(nextToWhite)
            current[x][y] = -2;
    }

    private int calculateScore() {
        for(int c = 0; c<2*current.length; ++c) {
            //we assign the territory for each emplacement
            //we repeat that 2*current.length times
            for(int i = 0; i<current.length; ++i)
                for(int j = 0; j<current.length; ++j)
                    assignTerritory(i, j);
        }

        blackScore = -1*bonusPoint;
        for(int i = 0; i<current.length; ++i)
            for(int j = 0; j<current.length; ++j) {
                int colour = current[i][j];
                if(colour<0)
                    --blackScore;
                else if(colour>0 && colour<10)
                    ++blackScore;
            }

        return blackScore;
    }

    /**
     * Places a stone on the board. (1, 1) is at the top left.
     * @param X horizontal coordinate, starting from the left (lowest is 1)
     * @param Y vertical coordinate, starting from the top (lowest is 1)
     * @return True if the move is valid, false otherwise
     */
    public boolean playStone(int X, int Y) {
        int x = Y-1, y = X-1;
        if(Math.max(x,y) >= current.length || Math.min(x,y) < 0 ||
                current[x][y]!=0 || isFinished)
            return false;

        consecutivePasses = 0;
        int playing = blackToPlay ? 1 : -1;

        current[x][y] = playing;

        // check if nearby enemy stones can breathe
        // if not, we destroy them and save their positions
        List<int[]> deletedStones = new ArrayList<int[]>();
        for(int[] point : neighbours(x, y)) {
            if(current[point[0]][point[1]] == -1*playing && !canBreathe(point[0], point[1]))
                deletedStones.addAll(deleteGroup(point[0], point[1]));
        }

        // check if the played stone can breathe AND ko rule
        if(!canBreathe(x, y) || Arrays.deepEquals(current, previous)) {
            current[x][y] = 0;
            // restore destroyed enemy stones
            for(int[] point : deletedStones)
                current[point[0]][point[1]] = -1*playing;
            return false;
        } else { // if not, the move is valid
            blackToPlay = !blackToPlay;
            if(previousMove != null)
                previous[previousMove[0]][previousMove[1]] = -1*playing;
            previousMove = new int[]{x, y};
            return true;
        }

    }

    /**
     * The player passes
     * @return True if the move is valid, false otherwise
     */
    public boolean pass() {
        if(isFinished)
            return false;
        blackToPlay = !blackToPlay;
        previousMove = null;
        ++consecutivePasses;
        if(consecutivePasses>1) {
            isFinished = true;
            wonByResignation = false;
            winner = Integer.compare(calculateScore(), 0);
        }
        return true;
    }

    /**
     * The player resigns
     * @return True if the move is valid, false otherwise
     */
    public boolean resign() {
        if(isFinished)
            return false;
        isFinished = true;
        winner = blackToPlay ? -1 : 1;
        wonByResignation = true;
        return true;
    }
}

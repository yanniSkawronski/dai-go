public class Main {
    public static void main(String[] args) {

        System.out.println("Hello World!");
        BoardState board = new BoardState(7,2);
        System.out.print(board);
        board.playStone(2,1);
        System.out.println(board.playStone(1,1));
        System.out.print(board);
        board.playStone(1,2);
        board.playStone(4,4);
        System.out.print(board);
        System.out.println(board.pass());
        board.pass();
        System.out.print(board);
        System.out.println(board.resign());
    }
}

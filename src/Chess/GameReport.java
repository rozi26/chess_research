package Chess;

import Chess.ChessMethods;
import Data.Data;
import Data.Data.ByteBuilder;

import java.util.ArrayList;
import java.util.List;

public class GameReport {


    //start properties
    final List<Move> moveList;//save the moves in linked list
    final Data.ByteBuilder moveBuilder;//save the moves in storage format
    final long startTime;//when the game started

    //end properties
    long endTime;//when the game ended
    int gameResult;//the result of the game (-1: bot2 won, 0: draw, 1:bot1 won)
    int gameResultReason;//why the game ended

    //game stats properties
    int moves = 0;//count how many moves the game had

    public GameReport() //constructor
    {
        startTime = System.currentTimeMillis();
        moveBuilder = new Data.ByteBuilder();
        moveList = new ArrayList<>();
    }
    public void addMove(char[] board, Move move)//add the next move to the report
    {
        moves++;
        moveList.add(move);
        moveBuilder.add(move.getMoveCode(board));
    }
    public void reportEnd(char[] board, ChessMethods.DrawListener drawListener, int wpd, int bpd) //end the report
    {
        endTime = System.currentTimeMillis();
        gameResultReason = getEndingReason(board,drawListener,wpd,bpd);
        gameResult = gameResultReason < 2?gameResultReason * 2 - 1:0;
    }
    private int getEndingReason(char[] board, ChessMethods.DrawListener listener, int wpd, int bpd) //return why the game is ended
    {
        final int draw = listener.getWhyDraw(board);
        if(draw != 0) return draw;
        final int white_matOrPat = ChessMethods.whiteInMatOrPat(board,bpd);
        if(white_matOrPat != 0) return white_matOrPat == 1?0:5;
        ChessMethods.switchSides(board);
        final int black_matOrPat = ChessMethods.whiteInMatOrPat(board,wpd);
        if(black_matOrPat != 0) return black_matOrPat == 1?1:6;
        return 0;
    }
    public Move[] getMoves() //return array of the moves that in the report
    {
        Move[] arr = new Move[moveList.size()];
        for(int i = 0; i < moveList.size(); i++)
            arr[i] = moveList.get(i);
        return arr;
    }

    public static String getReasonText(int reason) //convert ending id to text that explain why the game is ended
    {
        return switch (reason) {
            case 0 -> "black won";
            case 1 -> "white won";
            case 2 -> "draw (last three move repeat)";
            case 3 -> "draw (last 50 moves was returnable)";
            case 4 -> "draw (no one can win in this arrangement)";
            case 5 -> "draw (white in pat)";
            case 6 -> "draw (black in pat)";
            default -> "error unknown reason (reason id: " + reason + ")";
        };
    }
}

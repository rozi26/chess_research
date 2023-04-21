package Chess;
import AI.AI;
import GUI.GameReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessGame {


    private final char[] board;//the game board
    private int bot1_pawns_double = -1;//if the bot1's pawns moves two blocks
    private int bot2_pawns_double = -1;//if the bot2's pawns moves two blocks
    private final boolean[] bot1_castle_moves = new boolean[3];//if the bot1's king and rooks moved
    private final boolean[] bot2_castle_moves = new boolean[3];//if the bot2's king and rooks moved

    private final AI bot1;//the bot who play the white
    private final AI bot2;//the bot who play the black
    public ChessGame(AI _bot1, AI _bot2) //define chess game where bot1 start
    {
        bot1 = _bot1;
        bot2 = _bot2;
        board = ChessMethods.getDefaultBoard();
    }
    public GameReport playGame(boolean bot1_start)//start a chess game without GameReader
    {
        return playGame(bot1_start,null);
    }
    public GameReport playGame(boolean bot1_start, GameReader reader)//start a chess game (reader is object from the type GameReader that stream the game)
    {
        GameReport reporter = new GameReport();//collect the game stats
        ChessMethods.DrawListener drawListener = new ChessMethods.DrawListener();//check if there is a draw
        boolean bot1_turn = bot1_start;//if its bot1 turn

        if(reader != null) reader.clear();
        while (true)//run the game
        {

            AI bot;//the bot who play
            boolean[] bot_castle_moves;//if the bot's tools that important to castle moved
            boolean[] other_castle_moves;
            int other_pawns_double;//if the other bots pawns add double move
            if(bot1_turn)//preparing the data
            {
                bot = bot1;
                bot_castle_moves = bot1_castle_moves;
                other_castle_moves = bot2_castle_moves;
                other_pawns_double = bot2_pawns_double;
            }
            else
            {
                bot = bot2;
                bot_castle_moves = bot2_castle_moves;
                other_castle_moves = bot1_castle_moves;
                other_pawns_double = bot1_pawns_double;
                ChessMethods.switchSides(board);
            }
            if(ChessMethods.whiteInMatOrPat(board,other_pawns_double) != 0 || drawListener.checkDraw(board)){if(!bot1_turn) //check if the game is over
                ChessMethods.switchSides(board); break;}

            Move move = bot.play(board,bot_castle_moves,other_castle_moves,other_pawns_double);//get the move
            //if(move == null) {if(!bot1_turn){Chess.ChessMethods.switchSides(board);} break;}
            //update the castle tools and the pawns double
            bot1_pawns_double = bot2_pawns_double = -1;
            if(board[move.getFrom()] == 'a' && move.getFrom() - move.getTo() == 16) //check if there is a pawn the moved two squares (because of en Passant)
            {
                if (bot1_turn) bot1_pawns_double = move.getTo() % 8;
                else bot2_pawns_double = move.getTo() % 8;
            }
            if(move.getFrom() == 56 || move.getFrom() == 60 || move.getFrom() == 63) //check for move that effects castling
            {
                if(bot1_turn) bot1_castle_moves[(move.getFrom() - 56) / 3] = true;
                else bot2_castle_moves[(move.getFrom() - 56) / 3] = true;
            }
            if (!bot1_turn) {
                ChessMethods.switchSides(board);
                move = move.reverse();
            }
            reporter.addMove(board,move); //add the move to the reporter
            ChessMethods.executeMove(board,move); //execute the move

            if(reader != null) reader.update(move);
            drawListener.addMove(board,move);
            bot1_turn = !bot1_turn;
        }
        reporter.reportEnd(board,drawListener,bot1_pawns_double,bot2_pawns_double); //create report of the game
        return reporter;
    }

}

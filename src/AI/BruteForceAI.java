package AI;

import java.util.List;
import java.util.Random;
import Chess.ChessMethods;
import Chess.Move;

public class BruteForceAI extends AI{
    final static Random random = new Random();

    final int depth;
    public BruteForceAI(int _depth, boolean aggressive)
    {
        super("Brute_Force_" + _depth,aggressive);
        depth = _depth;
    }
    public Move play(char[] board, boolean[] castleMoves, boolean[] otherCastleMoves, int opponentPawnsDouble)
    {
        CalculateMoves calculator = new CalculateMoves(board,castleMoves,otherCastleMoves,opponentPawnsDouble);
        calculator.calculate(depth);
        return calculator.getFinalMove();
    }
    private class CalculateMoves//the class that find the best move
    {
        Move FINAL_MOVE = null;

        final char[] board;// the game board
        final boolean[] b1CastleMoves;
        final boolean[] b2CastleMoves;
        final int king_start_loc;
        int b1_pawn_double;
        int b2_pawn_double;
        boolean b1Turn = true;
        private CalculateMoves(char[] _board, boolean[] b1C,boolean[] b2C, int opponentPawnsDouble)//set up the class
        {
            board = _board;
            b1CastleMoves = b1C;
            b2CastleMoves = b2C;
            b1_pawn_double = -1;
            b2_pawn_double = opponentPawnsDouble;
            king_start_loc = getWhiteKingStartLoc();
        }
        private void addMoveScore(MoveChooser chooser, Move move, int level)//get move as input, calculate the move score and if the move is good add to chooser
        {
            boolean king_safe = true;
            //int score = getToolScore(move.getEat());
            int score = 0;
            if (move.getEat() != 'n')
            {
                if(b1Turn) score = getToolScore(move.getEat());
                else
                {
                    score = getToolScore((char)(move.getEat() - 6));
                   // System.out.println("score is " + getToolScore((char)(move.getEat() - 6)) + " insted of " + getToolScore(move.getEat()));
                }
            }
            if(level > 0)
            {
                final int pawn_double_save = b1Turn?b2_pawn_double:b1_pawn_double;//save the state of the double pawn move
                b1_pawn_double = b2_pawn_double = -1;//reset the double pawn moves
                final boolean[] castleMoves = b1Turn?b1CastleMoves:b2CastleMoves;
                final int castle_move_save = ((move.getFrom() == 56 || move.getFrom() == 60 || move.getFrom() == 63) && !castleMoves[(move.getFrom() - 56) / 3])?(move.getFrom() - 56) / 3:-1;//get if one of the peaces the important for castle moved for the first time
                if(castle_move_save != -1) castleMoves[castle_move_save] = true;
                if(move.getFrom() == 'a' && move.getTo() - move.getFrom() == 16)
                {
                    if(b1Turn) b1_pawn_double = move.getFrom() % 8;
                    else b2_pawn_double = move.getFrom() % 8;
                }
                ChessMethods.executeMove(board,move);
                king_safe = level != depth || (ChessMethods.getIfBlockIsSafe(move.getFrom()==king_start_loc?move.getTo():king_start_loc,board));

                if(king_safe)//if the king isn't safe after this the move is illegal
                {
                    b1Turn = !b1Turn;
                    ChessMethods.switchSides(board);//simulate the move

                    score -= calculate(level - 1);

                    ChessMethods.switchSides(board);
                    b1Turn = !b1Turn;
                }
                ChessMethods.undoMove(board,move);//undo the move

                b1_pawn_double = b2_pawn_double = -1;//reset the double pawn moves
                if(b1Turn) b2_pawn_double = pawn_double_save;
                else b1_pawn_double = pawn_double_save;
                if(castle_move_save != -1) castleMoves[castle_move_save] = false;
            }
            if(king_safe && chooser.checkIfExcepted(score)) chooser.sendMove(move);
        }
        private int calculate(int level)//get the best move in depth of level
        {
            MoveChooser chooser = new MoveChooser();
            final int pawn_double = (b1Turn)?b2_pawn_double:b1_pawn_double;
            for(int i = 0; i < 64; i++)//for each block in the board
            {
                if(ChessMethods.isWhite(board[i]))//if the block have white tool
                {
                    List<Integer> locations = ChessMethods.WhiteToolMove(board,i,pawn_double);//get where the tool can move to
                    for(int loc:locations)
                    {
                        final Move move = getMove(i,loc);// get the move
                        addMoveScore(chooser,move,level);
                    }
                }
            }

            final boolean[] castleMoves = b1Turn?b1CastleMoves:b2CastleMoves;
            if(ChessMethods.white_can_caste_b(board,castleMoves))
                addMoveScore(chooser,new Move(2),level);
            if(ChessMethods.white_can_caste_s(board,castleMoves))
                addMoveScore(chooser,new Move(3),level);
            if(level == depth)
            {
                FINAL_MOVE = chooser.getBestMove();
              //l  System.out.println(FINAL_MOVE);
            //    System.out.println("score " + chooser.bestScore);
            }
            return chooser.bestScore;
        }
        private Move getMove(int from, int to)
        {
            return new Move(from,to,board);
        }
        private Move getFinalMove()
        {
            return FINAL_MOVE;
        }

        private int getWhiteKingStartLoc()
        {
            for(int i = 0; i < 64; i++)
                if(board[i] == 'f') return i;
            System.out.println("error king doesn't found");
            return -1;
        }
    }

    public int getDepth()
    {
        return depth;
    }
}
package AI;
import java.util.Random;

import Chess.ChessMethods;
import Chess.Move;

public class AI {

    private final String id;
    private final boolean aggressive;
    //private final boolean opponentAggressive;
    public AI(String _id, boolean isAggressive)
    {
        id = _id;
        aggressive = isAggressive;
    }
    public String getId(){return id;}
    public String getAggressiveId()
    {
        return aggressive?"aggressive":"defensive";
    }
    public boolean isAggressive(){return aggressive;}
    //public boolean isOpponentAggressive(){return opponentAggressive;}

    public Move play(char[] board, boolean[] castleMoves, boolean[] otherCastleMoves, int opponentPawnsDouble)
    {
        return null;
    }

    //score methods
    public int getToolScore(char tool)//score tool from self prospective
    {
        boolean isBlack = ChessMethods.isBlack(tool);
        if(isBlack) tool = (char)(tool - 6);
        final int score = getPieceScore(tool);
        return isBlack?score + ((aggressive)?1:-1):score;
    }
    private int getPieceScore(char tool)
    {
        return switch (tool) {
            case 'a' -> 8;
            case 'b', 'c' -> 24;
            case 'd' -> 40;
            case 'e' -> 72;
            case 'f' -> 5000;
            default -> 0;
        };
    }

    public int getDepth()
    {
        return -1;
    }

    //
    Random random = new Random();
    protected class MoveChooser
    {
        Move best = null;
        int bestScore = 0;
        int repeatCount = 1;
        public MoveChooser()
        {

        }
        public boolean checkIfExcepted(int score)
        {
            if(score > bestScore || best == null)//if the score is bigger than the previous score the move is excepted
            {
                bestScore = score;
                repeatCount = 1;
                return true;
            }
            if(score == bestScore)
            {
               repeatCount++;
               return  getProbability(1.0/repeatCount);
            }
            return false;
        }
        public boolean getProbability(double probability)
        {
            return random.nextDouble() < probability;
        }
        public void sendMove(Move newBest)
        {
            best = newBest;
        }
        public int getBestScore(){return bestScore;}
        public Move getBestMove(){return best;}
    }


}

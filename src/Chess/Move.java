package Chess;

import Data.Data;

public class Move {
    private final int from;
    private final int to;
    private final char eat;
    private final int become;
    private final boolean passant;
    private final boolean castle;
    public Move(int castle_id)//define move by castle id
    {
        from = castle_id;
        to = castle_id;
        eat = 'n';
        become = 0;
        passant = false;
        castle = true;
    }
    public Move(int _from, int _to, char _eat, int _become, boolean _passant)//define move by all the move propitiates
    {
        from = _from;
        to = _to;
        eat = _eat;
        become = _become;
        passant = _passant;
        castle = false;
    }
    public Move(int _from, int _to, char[] board)//build move before the move execute!!!
    {
        from = _from;
        to = _to;
        if(board[_from] == 'a')
        {
            become = _to < 8?4:0;
            passant = (Math.abs(_from - _to - 8) == 1 && board[_to] == 'n');
            eat = passant?'g':board[_to];
        }
        else if(board[_from] == 'g')
        {
            become = _to > 55?4:0;
            passant = (Math.abs(_to - _from - 8) == 1 && board[_to] == 'n');
            eat = passant?'a':board[_to];
        }
        else
        {
            become = 0;
            eat = board[_to];
            passant = false;
        }
        castle = false;
    }
    public Move(boolean[] code, int start, char[] board)//build move from move code
    {
        boolean[] fromB = new boolean[6];
        System.arraycopy(code, start, fromB, 0, 6);
        from = Data.binaryToInt(fromB);
        final boolean isWhite = ChessMethods.isWhite(board[from]);
        if((isWhite && board[from] == 'a') || (!isWhite && board[from] == 'g'))
        {
            int toA;
            if(code[start + 7])
            {
                if(code[start + 6]) toA = 7;
                else toA = 9;
            }
            else
            {
                if(code[start + 6]) toA = 8;
                else toA = 16;
            }
            to = from + ((isWhite)?-toA:toA);
            passant = code[start + 8];
            if(passant) eat = (isWhite)?'g':'a';
            else eat = board[to];

            if((isWhite && to < 8) || (!isWhite && to > 55))
                become = Data.binaryToInt(new boolean[]{code[start + 9],code[start + 10]}) + 1;
            else become = 0;
        }
        else
        {
            boolean[] toB = new boolean[6];
            System.arraycopy(code, start + 6, toB, 0, 6);
            to = Data.binaryToInt(toB);
            eat = board[to];
            become = 0;
            passant = false;
        }
        castle = from == to;
    }
    public Move reverse()//return the same move only from the other side
    {
        if(from == to)
            return new Move((from < 2)?from + 2:from - 2);
        return new Move(ChessMethods.oppositeBlock(from), ChessMethods.oppositeBlock(to),eat == 'n'?'n':((char)(eat > 102?eat - 6:eat + 6)),become,passant);
    }

    public int getFrom(){return from;}
    public int getTo(){return to;}
    public char getEat(){return eat;}
    public int getBecome(){return become;}
    public boolean isPassant(){return passant;}
    public boolean isCrown()
    {
        return become != 0;
    }
    public boolean isCastle()
    {
        return castle;
    }

    public int getMoveCodeLength(char[] board)
    {
        if(board[from] == 'a' || board[from] == 'g') return 9 + ((getBecome() == 0) ? 0 : 2);
        return 12;
    }
    public boolean[] getMoveCode(char[] board)//return the move code (the board need to be before the move execute)
    {
        final boolean[] code = new boolean[getMoveCodeLength(board)];
        final boolean[] b1 = Data.intToBinary(from,6);//copy from
        System.arraycopy(b1, 0, code, 0, 6);
        if(board[from] == 'a' || board[from] == 'g')
        {
            code[6] = Math.abs(from - to) < 9;
            code[7] = eat != 'n';
            code[8] = passant;
            if(become != 0)
                System.arraycopy(Data.intToBinary(become - 1,2),0,code,9,2);
        }
        else
        {
            final boolean[] b2 = Data.intToBinary(to,6);//copy to
            System.arraycopy(b2, 0, code, 6, 6);
        }
        return code;
    }

    public boolean equal(Move other)
    {
        return from == other.getFrom() && to == other.getTo() && eat == other.getEat() && become == other.getBecome() && passant == other.isPassant();
    }
    public boolean equal_reverse(Move other)
    {
        return from == other.getTo() && to == other.getFrom() && eat == other.getEat() && become == other.getBecome() && passant == other.isPassant();
    }

    public String toString()
    {
        String space = "";
        if(getTo() < 10) space += " ";
        if(getFrom() < 10) space += " ";
        return from + " -> " + to + space + " (" + eat + ") [" + become + "],[" + ((passant)?'1':'0') + "]";
    }
    public Move clone()
    {
        return new Move(from,to,eat,become,passant);
    }
}

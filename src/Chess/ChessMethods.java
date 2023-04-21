package Chess;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChessMethods{
    public static char[] getDefaultBoard()
    {
        char[] board = new char[64];
        for(int i = 8;  i < 16; i++){board[i] = 'g';}
        for(int i = 16;  i < 48; i++){board[i] = 'n';}
        for(int i = 48;  i < 56; i++){board[i] = 'a';}
        char[] val = new char[]{'d','b','c','e','f','c','b','d'};
        for(int i = 0; i < 8; i++) {board[i] = (char)(val[i] + 6);board[i + 56] = val[i];}
        return board;
    }

    public static int oppositeBlock(int i){return (7 - (i / 8)) * 8 + (i % 8);}
    public static void switchSides(char[] board)//switch the positions of the white and black pieces
    {
        char[] newTools = new char[64];
        for(int i = 0; i < 64; i++)
        {
            newTools[i] = board[oppositeBlock(i)];
            if(newTools[i] < 103)
                newTools[i] = (char)((int)(newTools[i]) + 6);
            else if(newTools[i] != 'n')
                newTools[i] = (char)((int)(newTools[i]) - 6);
        }
        System.arraycopy(newTools, 0, board, 0, 64);
    }
    public static boolean isWhite(char tool)
    {
        return tool > 96 && tool < 103;
    }
    public static boolean isBlack(char tool)//if the tool is black
    {
        return tool > 102 && tool < 109;
    }

    //safe methods
    private static boolean getIfBlocksIsSafe(int[] blocks, char[] board) //check if the black tools can eat tool that in one of this blocks
    {
        int min = 63;
        int max = 0;
        for(int block:blocks)//check if black pawn can eat in some of this blocks
        {
            if(block > 7 && ((block % 8 != 0 && board[block - 9] == 'g') || (block % 8 != 7 && board[block - 7] == 'g'))) return false;
            min = Math.min(min,block);
            max = Math.max(max,block);
        }
        for(int i = 0; i < 64; i++)
        {
            if(isBlack(board[i]) && board[i] != 'g')
            {
                List<Integer> moves = ToolMove(board,i,(char)(board[i] - 6));
                for(int move:moves)
                {
                    if(move >= min && move <= max)
                    {
                        for(int block:blocks)
                        {
                            if(block == move) return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    public static boolean getIfBlockIsSafe(int loc, char[] board) //only work for white
    {
        final boolean eatWhite = ChessMethods.isBlack(board[loc]);
        final boolean[] dir = calculateDir(loc);

        if(!eatWhite && !dir[0] && ((!dir[2] && board[loc - 9] == 'g') || (!dir[3] && board[loc - 7] == 'g'))) return false;
        if(eatWhite  && !dir[1] && ((!dir[2] && board[loc + 7] == 'a') || (!dir[3] && board[loc + 9] == 'a'))) return false;

        final char KC = eatWhite?'b':'h';
        List<Integer> knightRange = hoursMove(board,loc,eatWhite);
        for(int i: knightRange) if(board[i] == KC) return false;

        final char QC = eatWhite?'e':'k';
        final char RC = eatWhite?'d':'j';
        List<Integer> rookRange = towerMove(board,loc,eatWhite);
        for(int i: rookRange) if(board[i] == RC || board[i] == QC) return false;

        final char BC = eatWhite?'c':'i';
        List<Integer> bishopRange = bisMove(board,loc,eatWhite);
        for(int i: bishopRange) if(board[i] == BC || board[i] == QC) return false;

        final char FC = eatWhite?'f':'l';
        List<Integer> kingRange = kingMove(board,loc,eatWhite);
        for(int i: kingRange) if(board[i] == FC) return false;

        return true;
    }
    //castle
    public static boolean white_can_caste_s(char[] board, boolean[] tool_moves)
    {
        if(tool_moves[1] || tool_moves[2]) return false;//if the king or the rook moved
        if(board[60] != 'f' || board[61] != 'n' || board[62] != 'n' || board[63] != 'd') return false;
        return getIfBlocksIsSafe(new int[]{60,61,62,63},board);
    }
    public static boolean white_can_caste_b(char[] board, boolean[] tool_moves)
    {
        if(tool_moves[1] || tool_moves[0]) return false;
        if(board[56] != 'd' || board[57] != 'n' || board[58] != 'n' || board[59] != 'n' || board[60] != 'f') return false;
        return getIfBlocksIsSafe(new int[]{56,57,58,59,60},board);
    }

    //move execution
    public static void executeMove(char[] board, Move m)//execute the move m in board
    {
        if(m.getFrom() == m.getTo())//castle
        {
            if(m.getFrom() == 0) {board[0] = 'n'; board[2] = 'l'; board[3] = 'j'; board[4] = 'n';}
            else if(m.getFrom() == 1) {board[4] = 'n'; board[5] = 'j'; board[6] = 'l'; board[7] = 'n';}
            else if(m.getFrom() == 2) {board[56] = 'n'; board[58] = 'f'; board[59] = 'd'; board[60] = 'n';}
            else if(m.getFrom() == 3) {board[60] = 'n'; board[61] = 'd'; board[62] = 'f'; board[63] = 'n';}
        }
        else
        {
            if(board[m.getFrom()] == 'a' && board[m.getTo()] == 'n' && Math.abs(m.getFrom() - m.getTo() - 8) == 1) board[m.getTo() + 8] = 'n';
            if(board[m.getFrom()] == 'g' && board[m.getTo()] == 'n' && Math.abs(m.getTo() - m.getFrom() - 8) == 1) board[m.getTo() - 8] = 'n';
            final char become = (m.getBecome() == 0)?board[m.getFrom()]:(char)(97 + m.getBecome() + (isWhite(board[m.getFrom()])?0:6));
            board[m.getTo()] = become;
            board[m.getFrom()] = 'n';
        }
    }
    public static void undoMove(char[] board, Move m)//undo the move m in board
    {
        if(m.getFrom() == m.getTo())
        {
            if(m.getFrom() == 0) {board[0] = 'j'; board[2] = 'n'; board[3] = 'n'; board[4] = 'l';}
            else if(m.getFrom() == 1) {board[4] = 'l'; board[5] = 'n'; board[6] = 'n'; board[7] = 'j';}
            else if(m.getFrom() == 2) {board[56] = 'd'; board[58] = 'n'; board[59] = 'n'; board[60] = 'f';}
            else if(m.getFrom() == 3) {board[60] = 'f'; board[61] = 'n'; board[62] = 'n'; board[63] = 'd';}
        }
        else
        {
            board[m.getFrom()] = (m.getBecome() == 0)?board[m.getTo()]:(isWhite(board[m.getTo()])?'a':'g');
            if(m.isPassant()){if(isWhite(board[m.getTo()]))board[m.getTo() + 8] = 'g';else board[m.getTo() - 8] = 'a'; board[m.getTo()] = 'n';}
            else  board[m.getTo()] = m.getEat();
        }
    }
    public static List<Integer> ToolMove(char[] board, int loc, int opponentPawnsDouble)//return the blocks that the tool in "loc" can go to
    {
        if(isBlack(board[loc])) return BlackToolMove(board,loc,opponentPawnsDouble);
        else return WhiteToolMove(board,loc,opponentPawnsDouble);
    }
    public static List<Integer> WhiteToolMove(char[] board, int loc, int opponentPawnsDouble)// return the blocks that the white tool in "loc" can go to
    {
        final char kind = board[loc];
        if(isWhite(kind))
        {
            if(kind == 'a') return whitePawnMove(board,loc,opponentPawnsDouble);
            return ToolMove(board,loc,kind);
        }
        return null;
    }
    public static List<Integer> BlackToolMove(char[] board, int loc, int opponentPawnsDouble)// return the blocks that the black tool in "loc" can go to
    {
        final char kind = board[loc];
        if(isBlack(kind))
        {
            if(kind == 'g') return blackPawnMove(board,loc,opponentPawnsDouble);
            return ToolMove(board,loc,(char)(kind - 6));
        }
        return null;
    }
    private static List<Integer> ToolMove(char[] board, int loc, char kind)//return the places the tool that isn't a pawn in "loc" can go to
    {
        final boolean eatWhite = isBlack(board[loc]);
        return switch (kind)
        {
            case 'b' -> hoursMove(board,loc,eatWhite);
            case 'c' -> bisMove(board, loc, eatWhite);
            case 'd' -> towerMove(board, loc, eatWhite);
            case 'e' -> queenMove(board, loc, eatWhite);
            case 'f' -> kingMoveSafe(board,loc, eatWhite);
            default -> null;
        };
    }
    private static boolean[] calculateDir(int loc)// return if the location is on the side
    {
        boolean[] dir = new boolean[4];
        dir[0] = loc < 8;//the loc is in the top line (up)
        dir[1] = loc > 55;//the loc is in the bottom line (down)
        dir[2] = loc % 8 == 0;//the loc is in the left column (left)
        dir[3] = loc % 8 == 7;//the loc is in the right column (right)
        return dir;
    }
    private static boolean exceptMove(boolean eatWhite, char kind)
    {
        return kind == 'n' || (eatWhite ^ isBlack(kind));
    }
    protected static List<Integer> whitePawnMove(char[] tools, int loc, int opponentPawnsDouble)// white pawn move blocks
    {
        final boolean[] dir = calculateDir(loc);
        List<Integer> list = new ArrayList<>();
        if(!dir[0] && tools[loc - 8] == 'n')
        {
            list.add(loc - 8);
            if(loc < 56 && loc >= 48 && tools[loc - 16] == 'n')
                list.add(loc - 16);
        }
        final boolean phantom_possible = opponentPawnsDouble != -1 && loc > 23 && loc < 32;
        if((!dir[3] && !dir[0] && isBlack(tools[loc - 7])) || (phantom_possible && opponentPawnsDouble == loc % 8 + 1))
            list.add(loc - 7);
        if((!dir[2] && !dir[0] && isBlack(tools[loc - 9])) || (phantom_possible && opponentPawnsDouble == loc % 8 - 1))
            list.add(loc - 9);
        return list;
    }
    protected static List<Integer> blackPawnMove(char[] tools, int loc, int opponentPawnsDouble)// black pawn move blocks
    {
        final boolean[] dir = calculateDir(loc);
        List<Integer> list = new ArrayList<>();
        if(!dir[1] && tools[loc + 8] == 'n')
        {
            list.add(loc + 8);
            if(loc < 16 && loc >= 8 && tools[loc + 16] == 'n') list.add(loc + 16);
        }
        final boolean phantom_possible = opponentPawnsDouble != -1 && loc > 31 && loc < 40;
        if((!dir[3] && !dir[1] && tools[loc + 9] != 'n') || (phantom_possible && opponentPawnsDouble == loc % 8 + 1)) list.add(loc + 9);
        if((!dir[2] && !dir[1] && tools[loc + 7] != 'n') || (phantom_possible && opponentPawnsDouble == loc % 8 - 1)) list.add(loc + 7);
        return list;
    }
    protected static List<Integer> hoursMove(char[] tools, int loc, boolean eatWhite)// knight move blocks
    {
        final boolean[] dir = calculateDir(loc);
        List<Integer> list = new ArrayList<>();
        if(loc > 15 && !dir[2] && exceptMove(eatWhite,tools[loc - 17])) list.add(loc - 17);
        if(loc > 15 && !dir[3] && exceptMove(eatWhite,tools[loc - 15])) list.add(loc - 15);
        if(!(loc % 8 == 0 || loc % 8 == 1) && !dir[0] && exceptMove(eatWhite,tools[loc - 10])) list.add(loc - 10);
        if(!(loc % 8 == 6 || loc % 8 == 7) && !dir[0] && exceptMove(eatWhite,tools[loc - 6])) list.add(loc - 6);
        if(!(loc % 8 == 0 || loc % 8 == 1) && !dir[1] && exceptMove(eatWhite,tools[loc + 6])) list.add(loc + 6);
        if(!(loc % 8 == 6 || loc % 8 == 7) && !dir[1] && exceptMove(eatWhite,tools[loc + 10])) list.add(loc + 10);
        if(loc < 48 && !dir[2] && exceptMove(eatWhite,tools[loc + 15])) list.add(loc + 15);
        if(loc < 48 && !dir[3] && exceptMove(eatWhite,tools[loc + 17])) list.add(loc + 17);
        return list;
    }
    protected static List<Integer> bisMove(char[] tools, int loc, boolean eatWhite)// bishop move blocks
    {
        List<Integer> list = new ArrayList<>();
        for(int i = loc + 7; i < 64; i+= 7)
        {
            if(i % 8 == 7) break;
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        for(int i = loc + 9; i < 64; i+= 9)
        {
            if(i % 8 == 0) break;
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        for(int i = loc - 7; i >= 0; i-= 7)
        {
            if(i % 8 == 0) break;
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        for(int i = loc - 9; i >= 0; i-= 9)
        {
            if(i % 8 == 7) break;
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        return list;
    }
    protected static List<Integer> towerMove(char[] tools, int loc, boolean eatWhite)// rook move blocks
    {
        List<Integer> list = new ArrayList<>();
        for (int i = loc - 8; i >= 0; i -= 8) {
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        for (int i = loc + 8; i < 64; i += 8) {
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        for (int i = loc + 1; i % 8 != 0; i++) {
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        for (int i = loc - 1; i % 8 != 7 && i != -1; i--) {
            if(tools[i] != 'n') {if(eatWhite ^ isBlack(tools[i])) list.add(i); break;}
            list.add(i);
        }
        return list;
    }
    protected static List<Integer> queenMove(char[] tools, int loc, boolean eatWhite)// queen move blocks
    {
        List<Integer> bisMoves = bisMove(tools,loc, eatWhite);
        bisMoves.addAll(towerMove(tools,loc,eatWhite));
        return bisMoves;
    }
    protected static List<Integer> kingMove(char[] tools, int loc, boolean eatWhite)// king move blocks
    {
        final boolean[] dir = calculateDir(loc);
        List<Integer> list = new ArrayList<>();
        if(!dir[0] && exceptMove(eatWhite,tools[loc - 8])) list.add(loc - 8);
        if(!dir[1] && exceptMove(eatWhite,tools[loc + 8])) list.add(loc + 8);
        if(!dir[3])
        {
            if(exceptMove(eatWhite,tools[loc + 1])) list.add(loc + 1);
            if(!dir[1] && exceptMove(eatWhite,tools[loc + 9])) list.add(loc + 9);
            if(!dir[0] && exceptMove(eatWhite,tools[loc - 7])) list.add(loc - 7);
        }
        if(!dir[2])
        {
            if(exceptMove(eatWhite,tools[loc - 1])) list.add(loc - 1);
            if(!dir[1] && exceptMove(eatWhite,tools[loc + 7])) list.add(loc + 7);
            if(!dir[0] && exceptMove(eatWhite,tools[loc - 9])) list.add(loc - 9);
        }
        return list;
    }
    private static List<Integer> kingMoveSafe(char[] tools, int loc, boolean eatWhite)
    {
        if(eatWhite) {switchSides(tools); loc = oppositeBlock(loc);}
        List<Integer> moves = kingMove(tools,loc,false);
        List<Integer> approve = new ArrayList<>();
        for (final int to : moves) {
            final Move move = new Move(loc,to,tools);
            executeMove(tools,move);
            final boolean safe = getIfBlockIsSafe(to,tools);
            undoMove(tools,move);
            if(safe)
                approve.add(eatWhite?oppositeBlock(to):to);
        }
        if(eatWhite) switchSides(tools);
        return approve;
    }

    //game over
    public static int whiteInMatOrPat(char[] board, int blackDoublePawn)//return 1 if the white king in mat or -1 if the white king in pat
    {
        int kingLoc = 0; for(int i = 1; i < 64; i++){if(board[i] == 'f'){kingLoc = i;break;}} //get the king location
        final List<Integer> kingSafeMoves = kingMoveSafe(board,kingLoc,false);
        if(kingSafeMoves.size() != 0) return 0;
        for(int i = 0; i < 64; i++)
        {
            if(isWhite(board[i]))
            {
                final boolean kingMove = i == kingLoc;
                final List<Integer> toolMove = WhiteToolMove(board,i,blackDoublePawn);
                for(int to:toolMove)
                {
                    Move move = new Move(i,to,board);
                    executeMove(board,move);
                    final boolean safe = getIfBlockIsSafe(kingMove?to:kingLoc,board);
                    undoMove(board,move);
                    if(safe) return 0;
                }
            }
        }
        return (getIfBlockIsSafe(kingLoc,board))?-1:1;
    }
    public static int blackInMatOrPath(char[] board,int whiteDoublePawn)
    {
        switchSides(board);
        final int res = whiteInMatOrPat(board,whiteDoublePawn);
        switchSides(board);
        return res;
    }

    public static class DrawListener
    {
        private static class ThreeReturnMoves
        {
            static class MoveLink
            {
                final Move value;
                MoveLink next = null;
                private MoveLink(Move move){value = move;}
            }
            int MovesCount = 0;
            MoveLink first = null;
            MoveLink last = null;
            private boolean addMove(Move move)
            {
                if(last == null)
                {
                    first = new MoveLink(move);
                    last = first;
                }
                else
                {
                    MoveLink next = new MoveLink(move);
                    last.next = next;
                    last = next;
                }
                if(MovesCount == 6)
                {
                    first = first.next;
                    return allEqual();
                }
                else
                    MovesCount++;
                return false;

            }
            private boolean allEqual()
            {
                if(MovesCount < 6)  return false;
                final boolean equal1= first.value.equal_reverse(first.next.next.value) && first.value.equal(first.next.next.next.next.value);
                if(!equal1) return false;
                return first.next.value.equal_reverse(first.next.next.next.value) && first.next.value.equal(first.next.next.next.next.next.value);
            }
        }
        ThreeReturnMoves returnMoves;//check for "Threefold repetition" law
        int returnableMovesCounter = 0;//check for "Fifty-move rule" law

        public DrawListener() {returnMoves = new ThreeReturnMoves();}
        public void addMove(char[] board, Move move) // run after execute
        {
            returnMoves.addMove(move); //if there "Threefold repetition"
            if(move.getEat() != 'n' || board[move.getTo()] == 'a' || board[move.getTo()] == 'g') returnableMovesCounter = 0; //if the move is nonreturnable
            else returnableMovesCounter++;
        }
        public boolean checkDraw(char[] board)
        {
            return returnMoves.allEqual() || returnableMovesCounter >= 50 || checkCantWin(board);
        }
        public int getWhyDraw(char[] board)
        {
            if(returnMoves.allEqual()) return 2;
            if(returnableMovesCounter >= 50) return 3;
            if(checkCantWin(board)) return 4;
            else return 0;
        }
        public String getWhyDrawS(char[] board)
        {
            return getReasonText(getWhyDraw(board));
        }
        private boolean checkCantWin(char[] board)
        {
            int whiteTools = 0;
            int blackTools = 0;
            int[] toolsCount = new int[12];
            for(int i = 0; i < 64; i++)//count how much there from each tool king
            {
                if(board[i] != 'n')
                {
                    toolsCount[board[i] - 97]++;
                    if(isWhite(board[i])) whiteTools++;
                    else blackTools++;
                }
            }
            return whiteTools <= 2 && blackTools <= 2 && toolsCount[0] == 0 && toolsCount[3] == 0 && toolsCount[4] == 0 && toolsCount[6] == 0 && toolsCount[9] == 0 && toolsCount[10] == 0;
        }
        private static String getReasonText(int reason) //convert ending id to text that explain why the game is ended
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


    //Monitoring
    public static boolean approveGameLegit(Move[] moves)//check game follow chess rules
    {
        final char[] board = getDefaultBoard();
        boolean[] white_castle_tools = new boolean[3];
        boolean[] black_castle_tools = new boolean[3];
        int white_double_pawn = -1;
        int black_double_pawn = -1;

        boolean whiteTurn = isWhite(board[moves[0].getFrom()]);
        int move_count = 1;
        for(Move move:moves)
        {
            String fail;
            if((move.getFrom() == move.getTo()?move.getFrom() > 1:isWhite(board[move.getFrom()])) != whiteTurn)
                fail = "game disapprove, move in the wrong turn";
            else
                fail = approveMoveLegit(board,move,whiteTurn?white_castle_tools:black_castle_tools,whiteTurn?black_double_pawn:white_double_pawn);
            if(!fail.isEmpty())
            {
                System.out.println("[" + move_count + "] " + fail);
                return false;
            }
            if(whiteTurn)
            {
                black_double_pawn = -1;
                if(move.getFrom() == 56 || move.getFrom() == 60 || move.getFrom() == 63) white_castle_tools[(move.getFrom() - 56) / 3] = true;
                if(board[move.getFrom()] == 'a' && move.getFrom() - move.getTo() == 16) white_double_pawn = move.getFrom() % 8;
            }
            else
            {
                white_double_pawn = -1;
                if(move.getFrom() == 0 || move.getFrom() == 4 || move.getFrom() == 7) black_castle_tools[move.getFrom() / 3] = true;
                if(board[move.getFrom()] == 'g' &&  move.getTo() - move.getFrom() == 16) black_double_pawn = move.getFrom() % 8;
            }
            executeMove(board,move);
            whiteTurn = !whiteTurn;
            move_count++;
        }
        return true;
    }
    public static String approveMoveLegit(char[] board, Move move, boolean[] castle_tools, int double_pawn)//check if move follow chess rules
    {
        final boolean whiteMove = isWhite(board[move.getFrom()]);
        final String C1 = (whiteMove)?"white":"black";
        final String moveS = "(" + move.toString() + ")";
        if(move.getEat() != 'n' && isWhite(board[move.getFrom()]) && isWhite(move.getEat())) return "game disapprove, there is move that make " + C1 + " tool eat other " + C1 + " tool " + moveS;
        if(move.getTo() == move.getFrom())
        {
            int castleId = move.getFrom();
            final boolean black = castleId < 2;
            if(black)
            {
                switchSides(board);
                castleId += 2;
            }
            boolean legit = (castleId == 2 && white_can_caste_b(board,castle_tools)) || (castleId == 3 && white_can_caste_s(board,castle_tools));
            if(black) switchSides(board);
            if(!legit) return "game disapprove, illegal castle " + moveS;
        }
        else
        {
            if(board[move.getFrom()] == 'n') return "game disapprove, there is a move that start from empty square " + moveS;
            List<Integer> togo = ToolMove(board,move.getFrom(),double_pawn);
            boolean found = false;
            for(int to:togo) if(move.getTo() == to){found = true;break;}
            if(!found) return "game disapprove, there is a move that doesn't follow the game rules " + moveS;
            if(move.isPassant() && move.getTo() % 8 != double_pawn) return "game disapprove, illegal passant " + moveS;
        }
        return "";
    }

    //board analyze


}

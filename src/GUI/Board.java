package GUI;

import AI.BruteForceAI;
import Chess.ChessMethods;
import Chess.Move;
import Data.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class Board extends JFrame {
    final String IMAGE_FOLDER_PATH = Data.getDataFolder() + "colors\\";

    final int SQUARE_SIZE = 120;
    final int MENU_WIDTH = 200;

    final int BOARD_SIZE = SQUARE_SIZE*8;
    final JButton[] Squares = new JButton[64];
    final Button[] Buttons;


    char[] board;
    char[] colors = new char[64];
    boolean user_turn = true;
    ChessMethods.DrawListener drawListener;

    Thread ai_thread;
    Thread game_thread;
    final char[] game_board = new char[64];

    boolean ai_aggressive = true;
    int ai_depth = 3;

    int white_double = -1;
    int black_double = -1;
    boolean[] whiteC = new boolean[3];
    boolean[] blackC = new boolean[3];
    int prev_loc = -1;

    final Label label;


    public Board()
    {
        //create the page
        setSize(BOARD_SIZE+MENU_WIDTH,BOARD_SIZE+50);
        setLocationRelativeTo(null);
        setLayout(null);
        setTitle("chess tester");

        //create the menu
        label = new Label();
        label.setLocation(BOARD_SIZE+20,20);
        label.setSize(MENU_WIDTH-40,30);
        label.setFont(new Font("arial",Font.BOLD,24));
        add(label);

        //create the board
        for(int i = 0; i < 64; i++)
        {
            JButton b = new JButton();
            b.setLocation((i%8)*SQUARE_SIZE,(i/8)*SQUARE_SIZE);
            b.setSize(SQUARE_SIZE,SQUARE_SIZE);
            b.addActionListener(blockClick);
            add(b);
            Squares[i] = b;
        }
        reset();

        List<String> names = new ArrayList<>();
        List<Action> clicks = new ArrayList<>();

        names.add("new game");
        clicks.add(newGame);
        names.add("bot aggressive");
        clicks.add(changeState);
        names.add("bot depth 4");
        clicks.add(changeLevel);
        names.add("move white");
        clicks.add(playWhite);
        names.add("move black");
        clicks.add(playBlack);
        names.add("bot play game");
        clicks.add(playGame);

        Buttons = new Button[names.size()];
        int count = 0;
        int y = 100;
        for(String name:names)
        {
            Button button = new Button();
            button.setLocation(BOARD_SIZE+20,y);
            button.setSize(MENU_WIDTH-40,60);
            y += 70;
            button.setFont(new Font("arial",Font.BOLD,20));
            button.setLabel(name);
            button.addActionListener(clicks.get(count));
            add(button);
            Buttons[count] = button;
            count++;
        }



        setVisible(true);
    }


    private void reset()
    {
        if(ai_thread != null && ai_thread.isAlive()) ai_thread.stop();
        if(game_thread != null && game_thread.isAlive()) game_thread.stop();
        drawListener = new ChessMethods.DrawListener();
        label.setText("your turn");
        user_turn = false;
        prev_loc = -1;
        board = ChessMethods.getDefaultBoard();
        clearColor();
        update();
        user_turn = true;
    }
    private void update()
    {
        for(int i = 0; i < 64; i++)
            Squares[i].setIcon(getImage(board[i],colors[i]));
    }
    private void clearColor()
    {
        for(int i = 0; i < 64; i++)
            colors[i] = ((i/8%2==0)?(i%2==0):(i%2==1))?'w':'b';
    }
    private ImageIcon getImage(char kind, char color)
    {
        //String IMAGE_FOLDER_PATH = "F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\";
        //if(color == 'o') color = 'r';
        return switch (kind) {
            case 'n' -> new ImageIcon(IMAGE_FOLDER_PATH + color + ".png");
            case 'a' -> new ImageIcon(IMAGE_FOLDER_PATH + "whitesol" + color + ".png");
            case 'b' -> new ImageIcon(IMAGE_FOLDER_PATH + "whitehorse" + color + ".png");
            case 'c' -> new ImageIcon(IMAGE_FOLDER_PATH + "whitebis" + color + ".png");
            case 'd' -> new ImageIcon(IMAGE_FOLDER_PATH + "whiteHook" + color + ".png");
            case 'e' -> new ImageIcon(IMAGE_FOLDER_PATH + "whitequeen" + color + ".png");
            case 'f' -> new ImageIcon(IMAGE_FOLDER_PATH + "whiteking" + color + ".png");
            case 'g' -> new ImageIcon(IMAGE_FOLDER_PATH + "blacksol" + color + ".png");
            case 'h' -> new ImageIcon(IMAGE_FOLDER_PATH + "blackhorse" + color + ".png");
            case 'i' -> new ImageIcon(IMAGE_FOLDER_PATH + "blackbis" + color + ".png");
            case 'j' -> new ImageIcon(IMAGE_FOLDER_PATH + "blackhook" + color + ".png");
            case 'k' -> new ImageIcon(IMAGE_FOLDER_PATH + "blackqueen" + color + ".png");
            case 'l' -> new ImageIcon(IMAGE_FOLDER_PATH + "blackking" + color + ".png");
            default -> null;
        };
    }

    private boolean executeMove(Move move)
    {
        white_double = -1;
        black_double = -1;
        if(move.isCastle())
        {
            if(move.getFrom()>1) whiteC[1] = true;
            else blackC[1] = true;
        }
        else
        {
            boolean white = ChessMethods.isWhite(board[move.getFrom()]);
            if(white)
            {
                if(move.getFrom() == 56) whiteC[0] = true;
                if(move.getFrom() == 60) whiteC[1] = true;
                if(move.getFrom() == 63) whiteC[2] = true;
                if(board[move.getFrom()] == 'a' && move.getTo()+16==move.getFrom())
                    white_double = move.getFrom()%8;
            }
            else
            {
                if(move.isCastle()) blackC[1] = true;
                if(move.getFrom() == 0) blackC[0] = true;
                if(move.getFrom() == 4) blackC[1] = true;
                if(move.getFrom() == 7) blackC[2] = true;
                if(board[move.getFrom()] == 'g' && move.getTo()-16==move.getFrom())
                    black_double = move.getFrom()%8;
            }
        }
        ChessMethods.executeMove(board,move);
        clearColor();
        update();
        drawListener.addMove(board,move);
        user_turn = false;
        if(drawListener.checkDraw(board))
        {
            label.setText("draw");
            return true;
        }
        final int white_lost = ChessMethods.whiteInMatOrPat(board,black_double);
        if(white_lost != 0)
        {
            label.setText(white_lost==1?"black won":"draw");
            return true;
        }
        ChessMethods.switchSides(board);
        final int black_lost = ChessMethods.whiteInMatOrPat(board,white_double);
        ChessMethods.switchSides(board);
        if(black_lost != 0)
        {
            label.setText(black_lost==1?"white won":"draw");
            return true;
        }
        user_turn = true;
        return false;
    }

    private void playBotMove(boolean white)
    {
        if(!user_turn) return;
        label.setText("bot loading");
        final BruteForceAI bot = new BruteForceAI(ai_depth,ai_aggressive);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                user_turn = false;
                Move move;
                if(white)
                    move = bot.play(board,whiteC,blackC,black_double);
                else
                {
                    ChessMethods.switchSides(board);
                    move = bot.play(board,blackC,whiteC,white_double);
                    ChessMethods.switchSides(board);
                    move = move.reverse();
                }
                label.setText("your turn");
                user_turn = !executeMove(move);
            }
        };
        ai_thread = new Thread(runnable);
        ai_thread.start();
    }

    Action blockClick = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!user_turn) return;
            Object src = e.getSource();
            int index = 0;
            for(int i = 1; i < 64; i++)
                if(Squares[i].equals(src)){index = i; break;}
            if(user_turn && (colors[index] == 'y' || colors[index] == 'c'))
            {
                Move move;
                if(colors[index] == 'y') move = new Move(prev_loc,index,board);
                else move = new Move(index==58?2:3);
                if(executeMove(move)) return;
                prev_loc = -1;
                playBotMove(false);
                return;
            }
            clearColor();
            if(ChessMethods.isWhite(board[index]))
            {

                if(prev_loc == index)
                    prev_loc = -1;
                else
                {
                    if(board[index] == 'f')
                    {
                        if(ChessMethods.white_can_caste_b(board,whiteC))
                            colors[58] = 'c';
                        if(ChessMethods.white_can_caste_s(board,whiteC))
                            colors[62] = 'c';
                    }
                    List<Integer> locs = ChessMethods.ToolMove(board,index,black_double);
                    for(int loc:locs)
                        colors[loc] = 'y';
                    prev_loc = index;
                }
            }
            update();
        }
    };
    AbstractAction newGame = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            reset();
        }
    };
    AbstractAction changeState = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(ai_aggressive)
                Buttons[1].setLabel("bot defensive");
            else Buttons[1].setLabel("bot aggressive");
            ai_aggressive = !ai_aggressive;
        }
    };
    AbstractAction changeLevel = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ai_depth++;
            if(ai_depth == 5)
                ai_depth = 1;
            Buttons[2].setLabel("bot depth " + (ai_depth+1));
        }
    };
    AbstractAction playWhite = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            playBotMove(true);
        }
    };
    AbstractAction playBlack = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            playBotMove(false);
        }
    };
    AbstractAction playGame = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(game_thread != null && game_thread.isAlive())
            {
                game_thread.stop();
                Buttons[5].setLabel("start game");
                label.setText("your turn");
                System.arraycopy(game_board,0,board,0,64);
                update();
                return;
            }
            Buttons[5].setLabel("stop");
            label.setText("running game");
            Runnable play = new Runnable() {
                @Override
                public void run() {
                    BruteForceAI ai = new BruteForceAI(ai_depth,ai_aggressive);
                    boolean white_turn = true;
                    boolean over = !user_turn;
                    while (!over)
                    {
                        System.arraycopy(board,0,game_board,0,64);
                        Move move;
                        if(white_turn) move = ai.play(board,whiteC,blackC,black_double);
                        else
                        {
                            ChessMethods.switchSides(board);
                            move = ai.play(board,blackC,whiteC,white_double);
                            ChessMethods.switchSides(board);
                            move = move.reverse();
                        }
                        white_turn = !white_turn;
                        over = executeMove(move);
                    }
                    Buttons[5].setLabel("start game");
                }
            };
            game_thread = new Thread(play);
            game_thread.start();
        }
    };
}

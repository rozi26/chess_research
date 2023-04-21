package GUI;

import Chess.ChessMethods;
import Chess.Move;
import Data.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;

public class GameReader implements KeyListener{
    private class MoveList
    {
        class MoveLink
        {
            final Move move;
            MoveLink prev = null;
            MoveLink(Move _move) {move = _move;}
        }
        MoveLink last = null;
        private void push(Move m)
        {
            if(last == null) last = new MoveLink(m);
            else
            {
                MoveLink l = new MoveLink(m);
                l.prev = last;
                last = l;
            }
        }
        private Move pop()
        {
            if(last == null) return null;
            Move m = last.move;
            last = last.prev;
            return m;
        }
    }

    int move_count = 0;
    int index = 0;
    final boolean[] code;
    MoveList moveList;


    char[] tools = new char[64];
    JButton[] blocks;
    JFrame frame;
    public GameReader(String gameCode)//to replay game
    {
        code = Data.StringToBinary(gameCode);
        moveList = new MoveList();

        frame = new JFrame();
        frame.setBackground(Color.GRAY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(132 * 8 + 20, 132 * 8 + 40);
        frame.enableInputMethods(false);
        frame.setVisible(true);
        frame.addKeyListener(this);
        blocks = new JButton[65];
        for (int i = 0; i < 65; i++) {
            blocks[i] = new JButton();
            blocks[i].setSize(132, 132);
            blocks[i].setLocation((i % 8) * 132, (i / 8) * 132);
            blocks[i].setVisible(true);
            blocks[i].setCursor(Cursor.getDefaultCursor());
            blocks[i].setContentAreaFilled(false);
            blocks[i].setOpaque(false);
            blocks[i].setContentAreaFilled(false);
            blocks[i].setBorderPainted(false);
            blocks[i].addActionListener(blockClick);
            frame.add(blocks[i]);
        }
        final String startOrder = "jhiklihjggggggggnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnaaaaaaaadbcefcbd";
        for(int i = 0; i < 64; i++)
        {
            tools[i] = startOrder.charAt(i);
        }
        update();
    }
    public void update(Move move)
    {
        ChessMethods.executeMove(tools,move);
        update();
    }
    public void clear()
    {
        tools = ChessMethods.getDefaultBoard();
        update();
    }
    public void update()
    {
        for(int i = 0; i < 64; i++)
        {
            final char color = (((i / 8) % 2 == 0)?i % 2 == 0:i % 2 == 1)?'w':'b';
            blocks[i].setIcon(getImage(tools[i],color));
        }
    }
    public static ImageIcon getImage(char kind, char color)
    {
        //if(color == 'o') color = 'r';
        switch (kind)
        {
            case 'n':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\" + color + ".png");
            case 'a':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\whitesol" + color + ".png");
            case 'b':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\whitehorse" + color + ".png");
            case 'c':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\whitebis" + color + ".png");
            case 'd':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\whiteHook" + color + ".png");
            case 'e':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\whitequeen" + color + ".png");
            case 'f':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\whiteking" + color + ".png");
            case 'g':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\blacksol" + color + ".png");
            case 'h':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\blackhorse" + color + ".png");
            case 'i':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\blackbis" + color + ".png");
            case 'j':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\blackhook" + color + ".png");
            case 'k':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\blackqueen" + color + ".png");
            case 'l':  return new ImageIcon("F:\\programing\\visual\\iamges\\chess_tools\\neo-131\\colors\\blackking" + color + ".png");
        }
        return null;
    }
    ActionListener blockClick = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
       final int key = e.getKeyCode();
       if(key == 39 || key == 68)
       {
           if(index < code.length - 8)
           {
               Move move = new Move(code,index,tools);
               index += move.getMoveCodeLength(tools);
               moveList.push(move);
               ChessMethods.executeMove(tools,move);
               update();
               move_count++;
           }
       }
       else if(key == 37 || key == 65)
       {
           if(moveList.last != null)
           {
               Move move = moveList.pop();
               ChessMethods.undoMove(tools,move);
               index -= move.getMoveCodeLength(tools);
               update();
               move_count--;
           }
       }
       else if(key == 82 && moveList.last != null)
               System.out.println("[move " + move_count + "] " + moveList.last.move.toString());
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public static List<Move> codeToList(boolean[] code)
    {
        List<Move> list = new ArrayList<>();
        char[] board = ChessMethods.getDefaultBoard();
        int i = 0;
        while (i < code.length - 8)
        {
            Move move = new Move(code,i,board);
            i += move.getMoveCodeLength(board);
            ChessMethods.executeMove(board,move);
            list.add(move);
        }
        return list;
    }
}

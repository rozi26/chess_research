import Chess.ChessMethods;
import Chess.ChessTournament;
import Chess.Move;
import AI.AI;
import AI.BruteForceAI;
import Data.Data;
import GUI.Board;
import GUI.GameReader;


import java.io.*;
import java.util.*;

public class Main {

    final static int threadsCount = Runtime.getRuntime().availableProcessors();
    //if you want this program to compile change the return value of the last method in the class named "data" the instruction are there.
    public static void main(String[] args) throws IOException, InterruptedException {

        /**
         * RUN GAMES
         *  example: runGames(0,4,100,"test1");
         *  to run number of games you can use the method "runGames", this method except four inputs:
         *      mode - set the state of the bots, -1: all the bots are defensive, 0: the white bot is aggressive and the black is defensive, 1: all the bots are aggressive
         *      depth - how many turn forward the bots are calculated
         *      games - how many games to run
         *      fileName - the name of the file where the content of the games and the txt file that write the statistics of the games (you need to write only the name of the file without the path, the file will be automatic saved in the folder "runs" in the results folder).
         *
         *  if you want the program to add the results of the run to access table you need to compile the drivers folder.
         *
         *
         *
         * WATCH GAME
         *  example: readGame("test1", 4);
         *  to watch games your can use the method readGame that take as an input name of some tournament file and some number and show the "game" number.
         *
         *
         * GUI
         *  example: Board b = new Board();
         *  to play against block you can create object of the class Board, this class constructor except an object from the class AI and let you play against this AI.
         *
         */
        //Board board = new Board();
        runGames(0,2,100,"test1");
        //readGame("test1",0);
    }
    private static void runGames(int mode, int depth, int games, String fileName) throws IOException, InterruptedException {
        AI bot1 = new BruteForceAI(depth-1,mode != -1);
        AI bot2 = new BruteForceAI(depth-1,mode == 1);
        runGames(bot1,bot2,games,fileName,Math.max(threadsCount - 2,1));
    }
    private static void runGames(AI bot1, AI bot2, int games, String fileName, int threads) throws IOException, InterruptedException {
        final int threadsCount = Runtime.getRuntime().availableProcessors();
        ChessTournament tournament = new ChessTournament(bot1,bot2);
        if(threads > threadsCount)
        {
            System.out.println("can't run. requested to run on " + threads + " threads but this computer cpu only have " + threadsCount + " threads.");
            return;
        }
        else if(threads == threadsCount)
        {
            System.out.print("you requested to run on " + threads + " threads but this computer cpu have " + threads + " so if you sure you want to use all your cpu threads enter yes: ");
            Scanner scanner = new Scanner(System.in);
            if(!scanner.nextLine().equals("yes")) return;
        }
        if(threads == 1) tournament.play(games);
        else{
            System.out.println("run with multiprocessing on " + threads + "\\" + threadsCount + " threads");
            tournament.playSplit(games,threads);
        }

        tournament.summary(Data.getDataFolder() + "runs\\" + fileName);
    }

    private static void readGame(String fileId, int game) throws IOException {//show the the "game" game of the run with the name "fileId"
        File file = new File(Data.getDataFolder() + "runs\\" + fileId + "\\games.txt");
        if(!file.exists()){System.out.println("there is no file with the id of \"" + fileId +"\""); return;}
        try {
            final boolean[] gameCode = Data.getFromArray(Data.getAllFileText(file),game);
            final Move[] moves = GameReader.codeToList(gameCode).toArray(Move[]::new);
            System.out.println("game is " + (ChessMethods.approveGameLegit(moves)?"approve":"disapprove"));
            GameReader reader = new GameReader(Data.BinaryToString(gameCode));
        }
        catch (Exception e)
        {
            System.out.println("fail to read the game, maybe the file named [" + fileId + "] have less then [" + (game + 1) + "] games");
        }
    }

}
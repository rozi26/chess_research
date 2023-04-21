package Chess;

//import methods and objects from java
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import methods and objects from this other files in this project
import Data.Data;
import Data.DATASET_CONNECTOR;
import AI.AI;

public class ChessTournament {

    final String GAME_BACKUP_FOLDER = Data.getDataFolder() + "tournament_backup";
    //final String GAME_BACKUP_FOLDER = "E:\\time_redering\\runs";

    final long startTime; //when to program start
    final String GAMES_BACKUP; //the path of the backup folder for the games
    final String STARTS_BACKUP; //the path of the backup folder for the statistics
    final private FileOutputStream gamesStream;
    final private File statsFile;
    final private AI bot1;//bot1
    final private AI bot2;//bot2
    final protected int[] STATS_COUNTER; //count the statistics
    /*
    [0]-total moves
    [1]-black win total moves
    [2]-draw total moves
    [3]-white win total moves
    [4-10]-wins count
     */
    private int gamesGoal = 0;
    ChessTournament[] subGames = null;
    private int NumberOfGames = 0;
    private long time = 0;
    private boolean displayOn = false; //if the program need to print its progress to the console

    public ChessTournament(AI _bot1, AI _bot2) throws FileNotFoundException { //constructor
        startTime = System.currentTimeMillis();
        bot1 = _bot1;
        bot2 = _bot2;
        STATS_COUNTER = new int[11];
        GAMES_BACKUP = GAME_BACKUP_FOLDER + "\\games.txt";
        STARTS_BACKUP = GAME_BACKUP_FOLDER + "\\stats.txt";
        gamesStream = new FileOutputStream(new File(GAMES_BACKUP));
        statsFile = new File(STARTS_BACKUP);
    }
    private ChessTournament(AI _bot1, AI _bot2, int THREAD_ID) throws FileNotFoundException {//constructor for the program to use when it uses more than one thread
        startTime = System.currentTimeMillis();
        bot1 = _bot1;
        bot2 = _bot2;
        STATS_COUNTER = new int[11];
        GAMES_BACKUP = GAME_BACKUP_FOLDER + "\\games" + THREAD_ID + ".txt";
        STARTS_BACKUP = GAME_BACKUP_FOLDER + "\\stats" + THREAD_ID + ".txt";
        gamesStream = new FileOutputStream(new File(GAMES_BACKUP));
        statsFile = new File(STARTS_BACKUP);
    }

    public void play(final int games) throws IOException{ //run number of games from a single thread
        play(games,false);
    }
    public void play(final int games, boolean sub) throws IOException { //run number of games, if there are more threads that call this methods set "sub" to true else set "sub" to false
        gamesGoal = games;
        final long startTime = System.currentTimeMillis();
        boolean bot1_turn = NumberOfGames % 2 == 0;

        for(int i = 0; i < games; i++)
        {
            if(!sub)System.out.print(((char)(13)) + "game " + i + "\\" + games);//write the current game
            ChessGame chessGame = new ChessGame(bot1,bot2);
            GameReport gameReport = chessGame.playGame(bot1_turn);

            STATS_COUNTER[0] += gameReport.moves;//add the games moves to the total moves
            STATS_COUNTER[gameReport.gameResult + 2] += gameReport.moves;//add the game moves to the game stats moves counter
            STATS_COUNTER[gameReport.gameResultReason + 4]++;//add to the game over reason

            Data.saveArray(statsFile,STATS_COUNTER);
            Data.addString(gamesStream, gameReport.moveBuilder.get());

            bot1_turn = !bot1_turn;
            NumberOfGames++;
        }
        time += System.currentTimeMillis() - startTime;
        System.out.print(" run over ");
    }
    public void playSplit(final int games, int threads) throws InterruptedException, IOException {//run number of games from number of threads
        final boolean no_input = true;
        final long startTime = System.currentTimeMillis();
        gamesGoal = games;
        subGames = new ChessTournament[threads];
        Runnable[] runs = new Runnable[threads];
        Thread[] processes = new Thread[threads];
        int[][] statsCounter = new int[threads][STATS_COUNTER.length];
        for(int i = 0; i < threads; i++)
        {
            int finalI = i;
            int[] stats = statsCounter[i];
            runs[i] = new Runnable() {
                @Override
                public void run() {
                    try {;
                        ChessTournament tournament = new ChessTournament(bot1,bot2,finalI);
                        subGames[finalI] = tournament;
                        tournament.play(games / threads,true);
                        for(int i = 0; i < STATS_COUNTER.length; i++)
                        {
                            stats[i] += tournament.STATS_COUNTER[i];
                        }
                    }
                    catch (Exception e){System.out.println("thread fail (" + e + ")");}
                }
            };
            processes[i] = new Thread(runs[i]);
            processes[i].start();
        }

        Runnable display_run = new Runnable() {
            @Override
            public void run() {
                final int limit = gamesGoal;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                while (displayOn)
                {
                    if(subGames != null)
                    {
                        int gamesCount = 0;
                        int min = limit;
                        for (ChessTournament subGame : subGames){ gamesCount += subGame.NumberOfGames; min = Math.min(min,subGame.NumberOfGames);}
                        final int left = limit / subGames.length - min;

                        final String text = "games: " + Data.niceWrite(gamesCount) + "\\" + Data.niceWrite(limit) + " (" + Data.presentOf(gamesCount,limit) + "%),\t time left: " + Data.predictTime(min,left,System.currentTimeMillis()-  startTime);
                        System.out.print(((char)(13)) + text);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };
        Scanner scanner = new Scanner(System.in);

        final boolean NO_INPUT = true;
        if(NO_INPUT)
        {
            displayOn = true;
            Thread thread = new Thread(display_run);
            thread.start();
            while (run(processes))
                Thread.sleep(100);
            displayOn = false;
        }
        else
        {
            while (run(processes))
            {
                System.out.print("enter command: ");
                final String input = scanner.nextLine();
                if(input.equals("report")) System.out.println(getState());
                else if(input.equals("time")) System.out.println("running for " + Data.getTimeDifferent(startTime,System.currentTimeMillis()));
                else if(input.equals("predict time"))
                {
                    int pass;
                    if(subGames == null) pass = NumberOfGames;
                    else
                    {
                        pass = subGames[0].NumberOfGames;
                        for(int i = 1; i < subGames.length; i++)
                            pass = Math.min(pass,subGames[i].NumberOfGames);
                    }
                    final int left = (subGames == null?gamesGoal:gamesGoal / subGames.length) - pass;
                    System.out.println("assessment time left: " + Data.predictTime(pass,left,System.currentTimeMillis() - startTime));
                }
                else if(input.equals("display"))
                {
                    displayOn = true;
                    Thread thread = new Thread(display_run);
                    thread.start();
                    if(!no_input)
                        scanner.nextLine();
                    displayOn = false;
                }
                else if(input.equals("clear"))
                    for(int i = 0; i < 1000000; i++)System.out.println();
            }
        }

        System.out.println("tournament over");
        List<String> list = new ArrayList<>();
        for(int i = 0; i < threads; i++)
        {
            for(int g = 0; g < STATS_COUNTER.length; g++)
                STATS_COUNTER[g] += statsCounter[i][g];
            List<String> add = Data.getList(Data.getAllFileTextO(new File(GAME_BACKUP_FOLDER + "\\games" + i + ".txt")));
            list.addAll(add);
        }
        FileOutputStream stream = new FileOutputStream(new File(GAME_BACKUP_FOLDER + "\\games.txt"));
        for(String s:list) Data.addString(stream,s);
        stream.close();
        time += System.currentTimeMillis() - startTime;
        NumberOfGames = games;
    }
    private boolean run(Thread[] p) { //check if all the threads in p are running
        for(Thread t:p)
            if(t.isAlive()) return true;
        return false;
    }

    public String getState()//print the current state of the tournament
    {
        if(subGames == null)
            return "game " + NumberOfGames + "\\" + gamesGoal;
        else
        {
            StringBuilder txt = new StringBuilder();
            final int limit = gamesGoal / subGames.length;
            final String limitS = Data.niceWrite(limit);
            int min = limit;
            int minLoc = 0;
            int max = 0;
            int maxLoc = 0;
            int gamesCount = 0;
            for(int i = 0; i < subGames.length; i++)
            {
                int games = subGames[i].NumberOfGames;
                gamesCount += games;
                if(games < min){min = games; minLoc = i + 1;}
                if(games > max){max = games; maxLoc = i + 1;}
                String s = "trade " + (i + 1) + ": " + Data.niceWrite(games) + "\\" + limitS +" (" + Data.presentOf(games,limit) + "%)";
                if(games == limit) s += " (done)";
                txt.append(s).append("\n");
            }
            txt.append("best  trade is trade ").append(maxLoc).append(" with ").append(Data.niceWrite(max)).append("\\").append(limitS).append(" games (").append(Data.presentOf(max,limit)).append("%)\n");
            txt.append("worst trade is trade ").append(minLoc).append(" with ").append(Data.niceWrite(min)).append("\\").append(limitS).append(" games (").append(Data.presentOf(min,limit)).append("%)\n");
            txt.append("\ntotal: ").append(Data.niceWrite(gamesCount)).append("\\").append(Data.niceWrite(gamesGoal)).append(" (").append(Data.presentOf(gamesCount,gamesGoal)).append("%)\n");
            return txt.toString();
        }
    }

    public void summary(String path) throws IOException  //summary all the data of the tournament into sql database and text file in "path", also this method save the content of the games in file that can be find in "path"
    {
        gamesStream.close();
        File folder = new File(path);
        if(folder.exists()) {
            System.out.println("folder in " + path + " already exist");
            path += "_1";
            folder = new File(path);
            int num = 1;
            while (folder.exists())
            {
                num++;
                path = path.substring(0,path.length() - 1) + num;
                folder = new File(path);
            }
            System.out.println("new folder path: " + path);
        }
        if(!folder.mkdir()) {System.out.println("fail to create folder in " + path); return;}

        //save the games
        final File gamesFile = new File(path + "\\games.txt");
        if(!gamesFile.createNewFile()) {System.out.println("fail to create game file"); return;}
        final FileWriter gamesWriter = new FileWriter(gamesFile);
        final List<String> gamesList = Data.getList(Data.getAllFileTextO(new File(GAMES_BACKUP)));//get the list of the games
        System.out.println("games list length: " + gamesList.size());
        gamesWriter.write(Data.collectArray(gamesList));
        gamesWriter.close();

        //save the stats
        final File statsFile = new File(path + "\\stats.txt");
        if(!statsFile.createNewFile()) {System.out.println("fail to create stats file"); return;}

        StringBuilder stats = new StringBuilder("stats summary\n\n");
        stats.append("run of ").append(Data.niceWrite(NumberOfGames)).append(" games\n");
        stats.append("as white: ").append(bot1.getId()).append(" (").append(bot1.getAggressiveId()).append(")\n");
        stats.append("as black: ").append(bot2.getId()).append(" (").append(bot2.getAggressiveId()).append(")\n");
        stats.append("\n");
        stats.append("white wins: ").append(Data.niceWrite(STATS_COUNTER[5])).append("\n");
        stats.append("black wins: ").append(Data.niceWrite(STATS_COUNTER[4])).append("\n");
        int draws = 0;
        StringBuilder drawsRezones = new StringBuilder();
        for(int i = 6; i < 11; i++)
        {
            drawsRezones.append(GameReport.getReasonText(i - 4)).append(": ").append(Data.niceWrite(STATS_COUNTER[i])).append("\n");
            draws += STATS_COUNTER[i];
        }
        stats.append("draws: ").append(Data.niceWrite(draws)).append("\n\n");
        stats.append(drawsRezones);
        stats.append("\n");

        final int games = draws + STATS_COUNTER[4]  + STATS_COUNTER[5];
        stats.append("white to black win ratio: ").append(Data.getRatio(STATS_COUNTER[5],STATS_COUNTER[4])).append("\n");
        stats.append("black to white win ratio: ").append(Data.getRatio(STATS_COUNTER[4],STATS_COUNTER[5])).append("\n");
        stats.append("wins to draw ratio: ").append(Data.getRatio(STATS_COUNTER[4] + STATS_COUNTER[5],draws)).append("\n");
        stats.append("draw present from all the games: ").append(Data.presentOf(draws,games)).append("%\n");
        stats.append("\n");
        stats.append("moves: ").append(Data.niceWrite(STATS_COUNTER[0])).append("\n");
        stats.append("average moves par game: ").append(Data.round((double)STATS_COUNTER[0] / games,3)).append("\n");
        stats.append("average moves per game when white win: ").append(Data.round((double)STATS_COUNTER[3] / STATS_COUNTER[5],3)).append("\n");
        stats.append("average moves per game when black win: ").append(Data.round((double)STATS_COUNTER[1] / STATS_COUNTER[4],3)).append("\n");
        stats.append("average moves per game when draw: ").append(Data.round((double)STATS_COUNTER[2] / draws,3)).append("\n");
        stats.append("\n");
        stats.append("run time: ").append(Data.getTimeDifferent(0,time));
        FileWriter statsWriter = new FileWriter(statsFile);
        statsWriter.write(stats.toString());
        statsWriter.close();
        if(!statsFile.setWritable(false)) System.out.println("stats file is still writable");

       /* Data.CSV_CONNECTOR csv_connector = new Data.CSV_CONNECTOR(Data.getDataFolder() + "games_data.csv");
        csv_connector.insert(Integer.toString(bot1.getDepth()),Data.niceWrite(NumberOfGames),Data.niceWrite(STATS_COUNTER[5]),Data.niceWrite(STATS_COUNTER[4]),Data.niceWrite(draws),Double.toString(Data.getRatio(STATS_COUNTER[5],STATS_COUNTER[4])));*/
        try {
            DATASET_CONNECTOR.ChessConnector connector = new DATASET_CONNECTOR.ChessConnector(Data.getDataFolder() + "\\data_set.accdb","table1");
            connector.insert(Integer.toString(bot1.getDepth()),bot1.isAggressive()?"1":"0",bot2.isAggressive()?"1":"0",Integer.toString(STATS_COUNTER[5]),Integer.toString(STATS_COUNTER[4]),Integer.toString(draws));
            connector.disconnect();
        }
        catch (Exception e)
        {
            System.out.println("\nerror. can't write the game results to access table (the games and the stats will be saved but the accesses table will not be updated). error code: [" + e + "]");
        }


    }
}

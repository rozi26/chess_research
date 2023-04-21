package Data;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class Data {

    //time
    public static String getDate() //return the current data
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat;
        String date = "";
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
        date += simpleDateFormat.format(calendar.getTime());
        return date;
    }
    //math
    private static int ceilLog2(int num) //calculate ceil(log2(n))
    {
        for(int i  = 31; i > 0; i--)
            if(((num >> i) & 1) == 1) return i + 1;
        return 0;
    }


    //binary
    public static boolean[] intToBinary(int num,int length)//convert "num" to binary in boolean array with in the size of length
    {
        boolean[] val = new boolean[length];
        for(int i = 0; i < length; i++)
        {
            val[i] = ((num >> i) & 1) == 1;
        }
        return val;
    }
    public static int binaryToInt(boolean[] arr) //convert binary array to int
    {
        int num = 0;
        int add = 1;
        for(boolean b:arr)
        {
            if(b) num += add;
            add *= 2;
        }
        return num;
    }
    public static boolean[] StringToBinary(String str) //convert string to binary
    {
        boolean[] code = new boolean[str.length() * 8];
        int counter = 0;
        for(int i = 0; i < str.length(); i++)
        {
            final char a = str.charAt(i);
            for(int g = 0; g < 8; g++)
            {
                code[counter] = ((a >> g) & 1) == 1;
                counter++;
            }
        }
        return code;
    }
    public static String BinaryToString(boolean[] arr) //convert binary to string
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < arr.length; i += 8)
        {
            char a = '\0';
            for(int g = 0; g < 8; g++)
                if(arr[i + g]) a |= 1<<g;
            builder.append(a);
        }
        return builder.toString();
    }
    private static boolean[] ByteToBinary(byte b) // convert byte to binary
    {
        boolean[] code = new boolean[8];
        for(int g = 0; g < 8; g++)
            code[g] = ((b >> g) & 1) == 1;
        return code;
    }
    private static byte[] StringToBytes(String str) // convert string to bytes
    {
        byte[] b = new byte[str.length()];
        for(int i=  0; i < str.length(); i++)
            b[i] = (byte)str.charAt(i);
        return b;
    }
    private static byte[] IntToBytes(int a) // convert int to bytes
    {
        byte[] b = new byte[4];
        for(int i = 0; i < 8; i++)
        {
            for(int g = 0; g < 4; g++)
                if(((a >> (i+g*8)) & 1) == 1) b[g] |= 1<<i;
        }
        return b;
    }

    //array (for saving in the computer storage)
    public static String collectArray(List<String> strings) //return code in binary that using the method "get from array" can return the string index n in O(1) complexity
    {
        int max = 0;
        for(String s:strings)
            max = Math.max(s.length(),max);
        return collectArray(strings,max * 8);
    }
    public static String collectArray(List<String> strings, int longestString)//marge Strings to one string where you can find each string from his index with complexity of O(1)
    {
        final int game_length_bits = ceilLog2(longestString) - 3;
        final ByteBuilder gamesBuilder = new ByteBuilder();//build the games and before each game write the game length
        final int[] gamesStart = new int[strings.size()];

        int counter = 0;
        for(int i = 0; i < strings.size(); i++)
        {
            gamesStart[i] = counter;
            final String game = strings.get(i);
            gamesBuilder.add(intToBinary(game.length(),game_length_bits));
            gamesBuilder.add(StringToBinary(game));
            final int gameLength = game.length() * 8;
            counter += gameLength + game_length_bits;
        }
        ///System.out.println(Arrays.toString(gamesStart));

        final ByteBuilder builder = new ByteBuilder();
        final int game_index_bits = ceilLog2(counter - game_length_bits * gamesStart.length);
        builder.add(intToBinary(gamesStart.length,32));//write the number of games
        builder.add(intToBinary(game_length_bits,32));//write the maximum length of game
        builder.add(intToBinary(game_index_bits,32));//write the number of bits that represent game index

        //System.out.println(game_index_bits);
        for(int start:gamesStart)
            builder.add(intToBinary(start,game_index_bits));
        builder.add(StringToBinary(gamesBuilder.get()));
        //System.out.println();
        return builder.get();
    }
    public static boolean[] getFromArray(String collection, int gameIndex)//return string by index from collection
    {
        final boolean[] code = StringToBinary(collection);
        final boolean[] gamesB = new boolean[32];System.arraycopy(code, 0, gamesB, 0, gamesB.length);
        final boolean[] sizeB = new boolean[32];System.arraycopy(code, gamesB.length, sizeB, 0, sizeB.length);
        final boolean[] indexB = new boolean[32];System.arraycopy(code, gamesB.length + sizeB.length, indexB, 0, indexB.length);
        final int dataStart = gamesB.length + sizeB.length + indexB.length;

        final int games_amount = binaryToInt(gamesB);
        final int game_length_bits = binaryToInt(sizeB);
        final int games_index_bits = binaryToInt(indexB);
        final int games_data_start = dataStart + games_amount * games_index_bits;
        //System.out.println("games: " + games_amount + "\ngame length bits: " + game_length_bits + "\ngames index bits: " + games_index_bits + "\n");

        final boolean[] startB = new boolean[games_index_bits]; System.arraycopy(code,dataStart + gameIndex * games_index_bits,startB,0,startB.length);
        final int start = binaryToInt(startB) + games_data_start;
        //System.out.println("start: " + start + " (" + (start - games_data_start) + ")");

        final boolean[] gameLengthB = new boolean[game_length_bits]; System.arraycopy(code,start,gameLengthB,0,gameLengthB.length);
        final int gameLength = binaryToInt(gameLengthB) * 8;

        //System.out.println("game length: " + gameLength);
        final boolean[] game = new boolean[gameLength]; System.arraycopy(code,start + game_length_bits,game,0,game.length);
        return game;
    }

    public static void saveArray(File file, int[] arr) throws IOException //save array of ints in file
    {
        StringBuilder builder = new StringBuilder();
        for (int j : arr) builder.append(BinaryToString(intToBinary(j, 32)));
        FileWriter writer = new FileWriter(file);
        writer.write(builder.toString());
        writer.close();
    }
    public static int[] getArray(File file) throws IOException //get array of ints from file that used "save array" method
    {
        final String str = getAllFileText(file);
        int[] arr = new int[str.length() / 4];
        for(int i = 0; i < arr.length; i++)
            arr[i] = binaryToInt(StringToBinary(str.substring(i*4,i*4+4)));
        return arr;
    }

    //list
    public static void addString(FileOutputStream out, String str) throws IOException { //add string to output stream that reprent list
        out.write(IntToBytes(str.length() * 8));
        out.write(StringToBytes(str));
    }
    public static List<String> getList(String text)//convert binary list to java list
    {
        List<String> list = new ArrayList<>();
        final boolean[] code = StringToBinary(text);
        final int size = 32;
        int i = 0;
        while (i < code.length)
        {
            final boolean[] lengthB = new boolean[size]; System.arraycopy(code,i,lengthB,0,size);
            final int length = binaryToInt(lengthB);
            i += size;
            final boolean[] stringB = new boolean[length]; System.arraycopy(code,i,stringB,0,length);
            list.add(BinaryToString(stringB));
            i += length;
        }
        return list;
    }
    //helpers
    public static void writeTextToFile(File file, String text) throws IOException {
        Files.writeString( file.toPath(), text);
    }
    public static String getAllFileText(File file) throws IOException {
        return Files.readString(file.toPath());
    }
    public static String getAllFileTextO(File file) throws IOException {
        InputStream inp = new FileInputStream(file);
        byte[] bytes = inp.readAllBytes();
        StringBuilder builder = new StringBuilder();
        for(byte b:bytes)
            builder.append((char)(b));
        inp.close();
        return builder.toString();
    }
    public static class ByteBuilder//save boolean arrays and return them as string
    {
        int length = 0;
        private class ByteLink
        {
            ByteLink prev;
            byte value;
            byte index;
            ByteLink(ByteLink _prev) {index = 0; value = 0; prev = _prev; length++;}
            int add(boolean[] b, int count)
            {
                while (index < 8 && count < b.length)
                {
                    if(b[count])
                        value |= 1<<index;
                    index++;
                    count++;
                }
                return count;
            }
        }
        ByteLink last;
        public ByteBuilder()
        {
            last = new ByteLink(null);
        }
        public void add(boolean[] move)
        {
            int index = 0;
            while (index < move.length)
            {
                if(last.index == 8)
                    last = new ByteLink(last);
                index = last.add(move,index);
            }
        }
        public void add(byte b)
        {
            add(ByteToBinary(b));
        }
        public String get()
        {
            StringBuilder builder = new StringBuilder();
            ByteLink node = last;
            while (node != null)
            {
                builder.append((char)(node.value));
                node = node.prev;
            }
            return builder.reverse().toString();
        }
    }


    //presenting
    public static String niceWrite(int num) //add commas to nicely represent an integer
    {
        return niceWrite(Integer.toString(num));
    }
    public static String niceWrite(String num)
    {
        StringBuilder text = new StringBuilder();
        int count = 0;
        for(int i = num.length() - 1; i > 0; i--)
        {
            text.append(num.charAt(i));
            count++;
            if(count == 3)
            {
                text.append(",");
                count = 0;
            }
        }
        text.append(num.charAt(0));
        return text.reverse().toString();
    }
    public static double round(double num, int digits) //round double
    {
        final int multer = (int)Math.pow(10,digits);
        return (double)((int)(num * multer)) / multer;
    }
    public static double presentOf(int num, int from)
    {
        return ((int)(((double)num / from * 100) * 100)) / 100.0;
    }
    public static double getRatio(double a, double b)
    {
        if(b == 0) return 1;
        return round(a / b,5);
    }
    public static String getTimeDifferent(long start, long end) { //return string that write the time different between two times
        String time = "";
        long different = end - start;
        if (different >= 86400000)
        {
            time += (different / 86400000) + " days, ";
            different %= 86400000;
        }
        if(different >= 3600000)
        {
            time += (different / 3600000) + " hours, ";
            different %= 3600000;
        }
        if(different >= 60000)
        {
            time += (different / 60000) + " minutes, ";
            different %= 60000;
        }
        if(different >= 1000)
        {
            time += (different / 1000) + " seconds, ";
            different %= 1000;
        }
        time += different + " milliseconds";
        return time;
    }
    public static String predictTime(int pass, int left, long sofar) //predict the time that left
    {
        return getTimeDifferent(0,(int)(((double) sofar / pass) * left));
    }

    public static class CSV_CONNECTOR //convert to csv table
    {
        final File file;
        final String[] properties_names;
        public CSV_CONNECTOR(String path) throws FileNotFoundException {
            file = new File(path);
            if(!file.exists())
                System.out.println("error file in " + path +"  doesn't exist");
            Scanner scanner = new Scanner(file);
            final String first_line = scanner.nextLine();
            List<String> names = new ArrayList<>();
            StringBuilder name_builder = new StringBuilder();
            for(int i = 0 ;i < first_line.length(); i++)
            {
                final char a = first_line.charAt(i);
                if(a == ',')
                {
                    names.add(name_builder.toString());
                    name_builder = new StringBuilder();
                }
                else
                    name_builder.append(a);
            }
            names.add(name_builder.toString());
            properties_names = names.toArray(String[]::new);
            System.out.println(Arrays.toString(properties_names));
        }

        public void insert(String... values) throws IOException {
            if(values.length != properties_names.length)
            {
                System.out.println("error there are " + values.length + " values but expected " + properties_names.length + " values");
                return;
            }
            Scanner reader = new Scanner(file);
            StringBuilder content = new StringBuilder();
            while (reader.hasNextLine())
                content.append(reader.nextLine()).append("\n");
            FileWriter writer = new FileWriter(file);
            boolean first = true;
            for(String val: values)
            {
                if(first) first = false;
                else content.append(",");
                content.append(val);
            }
            content.append("\n");
            writer.write(content.toString());
            writer.close();
        }
    }

    public static String getDataFolder() //return the data folder's path
    {
        return System.getProperty("user.dir") + "\\src\\results\\";
        //change this method to return the location of the folder named "results" in this project
      //  return "D:\\Users\\iddor\\IdeaProjects\\ChessAgressiveReserch\\src\\results\\";
    }
}

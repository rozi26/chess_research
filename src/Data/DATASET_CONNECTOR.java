package Data;

import java.io.File;
import java.sql.*;

public class DATASET_CONNECTOR {


    //connect to access dataset
    public static class AccessConnector //connect to acsses table
    {
        final Connection connection;
        final String table;
        final int columns_count;
        int rows_count;
        public AccessConnector(String path, String _table) throws SQLException {

            if(!new File(path).exists())
                System.out.println("error there is no file in [" + path + "]");

            table = _table;
            connection = DriverManager.getConnection("jdbc:ucanaccess://" + path);
            rows_count = get_rows_count();
            columns_count = get_columns_count();


            System.out.println("connected successfully");
        }


        //data methods
        public int get_rows_count() throws SQLException {
            ResultSet set = executeCommand("SELECT * FROM " + table);
            int count = 0;
            while (set.next())
                count++;
            return count;
        }
        public int get_columns_count() throws SQLException {
            ResultSet set = executeCommand("SELECT * FROM " + table);
            ResultSetMetaData setM = set.getMetaData();
            return setM.getColumnCount();
        }

        public String readTable() throws SQLException {
            StringBuilder text = new StringBuilder();
            ResultSet result = executeCommand("SELECT * FROM " + table);
            while (result.next())
            {

            }
            return "";
        }
        public void disconnect() throws SQLException {
            connection.close();
        }
        //execute sql command and return the result as resultset
        public ResultSet executeCommand(String command){
            try {
                Statement statement = connection.createStatement();
                return statement.executeQuery(command);
            }
            catch (Exception e)
            {
                System.out.println("error when executing [" + command + "] \t error messege: [" + e + "]");
                return null;
            }
        }

        public boolean insertRow(String[] names, String[] values) throws SQLException {
            StringBuilder command = new StringBuilder("INSERT INTO " + table + " (" + names[0]);
            for(int i = 1; i < names.length; i++)
                command.append(",").append(names[i]);
            command.append(") VALUES (").append(values[0]);
            for(int i = 1; i < names.length; i++)
                command.append(",").append(values[i]);
            command.append(")");
            System.out.println(command);
            PreparedStatement preparedStatement = connection.prepareStatement(command.toString());
            int row = preparedStatement.executeUpdate();
            return row > 0;
        }
    }




    //class for insert data into access dataset
    public static class ChessConnector extends AccessConnector
    {

        final int inputs_length = 6;
        final private String[] names = new String[]{"depth","white_aggressive","black_aggressive","white_wins","black_wins","draws","games", "white_to_black_ratio", "wins_to_draws_ratio"};
        public ChessConnector(String path, String table) throws SQLException {
            super(path,table);

        }

        public boolean insert(String... objects) throws SQLException {
            if(objects.length != inputs_length)
            {
                System.out.println("got  " + objects.length + " inputs but need " + inputs_length + " inputs");
                return false;
            }
            String[] values = new String[names.length];
            System.arraycopy(objects, 0, values, 0, inputs_length);
            final int aggressive_wins = Integer.parseInt(objects[3]);
            final int defensive_wins = Integer.parseInt(objects[4]);
            final int draws = Integer.parseInt(objects[5]);
            values[6] = Integer.toString(aggressive_wins + defensive_wins + draws); //write the total number of games
            values[7] = Double.toString(Data.getRatio(aggressive_wins,defensive_wins)); //write aggressive to defensive wins ratio
            values[8] = Double.toString(Data.getRatio(aggressive_wins + defensive_wins,draws)); // write the ratio between wins and draws
            return insertRow(names,values);
        }

        public String[] getParametersName() {
            return names;
        }
    }

}

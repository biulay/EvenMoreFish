package com.oheers.fish.database;

import org.bukkit.entity.Player;

import java.sql.*;

public class Database {

    static final String url = "jdbc:sqlite:plugins/EvenMoreFish/database.db";
    private static Connection connection;

    public static void getConnection() throws SQLException {
        if (connection == null) {
            // creates a connection to the database
            connection = DriverManager.getConnection(url);
        }
    }

    public static void closeConnections() throws SQLException {
        // memory stuff - closes all the database wotsits
        if (connection != null) {
            connection.close();
        }
    }

    // does the almighty "Fish" database exist
    public static boolean dbExists() throws SQLException {
        getConnection();

        // gets connection metadata
        DatabaseMetaData dbMD = connection.getMetaData();
        try (ResultSet tables = dbMD.getTables(null, null, "Fish", null)) {
            // if there's a tables.next() it returns true, if not, it returns false
            return tables.next();
        }
    }

    public static void createDatabase() throws SQLException {
        getConnection();

        String sql = "CREATE TABLE \"Fish\" (\n" +
                "\"FISH\"\tTEXT,\n" +
                "\"firstFisher\" TEXT,\n" +
                "\"totalCaught\" INTEGER,\n" +
                "\"largestFish\" FLOAT,\n" +
                "\"largestFishCatcher\" TEXT,\n" +
                "PRIMARY KEY(\"FISH\")\n" +
                ");";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            // Creates a table with FISH as the primary key
            prep.execute();
        }
    }

    public static boolean hasFish(String name) throws SQLException {
        getConnection();

        String sql = "SELECT FISH FROM Fish;";

        try (
                PreparedStatement prep = connection.prepareStatement(sql);
                ResultSet rs = prep.executeQuery()
        ) {
            while (rs.next()) {
                if (rs.getString(1).equals(name)) {
                    return true;
                }
            }
            // will return false if there's no fish with the given parameter
            return false;
        }
    }

    public static void add(String fish, Player fisher, Float length) throws SQLException {
        getConnection();

        String sql = "INSERT INTO Fish (FISH, firstFisher, totalCaught, largestFish, largestFishCatcher) VALUES (?,?,?,?,?);";

        // rounds it so it's all nice looking for when it goes into the database
        double lengthDouble = Math.round(length * 10.0) / 10.0;

        // starts a field for the new fish that's been fished for the first time
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fish);
            prep.setString(2, fisher.getUniqueId().toString());
            prep.setInt(3, 1);
            prep.setDouble(4, lengthDouble);
            prep.setString(5, fisher.getUniqueId().toString());
            prep.execute();
        }
    }

    // a fish has been caught, the total catches needs incrementing
    public static void fishIncrease(String fishName) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish SET totalCaught = totalCaught + 1 WHERE FISH = ?;";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fishName);
            prep.execute();
        }
    }

    // a player has just fished a fish that's longer than the current #1
    public static void newTopSpot(Player player, String fishName, Float length) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish SET largestFish = ?, largestFishCatcher=? WHERE FISH = ?;";

        // rounds it so it's all nice looking for when it goes into the database
        double lengthDouble = Math.round(length * 10.0) / 10.0;

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setDouble(1, lengthDouble);
            prep.setString(2, player.getUniqueId().toString());
            prep.setString(3, fishName);
            prep.execute();
        }
    }

    public static float getTopLength(String fishName) throws SQLException {
        getConnection();

        String sql = "SELECT largestFish FROM Fish WHERE FISH = ?;";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fishName);
            float returnable = Float.MAX_VALUE;
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                returnable = rs.getFloat(1);
            }
            rs.close();
            return returnable;
        }
    }
}

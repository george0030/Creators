package me.george0030.creators.io;

import me.george0030.creators.Creators;
import me.george0030.creators.misc.CreatorsRow;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class CreatorsDB {

    public final int cacheCapacity;
    private final Creators plugin;
    public Vector<CreatorsRow> cache;
    public volatile long cacheLastFetched;
    public volatile int numberOfCreatorsInDatabase;
    private Connection connection;
    private PreparedStatement fetchQuery;
    private PreparedStatement insertQuery;

    public CreatorsDB(Creators plugin) {
        this.plugin = plugin;
        this.cacheCapacity = plugin.getConfig().getInt("local_cache");
        cache = new Vector<CreatorsRow>(cacheCapacity);
    }

    public void openConnection() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        FileConfiguration config = plugin.getConfig();
        String host = config.getString("host");
        String port = config.getString("port");
        String database = config.getString("database");
        String username = config.getString("username");
        String password = config.getString("password");
        System.out.println(Class.forName("com.mysql.jdbc.Driver").newInstance().toString());
        connection = DriverManager.getConnection(
                String.format("jdbc:mysql://%s:%s/%s", host, port, database), username, password);
        connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS creators (playername VARCHAR(255) NOT NULL UNIQUE, youtube TINYTEXT, subcount BIGINT(255))");
        fetchQuery = connection.prepareStatement("SELECT playername, youtube FROM creators ORDER BY subcount DESC LIMIT ? OFFSET ?");
        insertQuery = connection.prepareStatement("INSERT INTO creators (playername, youtube, subcount) VALUES (?,?,?)");

    }

//    public List<CreatorsRow> get(int fromNthBySubcount, int number) throws SQLException {
//
//        if(fromNthBySubcount + number <= cacheCapacity)
//            return cache.subList(fromNthBySubcount,fromNthBySubcount + number);
//        else
//            return fetch(fromNthBySubcount, number);
//
//
//    }

    //100% functional
    public void fetchToCache() throws SQLException {
        fetchQuery.setInt(2, 0);
        fetchQuery.setInt(1, cacheCapacity);
        cacheLastFetched = System.currentTimeMillis();
        ResultSet result = fetchQuery.executeQuery();
        cache.clear();
        while (result.next()) {
            System.out.println(result.getString(1));
            cache.add(new CreatorsRow(result.getString(1), result.getString(2)));

        }

        numberOfCreatorsInDatabase = fetchNumberOfCreatorsInDatabase();

    }

    public int fetchNumberOfCreatorsInDatabase() throws SQLException {
        ResultSet result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM creators");
        result.next();
        return result.getInt(1);
    }

    public List<CreatorsRow> fetch(int fromNthBySubcount, int number) throws SQLException {
        fetchQuery.setInt(1, number);
        fetchQuery.setInt(2, fromNthBySubcount);
        ResultSet result = fetchQuery.executeQuery();
        LinkedList<CreatorsRow> table = new LinkedList<CreatorsRow>();
        int i = 0;
        while (result.next()) {
            table.add(new CreatorsRow(result.getString(1), result.getString(2)));
        }

        return table;

    }

    public void insert(String playerName, String youtube, long subs) throws SQLException {
        insertQuery.setString(1, playerName);
        insertQuery.setString(2, youtube);
        insertQuery.setLong(3, subs);
        insertQuery.execute();
    }

    public void refreshTable() throws SQLException, IOException {
        ResultSet youtubeIDs = connection.prepareStatement("SELECT youtube FROM creators").executeQuery();
        StringBuilder changes = new StringBuilder();
        LinkedList<String> faulty = new LinkedList<String>();
        String youtubeID;
        long subCount;
        while (youtubeIDs.next()) {
            youtubeID = youtubeIDs.getString(1);
            subCount = plugin.youtubeData.findSubCount(youtubeID);
            if (subCount < 0L) changes.append("DELETE FROM creators WHERE youtube IS'").append(youtubeID).append("';");
            else
                changes.append("UPDATE creators SET subcount = ").append(subCount).append(" WHERE youtube IS '").append(youtubeID).append("';");
        }

        connection.createStatement().executeQuery(changes.toString());
    }

    public boolean containsEntry(String playerName) throws SQLException {
        ResultSet result = connection.prepareStatement("SELECT COUNT(*) FROM creators WHERE playername = ?").executeQuery();
        result.next();
        return result.getInt(1) > 0;

    }


}

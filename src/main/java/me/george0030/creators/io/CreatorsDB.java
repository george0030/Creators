package me.george0030.creators.io;

import me.george0030.creators.Creators;
import me.george0030.creators.misc.CreatorsRow;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.*;
import java.util.*;

public class CreatorsDB {

    public final int cacheCapacity;
    private final Creators plugin;
    public Vector<CreatorsRow> cache;
    public volatile long cacheLastFetched;
    public volatile int numberOfCreatorsInDatabase;
    private Connection connection;
    private PreparedStatement fetchQuery;
    private String table;

    public CreatorsDB(Creators plugin) {
        this.plugin = plugin;
        this.cacheCapacity = plugin.getConfig().getInt("local_cache");
        cache = new Vector<CreatorsRow>(cacheCapacity);
    }

    public void openConnection() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
    
        FileConfiguration config = plugin.getConfig();
        String username = config.getString("username");
        String password = config.getString("password");
        Class.forName(plugin.getConfig().getString("sql_driver")).newInstance();
        connection = DriverManager.getConnection(plugin.getConfig().getString("url"), username, password);
        table = plugin.getConfig().getString("table");
        connection.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + table
                        + " (playerUUID BINARY(16), youtube TINYTEXT, subcount BIGINT(255))");
        fetchQuery = connection.prepareStatement("SELECT playerUUID, youtube, subcount FROM " + table + " ORDER BY "
                                                         + "subcount DESC LIMIT ? OFFSET ?");
    
    }
    
    public void fetchToCache() throws SQLException {
        fetchQuery.setInt(2, 0);
        fetchQuery.setInt(1, cacheCapacity);
        cacheLastFetched = System.currentTimeMillis();
        ResultSet result = fetchQuery.executeQuery();
        cache.clear();
        while (result.next()) {
            cache.add(new CreatorsRow(uuidFromBytes(result.getBytes(1)), result.getString(2), result.getLong(3)));
            
        }
        
        numberOfCreatorsInDatabase = fetchNumberOfCreatorsInDatabase();
        
    }
    
    private UUID uuidFromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }
    
    public int fetchNumberOfCreatorsInDatabase() throws SQLException {
        ResultSet result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM " + table);
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
            table.add(new CreatorsRow(uuidFromBytes(result.getBytes(1)), result.getString(2), result.getLong(3)));
        }
        
        return table;
        
    }
    
    public void insert(UUID playerUUID, String youtube, long subs) throws SQLException {
        PreparedStatement insertQuery = connection.prepareStatement("REPLACE INTO " + table + " (playerUUID, youtube, "
                                                                            + "subcount) VALUES (?,"
                                                                            + "?,?)");
        insertQuery.setBytes(1, bytesFromUUID(playerUUID));
        insertQuery.setString(2, youtube);
        insertQuery.setLong(3, subs);
        insertQuery.execute();
    }
    
    private byte[] bytesFromUUID(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return bytes;
    }
    
    public int[] refreshTable() throws SQLException, IOException {
        ResultSet youtubeIDs = connection.prepareStatement("SELECT youtube FROM " + table).executeQuery();
        Statement statement = connection.createStatement();
        String youtubeID;
        long subCount;
        while (youtubeIDs.next()) {
            StringBuilder changes = new StringBuilder();
            youtubeID = youtubeIDs.getString(1);
            subCount = plugin.youtubeData.findSubCount(youtubeID);
            if (subCount < 0L) {
                changes.append("DELETE FROM " + table + " WHERE youtube = '").append(youtubeID).append(
                        "'");
            } else {
                changes.append("UPDATE " + table + " SET subcount = ").append(subCount).append(
                        " WHERE youtube = '").append(youtubeID).append("'");
            }
            
            statement.addBatch(changes.toString());
        }
        
        return statement.executeBatch();
    }
    
    public boolean containsEntry(UUID playerUUID) {
        return cache.stream().anyMatch(row -> row.playerUUID.equals(playerUUID));
    }
    
    
}

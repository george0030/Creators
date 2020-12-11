package me.george0030.creators.tasks;

import me.george0030.creators.Creators;
import me.george0030.creators.io.CreatorsDB;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class TableMaintainer extends BukkitRunnable {
    
    private final Creators plugin;
    private final CreatorsDB db;
    
    
    public TableMaintainer(Creators plugin) {
        this.plugin = plugin;
        this.db = plugin.database;
    }
    
    @Override
    public void run() {
        try {
            int[] updates = db.refreshTable();
            int successfulUpdates = Arrays.stream(updates).reduce(0, (x, y) -> y > 0 ? x + y : x);
            plugin.getLogger().finest("Database maintenance: " + successfulUpdates + " entries' YouTube data "
                                              + "updated");
        } catch (SQLException throwables) {
            plugin.getLogger().warning("Exception in database maintenance");
            throwables.printStackTrace();
        } catch (IOException e) {
            plugin.getLogger().warning("Unable to access YouTube");
            e.printStackTrace();
        }
    }
}

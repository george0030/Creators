package me.george0030.creators.tasks;

import me.george0030.creators.Creators;
import me.george0030.creators.io.CreatorsDB;
import me.george0030.creators.listener.CreatorsGUI;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class RegularFetcher extends BukkitRunnable {

    private final Creators plugin;
    private final CreatorsDB db;
    private final CreatorsGUI gui;
    private final long queryCooldown;
    private final long retryCooldown;
    private final int queryRetries;
    private final boolean logging;
    private final int localCache;

    private int fails;
    private SQLException latestException;

    public RegularFetcher(Creators plugin) {
        this.plugin = plugin;
        db = plugin.database;
        gui = plugin.gui;
        queryCooldown = plugin.getConfig().getLong("query_cooldown");
        retryCooldown = plugin.getConfig().getLong("retry_cooldown");
        queryRetries = plugin.getConfig().getInt("query_retries");
        logging = plugin.getConfig().getBoolean("logging");
        localCache = plugin.getConfig().getInt("local_cache");
        fails = 0;
    }

    @Override
    public void run() {
        synchronized (db) {

            boolean hasSucceeded = false;
            while (fails < queryRetries && !hasSucceeded) {
                try {
                    db.fetchToCache();
                    hasSucceeded = true;
                } catch (SQLException throwables) {
                    fails++;
                    try {
                        db.openConnection();
                        latestException = throwables;
                    } catch (SQLException e) {
                        latestException = e;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (fails == queryRetries) {
                plugin.getLogger().warning("Database query failed " + fails + " times. Latest exception: ");
                latestException.printStackTrace();
                return;

            } else {
                plugin.getLogger().finest("Successfully queried with " + fails + " fails.");
            }

            gui.updateInventoryCache(db.cache, db.cache.size() >= db.numberOfCreatorsInDatabase);

            fails = 0;


        }
    }


}

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
    private final int queryRetries;

    private int fails;
    private SQLException latestException;

    public RegularFetcher(Creators plugin) {
        this.plugin = plugin;
        db = plugin.database;
        gui = plugin.gui;
        queryRetries = plugin.getConfig().getInt("query_retries");
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

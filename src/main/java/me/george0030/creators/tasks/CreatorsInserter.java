package me.george0030.creators.tasks;

import me.george0030.creators.Creators;
import me.george0030.creators.io.CreatorsDB;
import me.george0030.creators.listener.CreatorsGUI;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class CreatorsInserter extends BukkitRunnable {

    private final Creators plugin;
    private final CreatorsDB db;
    private final UUID playerID;
    private final String playerName;
    private final String youtube;
    private final boolean showGUI;
    private final boolean refreshCache;
    private final boolean closeAnvilGUI;

    public CreatorsInserter(Creators plugin, UUID playerID, String playerName, String youtube,
                            boolean refreshCache, boolean closeAnvilGUI, boolean showGUI) {

        this.plugin = plugin;
        this.db = plugin.database;
        this.playerID = playerID;
        this.playerName = playerName;
        this.youtube = youtube;
        this.refreshCache = refreshCache;
        this.showGUI = showGUI;
        this.closeAnvilGUI = closeAnvilGUI;

    }

    @Override
    public void run() {

        long subCount;
        try {
            subCount = plugin.youtubeData.findSubCount(youtube.substring(youtube.lastIndexOf("/") + 1));
        } catch (IOException e) {
            plugin.getLogger().warning("Unable to access YouTube");
            e.printStackTrace();
            new BukkitRunnable() {

                @Override
                public void run() {
    
                    plugin.gui.closeAnvilGUI(playerID);
                    plugin.gui.openAnvilGUI(plugin.getServer().getPlayer(playerID), "§4Unable to access youtube",
                                            "§7Loading...", CreatorsGUI.ITEM_TO_NAME, showGUI);
    
                }
            }.runTask(plugin);
            return;
        }

        if (subCount < 0) {
            new BukkitRunnable() {

                @Override
                public void run() {
    
                    plugin.gui.closeAnvilGUI(playerID);
                    plugin.gui.openAnvilGUI(plugin.getServer().getPlayer(playerID), "§4Channel not found", "§7Loading"
                            + "...", CreatorsGUI.ITEM_TO_NAME, showGUI);
    
                }
            }.runTask(plugin);
            return;
        }

        try {
            db.insert(playerName, youtube.substring(youtube.lastIndexOf("/") + 1), subCount);
        } catch (SQLException throwables) {
            plugin.getLogger().warning("Failed to insert " + playerName + " into database.");
            throwables.printStackTrace();
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.gui.closeAnvilGUI(playerID);
                    plugin.gui.openAnvilGUI(plugin.getServer().getPlayer(playerID), "§cUnable to write to database",
                                            "§7Loading...", CreatorsGUI.ITEM_TO_NAME, showGUI);
                }
            }.runTask(plugin);
            return;
        }
    
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().getPlayer(playerID).sendMessage(
                        "§a Successfully registered YouTube channel §dyoutube.com/" + youtube.substring(
                                youtube.lastIndexOf("/") + 1));
            }
        }.runTask(plugin);
    
        if (refreshCache) {
            synchronized (db) {
                try {
                    db.fetchToCache();
                    plugin.gui.updateInventoryCache(db.cache, db.cache.size() >= db.numberOfCreatorsInDatabase);
                } catch (SQLException throwables) {
                    plugin.getLogger().warning("Database query requested for " + playerName + " failed.");
                    throwables.printStackTrace();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.gui.closeAnvilGUI(playerID);
                            plugin.gui.openAnvilGUI(plugin.getServer().getPlayer(playerID), "§cCan't access database"
                                    , "§7Loading...", CreatorsGUI.ITEM_TO_NAME, showGUI);
                        }
                    }.runTask(plugin);
                    return;
                }
            }
        }

        if (closeAnvilGUI) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.gui.closeAnvilGUI(playerID);
                }
            }.runTask(plugin);
        }

        if (showGUI) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    plugin.gui.openGUIfromCache(plugin.getServer().getPlayer(playerID));
                }
            }.runTask(plugin);
        }

    }
}

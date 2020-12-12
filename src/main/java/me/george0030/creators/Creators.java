package me.george0030.creators;

import me.george0030.creators.io.CreatorsDB;
import me.george0030.creators.io.YoutubeData;
import me.george0030.creators.listener.*;
import me.george0030.creators.tasks.RegularFetcher;
import me.george0030.creators.tasks.TableMaintainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Creators extends JavaPlugin {
    
    
    public CreatorsGUI gui;
    public CreatorsDB database;
    public YoutubeData youtubeData;
    public CreatorsCommand creatorsCommand;
    public CreatorsListener listener;
    public RegularFetcher fetcher;
    public TableMaintainer maintainer;
    
    @Override
    public void onDisable() {
        
        try {
            int[] result = database.updateLastLogonsNow(
                    getServer().getOnlinePlayers().stream().map(p -> p.getUniqueId()).collect(Collectors.toList()));
            int successfulUpdates = Arrays.stream(result).reduce(0, (x, y) -> y > 0 ? x + y : x);
            getServer().getLogger().finest(
                    "Successfully updated last seen data of " + successfulUpdates + " creator(s) to the database");
        } catch (SQLException throwables) {
            getLogger().warning("Could not update last seen data of creators to database.");
            throwables.printStackTrace();
        }
        
    }
    
    @Override
    public void onEnable() {
        
        saveDefaultConfig();
        this.gui = new CreatorsGUI(this, getConfig().getInt("gui_size"));
        this.database = new CreatorsDB(this);
        this.youtubeData = new YoutubeData(this);
        this.creatorsCommand = new CreatorsCommand(this);
        this.listener = new CreatorsListener(this);
        this.fetcher = new RegularFetcher(this);
        this.maintainer = new TableMaintainer(this);
    
        try {
            database.openConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        } catch (ClassNotFoundException e) {
            getLogger().severe("SQL driver not found");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    
        maintainer.run();
        fetcher.run();
        maintainer.runTaskTimerAsynchronously(this, getConfig().getLong("maintenance_cooldown"), getConfig().getLong(
                "maintenance_cooldown"));
        fetcher.runTaskTimerAsynchronously(this, getConfig().getLong("query_cooldown"),
                                           getConfig().getLong("query_cooldown"));
        getServer().getPluginManager().registerEvents(listener, this);
        getCommand("creators").setExecutor(creatorsCommand);
    
    }
}

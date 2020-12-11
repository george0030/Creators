package me.george0030.creators;

import me.george0030.creators.io.CreatorsDB;
import me.george0030.creators.io.YoutubeData;
import me.george0030.creators.listener.*;
import me.george0030.creators.tasks.RegularFetcher;
import me.george0030.creators.tasks.TableMaintainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Creators extends JavaPlugin {


    public CreatorsGUI gui;
    public CreatorsDB database;
    public YoutubeData youtubeData;
    public CreatorsCommand creatorsCommand;
    public CreatorsListener listener;
    public RegularFetcher fetcher;
    public TableMaintainer maintainer;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        this.gui = new CreatorsGUI(this, 27);
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

    @Override
    public void onDisable() {
    }
}

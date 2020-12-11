package me.george0030.creators;

import me.george0030.creators.io.CreatorsDB;
import me.george0030.creators.io.YoutubeData;
import me.george0030.creators.listener.*;
import me.george0030.creators.tasks.RegularFetcher;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Creators extends JavaPlugin {


    public CreatorsGUI gui;
    public CreatorsDB database;
    public YoutubeData youtubeData;
    public CreatorsCommand creatorsCommand;
    public CreatorsListener listener;
    public RegularFetcher fetcher;
    public LuckPerms luckPerms;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        this.gui = new CreatorsGUI(this, 27);
        this.database = new CreatorsDB(this);
        this.youtubeData = new YoutubeData(this);
        this.creatorsCommand = new CreatorsCommand(this);
        this.listener = new CreatorsListener(this);
        this.fetcher = new RegularFetcher(this);
        luckPerms = LuckPermsProvider.get();

        try {
            database.openConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        fetcher.run();
        fetcher.runTaskTimerAsynchronously(this, getConfig().getLong("query_cooldown"), getConfig().getLong("query_cooldown"));
        getServer().getPluginManager().registerEvents(listener, this);
        getCommand("creators").setExecutor(creatorsCommand);

    }

    @Override
    public void onDisable() {
    }
}

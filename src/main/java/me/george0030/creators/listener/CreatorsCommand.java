package me.george0030.creators.listener;

import me.george0030.creators.Creators;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatorsCommand implements CommandExecutor {


    private final Creators plugin;

    public CreatorsCommand(Creators plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("creators.youtuber") && args.length > 1 /*&& plugin.database.containsEntry*/)
                plugin.gui.openAnvilGUI(player, "§4Enter YouTube ID:", "Loading...§7", CreatorsGUI.ITEM_TO_NAME);
            else if (plugin.getConfig().getBoolean("query_regularly"))
                plugin.gui.openGUIfromCache(player);
            else {

            }
        }


        return true;
    }
}

package me.george0030.creators.listener;

import me.george0030.creators.Creators;
import org.bukkit.command.*;
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
            if (args.length >= 1) {
                boolean hasPerms = player.hasPermission("creators.youtuber");
                switch (args[0]) {
                    case "register":
                        if (hasPerms) {
                            plugin.gui.openAnvilGUI(player, "§4Enter YouTube ID:", "Loading...§7",
                                                    CreatorsGUI.ITEM_TO_NAME, false);
                        }
                    default: {
                        player.sendMessage("§bUsage:\n§l§2/creators§r§b - Show YouTube creators that play on "
                                                   + "this server.");
                        if (hasPerms) {
                            player.sendMessage("§l§2/creators register§r§b - Register your own YouTube ID.");
                        }
                    }
                }
            } else {
                plugin.gui.openGUIfromCache(player);
            }
        }
    
        return true;
    }
}

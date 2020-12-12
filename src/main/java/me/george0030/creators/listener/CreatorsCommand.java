package me.george0030.creators.listener;

import me.george0030.creators.Creators;
import me.george0030.creators.misc.CreatorsRow;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Optional;

public class CreatorsCommand implements CommandExecutor {


    private final Creators plugin;

    public CreatorsCommand(Creators plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            boolean hasViewPerms = player.hasPermission("creators.view");
            boolean hasYoutuberPerms = player.hasPermission("creators.youtuber");
            if (args.length >= 1) {
                switch (args[0]) {
                    case "register":
                        if (hasYoutuberPerms) {
                            plugin.gui.openAnvilGUI(player, "§4Enter YouTube ID:", "Loading...§7",
                                                    CreatorsGUI.ITEM_TO_NAME, false);
                        }
                        break;
                    case "link":
                        if (hasViewPerms) {
                            if (args.length >= 2) {
                                Optional<CreatorsRow> entry = plugin.database.getEntry(args[1]);
                                if (entry.isPresent()) {
                                    player.sendMessage("§3Check out §6" + args[1] + "§3 at §d" + "youtube.com/channel/"
                                                               + entry.get().youtube);
                                } else {
                                    player.sendMessage("§c Could not find creator with name §l+" + args[1]);
                                }
                            } else {
                                sendUsageMessage(player);
                            }
                        }
                        break;
                    default:
                        sendUsageMessage(player);
                }
            } else {
                if (hasViewPerms) {
                    plugin.gui.openGUIfromCache(player);
                }
            }
        }
    
        return true;
    }
    
    private void sendUsageMessage(CommandSender target) {
        boolean hasViewerPerms = target.hasPermission("creators.view");
        boolean hasYoutuberPerms = target.hasPermission("creators.youtuber");
        if (hasViewerPerms || hasYoutuberPerms) {
            target.sendMessage("§bUsage:");
            if (hasViewerPerms) {
                target.sendMessage("§l§2/creators§r§b - List YouTube creators that play on this server.");
                target.sendMessage("§l§2/creators link <player>§r§b - Show link to given creator's channel");
            }
            if (hasYoutuberPerms) {
                target.sendMessage("§l§2/creators register§r§b - Register your own YouTube ID.");
            }
        }
    }
}

package me.george0030.creators.listener;

import me.george0030.creators.Creators;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class CreatorsListener implements Listener {
    
    private final Creators plugin;
    private final CreatorsGUI gui;
    
    public CreatorsListener(Creators plugin) {
        this.plugin = plugin;
        gui = plugin.gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            if (gui.hasGUIOpen(p)) {
                e.setCancelled(true);
                ItemStack item = e.getCurrentItem();
                if (item.getType() == CreatorsGUI.NEXT_PAGE.getType()) {

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gui.nextPage(p);
                        }
                    }.runTask(plugin);
    
                } else if (item.getType() == CreatorsGUI.PREVIOUS_PAGE.getType()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gui.previousPage(p);
                        }
                    }.runTask(plugin);

                } else {

                    SkullMeta skull = (SkullMeta) item.getItemMeta();
                    p.sendMessage("§3Check out §6" + skull.getDisplayName() + "§3 at §d" + skull.getLore().get(0));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gui.closeGUI(p);
                        }
                    }.runTask(plugin);
                }

            }


        }
    }
    
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player) {
            Player p = (Player) e.getPlayer();
            if (gui.currentlyChangingPage.get(p.getUniqueId()) != null && !gui.currentlyChangingPage.get(
                    p.getUniqueId())) {
                gui.removeGUI(p.getUniqueId());
            } else {
                gui.currentlyChangingPage.put(e.getPlayer().getUniqueId(), false);
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        
        
        if (e.getPlayer().hasPermission("creators.youtuber")) {
            
            if (!plugin.database.containsEntry(e.getPlayer().getUniqueId())) {
                plugin.gui.openAnvilGUI(e.getPlayer(), "§4Enter YouTube ID:", "Loading...§7", CreatorsGUI.ITEM_TO_NAME,
                                        false);
            }
        }
        
        
    }
    
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        
        Player p = e.getPlayer();
        if (p.hasPermission("creators.youtuber")) {
            if (plugin.database.containsEntry(p.getUniqueId())) {
                new BukkitRunnable() {
                    
                    @Override
                    public void run() {
                        try {
                            plugin.database.updateLastLogonNow(p.getUniqueId());
                            plugin.getLogger().finest("Successfully updated last seen data of " + p.getName() + "/"
                                                              + p.getUniqueId().toString() + " to the database.");
                        } catch (SQLException throwables) {
                            plugin.getLogger().warning("Unable to update last seen data of " + p.getName() + "/"
                                                               + p.getUniqueId().toString() + " to the database");
                            throwables.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(plugin);
            }
        }
        
    }
}



//TODO: WIP


//package me.george0030.creators.tasks;
//
//import me.george0030.creators.Creators;
//import me.george0030.creators.misc.CreatorsRow;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.sql.SQLException;
//import java.util.List;
//import java.util.UUID;
//
//public class OnDemandFetcher extends BukkitRunnable {
//
//
//    private final UUID playerID;
//    private final int firstCreator;
//    private final int querySize;
//    private final Creators plugin;
//    private final int inventorySize;
//
//    public OnDemandFetcher(UUID playerID, int firstCreator, int querySize, Creators plugin, int inventorySize){
//
//        this.playerID = playerID;
//        this.firstCreator = firstCreator;
//        this.querySize = querySize;
//        this.plugin = plugin;
//        this.inventorySize = inventorySize;
//
//    }
//
//    @Override
//    public void run() {
//
//        Player p = Bukkit.getPlayer(playerID);
//        List<CreatorsRow> result;
//        try {
//            result = plugin.database.fetch(firstCreator, querySize);
////            List<Inventory> newGUI = plugin.gui.createGUI(0,firstCreator == 0,
////                    plugin.getConfig().getInt("extra_query_size") + querySize <= plugin.database.numberOfCreatorsInDatabase,
////                    result,
////                    size
////                    );
//        } catch (SQLException throwables) {
//            plugin.getLogger().severe("Player p failed to fetch entries "+firstCreator+"-"+(firstCreator+querySize)+" from the database.");
//            throwables.printStackTrace();
//            new BukkitRunnable(){
//                @Override
//                public void run() {
//
//                    plugin.gui.closeGUI(p);
//                    p.sendMessage("§4Error in accessing database");
//
//                }
//            }.runTask(plugin);
//
//            return;
//        }
//
//        int page = plugin.gui.whatGUI.get(playerID).size();
//        plugin.gui.appendGUI(firstCreator == 0, firstCreator + result.size() <= plugin.database.numberOfCreatorsInDatabase, result, playerID, inventorySize);
//        plugin.gui.whatPage.put(playerID, page);
//
//        new BukkitRunnable(){
//            @Override
//            public void run() {
//                p.openInventory(
//            }
//        }
//
//    }
//}

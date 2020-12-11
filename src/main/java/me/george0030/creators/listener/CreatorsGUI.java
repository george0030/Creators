package me.george0030.creators.listener;

import javafx.util.Pair;
import me.george0030.creators.Creators;
import me.george0030.creators.io.CreatorsDB;
import me.george0030.creators.misc.CreatorsRow;
import me.george0030.creators.tasks.CreatorsInserter;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class CreatorsGUI {

    public static final ItemStack NEXT_PAGE = new ItemStack(Material.GREEN_WOOL);
    public static final ItemStack PREVIOUS_PAGE = new ItemStack(Material.RED_WOOL);
    public static final ItemStack ITEM_TO_NAME = new ItemStack(Material.PAPER);
    public static final Inventory LOADING_INVENTORY = Bukkit.createInventory(null, 27, "Loading...");
    public final HashMap<UUID, Integer> whatPage;
    public final HashMap<UUID, LinkedList<Inventory>> whatGUI;
    public final HashMap<UUID, Integer> numberOfHeadsInGui;
    public final HashMap<UUID, Boolean> currentlyChangingPage;
    public final HashMap<UUID, AnvilGUI> anvilInventory;
    private final Inventory inv;
    private final Creators plugin;
    private final int size;
    private volatile Pair<LinkedList<Inventory>, Integer> inventoryCache;

    public CreatorsGUI(Creators plugin, int size) {
        this.size = size;
        inv = Bukkit.createInventory(null, size);
        this.plugin = plugin;
        inventoryCache = new Pair<LinkedList<Inventory>, Integer>(new LinkedList<Inventory>(), 0);
        whatPage = new HashMap<UUID, Integer>();
        whatGUI = new HashMap<UUID, LinkedList<Inventory>>();
        numberOfHeadsInGui = new HashMap<UUID, Integer>();
        currentlyChangingPage = new HashMap<UUID, Boolean>();
        anvilInventory = new HashMap<UUID, AnvilGUI>();
    }

    //Probably fine
    public static Pair<LinkedList<Inventory>, Integer> createGUI(boolean isFirst, boolean isLast, List<CreatorsRow> creators, int size) {

        LinkedList<Inventory> list = new LinkedList<Inventory>();
        int i = 0;
        boolean firstPage;
        boolean lastPage;
        int numberOfHeads;
        int currentPage = 1;
        while (i < creators.size()) {
            firstPage = i == 0 && isFirst;
            //TODO: REFACTOR lastPage
            lastPage = isLast && creators.size() - 1 - i <= size;
            numberOfHeads = size - (firstPage ? 0 : 1) - (lastPage ? 0 : 1);
            list.add(createPage(firstPage, lastPage,
                    creators.subList(i, Math.min(i + numberOfHeads, creators.size())),
                    size,
                    "YouTube creators - Page " + currentPage
            ));
            i += numberOfHeads;
            currentPage++;
        }

        return new Pair<LinkedList<Inventory>, Integer>(list, creators.size());

    }

    //Fine
    private static Inventory createPage(boolean isFirst, boolean isLast, List<CreatorsRow> creators, int size, String title) {


        Inventory inv = Bukkit.createInventory(null, size, title);
        if (!isFirst) {
            inv.addItem(PREVIOUS_PAGE);
        }
        for (CreatorsRow row : creators) {
            inv.addItem(createHead(row.playerUUID, "youtube.com/channel/" + row.youtube));
        }
        if (!isLast) {
            inv.addItem(NEXT_PAGE);
        }
//
//        int until = Math.min(size, creators.size());
//        int i = 0;
//
//        if(!isFirst) {
//
//            inv.addItem(new ItemStack(PREVIOUS_PAGE));
//            until--;
//
//        }
//
//        while(i < until){
//            inv.addItem(createHead(creators.get(i).playerName, "youtube.com/channel/"+ creators.get(i).youtube));
//            i++;
//        }
//        if(!isLast){
//            inv.addItem(new ItemStack(NEXT_PAGE));
//        }
//        else {
//            inv.addItem(createHead(creators.get(i).playerName, "youtube.com/channel/"+ creators.get(i).youtube));
//        }

        return inv;
    }
    
    public static ItemStack createHead(String name, List<String> description) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(name);
        skull.setLore(description);
        skull.setOwner(name);
        item.setItemMeta(skull);
        return item;
    }
    
    public static ItemStack createHead(UUID playerUUID, String description) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(description);
        return createHead(playerUUID, list);
    }
    
    public static ItemStack createHead(UUID playerUUID, List<String> description) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(player.getName());
        skull.setLore(description);
        skull.setOwningPlayer(player);
        item.setItemMeta(skull);
        return item;
    }
    
    public static ItemStack createHead(String name, String description) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(description);
        return createHead(name, list);
    }
    
    //SYNC
    public void openGUIfromCache(Player p) {
        UUID playerID = p.getUniqueId();
        Pair<LinkedList<Inventory>, Integer> newGUI = inventoryCache;
        whatPage.put(playerID, 0);
        whatGUI.put(playerID, newGUI.getKey());
        numberOfHeadsInGui.put(playerID, newGUI.getValue());
        currentlyChangingPage.put(playerID, false);
        p.openInventory(newGUI.getKey().get(0));
    }

    //SYNC
    public void closeGUI(Player p) {
        removeGUI(p.getUniqueId());
        p.closeInventory();
    }

    public void removeGUI(UUID playerID) {
        whatPage.remove(playerID);
        whatGUI.remove(playerID);
        numberOfHeadsInGui.remove(playerID);
        currentlyChangingPage.remove(playerID);
    }

    //SYNC
    public boolean hasGUIOpen(Player p) {
        return hasGUIOpen(p.getUniqueId());
    }

    //SYNC
    public boolean hasGUIOpen(UUID playerID) {
        return whatGUI.containsKey(playerID);
    }

    //ASYNC
    public void appendGUI(boolean isFirst, boolean isLast, List<CreatorsRow> creators, UUID playerID, int size) {

        Pair<LinkedList<Inventory>, Integer> newGui = createGUI(isFirst, isLast, creators, size);
        if (hasGUIOpen(playerID)) {

        }
        numberOfHeadsInGui.put(playerID, newGui.getValue());
        if (!whatPage.containsKey(playerID)) whatPage.put(playerID, 0);
    }

    //SYNC
    //refactor with iterator
    public void nextPage(Player p) {
        int newPageNumber = whatPage.get(p.getUniqueId()) + 1;
        try {
            Inventory newPage = whatGUI.get(p.getUniqueId()).get(newPageNumber);
            currentlyChangingPage.put(p.getUniqueId(), true);
            p.openInventory(newPage);
            whatPage.put(p.getUniqueId(), newPageNumber);
        } catch (Exception e) {
            return;
        }
    }

    public void previousPage(Player p) {
        int newPageNumber = whatPage.get(p.getUniqueId()) - 1;
        try {
            Inventory newPage = whatGUI.get(p.getUniqueId()).get(newPageNumber);
            currentlyChangingPage.put(p.getUniqueId(), true);
            p.openInventory(newPage);
            whatPage.put(p.getUniqueId(), newPageNumber);
        } catch (Exception e) {
            return;
        }
    }

    //ASYNC, but the results are used only when async is over
    public void updateInventoryCache(List<CreatorsRow> creators, boolean isLast) {

        Pair<LinkedList<Inventory>, Integer> newCache = createGUI(true, isLast, creators, size);
        inventoryCache = newCache;
    }

    public Inventory createGUI(int startingFrom, List<CreatorsRow> creators) {
        int numberOfHeads;
        CreatorsDB db = plugin.database;
//        List<CreatorsRow> creators = db.get(startingFrom, size+1);
        ArrayList<ItemStack> items = new ArrayList<ItemStack>(size);
        boolean isFirst = startingFrom == 0;
        boolean isLast = creators.size() < size + 1;
        int until = Math.min(size - 2, creators.size());
        int i = 0;
        while (i < until) {
            items.add(createHead(creators.get(i).playerUUID, "youtube.com/channel/" + creators.get(i).youtube));
            i++;
        }
        if (!isFirst) {
            inv.addItem(new ItemStack(PREVIOUS_PAGE));
        } else {
            i++;
            items.add(createHead(creators.get(i).playerUUID, "youtube.com/channel/" + creators.get(i).youtube));
        }

        inv.addItem((ItemStack[]) items.toArray());
    
        if (!isLast) {
            inv.addItem(new ItemStack(NEXT_PAGE));
        } else {
            inv.addItem(
                    createHead(creators.get(i + 1).playerUUID, "youtube.com/channel/" + creators.get(i + 1).youtube));
        }
    
        return inv;
    }
    
    public void openAnvilGUI(Player p, String title, String loadingMessage, ItemStack itemToBeNamed,
                             boolean openChestGUIAfterwards) {
        
        anvilInventory.put(p.getUniqueId(), new AnvilGUI.Builder().itemLeft(itemToBeNamed)
                .title(title)
                .text("/")
                .plugin(plugin)
                .onComplete((player, s) -> {
                    
                    
                    new CreatorsInserter(plugin, player.getUniqueId(), player.getName(),
                                         s, true, true, openChestGUIAfterwards).runTaskAsynchronously(plugin);

                    return AnvilGUI.Response.text(loadingMessage);

                }).open(p));


//        if(!hasAnvilGUIOpen(p)){
//            Inventory inv = Bukkit.createInventory(null, InventoryType.ANVIL, title);
//            inv.setItem(0, itemToBeNamed);
//            inv.setItem(1, CreatorsGUI.NEXT_PAGE);
//            p.openInventory(inv);
//            anvilInventory.put(p.getUniqueId(), inv);
//            previousLevel.put(p.getUniqueId(), p.getLevel());
//            p.setLevel(1);
//
//            return inv;
//        }
//        else return null;
    }

    public void closeAnvilGUI(UUID playerID) {
        anvilInventory.get(playerID).closeInventory();
    }

    public boolean hasAnvilGUIOpen(Player p) {
        return anvilInventory.containsKey(p.getUniqueId());
    }


}

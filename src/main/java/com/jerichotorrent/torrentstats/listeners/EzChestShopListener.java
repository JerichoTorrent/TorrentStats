package com.jerichotorrent.torrentstats.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.jerichotorrent.torrentstats.TorrentStats;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;

import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.events.PlayerTransactEvent;

public class EzChestShopListener implements Listener {

    @EventHandler
    public void onPlayerTransaction(PlayerTransactEvent event) {
        OfflinePlayer customer = event.getCustomer();
        OfflinePlayer owner = event.getOwner();
        boolean isBuy = event.isBuy(); // true = buy from shop, false = sell to shop
        double price = event.getPrice();
        int amount = event.getCount();
        ItemStack item = event.getItem();

        if (customer == null || owner == null || item == null) return;

        UUID customerId = customer.getUniqueId();
        UUID ownerId = owner.getUniqueId();
        String itemName = item.getType().name();

        Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
            DatabaseManager db = TorrentStats.getInstance().getDatabaseManager();

            if (isBuy) {
                db.updateStat(customerId, customer.getName(), "shop_items_bought", amount);
                db.updateStat(customerId, customer.getName(), "shop_money_spent", (int) price);
                db.updateStat(ownerId, owner.getName(), "shop_items_sold", amount);
                db.updateStat(ownerId, owner.getName(), "shop_money_earned", (int) price);
                db.updateStat(ownerId, owner.getName(), "ez_profits", (int) price);
            } else {
                db.updateStat(customerId, customer.getName(), "shop_items_sold", amount);
                db.updateStat(customerId, customer.getName(), "shop_money_earned", (int) price);
                db.updateStat(customerId, customer.getName(), "ez_profits", (int) price);
                db.updateStat(ownerId, owner.getName(), "shop_items_bought", amount);
                db.updateStat(ownerId, owner.getName(), "shop_money_spent", (int) price);
            }

            db.updateStat(customerId, customer.getName(), "shop_transactions", 1);
            db.updateStat(ownerId, owner.getName(), "shop_transactions", 1);
            db.updateStat(customerId, customer.getName(), "shop_item_" + itemName.toLowerCase(), amount);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getName();
    
        Bukkit.getScheduler().runTaskLaterAsynchronously(TorrentStats.getInstance(), () -> {
            long ownedShops = ShopContainer.getShops().stream()
                .filter(shop -> uuid.equals(shop.getOwnerID()))
                .count();
    
            TorrentStats.getInstance().getDatabaseManager()
                .updateStat(uuid, username, "ez_shops", (int) ownedShops);
        }, 60L); // Delay to let EzChestShop finish loading
    }
}

package com.jerichotorrent.torrentstats.listeners;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.jerichotorrent.torrentstats.TorrentStats;

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

        if (isBuy) {
            update(customerId, customer.getName(), "shop_items_bought", amount);
            update(customerId, customer.getName(), "shop_money_spent", price);
            update(ownerId, owner.getName(), "shop_items_sold", amount);
            update(ownerId, owner.getName(), "shop_money_earned", price);
        } else {
            update(customerId, customer.getName(), "shop_items_sold", amount);
            update(customerId, customer.getName(), "shop_money_earned", price);
            update(ownerId, owner.getName(), "shop_items_bought", amount);
            update(ownerId, owner.getName(), "shop_money_spent", price);
        }

        update(customerId, customer.getName(), "shop_transactions", 1);
        update(ownerId, owner.getName(), "shop_transactions", 1);
        update(customerId, customer.getName(), "shop_item_" + itemName.toLowerCase(), amount);
    }

    private void update(UUID uuid, String name, String stat, double value) {
        TorrentStats.getInstance().getDatabaseManager().updateStat(uuid, name, stat, (int) value);
    }
}

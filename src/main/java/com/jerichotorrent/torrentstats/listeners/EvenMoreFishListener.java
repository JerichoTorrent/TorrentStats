package com.jerichotorrent.torrentstats.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.jerichotorrent.torrentstats.TorrentStats;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;
import com.oheers.fish.api.EMFFishEvent;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;

public class EvenMoreFishListener implements Listener {

    @EventHandler
    public void onFishCatch(EMFFishEvent event) {
        Fish fish = event.getFish();
        Rarity rarity = fish.getRarity();
        UUID uuid = event.getPlayer().getUniqueId();
        String username = event.getPlayer().getName();

        DatabaseManager db = TorrentStats.getInstance().getDatabaseManager();

        // Track legendary fish caught
        if (rarity.toString().equalsIgnoreCase("legendary")) {
            db.updateStat(uuid, username, "legendary_fish_caught", 1);
        }

        // Track largest fish caught
        double caughtLength = fish.getLength();
        double currentRecord = db.getDoubleStat(uuid, "largest_fish");

        if (caughtLength > currentRecord) {
            db.setDoubleStat(uuid, username, "largest_fish", caughtLength);
        }
    }
}

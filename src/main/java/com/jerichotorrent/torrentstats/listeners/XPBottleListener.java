package com.jerichotorrent.torrentstats.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.jerichotorrent.torrentstats.TorrentStats;

public class XPBottleListener implements Listener {

    @EventHandler
    public void onBottleCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().toLowerCase().startsWith("/xpbottle")) return;

        Player player = event.getPlayer();
        int xpLevel = player.getLevel(); // Estimate pre-bottle level

        // Delay check so the XP change has time to apply
        Bukkit.getScheduler().runTaskLater(TorrentStats.getInstance(), () -> {
            int newLevel = player.getLevel();
            int lost = xpLevel - newLevel;

            if (lost > 0) {
                // Run DB call asynchronously
                Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
                    TorrentStats.getInstance().getDatabaseManager()
                        .updateStat(player.getUniqueId(), player.getName(), "total_xp_bottled", lost);
                });
            }
        }, 2L); // Wait 2 ticks for XP to update
    }
}

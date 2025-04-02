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
        int xpLevel = player.getLevel(); // Best guess pre-bottle level

        // Schedule a delayed check AFTER plugin processes it
        Bukkit.getScheduler().runTaskLater(TorrentStats.getInstance(), () -> {
            int newLevel = player.getLevel();
            int lost = xpLevel - newLevel;

            if (lost > 0) {
                TorrentStats.getInstance().getDatabaseManager()
                    .updateStat(player.getUniqueId(), player.getName(), "total_xp_bottled", lost);
            }
        }, 2L); // 2 ticks delay to let the bottle plugin do its thing
    }
}

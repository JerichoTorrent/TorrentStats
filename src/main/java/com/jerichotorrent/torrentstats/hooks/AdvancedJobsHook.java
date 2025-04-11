package com.jerichotorrent.torrentstats.hooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jerichotorrent.torrentstats.TorrentStats;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;

public class AdvancedJobsHook {

    private final Plugin plugin;
    private final DatabaseManager database;

    public AdvancedJobsHook() {
        this.plugin = Bukkit.getPluginManager().getPlugin("AdvancedJobs");
        this.database = TorrentStats.getInstance().getDatabaseManager();
    }

    public boolean isAvailable() {
        return plugin != null && plugin.isEnabled();
    }

    public void syncJobStats(Player player) {
        if (!isAvailable()) return;

        UUID uuid = player.getUniqueId();
        String username = player.getName();

        Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
            try {
                Class<?> ajAPI = Class.forName("me.wazup.advancedjobs.API");
                Method getLevels = ajAPI.getMethod("getPlayerJobStats", Player.class);
                Method getXP = ajAPI.getMethod("getJobXP", Player.class, String.class);

                Object result = getLevels.invoke(null, player);
                if (result instanceof Map<?, ?> jobStats) {
                    for (Map.Entry<?, ?> entry : jobStats.entrySet()) {
                        String jobName = entry.getKey().toString();
                        int level = Integer.parseInt(entry.getValue().toString());

                        double xp = 0.0;
                        try {
                            Object xpResult = getXP.invoke(null, player, jobName);
                            xp = xpResult instanceof Number ? ((Number) xpResult).doubleValue() : 0.0;
                        } catch (IllegalAccessException | InvocationTargetException ignored) {}

                        database.updateJobStat(uuid, username, jobName, level, xp);
                    }
                }
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
                     NumberFormatException | SecurityException | InvocationTargetException e) {
                TorrentStats.getInstance().getLogger().log(Level.WARNING, "Failed to sync AdvancedJobs stats: {0}", e.getMessage());
            }
        });
    }
}

package com.jerichotorrent.torrentstats.hooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jerichotorrent.torrentstats.TorrentStats;

public class AdvancedJobsHook {

    private final Plugin plugin;

    public AdvancedJobsHook() {
        this.plugin = Bukkit.getPluginManager().getPlugin("AdvancedJobs");
    }

    public boolean isAvailable() {
        return plugin != null && plugin.isEnabled();
    }

    public void syncJobStats(Player player) {
        if (!isAvailable()) return;
    
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
    
                    TorrentStats.getInstance().getDatabaseManager()
                        .updateJobStat(player.getUniqueId(), player.getName(), jobName, level, xp);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | NumberFormatException | SecurityException | InvocationTargetException e) {
            TorrentStats.getInstance().getLogger().log(Level.WARNING, "Failed to sync AdvancedJobs stats: {0}", e.getMessage());
        }
    }
}

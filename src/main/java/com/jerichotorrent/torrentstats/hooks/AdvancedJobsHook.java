package com.jerichotorrent.torrentstats.hooks;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.jerichotorrent.torrentstats.TorrentStats;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;

import net.advancedplugins.jobs.Core;
import net.advancedplugins.jobs.objects.users.JobStore;
import net.advancedplugins.jobs.objects.users.User;
import net.advancedplugins.jobs.objects.users.UserJobInfo;
import net.advancedplugins.simplespigot.storage.storage.Storage;

public class AdvancedJobsHook {

    private final DatabaseManager database;

    public AdvancedJobsHook() {
        this.database = TorrentStats.getInstance().getDatabaseManager();
    }

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("AdvancedJobs");
    }

    @SuppressWarnings("LoggerStringConcat")
    public void syncJobStats(Player player) {
        if (!isAvailable()) return;

        UUID uuid = player.getUniqueId();
        String username = player.getName();

        Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
            try {
                Storage<User> storage = Core.getInstance().getUserStorage();
                User user = storage.load(uuid.toString());

                if (user == null) {
                    Bukkit.getLogger().warning("[TorrentStats] AdvancedJobs: No user data found for " + username);
                    return;
                }

                JobStore jobStore = user.getJobStore();
                Map<String, UserJobInfo> jobs = jobStore.asMap();

                if (jobs.isEmpty()) {
                    Bukkit.getLogger().info("[TorrentStats] AdvancedJobs: No jobs to sync for " + username);
                    return;
                }

                for (Map.Entry<String, UserJobInfo> entry : jobs.entrySet()) {
                    String jobName = entry.getKey();
                    UserJobInfo info = entry.getValue();
                    int level = info.getLevel();

                    double xp = 0.0;
                    try {
                        Field xpField = UserJobInfo.class.getDeclaredField("N");
                        xpField.setAccessible(true);
                        Object raw = xpField.get(info);
                        if (raw instanceof BigDecimal bd) {
                            xp = bd.doubleValue();
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                        Bukkit.getLogger().warning("[TorrentStats] Failed to read XP for job '" + jobName + "' → " + e.getMessage());
                    }

                    database.updateJobStat(uuid, username, jobName, level, xp);
                }

                Bukkit.getLogger().info("[TorrentStats] Synced " + jobs.size() + " AdvancedJobs for " + username);
            } catch (Exception ex) {
                Bukkit.getLogger().severe("[TorrentStats] Failed AdvancedJobs sync for " + username + ": " + ex.getMessage());
            }
        });
    }
}

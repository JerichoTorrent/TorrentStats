package com.jerichotorrent.torrentstats.hooks;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.McMMOPlayerNotFoundException;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.jerichotorrent.torrentstats.TorrentStats;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;

public class McMMOHook {

    private final DatabaseManager database;

    public McMMOHook() {
        this.database = TorrentStats.getInstance().getDatabaseManager();
    }

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("mcMMO");
    }

    public void syncMcMMOStats(Player player) {
        if (!isAvailable()) return;

        UUID uuid = player.getUniqueId();
        String username = player.getName();

        Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
            // Power level
            try {
                int powerLevel = ExperienceAPI.getPowerLevel(player);
                database.updatePowerLevel(uuid, username, powerLevel);

            // Loop through skills
                for (PrimarySkillType skill : PrimarySkillType.values()) {
                    if (skill.isChildSkill()) continue; // Skip child skills; deprecated but the only way I know of to do it
                    String skillName = skill.name();
                    @SuppressWarnings("deprecation")
                    int level = ExperienceAPI.getLevel(player, skillName);
                    float currentXp = ExperienceAPI.getXP(player, skillName);
                    float xpForNextLevel = ExperienceAPI.getXPToNextLevel(player, skillName);
                    float xpToLevel = Math.max(xpForNextLevel, 0);

                    database.updateSkillStat(uuid, skillName, level, currentXp, xpToLevel);
                }
            } catch (McMMOPlayerNotFoundException e) {
            Bukkit.getLogger().log(Level.WARNING, "[TorrentStats] mcMMO profile not loaded yet for {0}", player.getName());
            }
        });
    }
}

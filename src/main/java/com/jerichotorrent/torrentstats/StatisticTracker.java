package com.jerichotorrent.torrentstats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StatisticTracker {

    private final TorrentStats plugin;

    public StatisticTracker(TorrentStats plugin) {
        this.plugin = plugin;
    }

    public void syncPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();
        Map<String, Integer> stats = new HashMap<>();

        // Direct stat mappings
        stats.put("animals_bred", player.getStatistic(Statistic.ANIMALS_BRED));
        stats.put("aviate_cm", player.getStatistic(Statistic.AVIATE_ONE_CM));
        stats.put("climb_cm", player.getStatistic(Statistic.CLIMB_ONE_CM));
        stats.put("deaths", player.getStatistic(Statistic.DEATHS));
        stats.put("fall_cm", player.getStatistic(Statistic.FALL_ONE_CM));
        stats.put("fish_caught", player.getStatistic(Statistic.FISH_CAUGHT));
        stats.put("fly_cm", player.getStatistic(Statistic.FLY_ONE_CM));
        stats.put("jumps", player.getStatistic(Statistic.JUMP));
        stats.put("mob_kills", player.getStatistic(Statistic.MOB_KILLS));
        stats.put("ticks_played", player.getStatistic(Statistic.PLAY_ONE_MINUTE));
        stats.put("player_kills", player.getStatistic(Statistic.PLAYER_KILLS));
        stats.put("raid_wins", player.getStatistic(Statistic.RAID_WIN));
        stats.put("beds_slept", player.getStatistic(Statistic.SLEEP_IN_BED));
        stats.put("swim_cm", player.getStatistic(Statistic.SWIM_ONE_CM));
        stats.put("villager_trades", player.getStatistic(Statistic.TRADED_WITH_VILLAGER));
        stats.put("walk_cm", player.getStatistic(Statistic.WALK_ONE_CM));

        // Sub-stat totals
        int breakItem = 0;
        int craftItem = 0;
        int mineBlock = 0;

        for (Material material : Material.values()) {
            if (material.isItem()) {
                try {
                    breakItem += player.getStatistic(Statistic.BREAK_ITEM, material);
                    craftItem += player.getStatistic(Statistic.CRAFT_ITEM, material);
                } catch (IllegalArgumentException ignored) {}
            }

            if (material.isBlock()) {
                try {
                    mineBlock += player.getStatistic(Statistic.MINE_BLOCK, material);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        int killPlayerEntity = 0;
        try {
            killPlayerEntity = player.getStatistic(Statistic.KILL_ENTITY, EntityType.PLAYER);
        } catch (IllegalArgumentException ignored) {}

        stats.put("items_broken", breakItem);
        stats.put("items_crafted", craftItem);
        stats.put("blocks_mined", mineBlock);
        stats.put("players_killed_entity", killPlayerEntity);

        // Async database update
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().updateStatsBatch(uuid, username, stats);
        });
    }
}

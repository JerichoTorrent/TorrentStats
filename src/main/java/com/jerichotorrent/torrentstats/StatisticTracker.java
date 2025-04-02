package com.jerichotorrent.torrentstats;

import java.util.UUID;

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

        updateStat(uuid, username, "animals_bred", player.getStatistic(Statistic.ANIMALS_BRED));
        updateStat(uuid, username, "aviate_cm", player.getStatistic(Statistic.AVIATE_ONE_CM));
        updateStat(uuid, username, "climb_cm", player.getStatistic(Statistic.CLIMB_ONE_CM));
        updateStat(uuid, username, "deaths", player.getStatistic(Statistic.DEATHS));
        updateStat(uuid, username, "fall_cm", player.getStatistic(Statistic.FALL_ONE_CM));
        updateStat(uuid, username, "fish_caught", player.getStatistic(Statistic.FISH_CAUGHT));
        updateStat(uuid, username, "fly_cm", player.getStatistic(Statistic.FLY_ONE_CM));
        updateStat(uuid, username, "jumps", player.getStatistic(Statistic.JUMP));
        updateStat(uuid, username, "mob_kills", player.getStatistic(Statistic.MOB_KILLS));
        updateStat(uuid, username, "ticks_played", player.getStatistic(Statistic.PLAY_ONE_MINUTE));
        updateStat(uuid, username, "player_kills", player.getStatistic(Statistic.PLAYER_KILLS));
        updateStat(uuid, username, "raid_wins", player.getStatistic(Statistic.RAID_WIN));
        updateStat(uuid, username, "beds_slept", player.getStatistic(Statistic.SLEEP_IN_BED));
        updateStat(uuid, username, "swim_cm", player.getStatistic(Statistic.SWIM_ONE_CM));
        updateStat(uuid, username, "villager_trades", player.getStatistic(Statistic.TRADED_WITH_VILLAGER));
        updateStat(uuid, username, "walk_cm", player.getStatistic(Statistic.WALK_ONE_CM));

        // Sub-stat totals
        int breakItem = 0;
        int craftItem = 0;
        int mineBlock = 0;
        int killPlayerEntity = 0;

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

        try {
            killPlayerEntity = player.getStatistic(Statistic.KILL_ENTITY, EntityType.PLAYER);
        } catch (IllegalArgumentException ignored) {}

        updateStat(uuid, username, "items_broken", breakItem);
        updateStat(uuid, username, "items_crafted", craftItem);
        updateStat(uuid, username, "blocks_mined", mineBlock);
        updateStat(uuid, username, "players_killed_entity", killPlayerEntity);
    }

    private void updateStat(UUID uuid, String username, String column, int value) {
        plugin.getDatabaseManager().updateStat(uuid, username, column, value);
    }
}

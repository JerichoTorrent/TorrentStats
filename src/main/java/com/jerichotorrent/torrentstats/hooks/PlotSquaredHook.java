package com.jerichotorrent.torrentstats.hooks;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.jerichotorrent.torrentstats.TorrentStats;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

public class PlotSquaredHook {

    private final DatabaseManager database;

    public PlotSquaredHook() {
        this.database = TorrentStats.getInstance().getDatabaseManager();
    }

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("PlotSquared");
    }

    public void syncPlotStats(Player player) {
        if (!isAvailable()) return;

        UUID uuid = player.getUniqueId();
        String username = player.getName();

        Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
            PlotPlayer<?> plotPlayer = PlotPlayer.from(player);
            if (plotPlayer == null) return;

            Set<Plot> plots = plotPlayer.getPlots();
            if (plots == null || plots.isEmpty()) {
                database.updateStat(uuid, username, "plots_owned", 0);
                database.updateStat(uuid, username, "plots_merged", 0);
                return;
            }

            int ownedPlots = plots.size();
            long mergedPlots = plots.stream().filter(Plot::isMerged).count();

            database.updateStat(uuid, username, "plots_owned", ownedPlots);
            database.updateStat(uuid, username, "plots_merged", (int) mergedPlots);
        });
    }
}

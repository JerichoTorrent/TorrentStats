package com.jerichotorrent.torrentstats.listeners;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.customEvents.LevelupTeamEvent;
import com.booksaw.betterTeams.customEvents.post.PostPlayerJoinTeamEvent;
import com.booksaw.betterTeams.customEvents.post.PostPlayerLeaveTeamEvent;
import com.jerichotorrent.torrentstats.TorrentStats;

public class BetterTeamsListener implements Listener {

    private final TorrentStats plugin;

    public BetterTeamsListener(TorrentStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinTeam(PostPlayerJoinTeamEvent event) {
        OfflinePlayer offline = event.getPlayer();
        if (offline.isOnline()) {
            Player player = offline.getPlayer();
            if (player != null) {
                Team team = event.getTeam();
                updatePlayerTeamStatsAsync(player, team);
            }
        }
    }

    @EventHandler
    public void onPlayerLeaveTeam(PostPlayerLeaveTeamEvent event) {
        OfflinePlayer offline = event.getPlayer();
        if (offline.isOnline()) {
            Player player = offline.getPlayer();
            if (player != null) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getDatabaseManager().clearTeamStats(player.getUniqueId());
                });
            }
        }
    }

    @EventHandler
    public void onTeamLevelUp(LevelupTeamEvent event) {
        updateAllTeamMembersAsync(event.getTeam());
    }

    private void updateAllTeamMembersAsync(Team team) {
        Set<UUID> memberIds = team.getMembers().getOfflinePlayers().stream()
                .map(OfflinePlayer::getUniqueId)
                .collect(Collectors.toSet());

        for (UUID uuid : memberIds) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            if (offline.isOnline()) {
                Player player = offline.getPlayer();
                if (player != null) {
                    updatePlayerTeamStatsAsync(player, team);
                }
            }
        }
    }

    public void syncTeamStats(Player player) {
        Team team = com.booksaw.betterTeams.Team.getTeam(player);
        if (team == null) return;
        updatePlayerTeamStatsAsync(player, team);
    }

    private void updatePlayerTeamStatsAsync(Player player, Team team) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = player.getUniqueId();
            String username = player.getName();
            String teamName = team.getName();
            int level = team.getLevel();

            double balance = 0.0;
            try {
                balance = Double.parseDouble(team.getBalance());
            } catch (NumberFormatException ignored) {}

            int members = team.getMembers().getOfflinePlayers().size();
            String server = plugin.getConfigLoader().getServerName();

            plugin.getDatabaseManager().updateTeamStats(uuid, username, server, teamName, level, balance, members);
        });
    }
}

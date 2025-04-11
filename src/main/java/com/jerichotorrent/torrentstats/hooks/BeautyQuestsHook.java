package com.jerichotorrent.torrentstats.hooks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.jerichotorrent.torrentstats.TorrentStats;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;

import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;

public class BeautyQuestsHook {

    private final DatabaseManager database;

    public BeautyQuestsHook() {
        this.database = TorrentStats.getInstance().getDatabaseManager();
    }

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("BeautyQuests");
    }

    public void syncQuestStats(Player player) {
        if (!isAvailable()) return;

        UUID uuid = player.getUniqueId();
        String username = player.getName();

        // Async: get quest count and write to DB
        Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
            PlayerAccount account = PlayersManager.getPlayerAccount(player);
            if (account == null) return;

            int completed = account.getQuestsDatas().size();
            database.updateStat(uuid, username, "quests_completed", completed);
        });
    }
}

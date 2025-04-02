package com.jerichotorrent.torrentstats.hooks;

import com.jerichotorrent.torrentstats.TorrentStats;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class VaultHook {

    private Economy economy;

    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        economy = rsp.getProvider();
        return economy != null;
    }

    public void syncBalance(Player player) {
        if (economy == null) return;

        UUID uuid = player.getUniqueId();
        String name = player.getName();
        double balance = economy.getBalance(player);

        TorrentStats.getInstance().getDatabaseManager()
                .updateStat(uuid, name, "balance", (int) balance); // Store as int for now
    }
}

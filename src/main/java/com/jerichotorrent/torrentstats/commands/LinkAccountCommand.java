package com.jerichotorrent.torrentstats.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jerichotorrent.torrentstats.TorrentStats;

public class LinkAccountCommand implements CommandExecutor {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MILLIS = 60_000; // 1 minute

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN_MILLIS) {
            long secondsLeft = (COOLDOWN_MILLIS - (now - cooldowns.get(uuid))) / 1000;
            player.sendMessage("§cPlease wait " + secondsLeft + " seconds before using this command again.");
            return true;
        }

        cooldowns.put(uuid, now);

        String token = generateToken();

        // Async DB store
        Bukkit.getScheduler().runTaskAsynchronously(TorrentStats.getInstance(), () -> {
            TorrentStats.getInstance().getDatabaseManager().storeLinkToken(uuid, token);
        });

        String baseUrl = TorrentStats.getInstance().getConfig().getString("web-url", "https://torrentnetwork.com");
        String fullLink = baseUrl + "/link?token=" + token;

        player.sendMessage("§d§lLink your Minecraft account:");
        player.sendMessage("§5§n" + fullLink);

        return true;
    }

    private String generateToken() {
        // 32-character token: 16 hex digits x 2
        return Long.toHexString(ThreadLocalRandom.current().nextLong()) +
               Long.toHexString(ThreadLocalRandom.current().nextLong());
    }
}

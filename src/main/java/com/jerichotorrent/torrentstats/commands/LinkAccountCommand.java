package com.jerichotorrent.torrentstats.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkAccountCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        String token = player.getUniqueId().toString(); // Replace with real token logic
        String link = "https://torrentsmp.com/link?token=" + token;

        player.sendMessage("§dLink your account here:");
        player.sendMessage("§5§n" + link);
        return true;
    }
}

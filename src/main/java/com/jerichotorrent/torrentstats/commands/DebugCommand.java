package com.jerichotorrent.torrentstats.commands;

import com.jerichotorrent.torrentstats.TorrentStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DebugCommand implements CommandExecutor {

    private final TorrentStats plugin;

    public DebugCommand(TorrentStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("debug")) {
            if (!sender.hasPermission("torrentstats.debug")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            sender.sendMessage("§eTorrentStats Debug Info:");
            sender.sendMessage("§7Server Name: §f" + plugin.getServerName());
            sender.sendMessage("§7Database Connected: §f" +
                    (plugin.getDatabaseManager().isConnected() ? "§aYes" : "§cNo"));
            sender.sendMessage("§7Enabled Hooks: §f" +
                    plugin.getConfigLoader().getEnabledHooks().keySet());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("torrentstats.reload")) {
                sender.sendMessage("§cYou do not have permission to reload the config.");
                return true;
            }

            plugin.reloadConfig();
            plugin.setConfigLoader(new com.jerichotorrent.torrentstats.utils.ConfigLoader(plugin));
            plugin.setServerName(plugin.getConfig().getString("server-name", "default"));

            sender.sendMessage("§aTorrentStats config reloaded.");
            return true;
        }

        sender.sendMessage("§cUnknown subcommand. Use /torrentstats debug or /torrentstats reload.");
        return true;
    }
}

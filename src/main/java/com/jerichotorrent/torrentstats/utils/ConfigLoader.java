package com.jerichotorrent.torrentstats.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.jerichotorrent.torrentstats.TorrentStats;

public class ConfigLoader {
    private final Map<String, Boolean> enabledHooks = new HashMap<>();
    private final String serverName;

    public ConfigLoader(TorrentStats plugin) {
        FileConfiguration config = plugin.getConfig();

        if (config.contains("enabled-hooks")) {
            for (String hook : config.getConfigurationSection("enabled-hooks").getKeys(false)) {
                enabledHooks.put(hook, config.getBoolean("enabled-hooks." + hook));
            }
        }

        this.serverName = config.getString("server-name", "default");
    }

    public Map<String, Boolean> getEnabledHooks() {
        return enabledHooks;
    }

    public boolean isHookEnabled(String key) {
        return enabledHooks.getOrDefault(key, false);
    }

    public String getServerName() {
        return serverName;
    }
}

package com.jerichotorrent.torrentstats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jerichotorrent.torrentstats.commands.DebugCommand;
import com.jerichotorrent.torrentstats.commands.LinkAccountCommand;
import com.jerichotorrent.torrentstats.hooks.AdvancedJobsHook;
import com.jerichotorrent.torrentstats.hooks.BeautyQuestsHook;
import com.jerichotorrent.torrentstats.hooks.McMMOHook;
import com.jerichotorrent.torrentstats.hooks.PlotSquaredHook;
import com.jerichotorrent.torrentstats.hooks.VaultHook;
import com.jerichotorrent.torrentstats.listeners.BetterTeamsListener;
import com.jerichotorrent.torrentstats.listeners.EvenMoreFishListener;
import com.jerichotorrent.torrentstats.listeners.EzChestShopListener;
import com.jerichotorrent.torrentstats.listeners.XPBottleListener;
import com.jerichotorrent.torrentstats.storage.DatabaseManager;
import com.jerichotorrent.torrentstats.utils.ConfigLoader;

public class TorrentStats extends JavaPlugin {

    private static TorrentStats instance;
    private ConfigLoader configLoader;
    private DatabaseManager databaseManager;
    private VaultHook vaultHook;
    private AdvancedJobsHook advancedJobsHook;
    private McMMOHook mcMMOHook;
    private String serverName;
    private StatisticTracker statisticTracker;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configLoader = new ConfigLoader(this);

        serverName = getConfig().getString("server-name", "default");

        databaseManager = new DatabaseManager();
        databaseManager.initialize();

        // Command: /login
        if (getCommand("login") != null) {
            getCommand("login").setExecutor(new LinkAccountCommand());
        } else {
            getLogger().warning("Command 'login' not found in plugin.yml!");
        }
        getCommand("torrentstats").setExecutor(new DebugCommand(this));

        // Hook: XPBottle
        if (configLoader.isHookEnabled("expbottle")) {
            getServer().getPluginManager().registerEvents(new XPBottleListener(), this);
            getLogger().info("Listening for /bottle command (ExpBottle).");
        }

        // Hook: EvenMoreFish
        if (configLoader.isHookEnabled("evenmorefish")) {
            getServer().getPluginManager().registerEvents(new EvenMoreFishListener(), this);
            getLogger().info("Hooked into EvenMoreFish.");
        }

        // Hook: EzChestShop
        if (configLoader.isHookEnabled("ezchestshop")) {
            getServer().getPluginManager().registerEvents(new EzChestShopListener(), this);
            getLogger().info("Listening for EzChestShop transactions.");
        }

        // Hook: Vault
        if (configLoader.isHookEnabled("vault")) {
            vaultHook = new VaultHook();
            if (vaultHook.setupEconomy()) {
                getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onJoin(PlayerJoinEvent event) {
                        vaultHook.syncBalance(event.getPlayer());
                    }
                }, this);
                getLogger().info("Vault economy hooked.");
            } else {
                getLogger().warning("Vault found, but no Economy provider was found.");
            }
        }

        // Hook: AdvancedJobs
        if (configLoader.isHookEnabled("advancedjobs")) {
            advancedJobsHook = new AdvancedJobsHook();
            if (advancedJobsHook.isAvailable()) {
                getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onJoin(PlayerJoinEvent event) {
                        advancedJobsHook.syncJobStats(event.getPlayer());
                    }
                }, this);
                getLogger().info("AdvancedJobs hooked.");
            } else {
                getLogger().warning("AdvancedJobs plugin not found or inactive.");
            }
        }

        // Hook: PlotSquared
        if (configLoader.isHookEnabled("plotsquared")) {
            PlotSquaredHook plotHook = new PlotSquaredHook();
            if (plotHook.isAvailable()) {
                getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onJoin(PlayerJoinEvent event) {
                        plotHook.syncPlotStats(event.getPlayer());
                    }
                }, this);
                getLogger().info("PlotSquared hooked.");
            } else {
                getLogger().warning("PlotSquared plugin not found or inactive.");
            }
        }

        // Hook: mcMMO
        if (configLoader.isHookEnabled("mcmmo")) {
            mcMMOHook = new McMMOHook();
            if (mcMMOHook.isAvailable()) {
                getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onJoin(PlayerJoinEvent event) {
                        Player player = event.getPlayer();
                        Bukkit.getScheduler().runTaskLater(TorrentStats.this, () -> {
                            mcMMOHook.syncMcMMOStats(player);
                        }, 100L); // 5 second delay
                    }
                }, this);
                getLogger().info("mcMMO hooked.");
            } else {
                getLogger().warning("mcMMO plugin not found or inactive.");
            }
        }

        // Hook: BeautyQuests
        if (configLoader.isHookEnabled("beautyquests")) {
            BeautyQuestsHook questsHook = new BeautyQuestsHook();
            if (questsHook.isAvailable()) {
                getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onJoin(PlayerJoinEvent event) {
                        questsHook.syncQuestStats(event.getPlayer());
                    }
                }, this);
                getLogger().info("BeautyQuests hooked.");
            } else {
                getLogger().warning("BeautyQuests plugin not found or inactive.");
            }
        }

        // Hook:Teams
        if (configLoader.isHookEnabled("betterteams")) {
            BetterTeamsListener teamsListener = new BetterTeamsListener(this);
            getServer().getPluginManager().registerEvents(teamsListener, this);
        
            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onJoin(PlayerJoinEvent event) {
                    Player player = event.getPlayer();
                    Bukkit.getScheduler().runTaskLater(TorrentStats.this, () -> {
                        teamsListener.syncTeamStats(player);
                    }, 60L); // ~3 second delay to let BetterTeams load
                }
            }, this);
        
            getLogger().info("BetterTeams hooked.");
        }

        if (configLoader.isHookEnabled("bukkitstats")) {
            statisticTracker = new StatisticTracker(this);
            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onJoin(PlayerJoinEvent event) {
                    statisticTracker.syncPlayerStats(event.getPlayer());
                }
            }, this);
            getLogger().info("Bukkit stats tracker enabled.");
        }     

        getLogger().info("TorrentStats enabled.");

        if (getDatabaseManager().isConnected()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                getDatabaseManager().cleanupExpiredTokens();
            }, 0L, 20L * 60 * 30); // Every 30 minutes
        } else {
            getLogger().warning("Skipping token cleanup task — database not connected.");
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("TorrentStats disabled.");
    }

    public static TorrentStats getInstance() {
        return instance;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public String getServerName() {
        return serverName;
    }    

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StatisticTracker getStatisticTracker() {
        return statisticTracker;
    }

    public void setConfigLoader(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}

package com.jerichotorrent.torrentstats.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.jerichotorrent.torrentstats.TorrentStats;

public class DatabaseManager {

    private Connection connection;

    public void initialize() {
        String type = TorrentStats.getInstance().getConfig().getString("database.type", "sqlite");

        try {
            if (type.equalsIgnoreCase("mysql")) {
                String host = TorrentStats.getInstance().getConfig().getString("database.host");
                int port = TorrentStats.getInstance().getConfig().getInt("database.port");
                String database = TorrentStats.getInstance().getConfig().getString("database.name");
                String user = TorrentStats.getInstance().getConfig().getString("database.user");
                String password = TorrentStats.getInstance().getConfig().getString("database.password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
                connection = DriverManager.getConnection(url, user, password);
            } else {
                String path = TorrentStats.getInstance().getDataFolder().getAbsolutePath() + "/stats.db";
                connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            }

            createTable();
            Bukkit.getLogger().info("[TorrentStats] Database connected successfully.");

        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to connect to database: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String playerStats = "CREATE TABLE IF NOT EXISTS player_stats (" +
                    "server VARCHAR(32)," +
                    "uuid VARCHAR(36)," +
                    "username VARCHAR(32)," +
                    "total_xp_bottled INT DEFAULT 0," +
                    "total_elytras INT DEFAULT 0," +
                    "dungeons_completed INT DEFAULT 0," +
                    "quests_completed INT DEFAULT 0," +
                    "balance DOUBLE DEFAULT 0," +
                    "legendary_fish_caught INT DEFAULT 0," +
                    "largest_fish DOUBLE DEFAULT 0," +
                    "plots_owned INT DEFAULT 0," +
                    "plots_merged INT DEFAULT 0," +
                    "mcmmo_power_level INT DEFAULT 0," +
                    "ez_profits INT DEFAULT 0," +
                    "ez_shops INT DEFAULT 0," +
                    "animals_bred INT DEFAULT 0," +
                    "aviate_cm INT DEFAULT 0," +
                    "climb_cm INT DEFAULT 0," +
                    "deaths INT DEFAULT 0," +
                    "fall_cm INT DEFAULT 0," +
                    "fish_caught INT DEFAULT 0," +
                    "fly_cm INT DEFAULT 0," +
                    "jumps INT DEFAULT 0," +
                    "mob_kills INT DEFAULT 0," +
                    "ticks_played INT DEFAULT 0," +
                    "player_kills INT DEFAULT 0," +
                    "raid_wins INT DEFAULT 0," +
                    "beds_slept INT DEFAULT 0," +
                    "swim_cm INT DEFAULT 0," +
                    "villager_trades INT DEFAULT 0," +
                    "walk_cm INT DEFAULT 0," +
                    "items_broken INT DEFAULT 0," +
                    "items_crafted INT DEFAULT 0," +
                    "blocks_mined INT DEFAULT 0," +
                    "players_killed_entity INT DEFAULT 0," + 
                    "PRIMARY KEY (server, uuid)" +                 
                    ")";
            stmt.executeUpdate(playerStats);

            String playerJobs = "CREATE TABLE IF NOT EXISTS player_jobs (" +
                    "server VARCHAR(32)," +
                    "uuid VARCHAR(36)," +
                    "username VARCHAR(32)," +
                    "job_name VARCHAR(64)," +
                    "level INT DEFAULT 0," +
                    "xp DOUBLE DEFAULT 0," +
                    "PRIMARY KEY (server, uuid, job_name)" +
                    ")";
            stmt.executeUpdate(playerJobs);

            String playerSkills = "CREATE TABLE IF NOT EXISTS player_skills (" +
                    "server VARCHAR(32)," +
                    "uuid VARCHAR(36)," +
                    "skill_name VARCHAR(32)," +
                    "level INT DEFAULT 0," +
                    "current_xp DOUBLE DEFAULT 0," +
                    "xp_to_level DOUBLE DEFAULT 0," +
                    "PRIMARY KEY (server, uuid, skill_name)" +
                    ")";
            stmt.executeUpdate(playerSkills);

            String playerTeamStats = "CREATE TABLE IF NOT EXISTS player_team_stats (" +
                    "server VARCHAR(32)," +
                    "uuid VARCHAR(36)," +
                    "username VARCHAR(32)," +
                    "team_name VARCHAR(64)," +
                    "team_level INT," +
                    "team_members INT," +
                    "PRIMARY KEY (server, uuid)" +
                    ")";
            stmt.executeUpdate(playerTeamStats);
        }
    }

    public void updateStatsBatch(UUID uuid, String username, Map<String, Integer> stats) {
        if (connection == null) {
            Bukkit.getLogger().log(Level.WARNING, "[TorrentStats] Skipped stat update for {0} (no DB connection)", uuid);
            return;
        }
        String server = TorrentStats.getInstance().getConfigLoader().getServerName();

        StringBuilder sql = new StringBuilder("INSERT INTO player_stats (server, uuid, username");
        for (String key : stats.keySet()) {
            sql.append(", ").append(key);
        }
        sql.append(") VALUES (?, ?, ?");
        for (int i = 0; i < stats.size(); i++) {
            sql.append(", ?");
        }
        sql.append(") ON DUPLICATE KEY UPDATE username = VALUES(username)");
        for (String key : stats.keySet()) {
            sql.append(", ").append(key).append(" = ").append(key).append(" + VALUES(").append(key).append(")");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setString(1, server);
            ps.setString(2, uuid.toString());
            ps.setString(3, username);

            int index = 4;
            for (Integer value : stats.values()) {
                ps.setInt(index++, value);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed batch update for {0}: {1}", new Object[]{uuid, e.getMessage()});
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Bukkit.getLogger().info("[TorrentStats] Database connection closed.");
            }
        } catch (SQLException ignored) {
        }
    }
    public void updateStat(UUID uuid, String username, String column, int amount) {
        String sql = "INSERT INTO player_stats (server, uuid, username, " + column + ") VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE username = VALUES(username), " +
                     column + " = " + column + " + VALUES(" + column + ")";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, TorrentStats.getInstance().getConfigLoader().getServerName());
            ps.setString(2, uuid.toString());
            ps.setString(3, username);
            ps.setInt(4, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to update stat ''{0}'' for {1}", new Object[]{column, uuid});
        }
    }
    
    public void setDoubleStat(UUID uuid, String username, String column, double value) {
        String sql = "INSERT INTO player_stats (server, uuid, username, " + column + ") VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE username = VALUES(username), " +
                     column + " = VALUES(" + column + ")";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, TorrentStats.getInstance().getConfigLoader().getServerName());
            ps.setString(2, uuid.toString());
            ps.setString(3, username);
            ps.setDouble(4, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to set stat ''{0}'' for {1}", new Object[]{column, uuid});
        }
    }

    public double getDoubleStat(UUID uuid, String column) {
        try {
            String sql = "SELECT " + column + " FROM player_stats WHERE uuid = ? AND server = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, TorrentStats.getInstance().getConfigLoader().getServerName());
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble(column);
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to fetch stat ''{0}'' for {1}", new Object[]{column, uuid});
        }
    
        return 0.0;
    }

    public void updateTeamStats(UUID uuid, String username, String server, String teamName, int level, double balance, int members) {
        String sql = "INSERT INTO player_team_stats (server, uuid, username, team_name, team_level, team_balance, team_members) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE username = VALUES(username), team_name = VALUES(team_name), " +
                     "team_level = VALUES(team_level), team_balance = VALUES(team_balance), team_members = VALUES(team_members)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, server);
            ps.setString(2, uuid.toString());
            ps.setString(3, username);
            ps.setString(4, teamName);
            ps.setInt(5, level);
            ps.setDouble(6, balance);
            ps.setInt(7, members);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to update team stats for {0}", uuid);
        }
    }
    
    public void clearTeamStats(UUID uuid) {
        String sql = "DELETE FROM player_team_stats WHERE uuid = ? AND server = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, TorrentStats.getInstance().getConfigLoader().getServerName());
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to clear team stats for {0}", uuid);
        }
    }

    public void updatePowerLevel(UUID uuid, String username, int powerLevel) {
        updateStat(uuid, username, "mcmmo_power_level", powerLevel);
    }

    public void updateSkillStat(UUID uuid, String skillName, int level, float currentXp, float xpToLevel) {
        String sql = "INSERT INTO player_skills (server, uuid, skill_name, level, current_xp, xp_to_level) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE level = VALUES(level), current_xp = VALUES(current_xp), xp_to_level = VALUES(xp_to_level)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, TorrentStats.getInstance().getConfigLoader().getServerName());
            ps.setString(2, uuid.toString());
            ps.setString(3, skillName.toLowerCase());
            ps.setInt(4, level);
            ps.setFloat(5, currentXp);
            ps.setFloat(6, xpToLevel);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to update skill ''{0}'' for {1}", new Object[]{skillName, uuid});
        }
    }

    public void updateJobStat(UUID uuid, String username, String jobName, int level, double xp) {
        String sql = "INSERT INTO player_jobs (server, uuid, username, job_name, level, xp) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE username = VALUES(username), level = VALUES(level), xp = VALUES(xp)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, TorrentStats.getInstance().getConfigLoader().getServerName());
            ps.setString(2, uuid.toString());
            ps.setString(3, username);
            ps.setString(4, jobName.toLowerCase());
            ps.setInt(5, level);
            ps.setDouble(6, xp);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[TorrentStats] Failed to update job ''{0}'' for {1}", new Object[]{jobName, uuid});
        }
    }

    public void storeLinkToken(UUID uuid, String token) {
        String sql = "INSERT INTO link_tokens (token, uuid, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "[TorrentStats] Failed to store link token: {0}", e.getMessage());
        }
    }
    
    public void cleanupExpiredTokens() {
        String sql = "DELETE FROM link_tokens WHERE created_at < NOW() - INTERVAL 15 MINUTE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int removed = ps.executeUpdate();
            if (removed > 0) {
                Bukkit.getLogger().log(Level.INFO, "[TorrentStats] Cleaned up {0} expired link tokens.", removed);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "[TorrentStats] Failed to clean expired link tokens: {0}", e.getMessage());
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
}
package mindustry.api;

import arc.struct.*;
import mindustry.net.Administration.*;

/**
 * Administration and server control interface.
 */
public interface AdminController {
    
    // === Player Management ===
    
    /** Ban player by UUID */
    void banPlayer(String uuid, String reason);
    
    /** Ban player by IP */
    void banPlayerIP(String ip, String reason);
    
    /** Unban player */
    void unbanPlayer(String uuid);
    
    /** Kick player */
    void kickPlayer(String uuid, String reason);
    
    /** Make player admin */
    void makeAdmin(String uuid);
    
    /** Remove admin status */
    void removeAdmin(String uuid);
    
    /** Check if player is admin */
    boolean isAdmin(String uuid);
    
    /** Get banned players */
    Seq<PlayerInfo> getBannedPlayers();
    
    /** Get admin players */
    Seq<PlayerInfo> getAdminPlayers();
    
    // === Server Settings ===
    
    /** Set player limit */
    void setPlayerLimit(int limit);
    
    /** Get player limit */
    int getPlayerLimit();
    
    /** Set server name */
    void setServerName(String name);
    
    /** Get server name */
    String getServerName();
    
    /** Set server description */
    void setServerDescription(String description);
    
    /** Get server description */
    String getServerDescription();
    
    /** Enable/disable PvP */
    void setPvP(boolean enabled);
    
    /** Check if PvP is enabled */
    boolean isPvPEnabled();
    
    /** Set wave spacing */
    void setWaveSpacing(float seconds);
    
    /** Get wave spacing */
    float getWaveSpacing();
    
    // === Game Control ===
    
    /** Host server on port */
    void hostServer(int port);
    
    /** Stop server */
    void stopServer();
    
    /** Restart server */
    void restartServer();
    
    /** Check if server is running */
    boolean isServerRunning();
    
    /** Get server port */
    int getServerPort();
    
    // === Map Management ===
    
    /** Load map */
    void loadMap(String mapName);
    
    /** Get current map name */
    String getCurrentMap();
    
    /** Get available maps */
    Seq<String> getAvailableMaps();
    
    /** Set map rotation */
    void setMapRotation(Seq<String> maps);
    
    /** Get map rotation */
    Seq<String> getMapRotation();
    
    // === Rule Management ===
    
    /** Add action filter */
    void addActionFilter(ActionFilter filter);
    
    /** Remove action filter */
    void removeActionFilter(ActionFilter filter);
    
    /** Allow/disallow action type */
    void setActionAllowed(ActionType action, boolean allowed);
    
    /** Check if action is allowed */
    boolean isActionAllowed(ActionType action);
    
    // === Logging and Monitoring ===
    
    /** Set log level */
    void setLogLevel(LogLevel level);
    
    /** Get recent log entries */
    Seq<LogEntry> getRecentLogs(int count);
    
    /** Add custom log entry */
    void log(LogLevel level, String message);
    
    /** Enable/disable packet logging */
    void setPacketLogging(boolean enabled);
    
    // === Statistics ===
    
    /** Get server uptime */
    long getUptime();
    
    /** Get total players served */
    int getTotalPlayersServed();
    
    /** Get current player count */
    int getCurrentPlayerCount();
    
    /** Get server performance metrics */
    ServerMetrics getMetrics();
    
    // === Network ===
    
    /** Get connection info for all players */
    Seq<ConnectionInfo> getConnections();
    
    /** Send message to all players */
    void broadcastMessage(String message);
    
    /** Send message to specific player */
    void sendMessage(String uuid, String message);
    
    /** Get network statistics */
    NetworkStats getNetworkStats();
    
    // === Configuration ===
    
    /** Set configuration value */
    void setConfig(String key, Object value);
    
    /** Get configuration value */
    Object getConfig(String key);
    
    /** Save configuration */
    void saveConfig();
    
    /** Reload configuration */
    void reloadConfig();
    
    // === Data Classes ===
    
    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    class LogEntry {
        public LogLevel level;
        public String message;
        public long timestamp;
    }
    
    class ServerMetrics {
        public float averageTPS;
        public long memoryUsed;
        public long memoryTotal;
        public float cpuUsage;
        public int activeThreads;
    }
    
    class ConnectionInfo {
        public String uuid;
        public String ip;
        public String name;
        public long connectTime;
        public float ping;
        public long bytesReceived;
        public long bytesSent;
    }
    
    class NetworkStats {
        public long totalPacketsSent;
        public long totalPacketsReceived;
        public long totalBytesSent;
        public long totalBytesReceived;
        public float averagePing;
    }
}

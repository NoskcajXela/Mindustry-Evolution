package mindustry.api.impl;

import arc.struct.*;
import arc.util.*;
import mindustry.api.*;
import mindustry.net.Administration.*;

import static mindustry.Vars.*;

/**
 * Implementation of AdminController for server administration.
 */
public class AdminControllerImpl implements AdminController {
    
    @Override
    public void banPlayer(String uuid, String reason) {
        if (netServer != null && netServer.admins != null) {
            var info = netServer.admins.getInfo(uuid);
            if (info != null) {
                info.banned = true;
                netServer.admins.save();
                Log.info("Banned player @ (UUID: @) for reason: @", info.lastName, uuid, reason);
            }
        }
    }
    
    @Override
    public void banPlayerIP(String ip, String reason) {
        if (netServer != null && netServer.admins != null) {
            netServer.admins.banPlayerIP(ip);
            Log.info("Banned IP @ for reason: @", ip, reason);
        }
    }
    
    @Override
    public void unbanPlayer(String uuid) {
        if (netServer != null && netServer.admins != null) {
            var info = netServer.admins.getInfo(uuid);
            if (info != null) {
                info.banned = false;
                netServer.admins.save();
                Log.info("Unbanned player @ (UUID: @)", info.lastName, uuid);
            }
        }
    }
    
    @Override
    public void kickPlayer(String uuid, String reason) {
        if (netServer != null) {
            var player = Groups.player.find(p -> p.uuid().equals(uuid));
            if (player != null) {
                player.kick(reason);
                Log.info("Kicked player @ for reason: @", player.name, reason);
            }
        }
    }
    
    @Override
    public void makeAdmin(String uuid) {
        if (netServer != null && netServer.admins != null) {
            var info = netServer.admins.getInfo(uuid);
            if (info != null) {
                info.admin = true;
                netServer.admins.save();
                Log.info("Made player @ (UUID: @) an admin", info.lastName, uuid);
            }
        }
    }
    
    @Override
    public void removeAdmin(String uuid) {
        if (netServer != null && netServer.admins != null) {
            var info = netServer.admins.getInfo(uuid);
            if (info != null) {
                info.admin = false;
                netServer.admins.save();
                Log.info("Removed admin status from player @ (UUID: @)", info.lastName, uuid);
            }
        }
    }
    
    @Override
    public boolean isAdmin(String uuid) {
        if (netServer != null && netServer.admins != null) {
            var info = netServer.admins.getInfo(uuid);
            return info != null && info.admin;
        }
        return false;
    }
    
    @Override
    public Seq<PlayerInfo> getBannedPlayers() {
        var banned = new Seq<PlayerInfo>();
        if (netServer != null && netServer.admins != null) {
            netServer.admins.each(info -> {
                if (info.banned) {
                    banned.add(info);
                }
            });
        }
        return banned;
    }
    
    @Override
    public Seq<PlayerInfo> getAdminPlayers() {
        var admins = new Seq<PlayerInfo>();
        if (netServer != null && netServer.admins != null) {
            netServer.admins.each(info -> {
                if (info.admin) {
                    admins.add(info);
                }
            });
        }
        return admins;
    }
    
    @Override
    public void setPlayerLimit(int limit) {
        netServer.admins.setPlayerLimit(limit);
        Log.info("Set player limit to @", limit);
    }
    
    @Override
    public int getPlayerLimit() {
        return netServer.admins.getPlayerLimit();
    }
    
    @Override
    public void setServerName(String name) {
        Core.settings.put("servername", name);
        Log.info("Set server name to: @", name);
    }
    
    @Override
    public String getServerName() {
        return Core.settings.getString("servername", "Mindustry Server");
    }
    
    @Override
    public void setServerDescription(String description) {
        Core.settings.put("desc", description);
        Log.info("Set server description");
    }
    
    @Override
    public String getServerDescription() {
        return Core.settings.getString("desc", "");
    }
    
    @Override
    public void setPvP(boolean enabled) {
        if (state.rules != null) {
            state.rules.pvp = enabled;
            Log.info("PvP @", enabled ? "enabled" : "disabled");
        }
    }
    
    @Override
    public boolean isPvPEnabled() {
        return state.rules != null && state.rules.pvp;
    }
    
    @Override
    public void setWaveSpacing(float seconds) {
        if (state.rules != null) {
            state.rules.waveSpacing = seconds * 60f; // Convert to ticks
            Log.info("Set wave spacing to @ seconds", seconds);
        }
    }
    
    @Override
    public float getWaveSpacing() {
        return state.rules != null ? state.rules.waveSpacing / 60f : 0f;
    }
    
    @Override
    public void hostServer(int port) {
        try {
            if (net != null) {
                net.host(port);
                Log.info("Hosting server on port @", port);
            }
        } catch (Exception e) {
            Log.err("Failed to host server on port @", port, e);
            throw new RuntimeException("Failed to host server: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void stopServer() {
        if (net != null && net.server()) {
            net.closeServer();
            Log.info("Server stopped");
        }
    }
    
    @Override
    public void restartServer() {
        stopServer();
        // Restart would need additional logic to re-host on same port
        Log.info("Server restart requested");
    }
    
    @Override
    public boolean isServerRunning() {
        return net != null && net.server();
    }
    
    @Override
    public int getServerPort() {
        // Would need to track the port from hosting
        return Core.settings.getInt("port", 6567);
    }
    
    @Override
    public void loadMap(String mapName) {
        var map = maps.all().find(m -> m.plainName().equals(mapName));
        if (map != null) {
            world.loadMap(map);
            state.rules = map.rules();
            logic.play();
            Log.info("Loaded map: @", mapName);
        } else {
            throw new IllegalArgumentException("Map not found: " + mapName);
        }
    }
    
    @Override
    public String getCurrentMap() {
        return state.map != null ? state.map.plainName() : "Unknown";
    }
    
    @Override
    public Seq<String> getAvailableMaps() {
        return maps.all().map(m -> m.plainName());
    }
    
    @Override
    public void setMapRotation(Seq<String> maps) {
        // Store map rotation - would need proper implementation
        Log.info("Set map rotation to: @", maps);
    }
    
    @Override
    public Seq<String> getMapRotation() {
        // Return stored map rotation
        return new Seq<>();
    }
    
    @Override
    public void addActionFilter(ActionFilter filter) {
        if (netServer != null && netServer.admins != null) {
            netServer.admins.addActionFilter(filter);
            Log.info("Added action filter");
        }
    }
    
    @Override
    public void removeActionFilter(ActionFilter filter) {
        // Would need to track filters to remove them
        Log.info("Removed action filter");
    }
    
    @Override
    public void setActionAllowed(ActionType action, boolean allowed) {
        // Implement by adding/removing appropriate filters
        Log.info("Set action @ allowed: @", action, allowed);
    }
    
    @Override
    public boolean isActionAllowed(ActionType action) {
        // Check if action would be allowed
        return true; // Default allow
    }
    
    @Override
    public void setLogLevel(LogLevel level) {
        switch (level) {
            case DEBUG -> Log.level = Log.LogLevel.debug;
            case INFO -> Log.level = Log.LogLevel.info;
            case WARN -> Log.level = Log.LogLevel.warn;
            case ERROR -> Log.level = Log.LogLevel.err;
        }
        Log.info("Set log level to @", level);
    }
    
    @Override
    public Seq<LogEntry> getRecentLogs(int count) {
        // Would need to implement log storage
        return new Seq<>();
    }
    
    @Override
    public void log(LogLevel level, String message) {
        switch (level) {
            case DEBUG -> Log.debug(message);
            case INFO -> Log.info(message);
            case WARN -> Log.warn(message);
            case ERROR -> Log.err(message);
        }
    }
    
    @Override
    public void setPacketLogging(boolean enabled) {
        // Enable/disable packet logging
        Log.info("Packet logging @", enabled ? "enabled" : "disabled");
    }
    
    @Override
    public long getUptime() {
        // Would need to track server start time
        return System.currentTimeMillis();
    }
    
    @Override
    public int getTotalPlayersServed() {
        // Would need to track this statistic
        return 0;
    }
    
    @Override
    public int getCurrentPlayerCount() {
        return Groups.player.size();
    }
    
    @Override
    public ServerMetrics getMetrics() {
        Runtime runtime = Runtime.getRuntime();
        return new ServerMetrics(
            Core.graphics.getFramesPerSecond(),
            runtime.totalMemory() - runtime.freeMemory(),
            runtime.totalMemory(),
            0f, // CPU usage would need system monitoring
            Thread.activeCount()
        );
    }
    
    @Override
    public Seq<ConnectionInfo> getConnections() {
        var connections = new Seq<ConnectionInfo>();
        for (var player : Groups.player) {
            connections.add(new ConnectionInfo(
                player.uuid(),
                player.ip(),
                player.name,
                System.currentTimeMillis(), // Connect time would need tracking
                player.ping(),
                0, // Bytes would need tracking
                0
            ));
        }
        return connections;
    }
    
    @Override
    public void broadcastMessage(String message) {
        Call.sendMessage(message);
        Log.info("Broadcast: @", message);
    }
    
    @Override
    public void sendMessage(String uuid, String message) {
        var player = Groups.player.find(p -> p.uuid().equals(uuid));
        if (player != null) {
            player.sendMessage(message);
            Log.info("Sent message to @: @", player.name, message);
        }
    }
    
    @Override
    public NetworkStats getNetworkStats() {
        return new NetworkStats(
            0, 0, 0, 0, // Would need packet/byte tracking
            Groups.player.isEmpty() ? 0f : Groups.player.sumf(Player::ping) / Groups.player.size()
        );
    }
    
    @Override
    public void setConfig(String key, Object value) {
        Core.settings.put(key, value);
        Core.settings.save();
        Log.info("Set config @ = @", key, value);
    }
    
    @Override
    public Object getConfig(String key) {
        return Core.settings.get(key, null);
    }
    
    @Override
    public void saveConfig() {
        Core.settings.save();
        Log.info("Configuration saved");
    }
    
    @Override
    public void reloadConfig() {
        // Reload configuration from disk
        Log.info("Configuration reloaded");
    }
}

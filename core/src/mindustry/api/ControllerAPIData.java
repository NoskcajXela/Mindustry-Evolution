package mindustry.api;

import arc.struct.*;
import mindustry.type.*;

/**
 * Additional data classes and utilities for the Controller API.
 */
public class ControllerAPIData {
    
    // === Event System ===
    
    public static class GameEvent {
        public final String type;
        public final Object data;
        public final long timestamp;
        
        public GameEvent(String type, Object data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public static class PlayerEvent extends GameEvent {
        public final PlayerController player;
        
        public PlayerEvent(String type, PlayerController player, Object data) {
            super(type, data);
            this.player = player;
        }
    }
    
    // === Configuration ===
    
    public static class ControllerConfig {
        /** Enable detailed logging */
        public boolean enableLogging = true;
        
        /** Update rate in milliseconds */
        public int updateRate = 16; // ~60 FPS
        
        /** Maximum number of AI players */
        public int maxAIPlayers = 32;
        
        /** Enable automatic garbage collection */
        public boolean autoGC = true;
        
        /** GC interval in seconds */
        public float gcInterval = 30f;
        
        /** Enable performance monitoring */
        public boolean enableProfiling = false;
        
        /** Network timeout in seconds */
        public float networkTimeout = 30f;
        
        /** Default team for new AI players */
        public Team defaultTeam = Team.sharded;
        
        /** Enable event system */
        public boolean enableEvents = true;
    }
    
    // === Statistics and Monitoring ===
    
    public static class DetailedServerStats {
        public final int playerCount;
        public final int unitCount;
        public final int buildingCount;
        public final int wave;
        public final int enemies;
        public final float tps; // Ticks per second
        public final long memoryUsed;
        public final long memoryTotal;
        public final float memoryUsage;
        public final int activeThreads;
        public final long uptime;
        public final Seq<TeamInfo> teamStats;
        
        public DetailedServerStats(int playerCount, int unitCount, int buildingCount, 
                                 int wave, int enemies, float tps, long memoryUsed, 
                                 long memoryTotal, int activeThreads, long uptime,
                                 Seq<TeamInfo> teamStats) {
            this.playerCount = playerCount;
            this.unitCount = unitCount;
            this.buildingCount = buildingCount;
            this.wave = wave;
            this.enemies = enemies;
            this.tps = tps;
            this.memoryUsed = memoryUsed;
            this.memoryTotal = memoryTotal;
            this.memoryUsage = memoryUsed / (float) memoryTotal;
            this.activeThreads = activeThreads;
            this.uptime = uptime;
            this.teamStats = teamStats;
        }
    }
    
    public static class TeamInfo {
        public final Team team;
        public final int unitCount;
        public final int buildingCount;
        public final int coreCount;
        public final boolean isAlive;
        public final Seq<ItemStack> resources;
        
        public TeamInfo(Team team, int unitCount, int buildingCount, int coreCount, 
                       boolean isAlive, Seq<ItemStack> resources) {
            this.team = team;
            this.unitCount = unitCount;
            this.buildingCount = buildingCount;
            this.coreCount = coreCount;
            this.isAlive = isAlive;
            this.resources = resources;
        }
    }
    
    // === AI Behavior System ===
    
    public interface AIBehavior {
        /** Update AI behavior */
        void update(PlayerController player, float delta);
        
        /** Get behavior name */
        String getName();
        
        /** Check if behavior is applicable */
        boolean canApply(PlayerController player);
    }
    
    public static class BasicBuilderAI implements AIBehavior {
        private final Block[] buildQueue;
        private int buildIndex = 0;
        
        public BasicBuilderAI(Block... blocks) {
            this.buildQueue = blocks;
        }
        
        @Override
        public void update(PlayerController player, float delta) {
            if (buildIndex < buildQueue.length && player.getBuildPlans().isEmpty()) {
                var block = buildQueue[buildIndex];
                // Find a suitable location and place the block
                var pos = findBuildLocation(player, block);
                if (pos != null) {
                    player.placeBlock(block, pos.x, pos.y, 0);
                    buildIndex++;
                }
            }
        }
        
        private Point2 findBuildLocation(PlayerController player, Block block) {
            // Simple spiral search around player position
            var playerPos = player.getPosition();
            int centerX = (int) (playerPos.x / 8);
            int centerY = (int) (playerPos.y / 8);
            
            for (int radius = 1; radius <= 20; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;
                        
                        int x = centerX + dx;
                        int y = centerY + dy;
                        
                        if (player.canPerformAction(mindustry.net.Administration.ActionType.placeBlock, x, y)) {
                            return new Point2(x, y);
                        }
                    }
                }
            }
            return null;
        }
        
        @Override
        public String getName() {
            return "BasicBuilder";
        }
        
        @Override
        public boolean canApply(PlayerController player) {
            return player.isAlive() && player.getUnit() != null;
        }
    }
    
    public static class DefensiveAI implements AIBehavior {
        @Override
        public void update(PlayerController player, float delta) {
            if (!player.isAlive()) return;
            
            // Find nearest enemy unit
            var playerPos = player.getPosition();
            // This would need proper implementation to find enemies
            
            // For now, just demonstrate the concept
            player.setShooting(false); // Default to not shooting
        }
        
        @Override
        public String getName() {
            return "Defensive";
        }
        
        @Override
        public boolean canApply(PlayerController player) {
            return player.isAlive();
        }
    }
    
    // === Utility Classes ===
    
    public static class Point2 {
        public final int x, y;
        
        public Point2(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    public static class ItemStack {
        public final Item item;
        public final int amount;
        
        public ItemStack(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }
        
        public static final ItemStack empty = new ItemStack(null, 0);
        
        @Override
        public String toString() {
            return amount + "x " + (item != null ? item.name : "empty");
        }
    }
    
    // === Command System ===
    
    public interface ControllerCommand {
        /** Execute the command */
        CommandResult execute(String[] args, ControllerAPI api);
        
        /** Get command name */
        String getName();
        
        /** Get command description */
        String getDescription();
        
        /** Get usage information */
        String getUsage();
    }
    
    public static class StartGameCommand implements ControllerCommand {
        @Override
        public CommandResult execute(String[] args, ControllerAPI api) {
            try {
                if (args.length < 1) {
                    return new CommandResult(false, "Usage: " + getUsage());
                }
                
                String mapName = args[0];
                var maps = api.getAvailableMaps();
                var map = maps.find(m -> m.plainName().equals(mapName));
                
                if (map == null) {
                    return new CommandResult(false, "Map not found: " + mapName);
                }
                
                var gameController = api.startGame(map, new mindustry.game.Rules());
                return new CommandResult(true, "Started game with map: " + mapName);
                
            } catch (Exception e) {
                return new CommandResult(false, "Failed to start game: " + e.getMessage());
            }
        }
        
        @Override
        public String getName() {
            return "startgame";
        }
        
        @Override
        public String getDescription() {
            return "Start a new game with the specified map";
        }
        
        @Override
        public String getUsage() {
            return "startgame <mapname>";
        }
    }
    
    public static class CreatePlayerCommand implements ControllerCommand {
        @Override
        public CommandResult execute(String[] args, ControllerAPI api) {
            try {
                if (args.length < 2) {
                    return new CommandResult(false, "Usage: " + getUsage());
                }
                
                String name = args[0];
                Team team = Team.valueOf(args[1]);
                
                var player = api.createPlayer(name, team);
                return new CommandResult(true, "Created player: " + name + " on team " + team.name);
                
            } catch (Exception e) {
                return new CommandResult(false, "Failed to create player: " + e.getMessage());
            }
        }
        
        @Override
        public String getName() {
            return "createplayer";
        }
        
        @Override
        public String getDescription() {
            return "Create a new AI player";
        }
        
        @Override
        public String getUsage() {
            return "createplayer <name> <team>";
        }
    }
    
    public static class CommandResult {
        public final boolean success;
        public final String message;
        
        public CommandResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        @Override
        public String toString() {
            return (success ? "[SUCCESS] " : "[ERROR] ") + message;
        }
    }
}

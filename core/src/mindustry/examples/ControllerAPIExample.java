package mindustry.examples;

import arc.backend.headless.*;
import arc.struct.*;
import arc.util.*;
import mindustry.api.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;

/**
 * Comprehensive example demonstrating the Controller API capabilities.
 */
public class ControllerAPIExample {
    
    private ControllerAPI api;
    private boolean running = true;
    
    public static void main(String[] args) {
        new ControllerAPIExample().run();
    }
    
    public void run() {
        Log.info("Starting Controller API Example...");
        
        try {
            // 1. Initialize headless controller
            initializeController();
            
            // 2. Run various examples
            runBasicExample();
            runAIPlayerExample();
            runWorldManipulationExample();
            runMonitoringExample();
            
            Log.info("All examples completed successfully!");
            
        } catch (Exception e) {
            Log.err("Example failed", e);
        } finally {
            cleanup();
        }
    }
    
    private void initializeController() {
        Log.info("=== Initializing Controller ===");
        
        // Start headless launcher
        var launcher = HeadlessControllerLauncher.create();
        new HeadlessApplication(launcher);
        
        // Wait for initialization
        launcher.waitForInitialization();
        
        // Get API
        api = launcher.getControllerAPI();
        
        Log.info("Controller API initialized, version: @", api.getVersion());
    }
    
    private void runBasicExample() {
        Log.info("=== Basic Game Control Example ===");
        
        // Get available maps
        var maps = api.getAvailableMaps();
        Log.info("Available maps: @", maps.size);
        
        if (maps.isEmpty()) {
            Log.warn("No maps available, skipping game start");
            return;
        }
        
        // Start a game
        var map = maps.first();
        var rules = new Rules();
        rules.waveSpacing = 30f; // 30 seconds between waves
        rules.infiniteResources = true; // Unlimited resources
        
        var game = api.startGame(map, rules);
        Log.info("Started game with map: @", map.plainName());
        
        // Get game state
        var state = api.getGameState();
        Log.info("Game state - Wave: @, Paused: @", state.wave, state.isPaused);
        
        // Control game flow
        game.setWaveTime(10f); // Set wave countdown to 10 seconds
        Log.info("Set wave countdown to 10 seconds");
        
        // Wait a bit, then advance wave
        Timer.schedule(() -> {
            game.nextWave();
            Log.info("Advanced to next wave");
        }, 2f);
    }
    
    private void runAIPlayerExample() {
        Log.info("=== AI Player Example ===");
        
        // Create AI players on different teams
        var builder = api.createPlayer("AI-Builder", Team.sharded);
        var miner = api.createPlayer("AI-Miner", Team.sharded);
        var fighter = api.createPlayer("AI-Fighter", Team.crux);
        
        Log.info("Created 3 AI players");
        
        // Set up builder behavior
        setupBuilderAI(builder);
        
        // Set up miner behavior  
        setupMinerAI(miner);
        
        // Set up fighter behavior
        setupFighterAI(fighter);
        
        // Monitor players for a bit
        Timer.schedule(() -> {
            var players = api.getPlayers();
            Log.info("Active players: @", players.size);
            
            for (var player : players) {
                var stats = player.getStats();
                Log.info("  @ - Team: @, Alive: @, Position: @", 
                        stats.name, stats.team.name, stats.alive, stats.position);
            }
        }, 0f, 5f); // Every 5 seconds
    }
    
    private void setupBuilderAI(PlayerController player) {
        Log.info("Setting up builder AI for @", player.getName());
        
        // Builder focuses on infrastructure
        Timer.schedule(() -> {
            if (!player.isAlive()) return;
            
            // Build core if we don't have one
            var pos = player.getPosition();
            int x = (int)(pos.x / 8);
            int y = (int)(pos.y / 8);
            
            // Try to build various structures
            if (player.getBuildPlans().isEmpty()) {
                // Build in a pattern around starting position
                var buildQueue = Seq.with(
                    Blocks.coreNucleus,
                    Blocks.mechanicalDrill,
                    Blocks.conveyor,
                    Blocks.siliconSmelter,
                    Blocks.duo
                );
                
                for (int i = 0; i < buildQueue.size; i++) {
                    var block = buildQueue.get(i);
                    int buildX = x + (i % 3) * 3;
                    int buildY = y + (i / 3) * 3;
                    
                    if (player.placeBlock(block, buildX, buildY, 0)) {
                        Log.info("@ queued @ at @,@", player.getName(), block.name, buildX, buildY);
                        break;
                    }
                }
            }
        }, 2f, 3f); // Start after 2s, repeat every 3s
    }
    
    private void setupMinerAI(PlayerController player) {
        Log.info("Setting up miner AI for @", player.getName());
        
        // Miner focuses on resource collection
        Timer.schedule(() -> {
            if (!player.isAlive() || player.isMining()) return;
            
            // Find copper ore nearby
            var world = api.getWorld();
            var pos = player.getPosition();
            
            // Simple mining behavior - mine nearby tiles
            for (int radius = 1; radius <= 10; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;
                        
                        int x = (int)(pos.x / 8) + dx;
                        int y = (int)(pos.y / 8) + dy;
                        
                        var tile = world.getTile(x, y);
                        if (tile != null && tile.drop() == Items.copper) {
                            player.startMining(x, y);
                            Log.info("@ started mining at @,@", player.getName(), x, y);
                            return;
                        }
                    }
                }
            }
        }, 1f, 5f); // Start after 1s, repeat every 5s
    }
    
    private void setupFighterAI(PlayerController player) {
        Log.info("Setting up fighter AI for @", player.getName());
        
        // Fighter focuses on combat
        Timer.schedule(() -> {
            if (!player.isAlive()) return;
            
            // Simple combat behavior - patrol and attack
            var world = api.getWorld();
            var enemyUnits = world.getTeamUnits(Team.sharded); // Attack sharded team
            
            if (!enemyUnits.isEmpty()) {
                var target = enemyUnits.first();
                player.attackUnit(target);
                Log.info("@ attacking enemy unit at @,@", player.getName(), target.x, target.y);
            } else {
                // Patrol behavior - move randomly
                var pos = player.getPosition();
                float newX = pos.x + (float)(Math.random() - 0.5) * 200;
                float newY = pos.y + (float)(Math.random() - 0.5) * 200;
                player.moveTo(newX, newY);
            }
        }, 3f, 4f); // Start after 3s, repeat every 4s
    }
    
    private void runWorldManipulationExample() {
        Log.info("=== World Manipulation Example ===");
        
        var world = api.getWorld();
        
        // Get world info
        Log.info("World size: @x@", world.getWidth(), world.getHeight());
        Log.info("World name: @", world.getName());
        
        // Create a small base structure
        int centerX = world.getWidth() / 2;
        int centerY = world.getHeight() / 2;
        
        // Clear area and build a platform
        for (int x = centerX - 5; x <= centerX + 5; x++) {
            for (int y = centerY - 5; y <= centerY + 5; y++) {
                world.setFloor(x, y, Blocks.metalFloor);
            }
        }
        
        // Build defensive structures
        world.setBlock(centerX, centerY, Blocks.coreNucleus, Team.sharded, 0);
        world.setBlock(centerX - 3, centerY, Blocks.duo, Team.sharded, 0);
        world.setBlock(centerX + 3, centerY, Blocks.duo, Team.sharded, 0);
        world.setBlock(centerX, centerY - 3, Blocks.duo, Team.sharded, 0);
        world.setBlock(centerX, centerY + 3, Blocks.duo, Team.sharded, 0);
        
        Log.info("Built defensive base at @,@", centerX, centerY);
        
        // Analyze the area
        var analysis = world.analyzeArea(centerX - 10, centerY - 10, 20, 20);
        Log.info("Area analysis - Buildable: @, Resources: @", 
                analysis.buildable, analysis.resources.size);
    }
    
    private void runMonitoringExample() {
        Log.info("=== Monitoring Example ===");
        
        // Set up event listeners
        api.addEventListener("wave.start", (type, data) -> {
            Log.info("Wave started: @", data);
        });
        
        api.addEventListener("unit.death", (type, data) -> {
            Log.info("Unit died: @", data);
        });
        
        // Monitor server stats
        Timer.schedule(() -> {
            var stats = api.getServerStats();
            var gameState = api.getGameState();
            
            Log.info("=== Server Stats ===");
            Log.info("Players: @ | Wave: @ | Enemies: @", 
                    stats.playerCount, gameState.wave, gameState.enemies);
            Log.info("Memory: @MB/@MB (@%)", 
                    stats.memoryUsed / 1024 / 1024,
                    stats.memoryTotal / 1024 / 1024,
                    (int)(stats.memoryUsed * 100.0 / stats.memoryTotal));
            Log.info("FPS: @", stats.fps);
            
            // Check for game over
            if (gameState.isGameOver) {
                Log.info("Game Over! Stopping monitoring...");
                running = false;
            }
        }, 5f, 10f); // Start after 5s, repeat every 10s
        
        // Run monitoring for a limited time
        Timer.schedule(() -> {
            Log.info("Monitoring example completed");
            running = false;
        }, 60f); // Stop after 60 seconds
    }
    
    private void cleanup() {
        Log.info("=== Cleanup ===");
        
        if (api != null) {
            // Remove all AI players
            var players = api.getPlayers();
            for (var player : players) {
                api.removePlayer(player.getId());
                Log.info("Removed AI player: @", player.getName());
            }
            
            // Shutdown API
            api.shutdown();
        }
        
        Log.info("Cleanup completed");
    }
}

/**
 * Simple AI behavior implementation example.
 */
class SimpleAIBehavior {
    
    public static void applyBuilderBehavior(PlayerController player) {
        // Build essential structures in order
        var buildOrder = Seq.with(
            Blocks.coreNucleus,
            Blocks.mechanicalDrill, 
            Blocks.conveyor,
            Blocks.siliconSmelter,
            Blocks.duo,
            Blocks.scatter
        );
        
        Timer.schedule(new Timer.Task() {
            int buildIndex = 0;
            
            @Override
            public void run() {
                if (!player.isAlive() || buildIndex >= buildOrder.size) {
                    cancel();
                    return;
                }
                
                if (player.getBuildPlans().isEmpty()) {
                    var block = buildOrder.get(buildIndex);
                    var pos = findBuildLocation(player, block);
                    
                    if (pos != null) {
                        player.placeBlock(block, pos.x, pos.y, 0);
                        buildIndex++;
                    }
                }
            }
        }, 1f, 2f);
    }
    
    public static void applyMinerBehavior(PlayerController player) {
        Timer.schedule(() -> {
            if (!player.isAlive() || player.isMining()) return;
            
            // Find nearest ore
            var pos = player.getPosition();
            // Implementation would find nearest mineable tile
            
        }, 2f, 3f);
    }
    
    private static Point2 findBuildLocation(PlayerController player, Block block) {
        var pos = player.getPosition();
        int centerX = (int)(pos.x / 8);
        int centerY = (int)(pos.y / 8);
        
        // Simple spiral search
        for (int radius = 1; radius <= 15; radius++) {
            for (int angle = 0; angle < 360; angle += 45) {
                int x = centerX + (int)(Math.cos(Math.toRadians(angle)) * radius);
                int y = centerY + (int)(Math.sin(Math.toRadians(angle)) * radius);
                
                if (player.canPerformAction(mindustry.net.Administration.ActionType.placeBlock, x, y)) {
                    return new Point2(x, y);
                }
            }
        }
        
        return null;
    }
    
    static class Point2 {
        final int x, y;
        Point2(int x, int y) { this.x = x; this.y = y; }
    }
}

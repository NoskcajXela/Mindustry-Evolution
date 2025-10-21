# Controller API Usage Guide

This guide provides practical examples of how to use the Mindustry Controller API for different scenarios.

## Quick Start

### 1. Basic Setup

```java
import mindustry.api.ControllerAPI;
import mindustry.api.impl.ControllerAPIImpl;
import mindustry.api.ControllerAPIData.*;

// Create API instance
ControllerAPI api = new ControllerAPIImpl();

// Configure for headless operation
APIConfig config = new APIConfig();
config.enableHeadless = true;
config.maxAIPlayers = 8;
api.initialize(config);
```

### 2. Server Integration

```java
// Start with API enabled
java -jar server.jar --enable-api

// Or programmatically
ControllerServerLauncher launcher = new ControllerServerLauncher();
launcher.main(new String[]{"--enable-api"});
```

## Common Use Cases

### 1. Automated Testing

```java
public class GameplayTest {
    private ControllerAPI api;
    
    public void setUp() {
        api = new ControllerAPIImpl();
        APIConfig config = new APIConfig();
        config.enableHeadless = true;
        api.initialize(config);
    }
    
    public void testBasicGameplay() {
        // Start a game
        api.getGameController().startGame("Ancient Caldera", "survival");
        
        // Create AI players
        PlayerInfo player1 = new PlayerInfo();
        player1.name = "TestBot1";
        player1.team = Team.sharded;
        player1.isAI = true;
        
        api.getPlayerController().createAIPlayer(player1);
        
        // Wait for some waves
        waitForWaves(10);
        
        // Check game state
        GameInfo gameInfo = api.getGameController().getGameInfo();
        assert gameInfo.waveNumber >= 10;
    }
    
    private void waitForWaves(int targetWave) {
        while (api.getGameController().getGameInfo().waveNumber < targetWave) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
        }
    }
}
```

### 2. AI Training Environment

```java
public class AITrainingEnvironment {
    private ControllerAPI api;
    
    public void createTrainingScenario() {
        // Set up controlled environment
        api.getGameController().startGame("Training Map", "sandbox");
        
        // Create AI with specific behavior
        AIBehaviorConfig aiConfig = new AIBehaviorConfig();
        AIBehavior builderBehavior = new AIBehavior();
        builderBehavior.type = "builder";
        builderBehavior.priority = 10;
        builderBehavior.enabled = true;
        aiConfig.behaviors.add(builderBehavior);
        
        PlayerInfo aiPlayer = new PlayerInfo();
        aiPlayer.name = "TrainingBot";
        aiPlayer.team = Team.sharded;
        aiPlayer.isAI = true;
        aiPlayer.aiConfig = aiConfig;
        
        api.getPlayerController().createAIPlayer(aiPlayer);
        
        // Monitor performance
        monitorAIPerformance();
    }
    
    private void monitorAIPerformance() {
        api.addEventListener("building.placed", (event) -> {
            Log.info("AI placed building: " + event);
        });
        
        api.addEventListener("unit.spawned", (event) -> {
            Log.info("AI spawned unit: " + event);
        });
    }
}
```

### 3. Server Management

```java
public class ServerManager {
    private ControllerAPI api;
    
    public void manageServer() {
        // Get server status
        ServerInfo serverInfo = api.getAdminController().getServerInfo();
        Log.info("Server: " + serverInfo.name + " - " + 
                 serverInfo.playerCount + "/" + serverInfo.maxPlayers);
        
        // Monitor for events
        api.addEventListener("player.join", this::onPlayerJoin);
        api.addEventListener("player.leave", this::onPlayerLeave);
        api.addEventListener("game.over", this::onGameOver);
        
        // Auto-restart games
        setupAutoRestart();
    }
    
    private void onPlayerJoin(Object event) {
        Log.info("Player joined: " + event);
        
        // Auto-balance teams if needed
        if (needsTeamBalance()) {
            balanceTeams();
        }
    }
    
    private void onPlayerLeave(Object event) {
        Log.info("Player left: " + event);
        
        // Add AI to maintain player count
        if (shouldAddAI()) {
            addBalancingAI();
        }
    }
    
    private void onGameOver(Object event) {
        Log.info("Game ended: " + event);
        
        // Start new game after delay
        Timer.schedule(() -> {
            api.getGameController().startGame("Random Map", "survival");
        }, 30f); // 30 second delay
    }
}
```

### 4. World Building

```java
public class WorldBuilder {
    private ControllerAPI api;
    
    public void buildBase() {
        WorldController world = api.getWorldController();
        
        // Place core
        world.placeBuilding(Blocks.coreShard, 50, 50, Team.sharded);
        
        // Build resource production
        buildResourceProduction(world);
        
        // Build defenses
        buildDefenses(world);
        
        // Connect with conveyors
        connectWithConveyors(world);
    }
    
    private void buildResourceProduction(WorldController world) {
        // Mechanical drill
        world.placeBuilding(Blocks.mechanicalDrill, 45, 45, Team.sharded);
        
        // Copper walls around drill
        for (int x = 44; x <= 46; x++) {
            for (int y = 44; y <= 46; y++) {
                if (x == 45 && y == 45) continue; // Skip drill position
                world.placeBuilding(Blocks.copperWall, x, y, Team.sharded);
            }
        }
    }
    
    private void buildDefenses(WorldController world) {
        // Turrets in defensive positions
        world.placeBuilding(Blocks.duo, 40, 50, Team.sharded);
        world.placeBuilding(Blocks.duo, 60, 50, Team.sharded);
        world.placeBuilding(Blocks.scatter, 50, 40, Team.sharded);
        world.placeBuilding(Blocks.scatter, 50, 60, Team.sharded);
    }
    
    private void connectWithConveyors(WorldController world) {
        // Horizontal conveyor line
        for (int x = 46; x <= 49; x++) {
            world.placeBuilding(Blocks.conveyor, x, 45, Team.sharded);
        }
        
        // Vertical conveyor line
        for (int y = 46; y <= 49; y++) {
            world.placeBuilding(Blocks.conveyor, 49, y, Team.sharded);
        }
    }
}
```

### 5. Event-Driven Automation

```java
public class GameAutomation {
    private ControllerAPI api;
    private int lastWave = 0;
    
    public void setupAutomation() {
        // React to wave events
        api.addEventListener("wave.start", this::onWaveStart);
        api.addEventListener("wave.end", this::onWaveEnd);
        api.addEventListener("building.destroyed", this::onBuildingDestroyed);
        api.addEventListener("unit.death", this::onUnitDeath);
        
        // Periodic tasks
        Timer.schedule(this::performMaintenance, 0f, 60f); // Every minute
    }
    
    private void onWaveStart(Object event) {
        GameInfo gameInfo = api.getGameController().getGameInfo();
        Log.info("Wave " + gameInfo.waveNumber + " starting!");
        
        // Increase AI aggressiveness during waves
        for (PlayerInfo player : api.getPlayerController().getAIPlayers()) {
            AIBehaviorConfig config = player.aiConfig;
            if (config != null) {
                config.aggressiveness = Math.min(1.0f, config.aggressiveness + 0.1f);
                api.getPlayerController().updateAIBehavior(player.id, config);
            }
        }
    }
    
    private void onWaveEnd(Object event) {
        GameInfo gameInfo = api.getGameController().getGameInfo();
        Log.info("Wave " + gameInfo.waveNumber + " completed!");
        
        // Reset AI aggressiveness
        for (PlayerInfo player : api.getPlayerController().getAIPlayers()) {
            AIBehaviorConfig config = player.aiConfig;
            if (config != null) {
                config.aggressiveness = 0.5f; // Reset to default
                api.getPlayerController().updateAIBehavior(player.id, config);
            }
        }
        
        // Auto-save every 10 waves
        if (gameInfo.waveNumber % 10 == 0) {
            api.getGameController().saveGame("auto_save_wave_" + gameInfo.waveNumber);
        }
    }
    
    private void onBuildingDestroyed(Object event) {
        // Rebuild critical buildings
        rebuildCriticalBuildings();
    }
    
    private void onUnitDeath(Object event) {
        // Spawn replacement units if needed
        maintainUnitCount();
    }
    
    private void performMaintenance() {
        // Check resources
        checkResourceLevels();
        
        // Repair damaged buildings
        repairBuildings();
        
        // Clean up debris
        cleanupDebris();
    }
}
```

## Advanced Features

### 1. Custom AI Behaviors

```java
public class CustomAIBehavior {
    public static void createScoutBot(ControllerAPI api) {
        AIBehavior scoutBehavior = new AIBehavior();
        scoutBehavior.type = "scout";
        scoutBehavior.priority = 8;
        scoutBehavior.enabled = true;
        scoutBehavior.range = 200f;
        scoutBehavior.target = "enemy_base";
        
        AIBehaviorConfig config = new AIBehaviorConfig();
        config.behaviors.add(scoutBehavior);
        config.reactionTime = 0.5f; // Fast reactions
        config.aggressiveness = 0.3f; // Low aggression (scouting)
        
        PlayerInfo scoutBot = new PlayerInfo();
        scoutBot.name = "ScoutBot";
        scoutBot.team = Team.sharded;
        scoutBot.unitType = UnitTypes.flare; // Fast unit
        scoutBot.isAI = true;
        scoutBot.aiConfig = config;
        
        api.getPlayerController().createAIPlayer(scoutBot);
    }
}
```

### 2. Performance Monitoring

```java
public class PerformanceMonitor {
    private ControllerAPI api;
    private long lastUpdateTime = System.currentTimeMillis();
    
    public void startMonitoring() {
        Timer.schedule(this::checkPerformance, 0f, 10f); // Every 10 seconds
    }
    
    private void checkPerformance() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        
        GameInfo gameInfo = api.getGameController().getGameInfo();
        
        Log.info("Performance Report:");
        Log.info("  Game State: " + gameInfo.state);
        Log.info("  Wave: " + gameInfo.waveNumber);
        Log.info("  Players: " + gameInfo.playerCount);
        Log.info("  Update Time: " + deltaTime + "ms");
        
        // Check for performance issues
        if (deltaTime > 1000) { // More than 1 second
            Log.warn("Performance degradation detected!");
            optimizePerformance();
        }
        
        lastUpdateTime = currentTime;
    }
    
    private void optimizePerformance() {
        // Reduce AI count if needed
        List<PlayerInfo> aiPlayers = api.getPlayerController().getAIPlayers();
        if (aiPlayers.size() > 4) {
            Log.info("Reducing AI player count for performance");
            api.getPlayerController().removeAIPlayer(aiPlayers.get(aiPlayers.size() - 1).id);
        }
        
        // Clean up unnecessary entities
        api.getWorldController().cleanupDebris();
    }
}
```

## Error Handling

```java
public class ErrorHandling {
    private ControllerAPI api;
    
    public void robustGameSetup() {
        try {
            api.getGameController().startGame("Map Name", "survival");
        } catch (IllegalStateException e) {
            Log.err("Failed to start game: " + e.getMessage());
            // Try with default map
            try {
                api.getGameController().startGame("Ancient Caldera", "survival");
            } catch (Exception e2) {
                Log.err("Failed to start game with default map: " + e2.getMessage());
                throw new RuntimeException("Cannot start any game", e2);
            }
        }
        
        // Set up error recovery
        api.addEventListener("error", this::handleError);
    }
    
    private void handleError(Object event) {
        Log.err("Game error occurred: " + event);
        
        // Attempt recovery
        try {
            recoverFromError();
        } catch (Exception e) {
            Log.err("Recovery failed: " + e.getMessage());
            // Graceful shutdown
            shutdown();
        }
    }
    
    private void recoverFromError() {
        // Stop current game
        api.getGameController().stopGame();
        
        // Wait a moment
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        
        // Restart with safe defaults
        api.getGameController().startGame("Ancient Caldera", "survival");
    }
    
    private void shutdown() {
        Log.info("Performing graceful shutdown...");
        api.shutdown();
        System.exit(1);
    }
}
```

## Best Practices

1. **Always initialize the API with proper configuration**
2. **Use event listeners for reactive programming**
3. **Handle errors gracefully with try-catch blocks**
4. **Monitor performance and optimize as needed**
5. **Clean up resources when shutting down**
6. **Use appropriate thread safety measures**
7. **Test with small scenarios before scaling up**
8. **Keep AI behavior configurations reasonable**
9. **Save game state periodically**
10. **Log important events for debugging**

## Troubleshooting

### Common Issues

1. **API not responding**: Check if it's properly initialized
2. **AI players not acting**: Verify AI behavior configuration
3. **Performance issues**: Reduce AI count or complexity
4. **Build failures**: Check block placement validity
5. **Network errors**: Verify server is running and accessible

### Debug Mode

```java
APIConfig config = new APIConfig();
config.enableEventLogging = true;
config.debugMode = true;
api.initialize(config);
```

This will provide detailed logging of all API operations.

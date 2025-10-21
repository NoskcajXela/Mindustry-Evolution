# Mindustry Controller API

The Controller API provides programmatic, headless control over Mindustry games. This allows you to create AI players, automate gameplay, run simulations, and build advanced server management tools.

## Features

- **Headless Game Control**: Run Mindustry without graphics for automation and simulation
- **AI Player Management**: Create and control AI players programmatically  
- **World Manipulation**: Modify terrain, place buildings, spawn units
- **Event System**: React to game events and trigger custom actions
- **Server Administration**: Manage servers, players, and game settings
- **Performance Monitoring**: Track game performance and statistics

## Quick Start

### 1. Basic Headless Controller

```java
import mindustry.api.*;

public class BasicExample {
    public static void main(String[] args) {
        // Start headless controller
        var launcher = HeadlessControllerLauncher.create();
        new HeadlessApplication(launcher);
        
        // Wait for initialization
        launcher.waitForInitialization();
        
        // Get API
        var api = launcher.getControllerAPI();
        
        // Start a game
        var maps = api.getAvailableMaps();
        var game = api.startGame(maps.first(), new Rules());
        
        // Create AI player
        var player = api.createPlayer("MyAI", Team.sharded);
        
        // Build something
        player.placeBlock(Blocks.coreNucleus, 50, 50, 0);
        
        // Monitor game
        while (!game.isGameOver()) {
            Thread.sleep(1000);
            System.out.println("Wave: " + game.getWave());
        }
        
        api.shutdown();
    }
}
```

### 2. Server with Controller API

Start the server with API support:

```bash
java -jar server.jar --enable-api
```

Then use server commands:

```
api enable                 # Enable Controller API
createai "Builder" sharded # Create AI player
startgame "Ancient Caldera" # Start a game
apistats                   # Show detailed stats
```

### 3. Advanced AI Player

```java
// Create AI with custom behavior
var player = api.createPlayer("AdvancedAI", Team.sharded);

// Set up building behavior
player.addEventListener("update", (p, event, data) -> {
    if (p.getBuildPlans().isEmpty()) {
        // Find a good location for a drill
        var world = api.getWorld();
        var orePositions = world.getResourceTiles(Items.copper);
        
        if (!orePositions.isEmpty()) {
            var pos = orePositions.first();
            p.placeBlock(Blocks.mechanicalDrill, pos.x, pos.y, 0);
        }
    }
});
```

## Core Components

### ControllerAPI
Main interface for game control:
- `startGame(map, rules)` - Start new game
- `createPlayer(name, team)` - Create AI player  
- `getWorld()` - Access world controller
- `getAdmin()` - Access admin functions

### PlayerController
Control individual players:
- `placeBlock(block, x, y, rotation)` - Build structures
- `moveTo(x, y)` - Move player unit
- `setShooting(shooting)` - Control combat
- `startMining(x, y)` - Mine resources

### WorldController  
Manipulate the game world:
- `setBlock(x, y, block, team, rotation)` - Place blocks
- `getTile(x, y)` - Get tile information
- `getBuildingsInArea(x, y, w, h)` - Query buildings

### GameController
Control game state:
- `nextWave()` - Advance to next wave
- `spawnUnit(type, team, x, y)` - Spawn units
- `setRules(rules)` - Modify game rules

## Use Cases

### 1. AI Development
Create sophisticated AI players for testing and gameplay:

```java
// Create different AI types
var builder = api.createPlayer("Builder", Team.sharded);
var fighter = api.createPlayer("Fighter", Team.sharded); 
var miner = api.createPlayer("Miner", Team.sharded);

// Assign different behaviors
assignBuilderBehavior(builder);
assignFighterBehavior(fighter);  
assignMinerBehavior(miner);
```

### 2. Automated Testing
Test game balance and mechanics:

```java
// Run 100 games and collect statistics
for (int i = 0; i < 100; i++) {
    var game = api.startGame(testMap, testRules);
    
    // Set up test scenario
    setupTestScenario(game);
    
    // Run simulation
    var result = runGameSimulation(game);
    
    // Collect data
    testResults.add(result);
}
```

### 3. Server Management
Automate server administration:

```java
var admin = api.getAdmin();

// Auto-balance teams
if (getTeamImbalance() > 0.3f) {
    balanceTeams();
}

// Monitor performance  
var metrics = admin.getMetrics();
if (metrics.memoryUsage > 0.9f) {
    admin.broadcastMessage("Server restarting in 60 seconds...");
    scheduleRestart();
}
```

### 4. Data Collection
Gather gameplay analytics:

```java
// Track player behavior
api.addEventListener("player.action", (type, data) -> {
    var action = (PlayerAction) data;
    analytics.recordAction(action.player, action.type, action.position);
});

// Monitor resource flows
var world = api.getWorld();
world.forEachTile(tile -> {
    if (tile.build instanceof Drill drill) {
        analytics.recordProduction(drill.item(), drill.efficiency);
    }
});
```

## Configuration

Configure the Controller API behavior:

```java
var config = new ControllerConfig();
config.enableLogging = true;
config.updateRate = 16; // 60 FPS
config.maxAIPlayers = 32;
config.autoGC = true;

// Apply configuration
api.configure(config);
```

## Event System

Listen for and respond to game events:

```java
// Game events
api.addEventListener("game.start", (type, data) -> {
    log("Game started!");
});

api.addEventListener("game.wave", (type, data) -> {
    int wave = (Integer) data;
    log("Wave " + wave + " started");
});

// Player events
player.addEventListener("unit.death", (p, type, data) -> {
    log(p.getName() + " unit died, respawning...");
    p.respawn();
});
```

## Performance Considerations

- Use `api.setGameSpeed(speed)` to run simulations faster
- Call `api.forceGC()` periodically for long-running simulations
- Monitor memory usage with `api.getServerStats()`
- Batch world modifications using `world.batchModify()`

## Error Handling

```java
try {
    var game = api.startGame(map, rules);
} catch (IllegalStateException e) {
    log("Controller not ready: " + e.getMessage());
} catch (RuntimeException e) {
    log("Game start failed: " + e.getMessage());
}
```

## Threading

The Controller API is thread-safe for most operations:

```java
// Safe to call from multiple threads
CompletableFuture.runAsync(() -> {
    var stats = api.getServerStats();
    updateDashboard(stats);
});

// Player actions should be called from game thread
Timer.schedule(() -> {
    player.placeBlock(Blocks.conveyor, x, y, 0);
}, 1f);
```

## Building and Integration

Add to your project's dependencies:

```gradle
dependencies {
    implementation project(':core')
    implementation 'com.github.Anuken.Arc:arc-core:v146'
}
```

## Examples Repository

See the `examples/` directory for complete working examples:

- `BasicAI.java` - Simple AI player implementation
- `ServerAutomation.java` - Automated server management  
- `GameAnalytics.java` - Data collection and analysis
- `PerformanceTesting.java` - Automated performance testing
- `MapGeneration.java` - Procedural map generation

## API Reference

### ControllerAPI Methods

| Method | Description |
|--------|-------------|
| `startGame(map, rules)` | Start new game with map and rules |
| `loadGame(path)` | Load saved game |
| `saveGame(path)` | Save current game |
| `createPlayer(name, team)` | Create AI player |
| `getPlayers()` | Get all players |
| `getWorld()` | Get world controller |
| `getAdmin()` | Get admin controller |
| `shutdown()` | Cleanup and shutdown |

### PlayerController Methods

| Method | Description |
|--------|-------------|  
| `placeBlock(block, x, y, rotation)` | Place building |
| `breakBlock(x, y)` | Destroy building |
| `moveTo(x, y)` | Move to position |
| `setShooting(shooting)` | Control combat |
| `startMining(x, y)` | Start mining |
| `getStats()` | Get player statistics |

### WorldController Methods

| Method | Description |
|--------|-------------|
| `getTile(x, y)` | Get tile at position |
| `setBlock(x, y, block, team, rotation)` | Set block |
| `getBuildingsOfType(block)` | Find buildings of type |
| `getUnitsInArea(x, y, radius)` | Get nearby units |
| `forEachTile(operation)` | Apply operation to all tiles |

## Support

For questions and support:
- Check the [examples](examples/) directory
- Review the [API documentation](docs/api/)  
- Report issues on [GitHub](https://github.com/Anuken/Mindustry/issues)

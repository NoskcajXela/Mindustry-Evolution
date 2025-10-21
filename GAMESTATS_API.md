# GameStats API Documentation

The GameStats API provides comprehensive access to advanced game statistics including economy, power management, and efficiency metrics for Mindustry. This API is designed to be used through the existing Controller API system.

## Overview

The GameStats API extends the basic statistics available in Mindustry by providing:

- **Economy Statistics**: Production, consumption, and resource flow tracking
- **Power Statistics**: Generation, consumption, efficiency, and shortage monitoring  
- **Uptime and Efficiency**: Performance metrics and utilization tracking
- **Game Outcome Tracking**: Win/loss detection with real-time notifications
- **Real-time Monitoring**: Live updates and event-driven statistics
- **Data Export**: JSON and CSV export capabilities

## Getting Started

### Basic Access

```java
// Get the Controller API instance
ControllerAPI controllerAPI = // ... obtain from server or headless launcher

// Access the GameStats API
GameStatsAPI statsAPI = controllerAPI.getGameStats();

// Get basic statistics
var basicStats = statsAPI.getBasicStats();
System.out.println("Wave: " + statsAPI.getCurrentWave());
System.out.println("Units created: " + basicStats.unitsCreated);
```

### Economy Statistics

```java
// Get production and consumption data
var itemsProduced = statsAPI.getItemsProduced();
var itemsConsumed = statsAPI.getItemsConsumed();
var currentRates = statsAPI.getCurrentProductionRates();

// Calculate net flow (production - consumption)
var netFlow = statsAPI.getNetItemFlow();

// Get comprehensive economy summary
var economy = statsAPI.getEconomySummary();
System.out.println("Economy efficiency: " + economy.economyEfficiency);
System.out.println("Most produced item: " + economy.mostProducedItem);
```

### Power Statistics

```java
// Current power status
float generation = statsAPI.getCurrentPowerGeneration();
float consumption = statsAPI.getCurrentPowerConsumption();
float efficiency = statsAPI.getCurrentPowerEfficiency();

// Storage information
float stored = statsAPI.getCurrentPowerStored();
float capacity = statsAPI.getPowerStorageCapacity();

// Power shortage tracking
int shortages = statsAPI.getPowerShortageCount();
boolean inShortage = statsAPI.isCurrentlyInPowerShortage();

// Comprehensive power summary
var power = statsAPI.getPowerSummary();
```

### Real-time Monitoring

```java
// Set up real-time monitoring
statsAPI.addStatsUpdateListener(summary -> {
    System.out.println("Power efficiency: " + summary.power.efficiency);
    System.out.println("Economy efficiency: " + summary.economy.economyEfficiency);
    
    // Alert on issues
    if (summary.power.efficiency < 0.8f) {
        System.out.println("LOW POWER WARNING!");
    }
});

// Configure update frequency (60 ticks = 1 second)
statsAPI.setUpdateInterval(60);
```

## API Reference

### Core Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `getBasicStats()` | Basic game statistics | `BasicGameStats` |
| `getCurrentWave()` | Current wave number | `int` |
| `getTotalGameTime()` | Total game time in ticks | `long` |

### Economy Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `getItemsProduced()` | Total items produced by type | `ObjectLongMap<Item>` |
| `getItemsConsumed()` | Total items consumed by type | `ObjectLongMap<Item>` |
| `getCurrentProductionRates()` | Current production rates (items/sec) | `ObjectFloatMap<Item>` |
| `getCurrentConsumptionRates()` | Current consumption rates (items/sec) | `ObjectFloatMap<Item>` |
| `getNetItemFlow()` | Net item flow (production - consumption) | `ObjectLongMap<Item>` |
| `getEconomySummary()` | Comprehensive economy summary | `EconomySummary` |

### Power Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `getCurrentPowerGeneration()` | Current power generation rate | `float` |
| `getCurrentPowerConsumption()` | Current power consumption rate | `float` |
| `getCurrentPowerEfficiency()` | Current power efficiency (0.0-1.0) | `float` |
| `getCurrentPowerStored()` | Current power in batteries | `float` |
| `getPowerStorageCapacity()` | Total battery capacity | `float` |
| `isCurrentlyInPowerShortage()` | Check if experiencing shortage | `boolean` |
| `getPowerSummary()` | Comprehensive power summary | `PowerSummary` |

### Monitoring Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `addStatsUpdateListener()` | Add real-time update listener | `void` |
| `removeStatsUpdateListener()` | Remove update listener | `void` |
| `setUpdateInterval()` | Set update frequency in ticks | `void` |
| `forceStatsUpdate()` | Force immediate update | `void` |

### Export Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `exportStatsAsJSON()` | Export statistics as JSON | `String` |
| `exportStatsAsCSV()` | Export statistics as CSV | `String` |
| `getPerformanceSummary()` | Complete performance overview | `GamePerformanceSummary` |

## Data Structures

### EconomySummary
```java
public class EconomySummary {
    public long totalItemsProduced;      // Total items produced
    public long totalItemsConsumed;      // Total items consumed
    public long totalResourceValue;      // Total resource value mined
    public float averageProductionRate;  // Average production rate
    public float averageConsumptionRate; // Average consumption rate
    public Item mostProducedItem;        // Most produced item type
    public Item mostConsumedItem;        // Most consumed item type
    public float economyEfficiency;      // Overall efficiency ratio
}
```

### PowerSummary
```java
public class PowerSummary {
    public long totalGenerated;       // Total power generated
    public long totalConsumed;        // Total power consumed
    public float currentGeneration;   // Current generation rate
    public float currentConsumption;  // Current consumption rate
    public float efficiency;          // Current efficiency
    public float storageUtilization;  // Storage utilization %
    public int shortageEvents;        // Number of shortage events
    public long averageShortageTime;  // Average shortage duration
}
```

### EfficiencySummary
```java
public class EfficiencySummary {
    public float powerEfficiency;     // Power grid efficiency
    public float buildingEfficiency;  // Building survival rate
    public float resourceEfficiency;  // Resource extraction rate
    public float overallEfficiency;   // Combined efficiency
    public long activeTime;          // Time spent actively playing
    public long totalTime;           // Total game time
    public float uptime;             // Uptime percentage
}
```

## Usage Examples

### Simple Dashboard
```java
public void createDashboard(GameStatsAPI statsAPI) {
    Timer.schedule(() -> {
        var summary = statsAPI.getPerformanceSummary();
        
        System.out.printf("Wave %d | Power: %.1f%% | Economy: %.1f%%\n",
            summary.currentWave,
            summary.power.efficiency * 100,
            summary.economy.economyEfficiency * 100);
            
        // Check for issues
        if (statsAPI.isCurrentlyInPowerShortage()) {
            System.out.println("⚠️ POWER SHORTAGE!");
        }
        if (statsAPI.isCoreAtCapacity()) {
            System.out.println("📦 CORE FULL!");
        }
    }, 0f, 5f); // Update every 5 seconds
}
```

### Production Monitoring
```java
public void monitorProduction(GameStatsAPI statsAPI, Item item) {
    float currentRate = statsAPI.getCurrentProductionRates().get(item, 0f);
    float itemsPerMinute = statsAPI.getItemsPerMinute(item);
    
    System.out.printf("%s: %.2f/sec (%.1f/min)\n", 
        item.name, currentRate, itemsPerMinute);
}
```

### Performance Alerts
```java
public void setupAlerts(GameStatsAPI statsAPI) {
    statsAPI.addStatsUpdateListener(summary -> {
        // Power efficiency alert
        if (summary.power.efficiency < 0.7f) {
            alert("Low power efficiency: " + 
                  String.format("%.1f%%", summary.power.efficiency * 100));
        }
        
        // Economy efficiency alert
        if (summary.economy.economyEfficiency < 0.5f) {
            alert("Poor economy efficiency");
        }
        
        // Uptime alert
        if (summary.efficiency.uptime < 0.8f) {
            alert("Low uptime: " + 
                  String.format("%.1f%%", summary.efficiency.uptime * 100));
        }
    });
}
```

## Integration with Existing Systems

The GameStats API integrates seamlessly with the existing Controller API:

```java
// Server integration
ControllerServerControl server = new ControllerServerControl();
server.initializeControllerAPI();
GameStatsAPI stats = server.getControllerAPI().getGameStats();

// Headless integration
HeadlessControllerLauncher launcher = HeadlessControllerLauncher.create();
GameStatsAPI stats = launcher.getControllerAPI().getGameStats();

// Command integration
handler.register("stats", "Show game statistics", args -> {
    var summary = stats.getPerformanceSummary();
    info("Current stats: " + stats.exportStatsAsJSON());
});
```

### Game Outcome Tracking

The GameStats API provides comprehensive tracking of game outcomes, including win/loss detection and notifications:

```java
// Check current game state
GameOutcome outcome = statsAPI.getGameOutcome();
System.out.println("Game ended: " + outcome.gameEnded);
System.out.println("Victory: " + outcome.victory);
System.out.println("Winner: " + outcome.winnerTeam);

// Simple checks
boolean gameEnded = statsAPI.hasGameEnded();
boolean controllerWon = statsAPI.didControllerTeamWin();
String winner = statsAPI.getWinnerTeam();
```

#### Game Outcome Events

Register listeners to receive notifications when games end:

```java
// Add outcome listener
statsAPI.addGameOutcomeListener(new GameStatsAPI.GameOutcomeListener() {
    @Override
    public void onGameEnd(GameStatsAPI.GameOutcome outcome) {
        System.out.println("Game ended! Winner: " + outcome.winnerTeam);
        System.out.println("End reason: " + outcome.endReason);
        System.out.println("Final wave: " + outcome.finalWave);
        
        // Log all participating teams
        for (String team : outcome.participatingTeams) {
            System.out.println("Participant: " + team);
        }
    }
    
    @Override
    public void onControllerTeamVictory(GameStatsAPI.GameOutcome outcome) {
        System.out.println("🎉 VICTORY! We won on wave " + outcome.finalWave);
        // Send victory notification, update leaderboards, etc.
    }
    
    @Override
    public void onControllerTeamDefeat(GameStatsAPI.GameOutcome outcome) {
        System.out.println("💀 DEFEAT! Lost to " + outcome.winnerTeam);
        // Handle defeat logic, restart game, etc.
    }
});
```

#### GameOutcome Data Structure

The `GameOutcome` class provides comprehensive information about game results:

```java
public class GameOutcome {
    public boolean gameEnded;          // Whether the game has ended
    public boolean victory;            // Whether it was a victory (vs defeat)
    public String winnerTeam;          // Name of the winning team
    public String endReason;           // Reason the game ended
    public long gameEndTime;           // Timestamp when game ended
    public int finalWave;              // Wave number when game ended
    public boolean controllerTeamWon;  // Whether the API controller's team won
    public String[] participatingTeams; // All teams that participated
}
```

#### Use Cases

**Victory Notifications**: Get notified immediately when your team wins
```java
statsAPI.addGameOutcomeListener(new GameOutcomeListener() {
    public void onControllerTeamVictory(GameOutcome outcome) {
        sendDiscordNotification("Victory on wave " + outcome.finalWave + "!");
        updatePlayerStats(outcome);
    }
});
```

**Automated Restarts**: Restart games automatically after defeat
```java
public void onControllerTeamDefeat(GameOutcome outcome) {
    logger.info("Game lost, restarting in 10 seconds...");
    Timer.schedule(() -> restartGame(), 10f);
}
```

**Statistics Logging**: Track game outcomes for analysis
```java
public void onGameEnd(GameOutcome outcome) {
    GameResult result = new GameResult(
        outcome.victory,
        outcome.finalWave,
        outcome.gameEndTime - outcome.gameStartTime
    );
    database.saveGameResult(result);
}
```

## Best Practices

1. **Update Frequency**: Set appropriate update intervals to balance performance and responsiveness
2. **Resource Management**: Remove listeners when no longer needed to prevent memory leaks
3. **Error Handling**: Wrap API calls in try-catch blocks for production use
4. **Data Persistence**: Export statistics periodically for historical analysis
5. **Performance Monitoring**: Use the efficiency metrics to optimize game performance

## Troubleshooting

### Common Issues

**Issue**: Statistics not updating
- **Solution**: Ensure the game is running and the API is properly initialized

**Issue**: Memory usage increasing over time  
- **Solution**: Remove unused event listeners and limit history cache sizes

**Issue**: Performance impact
- **Solution**: Increase update intervals and reduce the number of active listeners

### Debug Information

```java
// Check API status
System.out.println("Stats start time: " + statsAPI.getStatsStartTime());
System.out.println("Update interval: " + statsAPI.getUpdateInterval());

// Force update for debugging
statsAPI.forceStatsUpdate();

// Export for analysis
String debugData = statsAPI.exportStatsAsJSON();
```

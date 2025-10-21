package mindustry.api.impl;

import arc.struct.*;
import arc.util.*;
import mindustry.api.GameStatsAPI;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.*;

/**
 * Implementation of the GameStatsAPI providing access to advanced game statistics.
 * Integrates with the existing GameStats system and provides real-time monitoring.
 */
public class GameStatsAPIImpl implements GameStatsAPI {
    
    private final Seq<StatsUpdateListener> listeners = new Seq<>();
    private final Seq<GameOutcomeListener> outcomeListeners = new Seq<>();
    private int updateInterval = 60; // 60 ticks = 1 second
    private long lastUpdate = 0;
    
    // Game outcome tracking
    private GameOutcome currentOutcome = new GameOutcome();
    private boolean wasGameOver = false;
    private String controllerTeamName = null;
    
    // Cached calculations
    private final ObjectFloatMap<Item> currentProductionCache = new ObjectFloatMap<>();
    private final ObjectFloatMap<Item> currentConsumptionCache = new ObjectFloatMap<>();
    private final Seq<Float> powerHistoryCache = new Seq<>();
    private final ObjectMap<Item, Seq<Float>> productionHistoryCache = new ObjectMap<>();
    
    public GameStatsAPIImpl() {
        // Initialize caches
        updateCaches();
    }
    
    @Override
    public BasicGameStats getBasicStats() {
        var basic = new BasicGameStats();
        basic.enemyUnitsDestroyed = state.stats.enemyUnitsDestroyed;
        basic.wavesLasted = state.stats.wavesLasted;
        basic.buildingsBuilt = state.stats.buildingsBuilt;
        basic.buildingsDeconstructed = state.stats.buildingsDeconstructed;
        basic.buildingsDestroyed = state.stats.buildingsDestroyed;
        basic.unitsCreated = state.stats.unitsCreated;
        basic.placedBlockCount = state.stats.placedBlockCount.copy();
        basic.coreItemCount = state.stats.coreItemCount.copy();
        return basic;
    }
    
    @Override
    public int getCurrentWave() {
        return state.wave;
    }
    
    @Override
    public long getTotalGameTime() {
        return state.stats.totalGameTime;
    }
    
    // === Economy Statistics ===
    
    @Override
    public ObjectLongMap<Item> getItemsProduced() {
        return state.stats.itemsProduced.copy();
    }
    
    @Override
    public ObjectLongMap<Item> getItemsConsumed() {
        return state.stats.itemsConsumed.copy();
    }
    
    @Override
    public ObjectFloatMap<Item> getPeakProductionRates() {
        return state.stats.peakItemProductionRate.copy();
    }
    
    @Override
    public ObjectFloatMap<Item> getPeakConsumptionRates() {
        return state.stats.peakItemConsumptionRate.copy();
    }
    
    @Override
    public ObjectFloatMap<Item> getCurrentProductionRates() {
        updateCaches();
        return currentProductionCache.copy();
    }
    
    @Override
    public ObjectFloatMap<Item> getCurrentConsumptionRates() {
        updateCaches();
        return currentConsumptionCache.copy();
    }
    
    @Override
    public long getTotalResourceValueMined() {
        return state.stats.totalResourceValueMined;
    }
    
    @Override
    public ObjectIntMap<Item> getItemsLaunched() {
        return state.stats.itemsLaunched.copy();
    }
    
    @Override
    public ObjectLongMap<Item> getTotalBuildingCosts() {
        return state.stats.totalBuildingCost.copy();
    }
    
    @Override
    public ObjectLongMap<Item> getNetItemFlow() {
        var net = new ObjectLongMap<Item>();
        var produced = getItemsProduced();
        var consumed = getItemsConsumed();
        
        // Calculate net flow (production - consumption)
        produced.each((item, amount) -> {
            long consumedAmount = consumed.get(item, 0L);
            net.put(item, amount - consumedAmount);
        });
        
        // Add items that were only consumed
        consumed.each((item, amount) -> {
            if (!produced.containsKey(item)) {
                net.put(item, -amount);
            }
        });
        
        return net;
    }
    
    // === Power Statistics ===
    
    @Override
    public long getTotalPowerGenerated() {
        return state.stats.totalPowerGenerated;
    }
    
    @Override
    public long getTotalPowerConsumed() {
        return state.stats.totalPowerConsumed;
    }
    
    @Override
    public float getPeakPowerGeneration() {
        return state.stats.peakPowerGeneration;
    }
    
    @Override
    public float getPeakPowerConsumption() {
        return state.stats.peakPowerConsumption;
    }
    
    @Override
    public float getCurrentPowerGeneration() {
        return calculateCurrentPowerGeneration();
    }
    
    @Override
    public float getCurrentPowerConsumption() {
        return calculateCurrentPowerConsumption();
    }
    
    @Override
    public float getCurrentPowerEfficiency() {
        float generation = getCurrentPowerGeneration();
        float consumption = getCurrentPowerConsumption();
        if (consumption == 0) return 1.0f;
        return Math.min(1.0f, generation / consumption);
    }
    
    @Override
    public float getMaxPowerStored() {
        return state.stats.maxPowerStored;
    }
    
    @Override
    public float getCurrentPowerStored() {
        return calculateCurrentPowerStored();
    }
    
    @Override
    public float getPowerStorageCapacity() {
        return calculatePowerStorageCapacity();
    }
    
    // === Power Shortage Statistics ===
    
    @Override
    public int getPowerShortageCount() {
        return state.stats.powerShortageCount;
    }
    
    @Override
    public long getTotalPowerShortageTime() {
        return state.stats.totalPowerShortageTime;
    }
    
    @Override
    public long getLastPowerShortageTime() {
        return state.stats.lastPowerShortageTime;
    }
    
    @Override
    public long getLongestPowerShortage() {
        return state.stats.longestPowerShortage;
    }
    
    @Override
    public boolean isCurrentlyInPowerShortage() {
        return getCurrentPowerEfficiency() < 0.95f; // Consider <95% efficiency as shortage
    }
    
    // === Uptime and Efficiency Statistics ===
    
    @Override
    public long getTimeAtFullPowerEfficiency() {
        return state.stats.timeAtFullPowerEfficiency;
    }
    
    @Override
    public long getActivePlayTime() {
        return state.stats.activePlayTime;
    }
    
    @Override
    public float getPowerEfficiencyPercentage() {
        return getCurrentPowerEfficiency() * 100.0f;
    }
    
    @Override
    public float getUptimePercentage() {
        long total = getTotalGameTime();
        if (total == 0) return 0.0f;
        return (getActivePlayTime() / (float) total) * 100.0f;
    }
    
    // === Building and Infrastructure Statistics ===
    
    @Override
    public int getGeneratorsDestroyed() {
        return state.stats.generatorsDestroyed;
    }
    
    @Override
    public int getPowerNodesDestroyed() {
        return state.stats.powerNodesDestroyed;
    }
    
    @Override
    public int getPowerNetworksCreated() {
        return state.stats.powerNetworksCreated;
    }
    
    @Override
    public int getLargestPowerNetworkSize() {
        return state.stats.largestPowerNetworkSize;
    }
    
    @Override
    public int getCurrentPowerNetworks() {
        return calculateCurrentPowerNetworks();
    }
    
    // === Core and Storage Statistics ===
    
    @Override
    public long getTimeAtMaxCoreCapacity() {
        return state.stats.timeAtMaxCoreCapacity;
    }
    
    @Override
    public int getCoreCapacityReachedCount() {
        return state.stats.coreCapacityReachedCount;
    }
    
    @Override
    public boolean isCoreAtCapacity() {
        var cores = state.teams.playerCores();
        if (cores.isEmpty()) return false;
        
        var core = cores.first();
        return core.items.total() >= core.storageCapacity;
    }
    
    @Override
    public float getCoreStoragePercentage() {
        var cores = state.teams.playerCores();
        if (cores.isEmpty()) return 0.0f;
        
        var core = cores.first();
        return (core.items.total() / (float) core.storageCapacity) * 100.0f;
    }
    
    // === Performance and Rate Statistics ===
    
    @Override
    public float getItemsPerMinute(Item item) {
        return getCurrentProductionRates().get(item, 0f) * 60f;
    }
    
    @Override
    public float getPowerPerMinute() {
        return getCurrentPowerGeneration() * 60f;
    }
    
    @Override
    public float getBuildingsPerMinute() {
        long totalTime = getTotalGameTime();
        if (totalTime == 0) return 0f;
        return (getBasicStats().buildingsBuilt / (totalTime / 60f / 60f)); // Convert to minutes
    }
    
    @Override
    public float getResourceEfficiencyPerMinute() {
        long totalTime = getTotalGameTime();
        if (totalTime == 0) return 0f;
        return (getTotalResourceValueMined() / (totalTime / 60f / 60f)); // Convert to minutes
    }
    
    // === Historical Data ===
    
    @Override
    public Seq<Float> getProductionHistory(Item item, int samples) {
        var history = productionHistoryCache.get(item);
        if (history == null) return new Seq<>();
        return history.copy().truncate(samples);
    }
    
    @Override
    public Seq<Float> getPowerHistory(int samples) {
        return powerHistoryCache.copy().truncate(samples);
    }
    
    @Override
    public Seq<Float> getEfficiencyHistory(int samples) {
        // Calculate efficiency history from power history
        var efficiency = new Seq<Float>();
        var power = getPowerHistory(samples);
        for (float p : power) {
            efficiency.add(Math.min(1.0f, p / Math.max(1.0f, getCurrentPowerConsumption())));
        }
        return efficiency;
    }
    
    // === Aggregated Statistics ===
    
    @Override
    public EconomySummary getEconomySummary() {
        var summary = new EconomySummary();
        var produced = getItemsProduced();
        var consumed = getItemsConsumed();
        
        summary.totalItemsProduced = produced.values().toArray().sum();
        summary.totalItemsConsumed = consumed.values().toArray().sum();
        summary.totalResourceValue = getTotalResourceValueMined();
        
        var currentProd = getCurrentProductionRates();
        summary.averageProductionRate = currentProd.values().toArray().sum();
        
        var currentCons = getCurrentConsumptionRates();
        summary.averageConsumptionRate = currentCons.values().toArray().sum();
        
        // Find most produced/consumed items
        Item mostProd = null;
        long maxProd = 0;
        for (var entry : produced.entries()) {
            if (entry.value > maxProd) {
                maxProd = entry.value;
                mostProd = entry.key;
            }
        }
        summary.mostProducedItem = mostProd;
        
        Item mostCons = null;
        long maxCons = 0;
        for (var entry : consumed.entries()) {
            if (entry.value > maxCons) {
                maxCons = entry.value;
                mostCons = entry.key;
            }
        }
        summary.mostConsumedItem = mostCons;
        
        // Calculate efficiency
        if (summary.totalItemsConsumed == 0) {
            summary.economyEfficiency = 1.0f;
        } else {
            summary.economyEfficiency = summary.totalItemsProduced / (float) summary.totalItemsConsumed;
        }
        
        return summary;
    }
    
    @Override
    public PowerSummary getPowerSummary() {
        var summary = new PowerSummary();
        summary.totalGenerated = getTotalPowerGenerated();
        summary.totalConsumed = getTotalPowerConsumed();
        summary.currentGeneration = getCurrentPowerGeneration();
        summary.currentConsumption = getCurrentPowerConsumption();
        summary.efficiency = getCurrentPowerEfficiency();
        summary.storageUtilization = getCurrentPowerStored() / Math.max(1.0f, getPowerStorageCapacity());
        summary.shortageEvents = getPowerShortageCount();
        summary.averageShortageTime = summary.shortageEvents > 0 ? 
            getTotalPowerShortageTime() / summary.shortageEvents : 0;
        return summary;
    }
    
    @Override
    public EfficiencySummary getEfficiencySummary() {
        var summary = new EfficiencySummary();
        summary.powerEfficiency = getCurrentPowerEfficiency();
        summary.buildingEfficiency = calculateBuildingEfficiency();
        summary.resourceEfficiency = calculateResourceEfficiency();
        summary.overallEfficiency = (summary.powerEfficiency + summary.buildingEfficiency + summary.resourceEfficiency) / 3.0f;
        summary.activeTime = getActivePlayTime();
        summary.totalTime = getTotalGameTime();
        summary.uptime = getUptimePercentage() / 100.0f;
        return summary;
    }
    
    @Override
    public GamePerformanceSummary getPerformanceSummary() {
        var summary = new GamePerformanceSummary();
        summary.basicStats = getBasicStats();
        summary.economy = getEconomySummary();
        summary.power = getPowerSummary();
        summary.efficiency = getEfficiencySummary();
        summary.gameStartTime = 0; // Would need to track game start
        summary.currentTime = getTotalGameTime();
        summary.currentWave = getCurrentWave();
        summary.isGameActive = state.isGame();
        return summary;
    }
    
    // === Real-time Monitoring ===
    
    @Override
    public void addStatsUpdateListener(StatsUpdateListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeStatsUpdateListener(StatsUpdateListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void forceStatsUpdate() {
        updateCaches();
        fireStatsUpdate();
    }
    
    @Override
    public void setUpdateInterval(int ticks) {
        this.updateInterval = Math.max(1, ticks);
    }
    
    // === Utility Methods ===
    
    @Override
    public void resetAllStats() {
        // Reset all statistics in GameStats
        state.stats.totalPowerGenerated = 0;
        state.stats.totalPowerConsumed = 0;
        state.stats.itemsProduced.clear();
        state.stats.itemsConsumed.clear();
        state.stats.peakItemProductionRate.clear();
        state.stats.peakItemConsumptionRate.clear();
        // Reset other stats...
        
        // Clear caches
        currentProductionCache.clear();
        currentConsumptionCache.clear();
        powerHistoryCache.clear();
        productionHistoryCache.clear();
    }
    
    @Override
    public String exportStatsAsJSON() {
        var summary = getPerformanceSummary();
        // Simple JSON export - in a real implementation you'd use a proper JSON library
        return "{\n" +
            "  \"wave\": " + summary.currentWave + ",\n" +
            "  \"gameTime\": " + summary.currentTime + ",\n" +
            "  \"unitsCreated\": " + summary.basicStats.unitsCreated + ",\n" +
            "  \"buildingsBuilt\": " + summary.basicStats.buildingsBuilt + ",\n" +
            "  \"powerGenerated\": " + summary.power.totalGenerated + ",\n" +
            "  \"powerConsumed\": " + summary.power.totalConsumed + ",\n" +
            "  \"powerEfficiency\": " + summary.power.efficiency + ",\n" +
            "  \"economyEfficiency\": " + summary.economy.economyEfficiency + "\n" +
            "}";
    }
    
    @Override
    public String exportStatsAsCSV() {
        var summary = getPerformanceSummary();
        return "Metric,Value\n" +
            "Wave," + summary.currentWave + "\n" +
            "Game Time," + summary.currentTime + "\n" +
            "Units Created," + summary.basicStats.unitsCreated + "\n" +
            "Buildings Built," + summary.basicStats.buildingsBuilt + "\n" +
            "Power Generated," + summary.power.totalGenerated + "\n" +
            "Power Consumed," + summary.power.totalConsumed + "\n" +
            "Power Efficiency," + summary.power.efficiency + "\n" +
            "Economy Efficiency," + summary.economy.economyEfficiency;
    }
    
    @Override
    public long getStatsStartTime() {
        return 0; // Would need to track when stats collection started
    }
    
    // === Private Helper Methods ===
    
    private void updateCaches() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate < updateInterval * 16) { // Convert ticks to ms (roughly)
            return;
        }
        
        updateProductionRates();
        updatePowerHistory();
        lastUpdate = currentTime;
    }
    
    private void updateProductionRates() {
        currentProductionCache.clear();
        currentConsumptionCache.clear();
        
        // Calculate current production/consumption rates
        // This would need to integrate with actual building update cycles
        // For now, return placeholder values
    }
    
    private void updatePowerHistory() {
        powerHistoryCache.add(getCurrentPowerGeneration());
        if (powerHistoryCache.size > 300) { // Keep last 5 minutes at 1Hz
            powerHistoryCache.removeRange(0, 100);
        }
    }
    
    private float calculateCurrentPowerGeneration() {
        // Sum power from all active generators
        float total = 0f;
        Groups.build.each(build -> {
            if (build instanceof PowerGenerator.GeneratorBuild gen && build.team == state.rules.defaultTeam) {
                total += gen.getPowerProduction();
            }
        });
        return total;
    }
    
    private float calculateCurrentPowerConsumption() {
        // Sum power consumption from all consumers
        float total = 0f;
        Groups.build.each(build -> {
            if (build.block.consumesPower && build.team == state.rules.defaultTeam) {
                var consumer = build.block.consPower;
                if (consumer != null) {
                    total += consumer.requestedPower(build);
                }
            }
        });
        return total;
    }
    
    private float calculateCurrentPowerStored() {
        float total = 0f;
        Groups.build.each(build -> {
            if (build.block.consumesPower && build.block.consPower.buffered && build.team == state.rules.defaultTeam) {
                total += build.power.status * build.block.consPower.capacity;
            }
        });
        return total;
    }
    
    private float calculatePowerStorageCapacity() {
        float total = 0f;
        Groups.build.each(build -> {
            if (build.block.consumesPower && build.block.consPower.buffered && build.team == state.rules.defaultTeam) {
                total += build.block.consPower.capacity;
            }
        });
        return total;
    }
    
    private int calculateCurrentPowerNetworks() {
        // Count unique power graphs
        var graphs = new ObjectSet<PowerGraph>();
        Groups.build.each(build -> {
            if (build.block.hasPower && build.team == state.rules.defaultTeam && build.power != null) {
                graphs.add(build.power.graph);
            }
        });
        return graphs.size;
    }
    
    private float calculateBuildingEfficiency() {
        // Calculate based on buildings destroyed vs built
        int built = getBasicStats().buildingsBuilt;
        int destroyed = getBasicStats().buildingsDestroyed;
        if (built == 0) return 1.0f;
        return Math.max(0f, (built - destroyed) / (float) built);
    }
    
    private float calculateResourceEfficiency() {
        // Calculate based on resource value vs time
        long value = getTotalResourceValueMined();
        long time = getTotalGameTime();
        if (time == 0) return 0f;
        return value / (float) time; // Resource value per tick
    }
    
    private void fireStatsUpdate() {
        if (listeners.isEmpty()) return;
        
        var summary = getPerformanceSummary();
        for (var listener : listeners) {
            try {
                listener.onStatsUpdate(summary);
            } catch (Exception e) {
                Log.err("Error in stats update listener", e);
            }
        }
    }
    
    // === Game Outcome Implementation ===
    
    @Override
    public GameOutcome getGameOutcome() {
        updateGameOutcome();
        return currentOutcome;
    }
    
    @Override
    public boolean hasGameEnded() {
        return state.gameOver;
    }
    
    @Override
    public boolean didControllerTeamWin() {
        updateGameOutcome();
        return currentOutcome.controllerTeamWon;
    }
    
    @Override
    public String getWinnerTeam() {
        updateGameOutcome();
        return currentOutcome.winnerTeam;
    }
    
    @Override
    public String getGameEndReason() {
        updateGameOutcome();
        return currentOutcome.endReason;
    }
    
    @Override
    public void addGameOutcomeListener(GameOutcomeListener listener) {
        if (listener != null) {
            outcomeListeners.add(listener);
        }
    }
    
    @Override
    public void removeGameOutcomeListener(GameOutcomeListener listener) {
        outcomeListeners.remove(listener);
    }
    
    private void updateGameOutcome() {
        boolean isGameOver = state.gameOver;
        
        // Update basic outcome info
        currentOutcome.gameEnded = isGameOver;
        currentOutcome.finalWave = state.wave;
        
        if (isGameOver) {
            if (currentOutcome.gameEndTime == 0) {
                currentOutcome.gameEndTime = System.currentTimeMillis();
            }
            
            // Determine winner and reason
            if (state.rules.pvp) {
                // PvP mode - check which team has cores remaining
                var teamsWithCores = new Seq<String>();
                for (var team : state.teams.getActive()) {
                    if (team.hasCore()) {
                        teamsWithCores.add(team.team.name);
                    }
                }
                
                if (teamsWithCores.size == 1) {
                    currentOutcome.winnerTeam = teamsWithCores.first();
                    currentOutcome.victory = true;
                    currentOutcome.endReason = "Enemy cores destroyed";
                } else if (teamsWithCores.size == 0) {
                    currentOutcome.winnerTeam = null;
                    currentOutcome.victory = false;
                    currentOutcome.endReason = "All teams eliminated";
                } else {
                    // Multiple teams with cores - shouldn't happen when game is over
                    currentOutcome.winnerTeam = "Unknown";
                    currentOutcome.victory = false;
                    currentOutcome.endReason = "Game ended unexpectedly";
                }
            } else {
                // Campaign/survival mode
                if (state.teams.playerTeam().hasCore()) {
                    currentOutcome.victory = true;
                    currentOutcome.winnerTeam = state.teams.playerTeam().team.name;
                    currentOutcome.endReason = "Victory conditions met";
                } else {
                    currentOutcome.victory = false;
                    currentOutcome.winnerTeam = "Enemy";
                    currentOutcome.endReason = "Core destroyed";
                }
            }
            
            // Determine if controller team won
            if (controllerTeamName == null) {
                controllerTeamName = state.teams.playerTeam().team.name;
            }
            currentOutcome.controllerTeamWon = controllerTeamName.equals(currentOutcome.winnerTeam);
            
            // Get participating teams
            var participatingTeams = new Seq<String>();
            for (var team : state.teams.getActive()) {
                participatingTeams.add(team.team.name);
            }
            currentOutcome.participatingTeams = participatingTeams.toArray(String.class);
        }
        
        // Fire events if game just ended
        if (isGameOver && !wasGameOver) {
            fireGameOutcomeEvents();
            wasGameOver = true;
        }
    }
    
    private void fireGameOutcomeEvents() {
        if (outcomeListeners.isEmpty()) return;
        
        for (var listener : outcomeListeners) {
            try {
                listener.onGameEnd(currentOutcome);
                
                if (currentOutcome.controllerTeamWon) {
                    listener.onControllerTeamVictory(currentOutcome);
                } else {
                    listener.onControllerTeamDefeat(currentOutcome);
                }
            } catch (Exception e) {
                Log.err("Error in game outcome listener", e);
            }
        }
    }
    
    /** Should be called periodically from the game loop */
    public void update() {
        if (System.currentTimeMillis() - lastUpdate >= updateInterval * 16) {
            updateCaches();
            updateGameOutcome(); // Check for game outcome changes
            fireStatsUpdate();
        }
    }
}

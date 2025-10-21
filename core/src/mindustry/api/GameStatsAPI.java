package mindustry.api;

import arc.struct.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * API for accessing advanced game statistics including economy, power, and efficiency metrics.
 * Provides comprehensive data about game performance, resource flow, and player activity.
 */
public interface GameStatsAPI {
    
    // === Basic Statistics ===
    
    /** Get basic game statistics (existing stats) */
    BasicGameStats getBasicStats();
    
    /** Get current wave number */
    int getCurrentWave();
    
    /** Get total game time in ticks */
    long getTotalGameTime();
    
    // === Economy Statistics ===
    
    /** Get total items produced by type */
    ObjectLongMap<Item> getItemsProduced();
    
    /** Get total items consumed by type */
    ObjectLongMap<Item> getItemsConsumed();
    
    /** Get peak production rate for each item (items per second) */
    ObjectFloatMap<Item> getPeakProductionRates();
    
    /** Get peak consumption rate for each item (items per second) */
    ObjectFloatMap<Item> getPeakConsumptionRates();
    
    /** Get current production rate for each item (items per second) */
    ObjectFloatMap<Item> getCurrentProductionRates();
    
    /** Get current consumption rate for each item (items per second) */
    ObjectFloatMap<Item> getCurrentConsumptionRates();
    
    /** Get total resource value mined (based on item rarity) */
    long getTotalResourceValueMined();
    
    /** Get items launched via launch pads */
    ObjectIntMap<Item> getItemsLaunched();
    
    /** Get total building costs by item type */
    ObjectLongMap<Item> getTotalBuildingCosts();
    
    /** Get net item flow (production - consumption) */
    ObjectLongMap<Item> getNetItemFlow();
    
    // === Power Statistics ===
    
    /** Get total power generated (all time) */
    long getTotalPowerGenerated();
    
    /** Get total power consumed (all time) */
    long getTotalPowerConsumed();
    
    /** Get peak power generation rate (power per second) */
    float getPeakPowerGeneration();
    
    /** Get peak power consumption rate (power per second) */
    float getPeakPowerConsumption();
    
    /** Get current power generation rate (power per second) */
    float getCurrentPowerGeneration();
    
    /** Get current power consumption rate (power per second) */
    float getCurrentPowerConsumption();
    
    /** Get current power grid efficiency (0.0 to 1.0) */
    float getCurrentPowerEfficiency();
    
    /** Get maximum power stored in batteries */
    float getMaxPowerStored();
    
    /** Get current power stored in batteries */
    float getCurrentPowerStored();
    
    /** Get power storage capacity */
    float getPowerStorageCapacity();
    
    // === Power Shortage Statistics ===
    
    /** Get number of power shortage events */
    int getPowerShortageCount();
    
    /** Get total time spent in power shortage (in ticks) */
    long getTotalPowerShortageTime();
    
    /** Get time of last power shortage (in ticks) */
    long getLastPowerShortageTime();
    
    /** Get longest continuous power shortage (in ticks) */
    long getLongestPowerShortage();
    
    /** Check if currently experiencing power shortage */
    boolean isCurrentlyInPowerShortage();
    
    // === Uptime and Efficiency Statistics ===
    
    /** Get time spent at full power efficiency (in ticks) */
    long getTimeAtFullPowerEfficiency();
    
    /** Get active play time (time spent building/actively playing in ticks) */
    long getActivePlayTime();
    
    /** Get power efficiency percentage (0.0 to 100.0) */
    float getPowerEfficiencyPercentage();
    
    /** Get uptime percentage (active time / total time) */
    float getUptimePercentage();
    
    // === Building and Infrastructure Statistics ===
    
    /** Get number of generators destroyed by overheating/explosion */
    int getGeneratorsDestroyed();
    
    /** Get number of power nodes destroyed */
    int getPowerNodesDestroyed();
    
    /** Get total number of power networks created */
    int getPowerNetworksCreated();
    
    /** Get largest power network size (number of connected buildings) */
    int getLargestPowerNetworkSize();
    
    /** Get current number of active power networks */
    int getCurrentPowerNetworks();
    
    // === Core and Storage Statistics ===
    
    /** Get time spent at maximum core capacity (in ticks) */
    long getTimeAtMaxCoreCapacity();
    
    /** Get number of times core reached capacity */
    int getCoreCapacityReachedCount();
    
    /** Check if core is currently at capacity */
    boolean isCoreAtCapacity();
    
    /** Get current core storage percentage (0.0 to 100.0) */
    float getCoreStoragePercentage();
    
    // === Performance and Rate Statistics ===
    
    /** Get items per minute for a specific item */
    float getItemsPerMinute(Item item);
    
    /** Get power per minute (generation rate) */
    float getPowerPerMinute();
    
    /** Get buildings per minute (construction rate) */
    float getBuildingsPerMinute();
    
    /** Get resource efficiency (value mined per minute) */
    float getResourceEfficiencyPerMinute();
    
    // === Historical Data ===
    
    /** Get production history for an item (last N samples) */
    Seq<Float> getProductionHistory(Item item, int samples);
    
    /** Get power generation history (last N samples) */
    Seq<Float> getPowerHistory(int samples);
    
    /** Get efficiency history (last N samples) */
    Seq<Float> getEfficiencyHistory(int samples);
    
    // === Aggregated Statistics ===
    
    /** Get comprehensive economy summary */
    EconomySummary getEconomySummary();
    
    /** Get comprehensive power summary */
    PowerSummary getPowerSummary();
    
    /** Get comprehensive efficiency summary */
    EfficiencySummary getEfficiencySummary();
    
    /** Get overall game performance summary */
    GamePerformanceSummary getPerformanceSummary();
    
    // === Real-time Monitoring ===
    
    /** Register a callback for real-time statistics updates */
    void addStatsUpdateListener(StatsUpdateListener listener);
    
    /** Remove a statistics update listener */
    void removeStatsUpdateListener(StatsUpdateListener listener);
    
    /** Force an immediate statistics update */
    void forceStatsUpdate();
    
    /** Set statistics update interval (in ticks) */
    void setUpdateInterval(int ticks);
    
    // === Utility Methods ===
    
    /** Reset all statistics */
    void resetAllStats();
    
    /** Export statistics to JSON format */
    String exportStatsAsJSON();
    
    /** Export statistics to CSV format */
    String exportStatsAsCSV();
    
    /** Get statistics collection start time */
    long getStatsStartTime();
    
    // === Data Classes ===
    
    /** Basic game statistics container */
    class BasicGameStats {
        public int enemyUnitsDestroyed;
        public int wavesLasted;
        public int buildingsBuilt;
        public int buildingsDeconstructed;
        public int buildingsDestroyed;
        public int unitsCreated;
        public ObjectIntMap<Block> placedBlockCount;
        public ObjectIntMap<Item> coreItemCount;
    }
    
    /** Economy statistics summary */
    class EconomySummary {
        public long totalItemsProduced;
        public long totalItemsConsumed;
        public long totalResourceValue;
        public float averageProductionRate;
        public float averageConsumptionRate;
        public Item mostProducedItem;
        public Item mostConsumedItem;
        public float economyEfficiency; // production efficiency
    }
    
    /** Power statistics summary */
    class PowerSummary {
        public long totalGenerated;
        public long totalConsumed;
        public float currentGeneration;
        public float currentConsumption;
        public float efficiency;
        public float storageUtilization;
        public int shortageEvents;
        public long averageShortageTime;
    }
    
    /** Efficiency statistics summary */
    class EfficiencySummary {
        public float powerEfficiency;
        public float buildingEfficiency;
        public float resourceEfficiency;
        public float overallEfficiency;
        public long activeTime;
        public long totalTime;
        public float uptime;
    }
    
    /** Game performance summary */
    class GamePerformanceSummary {
        public BasicGameStats basicStats;
        public EconomySummary economy;
        public PowerSummary power;
        public EfficiencySummary efficiency;
        public long gameStartTime;
        public long currentTime;
        public int currentWave;
        public boolean isGameActive;
    }
    
    /** Listener interface for real-time statistics updates */
    interface StatsUpdateListener {
        /** Called when statistics are updated */
        void onStatsUpdate(GamePerformanceSummary summary);
    }
}

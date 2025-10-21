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
    
    // === Additional Resource and Production Statistics ===
    
    /** Get total resources produced/gathered across all items */
    long getTotalResourcesProduced();
    
    /** Get average resource throughput (items per second) */
    float getAverageResourceThroughput();
    
    /** Get number of production chains completed */
    int getProductionChainsCompleted();
    
    /** Get resources produced per tick */
    float getResourcesPerTick();
    
    /** Get items transported per tick across all conveyors/transport */
    float getItemsTransportedPerTick();
    
    /** Get average latency in production lines (ticks between input and output) */
    float getProductionLineLatency();
    
    /** Get output per building (average efficiency across all buildings) */
    float getOutputPerBuilding();
    
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
    
    // === Enhanced Power Statistics ===
    
    /** Get time spent with positive net power (generation > consumption) */
    long getTimeWithPositivePower();
    
    /** Get ratio of generated vs consumed power (generation/consumption) */
    float getPowerGenerationRatio();
    
    /** Get average power grid uptime percentage */
    float getAveragePowerGridUptime();
    
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
    
    // === Combat and Survival Statistics ===
    
    /** Get number of waves survived */
    int getWavesSurvived();
    
    /** Get total damage dealt by player structures/units */
    long getTotalDamageDealt();
    
    /** Get total damage received by player structures/units */
    long getTotalDamageReceived();
    
    /** Get percentage of structures destroyed (destroyed/total built) */
    float getStructureDestructionPercentage();
    
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
    
    /** Get comprehensive combat summary */
    CombatSummary getCombatSummary();
    
    /** Get comprehensive spatial summary */
    SpatialSummary getSpatialSummary();
    
    /** Get comprehensive transport summary */
    TransportSummary getTransportSummary();

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
    
    // === Spatial and Infrastructure Statistics ===
    
    /** Get area covered by structures (in tiles) */
    int getAreaCoveredByStructures();
    
    /** Get distance of furthest structure from core (in tiles) */
    float getFurthestStructureDistance();
    
    /** Get mean resource balance variance (how much resource levels fluctuate) */
    float getMeanResourceBalanceVariance();

    // === Resource Balance and Production Chain Tracking ===
    
    /** Get resource balance for each item (current storage) */
    ObjectIntMap<Item> getCurrentResourceBalance();
    
    /** Get production chain efficiency for complex items */
    ObjectFloatMap<Item> getProductionChainEfficiency();
    
    /** Get bottlenecks in production chains */
    Seq<ProductionBottleneck> getProductionBottlenecks();

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
        public int wavesSurvived;
        public long totalDamageDealt;
        public long totalDamageReceived;
        public float structureDestructionPercentage;
        public int areaCoveredByStructures;
        public float furthestStructureDistance;
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
        public long totalResourcesProduced;
        public float averageResourceThroughput;
        public int productionChainsCompleted;
        public float resourcesPerTick;
        public float itemsTransportedPerTick;
        public float productionLineLatency;
        public float outputPerBuilding;
        public float meanResourceBalanceVariance;
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
        public long timeWithPositivePower;
        public float powerGenerationRatio;
        public float averagePowerGridUptime;
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
        public CombatSummary combat;
        public SpatialSummary spatial;
        public TransportSummary transport;
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
    
    // === Game Outcome Tracking ===
    
    /** Game outcome information */
    class GameOutcome {
        public boolean gameEnded;
        public boolean victory;
        public String winnerTeam;
        public String endReason;
        public long gameEndTime;
        public int finalWave;
        public boolean controllerTeamWon;
        public String[] participatingTeams;
    }
    
    /** Get current game outcome information */
    GameOutcome getGameOutcome();
    
    /** Check if the game has ended */
    boolean hasGameEnded();
    
    /** Check if this was a victory for the controller's team */
    boolean didControllerTeamWin();
    
    /** Get the winning team name (null if game not ended) */
    String getWinnerTeam();
    
    /** Get the reason the game ended */
    String getGameEndReason();
    
    /** Listener interface for game outcome notifications */
    interface GameOutcomeListener {
        /** Called when the game ends */
        void onGameEnd(GameOutcome outcome);
        
        /** Called when the controller's team wins */
        void onControllerTeamVictory(GameOutcome outcome);
        
        /** Called when the controller's team loses */
        void onControllerTeamDefeat(GameOutcome outcome);
    }
    
    /** Add a game outcome listener */
    void addGameOutcomeListener(GameOutcomeListener listener);
    
    /** Remove a game outcome listener */
    void removeGameOutcomeListener(GameOutcomeListener listener);

    /** Production bottleneck information */
    class ProductionBottleneck {
        public Item item;
        public Block bottleneckBuilding;
        public float efficiency;
        public String reason;
        public int x, y; // coordinates of bottleneck
    }
    
    /** Combat statistics summary */
    class CombatSummary {
        public long totalDamageDealt;
        public long totalDamageReceived;
        public int wavesDefeated;
        public int enemyUnitsDestroyed;
        public int playerUnitsLost;
        public float damageRatio; // dealt/received
        public float structureSurvivalRate;
    }
    
    /** Spatial statistics summary */
    class SpatialSummary {
        public int areaCovered;
        public float furthestDistance;
        public int totalStructures;
        public float structureDensity;
        public float coreProximityScore;
        public int isolatedStructures;
    }
    
    /** Transport and logistics summary */
    class TransportSummary {
        public float itemsPerTick;
        public float averageLatency;
        public float transportEfficiency;
        public int totalConveyors;
        public int bottleneckPoints;
        public float throughputUtilization;
    }
}

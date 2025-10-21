package mindustry.game;

import arc.struct.*;
import mindustry.type.*;
import mindustry.world.*;

public class GameStats{
    /** Enemy (red team) units destroyed. */
    public int enemyUnitsDestroyed;
    /** Total waves lasted. */
    public int wavesLasted;
    /** Friendly buildings fully built. */
    public int buildingsBuilt;
    /** Friendly buildings fully deconstructed. */
    public int buildingsDeconstructed;
    /** Friendly buildings destroyed. */
    public int buildingsDestroyed;
    /** Total units created by any means. */
    public int unitsCreated;
    /** Record of blocks that have been placed by count. Used for objectives only. */
    public ObjectIntMap<Block> placedBlockCount = new ObjectIntMap<>();
    /**
     * Record of items that have entered the core through transport blocks. Used for objectives only.
     * This can easily be ""spoofed"" with unloaders, so don't use it for anything remotely important.
     * */
    public ObjectIntMap<Item> coreItemCount = new ObjectIntMap<>();
    
    // Economy statistics
    /** Total items produced by all production buildings. */
    public ObjectLongMap<Item> itemsProduced = new ObjectLongMap<>();
    /** Total items consumed by all consumer buildings. */
    public ObjectLongMap<Item> itemsConsumed = new ObjectLongMap<>();
    /** Peak item production rate per second for each item type. */
    public ObjectFloatMap<Item> peakItemProductionRate = new ObjectFloatMap<>();
    /** Peak item consumption rate per second for each item type. */
    public ObjectFloatMap<Item> peakItemConsumptionRate = new ObjectFloatMap<>();
    /** Total resource value mined (items * rarity). */
    public long totalResourceValueMined;
    /** Total items launched via launch pads. */
    public ObjectIntMap<Item> itemsLaunched = new ObjectIntMap<>();
    
    // Power statistics
    /** Total power generated in all time (in power units). */
    public long totalPowerGenerated;
    /** Total power consumed in all time (in power units). */
    public long totalPowerConsumed;
    /** Peak power generation rate per second. */
    public float peakPowerGeneration;
    /** Peak power consumption rate per second. */
    public float peakPowerConsumption;
    /** Total power stored in batteries at peak capacity. */
    public float maxPowerStored;
    /** Number of times power grid experienced shortages. */
    public int powerShortageCount;
    /** Total time spent in power shortage (in ticks). */
    public long totalPowerShortageTime;
    /** Time when last power shortage occurred. */
    public long lastPowerShortageTime;
    /** Longest continuous power shortage duration (in ticks). */
    public long longestPowerShortage;
    
    // Uptime and efficiency statistics
    /** Total game time played (in ticks). */
    public long totalGameTime;
    /** Time spent with full power grid efficiency (in ticks). */
    public long timeAtFullPowerEfficiency;
    /** Time spent building/actively playing (in ticks). */
    public long activePlayTime;
    /** Number of generator buildings destroyed by overheating/explosion. */
    public int generatorsDestroyed;
    /** Number of power nodes destroyed. */
    public int powerNodesDestroyed;
    /** Total number of power networks created. */
    public int powerNetworksCreated;
    /** Largest power network size (number of connected buildings). */
    public int largestPowerNetworkSize;
    
    // Economic efficiency metrics
    /** Total building cost (sum of all building requirements). */
    public ObjectLongMap<Item> totalBuildingCost = new ObjectLongMap<>();
    /** Time spent at maximum core capacity (in ticks). */
    public long timeAtMaxCoreCapacity;
    /** Number of times core was filled to capacity. */
    public int coreCapacityReachedCount;
}

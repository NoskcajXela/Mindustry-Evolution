package mindustry.ai.evolutionary.neural;

import arc.struct.*;
import arc.util.*;
import mindustry.api.*;
import mindustry.api.GameStatsAPI.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * Encodes game state into neural network inputs.
 * Normalizes all values to [0, 1] range for better network performance.
 */
public class GameStateEncoder {
    
    private static final int INPUT_SIZE = 64; // Fixed input vector size
    
    /**
     * Encode current game state into a normalized input vector for the neural network.
     */
    public static float[] encode(GameStatsAPI statsAPI, ControllerAPI controllerAPI) {
        float[] inputs = new float[INPUT_SIZE];
        int idx = 0;
        
        try {
            // === Basic Game Stats (8 values) ===
            var basicStats = statsAPI.getBasicStats();
            inputs[idx++] = normalize(statsAPI.getCurrentWave(), 0, 100);
            inputs[idx++] = normalize(statsAPI.getTotalGameTime(), 0, 36000); // Up to 10 minutes
            inputs[idx++] = normalize(basicStats.unitsCreated, 0, 200);
            inputs[idx++] = normalize(basicStats.buildingsBuilt, 0, 500);
            inputs[idx++] = normalize(basicStats.buildingsDestroyed, 0, 100);
            inputs[idx++] = normalize(basicStats.enemyUnitsDestroyed, 0, 500);
            inputs[idx++] = basicStats.buildingsBuilt > 0 ? 
                (float) basicStats.buildingsDestroyed / basicStats.buildingsBuilt : 0f;
            inputs[idx++] = statsAPI.hasGameEnded() ? 1f : 0f;
            
            // === Economy Stats (12 values) ===
            var economy = statsAPI.getEconomySummary();
            inputs[idx++] = normalize(economy.totalItemsProduced, 0, 10000);
            inputs[idx++] = normalize(economy.totalItemsConsumed, 0, 10000);
            inputs[idx++] = economy.economyEfficiency;
            inputs[idx++] = normalize(economy.averageProductionRate, 0, 100);
            inputs[idx++] = normalize(economy.averageConsumptionRate, 0, 100);
            inputs[idx++] = normalize(statsAPI.getTotalResourcesProduced(), 0, 5000);
            inputs[idx++] = normalize(statsAPI.getAverageResourceThroughput(), 0, 50);
            inputs[idx++] = normalize(statsAPI.getProductionChainsCompleted(), 0, 50);
            inputs[idx++] = normalize(statsAPI.getResourcesPerTick(), 0, 10);
            inputs[idx++] = normalize(statsAPI.getItemsTransportedPerTick(), 0, 50);
            inputs[idx++] = normalize(statsAPI.getProductionLineLatency(), 0, 10);
            inputs[idx++] = normalize(statsAPI.getOutputPerBuilding(), 0, 5);
            
            // === Power Stats (10 values) ===
            var power = statsAPI.getPowerSummary();
            inputs[idx++] = power.efficiency;
            inputs[idx++] = power.storageUtilization;
            inputs[idx++] = normalize(power.currentGeneration, 0, 1000);
            inputs[idx++] = normalize(power.currentConsumption, 0, 1000);
            inputs[idx++] = power.currentConsumption > 0 ? 
                power.currentGeneration / power.currentConsumption : 1f;
            inputs[idx++] = normalize(power.shortageEvents, 0, 20);
            inputs[idx++] = statsAPI.isCurrentlyInPowerShortage() ? 1f : 0f;
            inputs[idx++] = normalize(statsAPI.getTimeWithPositivePower(), 0, 36000);
            inputs[idx++] = statsAPI.getPowerGenerationRatio();
            inputs[idx++] = statsAPI.getAveragePowerGridUptime();
            
            // === Combat Stats (8 values) ===
            var combat = statsAPI.getCombatSummary();
            inputs[idx++] = normalize(combat.totalDamageDealt, 0, 50000);
            inputs[idx++] = normalize(combat.totalDamageReceived, 0, 50000);
            inputs[idx++] = combat.damageRatio;
            inputs[idx++] = normalize(combat.wavesDefeated, 0, 100);
            inputs[idx++] = normalize(combat.enemyUnitsDestroyed, 0, 500);
            inputs[idx++] = combat.structureSurvivalRate / 100f;
            inputs[idx++] = normalize(statsAPI.getWavesSurvived(), 0, 100);
            inputs[idx++] = statsAPI.getStructureDestructionPercentage() / 100f;
            
            // === Spatial & Infrastructure Stats (8 values) ===
            var spatial = statsAPI.getSpatialSummary();
            inputs[idx++] = normalize(spatial.areaCovered, 0, 10000);
            inputs[idx++] = normalize(spatial.furthestDistance, 0, 200);
            inputs[idx++] = normalize(spatial.totalStructures, 0, 500);
            inputs[idx++] = normalize(spatial.structureDensity, 0, 1);
            inputs[idx++] = spatial.coreProximityScore / 100f;
            inputs[idx++] = normalize(statsAPI.getMeanResourceBalanceVariance(), 0, 100);
            
            var transport = statsAPI.getTransportSummary();
            inputs[idx++] = transport.transportEfficiency;
            inputs[idx++] = transport.throughputUtilization;
            
            // === Resource Balance (12 values - key resources) ===
            var resourceBalance = statsAPI.getCurrentResourceBalance();
            inputs[idx++] = normalize(resourceBalance.get(Items.copper, 0), 0, 10000);
            inputs[idx++] = normalize(resourceBalance.get(Items.lead, 0), 0, 10000);
            inputs[idx++] = normalize(resourceBalance.get(Items.graphite, 0), 0, 5000);
            inputs[idx++] = normalize(resourceBalance.get(Items.titanium, 0), 0, 5000);
            inputs[idx++] = normalize(resourceBalance.get(Items.thorium, 0), 0, 3000);
            inputs[idx++] = normalize(resourceBalance.get(Items.silicon, 0), 0, 5000);
            inputs[idx++] = normalize(resourceBalance.get(Items.coal, 0), 0, 5000);
            inputs[idx++] = normalize(resourceBalance.get(Items.sand, 0), 0, 10000);
            inputs[idx++] = normalize(resourceBalance.get(Items.scrap, 0), 0, 5000);
            inputs[idx++] = normalize(resourceBalance.get(Items.metaglass, 0), 0, 3000);
            inputs[idx++] = normalize(resourceBalance.get(Items.plastanium, 0), 0, 2000);
            inputs[idx++] = normalize(resourceBalance.get(Items.surgeAlloy, 0), 0, 1000);
            
            // === Efficiency Stats (6 values) ===
            var efficiency = statsAPI.getEfficiencySummary();
            inputs[idx++] = efficiency.overallEfficiency;
            inputs[idx++] = efficiency.uptime;
            inputs[idx++] = efficiency.powerEfficiency;
            inputs[idx++] = efficiency.buildingEfficiency;
            inputs[idx++] = efficiency.resourceEfficiency;
            inputs[idx++] = normalize(statsAPI.getActivePlayTime(), 0, 36000);
            
        } catch (Exception e) {
            Log.warn("Error encoding game state: " + e.getMessage());
        }
        
        // Pad remaining with zeros if we're under INPUT_SIZE
        while (idx < INPUT_SIZE) {
            inputs[idx++] = 0f;
        }
        
        return inputs;
    }
    
    /**
     * Normalize a value to [0, 1] range.
     */
    private static float normalize(float value, float min, float max) {
        if (max == min) return 0.5f;
        float normalized = (value - min) / (max - min);
        return Math.max(0f, Math.min(1f, normalized));
    }
    
    /**
     * Normalize a long value to [0, 1] range.
     */
    private static float normalize(long value, long min, long max) {
        return normalize((float) value, (float) min, (float) max);
    }
    
    /**
     * Get the input size for the neural network.
     */
    public static int getInputSize() {
        return INPUT_SIZE;
    }
}

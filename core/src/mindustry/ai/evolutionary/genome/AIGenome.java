package mindustry.ai.evolutionary.genome;

import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * AI Genome representing the genetic encoding of an AI's behavior and strategies.
 * Contains genes for resource management, building priorities, combat behavior, and more.
 */
public class AIGenome {
    
    // === Building Priority Genes ===
    public final ObjectFloatMap<Block> buildingPriorities = new ObjectFloatMap<>();
    
    // === Resource Management Genes ===
    public float resourceConservation = 0.5f;     // How conservative with resources (0=wasteful, 1=conservative)
    public float expansionAggression = 0.5f;      // How quickly to expand (0=defensive, 1=aggressive)
    public float powerBufferTarget = 0.3f;        // Target power surplus ratio
    
    // === Combat Genes ===
    public float defensiveBias = 0.5f;            // Defensive vs aggressive combat (0=aggressive, 1=defensive)
    public float unitProductionPriority = 0.5f;   // Priority for unit production vs buildings
    public float combatRangePreference = 0.5f;    // Preference for ranged vs melee units
    
    // === Economic Genes ===
    public float miningFocus = 0.5f;              // Focus on mining vs production
    public float productionChainDepth = 0.5f;     // Preference for complex vs simple production
    public float transportEfficiency = 0.5f;      // Focus on transport optimization
    
    // === Strategic Genes ===
    public float adaptability = 0.5f;             // How quickly to adapt to situations
    public float riskTolerance = 0.5f;            // Willingness to take risks
    public float planningHorizon = 0.5f;          // Short-term vs long-term planning
    
    // === Timing Genes ===
    public float reactionSpeed = 0.5f;            // How quickly to react to threats
    public float buildingPacing = 0.5f;           // Speed of construction
    public float techProgression = 0.5f;          // Speed of technology advancement
    
    // === Spatial Organization Genes ===
    public float compactness = 0.5f;              // Compact vs spread-out base layout
    public float symmetryPreference = 0.5f;       // Preference for symmetric layouts
    public float centralizedStorage = 0.5f;       // Centralized vs distributed storage
    
    private AIGenome() {
        initializeBuildingPriorities();
    }
    
    /**
     * Create a genome with random values.
     */
    public static AIGenome createRandom() {
        var genome = new AIGenome();
        
        // Randomize all float genes
        genome.resourceConservation = randomGene();
        genome.expansionAggression = randomGene();
        genome.powerBufferTarget = randomGene();
        genome.defensiveBias = randomGene();
        genome.unitProductionPriority = randomGene();
        genome.combatRangePreference = randomGene();
        genome.miningFocus = randomGene();
        genome.productionChainDepth = randomGene();
        genome.transportEfficiency = randomGene();
        genome.adaptability = randomGene();
        genome.riskTolerance = randomGene();
        genome.planningHorizon = randomGene();
        genome.reactionSpeed = randomGene();
        genome.buildingPacing = randomGene();
        genome.techProgression = randomGene();
        genome.compactness = randomGene();
        genome.symmetryPreference = randomGene();
        genome.centralizedStorage = randomGene();
        
        // Randomize building priorities
        genome.randomizeBuildingPriorities();
        
        return genome;
    }
    
    /**
     * Create offspring through crossover of two parent genomes.
     */
    public static AIGenome crossover(AIGenome parent1, AIGenome parent2) {
        var child = new AIGenome();
        
        // Single-point crossover for float genes
        float crossoverPoint = (float) Math.random();
        
        child.resourceConservation = crossoverPoint < 0.5f ? parent1.resourceConservation : parent2.resourceConservation;
        child.expansionAggression = crossoverPoint < 0.5f ? parent1.expansionAggression : parent2.expansionAggression;
        child.powerBufferTarget = crossoverPoint < 0.5f ? parent1.powerBufferTarget : parent2.powerBufferTarget;
        child.defensiveBias = crossoverPoint < 0.5f ? parent1.defensiveBias : parent2.defensiveBias;
        child.unitProductionPriority = crossoverPoint < 0.5f ? parent1.unitProductionPriority : parent2.unitProductionPriority;
        child.combatRangePreference = crossoverPoint < 0.5f ? parent1.combatRangePreference : parent2.combatRangePreference;
        child.miningFocus = crossoverPoint < 0.5f ? parent1.miningFocus : parent2.miningFocus;
        child.productionChainDepth = crossoverPoint < 0.5f ? parent1.productionChainDepth : parent2.productionChainDepth;
        child.transportEfficiency = crossoverPoint < 0.5f ? parent1.transportEfficiency : parent2.transportEfficiency;
        child.adaptability = crossoverPoint < 0.5f ? parent1.adaptability : parent2.adaptability;
        child.riskTolerance = crossoverPoint < 0.5f ? parent1.riskTolerance : parent2.riskTolerance;
        child.planningHorizon = crossoverPoint < 0.5f ? parent1.planningHorizon : parent2.planningHorizon;
        child.reactionSpeed = crossoverPoint < 0.5f ? parent1.reactionSpeed : parent2.reactionSpeed;
        child.buildingPacing = crossoverPoint < 0.5f ? parent1.buildingPacing : parent2.buildingPacing;
        child.techProgression = crossoverPoint < 0.5f ? parent1.techProgression : parent2.techProgression;
        child.compactness = crossoverPoint < 0.5f ? parent1.compactness : parent2.compactness;
        child.symmetryPreference = crossoverPoint < 0.5f ? parent1.symmetryPreference : parent2.symmetryPreference;
        child.centralizedStorage = crossoverPoint < 0.5f ? parent1.centralizedStorage : parent2.centralizedStorage;
        
        // Crossover building priorities
        child.buildingPriorities.clear();
        for (var entry : parent1.buildingPriorities.entries()) {
            Block block = entry.key;
            float priority1 = entry.value;
            float priority2 = parent2.buildingPriorities.get(block, 0.5f);
            
            child.buildingPriorities.put(block, 
                Math.random() < 0.5f ? priority1 : priority2);
        }
        
        return child;
    }
    
    /**
     * Create a mutated copy of this genome.
     */
    public AIGenome mutate(float mutationRate) {
        var mutated = this.copy();
        
        // Mutate float genes
        if (Math.random() < mutationRate) mutated.resourceConservation = mutateFloat(mutated.resourceConservation);
        if (Math.random() < mutationRate) mutated.expansionAggression = mutateFloat(mutated.expansionAggression);
        if (Math.random() < mutationRate) mutated.powerBufferTarget = mutateFloat(mutated.powerBufferTarget);
        if (Math.random() < mutationRate) mutated.defensiveBias = mutateFloat(mutated.defensiveBias);
        if (Math.random() < mutationRate) mutated.unitProductionPriority = mutateFloat(mutated.unitProductionPriority);
        if (Math.random() < mutationRate) mutated.combatRangePreference = mutateFloat(mutated.combatRangePreference);
        if (Math.random() < mutationRate) mutated.miningFocus = mutateFloat(mutated.miningFocus);
        if (Math.random() < mutationRate) mutated.productionChainDepth = mutateFloat(mutated.productionChainDepth);
        if (Math.random() < mutationRate) mutated.transportEfficiency = mutateFloat(mutated.transportEfficiency);
        if (Math.random() < mutationRate) mutated.adaptability = mutateFloat(mutated.adaptability);
        if (Math.random() < mutationRate) mutated.riskTolerance = mutateFloat(mutated.riskTolerance);
        if (Math.random() < mutationRate) mutated.planningHorizon = mutateFloat(mutated.planningHorizon);
        if (Math.random() < mutationRate) mutated.reactionSpeed = mutateFloat(mutated.reactionSpeed);
        if (Math.random() < mutationRate) mutated.buildingPacing = mutateFloat(mutated.buildingPacing);
        if (Math.random() < mutationRate) mutated.techProgression = mutateFloat(mutated.techProgression);
        if (Math.random() < mutationRate) mutated.compactness = mutateFloat(mutated.compactness);
        if (Math.random() < mutationRate) mutated.symmetryPreference = mutateFloat(mutated.symmetryPreference);
        if (Math.random() < mutationRate) mutated.centralizedStorage = mutateFloat(mutated.centralizedStorage);
        
        // Mutate building priorities
        for (var entry : mutated.buildingPriorities.entries()) {
            if (Math.random() < mutationRate * 0.5f) { // Lower rate for building priorities
                Block block = entry.key;
                float oldPriority = entry.value;
                mutated.buildingPriorities.put(block, mutateFloat(oldPriority));
            }
        }
        
        return mutated;
    }
    
    /**
     * Create a deep copy of this genome.
     */
    public AIGenome copy() {
        var copy = new AIGenome();
        
        // Copy float genes
        copy.resourceConservation = this.resourceConservation;
        copy.expansionAggression = this.expansionAggression;
        copy.powerBufferTarget = this.powerBufferTarget;
        copy.defensiveBias = this.defensiveBias;
        copy.unitProductionPriority = this.unitProductionPriority;
        copy.combatRangePreference = this.combatRangePreference;
        copy.miningFocus = this.miningFocus;
        copy.productionChainDepth = this.productionChainDepth;
        copy.transportEfficiency = this.transportEfficiency;
        copy.adaptability = this.adaptability;
        copy.riskTolerance = this.riskTolerance;
        copy.planningHorizon = this.planningHorizon;
        copy.reactionSpeed = this.reactionSpeed;
        copy.buildingPacing = this.buildingPacing;
        copy.techProgression = this.techProgression;
        copy.compactness = this.compactness;
        copy.symmetryPreference = this.symmetryPreference;
        copy.centralizedStorage = this.centralizedStorage;
        
        // Copy building priorities
        copy.buildingPriorities.clear();
        copy.buildingPriorities.putAll(this.buildingPriorities);
        
        return copy;
    }
    
    /**
     * Get the priority for building a specific block type.
     */
    public float getBuildingPriority(Block block) {
        return buildingPriorities.get(block, 0.5f);
    }
    
    /**
     * Get the most prioritized building from a list of candidates.
     */
    public Block getHighestPriorityBuilding(Seq<Block> candidates) {
        Block best = null;
        float bestPriority = -1f;
        
        for (Block block : candidates) {
            float priority = getBuildingPriority(block);
            if (priority > bestPriority) {
                bestPriority = priority;
                best = block;
            }
        }
        
        return best;
    }
    
    /**
     * Calculate fitness bonus based on genome diversity (prevent premature convergence).
     */
    public float calculateDiversityBonus(Seq<AIGenome> population) {
        if (population.size <= 1) return 0f;
        
        float totalDistance = 0f;
        int comparisons = 0;
        
        for (AIGenome other : population) {
            if (other != this) {
                totalDistance += calculateDistance(other);
                comparisons++;
            }
        }
        
        float averageDistance = totalDistance / comparisons;
        return averageDistance * 10f; // Scale diversity bonus
    }
    
    private void initializeBuildingPriorities() {
        // Initialize with default priorities for all important blocks
        buildingPriorities.put(Blocks.coreNucleus, 0.9f);
        buildingPriorities.put(Blocks.coreFortress, 0.85f);
        buildingPriorities.put(Blocks.coreBastion, 0.8f);
        
        // Drills and mining
        buildingPriorities.put(Blocks.mechanicalDrill, 0.7f);
        buildingPriorities.put(Blocks.pneumaticDrill, 0.75f);
        buildingPriorities.put(Blocks.laserDrill, 0.85f);
        buildingPriorities.put(Blocks.blastDrill, 0.9f);
        
        // Power generation
        buildingPriorities.put(Blocks.combustionGenerator, 0.6f);
        buildingPriorities.put(Blocks.steamGenerator, 0.7f);
        buildingPriorities.put(Blocks.differentialGenerator, 0.75f);
        buildingPriorities.put(Blocks.rtgGenerator, 0.8f);
        buildingPriorities.put(Blocks.solarPanel, 0.65f);
        buildingPriorities.put(Blocks.largeSolarPanel, 0.75f);
        
        // Production buildings
        buildingPriorities.put(Blocks.graphitePress, 0.65f);
        buildingPriorities.put(Blocks.multiPress, 0.7f);
        buildingPriorities.put(Blocks.siliconSmelter, 0.7f);
        buildingPriorities.put(Blocks.siliconCrucible, 0.75f);
        buildingPriorities.put(Blocks.kiln, 0.6f);
        buildingPriorities.put(Blocks.plastaniumCompressor, 0.8f);
        buildingPriorities.put(Blocks.phaseWeaver, 0.85f);
        
        // Defense
        buildingPriorities.put(Blocks.duo, 0.6f);
        buildingPriorities.put(Blocks.scatter, 0.65f);
        buildingPriorities.put(Blocks.scorch, 0.6f);
        buildingPriorities.put(Blocks.hail, 0.7f);
        buildingPriorities.put(Blocks.wave, 0.7f);
        buildingPriorities.put(Blocks.lancer, 0.75f);
        buildingPriorities.put(Blocks.arc, 0.7f);
        buildingPriorities.put(Blocks.parallax, 0.75f);
        buildingPriorities.put(Blocks.swarmer, 0.8f);
        buildingPriorities.put(Blocks.salvo, 0.75f);
        buildingPriorities.put(Blocks.segment, 0.8f);
        buildingPriorities.put(Blocks.tsunami, 0.85f);
        buildingPriorities.put(Blocks.fuse, 0.85f);
        buildingPriorities.put(Blocks.ripple, 0.9f);
        buildingPriorities.put(Blocks.cyclone, 0.9f);
        buildingPriorities.put(Blocks.foreshadow, 0.95f);
        buildingPriorities.put(Blocks.spectre, 0.95f);
        
        // Transport
        buildingPriorities.put(Blocks.conveyor, 0.5f);
        buildingPriorities.put(Blocks.titaniumConveyor, 0.6f);
        buildingPriorities.put(Blocks.plastaniumConveyor, 0.7f);
        buildingPriorities.put(Blocks.armoredConveyor, 0.75f);
        buildingPriorities.put(Blocks.junction, 0.55f);
        buildingPriorities.put(Blocks.itemBridge, 0.6f);
        buildingPriorities.put(Blocks.phaseConveyor, 0.8f);
        buildingPriorities.put(Blocks.massDriver, 0.85f);
        
        // Storage
        buildingPriorities.put(Blocks.container, 0.5f);
        buildingPriorities.put(Blocks.vault, 0.6f);
        
        // Factories
        buildingPriorities.put(Blocks.groundFactory, 0.7f);
        buildingPriorities.put(Blocks.airFactory, 0.75f);
        buildingPriorities.put(Blocks.navalFactory, 0.7f);
        buildingPriorities.put(Blocks.additiveReconstructor, 0.8f);
        buildingPriorities.put(Blocks.multiplicativeReconstructor, 0.85f);
        buildingPriorities.put(Blocks.exponentialReconstructor, 0.9f);
        buildingPriorities.put(Blocks.tetrativeReconstructor, 0.95f);
        
        // Power distribution
        buildingPriorities.put(Blocks.powerNode, 0.6f);
        buildingPriorities.put(Blocks.powerNodeLarge, 0.65f);
        buildingPriorities.put(Blocks.surgeTower, 0.7f);
        buildingPriorities.put(Blocks.diode, 0.55f);
        
        // Utility
        buildingPriorities.put(Blocks.repairPoint, 0.6f);
        buildingPriorities.put(Blocks.repairTurret, 0.7f);
        buildingPriorities.put(Blocks.mendProjector, 0.75f);
        buildingPriorities.put(Blocks.overdriveProjector, 0.8f);
        buildingPriorities.put(Blocks.overdriveDome, 0.85f);
        buildingPriorities.put(Blocks.forceProjector, 0.9f);
        buildingPriorities.put(Blocks.shockMine, 0.5f);
    }
    
    private void randomizeBuildingPriorities() {
        // Add some randomness to building priorities while keeping them reasonable
        for (var entry : buildingPriorities.entries()) {
            Block block = entry.key;
            float basePriority = entry.value;
            
            // Vary priority by ±20% from base value
            float variation = (float) (Math.random() - 0.5) * 0.4f;
            float newPriority = Math.max(0f, Math.min(1f, basePriority + variation));
            
            buildingPriorities.put(block, newPriority);
        }
    }
    
    private static float randomGene() {
        return (float) Math.random();
    }
    
    private float mutateFloat(float value) {
        // Gaussian mutation with 0.1 standard deviation
        float mutation = (float) (Math.random() * 0.2 - 0.1);
        return Math.max(0f, Math.min(1f, value + mutation));
    }
    
    private float calculateDistance(AIGenome other) {
        float distance = 0f;
        
        // Calculate Euclidean distance between genomes
        distance += Math.pow(this.resourceConservation - other.resourceConservation, 2);
        distance += Math.pow(this.expansionAggression - other.expansionAggression, 2);
        distance += Math.pow(this.powerBufferTarget - other.powerBufferTarget, 2);
        distance += Math.pow(this.defensiveBias - other.defensiveBias, 2);
        distance += Math.pow(this.unitProductionPriority - other.unitProductionPriority, 2);
        distance += Math.pow(this.combatRangePreference - other.combatRangePreference, 2);
        distance += Math.pow(this.miningFocus - other.miningFocus, 2);
        distance += Math.pow(this.productionChainDepth - other.productionChainDepth, 2);
        distance += Math.pow(this.transportEfficiency - other.transportEfficiency, 2);
        distance += Math.pow(this.adaptability - other.adaptability, 2);
        distance += Math.pow(this.riskTolerance - other.riskTolerance, 2);
        distance += Math.pow(this.planningHorizon - other.planningHorizon, 2);
        distance += Math.pow(this.reactionSpeed - other.reactionSpeed, 2);
        distance += Math.pow(this.buildingPacing - other.buildingPacing, 2);
        distance += Math.pow(this.techProgression - other.techProgression, 2);
        distance += Math.pow(this.compactness - other.compactness, 2);
        distance += Math.pow(this.symmetryPreference - other.symmetryPreference, 2);
        distance += Math.pow(this.centralizedStorage - other.centralizedStorage, 2);
        
        return (float) Math.sqrt(distance);
    }
    
    @Override
    public String toString() {
        return String.format("AIGenome{resource=%.2f, expansion=%.2f, power=%.2f, defensive=%.2f, " +
                           "unitProd=%.2f, combat=%.2f, mining=%.2f, production=%.2f, transport=%.2f, " +
                           "adapt=%.2f, risk=%.2f, planning=%.2f, reaction=%.2f, building=%.2f, " +
                           "tech=%.2f, compact=%.2f, symmetry=%.2f, storage=%.2f}",
                resourceConservation, expansionAggression, powerBufferTarget, defensiveBias,
                unitProductionPriority, combatRangePreference, miningFocus, productionChainDepth,
                transportEfficiency, adaptability, riskTolerance, planningHorizon, reactionSpeed,
                buildingPacing, techProgression, compactness, symmetryPreference, centralizedStorage);
    }
}

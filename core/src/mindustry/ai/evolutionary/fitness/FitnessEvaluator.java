package mindustry.ai.evolutionary.fitness;

import arc.util.*;
import mindustry.ai.evolutionary.behavior.GenomeBasedAI.*;
import mindustry.ai.evolutionary.genome.*;
import mindustry.api.GameStatsAPI.*;

/**
 * Interface for evaluating AI fitness based on game performance.
 */
public interface FitnessEvaluator {
    
    /**
     * Evaluate the fitness of an AI based on its game performance.
     * 
     * @param gameOutcome The outcome of the game
     * @param performance Comprehensive performance statistics
     * @param genome The AI's genome
     * @param behaviorStats Statistics about the AI's behavior
     * @return Fitness score (higher is better, typically 0-1000)
     */
    float evaluateFitness(GameOutcome gameOutcome, 
                         GamePerformanceSummary performance,
                         AIGenome genome,
                         BehaviorStats behaviorStats);
}

/**
 * Comprehensive fitness evaluator that considers multiple aspects of game performance.
 */
class ComprehensiveFitnessEvaluator implements FitnessEvaluator {
    
    // Fitness weight constants
    private static final float VICTORY_WEIGHT = 400f;        // Victory is most important
    private static final float SURVIVAL_WEIGHT = 200f;      // Surviving longer is good
    private static final float ECONOMY_WEIGHT = 150f;       // Economic efficiency matters
    private static final float POWER_WEIGHT = 100f;         // Power management is important
    private static final float DEFENSE_WEIGHT = 80f;        // Defense effectiveness
    private static final float EFFICIENCY_WEIGHT = 50f;     // Overall efficiency
    private static final float ADAPTABILITY_WEIGHT = 20f;   // Behavioral adaptability
    
    @Override
    public float evaluateFitness(GameOutcome gameOutcome, 
                               GamePerformanceSummary performance,
                               AIGenome genome,
                               BehaviorStats behaviorStats) {
        
        float fitness = 0f;
        
        try {
            // 1. Victory/Defeat Component (0-400 points)
            fitness += evaluateVictoryComponent(gameOutcome, performance);
            
            // 2. Survival Component (0-200 points)  
            fitness += evaluateSurvivalComponent(performance);
            
            // 3. Economic Component (0-150 points)
            fitness += evaluateEconomicComponent(performance.economy);
            
            // 4. Power Management Component (0-100 points)
            fitness += evaluatePowerComponent(performance.power);
            
            // 5. Defense Component (0-80 points)
            fitness += evaluateDefenseComponent(performance.combat);
            
            // 6. Efficiency Component (0-50 points)
            fitness += evaluateEfficiencyComponent(performance.efficiency);
            
            // 7. Adaptability Component (0-20 points)
            fitness += evaluateAdaptabilityComponent(behaviorStats, genome);
            
            // Apply penalties for failures
            fitness -= evaluatePenalties(behaviorStats, performance);
            
            // Ensure fitness is within bounds
            fitness = Math.max(0f, Math.min(1000f, fitness));
            
        } catch (Exception e) {
            Log.warn("Fitness evaluation failed: " + e.getMessage());
            fitness = 0f; // Assign worst fitness on error
        }
        
        return fitness;
    }
    
    private float evaluateVictoryComponent(GameOutcome gameOutcome, GamePerformanceSummary performance) {
        if (!gameOutcome.gameEnded) {
            // Game didn't end - moderate score based on progress
            return Math.min(100f, performance.currentWave * 2f);
        }
        
        if (gameOutcome.controllerTeamWon) {
            // Victory! Full points plus bonus for quick victory
            float victoryBonus = Math.max(0f, (60f - performance.currentWave) * 2f);
            return VICTORY_WEIGHT + victoryBonus;
        } else {
            // Defeat - some points for survival time
            return Math.min(50f, performance.currentWave * 1f);
        }
    }
    
    private float evaluateSurvivalComponent(GamePerformanceSummary performance) {
        // Points for surviving waves and lasting longer
        float wavePoints = Math.min(100f, performance.currentWave * 2f);
        float timePoints = Math.min(100f, performance.currentTime / 3600f); // Points per minute
        
        return wavePoints + timePoints;
    }
    
    private float evaluateEconomicComponent(EconomySummary economy) {
        float economyScore = 0f;
        
        // Economic efficiency (0-50 points)
        economyScore += economy.economyEfficiency * 50f;
        
        // Resource production rate (0-40 points)
        economyScore += Math.min(40f, economy.averageProductionRate * 4f);
        
        // Resource diversity bonus (0-30 points)
        if (economy.totalItemsProduced > 1000) {
            economyScore += 30f;
        } else if (economy.totalItemsProduced > 100) {
            economyScore += 15f;
        }
        
        // Advanced resource bonus (0-30 points)
        if (economy.totalResourcesProduced > 5000) {
            economyScore += 30f;
        } else if (economy.totalResourcesProduced > 1000) {
            economyScore += 15f;
        }
        
        return Math.min(ECONOMY_WEIGHT, economyScore);
    }
    
    private float evaluatePowerComponent(PowerSummary power) {
        float powerScore = 0f;
        
        // Power efficiency (0-40 points)
        powerScore += power.efficiency * 40f;
        
        // Power stability - fewer shortages is better (0-30 points)
        if (power.shortageEvents == 0) {
            powerScore += 30f;
        } else if (power.shortageEvents < 3) {
            powerScore += 20f;
        } else if (power.shortageEvents < 10) {
            powerScore += 10f;
        }
        
        // Power generation capacity (0-30 points)
        float generationRatio = power.totalGenerated > 0 ? 
            power.currentGeneration / Math.max(1f, power.currentConsumption) : 0f;
        powerScore += Math.min(30f, generationRatio * 15f);
        
        return Math.min(POWER_WEIGHT, powerScore);
    }
    
    private float evaluateDefenseComponent(CombatSummary combat) {
        float defenseScore = 0f;
        
        // Damage ratio - dealing more damage than received (0-30 points)
        if (combat.damageRatio > 2f) {
            defenseScore += 30f;
        } else if (combat.damageRatio > 1f) {
            defenseScore += combat.damageRatio * 15f;
        }
        
        // Enemy units destroyed (0-25 points)
        defenseScore += Math.min(25f, combat.enemyUnitsDestroyed * 0.5f);
        
        // Structure survival rate (0-25 points)
        defenseScore += combat.structureSurvivalRate * 0.25f;
        
        return Math.min(DEFENSE_WEIGHT, defenseScore);
    }
    
    private float evaluateEfficiencyComponent(EfficiencySummary efficiency) {
        float efficiencyScore = 0f;
        
        // Overall efficiency (0-25 points)
        efficiencyScore += efficiency.overallEfficiency * 25f;
        
        // Building efficiency (0-15 points)
        efficiencyScore += efficiency.buildingEfficiency * 15f;
        
        // Uptime (0-10 points)
        efficiencyScore += efficiency.uptime * 10f;
        
        return Math.min(EFFICIENCY_WEIGHT, efficiencyScore);
    }
    
    private float evaluateAdaptabilityComponent(BehaviorStats behaviorStats, AIGenome genome) {
        float adaptabilityScore = 0f;
        
        // Build success rate (0-10 points)
        adaptabilityScore += behaviorStats.getBuildSuccessRate() * 10f;
        
        // Behavior diversity - different types of buildings (0-10 points)
        int buildingTypes = 0;
        if (behaviorStats.miningBuilt > 0) buildingTypes++;
        if (behaviorStats.powerBuilt > 0) buildingTypes++;
        if (behaviorStats.defenseBuilt > 0) buildingTypes++;
        if (behaviorStats.transportBuilt > 0) buildingTypes++;
        
        adaptabilityScore += buildingTypes * 2.5f;
        
        return Math.min(ADAPTABILITY_WEIGHT, adaptabilityScore);
    }
    
    private float evaluatePenalties(BehaviorStats behaviorStats, GamePerformanceSummary performance) {
        float penalties = 0f;
        
        // Error penalty
        penalties += behaviorStats.errorCount * 5f;
        
        // Build failure penalty
        penalties += behaviorStats.buildFailures * 2f;
        
        // Stagnation penalty - if very few buildings were built for the time
        long gameTimeMinutes = performance.currentTime / 3600; // Convert to minutes
        if (gameTimeMinutes > 10 && behaviorStats.totalBuilt < gameTimeMinutes) {
            penalties += (gameTimeMinutes - behaviorStats.totalBuilt) * 1f;
        }
        
        return penalties;
    }
}

/**
 * Simple fitness evaluator focused mainly on survival and victory.
 */
class SimpleFitnessEvaluator implements FitnessEvaluator {
    
    @Override
    public float evaluateFitness(GameOutcome gameOutcome, 
                               GamePerformanceSummary performance,
                               AIGenome genome,
                               BehaviorStats behaviorStats) {
        
        float fitness = 0f;
        
        // Victory is worth 500 points
        if (gameOutcome.controllerTeamWon) {
            fitness += 500f;
        }
        
        // Survival points based on waves survived
        fitness += performance.currentWave * 5f;
        
        // Basic economic performance
        fitness += performance.economy.economyEfficiency * 100f;
        
        // Power management
        fitness += performance.power.efficiency * 50f;
        
        // Cap at 1000
        return Math.min(1000f, fitness);
    }
}

/**
 * Combat-focused fitness evaluator for training aggressive AIs.
 */
class CombatFitnessEvaluator implements FitnessEvaluator {
    
    @Override
    public float evaluateFitness(GameOutcome gameOutcome, 
                               GamePerformanceSummary performance,
                               AIGenome genome,
                               BehaviorStats behaviorStats) {
        
        float fitness = 0f;
        
        // Victory bonus
        if (gameOutcome.controllerTeamWon) {
            fitness += 400f;
        }
        
        // Combat effectiveness (most important)
        fitness += performance.combat.enemyUnitsDestroyed * 2f;
        fitness += performance.combat.damageRatio * 100f;
        fitness += performance.combat.structureSurvivalRate * 2f;
        
        // Defense building bonus
        fitness += behaviorStats.defenseBuilt * 10f;
        
        // Wave survival
        fitness += performance.currentWave * 3f;
        
        return Math.min(1000f, fitness);
    }
}

/**
 * Economic-focused fitness evaluator for training resource-efficient AIs.
 */
class EconomicFitnessEvaluator implements FitnessEvaluator {
    
    @Override
    public float evaluateFitness(GameOutcome gameOutcome, 
                               GamePerformanceSummary performance,
                               AIGenome genome,
                               BehaviorStats behaviorStats) {
        
        float fitness = 0f;
        
        // Victory bonus
        if (gameOutcome.controllerTeamWon) {
            fitness += 300f;
        }
        
        // Economic performance (most important)
        fitness += performance.economy.economyEfficiency * 200f;
        fitness += performance.economy.averageProductionRate * 5f;
        fitness += Math.min(200f, performance.economy.totalResourcesProduced / 50f);
        
        // Resource building bonus
        fitness += behaviorStats.miningBuilt * 15f;
        fitness += behaviorStats.transportBuilt * 10f;
        
        // Power efficiency
        fitness += performance.power.efficiency * 100f;
        
        return Math.min(1000f, fitness);
    }
}

package mindustry.ai.evolutionary.examples;

import arc.util.*;
import mindustry.ai.evolutionary.*;
import mindustry.ai.evolutionary.genome.*;
import mindustry.api.*;
import mindustry.api.impl.*;
import mindustry.api.HeadlessControllerLauncher;

/**
 * Example demonstrating how to use the Evolutionary AI system to train an AI to play Mindustry.
 */
public class EvolutionaryAIExample {
    
    private ControllerAPI controllerAPI;
    private EvolutionaryAI evolutionaryAI;
    
    public static void main(String[] args) {
        new EvolutionaryAIExample().run();
    }
    
    public void run() {
        Log.info("Starting Evolutionary AI Example...");
        
        try {
            // 1. Initialize the headless controller
            initializeController();
            
            // 2. Create and configure the evolutionary AI
            setupEvolutionaryAI();
            
            // 3. Run the evolution process
            AIGenome bestGenome = runEvolution();
            
            // 4. Test the best AI
            testBestAI(bestGenome);
            
            // 5. Demonstrate the AI playing
            demonstrateBestAI(bestGenome);
            
            Log.info("Evolutionary AI example completed successfully!");
            
        } catch (Exception e) {
            Log.err("Evolutionary AI example failed", e);
        } finally {
            cleanup();
        }
    }
    
    private void initializeController() {
        Log.info("=== Initializing Headless Controller ===");
        
        try {
            // Start headless controller
            var launcher = HeadlessControllerLauncher.create();
            new arc.backend.headless.HeadlessApplication(launcher);
            
            // Wait for initialization
            launcher.waitForInitialization();
            
            // Get Controller API
            controllerAPI = launcher.getControllerAPI();
            
            Log.info("Controller API initialized successfully");
            Log.info("Available maps: " + controllerAPI.getAvailableMaps().size);
            
        } catch (Exception e) {
            Log.err("Failed to initialize controller", e);
            throw new RuntimeException("Controller initialization failed", e);
        }
    }
    
    private void setupEvolutionaryAI() {
        Log.info("=== Setting up Evolutionary AI ===");
        
        evolutionaryAI = new EvolutionaryAI(controllerAPI);
        
        Log.info("Evolutionary AI created with:");
        Log.info("  Population size: 20");
        Log.info("  Max generations: 100");
        Log.info("  Mutation rate: 15%");
        Log.info("  Crossover rate: 70%");
    }
    
    private AIGenome runEvolution() {
        Log.info("=== Running Evolution Process ===");
        Log.info("This may take several hours depending on your hardware...");
        
        // Set up progress monitoring
        var progressMonitor = new Thread(() -> {
            while (evolutionaryAI.getCurrentGeneration() < 100) {
                try {
                    Thread.sleep(60000); // Check every minute
                    
                    int generation = evolutionaryAI.getCurrentGeneration();
                    float bestFitness = evolutionaryAI.getBestFitness();
                    
                    Log.info("Evolution Progress - Generation: " + generation + 
                            ", Best Fitness: " + String.format("%.2f", bestFitness));
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        progressMonitor.setDaemon(true);
        progressMonitor.start();
        
        // Run evolution
        AIGenome bestGenome = evolutionaryAI.evolve();
        
        progressMonitor.interrupt();
        
        Log.info("Evolution completed!");
        Log.info("Best genome fitness: " + String.format("%.2f", evolutionaryAI.getBestFitness()));
        Log.info("Best genome: " + bestGenome.toString());
        
        return bestGenome;
    }
    
    private void testBestAI(AIGenome bestGenome) {
        Log.info("=== Testing Best AI ===");
        
        // Test on multiple maps
        var testMaps = arc.struct.Seq.with(
            "Ancient Caldera",
            "Frozen Forest", 
            "Biomass Synthesis Facility",
            "Craters",
            "Ruinous Shores"
        );
        
        Log.info("Testing AI on " + testMaps.size + " different maps...");
        
        var testResults = evolutionaryAI.testBestAI(testMaps);
        
        Log.info("Test Results:");
        Log.info("  Overall Win Rate: " + String.format("%.1f%%", testResults.getWinRate() * 100));
        Log.info("  Average Fitness: " + String.format("%.2f", testResults.getAverageFitness()));
        
        // Show detailed results for each map
        for (var entry : testResults.getAllResults().entries()) {
            String mapName = entry.key;
            var result = entry.value;
            
            Log.info("  " + mapName + ": " + 
                    (result.won ? "WON" : "LOST") + 
                    " (Wave " + result.finalWave + 
                    ", Fitness: " + String.format("%.2f", result.fitness) + ")");
        }
    }
    
    private void demonstrateBestAI(AIGenome bestGenome) {
        Log.info("=== Demonstrating Best AI in Action ===");
        
        try {
            // Start a demonstration game
            var maps = controllerAPI.getAvailableMaps();
            if (maps.isEmpty()) {
                Log.warn("No maps available for demonstration");
                return;
            }
            
            var map = maps.first();
            var rules = new mindustry.game.Rules();
            rules.waveSpacing = 45f; // Slower waves for demonstration
            rules.enemyCoreBuildRadius = 200f;
            
            // Start game
            var gameController = controllerAPI.startGame(map, rules);
            var aiPlayer = controllerAPI.createPlayer("DemonstrateAI", mindustry.game.Team.sharded);
            
            // Create AI behavior
            var aiBehavior = new mindustry.ai.evolutionary.behavior.GenomeBasedAI(
                aiPlayer, bestGenome, controllerAPI, controllerAPI.getGameStats());
            
            Log.info("Demonstration started on map: " + map.plainName());
            Log.info("Watching AI play for 10 minutes or until game ends...");
            
            long startTime = System.currentTimeMillis();
            long maxDemoTime = 10 * 60 * 1000L; // 10 minutes
            
            // Monitor the game
            while (!gameController.isGameOver() && 
                   (System.currentTimeMillis() - startTime) < maxDemoTime) {
                
                // Update AI
                aiBehavior.update();
                
                // Log progress every 30 seconds
                if ((System.currentTimeMillis() - startTime) % 30000 < 1000) {
                    var stats = controllerAPI.getGameStats().getPerformanceSummary();
                    Log.info("Demo Progress - Wave: " + stats.currentWave + 
                            ", Power: " + String.format("%.1f%%", stats.power.efficiency * 100) +
                            ", Economy: " + String.format("%.1f%%", stats.economy.economyEfficiency * 100));
                    
                    var behaviorStats = aiBehavior.getBehaviorStats();
                    Log.info("AI Activity - Built: " + behaviorStats.totalBuilt + 
                            ", Mining: " + behaviorStats.miningBuilt +
                            ", Defense: " + behaviorStats.defenseBuilt +
                            ", Success Rate: " + String.format("%.1f%%", behaviorStats.getBuildSuccessRate() * 100));
                }
                
                Thread.sleep(1000); // 1 second update rate
            }
            
            // Final results
            var gameOutcome = controllerAPI.getGameStats().getGameOutcome();
            var finalStats = controllerAPI.getGameStats().getPerformanceSummary();
            
            Log.info("Demonstration completed!");
            Log.info("Final Results:");
            Log.info("  Game Ended: " + gameOutcome.gameEnded);
            if (gameOutcome.gameEnded) {
                Log.info("  Victory: " + gameOutcome.controllerTeamWon);
                Log.info("  Final Wave: " + gameOutcome.finalWave);
                Log.info("  End Reason: " + gameOutcome.endReason);
            }
            Log.info("  Current Wave: " + finalStats.currentWave);
            Log.info("  Game Time: " + String.format("%.1f", finalStats.currentTime / 3600f) + " minutes");
            Log.info("  Power Efficiency: " + String.format("%.1f%%", finalStats.power.efficiency * 100));
            Log.info("  Economy Efficiency: " + String.format("%.1f%%", finalStats.economy.economyEfficiency * 100));
            
        } catch (Exception e) {
            Log.err("Demonstration failed", e);
        }
    }
    
    private void cleanup() {
        Log.info("Cleaning up...");
        
        if (controllerAPI != null) {
            try {
                controllerAPI.shutdown();
            } catch (Exception e) {
                Log.warn("Error during controller cleanup: " + e.getMessage());
            }
        }
    }
    
    /**
     * Run a quick evolution example with fewer generations for testing.
     */
    public void runQuickExample() {
        Log.info("Running quick evolution example (10 generations)...");
        
        try {
            initializeController();
            
            var quickEvolution = new QuickEvolutionaryAI(controllerAPI);
            AIGenome bestGenome = quickEvolution.evolve();
            
            Log.info("Quick evolution completed!");
            Log.info("Best fitness: " + String.format("%.2f", quickEvolution.getBestFitness()));
            
        } catch (Exception e) {
            Log.err("Quick example failed", e);
        } finally {
            cleanup();
        }
    }
}

/**
 * Quick version of evolutionary AI for testing and demonstrations.
 */
class QuickEvolutionaryAI extends EvolutionaryAI {
    
    private static final int QUICK_POPULATION_SIZE = 8;
    private static final int QUICK_GENERATIONS = 10;
    private static final int QUICK_GAMES_PER_INDIVIDUAL = 1;
    private static final int QUICK_MAX_GAME_TIME_MINUTES = 10;
    
    public QuickEvolutionaryAI(ControllerAPI controllerAPI) {
        super(controllerAPI);
    }
    
    // Override evolution parameters for quick testing
    // (Implementation would modify the constants and evaluation process)
}

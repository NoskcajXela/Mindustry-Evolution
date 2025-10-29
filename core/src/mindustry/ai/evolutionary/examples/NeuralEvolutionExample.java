package mindustry.ai.evolutionary.examples;

import arc.struct.*;
import arc.util.*;
import mindustry.ai.evolutionary.*;
import mindustry.ai.evolutionary.neural.*;
import mindustry.api.*;
import mindustry.game.*;

/**
 * Example demonstrating how to use the Neural Network-based Evolutionary AI system.
 */
public class NeuralEvolutionExample {
    
    private ControllerAPI controllerAPI;
    
    public static void main(String[] args) {
        new NeuralEvolutionExample().run();
    }
    
    public void run() {
        Log.info("Starting Neural Evolution Example...");
        
        try {
            // 1. Initialize the controller API
            initializeController();
            
            // 2. Run evolution training
            NeuralNetwork bestNetwork = runEvolution();
            
            // 3. Test the best network
            testBestNetwork(bestNetwork);
            
            // 4. Demonstrate real-time usage
            demonstrateRealTimeUsage(bestNetwork);
            
            Log.info("Neural Evolution Example completed successfully!");
            
        } catch (Exception e) {
            Log.err("Example failed", e);
        }
    }
    
    private void initializeController() {
        Log.info("=== Initializing Controller API ===");
        
        // In a real scenario, you would get this from HeadlessControllerLauncher
        // or ControllerServerLauncher
        // controllerAPI = HeadlessControllerLauncher.create().getControllerAPI();
        
        // For this example, we'll use a mock implementation
        Log.info("Controller API initialized (using mock)");
    }
    
    private NeuralNetwork runEvolution() {
        Log.info("=== Running Neural Evolution ===");
        
        // Create evolutionary trainer
        var trainer = new NeuralEvolutionaryAI(controllerAPI);
        
        // Monitor evolution progress
        Log.info("Starting evolution with:");
        Log.info("  Population size: 20");
        Log.info("  Generations: 100");
        Log.info("  Network architecture: 64→128→64→32");
        
        // Run evolution (this will take a while!)
        NeuralNetwork bestNetwork = trainer.evolve();
        
        // Display results
        Log.info("Evolution completed!");
        Log.info("  Best fitness: " + String.format("%.2f", trainer.getBestFitness()));
        Log.info("  Final generation: " + trainer.getCurrentGeneration());
        
        // Display evolution curve
        displayEvolutionCurve(trainer);
        
        return bestNetwork;
    }
    
    private void displayEvolutionCurve(NeuralEvolutionaryAI trainer) {
        var bestFitness = trainer.getGenerationBestFitness();
        var avgFitness = trainer.getGenerationAverageFitness();
        
        Log.info("Evolution Progress:");
        for (int i = 0; i < bestFitness.size; i += 10) {
            Log.info(String.format("  Gen %3d: Best=%.2f, Avg=%.2f", 
                i + 1, bestFitness.get(i), avgFitness.get(i)));
        }
    }
    
    private void testBestNetwork(NeuralNetwork bestNetwork) {
        Log.info("=== Testing Best Network ===");
        
        // Create trainer for testing
        var trainer = new NeuralEvolutionaryAI(controllerAPI);
        
        // Test on multiple maps
        Seq<String> testMaps = Seq.with(
            "Ancient Caldera",
            "Tar Fields", 
            "Frozen Forest",
            "Salt Flats"
        );
        
        var results = trainer.testBestAI(testMaps);
        
        // Display test results
        Log.info("Test Results:");
        for (var entry : results.getAllResults().entries()) {
            var mapName = entry.key;
            var result = entry.value;
            
            Log.info("  " + mapName + ":");
            Log.info("    Result: " + (result.won ? "VICTORY" : "DEFEAT"));
            Log.info("    Final Wave: " + result.finalWave);
            Log.info("    Fitness: " + String.format("%.2f", result.fitness));
            Log.info("    Duration: " + formatTime(result.gameTime));
        }
        
        Log.info("Overall Performance:");
        Log.info("  Win Rate: " + String.format("%.1f%%", results.getWinRate() * 100));
        Log.info("  Avg Fitness: " + String.format("%.2f", results.getAverageFitness()));
    }
    
    private void demonstrateRealTimeUsage(NeuralNetwork bestNetwork) {
        Log.info("=== Demonstrating Real-Time Usage ===");
        
        // Start a game
        var maps = controllerAPI.getAvailableMaps();
        if (maps.isEmpty()) {
            Log.warn("No maps available for demonstration");
            return;
        }
        
        var rules = new Rules();
        rules.waveSpacing = 30f;
        rules.infiniteResources = false;
        
        var gameController = controllerAPI.startGame(maps.first(), rules);
        
        // Create AI player with trained network
        var aiPlayer = controllerAPI.createPlayer("TrainedNeuralAI", Team.sharded);
        var neuralAI = new NeuralAI(aiPlayer, bestNetwork, controllerAPI, controllerAPI.getGameStats());
        
        Log.info("Neural AI created and ready");
        Log.info("Network: " + bestNetwork.toString());
        
        // Run for a limited time
        int updates = 0;
        int maxUpdates = 60; // Run for about 1 minute
        
        while (!gameController.isGameOver() && updates < maxUpdates) {
            // Update AI
            neuralAI.update();
            
            // Log progress every 10 updates
            if (updates % 10 == 0) {
                var stats = neuralAI.getBehaviorStats();
                var gameStats = controllerAPI.getGameStats().getPerformanceSummary();
                
                Log.info(String.format("Update %d: Wave %d, Built: %d, Actions: %d, Success: %.1f%%",
                    updates, gameStats.currentWave, stats.totalBuilt, 
                    stats.totalActions, stats.getActionSuccessRate() * 100));
            }
            
            updates++;
            
            // Sleep to simulate real-time
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Display final statistics
        var finalStats = neuralAI.getBehaviorStats();
        Log.info("Demonstration completed!");
        Log.info("Final Statistics:");
        Log.info("  Total updates: " + finalStats.updateCount);
        Log.info("  Total actions: " + finalStats.totalActions);
        Log.info("  Action success rate: " + String.format("%.1f%%", finalStats.getActionSuccessRate() * 100));
        Log.info("  Buildings built: " + finalStats.totalBuilt);
        Log.info("    - Mining: " + finalStats.miningBuilt);
        Log.info("    - Power: " + finalStats.powerBuilt);
        Log.info("    - Defense: " + finalStats.defenseBuilt);
        Log.info("    - Transport: " + finalStats.transportBuilt);
        Log.info("    - Production: " + finalStats.productionBuilt);
    }
    
    private String formatTime(long ticks) {
        long seconds = ticks / 60;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Example of manual neural network usage (without evolution).
     */
    public void demonstrateManualNetwork() {
        Log.info("=== Manual Network Usage ===");
        
        // Create a random network
        int[] architecture = {
            GameStateEncoder.getInputSize(),
            128,
            64,
            ActionDecoder.getOutputSize()
        };
        
        var network = new NeuralNetwork(architecture, 0.01f, 
            NeuralNetwork.ActivationFunction.LEAKY_RELU);
        
        Log.info("Created network: " + network.toString());
        Log.info("Parameters: " + network.getParameterCount());
        
        // Encode game state
        var statsAPI = controllerAPI.getGameStats();
        float[] inputs = GameStateEncoder.encode(statsAPI, controllerAPI);
        
        Log.info("Encoded game state into " + inputs.length + " inputs");
        
        // Forward pass
        float[] outputs = network.forward(inputs);
        
        Log.info("Network outputs:");
        Log.info("  Building priorities:");
        Log.info("    Drill: " + String.format("%.3f", outputs[0]));
        Log.info("    Power: " + String.format("%.3f", outputs[1]));
        Log.info("    Defense: " + String.format("%.3f", outputs[2]));
        Log.info("  Resource focus: " + String.format("%.3f", outputs[10]));
        Log.info("  Combat aggression: " + String.format("%.3f", outputs[15]));
        
        // Decode into actions
        var aiPlayer = controllerAPI.createPlayer("TestAI", Team.sharded);
        var actions = ActionDecoder.decode(outputs, aiPlayer, controllerAPI, statsAPI);
        
        Log.info("Decoded " + actions.size + " actions:");
        for (var action : actions) {
            Log.info("  - " + action.type);
        }
    }
    
    /**
     * Example of network mutation and crossover.
     */
    public void demonstrateEvolutionOperations() {
        Log.info("=== Evolution Operations ===");
        
        // Create two parent networks
        int[] architecture = {64, 128, 64, 32};
        var parent1 = new NeuralNetwork(architecture, 0.01f, NeuralNetwork.ActivationFunction.LEAKY_RELU);
        var parent2 = new NeuralNetwork(architecture, 0.01f, NeuralNetwork.ActivationFunction.LEAKY_RELU);
        
        Log.info("Created parent networks");
        
        // Crossover
        var child = NeuralNetwork.crossover(parent1, parent2);
        Log.info("Created child through crossover");
        
        // Mutation
        var mutated = child.mutate(0.15f, 0.3f);
        Log.info("Mutated child network");
        
        // Copy
        var copy = mutated.copy();
        Log.info("Created copy of mutated network");
        
        Log.info("All operations completed successfully!");
    }
}

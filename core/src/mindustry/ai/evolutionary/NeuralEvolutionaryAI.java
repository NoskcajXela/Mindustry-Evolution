package mindustry.ai.evolutionary;

import arc.struct.*;
import arc.util.*;
import mindustry.ai.evolutionary.fitness.*;
import mindustry.ai.evolutionary.neural.*;
import mindustry.api.*;
import mindustry.api.GameStatsAPI.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.world.*;

import java.util.concurrent.*;

/**
 * Neural network-based evolutionary AI system for playing and winning Mindustry.
 * Uses neuroevolution (genetic algorithm on neural network weights) to evolve strategies.
 */
public class NeuralEvolutionaryAI {
    
    // Evolution parameters
    private static final int POPULATION_SIZE = 20;
    private static final int GENERATIONS = 100;
    private static final float MUTATION_RATE = 0.15f;
    private static final float MUTATION_STRENGTH = 0.3f;
    private static final float CROSSOVER_RATE = 0.7f;
    private static final int ELITE_SIZE = 4;
    private static final int TOURNAMENT_SIZE = 3;
    
    // Game parameters
    private static final int MAX_GAME_TIME_MINUTES = 30;
    private static final int FITNESS_EVALUATION_GAMES = 3;
    
    private final ControllerAPI controllerAPI;
    private final GameStatsAPI statsAPI;
    private final FitnessEvaluator fitnessEvaluator;
    private final Seq<NeuralNetwork> population;
    private final Seq<Float> fitnessScores;
    
    private int currentGeneration = 0;
    private NeuralNetwork bestNetwork = null;
    private float bestFitness = Float.NEGATIVE_INFINITY;
    
    // Evolution statistics
    private final Seq<Float> generationBestFitness = new Seq<>();
    private final Seq<Float> generationAverageFitness = new Seq<>();
    
    public NeuralEvolutionaryAI(ControllerAPI controllerAPI) {
        this.controllerAPI = controllerAPI;
        this.statsAPI = controllerAPI.getGameStats();
        this.fitnessEvaluator = new ComprehensiveFitnessEvaluator();
        this.population = new Seq<>();
        this.fitnessScores = new Seq<>();
        
        Log.info("Neural Evolutionary AI initialized with population size: " + POPULATION_SIZE);
    }
    
    /**
     * Start the evolutionary process to develop a neural network AI.
     */
    public NeuralNetwork evolve() {
        Log.info("Starting neural evolution training...");
        
        try {
            // Initialize population with random networks
            initializePopulation();
            
            // Evolution loop
            for (currentGeneration = 0; currentGeneration < GENERATIONS; currentGeneration++) {
                Log.info("=== Generation " + (currentGeneration + 1) + "/" + GENERATIONS + " ===");
                
                // Evaluate fitness for all individuals
                evaluatePopulation();
                
                // Log generation statistics
                logGenerationStats();
                
                // Check for early stopping
                if (shouldStopEarly()) {
                    Log.info("Early stopping criterion met at generation " + (currentGeneration + 1));
                    break;
                }
                
                // Create next generation
                if (currentGeneration < GENERATIONS - 1) {
                    createNextGeneration();
                }
            }
            
            Log.info("Evolution completed! Best fitness: " + bestFitness);
            Log.info("Best network: " + bestNetwork.toString());
            
            // Save best network
            saveBestNetwork();
            
            return bestNetwork;
            
        } catch (Exception e) {
            Log.err("Evolution failed", e);
            throw new RuntimeException("Evolution process failed", e);
        }
    }
    
    /**
     * Test the best evolved AI against specific scenarios.
     */
    public TestResults testBestAI(Seq<String> testMaps) {
        if (bestNetwork == null) {
            throw new IllegalStateException("No best network available. Run evolution first.");
        }
        
        Log.info("Testing best AI on " + testMaps.size + " test maps...");
        var results = new TestResults();
        
        for (String mapName : testMaps) {
            var mapResult = testOnMap(bestNetwork, mapName);
            results.addResult(mapName, mapResult);
            
            Log.info("Test on " + mapName + ": " + 
                    (mapResult.won ? "WON" : "LOST") + 
                    " (Wave " + mapResult.finalWave + ", Fitness: " + 
                    String.format("%.2f", mapResult.fitness) + ")");
        }
        
        return results;
    }
    
    private void initializePopulation() {
        Log.info("Initializing population with random neural networks...");
        population.clear();
        
        int[] architecture = {
            GameStateEncoder.getInputSize(),
            128,  // Hidden layer 1
            64,   // Hidden layer 2
            ActionDecoder.getOutputSize()
        };
        
        for (int i = 0; i < POPULATION_SIZE; i++) {
            var network = new NeuralNetwork(architecture, 0.01f, NeuralNetwork.ActivationFunction.LEAKY_RELU);
            population.add(network);
        }
        
        Log.info("Population initialized with " + population.size + " neural networks");
        Log.info("Network architecture: " + population.first().toString());
    }
    
    private void evaluatePopulation() {
        Log.info("Evaluating population fitness...");
        fitnessScores.clear();
        
        // Use parallel evaluation for speed
        var executor = Executors.newFixedThreadPool(Math.min(4, POPULATION_SIZE));
        var futures = new Seq<Future<Float>>();
        
        for (int i = 0; i < population.size; i++) {
            final int index = i;
            Future<Float> future = executor.submit(() -> {
                return evaluateIndividual(population.get(index));
            });
            futures.add(future);
        }
        
        // Collect results
        for (int i = 0; i < futures.size; i++) {
            try {
                float fitness = futures.get(i).get(10, TimeUnit.MINUTES);
                fitnessScores.add(fitness);
                
                // Update best network
                if (fitness > bestFitness) {
                    bestFitness = fitness;
                    bestNetwork = population.get(i).copy();
                    Log.info("New best fitness: " + String.format("%.2f", bestFitness));
                }
                
            } catch (Exception e) {
                Log.err("Failed to evaluate individual " + i, e);
                fitnessScores.add(0f); // Assign worst fitness
            }
        }
        
        executor.shutdown();
    }
    
    private float evaluateIndividual(NeuralNetwork network) {
        Log.debug("Evaluating network: " + network.toString());
        
        float totalFitness = 0f;
        int successfulGames = 0;
        
        // Run multiple games for robust evaluation
        for (int game = 0; game < FITNESS_EVALUATION_GAMES; game++) {
            try {
                float gameFitness = runGameWithNetwork(network, "Ancient Caldera");
                totalFitness += gameFitness;
                successfulGames++;
                
            } catch (Exception e) {
                Log.warn("Game evaluation failed: " + e.getMessage());
                // Continue with other games
            }
        }
        
        if (successfulGames == 0) {
            return 0f; // Worst possible fitness
        }
        
        return totalFitness / successfulGames;
    }
    
    private float runGameWithNetwork(NeuralNetwork network, String mapName) {
        // Get available maps
        var maps = controllerAPI.getAvailableMaps();
        var map = maps.find(m -> m.plainName().equals(mapName));
        
        if (map == null) {
            Log.warn("Map not found: " + mapName + ", using first available map");
            map = maps.isEmpty() ? null : maps.first();
        }
        
        if (map == null) {
            throw new RuntimeException("No maps available for testing");
        }
        
        // Set up game rules for AI testing
        var rules = new Rules();
        rules.waveSpacing = 30f; // 30 seconds between waves
        rules.infiniteResources = false; // Realistic resource constraints
        rules.enemyCoreBuildRadius = 200f;
        
        // Start game
        var gameController = controllerAPI.startGame(map, rules);
        
        // Create AI player
        var aiPlayer = controllerAPI.createPlayer("NeuralAI", Team.sharded);
        
        // Create Neural AI behavior
        var neuralAI = new NeuralAI(aiPlayer, network, controllerAPI, statsAPI);
        
        // Reset stats for this game
        statsAPI.resetAllStats();
        
        long startTime = System.currentTimeMillis();
        long maxGameTimeMs = MAX_GAME_TIME_MINUTES * 60 * 1000L;
        
        // Game loop
        while (!gameController.isGameOver() && 
               (System.currentTimeMillis() - startTime) < maxGameTimeMs) {
            
            // Update AI behavior
            neuralAI.update();
            
            // Small delay to prevent overwhelming the system
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Evaluate fitness based on game outcome
        var gameOutcome = statsAPI.getGameOutcome();
        var performanceSummary = statsAPI.getPerformanceSummary();
        
        // Use a simple genome-like structure for fitness evaluation compatibility
        // In future, could create NeuralNetworkFitnessEvaluator
        var dummyGenome = createDummyGenomeFromNetwork(network);
        
        float fitness = fitnessEvaluator.evaluateFitness(
            gameOutcome, performanceSummary, dummyGenome, neuralAI.getBehaviorStats()
        );
        
        Log.debug("Game completed. Fitness: " + String.format("%.2f", fitness) + 
                 ", Wave: " + performanceSummary.currentWave + 
                 ", Won: " + gameOutcome.controllerTeamWon);
        
        return fitness;
    }
    
    private void createNextGeneration() {
        Log.info("Creating next generation...");
        
        var newPopulation = new Seq<NeuralNetwork>();
        
        // Elitism: Keep best individuals
        var elites = selectElites();
        newPopulation.addAll(elites);
        
        // Generate rest of population through crossover and mutation
        while (newPopulation.size < POPULATION_SIZE) {
            NeuralNetwork parent1 = tournamentSelection();
            NeuralNetwork parent2 = tournamentSelection();
            
            NeuralNetwork child;
            if (Math.random() < CROSSOVER_RATE) {
                child = NeuralNetwork.crossover(parent1, parent2);
            } else {
                child = parent1.copy();
            }
            
            if (Math.random() < MUTATION_RATE) {
                child = child.mutate(MUTATION_RATE, MUTATION_STRENGTH);
            }
            
            newPopulation.add(child);
        }
        
        // Replace population
        population.clear();
        population.addAll(newPopulation);
    }
    
    private Seq<NeuralNetwork> selectElites() {
        var elites = new Seq<NeuralNetwork>();
        var indices = new Seq<Integer>();
        
        // Create index array
        for (int i = 0; i < population.size; i++) {
            indices.add(i);
        }
        
        // Sort by fitness (descending)
        indices.sort((a, b) -> Float.compare(fitnessScores.get(b), fitnessScores.get(a)));
        
        // Select top individuals
        for (int i = 0; i < Math.min(ELITE_SIZE, indices.size); i++) {
            elites.add(population.get(indices.get(i)).copy());
        }
        
        return elites;
    }
    
    private NeuralNetwork tournamentSelection() {
        NeuralNetwork best = null;
        float bestFitness = Float.NEGATIVE_INFINITY;
        
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = (int) (Math.random() * population.size);
            float fitness = fitnessScores.get(randomIndex);
            
            if (fitness > bestFitness) {
                bestFitness = fitness;
                best = population.get(randomIndex);
            }
        }
        
        return best;
    }
    
    private void logGenerationStats() {
        float sum = 0f;
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;
        
        for (float fitness : fitnessScores) {
            sum += fitness;
            max = Math.max(max, fitness);
            min = Math.min(min, fitness);
        }
        
        float average = sum / fitnessScores.size;
        
        generationBestFitness.add(max);
        generationAverageFitness.add(average);
        
        Log.info("Generation " + (currentGeneration + 1) + " stats:");
        Log.info("  Best fitness: " + String.format("%.2f", max));
        Log.info("  Average fitness: " + String.format("%.2f", average));
        Log.info("  Worst fitness: " + String.format("%.2f", min));
        Log.info("  Fitness range: " + String.format("%.2f", max - min));
    }
    
    private boolean shouldStopEarly() {
        // Stop if fitness hasn't improved in last 10 generations
        if (generationBestFitness.size >= 10) {
            float recentBest = generationBestFitness.get(generationBestFitness.size - 1);
            float tenGenerationsAgo = generationBestFitness.get(generationBestFitness.size - 10);
            
            if (recentBest - tenGenerationsAgo < 1.0f) {
                return true;
            }
        }
        
        // Stop if we achieve very high fitness
        return bestFitness > 950f; // Out of 1000 max fitness
    }
    
    private GameResult testOnMap(NeuralNetwork network, String mapName) {
        try {
            float fitness = runGameWithNetwork(network, mapName);
            var outcome = statsAPI.getGameOutcome();
            var performance = statsAPI.getPerformanceSummary();
            
            var result = new GameResult();
            result.fitness = fitness;
            result.won = outcome.controllerTeamWon;
            result.finalWave = performance.currentWave;
            result.gameTime = performance.currentTime;
            result.endReason = outcome.endReason;
            
            return result;
            
        } catch (Exception e) {
            Log.err("Test failed on map " + mapName, e);
            
            var result = new GameResult();
            result.fitness = 0f;
            result.won = false;
            result.finalWave = 0;
            result.endReason = "Test failed: " + e.getMessage();
            
            return result;
        }
    }
    
    /**
     * Create a dummy genome for compatibility with existing fitness evaluator.
     * In future, create a NeuralNetworkFitnessEvaluator that works directly with networks.
     */
    private mindustry.ai.evolutionary.genome.AIGenome createDummyGenomeFromNetwork(NeuralNetwork network) {
        // For now, return a random genome - fitness evaluator doesn't heavily depend on genome values
        return mindustry.ai.evolutionary.genome.AIGenome.createRandom();
    }
    
    // === Getters for monitoring evolution progress ===
    
    public int getCurrentGeneration() {
        return currentGeneration;
    }
    
    public NeuralNetwork getBestNetwork() {
        return bestNetwork;
    }
    
    public float getBestFitness() {
        return bestFitness;
    }
    
    public Seq<Float> getGenerationBestFitness() {
        return generationBestFitness.copy();
    }
    
    public Seq<Float> getGenerationAverageFitness() {
        return generationAverageFitness.copy();
    }
    
    /**
     * Save the best network to disk.
     */
    private void saveBestNetwork() {
        if (bestNetwork == null) {
            Log.warn("No best network to save");
            return;
        }
        
        try {
            // Create directory if it doesn't exist
            var saveDir = new arc.files.Fi("trained_networks");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            // Generate filename with timestamp and fitness
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = String.format("neural_ai_gen%d_fitness%.0f_%s.nnet", 
                currentGeneration + 1, bestFitness, timestamp);
            
            var saveFile = saveDir.child(filename);
            bestNetwork.save(saveFile);
            
            // Also save as "best_neural_ai.nnet" for easy access
            bestNetwork.save(saveDir.child("best_neural_ai.nnet"));
            
            Log.info("Best network saved to: " + saveFile.absolutePath());
            Log.info("Also saved as: trained_networks/best_neural_ai.nnet");
        } catch (Exception e) {
            Log.err("Failed to save best network", e);
        }
    }
    
    /**
     * Load a previously trained network.
     */
    public static NeuralNetwork loadBestNetwork() {
        var file = new arc.files.Fi("trained_networks/best_neural_ai.nnet");
        if (!file.exists()) {
            throw new RuntimeException("No trained network found at: " + file.absolutePath());
        }
        return NeuralNetwork.load(file);
    }
    
    /**
     * Load a specific network file.
     */
    public static NeuralNetwork loadNetwork(String filename) {
        var file = new arc.files.Fi("trained_networks/" + filename);
        if (!file.exists()) {
            throw new RuntimeException("Network file not found: " + file.absolutePath());
        }
        return NeuralNetwork.load(file);
    }
    
    // === Inner Classes ===
    
    public static class TestResults {
        private final arc.struct.ObjectMap<String, GameResult> results = new arc.struct.ObjectMap<>();
        
        public void addResult(String mapName, GameResult result) {
            results.put(mapName, result);
        }
        
        public GameResult getResult(String mapName) {
            return results.get(mapName);
        }
        
        public arc.struct.ObjectMap<String, GameResult> getAllResults() {
            return results.copy();
        }
        
        public float getWinRate() {
            if (results.isEmpty()) return 0f;
            
            int wins = 0;
            for (var result : results.values()) {
                if (result.won) wins++;
            }
            
            return wins / (float) results.size;
        }
        
        public float getAverageFitness() {
            if (results.isEmpty()) return 0f;
            
            float sum = 0f;
            for (var result : results.values()) {
                sum += result.fitness;
            }
            
            return sum / results.size;
        }
    }
    
    public static class GameResult {
        public float fitness;
        public boolean won;
        public int finalWave;
        public long gameTime;
        public String endReason;
    }
}

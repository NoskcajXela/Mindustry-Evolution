package mindustry.ai.evolutionary;

import arc.struct.*;
import arc.util.*;
import mindustry.ai.evolutionary.genome.*;
import mindustry.ai.evolutionary.fitness.*;
import mindustry.api.*;
import mindustry.api.GameStatsAPI.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.world.*;

import java.util.concurrent.*;

/**
 * Evolutionary AI system for playing and winning Mindustry.
 * Uses genetic algorithms to evolve strategies for resource management, base building, and combat.
 */
public class EvolutionaryAI {
    
    // Evolution parameters
    private static final int POPULATION_SIZE = 20;
    private static final int GENERATIONS = 100;
    private static final float MUTATION_RATE = 0.15f;
    private static final float CROSSOVER_RATE = 0.7f;
    private static final int ELITE_SIZE = 4;
    private static final int TOURNAMENT_SIZE = 3;
    
    // Game parameters
    private static final int MAX_GAME_TIME_MINUTES = 30;
    private static final int FITNESS_EVALUATION_GAMES = 3;
    
    private final ControllerAPI controllerAPI;
    private final GameStatsAPI statsAPI;
    private final FitnessEvaluator fitnessEvaluator;
    private final Seq<AIGenome> population;
    private final Seq<Float> fitnessScores;
    
    private int currentGeneration = 0;
    private AIGenome bestGenome = null;
    private float bestFitness = Float.NEGATIVE_INFINITY;
    
    // Evolution statistics
    private final Seq<Float> generationBestFitness = new Seq<>();
    private final Seq<Float> generationAverageFitness = new Seq<>();
    
    public EvolutionaryAI(ControllerAPI controllerAPI) {
        this.controllerAPI = controllerAPI;
        this.statsAPI = controllerAPI.getGameStats();
        this.fitnessEvaluator = new ComprehensiveFitnessEvaluator();
        this.population = new Seq<>();
        this.fitnessScores = new Seq<>();
        
        Log.info("Evolutionary AI initialized with population size: " + POPULATION_SIZE);
    }
    
    /**
     * Start the evolutionary process to develop an AI that can win Mindustry.
     */
    public AIGenome evolve() {
        Log.info("Starting evolutionary AI training...");
        
        try {
            // Initialize population
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
            Log.info("Best genome: " + bestGenome.toString());
            
            return bestGenome;
            
        } catch (Exception e) {
            Log.err("Evolution failed", e);
            throw new RuntimeException("Evolution process failed", e);
        }
    }
    
    /**
     * Test the best evolved AI against specific scenarios.
     */
    public TestResults testBestAI(Seq<String> testMaps) {
        if (bestGenome == null) {
            throw new IllegalStateException("No best genome available. Run evolution first.");
        }
        
        Log.info("Testing best AI on " + testMaps.size + " test maps...");
        var results = new TestResults();
        
        for (String mapName : testMaps) {
            var mapResult = testOnMap(bestGenome, mapName);
            results.addResult(mapName, mapResult);
            
            Log.info("Test on " + mapName + ": " + 
                    (mapResult.won ? "WON" : "LOST") + 
                    " (Wave " + mapResult.finalWave + ", Fitness: " + 
                    String.format("%.2f", mapResult.fitness) + ")");
        }
        
        return results;
    }
    
    private void initializePopulation() {
        Log.info("Initializing population with random genomes...");
        population.clear();
        
        for (int i = 0; i < POPULATION_SIZE; i++) {
            AIGenome genome = AIGenome.createRandom();
            population.add(genome);
        }
        
        Log.info("Population initialized with " + population.size + " individuals");
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
                
                // Update best genome
                if (fitness > bestFitness) {
                    bestFitness = fitness;
                    bestGenome = population.get(i).copy();
                    Log.info("New best fitness: " + String.format("%.2f", bestFitness));
                }
                
            } catch (Exception e) {
                Log.err("Failed to evaluate individual " + i, e);
                fitnessScores.add(0f); // Assign worst fitness
            }
        }
        
        executor.shutdown();
    }
    
    private float evaluateIndividual(AIGenome genome) {
        Log.debug("Evaluating genome: " + genome.toString());
        
        float totalFitness = 0f;
        int successfulGames = 0;
        
        // Run multiple games for robust evaluation
        for (int game = 0; game < FITNESS_EVALUATION_GAMES; game++) {
            try {
                float gameFitness = runGameWithGenome(genome, "Ancient Caldera");
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
    
    private float runGameWithGenome(AIGenome genome, String mapName) {
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
        var aiPlayer = controllerAPI.createPlayer("EvolutionaryAI", Team.sharded);
        
        // Create AI behavior based on genome
        var aiBehavior = new GenomeBasedAI(aiPlayer, genome, controllerAPI, statsAPI);
        
        // Reset stats for this game
        statsAPI.resetAllStats();
        
        long startTime = System.currentTimeMillis();
        long maxGameTimeMs = MAX_GAME_TIME_MINUTES * 60 * 1000L;
        
        // Game loop
        while (!gameController.isGameOver() && 
               (System.currentTimeMillis() - startTime) < maxGameTimeMs) {
            
            // Update AI behavior
            aiBehavior.update();
            
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
        
        float fitness = fitnessEvaluator.evaluateFitness(
            gameOutcome, performanceSummary, genome, aiBehavior.getBehaviorStats()
        );
        
        Log.debug("Game completed. Fitness: " + String.format("%.2f", fitness) + 
                 ", Wave: " + performanceSummary.currentWave + 
                 ", Won: " + gameOutcome.controllerTeamWon);
        
        return fitness;
    }
    
    private void createNextGeneration() {
        Log.info("Creating next generation...");
        
        var newPopulation = new Seq<AIGenome>();
        
        // Elitism: Keep best individuals
        var elites = selectElites();
        newPopulation.addAll(elites);
        
        // Generate rest of population through crossover and mutation
        while (newPopulation.size < POPULATION_SIZE) {
            AIGenome parent1 = tournamentSelection();
            AIGenome parent2 = tournamentSelection();
            
            AIGenome child;
            if (Math.random() < CROSSOVER_RATE) {
                child = crossover(parent1, parent2);
            } else {
                child = parent1.copy();
            }
            
            if (Math.random() < MUTATION_RATE) {
                child = mutate(child);
            }
            
            newPopulation.add(child);
        }
        
        // Replace population
        population.clear();
        population.addAll(newPopulation);
    }
    
    private Seq<AIGenome> selectElites() {
        var elites = new Seq<AIGenome>();
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
    
    private AIGenome tournamentSelection() {
        AIGenome best = null;
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
    
    private AIGenome crossover(AIGenome parent1, AIGenome parent2) {
        return AIGenome.crossover(parent1, parent2);
    }
    
    private AIGenome mutate(AIGenome genome) {
        return genome.mutate(MUTATION_RATE);
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
    
    private GameResult testOnMap(AIGenome genome, String mapName) {
        try {
            float fitness = runGameWithGenome(genome, mapName);
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
    
    // === Getters for monitoring evolution progress ===
    
    public int getCurrentGeneration() {
        return currentGeneration;
    }
    
    public AIGenome getBestGenome() {
        return bestGenome;
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
    
    // === Inner Classes ===
    
    public static class TestResults {
        private final ObjectMap<String, GameResult> results = new ObjectMap<>();
        
        public void addResult(String mapName, GameResult result) {
            results.put(mapName, result);
        }
        
        public GameResult getResult(String mapName) {
            return results.get(mapName);
        }
        
        public ObjectMap<String, GameResult> getAllResults() {
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

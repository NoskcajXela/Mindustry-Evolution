package mindustry.ai.evolutionary.launcher;

import arc.backend.headless.*;
import arc.util.*;
import mindustry.ai.evolutionary.*;
import mindustry.ai.evolutionary.examples.*;
import mindustry.api.*;
import mindustry.api.HeadlessControllerLauncher;

/**
 * Headless launcher specifically designed for running evolutionary AI training.
 * Provides optimized settings and monitoring for long-running evolution processes.
 */
public class EvolutionaryAILauncher {
    
    private static final String VERSION = "1.0.0";
    
    public static void main(String[] args) {
        Log.info("Mindustry Evolutionary AI Trainer v" + VERSION);
        Log.info("=".repeat(50));
        
        // Parse command line arguments
        var config = parseArguments(args);
        
        try {
            switch (config.mode) {
                case FULL_EVOLUTION -> runFullEvolution(config);
                case QUICK_TEST -> runQuickTest(config);
                case EXAMPLE -> runExample(config);
                case BENCHMARK -> runBenchmark(config);
                default -> showUsage();
            }
            
        } catch (Exception e) {
            Log.err("Evolutionary AI training failed", e);
            System.exit(1);
        }
        
        Log.info("Evolutionary AI training completed successfully!");
        System.exit(0);
    }
    
    private static void runFullEvolution(LauncherConfig config) {
        Log.info("Starting full evolutionary AI training...");
        Log.info("This process may take 6-12 hours depending on hardware.");
        Log.info("Progress will be logged every 10 minutes.");
        
        // Initialize controller with optimized settings
        var launcher = createOptimizedController();
        var controllerAPI = launcher.getControllerAPI();
        
        // Create evolutionary AI with custom settings
        var evolutionaryAI = new EvolutionaryAI(controllerAPI);
        
        // Set up monitoring
        setupEvolutionMonitoring(evolutionaryAI);
        
        // Run evolution
        var bestGenome = evolutionaryAI.evolve();
        
        // Save results
        saveResults(bestGenome, evolutionaryAI, config.outputPath);
        
        // Test on standard maps
        testOnStandardMaps(evolutionaryAI, config.outputPath);
        
        controllerAPI.shutdown();
    }
    
    private static void runQuickTest(LauncherConfig config) {
        Log.info("Running quick evolution test (reduced parameters)...");
        
        var launcher = createOptimizedController();
        var controllerAPI = launcher.getControllerAPI();
        
        // Run with reduced parameters for testing
        var quickAI = new QuickEvolutionaryAI(controllerAPI, 
                                            config.populationSize, 
                                            config.generations);
        
        var bestGenome = quickAI.evolve();
        
        Log.info("Quick test completed. Best fitness: " + 
                String.format("%.2f", quickAI.getBestFitness()));
        
        if (config.outputPath != null) {
            saveResults(bestGenome, quickAI, config.outputPath);
        }
        
        controllerAPI.shutdown();
    }
    
    private static void runExample(LauncherConfig config) {
        Log.info("Running evolutionary AI example...");
        
        var example = new EvolutionaryAIExample();
        if (config.quick) {
            example.runQuickExample();
        } else {
            example.run();
        }
    }
    
    private static void runBenchmark(LauncherConfig config) {
        Log.info("Running evolutionary AI performance benchmark...");
        
        var launcher = createOptimizedController();
        var controllerAPI = launcher.getControllerAPI();
        
        // Run benchmark with different population sizes
        int[] populationSizes = {4, 8, 12, 16, 20};
        int benchmarkGenerations = 5;
        
        for (int popSize : populationSizes) {
            Log.info("Benchmarking with population size: " + popSize);
            
            long startTime = System.currentTimeMillis();
            
            var benchmarkAI = new QuickEvolutionaryAI(controllerAPI, popSize, benchmarkGenerations);
            benchmarkAI.evolve();
            
            long duration = System.currentTimeMillis() - startTime;
            float timePerGeneration = duration / (float) benchmarkGenerations / 1000f;
            
            Log.info("Population " + popSize + ": " + 
                    String.format("%.1f", timePerGeneration) + " seconds/generation, " +
                    "Best fitness: " + String.format("%.2f", benchmarkAI.getBestFitness()));
        }
        
        controllerAPI.shutdown();
    }
    
    private static HeadlessControllerLauncher createOptimizedController() {
        Log.info("Initializing optimized headless controller...");
        
        // Set JVM optimization flags for evolutionary training
        System.setProperty("java.awt.headless", "true");
        
        // Create and start headless controller
        var launcher = HeadlessControllerLauncher.create();
        new HeadlessApplication(launcher);
        
        launcher.waitForInitialization();
        
        Log.info("Controller initialized successfully");
        return launcher;
    }
    
    private static void setupEvolutionMonitoring(EvolutionaryAI evolutionaryAI) {
        // Create monitoring thread
        var monitorThread = new Thread(() -> {
            int lastGeneration = -1;
            
            while (evolutionaryAI.getCurrentGeneration() < 100) {
                try {
                    Thread.sleep(60000); // Check every minute
                    
                    int currentGeneration = evolutionaryAI.getCurrentGeneration();
                    
                    if (currentGeneration != lastGeneration) {
                        float bestFitness = evolutionaryAI.getBestFitness();
                        var bestFitnessHistory = evolutionaryAI.getGenerationBestFitness();
                        var avgFitnessHistory = evolutionaryAI.getGenerationAverageFitness();
                        
                        Log.info("=== Generation " + currentGeneration + " Complete ===");
                        Log.info("Best Fitness: " + String.format("%.2f", bestFitness));
                        
                        if (bestFitnessHistory.size >= 2) {
                            float improvement = bestFitnessHistory.get(bestFitnessHistory.size - 1) - 
                                              bestFitnessHistory.get(bestFitnessHistory.size - 2);
                            Log.info("Improvement: " + String.format("%.2f", improvement));
                        }
                        
                        if (avgFitnessHistory.size > 0) {
                            float avgFitness = avgFitnessHistory.get(avgFitnessHistory.size - 1);
                            Log.info("Average Fitness: " + String.format("%.2f", avgFitness));
                        }
                        
                        lastGeneration = currentGeneration;
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Log.warn("Monitor error: " + e.getMessage());
                }
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.setName("EvolutionMonitor");
        monitorThread.start();
    }
    
    private static void saveResults(var bestGenome, EvolutionaryAI evolutionaryAI, String outputPath) {
        Log.info("Saving evolution results...");
        
        try {
            // Create output directory
            var outputDir = new java.io.File(outputPath != null ? outputPath : "evolutionary-ai-results");
            outputDir.mkdirs();
            
            // Save best genome
            var genomeFile = new java.io.File(outputDir, "best-genome.txt");
            try (var writer = new java.io.PrintWriter(genomeFile)) {
                writer.println("# Best Evolved AI Genome");
                writer.println("# Fitness: " + String.format("%.2f", evolutionaryAI.getBestFitness()));
                writer.println("# Generation: " + evolutionaryAI.getCurrentGeneration());
                writer.println();
                writer.println(bestGenome.toString());
            }
            
            // Save fitness history
            var historyFile = new java.io.File(outputDir, "fitness-history.csv");
            try (var writer = new java.io.PrintWriter(historyFile)) {
                writer.println("Generation,BestFitness,AverageFitness");
                
                var bestHistory = evolutionaryAI.getGenerationBestFitness();
                var avgHistory = evolutionaryAI.getGenerationAverageFitness();
                
                for (int i = 0; i < bestHistory.size; i++) {
                    writer.printf("%d,%.2f,%.2f%n", 
                                i + 1, 
                                bestHistory.get(i),
                                i < avgHistory.size ? avgHistory.get(i) : 0f);
                }
            }
            
            Log.info("Results saved to: " + outputDir.getAbsolutePath());
            
        } catch (Exception e) {
            Log.err("Failed to save results", e);
        }
    }
    
    private static void testOnStandardMaps(EvolutionaryAI evolutionaryAI, String outputPath) {
        Log.info("Testing best AI on standard maps...");
        
        var testMaps = arc.struct.Seq.with(
            "Ancient Caldera",
            "Frozen Forest",
            "Biomass Synthesis Facility", 
            "Craters",
            "Ruinous Shores",
            "Windswept Islands",
            "Tar Fields",
            "Impact 0078",
            "Desolate Rift",
            "Planetary Launch Terminal"
        );
        
        var testResults = evolutionaryAI.testBestAI(testMaps);
        
        // Log detailed results
        Log.info("=== Standard Map Test Results ===");
        Log.info("Overall Win Rate: " + String.format("%.1f%%", testResults.getWinRate() * 100));
        Log.info("Average Fitness: " + String.format("%.2f", testResults.getAverageFitness()));
        
        for (var entry : testResults.getAllResults().entries()) {
            String mapName = entry.key;
            var result = entry.value;
            
            Log.info(String.format("%-30s: %s (Wave %2d, Fitness %.2f)",
                    mapName,
                    result.won ? "WON " : "LOST",
                    result.finalWave,
                    result.fitness));
        }
        
        // Save test results
        if (outputPath != null) {
            saveTestResults(testResults, outputPath);
        }
    }
    
    private static void saveTestResults(EvolutionaryAI.TestResults testResults, String outputPath) {
        try {
            var outputDir = new java.io.File(outputPath);
            var testFile = new java.io.File(outputDir, "map-test-results.csv");
            
            try (var writer = new java.io.PrintWriter(testFile)) {
                writer.println("MapName,Won,FinalWave,Fitness,GameTime,EndReason");
                
                for (var entry : testResults.getAllResults().entries()) {
                    String mapName = entry.key;
                    var result = entry.value;
                    
                    writer.printf("%s,%s,%d,%.2f,%d,%s%n",
                            mapName,
                            result.won ? "true" : "false",
                            result.finalWave,
                            result.fitness,
                            result.gameTime,
                            result.endReason != null ? result.endReason.replace(",", ";") : "");
                }
            }
            
            Log.info("Test results saved to: " + testFile.getAbsolutePath());
            
        } catch (Exception e) {
            Log.err("Failed to save test results", e);
        }
    }
    
    private static LauncherConfig parseArguments(String[] args) {
        var config = new LauncherConfig();
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mode", "-m" -> {
                    if (i + 1 < args.length) {
                        config.mode = RunMode.valueOf(args[++i].toUpperCase());
                    }
                }
                case "--output", "-o" -> {
                    if (i + 1 < args.length) {
                        config.outputPath = args[++i];
                    }
                }
                case "--population", "-p" -> {
                    if (i + 1 < args.length) {
                        config.populationSize = Integer.parseInt(args[++i]);
                    }
                }
                case "--generations", "-g" -> {
                    if (i + 1 < args.length) {
                        config.generations = Integer.parseInt(args[++i]);
                    }
                }
                case "--quick", "-q" -> config.quick = true;
                case "--help", "-h", "help" -> {
                    showUsage();
                    System.exit(0);
                }
                default -> Log.warn("Unknown argument: " + args[i]);
            }
        }
        
        return config;
    }
    
    private static void showUsage() {
        System.out.println("Mindustry Evolutionary AI Trainer v" + VERSION);
        System.out.println();
        System.out.println("Usage: java -jar evolutionary-ai.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --mode, -m <mode>         Run mode: FULL_EVOLUTION, QUICK_TEST, EXAMPLE, BENCHMARK");
        System.out.println("  --output, -o <path>       Output directory for results");
        System.out.println("  --population, -p <size>   Population size (default: 20)");
        System.out.println("  --generations, -g <count> Number of generations (default: 100)");
        System.out.println("  --quick, -q               Use quick settings for testing");
        System.out.println("  --help, -h                Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar evolutionary-ai.jar --mode FULL_EVOLUTION --output results");
        System.out.println("  java -jar evolutionary-ai.jar --mode QUICK_TEST --population 8 --generations 10");
        System.out.println("  java -jar evolutionary-ai.jar --mode EXAMPLE --quick");
        System.out.println("  java -jar evolutionary-ai.jar --mode BENCHMARK");
    }
    
    // === Inner Classes ===
    
    private enum RunMode {
        FULL_EVOLUTION, QUICK_TEST, EXAMPLE, BENCHMARK
    }
    
    private static class LauncherConfig {
        RunMode mode = RunMode.EXAMPLE;
        String outputPath = null;
        int populationSize = 20;
        int generations = 100;
        boolean quick = false;
    }
}

/**
 * Quick evolutionary AI implementation for testing and demonstrations.
 */
class QuickEvolutionaryAI extends EvolutionaryAI {
    
    private final int populationSize;
    private final int generations;
    
    public QuickEvolutionaryAI(ControllerAPI controllerAPI, int populationSize, int generations) {
        super(controllerAPI);
        this.populationSize = populationSize;
        this.generations = generations;
    }
    
    // Override parent class constants would go here in a real implementation
    // For now, this serves as a placeholder showing the concept
}

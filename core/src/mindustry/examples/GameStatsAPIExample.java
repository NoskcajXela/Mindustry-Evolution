package mindustry.examples;

import arc.util.*;
import mindustry.api.*;
import mindustry.api.impl.*;
import mindustry.game.*;
import mindustry.type.*;

/**
 * Example demonstrating the GameStatsAPI usage for monitoring game statistics.
 * Shows how to access economy, power, and efficiency metrics in real-time.
 */
public class GameStatsAPIExample {
    
    private ControllerAPI api;
    private GameStatsAPI statsAPI;
    private boolean monitoring = true;
    
    public static void main(String[] args) {
        new GameStatsAPIExample().run();
    }
    
    public void run() {
        Log.info("Starting GameStats API Example...");
        
        try {
            // Initialize the API
            initializeAPI();
            
            // Demonstrate various statistics features
            demonstrateBasicStats();
            demonstrateEconomyStats();
            demonstratePowerStats();
            demonstrateRealTimeMonitoring();
            demonstrateDataExport();
            
            Log.info("GameStats API example completed successfully!");
            
        } catch (Exception e) {
            Log.err("Example failed", e);
        } finally {
            cleanup();
        }
    }
    
    private void initializeAPI() {
        Log.info("=== Initializing API ===");
        
        // Create API instance (in a real scenario, you'd get this from a running game)
        api = new ControllerAPIImpl();
        statsAPI = api.getGameStats();
        
        Log.info("GameStats API initialized successfully");
    }
    
    private void demonstrateBasicStats() {
        Log.info("=== Basic Game Statistics ===");
        
        // Get basic game statistics
        var basicStats = statsAPI.getBasicStats();
        Log.info("Current wave: " + statsAPI.getCurrentWave());
        Log.info("Total game time: " + statsAPI.getTotalGameTime() + " ticks");
        Log.info("Units created: " + basicStats.unitsCreated);
        Log.info("Buildings built: " + basicStats.buildingsBuilt);
        Log.info("Buildings destroyed: " + basicStats.buildingsDestroyed);
        Log.info("Enemy units destroyed: " + basicStats.enemyUnitsDestroyed);
    }
    
    private void demonstrateEconomyStats() {
        Log.info("=== Economy Statistics ===");
        
        // Get production and consumption data
        var itemsProduced = statsAPI.getItemsProduced();
        var itemsConsumed = statsAPI.getItemsConsumed();
        var currentProduction = statsAPI.getCurrentProductionRates();
        var netFlow = statsAPI.getNetItemFlow();
        
        Log.info("Total items produced: " + itemsProduced.size + " types");
        Log.info("Total items consumed: " + itemsConsumed.size + " types");
        Log.info("Total resource value mined: " + statsAPI.getTotalResourceValueMined());
        
        // Show per-item details (if any items exist)
        if (!itemsProduced.isEmpty()) {
            Log.info("Production details:");
            itemsProduced.each((item, amount) -> {
                float rate = currentProduction.get(item, 0f);
                long net = netFlow.get(item, 0L);
                Log.info("  " + item.name + ": " + amount + " total, " + 
                        String.format("%.2f", rate) + "/sec, net: " + net);
            });
        }
        
        // Get economy summary
        var economySummary = statsAPI.getEconomySummary();
        Log.info("Economy Summary:");
        Log.info("  Total produced: " + economySummary.totalItemsProduced);
        Log.info("  Total consumed: " + economySummary.totalItemsConsumed);
        Log.info("  Economy efficiency: " + String.format("%.2f%%", economySummary.economyEfficiency * 100));
        Log.info("  Average production rate: " + String.format("%.2f", economySummary.averageProductionRate) + "/sec");
    }
    
    private void demonstratePowerStats() {
        Log.info("=== Power Statistics ===");
        
        // Get power metrics
        long totalGenerated = statsAPI.getTotalPowerGenerated();
        long totalConsumed = statsAPI.getTotalPowerConsumed();
        float currentGeneration = statsAPI.getCurrentPowerGeneration();
        float currentConsumption = statsAPI.getCurrentPowerConsumption();
        float efficiency = statsAPI.getCurrentPowerEfficiency();
        
        Log.info("Power generation (total): " + totalGenerated + " units");
        Log.info("Power consumption (total): " + totalConsumed + " units");
        Log.info("Current generation: " + String.format("%.2f", currentGeneration) + " units/sec");
        Log.info("Current consumption: " + String.format("%.2f", currentConsumption) + " units/sec");
        Log.info("Power efficiency: " + String.format("%.2f%%", efficiency * 100));
        
        // Power storage info
        float currentStored = statsAPI.getCurrentPowerStored();
        float capacity = statsAPI.getPowerStorageCapacity();
        Log.info("Power storage: " + String.format("%.2f", currentStored) + "/" + 
                String.format("%.2f", capacity) + " (" + 
                String.format("%.1f%%", statsAPI.getCoreStoragePercentage()) + ")");
        
        // Power shortage statistics
        if (statsAPI.getPowerShortageCount() > 0) {
            Log.info("Power shortage events: " + statsAPI.getPowerShortageCount());
            Log.info("Total shortage time: " + statsAPI.getTotalPowerShortageTime() + " ticks");
            Log.info("Longest shortage: " + statsAPI.getLongestPowerShortage() + " ticks");
            Log.info("Currently in shortage: " + statsAPI.isCurrentlyInPowerShortage());
        }
        
        // Get power summary
        var powerSummary = statsAPI.getPowerSummary();
        Log.info("Power Summary:");
        Log.info("  Efficiency: " + String.format("%.2f%%", powerSummary.efficiency * 100));
        Log.info("  Storage utilization: " + String.format("%.2f%%", powerSummary.storageUtilization * 100));
        Log.info("  Shortage events: " + powerSummary.shortageEvents);
    }
    
    private void demonstrateRealTimeMonitoring() {
        Log.info("=== Real-Time Monitoring ===");
        
        // Set up real-time monitoring
        statsAPI.addStatsUpdateListener(this::onStatsUpdate);
        statsAPI.setUpdateInterval(60); // Update every second (60 ticks)
        
        Log.info("Real-time monitoring enabled. Collecting data for 10 seconds...");
        
        // Monitor for a short period
        try {
            Thread.sleep(10000); // 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        monitoring = false;
        statsAPI.removeStatsUpdateListener(this::onStatsUpdate);
        Log.info("Real-time monitoring disabled");
    }
    
    private void onStatsUpdate(GameStatsAPI.GamePerformanceSummary summary) {
        if (!monitoring) return;
        
        Log.info("Stats Update - Wave: " + summary.currentWave + 
                ", Power Efficiency: " + String.format("%.1f%%", summary.power.efficiency * 100) +
                ", Economy Efficiency: " + String.format("%.1f%%", summary.economy.economyEfficiency * 100));
    }
    
    private void demonstrateDataExport() {
        Log.info("=== Data Export ===");
        
        // Export statistics in different formats
        String jsonData = statsAPI.exportStatsAsJSON();
        String csvData = statsAPI.exportStatsAsCSV();
        
        Log.info("JSON Export:\n" + jsonData);
        Log.info("CSV Export:\n" + csvData);
        
        // Demonstrate historical data
        var powerHistory = statsAPI.getPowerHistory(10);
        if (!powerHistory.isEmpty()) {
            Log.info("Recent power history (last 10 samples): " + powerHistory);
        }
        
        // Get comprehensive performance summary
        var performance = statsAPI.getPerformanceSummary();
        Log.info("Performance Summary:");
        Log.info("  Game active: " + performance.isGameActive);
        Log.info("  Current time: " + performance.currentTime);
        Log.info("  Overall efficiency: " + String.format("%.2f%%", performance.efficiency.overallEfficiency * 100));
        Log.info("  Uptime: " + String.format("%.2f%%", performance.efficiency.uptime * 100));
    }
    
    private void cleanup() {
        monitoring = false;
        if (api != null) {
            try {
                api.shutdown();
            } catch (Exception e) {
                Log.err("Error during cleanup", e);
            }
        }
    }
}

/**
 * Example of a simple statistics dashboard using the GameStatsAPI.
 */
class SimpleStatsDashboard {
    
    private final GameStatsAPI statsAPI;
    private final Timer.Task updateTask;
    
    public SimpleStatsDashboard(GameStatsAPI statsAPI) {
        this.statsAPI = statsAPI;
        
        // Update dashboard every 5 seconds
        this.updateTask = Timer.schedule(this::updateDashboard, 0f, 5f);
    }
    
    private void updateDashboard() {
        var summary = statsAPI.getPerformanceSummary();
        
        System.out.println("\n=== Game Statistics Dashboard ===");
        System.out.println("Wave: " + summary.currentWave + " | Time: " + formatTime(summary.currentTime));
        System.out.println("Units: " + summary.basicStats.unitsCreated + " | Buildings: " + summary.basicStats.buildingsBuilt);
        System.out.println("Power: " + String.format("%.1f%%", summary.power.efficiency * 100) + 
                          " | Economy: " + String.format("%.1f%%", summary.economy.economyEfficiency * 100));
        System.out.println("Uptime: " + String.format("%.1f%%", summary.efficiency.uptime * 100));
        
        // Alert on issues
        if (summary.power.efficiency < 0.8f) {
            System.out.println("⚠️  LOW POWER EFFICIENCY!");
        }
        if (statsAPI.isCurrentlyInPowerShortage()) {
            System.out.println("🔴 POWER SHORTAGE DETECTED!");
        }
        if (statsAPI.isCoreAtCapacity()) {
            System.out.println("📦 CORE AT CAPACITY!");
        }
    }
    
    private String formatTime(long ticks) {
        long seconds = ticks / 60;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%d:%02d", minutes, seconds % 60);
        }
    }
    
    public void shutdown() {
        updateTask.cancel();
    }
}

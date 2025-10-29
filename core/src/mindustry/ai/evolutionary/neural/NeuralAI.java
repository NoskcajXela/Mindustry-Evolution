package mindustry.ai.evolutionary.neural;

import arc.struct.*;
import arc.util.*;
import mindustry.api.*;
import mindustry.api.GameStatsAPI.*;
import mindustry.game.*;

/**
 * Neural network-based AI that replaces genome-based evolution.
 * Uses a neural network to process game stats and output actions to the Controller API.
 */
public class NeuralAI {
    
    private final PlayerController player;
    private final NeuralNetwork network;
    private final ControllerAPI controllerAPI;
    private final GameStatsAPI statsAPI;
    
    // AI state tracking
    private long lastUpdateTime = 0;
    private int updateCount = 0;
    private final BehaviorStats behaviorStats = new BehaviorStats();
    
    // Network architecture
    private static final int INPUT_SIZE = GameStateEncoder.getInputSize();
    private static final int HIDDEN_SIZE_1 = 128;
    private static final int HIDDEN_SIZE_2 = 64;
    private static final int OUTPUT_SIZE = ActionDecoder.getOutputSize();
    
    // Update frequency
    private static final long UPDATE_INTERVAL_MS = 1000; // 1 second
    
    public NeuralAI(PlayerController player, NeuralNetwork network, 
                    ControllerAPI controllerAPI, GameStatsAPI statsAPI) {
        this.player = player;
        this.network = network;
        this.controllerAPI = controllerAPI;
        this.statsAPI = statsAPI;
        
        Log.info("NeuralAI initialized for player: " + player.getName());
        Log.info("Network architecture: " + network.toString());
    }
    
    /**
     * Create a new NeuralAI with a random network.
     */
    public static NeuralAI createRandom(PlayerController player, ControllerAPI controllerAPI, 
                                        GameStatsAPI statsAPI) {
        int[] architecture = {INPUT_SIZE, HIDDEN_SIZE_1, HIDDEN_SIZE_2, OUTPUT_SIZE};
        var network = new NeuralNetwork(architecture, 0.01f, NeuralNetwork.ActivationFunction.LEAKY_RELU);
        return new NeuralAI(player, network, controllerAPI, statsAPI);
    }
    
    /**
     * Main update loop for AI behavior.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        
        // Update at fixed interval
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL_MS) {
            return;
        }
        
        lastUpdateTime = currentTime;
        updateCount++;
        
        try {
            // 1. Encode current game state into neural network inputs
            float[] inputs = GameStateEncoder.encode(statsAPI, controllerAPI);
            
            // 2. Forward pass through neural network
            float[] outputs = network.forward(inputs);
            
            // 3. Decode outputs into game actions
            var actions = ActionDecoder.decode(outputs, player, controllerAPI, statsAPI);
            
            // 4. Execute actions
            executeActions(actions);
            
            // 5. Update statistics
            behaviorStats.updateCount = updateCount;
            behaviorStats.totalActions += actions.size;
            
        } catch (Exception e) {
            Log.warn("NeuralAI update failed: " + e.getMessage());
            behaviorStats.errorCount++;
        }
    }
    
    private void executeActions(Seq<ActionDecoder.GameAction> actions) {
        int successCount = 0;
        int failureCount = 0;
        
        for (var action : actions) {
            try {
                action.execute(player, controllerAPI);
                successCount++;
                
                // Track action types
                updateActionStats(action);
                
            } catch (Exception e) {
                Log.debug("Action execution failed: " + e.getMessage());
                failureCount++;
            }
        }
        
        behaviorStats.actionSuccesses += successCount;
        behaviorStats.actionFailures += failureCount;
    }
    
    private void updateActionStats(ActionDecoder.GameAction action) {
        switch (action.type) {
            case BUILD_DRILL -> behaviorStats.miningBuilt++;
            case BUILD_POWER -> behaviorStats.powerBuilt++;
            case BUILD_DEFENSE -> behaviorStats.defenseBuilt++;
            case BUILD_TRANSPORT -> behaviorStats.transportBuilt++;
            case BUILD_PRODUCTION -> behaviorStats.productionBuilt++;
            default -> {}
        }
        behaviorStats.totalBuilt++;
    }
    
    /**
     * Train the network on a single experience.
     */
    public void train(float[] inputs, float[] expectedOutputs) {
        network.train(inputs, expectedOutputs);
    }
    
    /**
     * Get the underlying neural network.
     */
    public NeuralNetwork getNetwork() {
        return network;
    }
    
    /**
     * Get behavior statistics.
     */
    public BehaviorStats getBehaviorStats() {
        return behaviorStats;
    }
    
    /**
     * Copy this AI with its network.
     */
    public NeuralAI copy(PlayerController player) {
        return new NeuralAI(player, network.copy(), controllerAPI, statsAPI);
    }
    
    // === Behavior Statistics ===
    
    public static class BehaviorStats {
        public int updateCount = 0;
        public int totalActions = 0;
        public int actionSuccesses = 0;
        public int actionFailures = 0;
        public int errorCount = 0;
        
        // Building statistics
        public int totalBuilt = 0;
        public int miningBuilt = 0;
        public int powerBuilt = 0;
        public int defenseBuilt = 0;
        public int transportBuilt = 0;
        public int productionBuilt = 0;
        
        public float getActionSuccessRate() {
            int total = actionSuccesses + actionFailures;
            return total > 0 ? (float) actionSuccesses / total : 0f;
        }
        
        public float getAverageActionsPerUpdate() {
            return updateCount > 0 ? (float) totalActions / updateCount : 0f;
        }
        
        @Override
        public String toString() {
            return String.format("BehaviorStats[updates=%d, actions=%d, success=%.1f%%, built=%d]",
                updateCount, totalActions, getActionSuccessRate() * 100, totalBuilt);
        }
    }
}

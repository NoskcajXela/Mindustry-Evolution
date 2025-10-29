package mindustry.ai.evolutionary.neural;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.api.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * Decodes neural network outputs into game actions via the Controller API.
 * Maps continuous network outputs to discrete game actions.
 */
public class ActionDecoder {
    
    private static final int OUTPUT_SIZE = 32; // Fixed output vector size
    
    // Action categories (indices in output vector)
    private static final int BUILD_PRIORITY_START = 0;   // 10 outputs for building priorities
    private static final int BUILD_PRIORITY_END = 10;
    private static final int RESOURCE_FOCUS_START = 10;  // 5 outputs for resource focus
    private static final int RESOURCE_FOCUS_END = 15;
    private static final int COMBAT_STRATEGY_START = 15; // 5 outputs for combat strategy
    private static final int COMBAT_STRATEGY_END = 20;
    private static final int EXPANSION_STRATEGY_START = 20; // 5 outputs for expansion
    private static final int EXPANSION_STRATEGY_END = 25;
    private static final int TIMING_PARAMS_START = 25;   // 7 outputs for timing/pace
    private static final int TIMING_PARAMS_END = 32;
    
    /**
     * Decode network outputs into a sequence of game actions.
     */
    public static Seq<GameAction> decode(float[] outputs, PlayerController player, 
                                         ControllerAPI controllerAPI, GameStatsAPI statsAPI) {
        if (outputs.length != OUTPUT_SIZE) {
            throw new IllegalArgumentException("Output size mismatch: expected " + OUTPUT_SIZE + ", got " + outputs.length);
        }
        
        var actions = new Seq<GameAction>();
        
        try {
            // Decode building priorities
            var buildingActions = decodeBuildingActions(outputs, player, controllerAPI, statsAPI);
            actions.addAll(buildingActions);
            
            // Decode resource management actions
            var resourceActions = decodeResourceActions(outputs, player, controllerAPI, statsAPI);
            actions.addAll(resourceActions);
            
            // Decode combat actions
            var combatActions = decodeCombatActions(outputs, player, controllerAPI);
            actions.addAll(combatActions);
            
            // Decode expansion actions
            var expansionActions = decodeExpansionActions(outputs, player, controllerAPI);
            actions.addAll(expansionActions);
            
        } catch (Exception e) {
            Log.warn("Error decoding actions: " + e.getMessage());
        }
        
        return actions;
    }
    
    private static Seq<GameAction> decodeBuildingActions(float[] outputs, PlayerController player, 
                                                          ControllerAPI controllerAPI, GameStatsAPI statsAPI) {
        var actions = new Seq<GameAction>();
        
        // Extract building priorities from outputs
        float drillPriority = outputs[BUILD_PRIORITY_START + 0];
        float powerPriority = outputs[BUILD_PRIORITY_START + 1];
        float defensePriority = outputs[BUILD_PRIORITY_START + 2];
        float transportPriority = outputs[BUILD_PRIORITY_START + 3];
        float productionPriority = outputs[BUILD_PRIORITY_START + 4];
        float storePriority = outputs[BUILD_PRIORITY_START + 5];
        float corePriority = outputs[BUILD_PRIORITY_START + 6];
        float turretPriority = outputs[BUILD_PRIORITY_START + 7];
        float unitPriority = outputs[BUILD_PRIORITY_START + 8];
        float wallPriority = outputs[BUILD_PRIORITY_START + 9];
        
        // Decide what to build based on priorities and game state
        var power = statsAPI.getPowerSummary();
        var economy = statsAPI.getEconomySummary();
        var combat = statsAPI.getCombatSummary();
        
        // Build drills if resources are low and drill priority is high
        if (drillPriority > 0.6f && economy.totalItemsProduced < 1000) {
            actions.add(new BuildAction(ActionType.BUILD_DRILL, chooseDrill(outputs)));
        }
        
        // Build power if efficiency is low and power priority is high
        if (powerPriority > 0.6f && power.efficiency < 0.8f) {
            actions.add(new BuildAction(ActionType.BUILD_POWER, chooseGenerator(outputs)));
        }
        
        // Build defense if under attack and defense priority is high
        if (defensePriority > 0.6f && combat.totalDamageReceived > 1000) {
            actions.add(new BuildAction(ActionType.BUILD_DEFENSE, chooseTurret(outputs)));
        }
        
        // Build transport if latency is high and transport priority is high
        if (transportPriority > 0.6f) {
            actions.add(new BuildAction(ActionType.BUILD_TRANSPORT, Blocks.conveyor));
        }
        
        // Build production facilities if production priority is high
        if (productionPriority > 0.6f) {
            actions.add(new BuildAction(ActionType.BUILD_PRODUCTION, chooseProduction(outputs)));
        }
        
        // Build walls if wall priority is high
        if (wallPriority > 0.5f && combat.totalDamageReceived > 500) {
            actions.add(new BuildAction(ActionType.BUILD_WALL, chooseWall(outputs)));
        }
        
        return actions;
    }
    
    private static Seq<GameAction> decodeResourceActions(float[] outputs, PlayerController player, 
                                                          ControllerAPI controllerAPI, GameStatsAPI statsAPI) {
        var actions = new Seq<GameAction>();
        
        // Extract resource management parameters
        float miningFocus = outputs[RESOURCE_FOCUS_START + 0];
        float expansionRate = outputs[RESOURCE_FOCUS_START + 1];
        float conservationLevel = outputs[RESOURCE_FOCUS_START + 2];
        float techSpeed = outputs[RESOURCE_FOCUS_START + 3];
        float chainDepth = outputs[RESOURCE_FOCUS_START + 4];
        
        // Decide on resource gathering strategy
        if (miningFocus > 0.7f) {
            actions.add(new StrategyAction(ActionType.FOCUS_MINING));
        } else if (chainDepth > 0.7f) {
            actions.add(new StrategyAction(ActionType.FOCUS_PRODUCTION));
        }
        
        // Expansion decisions
        if (expansionRate > 0.6f) {
            actions.add(new StrategyAction(ActionType.EXPAND_TERRITORY));
        }
        
        return actions;
    }
    
    private static Seq<GameAction> decodeCombatActions(float[] outputs, PlayerController player, 
                                                        ControllerAPI controllerAPI) {
        var actions = new Seq<GameAction>();
        
        // Extract combat strategy parameters
        float aggressiveness = outputs[COMBAT_STRATEGY_START + 0];
        float defensiveBias = outputs[COMBAT_STRATEGY_START + 1];
        float rangePreference = outputs[COMBAT_STRATEGY_START + 2];
        float unitFocus = outputs[COMBAT_STRATEGY_START + 3];
        float turretFocus = outputs[COMBAT_STRATEGY_START + 4];
        
        // Decide combat strategy
        if (aggressiveness > 0.7f) {
            actions.add(new StrategyAction(ActionType.COMBAT_AGGRESSIVE));
        } else if (defensiveBias > 0.7f) {
            actions.add(new StrategyAction(ActionType.COMBAT_DEFENSIVE));
        }
        
        // Unit production decisions
        if (unitFocus > 0.6f) {
            actions.add(new BuildAction(ActionType.BUILD_UNITS, Blocks.groundFactory));
        }
        
        return actions;
    }
    
    private static Seq<GameAction> decodeExpansionActions(float[] outputs, PlayerController player, 
                                                           ControllerAPI controllerAPI) {
        var actions = new Seq<GameAction>();
        
        // Extract expansion parameters
        float expansionAggression = outputs[EXPANSION_STRATEGY_START + 0];
        float compactness = outputs[EXPANSION_STRATEGY_START + 1];
        float symmetryPreference = outputs[EXPANSION_STRATEGY_START + 2];
        float centralizedStorage = outputs[EXPANSION_STRATEGY_START + 3];
        float defenseDepth = outputs[EXPANSION_STRATEGY_START + 4];
        
        // Decide expansion strategy
        if (expansionAggression > 0.7f) {
            actions.add(new StrategyAction(ActionType.EXPAND_AGGRESSIVE));
        } else if (compactness > 0.7f) {
            actions.add(new StrategyAction(ActionType.BUILD_COMPACT));
        }
        
        return actions;
    }
    
    // === Block Selection Helpers ===
    
    private static Block chooseDrill(float[] outputs) {
        // Choose drill based on tech level and priorities
        float techLevel = outputs[TIMING_PARAMS_START + 3]; // Tech progression output
        if (techLevel > 0.8f) return Blocks.laserDrill;
        if (techLevel > 0.5f) return Blocks.pneumaticDrill;
        return Blocks.mechanicalDrill;
    }
    
    private static Block chooseGenerator(float[] outputs) {
        float techLevel = outputs[TIMING_PARAMS_START + 3];
        if (techLevel > 0.8f) return Blocks.thoriumReactor;
        if (techLevel > 0.6f) return Blocks.steamGenerator;
        if (techLevel > 0.4f) return Blocks.thermalGenerator;
        return Blocks.combustionGenerator;
    }
    
    private static Block chooseTurret(float[] outputs) {
        float rangePreference = outputs[COMBAT_STRATEGY_START + 2];
        float techLevel = outputs[TIMING_PARAMS_START + 3];
        
        if (techLevel > 0.8f) {
            return rangePreference > 0.5f ? Blocks.ripple : Blocks.fuse;
        } else if (techLevel > 0.5f) {
            return rangePreference > 0.5f ? Blocks.scatter : Blocks.hail;
        } else {
            return rangePreference > 0.5f ? Blocks.duo : Blocks.salvo;
        }
    }
    
    private static Block chooseProduction(float[] outputs) {
        float techLevel = outputs[TIMING_PARAMS_START + 3];
        if (techLevel > 0.7f) return Blocks.surgeSmelter;
        if (techLevel > 0.5f) return Blocks.siliconSmelter;
        return Blocks.graphitePress;
    }
    
    private static Block chooseWall(float[] outputs) {
        float techLevel = outputs[TIMING_PARAMS_START + 3];
        if (techLevel > 0.8f) return Blocks.surgeWall;
        if (techLevel > 0.6f) return Blocks.thoriumWall;
        if (techLevel > 0.4f) return Blocks.titaniumWall;
        return Blocks.copperWall;
    }
    
    /**
     * Get the output size for the neural network.
     */
    public static int getOutputSize() {
        return OUTPUT_SIZE;
    }
    
    // === Action Classes ===
    
    public static abstract class GameAction {
        public final ActionType type;
        
        public GameAction(ActionType type) {
            this.type = type;
        }
        
        public abstract void execute(PlayerController player, ControllerAPI controllerAPI);
    }
    
    public static class BuildAction extends GameAction {
        public final Block block;
        
        public BuildAction(ActionType type, Block block) {
            super(type);
            this.block = block;
        }
        
        @Override
        public void execute(PlayerController player, ControllerAPI controllerAPI) {
            // Find a suitable location to build
            Vec2 location = findBuildLocation(player, block, controllerAPI);
            if (location != null) {
                try {
                    player.placeBlock(block, (int) location.x, (int) location.y, 0);
                } catch (Exception e) {
                    Log.debug("Failed to place block: " + e.getMessage());
                }
            }
        }
        
        private Vec2 findBuildLocation(PlayerController player, Block block, ControllerAPI controllerAPI) {
            // Simple location finder - find empty space near core
            // In production, this would be more sophisticated
            var cores = player.getTeam().cores();
            if (cores.isEmpty()) return null;
            
            var core = cores.first();
            int coreX = core.tileX();
            int coreY = core.tileY();
            
            // Search in expanding radius
            for (int radius = 3; radius < 20; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;
                        
                        int x = coreX + dx;
                        int y = coreY + dy;
                        
                        // Check if location is valid (simplified)
                        if (x >= 0 && y >= 0 && x < 500 && y < 500) {
                            return new Vec2(x, y);
                        }
                    }
                }
            }
            
            return null;
        }
    }
    
    public static class StrategyAction extends GameAction {
        public StrategyAction(ActionType type) {
            super(type);
        }
        
        @Override
        public void execute(PlayerController player, ControllerAPI controllerAPI) {
            // Strategy actions affect behavior but don't directly place blocks
            // They would be used to set internal AI state/flags
            Log.debug("Strategy action: " + type);
        }
    }
    
    public enum ActionType {
        // Building actions
        BUILD_DRILL,
        BUILD_POWER,
        BUILD_DEFENSE,
        BUILD_TRANSPORT,
        BUILD_PRODUCTION,
        BUILD_WALL,
        BUILD_UNITS,
        
        // Strategy actions
        FOCUS_MINING,
        FOCUS_PRODUCTION,
        EXPAND_TERRITORY,
        COMBAT_AGGRESSIVE,
        COMBAT_DEFENSIVE,
        EXPAND_AGGRESSIVE,
        BUILD_COMPACT
    }
}

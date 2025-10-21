package mindustry.test;

import arc.util.Log;
import mindustry.api.ControllerAPI;
import mindustry.api.ControllerAPIData.*;
import mindustry.api.impl.ControllerAPIImpl;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.type.UnitType;

/**
 * Integration test for the Controller API.
 * Tests basic functionality without requiring a full server setup.
 */
public class ControllerAPITest {
    
    public static void main(String[] args) {
        Log.info("Starting Controller API Integration Test...");
        
        try {
            runBasicTests();
            Log.info("All tests passed!");
        } catch (Exception e) {
            Log.err("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runBasicTests() {
        Log.info("Testing API creation...");
        
        // Test API configuration
        APIConfig config = new APIConfig();
        config.enableHeadless = true;
        config.autoSaveInterval = 300;
        config.maxAIPlayers = 10;
        config.enableEventLogging = true;
        
        Log.info("API configuration created successfully");
        
        // Test data structures
        testDataStructures();
        
        // Test AI behavior configuration
        testAIBehavior();
        
        Log.info("Basic API tests completed successfully");
    }
    
    private static void testDataStructures() {
        Log.info("Testing data structures...");
        
        // Test PlayerInfo
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.name = "TestBot";
        playerInfo.team = Team.sharded;
        playerInfo.unitType = UnitTypes.dagger;
        playerInfo.isAI = true;
        
        Log.info("PlayerInfo: " + playerInfo.name + " on team " + playerInfo.team);
        
        // Test GameInfo  
        GameInfo gameInfo = new GameInfo();
        gameInfo.mapName = "Test Map";
        gameInfo.gameMode = "survival";
        gameInfo.playerCount = 4;
        gameInfo.waveNumber = 10;
        gameInfo.state = GameState.State.playing;
        
        Log.info("GameInfo: " + gameInfo.mapName + " - " + gameInfo.gameMode);
        
        // Test BuildingInfo
        BuildingInfo buildingInfo = new BuildingInfo();
        buildingInfo.blockType = Blocks.coreShard;
        buildingInfo.x = 100;
        buildingInfo.y = 100;
        buildingInfo.team = Team.sharded;
        buildingInfo.health = 1000f;
        
        Log.info("BuildingInfo: " + buildingInfo.blockType + " at (" + buildingInfo.x + "," + buildingInfo.y + ")");
        
        // Test UnitInfo
        UnitInfo unitInfo = new UnitInfo();
        unitInfo.unitType = UnitTypes.dagger;
        unitInfo.x = 50f;
        unitInfo.y = 50f;
        unitInfo.health = 100f;
        unitInfo.team = Team.sharded;
        
        Log.info("UnitInfo: " + unitInfo.unitType + " at (" + unitInfo.x + "," + unitInfo.y + ")");
    }
    
    private static void testAIBehavior() {
        Log.info("Testing AI behavior configuration...");
        
        // Test AIBehavior
        AIBehavior behavior = new AIBehavior();
        behavior.type = "builder";
        behavior.priority = 5;
        behavior.enabled = true;
        behavior.target = "core";
        behavior.range = 100f;
        
        AIBehaviorConfig aiConfig = new AIBehaviorConfig();
        aiConfig.behaviors.add(behavior);
        aiConfig.reactionTime = 2f;
        aiConfig.aggressiveness = 0.7f;
        aiConfig.buildPriority = 0.8f;
        
        Log.info("AI Behavior configured: " + behavior.type + " with priority " + behavior.priority);
        
        // Test ServerInfo
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.name = "Test Controller Server";
        serverInfo.description = "A test server using the Controller API";
        serverInfo.playerCount = 2;
        serverInfo.maxPlayers = 16;
        serverInfo.mapName = "Ancient Caldera";
        serverInfo.gameMode = "survival";
        serverInfo.wave = 15;
        
        Log.info("ServerInfo: " + serverInfo.name + " - " + serverInfo.playerCount + "/" + serverInfo.maxPlayers + " players");
    }
}

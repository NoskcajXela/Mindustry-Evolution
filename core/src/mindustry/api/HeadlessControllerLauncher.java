package mindustry.api;

import arc.*;
import arc.backend.headless.*;
import arc.util.*;
import mindustry.*;
import mindustry.api.impl.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.net.*;

import static mindustry.Vars.*;

/**
 * Headless launcher with Controller API integration.
 * Provides programmatic control over Mindustry without rendering.
 */
public class HeadlessControllerLauncher implements ApplicationListener {
    
    private ControllerAPIImpl controllerAPI;
    private boolean initialized = false;
    
    public static void main(String[] args) {
        try {
            // Setup platform and networking
            Vars.platform = new Platform(){};
            Vars.net = new Net(platform.getNet());
            
            // Setup logging
            Log.logger = (level, text) -> {
                System.out.println("[" + java.time.LocalDateTime.now() + "] " + text);
            };
            
            // Create and run headless application
            var launcher = new HeadlessControllerLauncher();
            new HeadlessApplication(launcher, throwable -> {
                Log.err("Fatal error in headless controller", throwable);
                System.exit(1);
            });
            
        } catch (Throwable t) {
            Log.err("Failed to start headless controller", t);
            System.exit(1);
        }
    }
    
    @Override
    public void init() {
        Log.info("Initializing Headless Controller API...");
        
        try {
            // Basic initialization
            Core.settings.setDataDirectory(Core.files.local("mindustry-controller/"));
            headless = true;
            
            // Initialize core systems
            Vars.init();
            
            // Initialize content
            content.createBaseContent();
            
            // Load mods
            mods.loadScripts();
            content.createModContent();
            
            // Initialize world and logic
            add(logic = new Logic());
            add(netServer = new NetServer());
            
            // Finalize content initialization
            content.init();
            
            // Initialize mods
            mods.eachClass(Mod::init);
            
            if(mods.hasContentErrors()){
                Log.warn("Some mod content has errors and may not function correctly.");
                for(LoadedMod mod : mods.list()){
                    if(mod.hasContentErrors()){
                        Log.warn("Mod '@' has content errors:", mod.name);
                        for(Content cont : mod.erroredContent){
                            Log.warn("  - @: @", cont.name, cont.error);
                        }
                    }
                }
            }
            
            // Create Controller API
            controllerAPI = new ControllerAPIImpl();
            initialized = true;
            
            Log.info("Headless Controller API initialized successfully");
            Log.info("API Version: @", controllerAPI.getVersion());
            
            // Fire initialization event
            Events.fire(new ClientLoadEvent());
            
        } catch (Exception e) {
            Log.err("Failed to initialize Headless Controller API", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }
    
    public ControllerAPI getControllerAPI() {
        if (!initialized) {
            throw new IllegalStateException("Controller API not yet initialized");
        }
        return controllerAPI;
    }
    
    @Override
    public void update() {
        if (initialized) {
            // Update core systems
            if (logic != null) {
                logic.update();
            }
            
            if (netServer != null) {
                netServer.update();
            }
            
            // Update timers
            Time.update();
        }
    }
    
    @Override
    public void dispose() {
        Log.info("Shutting down Headless Controller API...");
        
        if (controllerAPI != null) {
            controllerAPI.shutdown();
        }
        
        if (netServer != null) {
            netServer.dispose();
        }
        
        Log.info("Headless Controller API shutdown complete");
    }
    
    // === Factory Methods ===
    
    /** Create a new headless controller instance */
    public static HeadlessControllerLauncher create() {
        return new HeadlessControllerLauncher();
    }
    
    /** Start a headless controller in a separate thread */
    public static Thread startAsync() {
        var thread = new Thread(() -> {
            main(new String[0]);
        }, "HeadlessController");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
    
    // === Utility Methods ===
    
    /** Wait until the controller is fully initialized */
    public void waitForInitialization() {
        while (!initialized) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for initialization", e);
            }
        }
    }
    
    /** Check if the controller is ready to use */
    public boolean isReady() {
        return initialized && controllerAPI != null && controllerAPI.isRunning();
    }
}

// === Example Usage Class ===

/**
 * Example demonstrating how to use the Headless Controller API.
 */
class HeadlessControllerExample {
    
    public static void main(String[] args) {
        // Start the headless controller
        var launcher = HeadlessControllerLauncher.create();
        new HeadlessApplication(launcher);
        
        // Wait for initialization
        launcher.waitForInitialization();
        
        // Get the Controller API
        var api = launcher.getControllerAPI();
        
        try {
            // Example: Start a game
            var maps = api.getAvailableMaps();
            if (!maps.isEmpty()) {
                var gameController = api.startGame(maps.first(), new Rules());
                
                // Create AI players
                var player1 = api.createPlayer("AI-Builder", Team.sharded);
                var player2 = api.createPlayer("AI-Fighter", Team.crux);
                
                // Set up the world
                var world = api.getWorld();
                
                // Build some structures
                player1.placeBlock(Blocks.coreNucleus, 50, 50, 0);
                player1.placeBlock(Blocks.siliconSmelter, 55, 50, 0);
                
                player2.placeBlock(Blocks.coreNucleus, 150, 150, 0);
                player2.placeBlock(Blocks.duo, 145, 150, 0);
                
                // Start the game
                gameController.nextWave();
                
                // Monitor the game
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(1000);
                    
                    var stats = api.getServerStats();
                    System.out.println("Wave: " + gameController.getWave() + 
                                     ", Players: " + stats.playerCount + 
                                     ", Enemies: " + stats.enemyCount);
                    
                    if (gameController.isGameOver()) {
                        System.out.println("Game Over! Winner: " + gameController.getWinningTeam());
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cleanup
            api.shutdown();
        }
    }
}

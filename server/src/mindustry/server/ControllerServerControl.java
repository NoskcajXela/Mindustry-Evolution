package mindustry.server;

import arc.util.*;
import mindustry.api.*;
import mindustry.api.impl.*;

import static mindustry.Vars.*;

/**
 * Enhanced ServerControl with integrated Controller API support.
 * Extends the existing server control with headless API capabilities.
 */
public class ControllerServerControl extends ServerControl {
    
    private ControllerAPIImpl controllerAPI;
    private boolean apiEnabled = false;
    
    public ControllerServerControl() {
        super();
        
        // Add controller API commands to the existing command handler
        registerControllerCommands();
        
        Log.info("Controller Server Control initialized");
    }
    
    /** Initialize the Controller API */
    public void initializeControllerAPI() {
        if (controllerAPI == null) {
            try {
                controllerAPI = new ControllerAPIImpl();
                apiEnabled = true;
                
                Log.info("Controller API enabled on server");
                
                // Register API event listeners for server events
                registerAPIEventListeners();
                
            } catch (Exception e) {
                Log.err("Failed to initialize Controller API", e);
            }
        }
    }
    
    /** Get the Controller API instance */
    public ControllerAPI getControllerAPI() {
        if (!apiEnabled) {
            throw new IllegalStateException("Controller API not initialized. Call initializeControllerAPI() first.");
        }
        return controllerAPI;
    }
    
    /** Check if Controller API is enabled */
    public boolean isAPIEnabled() {
        return apiEnabled && controllerAPI != null && controllerAPI.isRunning();
    }
    
    private void registerControllerCommands() {
        // Add commands for Controller API management
        handler.register("api", "[enable|disable|status]", "Manage Controller API", args -> {
            if (args.length == 0) {
                info("Controller API status: @", isAPIEnabled() ? "enabled" : "disabled");
                return;
            }
            
            switch (args[0]) {
                case "enable" -> {
                    if (isAPIEnabled()) {
                        info("Controller API is already enabled");
                    } else {
                        initializeControllerAPI();
                        info("Controller API enabled");
                    }
                }
                case "disable" -> {
                    if (isAPIEnabled()) {
                        controllerAPI.shutdown();
                        apiEnabled = false;
                        info("Controller API disabled");
                    } else {
                        info("Controller API is already disabled");
                    }
                }
                case "status" -> {
                    info("Controller API status: @", isAPIEnabled() ? "enabled" : "disabled");
                    if (isAPIEnabled()) {
                        var stats = controllerAPI.getServerStats();
                        info("  Players: @", stats.playerCount);
                        info("  Memory: @MB / @MB", stats.memoryUsed / 1024 / 1024, stats.memoryTotal / 1024 / 1024);
                        info("  FPS: @", stats.fps);
                    }
                }
                default -> err("Unknown API command. Use: enable, disable, or status");
            }
        });
        
        // Command to create AI players
        handler.register("createai", "<name> <team>", "Create an AI player", args -> {
            if (!isAPIEnabled()) {
                err("Controller API not enabled. Use 'api enable' first.");
                return;
            }
            
            if (args.length < 2) {
                err("Usage: createai <name> <team>");
                return;
            }
            
            try {
                var team = Team.valueOf(args[1]);
                var player = controllerAPI.createPlayer(args[0], team);
                info("Created AI player '@' on team @", args[0], team.name);
            } catch (Exception e) {
                err("Failed to create AI player: @", e.getMessage());
            }
        });
        
        // Command to list AI players
        handler.register("listai", "List all AI players", args -> {
            if (!isAPIEnabled()) {
                err("Controller API not enabled.");
                return;
            }
            
            var players = controllerAPI.getPlayers();
            if (players.isEmpty()) {
                info("No AI players active");
            } else {
                info("Active AI players (@):", players.size);
                for (var player : players) {
                    var stats = player.getStats();
                    info("  @ - Team: @, Alive: @", stats.name, stats.team.name, stats.alive);
                }
            }
        });
        
        // Command to start a game via API
        handler.register("startgame", "<mapname>", "Start a game using Controller API", args -> {
            if (!isAPIEnabled()) {
                err("Controller API not enabled.");
                return;
            }
            
            if (args.length < 1) {
                err("Usage: startgame <mapname>");
                return;
            }
            
            try {
                var maps = controllerAPI.getAvailableMaps();
                var map = maps.find(m -> m.plainName().equals(args[0]));
                
                if (map == null) {
                    err("Map not found: @", args[0]);
                    info("Available maps:");
                    maps.each(m -> info("  @", m.plainName()));
                    return;
                }
                
                var gameController = controllerAPI.startGame(map, new mindustry.game.Rules());
                info("Started game with map: @", args[0]);
                
            } catch (Exception e) {
                err("Failed to start game: @", e.getMessage());
            }
        });
        
        // Command to get detailed API stats
        handler.register("apistats", "Show detailed Controller API statistics", args -> {
            if (!isAPIEnabled()) {
                err("Controller API not enabled.");
                return;
            }
            
            try {
                var stats = controllerAPI.getServerStats();
                var gameState = controllerAPI.getGameState();
                
                info("=== Controller API Statistics ===");
                info("Game State: @", gameState.isGame ? (gameState.isPaused ? "Paused" : "Playing") : "Menu");
                info("Wave: @ | Enemies: @", gameState.wave, gameState.enemies);
                info("Players: @ | AI Players: @", stats.playerCount, controllerAPI.getPlayers().size);
                info("Memory: @MB / @MB (@%)", 
                     stats.memoryUsed / 1024 / 1024, 
                     stats.memoryTotal / 1024 / 1024,
                     (int)(stats.memoryUsed * 100.0 / stats.memoryTotal));
                info("FPS: @", stats.fps);
                
                if (gameState.isGame) {
                    var world = controllerAPI.getWorld();
                    info("World: @x@ (@)", world.getWidth(), world.getHeight(), world.getName());
                }
                
            } catch (Exception e) {
                err("Failed to get API statistics: @", e.getMessage());
            }
        });
    }
    
    private void registerAPIEventListeners() {
        // Listen for game events and log them
        controllerAPI.addEventListener("game.start", (eventType, data) -> {
            Log.info("Game started via Controller API");
        });
        
        controllerAPI.addEventListener("game.end", (eventType, data) -> {
            Log.info("Game ended via Controller API");
        });
        
        controllerAPI.addEventListener("player.created", (eventType, data) -> {
            if (data instanceof PlayerController player) {
                Log.info("AI player created: @ on team @", player.getName(), player.getTeam().name);
            }
        });
        
        controllerAPI.addEventListener("player.removed", (eventType, data) -> {
            if (data instanceof PlayerController player) {
                Log.info("AI player removed: @", player.getName());
            }
        });
    }
    
    @Override
    public void update() {
        super.update();
        
        // Update Controller API if enabled
        if (isAPIEnabled()) {
            // The API handles its own updates, but we could add monitoring here
        }
    }
    
    @Override
    public void dispose() {
        // Shutdown Controller API
        if (isAPIEnabled()) {
            Log.info("Shutting down Controller API...");
            controllerAPI.shutdown();
        }
        
        super.dispose();
    }
}

package mindustry.api.impl;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.api.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.maps.*;
import mindustry.net.Administration.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/**
 * Main implementation of the Controller API.
 * Provides headless programmatic control over Mindustry.
 */
public class ControllerAPIImpl implements ControllerAPI {
    
    private boolean running = false;
    private final Seq<PlayerController> players = new Seq<>();
    private final ObjectMap<String, Seq<EventListener>> eventListeners = new ObjectMap<>();
    private GameController gameController;
    private WorldController worldController;
    private AdminController adminController;
    
    public ControllerAPIImpl() {
        this.gameController = new GameControllerImpl(this);
        this.worldController = new WorldControllerImpl();
        this.adminController = new AdminControllerImpl();
        this.running = true;
    }
    
    @Override
    public GameController startGame(Map map, Rules rules) {
        if (!running) {
            throw new IllegalStateException("Controller is not running");
        }
        
        try {
            // Initialize world
            world.loadMap(map, rules);
            
            // Start logic
            if (logic != null) {
                logic.play();
            }
            
            Log.info("Started game with map: @ and custom rules", map.plainName());
            return gameController;
            
        } catch (Exception e) {
            Log.err("Failed to start game", e);
            throw new RuntimeException("Failed to start game: " + e.getMessage(), e);
        }
    }
    
    @Override
    public GameController loadGame(String savePath) {
        if (!running) {
            throw new IllegalStateException("Controller is not running");
        }
        
        try {
            var save = control.saves.getSaveSlots().find(s -> s.file.nameWithoutExtension().equals(savePath));
            if (save == null) {
                throw new IllegalArgumentException("Save file not found: " + savePath);
            }
            
            save.load();
            Log.info("Loaded game from: @", savePath);
            return gameController;
            
        } catch (Exception e) {
            Log.err("Failed to load game", e);
            throw new RuntimeException("Failed to load game: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void saveGame(String savePath) {
        if (!running || !state.isGame()) {
            throw new IllegalStateException("No active game to save");
        }
        
        try {
            control.saves.addSave(savePath);
            Log.info("Saved game to: @", savePath);
        } catch (Exception e) {
            Log.err("Failed to save game", e);
            throw new RuntimeException("Failed to save game: " + e.getMessage(), e);
        }
    }
    
    @Override
    public GameStateInfo getGameState() {
        return new GameStateInfo(
            state.isGame(),
            state.isPaused(),
            state.gameOver,
            state.wave,
            state.enemies,
            state.rules
        );
    }
    
    @Override
    public void setPaused(boolean paused) {
        if (state.isGame()) {
            state.set(paused ? State.paused : State.playing);
        }
    }
    
    @Override
    public void setGameSpeed(float speed) {
        Time.setDeltaProvider(() -> Math.min(Core.graphics.getDeltaTime() * 60f * speed, 3f * speed));
    }
    
    @Override
    public PlayerController createPlayer(String name, Team team) {
        if (!running) {
            throw new IllegalStateException("Controller is not running");
        }
        
        var player = new PlayerControllerImpl(name, team);
        players.add(player);
        
        Log.info("Created AI player: @ on team @", name, team.name);
        return player;
    }
    
    @Override
    public Seq<PlayerController> getPlayers() {
        return players.copy();
    }
    
    @Override
    public PlayerController getPlayer(int id) {
        return players.find(p -> p.getId() == id);
    }
    
    @Override
    public void removePlayer(int playerId) {
        var player = getPlayer(playerId);
        if (player != null) {
            players.remove((PlayerController) player);
            ((PlayerControllerImpl) player).dispose();
        }
    }
    
    @Override
    public WorldController getWorld() {
        return worldController;
    }
    
    @Override
    public Seq<Map> getAvailableMaps() {
        return maps.all().copy();
    }
    
    @Override
    public Map generateMap(int width, int height, MapGenParams params) {
        // Create a basic random map
        var map = new Map(StringMap.of(
            "name", "Generated-" + System.currentTimeMillis(),
            "width", String.valueOf(width),
            "height", String.valueOf(height)
        ));
        
        // Basic terrain generation would go here
        // For now, return a simple map
        return map;
    }
    
    @Override
    public void addEventListener(String eventType, EventListener listener) {
        eventListeners.get(eventType, Seq::new).add(listener);
    }
    
    @Override
    public void removeEventListener(String eventType, EventListener listener) {
        var listeners = eventListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    @Override
    public void fireEvent(String eventType, Object data) {
        var listeners = eventListeners.get(eventType);
        if (listeners != null) {
            for (var listener : listeners) {
                try {
                    listener.onEvent(eventType, data);
                } catch (Exception e) {
                    Log.err("Error in event listener", e);
                }
            }
        }
    }
    
    @Override
    public AdminController getAdmin() {
        return adminController;
    }
    
    @Override
    public CommandResult executeCommand(String command) {
        try {
            if (netServer != null && netServer.clientCommands != null) {
                var response = netServer.clientCommands.handleMessage(command, null);
                return new CommandResult(true, response != null ? response.toString() : "Command executed");
            }
            return new CommandResult(false, "Server not available");
        } catch (Exception e) {
            return new CommandResult(false, "Command failed: " + e.getMessage());
        }
    }
    
    @Override
    public ServerStats getServerStats() {
        return new ServerStats(
            Groups.player.size(),
            state.enemies,
            state.wave,
            Core.graphics.getFramesPerSecond(),
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().totalMemory()
        );
    }
    
    @Override
    public void shutdown() {
        if (!running) return;
        
        running = false;
        
        // Dispose all players
        for (var player : players) {
            ((PlayerControllerImpl) player).dispose();
        }
        players.clear();
        
        // Clear event listeners
        eventListeners.clear();
        
        Log.info("Controller API shutdown complete");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public String getVersion() {
        return Version.combined();
    }
    
    // === Data Classes ===
    
    public static class GameStateInfo {
        public final boolean isGame;
        public final boolean isPaused;
        public final boolean isGameOver;
        public final int wave;
        public final int enemies;
        public final Rules rules;
        
        public GameStateInfo(boolean isGame, boolean isPaused, boolean isGameOver, 
                           int wave, int enemies, Rules rules) {
            this.isGame = isGame;
            this.isPaused = isPaused;
            this.isGameOver = isGameOver;
            this.wave = wave;
            this.enemies = enemies;
            this.rules = rules;
        }
    }
    
    public static class CommandResult {
        public final boolean success;
        public final String message;
        
        public CommandResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
    
    public static class ServerStats {
        public final int playerCount;
        public final int enemyCount;
        public final int wave;
        public final int fps;
        public final long memoryUsed;
        public final long memoryTotal;
        
        public ServerStats(int playerCount, int enemyCount, int wave, int fps, 
                         long memoryUsed, long memoryTotal) {
            this.playerCount = playerCount;
            this.enemyCount = enemyCount;
            this.wave = wave;
            this.fps = fps;
            this.memoryUsed = memoryUsed;
            this.memoryTotal = memoryTotal;
        }
    }
    
    public static class MapGenParams {
        public String terrain = "mixed";
        public float resourceDensity = 1.0f;
        public boolean hasEnemyBases = true;
        public int symmetry = 0; // 0 = none, 1 = point, 2 = line
    }
}

// === Interfaces ===

interface EventListener {
    void onEvent(String eventType, Object data);
}

interface PlayerEventListener {
    void onPlayerEvent(PlayerController player, String eventType, Object data);
}

package mindustry.api;

import arc.math.geom.*;
import arc.struct.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.maps.*;
import mindustry.net.Administration.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

/**
 * Main Controller API interface for headless game control.
 * Provides programmatic control over Mindustry game state and actions.
 */
public interface ControllerAPI {
    
    // === Game State Management ===
    
    /** Start a new game with the specified map and rules */
    GameController startGame(Map map, Rules rules);
    
    /** Load a saved game */
    GameController loadGame(String savePath);
    
    /** Save the current game state */
    void saveGame(String savePath);
    
    /** Get current game state information */
    GameStateInfo getGameState();
    
    /** Pause/unpause the game */
    void setPaused(boolean paused);
    
    /** Set game speed multiplier */
    void setGameSpeed(float speed);
    
    // === Player Management ===
    
    /** Create a new AI-controlled player */
    PlayerController createPlayer(String name, Team team);
    
    /** Get all players in the game */
    Seq<PlayerController> getPlayers();
    
    /** Get player by ID */
    PlayerController getPlayer(int id);
    
    /** Remove a player from the game */
    void removePlayer(int playerId);
    
    // === Map and World Access ===
    
    /** Get world information */
    WorldController getWorld();
    
    /** Get available maps */
    Seq<Map> getAvailableMaps();
    
    /** Generate a new random map */
    Map generateMap(int width, int height, MapGenParams params);
    
    // === Event System ===
    
    /** Register an event listener */
    void addEventListener(String eventType, EventListener listener);
    
    /** Remove an event listener */
    void removeEventListener(String eventType, EventListener listener);
    
    /** Fire a custom event */
    void fireEvent(String eventType, Object data);
    
    // === Server Control ===
    
    /** Get server administration interface */
    AdminController getAdmin();
    
    /** Execute a server command */
    CommandResult executeCommand(String command);
    
    /** Get server statistics */
    ServerStats getServerStats();
    
    // === Utility ===
    
    /** Shutdown the controller and cleanup resources */
    void shutdown();
    
    /** Check if the controller is running */
    boolean isRunning();
    
    /** Get API version */
    String getVersion();
}

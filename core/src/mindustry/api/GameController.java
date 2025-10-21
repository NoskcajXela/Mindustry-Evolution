package mindustry.api;

import arc.struct.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * Controller for game-wide state and management.
 */
public interface GameController {
    
    // === Game State ===
    
    /** Get current wave number */
    int getWave();
    
    /** Skip to next wave */
    void nextWave();
    
    /** Run wave immediately */
    void runWave();
    
    /** Get wave countdown time */
    float getWaveTime();
    
    /** Set wave countdown time */
    void setWaveTime(float time);
    
    /** Get game rules */
    Rules getRules();
    
    /** Update game rules */
    void setRules(Rules rules);
    
    /** Check if game is over */
    boolean isGameOver();
    
    /** Get winning team */
    Team getWinningTeam();
    
    /** Force game over with winner */
    void forceGameOver(Team winner);
    
    // === Teams ===
    
    /** Get all active teams */
    Seq<Team> getActiveTeams();
    
    /** Get team data */
    TeamData getTeamData(Team team);
    
    /** Get team's core buildings */
    Seq<Building> getTeamCores(Team team);
    
    /** Get team's units */
    Seq<Unit> getTeamUnits(Team team);
    
    /** Get team statistics */
    TeamStats getTeamStats(Team team);
    
    // === Spawning ===
    
    /** Spawn unit for team at position */
    Unit spawnUnit(UnitType type, Team team, float x, float y);
    
    /** Spawn effect at position */
    void spawnEffect(String effectName, float x, float y);
    
    /** Get enemy spawn points */
    Seq<Tile> getSpawnPoints();
    
    // === World Modification ===
    
    /** Set tile at position */
    void setTile(int x, int y, Block floor, Block block);
    
    /** Set floor tile */
    void setFloorTile(int x, int y, Block floor);
    
    /** Set block tile */
    void setBlockTile(int x, int y, Block block);
    
    /** Get tile at position */
    Tile getTile(int x, int y);
    
    /** Check if position is within world bounds */
    boolean inBounds(int x, int y);
    
    /** Get world width */
    int getWorldWidth();
    
    /** Get world height */
    int getWorldHeight();
    
    // === Resources and Items ===
    
    /** Add items to team's cores */
    void addTeamItems(Team team, Item item, int amount);
    
    /** Remove items from team's cores */
    void removeTeamItems(Team team, Item item, int amount);
    
    /** Get team's total item count */
    int getTeamItems(Team team, Item item);
    
    /** Set infinite resources for team */
    void setInfiniteResources(Team team, boolean infinite);
    
    // === Events and Triggers ===
    
    /** Fire a trigger event */
    void fireTrigger(String trigger);
    
    /** Add custom trigger listener */
    void addTriggerListener(String trigger, Runnable action);
    
    // === Time and Simulation ===
    
    /** Get current game tick */
    long getTick();
    
    /** Get game time in seconds */
    float getGameTime();
    
    /** Schedule action to run after delay */
    void scheduleAction(float delay, Runnable action);
    
    /** Run action every interval */
    void scheduleRepeating(float interval, Runnable action);
    
    // === Weather and Environment ===
    
    /** Start weather effect */
    void startWeather(String weatherType, float intensity, float duration);
    
    /** Stop all weather */
    void stopWeather();
    
    /** Get current weather effects */
    Seq<String> getActiveWeather();
    
    // === Logic and Control ===
    
    /** Execute logic code */
    void executeLogic(String code);
    
    /** Set global logic variable */
    void setLogicVar(String name, Object value);
    
    /** Get global logic variable */
    Object getLogicVar(String name);
    
    // === Debugging ===
    
    /** Enable/disable debug mode */
    void setDebugMode(boolean debug);
    
    /** Get game performance stats */
    PerformanceStats getPerformanceStats();
    
    /** Force garbage collection */
    void forceGC();
}

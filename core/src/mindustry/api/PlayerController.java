package mindustry.api;

import arc.math.geom.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

/**
 * Controller for managing individual players programmatically.
 */
public interface PlayerController {
    
    // === Basic Info ===
    
    /** Get player ID */
    int getId();
    
    /** Get player name */
    String getName();
    
    /** Set player name */
    void setName(String name);
    
    /** Get player team */
    Team getTeam();
    
    /** Set player team */
    void setTeam(Team team);
    
    /** Get player position */
    Vec2 getPosition();
    
    /** Check if player is alive */
    boolean isAlive();
    
    /** Check if player is dead */
    boolean isDead();
    
    /** Respawn the player */
    void respawn();
    
    // === Unit Control ===
    
    /** Get current controlled unit */
    Unit getUnit();
    
    /** Control a specific unit */
    void controlUnit(Unit unit);
    
    /** Clear unit control (return to core unit) */
    void clearUnitControl();
    
    /** Move unit to target position */
    void moveTo(float x, float y);
    
    /** Move unit in direction */
    void moveDirection(float x, float y);
    
    /** Set unit rotation */
    void setRotation(float rotation);
    
    /** Make unit aim at target */
    void aimAt(float x, float y);
    
    /** Start/stop shooting */
    void setShooting(boolean shooting);
    
    /** Set boost/sprint state */
    void setBoosting(boolean boosting);
    
    // === Building ===
    
    /** Place a block at the specified position */
    boolean placeBlock(Block block, int x, int y, int rotation);
    
    /** Break block at position */
    boolean breakBlock(int x, int y);
    
    /** Configure a building */
    boolean configureBuilding(int x, int y, Object config);
    
    /** Rotate a building */
    boolean rotateBuilding(int x, int y, boolean clockwise);
    
    /** Get building at position */
    Building getBuildingAt(int x, int y);
    
    /** Start building a queue of blocks */
    void addBuildPlan(Block block, int x, int y, int rotation);
    
    /** Clear all build plans */
    void clearBuildPlans();
    
    /** Get current build plans */
    Seq<BuildPlan> getBuildPlans();
    
    // === Items and Resources ===
    
    /** Get player's current item stack */
    ItemStack getItemStack();
    
    /** Drop current items */
    void dropItems();
    
    /** Pick up items from a building */
    boolean takeItems(Building building, Item item, int amount);
    
    /** Deposit items to a building */
    boolean depositItems(Building building);
    
    /** Transfer items to/from building */
    boolean transferItems(Building building);
    
    // === Mining ===
    
    /** Start mining at position */
    void startMining(int x, int y);
    
    /** Stop mining */
    void stopMining();
    
    /** Check if currently mining */
    boolean isMining();
    
    /** Get current mining target */
    Tile getMiningTarget();
    
    // === Combat ===
    
    /** Attack target unit */
    void attackUnit(Unit target);
    
    /** Attack building */
    void attackBuilding(Building target);
    
    /** Set target for unit */
    void setTarget(Posc target);
    
    /** Clear current target */
    void clearTarget();
    
    // === Command Mode (for controlling multiple units) ===
    
    /** Enter command mode */
    void enterCommandMode();
    
    /** Exit command mode */
    void exitCommandMode();
    
    /** Select units in rectangle */
    Seq<Unit> selectUnits(float x, float y, float width, float height);
    
    /** Command selected units to move to position */
    void commandUnitsMoveTo(float x, float y);
    
    /** Command selected units to attack target */
    void commandUnitsAttack(Posc target);
    
    /** Get currently selected units */
    Seq<Unit> getSelectedUnits();
    
    // === Information ===
    
    /** Get player statistics */
    PlayerStats getStats();
    
    /** Check if player can perform action at location */
    boolean canPerformAction(ActionType action, int x, int y);
    
    /** Get player's visible area */
    Rect getVisibleArea();
    
    // === Events ===
    
    /** Register player-specific event listener */
    void addEventListener(String eventType, PlayerEventListener listener);
    
    /** Remove player event listener */
    void removeEventListener(String eventType, PlayerEventListener listener);
}

package mindustry.api.impl;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.api.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.net.Administration.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

import static mindustry.Vars.*;

/**
 * Implementation of PlayerController for programmatic player control.
 */
public class PlayerControllerImpl implements PlayerController {
    
    private static int nextId = 1;
    
    private final int id;
    private final Player player;
    private final HeadlessInputHandler inputHandler;
    private final ObjectMap<String, Seq<PlayerEventListener>> eventListeners = new ObjectMap<>();
    
    public PlayerControllerImpl(String name, Team team) {
        this.id = nextId++;
        
        // Create player
        this.player = Player.create();
        this.player.name = name;
        this.player.team(team);
        
        // Create headless input handler
        this.inputHandler = new HeadlessInputHandler(this);
        
        // Add to game if active
        if (state.isGame()) {
            player.add();
        }
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return player.name;
    }
    
    @Override
    public void setName(String name) {
        player.name = name;
    }
    
    @Override
    public Team getTeam() {
        return player.team();
    }
    
    @Override
    public void setTeam(Team team) {
        player.team(team);
    }
    
    @Override
    public Vec2 getPosition() {
        return new Vec2(player.x, player.y);
    }
    
    @Override
    public boolean isAlive() {
        return !player.dead();
    }
    
    @Override
    public boolean isDead() {
        return player.dead();
    }
    
    @Override
    public void respawn() {
        if (player.dead()) {
            player.checkSpawn();
        }
    }
    
    @Override
    public Unit getUnit() {
        return player.unit();
    }
    
    @Override
    public void controlUnit(Unit unit) {
        if (unit != null && unit.team == player.team() && unit.isAI()) {
            Call.unitControl(player, unit);
        }
    }
    
    @Override
    public void clearUnitControl() {
        Call.unitClear(player);
    }
    
    @Override
    public void moveTo(float x, float y) {
        if (player.unit() != null) {
            var unit = player.unit();
            Vec2 target = new Vec2(x, y);
            unit.movePref(target.sub(unit.x, unit.y).nor().scl(unit.speed()));
        }
    }
    
    @Override
    public void moveDirection(float x, float y) {
        if (player.unit() != null) {
            var unit = player.unit();
            unit.movePref(new Vec2(x, y).nor().scl(unit.speed()));
        }
    }
    
    @Override
    public void setRotation(float rotation) {
        if (player.unit() != null) {
            player.unit().rotation(rotation);
        }
    }
    
    @Override
    public void aimAt(float x, float y) {
        if (player.unit() != null) {
            player.unit().aim(x, y);
        }
    }
    
    @Override
    public void setShooting(boolean shooting) {
        player.shooting = shooting;
        if (player.unit() != null) {
            player.unit().controlWeapons(true, shooting);
        }
    }
    
    @Override
    public void setBoosting(boolean boosting) {
        player.boosting = boosting;
    }
    
    @Override
    public boolean placeBlock(Block block, int x, int y, int rotation) {
        if (!player.isBuilder() || player.dead()) return false;
        
        // Check if placement is valid
        if (Build.validPlace(block, player.team(), x, y, rotation, true, true)) {
            // Add build plan
            player.unit().addBuild(new BuildPlan(x, y, rotation, block));
            return true;
        }
        return false;
    }
    
    @Override
    public boolean breakBlock(int x, int y) {
        if (!player.isBuilder() || player.dead()) return false;
        
        var tile = world.tile(x, y);
        if (tile != null && tile.build != null && Build.validBreak(player.team(), x, y)) {
            // Add break plan
            player.unit().addBuild(new BuildPlan(x, y));
            return true;
        }
        return false;
    }
    
    @Override
    public boolean configureBuilding(int x, int y, Object config) {
        var building = world.build(x, y);
        if (building != null && building.interactable(player.team())) {
            Call.tileConfig(player, building, config);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean rotateBuilding(int x, int y, boolean clockwise) {
        var building = world.build(x, y);
        if (building != null && building.interactable(player.team())) {
            Call.rotateBlock(player, building, clockwise);
            return true;
        }
        return false;
    }
    
    @Override
    public Building getBuildingAt(int x, int y) {
        return world.build(x, y);
    }
    
    @Override
    public void addBuildPlan(Block block, int x, int y, int rotation) {
        if (player.isBuilder() && !player.dead()) {
            player.unit().addBuild(new BuildPlan(x, y, rotation, block));
        }
    }
    
    @Override
    public void clearBuildPlans() {
        if (player.isBuilder() && !player.dead()) {
            player.unit().plans().clear();
        }
    }
    
    @Override
    public Seq<BuildPlan> getBuildPlans() {
        if (player.isBuilder() && !player.dead()) {
            return player.unit().plans().copy();
        }
        return new Seq<>();
    }
    
    @Override
    public ItemStack getItemStack() {
        if (player.unit() != null) {
            return new ItemStack(player.unit().item(), player.unit().stack.amount);
        }
        return ItemStack.empty;
    }
    
    @Override
    public void dropItems() {
        if (player.unit() != null && player.unit().stack.amount > 0) {
            Call.dropItem(player, player.unit().rotation);
        }
    }
    
    @Override
    public boolean takeItems(Building building, Item item, int amount) {
        if (building != null && building.items != null && building.items.has(item)) {
            Call.requestItem(player, building, item, amount);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean depositItems(Building building) {
        if (building != null && player.unit().stack.amount > 0) {
            Call.transferInventory(player, building);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean transferItems(Building building) {
        return depositItems(building);
    }
    
    @Override
    public void startMining(int x, int y) {
        var tile = world.tile(x, y);
        if (tile != null && player.unit() != null) {
            player.unit().mineTile = tile;
        }
    }
    
    @Override
    public void stopMining() {
        if (player.unit() != null) {
            player.unit().mineTile = null;
        }
    }
    
    @Override
    public boolean isMining() {
        return player.unit() != null && player.unit().mineTile != null;
    }
    
    @Override
    public Tile getMiningTarget() {
        return player.unit() != null ? player.unit().mineTile : null;
    }
    
    @Override
    public void attackUnit(Unit target) {
        if (player.unit() != null && target != null) {
            setTarget(target);
            setShooting(true);
        }
    }
    
    @Override
    public void attackBuilding(Building target) {
        if (player.unit() != null && target != null) {
            setTarget(target);
            setShooting(true);
        }
    }
    
    @Override
    public void setTarget(Posc target) {
        if (player.unit() != null && target != null) {
            aimAt(target.getX(), target.getY());
        }
    }
    
    @Override
    public void clearTarget() {
        setShooting(false);
    }
    
    @Override
    public void enterCommandMode() {
        inputHandler.commandMode = true;
    }
    
    @Override
    public void exitCommandMode() {
        inputHandler.commandMode = false;
        inputHandler.selectedUnits.clear();
    }
    
    @Override
    public Seq<Unit> selectUnits(float x, float y, float width, float height) {
        var units = inputHandler.selectedCommandUnits(x, y, width, height);
        inputHandler.selectedUnits.clear();
        inputHandler.selectedUnits.addAll(units);
        return units.copy();
    }
    
    @Override
    public void commandUnitsMoveTo(float x, float y) {
        if (!inputHandler.selectedUnits.isEmpty()) {
            for (var unit : inputHandler.selectedUnits) {
                unit.command().commandPosition(new Vec2(x, y));
            }
        }
    }
    
    @Override
    public void commandUnitsAttack(Posc target) {
        if (!inputHandler.selectedUnits.isEmpty() && target != null) {
            for (var unit : inputHandler.selectedUnits) {
                unit.command().commandTarget(target);
            }
        }
    }
    
    @Override
    public Seq<Unit> getSelectedUnits() {
        return inputHandler.selectedUnits.copy();
    }
    
    @Override
    public PlayerStats getStats() {
        return new PlayerStats(
            player.name,
            player.team(),
            isAlive(),
            getPosition(),
            player.unit() != null ? player.unit().type : null
        );
    }
    
    @Override
    public boolean canPerformAction(ActionType action, int x, int y) {
        return netServer.admins.allowAction(player, action, world.tile(x, y), a -> {});
    }
    
    @Override
    public Rect getVisibleArea() {
        // For AI players, assume they can see everything their team can see
        return new Rect(0, 0, world.width() * 8, world.height() * 8);
    }
    
    @Override
    public void addEventListener(String eventType, PlayerEventListener listener) {
        eventListeners.get(eventType, Seq::new).add(listener);
    }
    
    @Override
    public void removeEventListener(String eventType, PlayerEventListener listener) {
        var listeners = eventListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    public void fireEvent(String eventType, Object data) {
        var listeners = eventListeners.get(eventType);
        if (listeners != null) {
            for (var listener : listeners) {
                try {
                    listener.onPlayerEvent(this, eventType, data);
                } catch (Exception e) {
                    Log.err("Error in player event listener", e);
                }
            }
        }
    }
    
    public void dispose() {
        if (player.added) {
            player.remove();
        }
        eventListeners.clear();
    }
    
    // === Data Classes ===
    
    public static class PlayerStats {
        public final String name;
        public final Team team;
        public final boolean alive;
        public final Vec2 position;
        public final UnitType unitType;
        
        public PlayerStats(String name, Team team, boolean alive, Vec2 position, UnitType unitType) {
            this.name = name;
            this.team = team;
            this.alive = alive;
            this.position = position;
            this.unitType = unitType;
        }
    }
    
    // === Headless Input Handler ===
    
    private static class HeadlessInputHandler extends InputHandler {
        private final PlayerControllerImpl controller;
        
        public HeadlessInputHandler(PlayerControllerImpl controller) {
            this.controller = controller;
        }
        
        @Override
        public void update() {
            // Update player state
            if (controller.player.unit() != null) {
                controller.player.unit().controller(controller.player);
            }
        }
        
        @Override
        public boolean selectedBlock() {
            return false;
        }
        
        @Override
        public float getMouseX() {
            return 0;
        }
        
        @Override
        public float getMouseY() {
            return 0;
        }
        
        @Override
        public void updateState() {
            // No-op for headless
        }
        
        @Override
        public void panCamera(Vec2 position) {
            // No-op for headless
        }
    }
}

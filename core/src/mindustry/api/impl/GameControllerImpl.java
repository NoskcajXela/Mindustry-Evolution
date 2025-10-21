package mindustry.api.impl;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.api.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/**
 * Implementation of GameController for game state management.
 */
public class GameControllerImpl implements GameController {
    
    private final ControllerAPIImpl parent;
    private final ObjectMap<String, Seq<Runnable>> triggerListeners = new ObjectMap<>();
    
    public GameControllerImpl(ControllerAPIImpl parent) {
        this.parent = parent;
    }
    
    @Override
    public int getWave() {
        return state.wave;
    }
    
    @Override
    public void nextWave() {
        if (state.isGame() && state.rules.waves) {
            spawner.spawnEnemies();
            state.wave++;
            state.wavetime = state.rules.waveSpacing;
        }
    }
    
    @Override
    public void runWave() {
        if (state.isGame() && state.rules.waves) {
            logic.runWave();
        }
    }
    
    @Override
    public float getWaveTime() {
        return state.wavetime;
    }
    
    @Override
    public void setWaveTime(float time) {
        state.wavetime = time;
    }
    
    @Override
    public Rules getRules() {
        return state.rules.copy();
    }
    
    @Override
    public void setRules(Rules rules) {
        state.rules = rules;
    }
    
    @Override
    public boolean isGameOver() {
        return state.gameOver;
    }
    
    @Override
    public Team getWinningTeam() {
        if (state.gameOver) {
            // Find the team that's still alive
            return state.teams.getActive().find(t -> t.isAlive())?.team;
        }
        return null;
    }
    
    @Override
    public void forceGameOver(Team winner) {
        logic.updateGameOver(winner);
    }
    
    @Override
    public Seq<Team> getActiveTeams() {
        return state.teams.getActive().map(t -> t.team);
    }
    
    @Override
    public TeamData getTeamData(Team team) {
        return state.teams.get(team);
    }
    
    @Override
    public Seq<Building> getTeamCores(Team team) {
        return team.data().buildings.select(b -> b instanceof CoreBlock.CoreBuild);
    }
    
    @Override
    public Seq<Unit> getTeamUnits(Team team) {
        return team.data().units.copy();
    }
    
    @Override
    public TeamStats getTeamStats(Team team) {
        var data = team.data();
        return new TeamStats(
            team,
            data.units.size,
            data.buildings.size,
            data.cores.size,
            data.unitCount,
            data.buildingCount
        );
    }
    
    @Override
    public Unit spawnUnit(UnitType type, Team team, float x, float y) {
        var unit = type.create(team);
        unit.set(x, y);
        unit.add();
        return unit;
    }
    
    @Override
    public void spawnEffect(String effectName, float x, float y) {
        try {
            var effect = content.getByName(ContentType.effect, effectName);
            if (effect != null) {
                // Create effect at position - this would need proper effect system integration
                Log.info("Spawning effect @ at @, @", effectName, x, y);
            }
        } catch (Exception e) {
            Log.err("Failed to spawn effect: @", effectName, e);
        }
    }
    
    @Override
    public Seq<Tile> getSpawnPoints() {
        var spawns = new Seq<Tile>();
        if (spawner != null) {
            spawner.getSpawns().each(spawns::add);
        }
        return spawns;
    }
    
    @Override
    public void setTile(int x, int y, Block floor, Block block) {
        var tile = world.tile(x, y);
        if (tile != null) {
            if (floor != null) {
                tile.setFloor(floor.asFloor());
            }
            if (block != null) {
                tile.setBlock(block);
            }
        }
    }
    
    @Override
    public void setFloorTile(int x, int y, Block floor) {
        var tile = world.tile(x, y);
        if (tile != null && floor != null) {
            tile.setFloor(floor.asFloor());
        }
    }
    
    @Override
    public void setBlockTile(int x, int y, Block block) {
        var tile = world.tile(x, y);
        if (tile != null) {
            if (block != null) {
                tile.setBlock(block);
            } else {
                tile.setBlock(Blocks.air);
            }
        }
    }
    
    @Override
    public Tile getTile(int x, int y) {
        return world.tile(x, y);
    }
    
    @Override
    public boolean inBounds(int x, int y) {
        return world.tiles != null && x >= 0 && y >= 0 && x < world.width() && y < world.height();
    }
    
    @Override
    public int getWorldWidth() {
        return world.width();
    }
    
    @Override
    public int getWorldHeight() {
        return world.height();
    }
    
    @Override
    public void addTeamItems(Team team, Item item, int amount) {
        var cores = getTeamCores(team);
        if (!cores.isEmpty()) {
            var core = (CoreBlock.CoreBuild) cores.first();
            core.items.add(item, amount);
        }
    }
    
    @Override
    public void removeTeamItems(Team team, Item item, int amount) {
        var cores = getTeamCores(team);
        if (!cores.isEmpty()) {
            var core = (CoreBlock.CoreBuild) cores.first();
            core.items.remove(item, amount);
        }
    }
    
    @Override
    public int getTeamItems(Team team, Item item) {
        var cores = getTeamCores(team);
        int total = 0;
        for (var building : cores) {
            var core = (CoreBlock.CoreBuild) building;
            total += core.items.get(item);
        }
        return total;
    }
    
    @Override
    public void setInfiniteResources(Team team, boolean infinite) {
        if (infinite) {
            team.rules().infiniteResources = true;
        } else {
            team.rules().infiniteResources = false;
        }
    }
    
    @Override
    public void fireTrigger(String trigger) {
        var listeners = triggerListeners.get(trigger);
        if (listeners != null) {
            for (var listener : listeners) {
                try {
                    listener.run();
                } catch (Exception e) {
                    Log.err("Error in trigger listener", e);
                }
            }
        }
    }
    
    @Override
    public void addTriggerListener(String trigger, Runnable action) {
        triggerListeners.get(trigger, Seq::new).add(action);
    }
    
    @Override
    public long getTick() {
        return (long) state.tick;
    }
    
    @Override
    public float getGameTime() {
        return state.tick / 60f; // Convert ticks to seconds
    }
    
    @Override
    public void scheduleAction(float delay, Runnable action) {
        Timer.schedule(action, delay);
    }
    
    @Override
    public void scheduleRepeating(float interval, Runnable action) {
        Timer.schedule(action, 0, interval);
    }
    
    @Override
    public void startWeather(String weatherType, float intensity, float duration) {
        try {
            var weather = content.getByName(ContentType.weather, weatherType);
            if (weather != null) {
                // Create weather event - this would need proper weather system integration
                Log.info("Starting weather @ with intensity @ for @ seconds", weatherType, intensity, duration);
            }
        } catch (Exception e) {
            Log.err("Failed to start weather: @", weatherType, e);
        }
    }
    
    @Override
    public void stopWeather() {
        // Stop all active weather effects
        Log.info("Stopping all weather effects");
    }
    
    @Override
    public Seq<String> getActiveWeather() {
        // Return list of active weather effects
        return new Seq<>();
    }
    
    @Override
    public void executeLogic(String code) {
        try {
            // Execute logic processor code - would need integration with logic system
            Log.info("Executing logic code: @", code);
        } catch (Exception e) {
            Log.err("Failed to execute logic code", e);
        }
    }
    
    @Override
    public void setLogicVar(String name, Object value) {
        if (logicVars != null) {
            // Set global logic variable
            Log.info("Setting logic variable @ = @", name, value);
        }
    }
    
    @Override
    public Object getLogicVar(String name) {
        if (logicVars != null) {
            // Get global logic variable
            return null; // Would need proper implementation
        }
        return null;
    }
    
    @Override
    public void setDebugMode(boolean debug) {
        // Enable/disable debug features
        Log.info("Debug mode: @", debug);
    }
    
    @Override
    public PerformanceStats getPerformanceStats() {
        Runtime runtime = Runtime.getRuntime();
        return new PerformanceStats(
            Core.graphics.getFramesPerSecond(),
            runtime.totalMemory() - runtime.freeMemory(),
            runtime.totalMemory(),
            Thread.activeCount(),
            state.enemies,
            Groups.unit.size()
        );
    }
    
    @Override
    public void forceGC() {
        System.gc();
    }
    
    // === Data Classes ===
    
    public static class TeamStats {
        public final Team team;
        public final int unitCount;
        public final int buildingCount;
        public final int coreCount;
        public final int totalUnits;
        public final int totalBuildings;
        
        public TeamStats(Team team, int unitCount, int buildingCount, int coreCount, 
                        int totalUnits, int totalBuildings) {
            this.team = team;
            this.unitCount = unitCount;
            this.buildingCount = buildingCount;
            this.coreCount = coreCount;
            this.totalUnits = totalUnits;
            this.totalBuildings = totalBuildings;
        }
    }
    
    public static class PerformanceStats {
        public final int fps;
        public final long memoryUsed;
        public final long memoryTotal;
        public final int threads;
        public final int enemies;
        public final int totalUnits;
        
        public PerformanceStats(int fps, long memoryUsed, long memoryTotal, 
                              int threads, int enemies, int totalUnits) {
            this.fps = fps;
            this.memoryUsed = memoryUsed;
            this.memoryTotal = memoryTotal;
            this.threads = threads;
            this.enemies = enemies;
            this.totalUnits = totalUnits;
        }
    }
}

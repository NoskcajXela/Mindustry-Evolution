package mindustry.ai.evolutionary.behavior;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.evolutionary.genome.*;
import mindustry.api.*;
import mindustry.api.GameStatsAPI.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/**
 * AI behavior implementation based on evolved genome parameters.
 * Translates genome traits into actual game actions and strategies.
 */
public class GenomeBasedAI {
    
    private final PlayerController player;
    private final AIGenome genome;
    private final ControllerAPI controllerAPI;
    private final GameStatsAPI statsAPI;
    private final WorldController worldController;
    
    // AI state tracking
    private long lastUpdateTime = 0;
    private final Seq<BuildPlan> buildQueue = new Seq<>();
    private final ObjectMap<String, Long> lastActionTimes = new ObjectMap<>(); 
    private final BehaviorStats behaviorStats = new BehaviorStats();
    
    // Strategy state
    private AIPhase currentPhase = AIPhase.EARLY_GAME;
    private Vec2 corePosition = new Vec2();
    private final Seq<Vec2> expansionTargets = new Seq<>();
    private final Seq<Vec2> defensePoints = new Seq<>();
    
    public GenomeBasedAI(PlayerController player, AIGenome genome, 
                        ControllerAPI controllerAPI, GameStatsAPI statsAPI) {
        this.player = player;
        this.genome = genome;
        this.controllerAPI = controllerAPI;
        this.statsAPI = statsAPI;
        this.worldController = controllerAPI.getWorld();
        
        // Initialize core position
        updateCorePosition();
        
        Log.info("GenomeBasedAI initialized for player: " + player.getName());
        Log.info("Genome traits: " + genome.toString());
    }
    
    /**
     * Main update loop for AI behavior.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        
        // Update based on reaction speed gene
        long updateInterval = (long) (1000 / (genome.reactionSpeed * 10 + 1)); // 0.1-10 Hz
        if (currentTime - lastUpdateTime < updateInterval) {
            return;
        }
        
        lastUpdateTime = currentTime;
        
        try {
            // Update AI state
            updateGamePhase();
            updateCorePosition();
            updateExpansionTargets();
            
            // Execute AI behaviors based on current phase
            switch (currentPhase) {
                case EARLY_GAME -> executeEarlyGameStrategy();
                case MID_GAME -> executeMidGameStrategy();
                case LATE_GAME -> executeLateGameStrategy();
                case SURVIVAL -> executeSurvivalStrategy();
            }
            
            // Execute queued builds
            executeBuildQueue();
            
            // Update behavior statistics
            updateBehaviorStats();
            
        } catch (Exception e) {
            Log.warn("AI update failed: " + e.getMessage());
            behaviorStats.errorCount++;
        }
    }
    
    private void executeEarlyGameStrategy() {
        // Early game: Focus on basic resource extraction and power
        
        // 1. Ensure core is built
        ensureCoreBuilt();
        
        // 2. Build basic resource extraction
        if (shouldPerformAction("mining", 2000)) {
            buildResourceExtraction();
        }
        
        // 3. Establish basic power generation
        if (shouldPerformAction("power", 3000)) {
            buildPowerGeneration();
        }
        
        // 4. Basic transport infrastructure
        if (shouldPerformAction("transport", 5000)) {
            buildTransportInfrastructure();
        }
        
        // 5. Minimal defense
        if (shouldPerformAction("defense", 8000)) {
            buildBasicDefense();
        }
    }
    
    private void executeMidGameStrategy() {
        // Mid game: Expand production and strengthen defense
        
        // 1. Expand resource extraction
        if (shouldPerformAction("expansion", 5000)) {
            expandResourceExtraction();
        }
        
        // 2. Build production chains
        if (shouldPerformAction("production", 4000)) {
            buildProductionChains();
        }
        
        // 3. Strengthen defense
        if (shouldPerformAction("defense", 6000)) {
            upgradeDefenses();
        }
        
        // 4. Optimize transport
        if (shouldPerformAction("transport", 7000)) {
            optimizeTransport();
        }
        
        // 5. Unit production
        if (shouldPerformAction("units", 10000)) {
            buildUnitProduction();
        }
    }
    
    private void executeLateGameStrategy() {
        // Late game: Advanced technology and overwhelming force
        
        // 1. Advanced production
        if (shouldPerformAction("advanced_production", 8000)) {
            buildAdvancedProduction();
        }
        
        // 2. Heavy defense
        if (shouldPerformAction("heavy_defense", 7000)) {
            buildHeavyDefense();
        }
        
        // 3. Advanced units
        if (shouldPerformAction("advanced_units", 12000)) {
            buildAdvancedUnits();
        }
        
        // 4. Territory expansion
        if (shouldPerformAction("territory", 15000)) {
            expandTerritory();
        }
    }
    
    private void executeSurvivalStrategy() {
        // Survival mode: Focus on immediate threats and resource conservation
        
        // 1. Emergency power
        if (statsAPI.isCurrentlyInPowerShortage()) {
            buildEmergencyPower();
        }
        
        // 2. Immediate defense
        buildImmediateDefense();
        
        // 3. Resource conservation
        optimizeResourceUsage();
        
        // 4. Repair critical infrastructure
        repairCriticalBuildings();
    }
    
    private void ensureCoreBuilt() {
        // Check if we have a core
        var cores = player.getTeam().cores();
        if (cores.isEmpty()) {
            // Find suitable location for core
            Vec2 pos = findBestCoreLocation();
            if (pos != null) {
                Block coreType = genome.getHighestPriorityBuilding(
                    Seq.with(Blocks.coreNucleus, Blocks.coreFortress, Blocks.coreBastion));
                queueBuild(coreType, (int)pos.x, (int)pos.y, 0);
                behaviorStats.coresBuilt++;
            }
        } else {
            corePosition.set(cores.first());
        }
    }
    
    private void buildResourceExtraction() {
        // Find resource deposits near core
        var resourceTiles = findNearbyResources(corePosition, 15f);
        
        for (Vec2 tile : resourceTiles) {
            if (canBuildAt((int)tile.x, (int)tile.y)) {
                // Choose drill type based on genome preferences and tech level
                Block drill = chooseBestDrill();
                if (drill != null) {
                    queueBuild(drill, (int)tile.x, (int)tile.y, 0);
                    behaviorStats.miningBuilt++;
                    
                    // Connect to transport network if transport efficiency is high
                    if (genome.transportEfficiency > 0.6f) {
                        connectToTransportNetwork(tile);
                    }
                }
            }
        }
    }
    
    private void buildPowerGeneration() {
        var powerSummary = statsAPI.getPowerSummary();
        
        // Calculate power needed based on genome power buffer target
        float targetBuffer = genome.powerBufferTarget;
        float currentRatio = powerSummary.efficiency;
        
        if (currentRatio < (1f - targetBuffer)) {
            // Find good location for power generation
            Vec2 location = findBestPowerLocation();
            if (location != null) {
                Block generator = chooseBestGenerator();
                if (generator != null) {
                    queueBuild(generator, (int)location.x, (int)location.y, 0);
                    behaviorStats.powerBuilt++;
                    
                    // Connect to power network
                    connectToPowerNetwork(location);
                }
            }
        }
    }
    
    private void buildTransportInfrastructure() {
        // Build conveyors based on transport efficiency gene
        if (genome.transportEfficiency > 0.5f) {
            // Connect mining sites to core
            var drills = findNearbyBuildings(corePosition, 20f, 
                b -> b.block.category == Category.production);
            
            for (var drill : drills) {
                if (!isConnectedToCore(drill.tile.pos())) {
                    buildConveyorPath(drill.tile.pos(), corePosition);
                    behaviorStats.transportBuilt++;
                }
            }
        }
    }
    
    private void buildBasicDefense() {
        // Build defensive turrets based on defensive bias
        float defensiveStrength = genome.defensiveBias;
        int turretsNeeded = (int) (defensiveStrength * 8 + 2); // 2-10 turrets
        
        var currentTurrets = findNearbyBuildings(corePosition, 15f,
            b -> b.block.category == Category.turret);
        
        if (currentTurrets.size < turretsNeeded) {
            Vec2 defensePos = findBestDefenseLocation();
            if (defensePos != null) {
                Block turret = chooseBestTurret();
                if (turret != null) {
                    queueBuild(turret, (int)defensePos.x, (int)defensePos.y, 0);
                    behaviorStats.defenseBuilt++;
                }
            }
        }
    }
    
    private Vec2 findBestCoreLocation() {
        // Simple heuristic: find open area with resources nearby
        for (int attempts = 0; attempts < 20; attempts++) {
            int x = (int) (Math.random() * world.width());
            int y = (int) (Math.random() * world.height());
            
            if (canBuildAt(x, y) && hasResourcesNearby(x, y, 10f)) {
                return new Vec2(x, y);
            }
        }
        return null;
    }
    
    private Seq<Vec2> findNearbyResources(Vec2 center, float radius) {
        var resources = new Seq<Vec2>();
        
        int minX = Math.max(0, (int)(center.x - radius));
        int maxX = Math.min(world.width() - 1, (int)(center.x + radius));
        int minY = Math.max(0, (int)(center.y - radius));
        int maxY = Math.min(world.height() - 1, (int)(center.y + radius));
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                var tile = world.tile(x, y);
                if (tile != null && tile.drop() != null && tile.drop() != Items.scrap) {
                    if (center.dst(x, y) <= radius) {
                        resources.add(new Vec2(x, y));
                    }
                }
            }
        }
        
        return resources;
    }
    
    private Block chooseBestDrill() {
        // Choose drill based on genome preferences and available resources
        var drills = Seq.with(Blocks.mechanicalDrill, Blocks.pneumaticDrill, 
                             Blocks.laserDrill, Blocks.blastDrill);
        
        // Filter by what we can afford and have researched
        drills.removeAll(drill -> !canAfford(drill) || !hasResearched(drill));
        
        if (drills.isEmpty()) return Blocks.mechanicalDrill; // Fallback
        
        return genome.getHighestPriorityBuilding(drills);
    }
    
    private Block chooseBestGenerator() {
        var generators = Seq.with(Blocks.combustionGenerator, Blocks.steamGenerator,
                                 Blocks.differentialGenerator, Blocks.rtgGenerator);
        
        generators.removeAll(gen -> !canAfford(gen) || !hasResearched(gen));
        
        if (generators.isEmpty()) return Blocks.combustionGenerator; // Fallback
        
        return genome.getHighestPriorityBuilding(generators);
    }
    
    private Block chooseBestTurret() {
        var turrets = Seq.with(Blocks.duo, Blocks.scatter, Blocks.hail, Blocks.wave,
                             Blocks.lancer, Blocks.arc, Blocks.swarmer, Blocks.salvo);
        
        turrets.removeAll(turret -> !canAfford(turret) || !hasResearched(turret));
        
        if (turrets.isEmpty()) return Blocks.duo; // Fallback
        
        // Filter by combat preference (ranged vs close)
        if (genome.combatRangePreference > 0.6f) {
            turrets.removeAll(t -> t.range < 10f);
        }
        
        return genome.getHighestPriorityBuilding(turrets);
    }
    
    private void queueBuild(Block block, int x, int y, int rotation) {
        var plan = new BuildPlan();
        plan.block = block;
        plan.x = x;
        plan.y = y;
        plan.rotation = rotation;
        buildQueue.add(plan);
    }
    
    private void executeBuildQueue() {
        // Execute builds based on building pacing gene
        int buildsPerUpdate = (int) (genome.buildingPacing * 3 + 1); // 1-4 builds per update
        
        for (int i = 0; i < Math.min(buildsPerUpdate, buildQueue.size); i++) {
            var plan = buildQueue.get(0);
            buildQueue.remove(0);
            
            try {
                if (canBuildAt(plan.x, plan.y) && canAfford(plan.block)) {
                    player.placeBlock(plan.block, plan.x, plan.y, plan.rotation);
                    behaviorStats.totalBuilt++;
                }
            } catch (Exception e) {
                Log.warn("Failed to execute build: " + e.getMessage());
                behaviorStats.buildFailures++;
            }
        }
    }
    
    private boolean canBuildAt(int x, int y) {
        var tile = world.tile(x, y);
        return tile != null && tile.build == null && !tile.solid();
    }
    
    private boolean canAfford(Block block) {
        // Simple affordability check - in real implementation would check resources
        return true; // Placeholder
    }
    
    private boolean hasResearched(Block block) {
        // Simple research check - in real implementation would check tech tree
        return true; // Placeholder
    }
    
    private boolean shouldPerformAction(String action, long cooldownMs) {
        long lastTime = lastActionTimes.get(action, 0L);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastTime >= cooldownMs) {
            lastActionTimes.put(action, currentTime);
            return true;
        }
        
        return false;
    }
    
    private void updateGamePhase() {
        var performance = statsAPI.getPerformanceSummary();
        int wave = performance.currentWave;
        long gameTime = performance.currentTime;
        
        // Determine game phase based on wave and time
        if (wave < 10 && gameTime < 18000) { // First 5 minutes
            currentPhase = AIPhase.EARLY_GAME;
        } else if (wave < 30 && gameTime < 72000) { // Up to 20 minutes
            currentPhase = AIPhase.MID_GAME;
        } else if (wave < 60) {
            currentPhase = AIPhase.LATE_GAME;
        } else {
            currentPhase = AIPhase.SURVIVAL;
        }
        
        // Emergency survival mode if under severe threat
        var power = statsAPI.getPowerSummary();
        if (power.efficiency < 0.3f || statsAPI.isCoreAtCapacity()) {
            currentPhase = AIPhase.SURVIVAL;
        }
    }
    
    private void updateCorePosition() {
        var cores = player.getTeam().cores();
        if (!cores.isEmpty()) {
            corePosition.set(cores.first());
        }
    }
    
    private void updateExpansionTargets() {
        // Update expansion targets based on genome expansiveness
        // Implementation would analyze map for good expansion locations
    }
    
    private void updateBehaviorStats() {
        behaviorStats.updateCount++;
        behaviorStats.currentPhase = currentPhase;
        behaviorStats.buildQueueSize = buildQueue.size;
    }
    
    // Placeholder methods for more complex behaviors
    private void expandResourceExtraction() { /* TODO */ }
    private void buildProductionChains() { /* TODO */ }
    private void upgradeDefenses() { /* TODO */ }
    private void optimizeTransport() { /* TODO */ }
    private void buildUnitProduction() { /* TODO */ }
    private void buildAdvancedProduction() { /* TODO */ }
    private void buildHeavyDefense() { /* TODO */ }
    private void buildAdvancedUnits() { /* TODO */ }
    private void expandTerritory() { /* TODO */ }
    private void buildEmergencyPower() { /* TODO */ }
    private void buildImmediateDefense() { /* TODO */ }
    private void optimizeResourceUsage() { /* TODO */ }
    private void repairCriticalBuildings() { /* TODO */ }
    
    private Vec2 findBestPowerLocation() { return new Vec2(corePosition).add(5, 5); }
    private Vec2 findBestDefenseLocation() { return new Vec2(corePosition).add(8, 0); }
    private void connectToPowerNetwork(Vec2 location) { /* TODO */ }
    private void connectToTransportNetwork(Vec2 location) { /* TODO */ }
    private Seq<Building> findNearbyBuildings(Vec2 center, float radius, 
                                            java.util.function.Predicate<Building> filter) { return new Seq<>(); }
    private boolean isConnectedToCore(Vec2 position) { return false; }
    private void buildConveyorPath(Vec2 from, Vec2 to) { /* TODO */ }
    private boolean hasResourcesNearby(int x, int y, float radius) { return true; }
    
    public BehaviorStats getBehaviorStats() {
        return behaviorStats;
    }
    
    // === Inner Classes ===
    
    private enum AIPhase {
        EARLY_GAME, MID_GAME, LATE_GAME, SURVIVAL
    }
    
    private static class BuildPlan {
        Block block;
        int x, y, rotation;
    }
    
    public static class BehaviorStats {
        public int updateCount = 0;
        public int totalBuilt = 0;
        public int buildFailures = 0;
        public int errorCount = 0;
        public int buildQueueSize = 0;
        public int miningBuilt = 0;
        public int powerBuilt = 0;
        public int defenseBuilt = 0;
        public int transportBuilt = 0;
        public int coresBuilt = 0;
        public AIPhase currentPhase = AIPhase.EARLY_GAME;
        
        public float getBuildSuccessRate() {
            int total = totalBuilt + buildFailures;
            return total > 0 ? totalBuilt / (float) total : 0f;
        }
    }
}

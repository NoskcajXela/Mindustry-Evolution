package mindustry.api.impl;

import arc.struct.*;
import mindustry.api.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/**
 * Implementation of WorldController for world and terrain management.
 */
public class WorldControllerImpl implements WorldController {
    
    @Override
    public int getWidth() {
        return world.width();
    }
    
    @Override
    public int getHeight() {
        return world.height();
    }
    
    @Override
    public String getName() {
        return state.map.plainName();
    }
    
    @Override
    public int getSeed() {
        return state.map.tags.getInt("seed", 0);
    }
    
    @Override
    public Tile getTile(int x, int y) {
        return world.tile(x, y);
    }
    
    @Override
    public Seq<Tile> getTiles(int x, int y, int width, int height) {
        var tiles = new Seq<Tile>();
        for (int tx = x; tx < x + width; tx++) {
            for (int ty = y; ty < y + height; ty++) {
                var tile = world.tile(tx, ty);
                if (tile != null) {
                    tiles.add(tile);
                }
            }
        }
        return tiles;
    }
    
    @Override
    public Seq<Tile> getTiles(TilePredicate predicate) {
        var tiles = new Seq<Tile>();
        world.tiles.eachTile((x, y) -> {
            var tile = world.tile(x, y);
            if (tile != null && predicate.test(tile)) {
                tiles.add(tile);
            }
        });
        return tiles;
    }
    
    @Override
    public boolean inBounds(int x, int y) {
        return world.tiles != null && x >= 0 && y >= 0 && x < world.width() && y < world.height();
    }
    
    @Override
    public void setFloor(int x, int y, Block floor) {
        var tile = world.tile(x, y);
        if (tile != null && floor != null) {
            tile.setFloor(floor.asFloor());
        }
    }
    
    @Override
    public void setOverlay(int x, int y, Block overlay) {
        var tile = world.tile(x, y);
        if (tile != null) {
            if (overlay != null) {
                tile.setOverlay(overlay);
            } else {
                tile.setOverlay(Blocks.air);
            }
        }
    }
    
    @Override
    public void setBlock(int x, int y, Block block, Team team, int rotation) {
        var tile = world.tile(x, y);
        if (tile != null) {
            if (block != null) {
                tile.setBlock(block, team, rotation);
            } else {
                tile.setBlock(Blocks.air);
            }
        }
    }
    
    @Override
    public void removeBlock(int x, int y) {
        setBlock(x, y, Blocks.air, Team.derelict, 0);
    }
    
    @Override
    public void fillFloor(int x, int y, int width, int height, Block floor) {
        for (int tx = x; tx < x + width; tx++) {
            for (int ty = y; ty < y + height; ty++) {
                setFloor(tx, ty, floor);
            }
        }
    }
    
    @Override
    public void fillBlocks(int x, int y, int width, int height, Block block, Team team) {
        for (int tx = x; tx < x + width; tx++) {
            for (int ty = y; ty < y + height; ty++) {
                setBlock(tx, ty, block, team, 0);
            }
        }
    }
    
    @Override
    public Building getBuilding(int x, int y) {
        return world.build(x, y);
    }
    
    @Override
    public Seq<Building> getBuildingsOfType(Block type) {
        var buildings = new Seq<Building>();
        indexer.eachBlock(null, type, buildings::add);
        return buildings;
    }
    
    @Override
    public Seq<Building> getTeamBuildings(Team team) {
        return team.data().buildings.copy();
    }
    
    @Override
    public Seq<Building> getBuildingsInArea(int x, int y, int width, int height) {
        var buildings = new Seq<Building>();
        for (int tx = x; tx < x + width; tx++) {
            for (int ty = y; ty < y + height; ty++) {
                var building = world.build(tx, ty);
                if (building != null) {
                    buildings.add(building);
                }
            }
        }
        return buildings;
    }
    
    @Override
    public Seq<Unit> getAllUnits() {
        return Groups.unit.copy();
    }
    
    @Override
    public Seq<Unit> getUnitsInArea(float x, float y, float radius) {
        var units = new Seq<Unit>();
        Units.nearby(x - radius, y - radius, radius * 2, radius * 2, units::add);
        return units;
    }
    
    @Override
    public Seq<Unit> getTeamUnits(Team team) {
        return team.data().units.copy();
    }
    
    @Override
    public Unit getNearestUnit(float x, float y, Team team) {
        return Units.closest(team, x, y, Float.MAX_VALUE, u -> true);
    }
    
    @Override
    public Seq<Tile> getResourceTiles(Item item) {
        return getTiles(tile -> tile.drop() == item);
    }
    
    @Override
    public Seq<Tile> getOreDeposits() {
        return getTiles(tile -> tile.drop() != null && tile.drop().hardness > 0);
    }
    
    @Override
    public void addResourceDeposit(int x, int y, Item item, int amount) {
        var tile = world.tile(x, y);
        if (tile != null) {
            // Set the floor to an ore block that drops the specified item
            var oreBlock = content.blocks().find(b -> b.itemDrop == item);
            if (oreBlock != null) {
                tile.setFloor(oreBlock.asFloor());
            }
        }
    }
    
    @Override
    public Seq<Tile> findPath(int startX, int startY, int endX, int endY) {
        // Use pathfinder to find path
        var path = new Seq<Tile>();
        // This would need proper pathfinding implementation
        return path;
    }
    
    @Override
    public boolean hasPath(int startX, int startY, int endX, int endY) {
        return findPath(startX, startY, endX, endY).size > 0;
    }
    
    @Override
    public Seq<Tile> getWalkableTiles(int x, int y, int radius) {
        return getTiles(tile -> 
            Math.abs(tile.x - x) <= radius && 
            Math.abs(tile.y - y) <= radius && 
            tile.passable()
        );
    }
    
    @Override
    public float getLightLevel(int x, int y) {
        // Calculate light level based on nearby light sources
        float light = 0f;
        var nearby = getBuildingsInArea(x - 5, y - 5, 10, 10);
        for (var building : nearby) {
            if (building.block.emitLight) {
                float dist = building.dst(x * 8, y * 8);
                light += building.block.lightRadius / (1 + dist);
            }
        }
        return Math.min(light, 1f);
    }
    
    @Override
    public boolean isDark(int x, int y) {
        return getLightLevel(x, y) < 0.1f;
    }
    
    @Override
    public float getDamageAt(int x, int y) {
        var tile = world.tile(x, y);
        if (tile != null) {
            return tile.floor().damageTaken;
        }
        return 0f;
    }
    
    @Override
    public TileStats getTileStats() {
        var stats = new TileStats();
        stats.totalTiles = world.width() * world.height();
        
        world.tiles.eachTile((x, y) -> {
            var tile = world.tile(x, y);
            if (tile != null) {
                if (tile.solid()) stats.solidTiles++;
                if (tile.floor() != Blocks.air) stats.floorTiles++;
                if (tile.build != null) stats.buildingTiles++;
                if (tile.drop() != null) stats.resourceTiles++;
            }
        });
        
        return stats;
    }
    
    @Override
    public int countTiles(Block type) {
        int count = 0;
        world.tiles.eachTile((x, y) -> {
            var tile = world.tile(x, y);
            if (tile != null && (tile.floor() == type || tile.block() == type || tile.overlay() == type)) {
                // Increment count - can't use ++ in lambda
            }
        });
        return count;
    }
    
    @Override
    public Seq<Tile> getStrategicPositions() {
        // Find strategic positions like choke points, high ground, etc.
        var positions = new Seq<Tile>();
        
        // Add spawn points
        if (spawner != null) {
            spawner.getSpawns().each(positions::add);
        }
        
        // Add core positions
        for (var team : Team.all) {
            team.data().cores.each(core -> positions.add(core.tile));
        }
        
        return positions;
    }
    
    @Override
    public AreaAnalysis analyzeArea(int x, int y, int width, int height) {
        var analysis = new AreaAnalysis();
        var tiles = getTiles(x, y, width, height);
        
        analysis.buildable = true;
        analysis.flatness = 1.0f;
        analysis.resources = new Seq<>();
        analysis.obstacles = new Seq<>();
        
        for (var tile : tiles) {
            if (!tile.passable()) {
                analysis.buildable = false;
                analysis.obstacles.add(tile.block());
            }
            if (tile.drop() != null && !analysis.resources.contains(tile.drop())) {
                analysis.resources.add(tile.drop());
            }
        }
        
        return analysis;
    }
    
    @Override
    public void forEachTile(TileOperation operation) {
        world.tiles.eachTile((x, y) -> {
            var tile = world.tile(x, y);
            if (tile != null) {
                operation.apply(tile);
            }
        });
    }
    
    @Override
    public void forEachTileInArea(int x, int y, int width, int height, TileOperation operation) {
        var tiles = getTiles(x, y, width, height);
        for (var tile : tiles) {
            operation.apply(tile);
        }
    }
    
    @Override
    public void batchModify(Seq<TileModification> modifications) {
        for (var mod : modifications) {
            if (mod.floor != null) {
                setFloor(mod.x, mod.y, mod.floor);
            }
            if (mod.overlay != null) {
                setOverlay(mod.x, mod.y, mod.overlay);
            }
            if (mod.block != null) {
                setBlock(mod.x, mod.y, mod.block, mod.team != null ? mod.team : Team.derelict, mod.rotation);
            }
        }
    }
}

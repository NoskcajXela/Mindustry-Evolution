package mindustry.api;

import arc.struct.*;
import mindustry.world.*;

/**
 * Controller for world and terrain management.
 */
public interface WorldController {
    
    // === World Properties ===
    
    /** Get world width in tiles */
    int getWidth();
    
    /** Get world height in tiles */
    int getHeight();
    
    /** Get world name */
    String getName();
    
    /** Get world seed (if generated) */
    int getSeed();
    
    // === Tile Access ===
    
    /** Get tile at position */
    Tile getTile(int x, int y);
    
    /** Get all tiles in rectangle */
    Seq<Tile> getTiles(int x, int y, int width, int height);
    
    /** Get all tiles matching predicate */
    Seq<Tile> getTiles(TilePredicate predicate);
    
    /** Check if coordinates are within world bounds */
    boolean inBounds(int x, int y);
    
    // === Terrain Modification ===
    
    /** Set floor tile at position */
    void setFloor(int x, int y, Block floor);
    
    /** Set overlay tile at position */
    void setOverlay(int x, int y, Block overlay);
    
    /** Set block at position */
    void setBlock(int x, int y, Block block, Team team, int rotation);
    
    /** Remove block at position */
    void removeBlock(int x, int y);
    
    /** Fill area with floor */
    void fillFloor(int x, int y, int width, int height, Block floor);
    
    /** Fill area with blocks */
    void fillBlocks(int x, int y, int width, int height, Block block, Team team);
    
    // === Buildings ===
    
    /** Get building at position */
    Building getBuilding(int x, int y);
    
    /** Get all buildings of type */
    Seq<Building> getBuildingsOfType(Block type);
    
    /** Get all buildings owned by team */
    Seq<Building> getTeamBuildings(Team team);
    
    /** Get buildings in area */
    Seq<Building> getBuildingsInArea(int x, int y, int width, int height);
    
    // === Units ===
    
    /** Get all units in world */
    Seq<Unit> getAllUnits();
    
    /** Get units in area */
    Seq<Unit> getUnitsInArea(float x, float y, float radius);
    
    /** Get units of team */
    Seq<Unit> getTeamUnits(Team team);
    
    /** Get nearest unit to position */
    Unit getNearestUnit(float x, float y, Team team);
    
    // === Resources and Deposits ===
    
    /** Get resource tiles */
    Seq<Tile> getResourceTiles(Item item);
    
    /** Get ore deposits */
    Seq<Tile> getOreDeposits();
    
    /** Add resource deposit */
    void addResourceDeposit(int x, int y, Item item, int amount);
    
    // === Pathfinding ===
    
    /** Find path between two points */
    Seq<Tile> findPath(int startX, int startY, int endX, int endY);
    
    /** Check if path exists */
    boolean hasPath(int startX, int startY, int endX, int endY);
    
    /** Get walkable tiles around position */
    Seq<Tile> getWalkableTiles(int x, int y, int radius);
    
    // === Environmental ===
    
    /** Get light level at position */
    float getLightLevel(int x, int y);
    
    /** Check if tile is dark */
    boolean isDark(int x, int y);
    
    /** Get damage at position (for environmental hazards) */
    float getDamageAt(int x, int y);
    
    // === Analysis ===
    
    /** Get tile statistics */
    TileStats getTileStats();
    
    /** Count tiles of type */
    int countTiles(Block type);
    
    /** Get strategic positions */
    Seq<Tile> getStrategicPositions();
    
    /** Analyze area for buildability */
    AreaAnalysis analyzeArea(int x, int y, int width, int height);
    
    // === Bulk Operations ===
    
    /** Apply operation to all tiles */
    void forEachTile(TileOperation operation);
    
    /** Apply operation to tiles in area */
    void forEachTileInArea(int x, int y, int width, int height, TileOperation operation);
    
    /** Batch modify multiple tiles */
    void batchModify(Seq<TileModification> modifications);
    
    // === Functional Interfaces ===
    
    interface TilePredicate {
        boolean test(Tile tile);
    }
    
    interface TileOperation {
        void apply(Tile tile);
    }
    
    // === Data Classes ===
    
    class TileStats {
        public int totalTiles;
        public int solidTiles;
        public int floorTiles;
        public int buildingTiles;
        public int resourceTiles;
    }
    
    class AreaAnalysis {
        public boolean buildable;
        public float flatness;
        public Seq<Item> resources;
        public Seq<Block> obstacles;
    }
    
    class TileModification {
        public int x, y;
        public Block floor, overlay, block;
        public Team team;
        public int rotation;
    }
}

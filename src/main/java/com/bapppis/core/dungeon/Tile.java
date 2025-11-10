package com.bapppis.core.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.event.Event;
import com.bapppis.core.item.Item;
import com.bapppis.core.loot.LootPool;
import com.bapppis.core.loot.LootPoolLoader;

/**
 * Tile represents a specific instance of a tile at a coordinate.
 * It references a TileType (blueprint) and adds instance-specific data.
 */
public class Tile {
    private final TileType tileType;
    private final Coordinate coordinate;
    private boolean isDiscovered = false;
    private boolean isSpawn = false;
    private LootPool loot = null;
    private Event isEvent = null;
    private List<Creature> occupants = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private Tile left = null;
    private Tile right = null;
    private Tile up = null;
    private Tile down = null;

    public Tile(Coordinate coordinate, TileType tileType) {
        this.coordinate = coordinate;
        if (tileType == null) {
            throw new IllegalArgumentException("TileType cannot be null for coordinate " + coordinate);
        }
        this.tileType = tileType;
        if (tileType.lootPoolName != null && !tileType.lootPoolName.isEmpty()) {
            this.loot = LootPoolLoader.getLootPoolByName(tileType.lootPoolName);
        }
    }

    @Deprecated
    public Tile(Coordinate coordinate, char symbol) {
        this.coordinate = coordinate;
        String tileTypeName;
        switch (symbol) {
            case '#': tileTypeName = "basicWall"; break;
            case '.': tileTypeName = "basicFloor"; break;
            case ':': tileTypeName = "basicGenFloor"; break;
            case '^': tileTypeName = "basicUpStairs"; break;
            case 'v': tileTypeName = "basicDownStairs"; break;
            case '@': tileTypeName = "basicFloor"; this.isSpawn = true; break;
            case 'C': tileTypeName = "commonTreasureChest"; break;
            default: tileTypeName = "basicFloor"; System.err.println("Warning: Unknown symbol '" + symbol + "', defaulting to basicFloor"); break;
        }
        TileType type = TileTypeLoader.getTileTypeByName(tileTypeName);
        if (type == null) {
            throw new IllegalStateException("TileType not loaded: " + tileTypeName);
        }
        this.tileType = type;
        if (tileType.lootPoolName != null && !tileType.lootPoolName.isEmpty()) {
            this.loot = LootPoolLoader.getLootPoolByName(tileType.lootPoolName);
        }
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public char getSymbol() {
        return tileType.symbol;
    }

    public String getSprite() {
        return tileType.sprite;
    }

    public TileType getTileType() {
        return tileType;
    }

    // Neighbor getters
    public Tile getLeft() {
        return left;
    }

    public Tile getRight() {
        return right;
    }

    public Tile getUp() {
        return up;
    }

    public Tile getDown() {
        return down;
    }

    // Neighbor setters
    public void setLeft(Tile left) {
        this.left = left;
    }

    public void setRight(Tile right) {
        this.right = right;
    }

    public void setUp(Tile up) {
        this.up = up;
    }

    public void setDown(Tile down) {
        this.down = down;
    }

    // Fog of war and wall logic
    public boolean isWall() {
        return tileType.isWall;
    }

    public boolean isDiscovered() {
        return isDiscovered;
    }

    public void setDiscovered(boolean discovered) {
        this.isDiscovered = discovered;
    }

    public boolean isOccupied() {
        return tileType.isOccupied || !occupants.isEmpty();
    }

    public boolean isUpstairs() {
        return tileType.isUpstairs;
    }

    public boolean isDownstairs() {
        return tileType.isDownstairs;
    }

    public LootPool getLoot() {
        return loot;
    }

    public List<Creature> getOccupants() {
        return occupants;
    }

    public List<Item> getItems() {
        return items;
    }

    // === Dynamic tile modifications ===

    public void setSpawn() {
        this.isSpawn = true;
    }

    public boolean isSpawn() {
        return isSpawn;
    }

    public void spawnTreasureChest(LootPool loot) {
        this.loot = loot;
    }

    @Deprecated
    public void setAsGenFloor() {
        // This method is deprecated - tiles should use TileTypes
    }

    @Deprecated
    public void setAsWall() {
        // This method is deprecated - tiles should use TileTypes
    }

    @Deprecated
    public void breakWall() {
        // This method is deprecated - tiles should use TileTypes
    }
}

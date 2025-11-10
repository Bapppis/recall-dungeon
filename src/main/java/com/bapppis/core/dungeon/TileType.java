package com.bapppis.core.dungeon;

/**
 * TileType represents a template/blueprint for tiles loaded from JSON.
 * Multiple Tile instances can reference the same TileType.
 */
public class TileType {
    public String name;
    public char symbol;
    public String sprite;
    public boolean isWall;
    public boolean isBreakableWall;
    public boolean isUpstairs;
    public boolean isDownstairs;
    public boolean isSpawn;
    public boolean isPit;
    public boolean isOccupied;
    public String lootPoolName; // For treasure chests

    public TileType() {
        // Default constructor for GSON
    }

    @Override
    public String toString() {
        return "TileType{" +
                "name='" + name + '\'' +
                ", symbol=" + symbol +
                ", sprite='" + sprite + '\'' +
                ", isWall=" + isWall +
                ", isOccupied=" + isOccupied +
                '}';
    }
}

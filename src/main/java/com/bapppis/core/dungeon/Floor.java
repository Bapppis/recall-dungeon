package com.bapppis.core.dungeon;

import java.util.HashMap;

public abstract class Floor {
    // Make a hash map to store the tiles
    private HashMap<Coordinate, Tile> tiles = new HashMap<>();

    public void addTile(Coordinate coordinate, Tile tile) {
        tiles.put(coordinate, tile);
    }

    public Tile getTile(Coordinate coordinate) {
        return tiles.get(coordinate);
    }
}

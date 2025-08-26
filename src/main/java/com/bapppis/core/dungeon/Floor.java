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

    public HashMap<Coordinate, Tile> getTiles() {
        return tiles;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                Tile tile = getTile(new Coordinate(x, y));
                sb.append(tile != null ? tile.getSymbol() : '.');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

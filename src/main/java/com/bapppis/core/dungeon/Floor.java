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
    /**
     * Reveal all tiles within vision range of (px, py), blocked by walls.
     * Uses Bresenham's line algorithm for line of sight.
     */
    public void revealTilesWithVision(int px, int py, int visionRange) {
        for (int dx = -visionRange; dx <= visionRange; dx++) {
            for (int dy = -visionRange; dy <= visionRange; dy++) {
                int tx = px + dx;
                int ty = py + dy;
                // Use Chebyshev distance (max of dx,dy) to include corners
                if (Math.max(Math.abs(dx), Math.abs(dy)) > visionRange) continue;
                Coordinate target = new Coordinate(tx, ty);
                if (tiles.containsKey(target) && hasLineOfSight(px, py, tx, ty)) {
                    tiles.get(target).setDiscovered(true);
                }
            }
        }
    }

    /**
     * Returns true if there is line of sight from (x0, y0) to (x1, y1) (no wall blocks).
     * Uses Bresenham's line algorithm.
     */
    public boolean hasLineOfSight(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0;
        int cy = y0;
        while (true) {
            if (!(cx == x0 && cy == y0)) { // skip starting tile
                Tile t = tiles.get(new Coordinate(cx, cy));
                if (t == null) return false;
                if (t.isWall()) return false;
            }
            if (cx == x1 && cy == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 < dx) { err += dx; cy += sy; }
        }
        return true;
    }
}

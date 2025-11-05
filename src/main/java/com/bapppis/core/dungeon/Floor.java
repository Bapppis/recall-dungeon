package com.bapppis.core.dungeon;

import java.util.HashMap;

public abstract class Floor {
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

    /**
     * Reveal all tiles on this floor (set discovered = true).
     */
    public void revealAll() {
        for (Tile t : tiles.values()) {
            if (t != null) t.setDiscovered(true);
        }
    }

    /**
     * Hide all tiles on this floor (set discovered = false).
     */
    public void hideAll() {
        for (Tile t : tiles.values()) {
            if (t != null) t.setDiscovered(false);
        }
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

    public void revealTilesWithVision(int px, int py, int visionRange) {
        for (int dx = -visionRange; dx <= visionRange; dx++) {
            for (int dy = -visionRange; dy <= visionRange; dy++) {
                int tx = px + dx;
                int ty = py + dy;
                if (Math.max(Math.abs(dx), Math.abs(dy)) > visionRange) continue;
                Coordinate target = new Coordinate(tx, ty);
                if (tiles.containsKey(target) && hasLineOfSight(px, py, tx, ty)) {
                    tiles.get(target).setDiscovered(true);
                }
            }
        }
    }

    public boolean hasLineOfSight(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0;
        int cy = y0;
        while (true) {
            if (!(cx == x0 && cy == y0)) {
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

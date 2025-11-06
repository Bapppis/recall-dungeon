package com.bapppis.core.dungeon.generator;

import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.Tile;

import java.util.Random;

/**
 * Simple room-based map generator.
 * Creates floors with two-layer outer walls and places upstairs/downstairs.
 */
public class BSPRoomGenerator implements MapGenerator {

  @Override
  public Floor generate(int width, int height, int floorNumber, long seed) {
    Random random = new Random(seed);
    Floor floor = new Floor() {};

    // Create all tiles
    Tile[][] tiles = new Tile[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Coordinate coord = new Coordinate(x, y);
        // Two outer layers become walls
        if (x < 2 || x >= width - 2 || y < 2 || y >= height - 2) {
          tiles[x][y] = new Tile(coord, '#');
        } else {
          tiles[x][y] = new Tile(coord, '.');
        }
      }
    }

    // Place upstairs (fixed position for now - top-left quadrant)
    int upX = 3 + random.nextInt(Math.max(1, width / 4 - 4));
    int upY = 3 + random.nextInt(Math.max(1, height / 4 - 4));
    tiles[upX][upY] = new Tile(new Coordinate(upX, upY), '^');

    // Place downstairs (fixed position for now - bottom-right quadrant)
    int downX = width - 3 - random.nextInt(Math.max(1, width / 4 - 4));
    int downY = height - 3 - random.nextInt(Math.max(1, height / 4 - 4));
    tiles[downX][downY] = new Tile(new Coordinate(downX, downY), 'v');
    tiles[downX-1][downY].setSpawn();

    // Link neighbors
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Tile tile = tiles[x][y];
        if (x > 0)
          tile.setLeft(tiles[x - 1][y]);
        if (x < width - 1)
          tile.setRight(tiles[x + 1][y]);
        if (y > 0)
          tile.setUp(tiles[x][y - 1]);
        if (y < height - 1)
          tile.setDown(tiles[x][y + 1]);
      }
    }

    // Add tiles to floor
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        floor.addTile(tiles[x][y].getCoordinate(), tiles[x][y]);
      }
    }

    // Convert all plain floor tiles ('.') into genfloors (':'), but leave stairs intact
    // For testing
    for (Tile t : floor.getTiles().values()) {
      if (t == null) continue;
      char s = t.getSymbol();
      if (s == '.') {
        t.setAsGenFloor();
      }
    }

    return floor;
  }
}

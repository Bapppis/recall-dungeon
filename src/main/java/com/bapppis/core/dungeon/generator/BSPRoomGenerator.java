package com.bapppis.core.dungeon.generator;

import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.Tile;
import com.bapppis.core.dungeon.TileType;
import com.bapppis.core.dungeon.TileTypeLoader;
import com.bapppis.core.loot.LootPool;
import com.bapppis.core.loot.LootPoolLoader;

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

    // Load tile types
    TileType wallType = TileTypeLoader.getTileTypeByName("basicWall");
    TileType floorType = TileTypeLoader.getTileTypeByName("basicFloor");
    TileType upStairsType = TileTypeLoader.getTileTypeByName("basicUpStairs");
    TileType downStairsType = TileTypeLoader.getTileTypeByName("basicDownStairs");

    // Create all tiles
    Tile[][] tiles = new Tile[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Coordinate coord = new Coordinate(x, y);
        // Two outer layers become walls
        if (x < 2 || x >= width - 2 || y < 2 || y >= height - 2) {
          tiles[x][y] = new Tile(coord, wallType);
        } else {
          tiles[x][y] = new Tile(coord, floorType);
        }
      }
    }

    // --- quadrant-aware stair & spawn placement ---
    // inner bounds (exclude the two-tile outer wall)
    int innerMinX = 2;
    int innerMinY = 2;
    int innerMaxX = Math.max(innerMinX, width - 3);
    int innerMaxY = Math.max(innerMinY, height - 3);

    // compute split lines for quadrants
    int centerX = innerMinX + (innerMaxX - innerMinX + 1) / 2;
    int centerY = innerMinY + (innerMaxY - innerMinY + 1) / 2;

    int leftMin = innerMinX;
    int leftMax = Math.max(leftMin, centerX - 1);
    int rightMin = Math.max(centerX, leftMin);
    int rightMax = innerMaxX;

    int topMin = innerMinY;
    int topMax = Math.max(topMin, centerY - 1);
    int bottomMin = Math.max(centerY, topMin);
    int bottomMax = innerMaxY;

    // helper to pick a coord in a range safely
    java.util.function.BiFunction<Integer, Integer, Integer> pick =
        (min, max) -> (max <= min) ? min : min + random.nextInt(max - min + 1);

    // pick distinct quadrants for up/down
    int upQuad = random.nextInt(4);
    int downQuad;
    do {
      downQuad = random.nextInt(4);
    } while (downQuad == upQuad);

    // helper to pick a coordinate inside a quadrant
    java.util.function.Function<Integer, Coordinate> pickInQuadrant = (quad) -> {
      int x, y;
      switch (quad) {
        case 0: // top-left
          x = pick.apply(leftMin, leftMax);
          y = pick.apply(topMin, topMax);
          break;
        case 1: // top-right
          x = pick.apply(rightMin, rightMax);
          y = pick.apply(topMin, topMax);
          break;
        case 2: // bottom-left
          x = pick.apply(leftMin, leftMax);
          y = pick.apply(bottomMin, bottomMax);
          break;
        case 3: // bottom-right
        default:
          x = pick.apply(rightMin, rightMax);
          y = pick.apply(bottomMin, bottomMax);
          break;
      }
      return new Coordinate(x, y);
    };

    // Place upstairs
    Coordinate upCoord = pickInQuadrant.apply(upQuad);
    tiles[upCoord.getX()][upCoord.getY()] = new Tile(upCoord, upStairsType);

    // Place downstairs
    Coordinate downCoord = pickInQuadrant.apply(downQuad);
    tiles[downCoord.getX()][downCoord.getY()] = new Tile(downCoord, downStairsType);

    // Spawn placement:
    if (floorNumber == 0) {
      // choose a third quadrant distinct from both up and down
      int spawnQuad = -1;
      for (int q = 0; q < 4; q++) {
        if (q != upQuad && q != downQuad) {
          spawnQuad = q;
          break;
        }
      }
      if (spawnQuad == -1) {
        // fallback: choose any quadrant not equal to upQuad
        do {
          spawnQuad = random.nextInt(4);
        } while (spawnQuad == upQuad);
      }
      Coordinate spawnCoord = pickInQuadrant.apply(spawnQuad);
      tiles[spawnCoord.getX()][spawnCoord.getY()].setSpawn();
    } else {
      // non-zero floors: prefer spawn adjacent to the downstairs ('v').
      // If player arrives via upstairs on this floor, game logic should place the player at the corresponding downstairs,
      // so having the spawn adjacent to 'v' is a sensible default. If adjacency fails, try upstairs adjacency.
      boolean placed = false;
      int sx = downCoord.getX();
      int sy = downCoord.getY();
      int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
      for (int[] d : dirs) {
        int nx = sx + d[0];
        int ny = sy + d[1];
        if (nx >= innerMinX && nx <= innerMaxX && ny >= innerMinY && ny <= innerMaxY) {
          Tile candidate = tiles[nx][ny];
          if (candidate != null && candidate.getSymbol() != '#') {
            candidate.setSpawn();
            placed = true;
            break;
          }
        }
      }
      if (!placed) {
        // try adjacent to upstairs '^'
        sx = upCoord.getX();
        sy = upCoord.getY();
        for (int[] d : dirs) {
          int nx = sx + d[0];
          int ny = sy + d[1];
          if (nx >= innerMinX && nx <= innerMaxX && ny >= innerMinY && ny <= innerMaxY) {
            Tile candidate = tiles[nx][ny];
            if (candidate != null && candidate.getSymbol() != '#') {
              candidate.setSpawn();
              placed = true;
              break;
            }
          }
        }
      }
      // final fallback: place spawn on first non-wall inner tile
      if (!placed) {
        outer:
        for (int x = innerMinX; x <= innerMaxX; x++) {
          for (int y = innerMinY; y <= innerMaxY; y++) {
            if (tiles[x][y].getSymbol() != '#') {
              tiles[x][y].setSpawn();
              break outer;
            }
          }
        }
      }
    }

    // Place a random chest in a random quadrant (avoid placing on stairs or spawn)
    LootPool commonTreasureChest = LootPoolLoader.getLootPoolByName("Common Treasure Chest");
    System.out.println("DEBUG: Common Treasure Chest loot pool loaded: " + commonTreasureChest);
    if (commonTreasureChest != null) {
      int chestQuad = random.nextInt(4);
      boolean chestPlaced = false;
      // Try a few attempts in the chosen quadrant first
      for (int attempt = 0; attempt < 12 && !chestPlaced; attempt++) {
        Coordinate cc = pickInQuadrant.apply(chestQuad);
        int cx = cc.getX();
        int cy = cc.getY();
        Tile cand = tiles[cx][cy];
        if (cand != null && cand.getSymbol() == '.') {
          cand.spawnTreasureChest(commonTreasureChest);
          System.out.println("DEBUG: Chest placed at (" + cx + ", " + cy + ") with loot: " + cand.getLoot());
          chestPlaced = true;
          break;
        }
      }
      // If not placed, try other quadrants
      if (!chestPlaced) {
        for (int q = 0; q < 4 && !chestPlaced; q++) {
          for (int attempt = 0; attempt < 8 && !chestPlaced; attempt++) {
            Coordinate cc = pickInQuadrant.apply(q);
            int cx = cc.getX();
            int cy = cc.getY();
            Tile cand = tiles[cx][cy];
            if (cand != null && cand.getSymbol() == '.') {
              cand.spawnTreasureChest(commonTreasureChest);
              chestPlaced = true;
              break;
            }
          }
        }
      }
      // Final fallback: scan inner area for first plain floor tile
      if (!chestPlaced) {
        outer2:
        for (int x = innerMinX; x <= innerMaxX; x++) {
          for (int y = innerMinY; y <= innerMaxY; y++) {
            if (tiles[x][y].getSymbol() == '.') {
              tiles[x][y].spawnTreasureChest(commonTreasureChest);
              break outer2;
            }
          }
        }
      }
    }

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
    /* for (Tile t : floor.getTiles().values()) {
      if (t == null) continue;
      char s = t.getSymbol();
      if (s == '.') {
        t.setAsGenFloor();
      }
    } */

    return floor;
  }
}

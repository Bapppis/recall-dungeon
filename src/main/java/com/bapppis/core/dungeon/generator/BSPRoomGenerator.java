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
    Floor floor = new Floor() {
    };

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
    java.util.function.BiFunction<Integer, Integer, Integer> pick = (min, max) -> (max <= min) ? min
        : min + random.nextInt(max - min + 1);

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
      // If player arrives via upstairs on this floor, game logic should place the
      // player at the corresponding downstairs,
      // so having the spawn adjacent to 'v' is a sensible default. If adjacency
      // fails, try upstairs adjacency.
      boolean placed = false;
      int sx = downCoord.getX();
      int sy = downCoord.getY();
      int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
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
        outer: for (int x = innerMinX; x <= innerMaxX; x++) {
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
        outer2: for (int x = innerMinX; x <= innerMaxX; x++) {
          for (int y = innerMinY; y <= innerMaxY; y++) {
            if (tiles[x][y].getSymbol() == '.') {
              tiles[x][y].spawnTreasureChest(commonTreasureChest);
              break outer2;
            }
          }
        }
      }
    }

    // --- Spawn enemies on floor 0: place one monster in each quadrant that does
    // NOT contain the player ---
    if (floorNumber == 0) {
      LootPool enemies = LootPoolLoader.getLootPoolByName("Floor 0 Enemies");
      System.out.println("DEBUG: Floor 0 Enemies loot pool loaded: " + enemies);
      if (enemies != null && enemies.entries != null && !enemies.entries.isEmpty()) {
        // Determine the player's quadrant by locating the tile marked as spawn()
        int spawnQuad = -1;
        outerFindSpawn: for (int x = innerMinX; x <= innerMaxX; x++) {
          for (int y = innerMinY; y <= innerMaxY; y++) {
            Tile t = tiles[x][y];
            if (t != null && t.isSpawn()) {
              // map coordinate to quadrant index using left/right & top/bottom ranges
              boolean isLeft = (x >= leftMin && x <= leftMax);
              boolean isTop = (y >= topMin && y <= topMax);
              if (isTop && isLeft)
                spawnQuad = 0;
              else if (isTop && !isLeft)
                spawnQuad = 1;
              else if (!isTop && isLeft)
                spawnQuad = 2;
              else
                spawnQuad = 3;
              break outerFindSpawn;
            }
          }
        }
        if (spawnQuad == -1) {
          // fallback: pick the same spawnQuad logic used earlier (choose one at random)
          spawnQuad = random.nextInt(4);
        }

        // spawn in the other three quadrants
        for (int q = 0; q < 4; q++) {
          if (q == spawnQuad)
            continue; // skip player's quadrant

          System.out.println("DEBUG: Attempting to spawn monster in quadrant " + q);

          // Pick a monster entry from the loot pool weighted by 'weight' and
          // type==monster (ONCE per quadrant)
          com.bapppis.core.loot.LootPool.Entry choice = null;
          int totalWeight = 0;
          for (com.bapppis.core.loot.LootPool.Entry e : enemies.entries) {
            if (e == null)
              continue;
            if (e.type == null || !"monster".equalsIgnoreCase(e.type))
              continue;
            int w = (e.weight == null) ? 1 : e.weight;
            if (w <= 0)
              continue;
            totalWeight += w;
          }
          if (totalWeight <= 0) {
            continue;
          }
          int pickWeight = random.nextInt(totalWeight);
          int running = 0;
          for (com.bapppis.core.loot.LootPool.Entry e : enemies.entries) {
            if (e == null)
              continue;
            if (e.type == null || !"monster".equalsIgnoreCase(e.type))
              continue;
            int w = (e.weight == null) ? 1 : e.weight;
            if (w <= 0)
              continue;
            running += w;
            if (pickWeight < running) {
              choice = e;
              break;
            }
          }

          if (choice == null) continue;

          // Use CreatureLoader to create a fresh instance by the entry id or name
          String monsterRef = (choice.id != null) ? choice.id : choice.name;
          com.bapppis.core.creature.Creature spawned = null;
          try {
            com.bapppis.core.creature.CreatureLoader.loadCreatures();
            spawned = com.bapppis.core.creature.CreatureLoader.spawnCreatureByName(monsterRef);
          } catch (Exception ex) {
            continue;
          }
          if (spawned == null) continue;

          boolean placed = false;
          // Try several random picks inside the quadrant first
          for (int attempt = 0; attempt < 16 && !placed; attempt++) {
            Coordinate cc = pickInQuadrant.apply(q);
            int tx = cc.getX();
            int ty = cc.getY();
            Tile cand = tiles[tx][ty];
            if (cand == null)
              continue;
            // Only skip if tile reports occupied (wall, chest, or occupant)
            if (cand.isOccupied())
              continue;

            // Add to tile occupants
            cand.getOccupants().add(spawned);
            placed = true;
            break;
          }

          // Fallback: scan quadrant for first valid tile
          if (!placed) {
            int xstart, xend, ystart, yend;
            switch (q) {
              case 0:
                xstart = leftMin;
                xend = leftMax;
                ystart = topMin;
                yend = topMax;
                break;
              case 1:
                xstart = rightMin;
                xend = rightMax;
                ystart = topMin;
                yend = topMax;
                break;
              case 2:
                xstart = leftMin;
                xend = leftMax;
                ystart = bottomMin;
                yend = bottomMax;
                break;
              case 3:
              default:
                xstart = rightMin;
                xend = rightMax;
                ystart = bottomMin;
                yend = bottomMax;
                break;
            }
            outerScan: for (int x = xstart; x <= xend; x++) {
              for (int y = ystart; y <= yend; y++) {
                Tile cand = tiles[x][y];
                if (cand == null)
                  continue;
                // Only skip if tile reports occupied
                if (cand.isOccupied())
                  continue;

                cand.getOccupants().add(spawned);
                System.out
                    .println("DEBUG: (fallback) Spawned " + spawned + " at (" + x + "," + y + ") in quadrant " + q);
                placed = true;
                break outerScan;
              }
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

    // Convert all plain floor tiles ('.') into genfloors (':'), but leave stairs
    // intact
    // For testing
    /*
     * for (Tile t : floor.getTiles().values()) {
     * if (t == null) continue;
     * char s = t.getSymbol();
     * if (s == '.') {
     * t.setAsGenFloor();
     * }
     * }
     */

    return floor;
  }
}

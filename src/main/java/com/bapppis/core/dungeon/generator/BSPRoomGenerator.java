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
          tiles[x][y] = new Tile(coord, wallType); // Start inner as walls for maze generation
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

    // --- Generate maze in inner area ---
    generateMaze(tiles, innerMinX, innerMinY, innerMaxX, innerMaxY, wallType, floorType, random);

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
    ensureFloorAt(tiles, upCoord, floorType);
    tiles[upCoord.getX()][upCoord.getY()] = new Tile(upCoord, upStairsType);

    // Place downstairs
    Coordinate downCoord = pickInQuadrant.apply(downQuad);
    ensureFloorAt(tiles, downCoord, floorType);
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
      ensureFloorAt(tiles, spawnCoord, floorType);
      tiles[spawnCoord.getX()][spawnCoord.getY()].setSpawn();

      // Guarantee connectivity: carve paths if needed
      carvePathIfNeeded(tiles, spawnCoord, upCoord, floorType);
      carvePathIfNeeded(tiles, spawnCoord, downCoord, floorType);
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
    // AND ensure chest doesn't block critical paths
    LootPool commonTreasureChest = LootPoolLoader.getLootPoolByName("Common Treasure Chest");
    if (commonTreasureChest != null) {
      int chestQuad = random.nextInt(4);
      boolean chestPlaced = false;
      // Try a few attempts in the chosen quadrant first
      for (int attempt = 0; attempt < 20 && !chestPlaced; attempt++) {
        Coordinate cc = pickInQuadrant.apply(chestQuad);
        int cx = cc.getX();
        int cy = cc.getY();
        Tile cand = tiles[cx][cy];
        if (cand != null && cand.getSymbol() == '.' && !cand.isSpawn()) {
          // Test: if we place chest here, can spawn still reach both stairs?
          if (!wouldBlockPath(tiles, cx, cy, upCoord, downCoord)) {
            cand.spawnTreasureChest(commonTreasureChest);
            chestPlaced = true;
            break;
          }
        }
      }
      // If not placed, try other quadrants
      if (!chestPlaced) {
        for (int q = 0; q < 4 && !chestPlaced; q++) {
          for (int attempt = 0; attempt < 12 && !chestPlaced; attempt++) {
            Coordinate cc = pickInQuadrant.apply(q);
            int cx = cc.getX();
            int cy = cc.getY();
            Tile cand = tiles[cx][cy];
            if (cand != null && cand.getSymbol() == '.' && !cand.isSpawn()) {
              if (!wouldBlockPath(tiles, cx, cy, upCoord, downCoord)) {
                cand.spawnTreasureChest(commonTreasureChest);
                chestPlaced = true;
                break;
              }
            }
          }
        }
      }
      // Final fallback: scan inner area for first valid non-blocking floor tile
      if (!chestPlaced) {
        outer2: for (int x = innerMinX; x <= innerMaxX; x++) {
          for (int y = innerMinY; y <= innerMaxY; y++) {
            Tile cand = tiles[x][y];
            if (cand != null && cand.getSymbol() == '.' && !cand.isSpawn()) {
              if (!wouldBlockPath(tiles, x, y, upCoord, downCoord)) {
                cand.spawnTreasureChest(commonTreasureChest);
                break outer2;
              }
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

          if (choice == null)
            continue;

          // Use CreatureLoader to create a fresh instance by the entry id or name
          String monsterRef = (choice.id != null) ? choice.id : choice.name;
          com.bapppis.core.creature.Creature spawned = null;
          try {
            com.bapppis.core.creature.CreatureLoader.loadCreatures();
            spawned = com.bapppis.core.creature.CreatureLoader.spawnCreatureByName(monsterRef);
          } catch (Exception ex) {
            continue;
          }
          if (spawned == null)
            continue;

          boolean placed = false;
          // Try several random picks inside the quadrant first
          for (int attempt = 0; attempt < 16 && !placed; attempt++) {
            Coordinate cc = pickInQuadrant.apply(q);
            int tx = cc.getX();
            int ty = cc.getY();
            Tile cand = tiles[tx][ty];
            if (cand == null)
              continue;
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

  // ========== Maze Generation & Connectivity Helpers ==========

  /**
   * Generate a perfect maze using recursive backtracker algorithm.
   * Carves corridors on odd coordinates for traditional maze structure.
   */
  private void generateMaze(Tile[][] tiles, int minX, int minY, int maxX, int maxY, TileType wallType,
      TileType floorType, Random random) {
    // Start all inner tiles as walls (already done in tile creation)
    // Carve using recursive backtracker on odd-coordinate grid
    java.util.Stack<Coordinate> stack = new java.util.Stack<>();

    // Find valid starting cell (prefer odd coordinates for classic maze structure)
    int sx = (minX % 2 == 1) ? minX : Math.min(minX + 1, maxX);
    int sy = (minY % 2 == 1) ? minY : Math.min(minY + 1, maxY);

    stack.push(new Coordinate(sx, sy));
    tiles[sx][sy] = new Tile(new Coordinate(sx, sy), floorType);

    while (!stack.isEmpty()) {
      Coordinate cur = stack.peek();
      java.util.List<Coordinate> neighbors = new java.util.ArrayList<>();

      // Look two cells away in each cardinal direction
      int[][] dirs = { { 0, -2 }, { 0, 2 }, { -2, 0 }, { 2, 0 } };
      for (int[] d : dirs) {
        int nx = cur.getX() + d[0];
        int ny = cur.getY() + d[1];
        if (nx >= minX && nx <= maxX && ny >= minY && ny <= maxY) {
          if (tiles[nx][ny].getSymbol() == '#') { // still wall
            neighbors.add(new Coordinate(nx, ny));
          }
        }
      }

      if (!neighbors.isEmpty()) {
        Coordinate chosen = neighbors.get(random.nextInt(neighbors.size()));
        // Carve the wall between current and chosen cell
        int betweenX = (cur.getX() + chosen.getX()) / 2;
        int betweenY = (cur.getY() + chosen.getY()) / 2;
        tiles[betweenX][betweenY] = new Tile(new Coordinate(betweenX, betweenY), floorType);
        tiles[chosen.getX()][chosen.getY()] = new Tile(new Coordinate(chosen.getX(), chosen.getY()), floorType);
        stack.push(chosen);
      } else {
        stack.pop();
      }
    }
  }

  /**
   * Ensure the tile at coordinate c is a floor tile (convert if needed).
   */
  private void ensureFloorAt(Tile[][] tiles, Coordinate c, TileType floorType) {
    int x = c.getX(), y = c.getY();
    Tile t = tiles[x][y];
    if (t == null || t.getSymbol() == '#') {
      tiles[x][y] = new Tile(new Coordinate(x, y), floorType);
    }
  }

  /**
   * Check if two coordinates are reachable via floor tiles.
   * Uses BFS to test connectivity.
   */
  private boolean isReachable(Tile[][] tiles, Coordinate a, Coordinate b) {
    if (a == null || b == null)
      return false;

    java.util.Queue<Coordinate> q = new java.util.ArrayDeque<>();
    java.util.Set<String> seen = new java.util.HashSet<>();
    q.add(a);
    seen.add(a.getX() + "," + a.getY());

    int width = tiles.length, height = tiles[0].length;
    while (!q.isEmpty()) {
      Coordinate cur = q.remove();
      if (cur.getX() == b.getX() && cur.getY() == b.getY())
        return true;

      int[][] dirs = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
      for (int[] d : dirs) {
        int nx = cur.getX() + d[0], ny = cur.getY() + d[1];
        if (nx < 0 || ny < 0 || nx >= width || ny >= height)
          continue;

        Tile t = tiles[nx][ny];
        if (t == null)
          continue;

        // Can walk through floors, stairs, and spawn tiles (not walls or occupied
        // tiles)
        char sym = t.getSymbol();
        if (sym == '#')
          continue;
        if (t.isOccupied() && !(nx == b.getX() && ny == b.getY()))
          continue; // Allow target even if occupied

        String key = nx + "," + ny;
        if (seen.contains(key))
          continue;
        seen.add(key);
        q.add(new Coordinate(nx, ny));
      }
    }
    return false;
  }

  /**
   * Carve a floor path between start and target if they are not already
   * connected.
   * Uses BFS ignoring walls to find shortest path, then converts path to floor
   * tiles.
   */
  private void carvePathIfNeeded(Tile[][] tiles, Coordinate start, Coordinate target, TileType floorType) {
    if (start == null || target == null)
      return;
    if (isReachable(tiles, start, target))
      return; // Already connected

    // BFS that ignores walls to find path
    java.util.Queue<Coordinate> q = new java.util.ArrayDeque<>();
    java.util.Map<Coordinate, Coordinate> prev = new java.util.HashMap<>();
    java.util.Set<String> seen = new java.util.HashSet<>();
    q.add(target);
    seen.add(target.getX() + "," + target.getY());

    boolean found = false;
    int width = tiles.length, height = tiles[0].length;

    while (!q.isEmpty() && !found) {
      Coordinate cur = q.remove();
      int[][] dirs = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
      for (int[] d : dirs) {
        int nx = cur.getX() + d[0], ny = cur.getY() + d[1];
        if (nx < 0 || ny < 0 || nx >= width || ny >= height)
          continue;

        String key = nx + "," + ny;
        if (seen.contains(key))
          continue;
        seen.add(key);

        Coordinate next = new Coordinate(nx, ny);
        prev.put(next, cur);

        if (nx == start.getX() && ny == start.getY()) {
          found = true;
          break;
        }
        q.add(next);
      }
    }

    // Backtrack and carve path
    if (found) {
      Coordinate cur = start;
      while (cur != null && !(cur.getX() == target.getX() && cur.getY() == target.getY())) {
        Tile t = tiles[cur.getX()][cur.getY()];
        if (t == null || t.getSymbol() == '#') {
          tiles[cur.getX()][cur.getY()] = new Tile(new Coordinate(cur.getX(), cur.getY()), floorType);
        }
        cur = prev.get(cur);
      }
    }
  }

  /**
   * Test if placing a chest at (x,y) would block critical paths.
   * Returns true if blocking spawn→upstairs or spawn→downstairs.
   */
  private boolean wouldBlockPath(Tile[][] tiles, int x, int y, Coordinate upCoord, Coordinate downCoord) {
    // Find spawn tile
    Coordinate spawnCoord = null;
    for (int sx = 0; sx < tiles.length && spawnCoord == null; sx++) {
      for (int sy = 0; sy < tiles[0].length && spawnCoord == null; sy++) {
        if (tiles[sx][sy] != null && tiles[sx][sy].isSpawn()) {
          spawnCoord = new Coordinate(sx, sy);
        }
      }
    }
    if (spawnCoord == null)
      return false; // No spawn, can't block

    // Temporarily mark chest location as occupied
    Tile original = tiles[x][y];

    // Create temporary wall tile to simulate blocking
    Tile tempWall = new Tile(new Coordinate(x, y), original.getTileType());
    tiles[x][y] = tempWall;

    // Test reachability
    boolean blocksUp = !isReachable(tiles, spawnCoord, upCoord);
    boolean blocksDown = !isReachable(tiles, spawnCoord, downCoord);

    // Restore original tile
    tiles[x][y] = original;

    return blocksUp || blocksDown;
  }
}

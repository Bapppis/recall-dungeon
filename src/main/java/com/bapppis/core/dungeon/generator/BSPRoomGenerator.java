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

  // Helper class to store quadrant boundary information
  private static class QuadrantBounds {
    int leftMin, leftMax, rightMin, rightMax;
    int topMin, topMax, bottomMin, bottomMax;

    int[][] getQuadrantBounds(int quadrant) {
      switch (quadrant) {
        case 0:
          return new int[][] { { leftMin, leftMax }, { topMin, topMax } };
        case 1:
          return new int[][] { { rightMin, rightMax }, { topMin, topMax } };
        case 2:
          return new int[][] { { leftMin, leftMax }, { bottomMin, bottomMax } };
        case 3:
          return new int[][] { { rightMin, rightMax }, { bottomMin, bottomMax } };
        default:
          return new int[][] { { leftMin, leftMax }, { topMin, topMax } };
      }
    }
  }

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

    // Store quadrant bounds for spawning methods
    QuadrantBounds qBounds = new QuadrantBounds();
    qBounds.leftMin = leftMin;
    qBounds.leftMax = leftMax;
    qBounds.rightMin = rightMin;
    qBounds.rightMax = rightMax;
    qBounds.topMin = topMin;
    qBounds.topMax = topMax;
    qBounds.bottomMin = bottomMin;
    qBounds.bottomMax = bottomMax;

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

    // Spawn chests using new method
    if (floorNumber == 0) {
      spawnChests(tiles, "Common Treasure Chest", 1, 2, false, qBounds, upCoord, downCoord, random);
    }

    // --- Spawn enemies on floor 0: place one monster in each quadrant that does
    // NOT contain the player ---
    if (floorNumber == 0) {
      spawnMonsters(tiles, "Floor 0 Enemies", 2, 3, true, qBounds, random);
    }

    // FINAL CONNECTIVITY CHECK: Ensure upstairs and downstairs are mutually
    // reachable
    // This catches any edge cases where maze generation might have isolated a
    // staircase
    if (!isReachable(tiles, upCoord, downCoord)) {
      System.out.println("WARNING: Stairs not connected on floor " + floorNumber + ", carving path...");
      carvePathIfNeeded(tiles, upCoord, downCoord, floorType);
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

    return floor;
  }

  // ========== Maze Generation & Connectivity Helpers ==========

  /**
   * Generate a hybrid dungeon: BSP rooms + maze corridors + widening + open
   * areas.
   * Creates a more dungeon-like layout with structured rooms and winding
   * passages.
   */
  private void generateMaze(Tile[][] tiles, int minX, int minY, int maxX, int maxY, TileType wallType,
      TileType floorType, Random random) {

    // Phase 1: Create BSP rooms (3-6 rooms depending on floor size)
    int roomCount = 3 + random.nextInt(4); // 3-6 rooms

    // Use BSP to partition space and create rooms
    java.util.List<Rectangle> partitions = bspPartition(minX, minY, maxX, maxY, roomCount, random);

    for (Rectangle partition : partitions) {
      // Create a room within this partition (leave 1-2 tile margins)
      int roomMargin = 1 + random.nextInt(2);
      int roomMinX = Math.max(partition.x + roomMargin, partition.x);
      int roomMinY = Math.max(partition.y + roomMargin, partition.y);
      int roomMaxX = Math.min(partition.x + partition.width - 1 - roomMargin, partition.x + partition.width - 1);
      int roomMaxY = Math.min(partition.y + partition.height - 1 - roomMargin, partition.y + partition.height - 1);

      if (roomMaxX > roomMinX && roomMaxY > roomMinY) {
        // Carve out the room
        for (int x = roomMinX; x <= roomMaxX; x++) {
          for (int y = roomMinY; y <= roomMaxY; y++) {
            tiles[x][y] = new Tile(new Coordinate(x, y), floorType);
          }
        }
      }
    }

    // Phase 2: Generate maze in remaining areas (using recursive backtracker on odd
    // coordinates)
    java.util.Stack<Coordinate> stack = new java.util.Stack<>();

    // Find a starting cell for maze (prefer odd coordinates outside rooms)
    int sx = -1, sy = -1;
    for (int attempt = 0; attempt < 50; attempt++) {
      int tx = minX + random.nextInt(maxX - minX + 1);
      int ty = minY + random.nextInt(maxY - minY + 1);

      // Prefer odd coordinates
      if (tx % 2 == 0)
        tx = Math.min(tx + 1, maxX);
      if (ty % 2 == 0)
        ty = Math.min(ty + 1, maxY);

      if (tiles[tx][ty].getSymbol() == '#') {
        sx = tx;
        sy = ty;
        break;
      }
    }

    if (sx == -1) {
      // Fallback: use first wall cell
      sx = (minX % 2 == 1) ? minX : Math.min(minX + 1, maxX);
      sy = (minY % 2 == 1) ? minY : Math.min(minY + 1, maxY);
    }

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
          if (tiles[nx][ny].getSymbol() == '#') {
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

    // Phase 3: Widen corridors (make 2-tile wide passages in some areas)
    widenCorridors(tiles, minX, minY, maxX, maxY, floorType, random, 0.10); // 10% of eligible walls

    // Phase 4: Create some open areas by removing walls between adjacent floor
    // tiles
    // Reduced from 8% to 3% to make the map less open by default
    createOpenAreas(tiles, minX, minY, maxX, maxY, floorType, random, 0.03); // 3% of walls

    // Phase 5: Add some dead-end branches
    addDeadEnds(tiles, minX, minY, maxX, maxY, floorType, random, 3 + random.nextInt(5)); // 3-7 dead ends
  }

  /**
   * BSP partition helper: recursively splits space into rectangular partitions.
   */
  private java.util.List<Rectangle> bspPartition(int x, int y, int maxX, int maxY, int targetCount, Random random) {
    java.util.List<Rectangle> result = new java.util.ArrayList<>();
    java.util.Queue<Rectangle> queue = new java.util.ArrayDeque<>();

    queue.add(new Rectangle(x, y, maxX - x + 1, maxY - y + 1));

    while (!queue.isEmpty() && result.size() < targetCount) {
      Rectangle rect = queue.remove();

      // Stop splitting if too small
      if (rect.width < 8 || rect.height < 8) {
        result.add(rect);
        continue;
      }

      // Randomly choose horizontal or vertical split
      boolean splitHorizontal = random.nextBoolean();

      if (splitHorizontal && rect.height >= 8) {
        // Split horizontally
        int splitY = rect.y + 4 + random.nextInt(rect.height - 7);
        queue.add(new Rectangle(rect.x, rect.y, rect.width, splitY - rect.y));
        queue.add(new Rectangle(rect.x, splitY, rect.width, rect.y + rect.height - splitY));
      } else if (!splitHorizontal && rect.width >= 8) {
        // Split vertically
        int splitX = rect.x + 4 + random.nextInt(rect.width - 7);
        queue.add(new Rectangle(rect.x, rect.y, splitX - rect.x, rect.height));
        queue.add(new Rectangle(splitX, rect.y, rect.x + rect.width - splitX, rect.height));
      } else {
        result.add(rect);
      }
    }

    // Add any remaining partitions
    result.addAll(queue);
    return result;
  }

  /**
   * Widen corridors by removing walls adjacent to floor tiles.
   */
  private void widenCorridors(Tile[][] tiles, int minX, int minY, int maxX, int maxY,
      TileType floorType, Random random, double chance) {

    for (int x = minX + 1; x < maxX; x++) {
      for (int y = minY + 1; y < maxY; y++) {
        if (tiles[x][y].getSymbol() != '#')
          continue;
        if (random.nextDouble() > chance)
          continue;

        // Count adjacent floor tiles
        int floorCount = 0;
        if (tiles[x - 1][y].getSymbol() != '#')
          floorCount++;
        if (tiles[x + 1][y].getSymbol() != '#')
          floorCount++;
        if (tiles[x][y - 1].getSymbol() != '#')
          floorCount++;
        if (tiles[x][y + 1].getSymbol() != '#')
          floorCount++;

        // If wall has 2+ adjacent floors (corridor), widen it
        if (floorCount >= 2) {
          tiles[x][y] = new Tile(new Coordinate(x, y), floorType);
        }
      }
    }
  }

  /**
   * Create open areas by removing some walls.
   */
  private void createOpenAreas(Tile[][] tiles, int minX, int minY, int maxX, int maxY,
      TileType floorType, Random random, double chance) {

    for (int x = minX + 1; x < maxX; x++) {
      for (int y = minY + 1; y < maxY; y++) {
        if (tiles[x][y].getSymbol() != '#')
          continue;
        if (random.nextDouble() > chance)
          continue;

        // Count adjacent floor tiles (including diagonals)
        int floorCount = 0;
        for (int dx = -1; dx <= 1; dx++) {
          for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dy == 0)
              continue;
            int nx = x + dx, ny = y + dy;
            if (nx >= minX && nx <= maxX && ny >= minY && ny <= maxY) {
              if (tiles[nx][ny].getSymbol() != '#')
                floorCount++;
            }
          }
        }

        // If wall has many adjacent floors, remove it for openness
        if (floorCount >= 4) {
          tiles[x][y] = new Tile(new Coordinate(x, y), floorType);
        }
      }
    }
  }

  /**
   * Add dead-end branches (short corridors that don't connect to anything).
   */
  private void addDeadEnds(Tile[][] tiles, int minX, int minY, int maxX, int maxY,
      TileType floorType, Random random, int count) {

    for (int i = 0; i < count; i++) {
      // Find a wall adjacent to exactly one floor tile
      for (int attempt = 0; attempt < 30; attempt++) {
        int x = minX + 1 + random.nextInt(maxX - minX - 1);
        int y = minY + 1 + random.nextInt(maxY - minY - 1);

        if (tiles[x][y].getSymbol() != '#')
          continue;

        int floorCount = 0;
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
          int nx = x + d[0], ny = y + d[1];
          if (tiles[nx][ny].getSymbol() != '#')
            floorCount++;
        }

        // If wall has exactly 1 adjacent floor, carve a short branch
        if (floorCount == 1) {
          int branchLength = 1 + random.nextInt(3); // 1-3 tiles
          int dx = 0, dy = 0;

          // Pick a direction away from floor
          if (tiles[x - 1][y].getSymbol() != '#') {
            dx = 1;
          } else if (tiles[x + 1][y].getSymbol() != '#') {
            dx = -1;
          } else if (tiles[x][y - 1].getSymbol() != '#') {
            dy = 1;
          } else if (tiles[x][y + 1].getSymbol() != '#') {
            dy = -1;
          }

          // Carve branch
          int cx = x, cy = y;
          for (int step = 0; step < branchLength; step++) {
            if (cx < minX + 1 || cx > maxX - 1 || cy < minY + 1 || cy > maxY - 1)
              break;
            if (tiles[cx][cy].getSymbol() != '#')
              break;

            tiles[cx][cy] = new Tile(new Coordinate(cx, cy), floorType);
            cx += dx;
            cy += dy;
          }
          break;
        }
      }
    }
  }

  // Helper class for BSP partitioning
  private static class Rectangle {
    int x, y, width, height;

    Rectangle(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
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

  // ========== Content Spawning Methods ==========

  /**
   * Spawn monsters from a monster pool with min/max count and optional spawn
   * avoidance.
   *
   * @param tiles           The tile grid
   * @param monsterPoolName Name of the monster pool to spawn from
   * @param min             Minimum number of monsters to spawn
   * @param max             Maximum number of monsters to spawn
   * @param avoidSpawn      If true, avoid spawning in the same quadrant as player
   *                        spawn
   * @param qBounds         Quadrant boundary information
   * @param random          Random number generator
   */
  private void spawnMonsters(Tile[][] tiles, String monsterPoolName, int min, int max, boolean avoidSpawn,
      QuadrantBounds qBounds, Random random) {

    LootPool monsterPool = LootPoolLoader.getLootPoolByName(monsterPoolName);
    if (monsterPool == null || monsterPool.entries == null || monsterPool.entries.isEmpty()) {
      return;
    }

    // Determine spawn quadrant if avoidance is needed
    int spawnQuad = -1;
    if (avoidSpawn) {
      outerFindSpawn: for (int x = 0; x < tiles.length; x++) {
        for (int y = 0; y < tiles[0].length; y++) {
          Tile t = tiles[x][y];
          if (t != null && t.isSpawn()) {
            boolean isLeft = (x >= qBounds.leftMin && x <= qBounds.leftMax);
            boolean isTop = (y >= qBounds.topMin && y <= qBounds.topMax);
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
    }

    // Build list of available quadrants
    java.util.List<Integer> availableQuads = new java.util.ArrayList<>();
    for (int q = 0; q < 4; q++) {
      if (!avoidSpawn || q != spawnQuad) {
        availableQuads.add(q);
      }
    }

    if (availableQuads.isEmpty()) {
      availableQuads.add(0); // Fallback to quadrant 0
    }

    // Pick random count in range [min, max]
    int count = (max <= min) ? min : min + random.nextInt(max - min + 1);

    // Spawn one per available quadrant first, then distribute remaining randomly
    int spawned = 0;

    // Phase 1: One per quadrant
    for (int q : availableQuads) {
      if (spawned >= count)
        break;

      com.bapppis.core.creature.Creature monster = pickRandomMonsterFromPool(monsterPool, random);
      if (monster != null && placeMonsterInQuadrant(tiles, monster, q, qBounds, random)) {
        spawned++;
      }
    }

    // Phase 2: Distribute remaining randomly across available quadrants
    while (spawned < count) {
      int q = availableQuads.get(random.nextInt(availableQuads.size()));
      com.bapppis.core.creature.Creature monster = pickRandomMonsterFromPool(monsterPool, random);
      if (monster != null && placeMonsterInQuadrant(tiles, monster, q, qBounds, random)) {
        spawned++;
      } else {
        // Avoid infinite loop if placement consistently fails
        break;
      }
    }
  }

  /**
   * Spawn treasure chests from a loot pool with min/max count and optional spawn
   * avoidance.
   * Validates chest placement to ensure critical paths aren't blocked.
   * 
   * @param tiles        The tile grid
   * @param lootPoolName Name of the loot pool for chest contents
   * @param min          Minimum number of chests to spawn
   * @param max          Maximum number of chests to spawn
   * @param avoidSpawn   If true, avoid spawning in the same quadrant as player
   *                     spawn
   * @param qBounds      Quadrant boundary information
   * @param upCoord      Upstairs coordinate (for path validation)
   * @param downCoord    Downstairs coordinate (for path validation)
   * @param random       Random number generator
   */
  private void spawnChests(Tile[][] tiles, String lootPoolName, int min, int max, boolean avoidSpawn,
      QuadrantBounds qBounds, Coordinate upCoord, Coordinate downCoord, Random random) {

    LootPool chestLoot = LootPoolLoader.getLootPoolByName(lootPoolName);
    if (chestLoot == null) {
      return;
    }

    // Determine spawn quadrant if avoidance is needed
    int spawnQuad = -1;
    if (avoidSpawn) {
      outerFindSpawn: for (int x = 0; x < tiles.length; x++) {
        for (int y = 0; y < tiles[0].length; y++) {
          Tile t = tiles[x][y];
          if (t != null && t.isSpawn()) {
            boolean isLeft = (x >= qBounds.leftMin && x <= qBounds.leftMax);
            boolean isTop = (y >= qBounds.topMin && y <= qBounds.topMax);
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
    }

    // Build list of available quadrants
    java.util.List<Integer> availableQuads = new java.util.ArrayList<>();
    for (int q = 0; q < 4; q++) {
      if (!avoidSpawn || q != spawnQuad) {
        availableQuads.add(q);
      }
    }

    if (availableQuads.isEmpty()) {
      availableQuads.add(0); // Fallback
    }

    // Pick random count in range [min, max]
    int count = (max <= min) ? min : min + random.nextInt(max - min + 1);

    int spawned = 0;
    int attempts = 0;
    int maxAttempts = count * 50; // Prevent infinite loops

    while (spawned < count && attempts < maxAttempts) {
      attempts++;

      // Pick a random quadrant from available ones
      int q = availableQuads.get(random.nextInt(availableQuads.size()));
      int[][] bounds = qBounds.getQuadrantBounds(q);
      int xMin = bounds[0][0], xMax = bounds[0][1];
      int yMin = bounds[1][0], yMax = bounds[1][1];

      // Pick random coordinate in quadrant
      int x = (xMax <= xMin) ? xMin : xMin + random.nextInt(xMax - xMin + 1);
      int y = (yMax <= yMin) ? yMin : yMin + random.nextInt(yMax - yMin + 1);

      Tile cand = tiles[x][y];
      if (cand == null)
        continue;

      // Prefer floor tiles that aren't spawn and don't block paths
      if (cand.getSymbol() == '.' && !cand.isSpawn() && !cand.isOccupied()) {
        if (!wouldBlockPath(tiles, x, y, upCoord, downCoord)) {
          // Replace floor tile with chest tile
          TileType chestType = TileTypeLoader.getTileTypeByName("commonTreasureChest");
          if (chestType != null) {
            Tile chestTile = new Tile(new Coordinate(x, y), chestType);
            // Copy navigation references
            chestTile.setLeft(cand.getLeft());
            chestTile.setRight(cand.getRight());
            chestTile.setUp(cand.getUp());
            chestTile.setDown(cand.getDown());
            chestTile.setDiscovered(cand.isDiscovered());
            tiles[x][y] = chestTile;

            // Update neighbor references
            if (chestTile.getLeft() != null)
              chestTile.getLeft().setRight(chestTile);
            if (chestTile.getRight() != null)
              chestTile.getRight().setLeft(chestTile);
            if (chestTile.getUp() != null)
              chestTile.getUp().setDown(chestTile);
            if (chestTile.getDown() != null)
              chestTile.getDown().setUp(chestTile);
            spawned++;
          }
        }
      }
    }

    // Fallback: If we couldn't place all chests on floors, replace walls
    if (spawned < count) {
      TileType chestType = TileTypeLoader.getTileTypeByName("commonTreasureChest");
      if (chestType == null) {
        // If no chest tile type, use floor + loot
        chestType = TileTypeLoader.getTileTypeByName("basicFloor");
      }

      while (spawned < count && attempts < maxAttempts * 2) {
        attempts++;

        int q = availableQuads.get(random.nextInt(availableQuads.size()));
        int[][] bounds = qBounds.getQuadrantBounds(q);
        int xMin = bounds[0][0], xMax = bounds[0][1];
        int yMin = bounds[1][0], yMax = bounds[1][1];

        int x = (xMax <= xMin) ? xMin : xMin + random.nextInt(xMax - xMin + 1);
        int y = (yMax <= yMin) ? yMin : yMin + random.nextInt(yMax - yMin + 1);

        Tile cand = tiles[x][y];
        if (cand == null)
          continue;

        // Replace wall with chest if it doesn't block critical paths
        if (cand.getSymbol() == '#' && !wouldBlockPath(tiles, x, y, upCoord, downCoord)) {
          Tile newTile = new Tile(new Coordinate(x, y), chestType);
          // Copy navigation and state
          newTile.setLeft(cand.getLeft());
          newTile.setRight(cand.getRight());
          newTile.setUp(cand.getUp());
          newTile.setDown(cand.getDown());
          newTile.setDiscovered(cand.isDiscovered());
          tiles[x][y] = newTile;

          // Update neighbor references
          if (newTile.getLeft() != null)
            newTile.getLeft().setRight(newTile);
          if (newTile.getRight() != null)
            newTile.getRight().setLeft(newTile);
          if (newTile.getUp() != null)
            newTile.getUp().setDown(newTile);
          if (newTile.getDown() != null)
            newTile.getDown().setUp(newTile);
          spawned++;
        }
      }
    }
  }

  /**
   * Pick a random monster from a monster pool using weighted selection.
   */
  private com.bapppis.core.creature.Creature pickRandomMonsterFromPool(LootPool pool, Random random) {
    int totalWeight = 0;
    for (com.bapppis.core.loot.LootPool.Entry e : pool.entries) {
      if (e == null)
        continue;
      if (e.type == null || !"monster".equalsIgnoreCase(e.type))
        continue;
      int w = (e.weight == null) ? 1 : e.weight;
      if (w <= 0)
        continue;
      totalWeight += w;
    }

    if (totalWeight <= 0)
      return null;

    int pickWeight = random.nextInt(totalWeight);
    int running = 0;
    for (com.bapppis.core.loot.LootPool.Entry e : pool.entries) {
      if (e == null)
        continue;
      if (e.type == null || !"monster".equalsIgnoreCase(e.type))
        continue;
      int w = (e.weight == null) ? 1 : e.weight;
      if (w <= 0)
        continue;
      running += w;
      if (pickWeight < running) {
        String monsterRef = (e.id != null) ? e.id : e.name;
        try {
          com.bapppis.core.creature.CreatureLoader.loadCreatures();
          return com.bapppis.core.creature.CreatureLoader.spawnCreatureByName(monsterRef);
        } catch (Exception ex) {
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Attempt to place a monster in a specific quadrant.
   * Returns true if successful, false otherwise.
   */
  private boolean placeMonsterInQuadrant(Tile[][] tiles, com.bapppis.core.creature.Creature monster, int quadrant,
      QuadrantBounds qBounds, Random random) {

    int[][] bounds = qBounds.getQuadrantBounds(quadrant);
    int xMin = bounds[0][0], xMax = bounds[0][1];
    int yMin = bounds[1][0], yMax = bounds[1][1];

    // Try random placement first (16 attempts)
    for (int attempt = 0; attempt < 16; attempt++) {
      int x = (xMax <= xMin) ? xMin : xMin + random.nextInt(xMax - xMin + 1);
      int y = (yMax <= yMin) ? yMin : yMin + random.nextInt(yMax - yMin + 1);

      Tile cand = tiles[x][y];
      if (cand == null)
        continue;
      if (cand.isOccupied())
        continue;
      if (cand.getSymbol() == '#')
        continue;

      cand.getOccupants().add(monster);
      // Set enemy position if it's an Enemy
      if (monster instanceof com.bapppis.core.creature.Enemy) {
        com.bapppis.core.creature.Enemy enemy = (com.bapppis.core.creature.Enemy) monster;
        enemy.setPosition(cand.getCoordinate());
        // Initialize patrol route with 3-5 tile radius from spawn
        enemy.initializePatrolRoute(cand.getCoordinate(), 4);
      }
      return true;
    }

    // Fallback: scan quadrant for first valid tile
    for (int x = xMin; x <= xMax; x++) {
      for (int y = yMin; y <= yMax; y++) {
        Tile cand = tiles[x][y];
        if (cand == null)
          continue;
        if (cand.isOccupied())
          continue;
        if (cand.getSymbol() == '#')
          continue;

        cand.getOccupants().add(monster);
        // Set enemy position if it's an Enemy
        if (monster instanceof com.bapppis.core.creature.Enemy) {
          com.bapppis.core.creature.Enemy enemy = (com.bapppis.core.creature.Enemy) monster;
          enemy.setPosition(cand.getCoordinate());
          // Initialize patrol route with 3-5 tile radius from spawn
          enemy.initializePatrolRoute(cand.getCoordinate(), 4);
        }
        return true;
      }
    }

    return false;
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

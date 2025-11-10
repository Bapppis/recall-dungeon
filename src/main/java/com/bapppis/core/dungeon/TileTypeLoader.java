package com.bapppis.core.dungeon;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TileTypeLoader {
  private static final Gson GSON = new Gson();
  private static Map<String, TileType> tileTypesByName = new HashMap<>();

  /**
   * Load all tile types from the data/tile_types directory
   */
  public static void loadTileTypes() {
    tileTypesByName.clear();

    // List of tile type file names to load
    String[] tileTypeFiles = {
      "basicWall.json",
      "basicFloor.json",
      "basicGenFloor.json",
      "basicUpStairs.json",
      "basicDownStairs.json",
      "commonTreasureChest.json"
    };

    for (String fileName : tileTypeFiles) {
      String resourcePath = "data/tile_types/" + fileName;
      try (InputStream is = TileTypeLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
        if (is == null) {
          System.err.println("Could not find tile type resource: " + resourcePath);
          continue;
        }
        try (JsonReader jr = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
          TileType tileType = GSON.fromJson(jr, TileType.class);
          if (tileType != null && tileType.name != null) {
            tileTypesByName.put(tileType.name.toLowerCase(), tileType);
            System.out.println("Loaded tile type: " + tileType.name);
          }
        }
      } catch (Exception e) {
        System.err.println("Error loading tile type from " + fileName + ": " + e.getMessage());
        e.printStackTrace();
      }
    }

    System.out.println("Loaded " + tileTypesByName.size() + " tile types");
  }

  /**
   * Get a tile type by name (case-insensitive)
   */
  public static TileType getTileTypeByName(String name) {
    if (name == null)
      return null;
    return tileTypesByName.get(name.toLowerCase());
  }

  /**
   * Check if a tile type exists
   */
  public static boolean hasTileType(String name) {
    return getTileTypeByName(name) != null;
  }

  /**
   * Get all loaded tile types
   */
  public static Map<String, TileType> getAllTileTypes() {
    return new HashMap<>(tileTypesByName);
  }
}

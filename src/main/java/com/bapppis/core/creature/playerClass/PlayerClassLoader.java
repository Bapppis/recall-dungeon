package com.bapppis.core.creature.playerClass;

import com.bapppis.core.Resistances;
import com.bapppis.core.util.ResistancesDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loader for PlayerClass definitions from JSON files.
 * 
 * Loads all player classes from data/creatures/player_classes/ directory
 * and provides lookup methods by ID or name.
 */
public class PlayerClassLoader {
  private static final String PLAYER_CLASS_DIRECTORY = "data/creatures/player_classes";

  private Map<Integer, PlayerClass> classesById;
  private Map<String, PlayerClass> classesByName;

  public PlayerClassLoader() {
    this.classesById = new HashMap<>();
    this.classesByName = new HashMap<>();
  }

  /**
   * Loads all player class JSON files from the player_classes directory.
   */
  public void loadAllPlayerClasses() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Resistances.class, new ResistancesDeserializer())
        .create();

    try (ScanResult scanResult = new ClassGraph()
        .acceptPaths(PLAYER_CLASS_DIRECTORY)
        .scan()) {

      for (Resource resource : scanResult.getAllResources()) {
        String relPath = resource.getPath();
        if (!relPath.endsWith(".json")) continue;

        // Only load JSON files that are directly under the player_classes directory.
        // Ignore talent_trees or other nested subdirectories (they have different schemas).
        String prefix = PLAYER_CLASS_DIRECTORY + "/";
        String relative = relPath.startsWith(prefix) ? relPath.substring(prefix.length()) : relPath;
        // If the relative path contains a slash, it's in a subdirectory - skip it.
        if (relative.contains("/")) {
          // skip nested JSONs (e.g., talent_trees/*.json)
          continue;
        }

        try (Reader reader = new InputStreamReader(resource.open())) {
          PlayerClass playerClass = gson.fromJson(reader, PlayerClass.class);

          if (playerClass != null) {
            classesById.put(playerClass.getId(), playerClass);
            classesByName.put(playerClass.getName().toLowerCase(), playerClass);
            System.out.println("Loaded player class: " + playerClass.getName() + " (ID: " + playerClass.getId() + ")");
          }
        } catch (Exception e) {
          System.err.println("Error loading player class from " + relPath + ": " + e.getMessage());
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      System.err.println("Error scanning player class directory: " + e.getMessage());
      e.printStackTrace();
    }

    System.out.println("Loaded " + classesById.size() + " player classes.");
  }

  /**
   * Gets a player class by its ID.
   * 
   * @param id The class ID (60000-60999 range)
   * @return The PlayerClass, or null if not found
   */
  public PlayerClass getPlayerClassById(int id) {
    return classesById.get(id);
  }

  /**
   * Gets a player class by its name (case-insensitive).
   * 
   * @param name The class name (e.g., "Paladin", "Rogue")
   * @return The PlayerClass, or null if not found
   */
  public PlayerClass getPlayerClassByName(String name) {
    if (name == null) {
      return null;
    }
    return classesByName.get(name.toLowerCase());
  }

  /**
   * Gets all loaded player classes.
   * 
   * @return Map of all classes by ID
   */
  public Map<Integer, PlayerClass> getAllClasses() {
    return new HashMap<>(classesById);
  }

  /**
   * Checks if a player class with the given ID exists.
   * 
   * @param id The class ID
   * @return true if the class exists, false otherwise
   */
  public boolean hasClass(int id) {
    return classesById.containsKey(id);
  }

  /**
   * Checks if a player class with the given name exists.
   * 
   * @param name The class name (case-insensitive)
   * @return true if the class exists, false otherwise
   */
  public boolean hasClass(String name) {
    if (name == null) {
      return false;
    }
    return classesByName.containsKey(name.toLowerCase());
  }

  /**
   * Gets the total number of loaded player classes.
   * 
   * @return Number of classes
   */
  public int getClassCount() {
    return classesById.size();
  }
}

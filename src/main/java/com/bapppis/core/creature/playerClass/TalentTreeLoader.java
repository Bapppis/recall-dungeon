package com.bapppis.core.creature.playerClass;

import com.bapppis.core.util.ResistancesDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads talent trees from JSON files in data/creatures/talent_trees/
 */
public class TalentTreeLoader {
  private static final String TALENT_TREE_PATH = "data/creatures/talent_trees";

  private final Map<Integer, TalentTree> treesById = new HashMap<>();
  private final Map<Integer, TalentTree> treesByClassId = new HashMap<>();
  private final Gson gson;

  public TalentTreeLoader() {
    this.gson = new GsonBuilder()
        .registerTypeAdapter(com.bapppis.core.Resistances.class, new ResistancesDeserializer())
        .create();
    loadAllTalentTrees();
  }

  private void loadAllTalentTrees() {
    try (ScanResult scanResult = new ClassGraph()
        .acceptPaths(TALENT_TREE_PATH)
        .scan()) {

      for (Resource resource : scanResult.getResourcesWithExtension("json")) {
        try {
          String json = resource.getContentAsString();
          TalentTree tree = gson.fromJson(json, TalentTree.class);

          if (tree != null) {
            treesById.put(tree.getId(), tree);
            if (tree.getClassId() != null) {
              treesByClassId.put(tree.getClassId(), tree);
            }
            System.out.println("Loaded talent tree: " + tree.getName() + " (ID: " + tree.getId() + ")");
          }
        } catch (Exception e) {
          System.err.println("Error loading talent tree from " + resource.getPath() + ": " + e.getMessage());
          e.printStackTrace();
        }
      }

      System.out.println("Loaded " + treesById.size() + " talent trees.");
    } catch (Exception e) {
      System.err.println("Error scanning talent trees: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Gets a talent tree by its ID
   */
  public TalentTree getTalentTreeById(int id) {
    return treesById.get(id);
  }

  /**
   * Gets the talent tree for a specific class ID
   */
  public TalentTree getTalentTreeByClassId(int classId) {
    return treesByClassId.get(classId);
  }

  /**
   * Gets all loaded talent trees
   */
  public Map<Integer, TalentTree> getAllTrees() {
    return new HashMap<>(treesById);
  }

  /**
   * Checks if a talent tree exists for a given ID
   */
  public boolean hasTree(int id) {
    return treesById.containsKey(id);
  }

  /**
   * Checks if a talent tree exists for a given class ID
   */
  public boolean hasTreeForClass(int classId) {
    return treesByClassId.containsKey(classId);
  }

  /**
   * Gets the number of loaded talent trees
   */
  public int getTreeCount() {
    return treesById.size();
  }
}

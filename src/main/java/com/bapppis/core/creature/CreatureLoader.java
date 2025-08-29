package com.bapppis.core.creature;

import com.bapppis.core.property.PropertyManager;
import com.bapppis.core.property.Property;
import com.google.gson.Gson;
import com.bapppis.core.creature.player.Player;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CreatureLoader {
    private static final HashMap<String, Creature> creatureMap = new HashMap<>();

    public static void loadCreatures() {
        Gson gson = new Gson();
        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("assets/creatures/players/humanplayers") // Add more paths as needed
                .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        Creature creature;
                        // Try to load as Player, fallback to Creature
                        if (resource.getPath().contains("players")) {
                            creature = gson.fromJson(reader, Player.class);
                        } else {
                            creature = gson.fromJson(reader, Creature.class);
                        }
                        // Load properties by ID
                        if (creature != null && creature.getName() != null) {
                            List<Integer> propertyIds = getPropertyIdsFromJson(resource.getPath(), gson);
                            if (propertyIds != null) {
                                for (Integer id : propertyIds) {
                                    Property prop = PropertyManager.getProperty(id);
                                    if (prop != null) {
                                        creature.addProperty(prop);
                                    }
                                }
                            }
                            // If resistances are present in JSON, they will override defaults
                            creatureMap.put(creature.getName(), creature);
                            System.out.println("Loaded creature: " + creature.getName() + " from " + resource.getPath());
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading creature from: " + resource.getPath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Helper to extract property IDs from JSON
    private static List<Integer> getPropertyIdsFromJson(String resourcePath, Gson gson) {
        try (Reader reader = new InputStreamReader(CreatureLoader.class.getClassLoader().getResourceAsStream(resourcePath))) {
            com.google.gson.JsonObject obj = gson.fromJson(reader, com.google.gson.JsonObject.class);
            if (obj != null && obj.has("properties")) {
                List<Integer> ids = new ArrayList<>();
                for (com.google.gson.JsonElement el : obj.getAsJsonArray("properties")) {
                    ids.add(el.getAsInt());
                }
                return ids;
            }
        } catch (Exception e) {
            // Ignore, fallback to none
        }
        return null;
    }

    public static Creature getCreature(String name) {
        return creatureMap.get(name);
    }

    public static List<Creature> getAllCreatures() {
        return new ArrayList<>(creatureMap.values());
    }
}

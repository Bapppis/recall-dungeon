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
import java.util.EnumMap;

public class CreatureLoader {
    private static final HashMap<String, Creature> creatureMap = new HashMap<>();
    private static final HashMap<Integer, Creature> creatureIdMap = new HashMap<>();

    public static void loadCreatures() {
        // Fresh load each time
        creatureMap.clear();
        creatureIdMap.clear();

        Gson gson = new Gson();
        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("assets/creatures") // Scan all creatures and subfolders (players, enemies, etc.)
                .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        Creature creature;
                        // Try to load as Player, fallback to Creature
                        if (resource.getPath().contains("players")) {
                            creature = gson.fromJson(reader, Player.class);
                        } else {
                            creature = gson.fromJson(reader, Enemy.class);
                        }
                        // Load properties by ID array from JSON
                        if (creature != null) {
                            // Determine creature type based on id ranges when available
                            int cidForType = creature.getId();
                            if (cidForType >= 5000 && cidForType < 6000) {
                                creature.setType(Creature.Type.PLAYER);
                            } else if (cidForType >= 6000 && cidForType < 7000) {
                                creature.setType(Creature.Type.ENEMY);
                            }
                            // Ensure all stats are set to defaults if missing
                            // Only set default if stat key is missing from the map
                            @SuppressWarnings("unchecked")
                            EnumMap<Creature.Stats, Integer> statMap = null;
                            try {
                                java.lang.reflect.Field statsField = Creature.class.getDeclaredField("stats");
                                statsField.setAccessible(true);
                                statMap = (EnumMap<Creature.Stats, Integer>) statsField.get(creature);
                            } catch (Exception e) {
                                // Should not happen
                            }
                            if (statMap != null) {
                                for (Creature.Stats stat : Creature.Stats.values()) {
                                    if (!statMap.containsKey(stat)) {
                                        if (stat == Creature.Stats.LUCK) {
                                            creature.setStat(stat, 1);
                                        } else {
                                            creature.setStat(stat, 10);
                                        }
                                    }
                                }
                            }
                            // Ensure all resistances are set to 100 unless provided
                            @SuppressWarnings("unchecked")
                            EnumMap<Creature.Resistances, Integer> resistMap = null;
                            try {
                                java.lang.reflect.Field resistField = Creature.class.getDeclaredField("resistances");
                                resistField.setAccessible(true);
                                resistMap = (EnumMap<Creature.Resistances, Integer>) resistField.get(creature);
                            } catch (Exception e) {
                                // Should not happen
                            }
                            if (resistMap != null) {
                                for (Creature.Resistances res : Creature.Resistances.values()) {
                                    if (!resistMap.containsKey(res)) {
                                        creature.setResistance(res, 100);
                                    }
                                }
                            }
                            if (creature.getHpLvlBonus() == 0) {
                                creature.setHpLvlBonus(6);
                            }
                            if (creature.getVisionRange() == 0) {
                                creature.setVisionRange(1);
                            }
                            // Now apply properties (modifiers will be correct)
                            List<Integer> propertyIds = getPropertyIdsFromJson(resource.getPath(), gson);
                            if (propertyIds != null) {
                                for (Integer pid : propertyIds) {
                                    Property prop = PropertyManager.getProperty(pid);
                                    if (prop != null) {
                                        creature.addProperty(prop);
                                    }
                                }
                            }
                            // Apply any property effects that modify stats or other attributes
                            int baseHp = creature.getBaseHp();
                            creature.setMaxHp(baseHp); // Reset max HP to base before applying properties
                            //creature.setCurrentHp(baseHp);
                            creature.updateMaxHp();
                            int tempXp = creature.getXp();
                            // Convert all levels to XP and add to tempXp
                            int lvl = creature.getLevel();
                            for (int i = lvl; i > 0; i--) {
                                tempXp += i * 10;
                            }
                            creature.setLevel(0); // Reset level to 0 after converting to XP
                            creature.setXp(0); // Reset XP to 0 before adding
                            creature.addXp(tempXp); // Add any extra XP specified in JSON
                            // Index by id (primary) and by name (optional)
                            int cid = creature.getId();
                            if (cid > 0) {
                                if (creatureIdMap.containsKey(cid)) {
                                    System.out.println(
                                            "Warning: Duplicate creature id " + cid + ". Overwriting previous entry.");
                                }
                                creatureIdMap.put(cid, creature);
                            }

                            String cname = creature.getName();
                            if (cname != null && !cname.isEmpty()) {
                                creatureMap.put(cname, creature);
                            }
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
        try (Reader reader = new InputStreamReader(
                CreatureLoader.class.getClassLoader().getResourceAsStream(resourcePath))) {
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

    public static Creature getCreatureById(int id) {
        return creatureIdMap.get(id);
    }

    public static List<Creature> getAllCreatures() {
        return new ArrayList<>(creatureMap.values());
    }
}

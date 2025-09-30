package com.bapppis.core.creature;

import com.bapppis.core.property.PropertyManager;
import com.bapppis.core.property.Property;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;
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
    private static final HashMap<String, Player> playerMap = new HashMap<>();
    private static final HashMap<Integer, Player> playerIdMap = new HashMap<>();

    public static void loadCreatures() {
        // Fresh load each time
        creatureMap.clear();
        creatureIdMap.clear();
        playerMap.clear();
        playerIdMap.clear();

        // Ensure items are loaded first so starting inventory/equipment ids resolve
        try {
            com.bapppis.core.item.ItemLoader.loadItems();
        } catch (Exception e) {
            // ignore if items already loaded or loading fails here
        }

        Gson gson = new Gson();
        try (ScanResult scanResult = new ClassGraph()
        // Scan production creature data and also test fixture package so unit tests
        // with JSON under com/... are found during test execution.
        .acceptPaths("data/creatures", "com/bapppis/core/Creature")
        .scan()) {
            // Keep track of relative resource paths we've processed so we don't load the
            // same path multiple times when it appears on the classpath (for example
            // when both a source and compiled copy exist). This prevents duplicate-id
            // warnings caused by duplicate classpath entries.
            java.util.Set<String> processedResourcePaths = new java.util.HashSet<>();
            for (Resource resource : scanResult.getAllResources()) {
                String relPath = resource.getPath();
                // Skip duplicates of the same relative path
                if (processedResourcePaths.contains(relPath)) continue;
                processedResourcePaths.add(relPath);
                if (relPath.endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        Creature creature;
                        // Read the JSON into a JsonObject first so we can strip fields that would
                        // conflict with existing types (for example: inventory is an array in
                        // JSON but Creature.inventory is an Inventory object). We'll remove
                        // the 'inventory' and equipment slot fields before letting Gson map
                        // the JSON to the Creature/Player class.
                        com.google.gson.JsonElement parsed = gson.fromJson(reader, com.google.gson.JsonElement.class);
                        if (parsed == null || !parsed.isJsonObject()) {
                            continue;
                        }
                        com.google.gson.JsonObject jsonObj = parsed.getAsJsonObject();
                        // Remove starting-item related fields so Gson won't try to map them
                        jsonObj.remove("inventory");
                        jsonObj.remove("helmet");
                        jsonObj.remove("armor");
                        jsonObj.remove("legwear");
                        jsonObj.remove("weapon");
                        jsonObj.remove("offhand");

                        // Try to load as Player, fallback to Enemy
                        if (resource.getPath().contains("players")) {
                            creature = gson.fromJson(jsonObj, Player.class);
                        } else {
                            creature = gson.fromJson(jsonObj, Enemy.class);
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
                            // Load starting inventory and equipment slots from JSON
                            applyStartingItemsFromJson(resource.getPath(), gson, creature);
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
                                // If we've already indexed this numeric id from a previously
                                // processed resource, skip the duplicate to avoid overwriting
                                // and noisy warnings when the same file appears on the
                                // classpath multiple times (source + compiled).
                                if (creatureIdMap.containsKey(cid) || playerIdMap.containsKey(cid)) {
                                    // skip this duplicate resource entirely
                                    continue;
                                }
                                if (creature instanceof Player) {
                                    playerIdMap.put(cid, (Player) creature);
                                } else {
                                    creatureIdMap.put(cid, creature);
                                }
                            }

                            String cname = creature.getName();
                            if (cname != null && !cname.isEmpty()) {
                                if (creature instanceof Player) {
                                    playerMap.put(cname, (Player) creature);
                                } else {
                                    creatureMap.put(cname, creature);
                                }
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

    private static void applyStartingItemsFromJson(String resourcePath, Gson gson, Creature creature) {
        try (Reader reader = new InputStreamReader(
                CreatureLoader.class.getClassLoader().getResourceAsStream(resourcePath))) {
            com.google.gson.JsonObject obj = gson.fromJson(reader, com.google.gson.JsonObject.class);
            if (obj == null) return;
            // Inventory array
            if (obj.has("inventory") && obj.get("inventory").isJsonArray()) {
                for (com.google.gson.JsonElement el : obj.getAsJsonArray("inventory")) {
                    try {
                        int itemId = el.getAsInt();
                        com.bapppis.core.item.Item template = ItemLoader.getItemById(itemId);
                        if (template != null) {
                            // Deep-copy the template so each creature gets its own instance
                            com.bapppis.core.item.Item copy = gson.fromJson(gson.toJson(template), template.getClass());
                            creature.getInventory().addItem(copy);
                        }
                    } catch (Exception e) {
                        // ignore invalid item entries
                    }
                }
            }

            // Equipment slots: helmet, armor, legwear, weapon, offhand
            String[] slots = new String[] {"helmet", "armor", "legwear", "weapon", "offhand"};
            for (String slotName : slots) {
                if (obj.has(slotName) && obj.get(slotName).isJsonPrimitive()) {
                    try {
                        int itemId = obj.get(slotName).getAsInt();
                        com.bapppis.core.item.Item template = ItemLoader.getItemById(itemId);
                        if (template != null) {
                            com.bapppis.core.item.Item copy = gson.fromJson(gson.toJson(template), template.getClass());
                            // Add to inventory first (equipItem will remove it from inventory)
                            creature.getInventory().addItem(copy);
                            creature.equipItem(copy);
                        }
                    } catch (Exception e) {
                        // ignore invalid slot entries
                    }
                }
            }
        } catch (Exception e) {
            // ignore
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
        Creature c = creatureMap.get(name);
        if (c != null) return c;
        Creature p = playerMap.get(name);
        if (p != null) return p;
        // Debug: if not found, list available creature names (helpful for tests)
        System.out.println("CreatureLoader: lookup failed for '" + name + "'. Available creatures: " + creatureMap.keySet());
        System.out.println("CreatureLoader: available players: " + playerMap.keySet());
        return null;
    }

    public static Creature getCreatureById(int id) {
        Creature c = creatureIdMap.get(id);
        if (c != null) return c;
        return playerIdMap.get(id);
    }

    public static List<Creature> getAllCreatures() {
        ArrayList<Creature> combined = new ArrayList<>(creatureMap.values());
        combined.addAll(playerMap.values());
        return combined;
    }

    // Player-specific accessors
    public static Player getPlayer(String name) {
        return playerMap.get(name);
    }

    public static Player getPlayerById(int id) {
        return playerIdMap.get(id);
    }

    public static List<Player> getAllPlayers() {
        return new ArrayList<>(playerMap.values());
    }
}

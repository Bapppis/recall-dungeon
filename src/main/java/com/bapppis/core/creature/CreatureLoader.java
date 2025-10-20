package com.bapppis.core.creature;

import com.bapppis.core.property.PropertyLoader;
import com.bapppis.core.property.Property;
import com.bapppis.core.item.ItemLoader;

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
    private static boolean loaded = false;

    public static void loadCreatures() {
        if (loaded)
            return;
        forceReload();
    }

    /**
     * Force reload of all creatures, even if already loaded. Use in individual
     * tests.
     */
    public static void forceReload() {
        creatureMap.clear();
        creatureIdMap.clear();
        playerMap.clear();
        playerIdMap.clear();
        loaded = false;

        // Ensure items are loaded first so starting inventory/equipment ids resolve
        try {
            com.bapppis.core.item.ItemLoader.loadItems();
        } catch (Exception e) {
            // ignore if items already loaded or loading fails here
        }

        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .registerTypeAdapter(com.bapppis.core.Resistances.class,
                        new com.bapppis.core.util.ResistancesDeserializer())
                .create();
        try (ScanResult scanResult = new ClassGraph()
                // Scan production creature data and the test fixture package so unit tests
                // find JSON under com/... during test execution.
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
                if (processedResourcePaths.contains(relPath))
                    continue;
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

                        // Extract species and creatureType from JSON to determine the correct class
                        String speciesName = null;
                        String creatureTypeName = null;
                        if (jsonObj.has("species") && !jsonObj.get("species").isJsonNull()) {
                            speciesName = jsonObj.get("species").getAsString();
                        }
                        if (jsonObj.has("creatureType") && !jsonObj.get("creatureType").isJsonNull()) {
                            creatureTypeName = jsonObj.get("creatureType").getAsString();
                        }

                        // Try to instantiate the specific species class if available
                        if (speciesName != null && creatureTypeName != null) {
                            String packageName = "com.bapppis.core.creature.creaturetype."
                                    + creatureTypeName.toLowerCase();
                            String className = packageName + "." + speciesName;
                            try {
                                Class<?> speciesClass = Class.forName(className);

                                // Create a species template to get constructor-added properties and fields
                                Creature speciesTemplate = (Creature) speciesClass.getDeclaredConstructor()
                                        .newInstance();

                                // Create the actual Player/Enemy/NPC instance
                                if (resource.getPath().contains("players")) {
                                    creature = new Player();
                                } else if (resource.getPath().contains("npcs")) {
                                    creature = new NPC();
                                } else {
                                    creature = new Enemy();
                                }

                                // Load JSON data first
                                Creature tempCreature;
                                if (resource.getPath().contains("players")) {
                                    tempCreature = gson.fromJson(jsonObj, Player.class);
                                } else if (resource.getPath().contains("npcs")) {
                                    tempCreature = gson.fromJson(jsonObj, NPC.class);
                                } else {
                                    tempCreature = gson.fromJson(jsonObj, Enemy.class);
                                }

                                // Priority 1 (lowest): Copy JSON fields to creature
                                copyCreatureFields(tempCreature, creature);
                                
                                // Priority 2 (highest): Copy species-specific fields and properties
                                // This includes size, properties from constructors, etc.
                                // Uses selective copying to preserve JSON data (id, name, stats)
                                copySpeciesFields(speciesTemplate, creature);
                            } catch (ClassNotFoundException e) {
                                // Species class not found, fall back to base type loading
                                if (resource.getPath().contains("players")) {
                                    creature = gson.fromJson(jsonObj, Player.class);
                                } else if (resource.getPath().contains("npcs")) {
                                    creature = gson.fromJson(jsonObj, NPC.class);
                                } else {
                                    creature = gson.fromJson(jsonObj, Enemy.class);
                                }
                            } catch (Exception e) {
                                // Any other error, fall back to base type loading
                                if (resource.getPath().contains("players")) {
                                    creature = gson.fromJson(jsonObj, Player.class);
                                } else if (resource.getPath().contains("npcs")) {
                                    creature = gson.fromJson(jsonObj, NPC.class);
                                } else {
                                    creature = gson.fromJson(jsonObj, Enemy.class);
                                }
                            }
                        } else {
                            // No species specified, use base type loading
                            if (resource.getPath().contains("players")) {
                                creature = gson.fromJson(jsonObj, Player.class);
                            } else if (resource.getPath().contains("npcs")) {
                                creature = gson.fromJson(jsonObj, NPC.class);
                            } else {
                                creature = gson.fromJson(jsonObj, Enemy.class);
                            }
                        }
                        // Load properties by ID array from JSON
                        if (creature != null) {
                            // Determine creature type based on id ranges or class type
                            int cidForType = creature.getId();
                            if (creature instanceof Player || (cidForType >= 5000 && cidForType < 5500)) {
                                creature.setType(com.bapppis.core.creature.creatureEnums.Type.PLAYER);
                            } else if (creature instanceof NPC || (cidForType >= 5500 && cidForType < 6000)) {
                                creature.setType(com.bapppis.core.creature.creatureEnums.Type.NPC);
                            } else if (creature instanceof Enemy || (cidForType >= 6000 && cidForType < 20000)) {
                                creature.setType(com.bapppis.core.creature.creatureEnums.Type.ENEMY);
                            }
                            // Ensure all stats are set to defaults if missing
                            // Only set default if stat key is missing from the map
                            @SuppressWarnings("unchecked")
                            EnumMap<com.bapppis.core.creature.creatureEnums.Stats, Integer> statMap = null;
                            try {
                                java.lang.reflect.Field statsField = Creature.class.getDeclaredField("stats");
                                statsField.setAccessible(true);
                                statMap = (EnumMap<com.bapppis.core.creature.creatureEnums.Stats, Integer>) statsField.get(creature);
                            } catch (Exception e) {
                                // Should not happen
                            }
                            if (statMap != null) {
                                for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats.values()) {
                                    if (!statMap.containsKey(stat)) {
                                        if (stat == com.bapppis.core.creature.creatureEnums.Stats.LUCK) {
                                            creature.setStat(stat, 1);
                                        } else {
                                            creature.setStat(stat, 10);
                                        }
                                    }
                                }
                            }
                            // Ensure all resistances are set to 100 unless provided
                            @SuppressWarnings("unchecked")
                            EnumMap<com.bapppis.core.Resistances, Integer> resistMap = null;
                            try {
                                java.lang.reflect.Field resistField = Creature.class.getDeclaredField("resistances");
                                resistField.setAccessible(true);
                                resistMap = (EnumMap<com.bapppis.core.Resistances, Integer>) resistField.get(creature);
                            } catch (Exception e) {
                                // Should not happen
                            }
                            if (resistMap != null) {
                                for (com.bapppis.core.Resistances res : com.bapppis.core.Resistances.values()) {
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
                            // Apply properties (modifiers will be correct)
                            List<Integer> propertyIds = getPropertyIdsFromJson(resource.getPath(), gson);
                            if (propertyIds != null) {
                                for (Integer pid : propertyIds) {
                                    Property prop = PropertyLoader.getProperty(pid);
                                    if (prop != null) {
                                        creature.addProperty(prop);
                                    }
                                }
                            }
                            // Load starting inventory and equipment slots from JSON
                            applyStartingItemsFromJson(resource.getPath(), gson, creature);

                            // Finalize creature fields after load (resets HP, recalculates mana, converts
                            // level->XP)
                            creature.finalizeAfterLoad();
                            creature.recalcDerivedStats();
                            // Index by id (primary) and by name (optional)
                            int cid = creature.getId();
                            if (cid > 0) {
                                if (creatureIdMap.containsKey(cid) || playerIdMap.containsKey(cid)) {
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
                                String key = cname.trim().toLowerCase();
                                String keyNoSpace = key.replaceAll("\\s+", "");
                                if (creature instanceof Player) {
                                    playerMap.put(key, (Player) creature);
                                    if (!playerMap.containsKey(keyNoSpace)) {
                                        playerMap.put(keyNoSpace, (Player) creature);
                                    }
                                } else {
                                    creatureMap.put(key, creature);
                                    if (!creatureMap.containsKey(keyNoSpace)) {
                                        creatureMap.put(keyNoSpace, creature);
                                    }
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
        loaded = true;
    }

    /**
     * Copy fields from source creature to destination creature using reflection.
     * This is used when a species-specific class instance needs to be populated
     * with JSON data. Properties are handled separately to preserve those added
     * by type/species constructors.
     */
    private static void copyCreatureFields(Creature source, Creature dest) {
        if (source == null || dest == null)
            return;

        try {
            // Copy all accessible fields from Creature class
            java.lang.reflect.Field[] fields = Creature.class.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                // Skip static, final, and propertyManager fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                        java.lang.reflect.Modifier.isFinal(field.getModifiers()) ||
                        field.getName().equals("propertyManager")) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(source);
                field.set(dest, value);
            }
        } catch (Exception e) {
            System.err.println("Error copying creature fields: " + e.getMessage());
        }
    }

    /**
     * Copy only species-specific fields (size, properties) from species template to creature.
     * This preserves JSON data like id, name, stats while applying species defaults.
     */
    private static void copySpeciesFields(Creature speciesTemplate, Creature creature) {
        if (speciesTemplate == null || creature == null)
            return;

        try {
            // Copy size (species can override)
            creature.setSize(speciesTemplate.getSize());

            // Copy properties from species template (traits, buffs, debuffs)
            for (com.bapppis.core.property.Property prop : speciesTemplate.getTraits().values()) {
                creature.addProperty(prop);
            }
            for (com.bapppis.core.property.Property prop : speciesTemplate.getBuffs().values()) {
                creature.addProperty(prop);
            }
            for (com.bapppis.core.property.Property prop : speciesTemplate.getDebuffs().values()) {
                creature.addProperty(prop);
            }
        } catch (Exception e) {
            System.err.println("Error copying species fields: " + e.getMessage());
        }
    }

    private static void applyStartingItemsFromJson(String resourcePath, com.google.gson.Gson gson, Creature creature) {
        try (Reader reader = new InputStreamReader(
                CreatureLoader.class.getClassLoader().getResourceAsStream(resourcePath))) {
            com.google.gson.JsonObject obj = gson.fromJson(reader, com.google.gson.JsonObject.class);
            if (obj == null)
                return;
            // Inventory array
            if (obj.has("inventory") && obj.get("inventory").isJsonArray()) {
                for (com.google.gson.JsonElement el : obj.getAsJsonArray("inventory")) {
                    try {
                        com.bapppis.core.item.Item template = null;
                        if (el.isJsonPrimitive()) {
                            if (el.getAsJsonPrimitive().isNumber()) {
                                int itemId = el.getAsInt();
                                template = ItemLoader.getItemById(itemId);
                            } else if (el.getAsJsonPrimitive().isString()) {
                                String itemNameOrId = el.getAsString();
                                try {
                                    int itemId = Integer.parseInt(itemNameOrId);
                                    template = ItemLoader.getItemById(itemId);
                                } catch (NumberFormatException nfe) {
                                    template = ItemLoader.getItemByName(itemNameOrId);
                                }
                            }
                        }
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
            String[] slots = new String[] { "helmet", "armor", "legwear", "weapon", "offhand" };
            for (String slotName : slots) {
                if (obj.has(slotName) && obj.get(slotName).isJsonPrimitive()) {
                    try {
                        com.bapppis.core.item.Item template = null;
                        if (obj.get(slotName).isJsonPrimitive()) {
                            if (obj.get(slotName).getAsJsonPrimitive().isNumber()) {
                                int itemId = obj.get(slotName).getAsInt();
                                template = ItemLoader.getItemById(itemId);
                            } else if (obj.get(slotName).getAsJsonPrimitive().isString()) {
                                String itemNameOrId = obj.get(slotName).getAsString();
                                try {
                                    int itemId = Integer.parseInt(itemNameOrId);
                                    template = ItemLoader.getItemById(itemId);
                                } catch (NumberFormatException nfe) {
                                    template = ItemLoader.getItemByName(itemNameOrId);
                                }
                            }
                        }
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
    private static List<Integer> getPropertyIdsFromJson(String resourcePath, com.google.gson.Gson gson) {
        try (Reader reader = new InputStreamReader(
                CreatureLoader.class.getClassLoader().getResourceAsStream(resourcePath))) {
            com.google.gson.JsonObject obj = gson.fromJson(reader, com.google.gson.JsonObject.class);
            if (obj != null && obj.has("properties")) {
                List<Integer> ids = new ArrayList<>();
                for (com.google.gson.JsonElement el : obj.getAsJsonArray("properties")) {
                    try {
                        if (el.isJsonPrimitive()) {
                            if (el.getAsJsonPrimitive().isNumber()) {
                                ids.add(el.getAsInt());
                            } else if (el.getAsJsonPrimitive().isString()) {
                                String propNameOrId = el.getAsString();
                                try {
                                    int propId = Integer.parseInt(propNameOrId);
                                    ids.add(propId);
                                } catch (NumberFormatException nfe) {
                                    com.bapppis.core.property.Property prop = com.bapppis.core.property.PropertyLoader
                                            .getPropertyByName(propNameOrId);
                                    if (prop != null) {
                                        ids.add(prop.getId());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // ignore invalid property entries
                    }
                }
                return ids;
            }
        } catch (Exception e) {
            // Ignore, fallback to none
        }
        return null;
    }

    public static Creature getCreature(String name) {
        if (name == null)
            return null;
        String key = name.trim().toLowerCase();
        Creature c = creatureMap.get(key);
        if (c != null)
            return c;
        Creature p = playerMap.get(key);
        if (p != null)
            return p;
        // try space-free variant
        String keyNoSpace = key.replaceAll("\\s+", "");
        c = creatureMap.get(keyNoSpace);
        if (c != null)
            return c;
        return playerMap.get(keyNoSpace);
    }

    public static Creature getCreatureById(int id) {
        Creature c = creatureIdMap.get(id);
        if (c != null)
            return c;
        return playerIdMap.get(id);
    }

    /**
     * Create a deep-copy spawn of the creature template looked up by name or id.
     * Returns null if no matching template found.
     */
    public static Creature spawnCreatureByName(String name) {
        if (name == null || name.isBlank())
            return null;
        String t = name.trim();
        try {
            int id = Integer.parseInt(t);
            Creature tmpl = getCreatureById(id);
            if (tmpl == null)
                return null;
            // Use a Gson instance that excludes the 'propertyManager' field during
            // serialization to avoid serializing the back-reference to the creature
            // (which causes infinite recursion). The new instance will create its
            // own PropertyManager in the constructor when deserialized.
            com.google.gson.Gson g = new com.google.gson.GsonBuilder()
                    .addSerializationExclusionStrategy(new com.google.gson.ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(com.google.gson.FieldAttributes f) {
                            return "propertyManager".equals(f.getName());
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> clazz) {
                            return false;
                        }
                    }).create();
            Creature copy = g.fromJson(g.toJson(tmpl), tmpl.getClass());
            copy.finalizeAfterLoad();
            return copy;
        } catch (NumberFormatException ignored) {
        }
        Creature tmpl = getCreature(name);
        if (tmpl == null)
            return null;
        try {
            com.google.gson.Gson g = new com.google.gson.GsonBuilder()
                    .addSerializationExclusionStrategy(new com.google.gson.ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(com.google.gson.FieldAttributes f) {
                            return "propertyManager".equals(f.getName());
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> clazz) {
                            return false;
                        }
                    }).create();
            Creature copy = g.fromJson(g.toJson(tmpl), tmpl.getClass());
            copy.finalizeAfterLoad();
            return copy;
        } catch (Exception e) {
            return null;
        }
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
        // Use playerIdMap to avoid duplicates (playerMap has same player under multiple
        // name keys)
        return new ArrayList<>(playerIdMap.values());
    }
}

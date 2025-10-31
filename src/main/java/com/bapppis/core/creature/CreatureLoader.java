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
                        // Remember if JSON explicitly provided a size — JSON should override species/type
                        boolean jsonProvidedSize = jsonObj.has("size") && !jsonObj.get("size").isJsonNull();
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
                                // Check if species class exists
                                Class<?> speciesClass = Class.forName(className);

                                // Create TWO templates: one before species mods, one after
                                Creature beforeTemplate = (Creature) speciesClass.getDeclaredConstructor()
                                        .newInstance();

                                // Capture initial state (all stats = 10 except LUCK = 1, size = MEDIUM)
                                java.util.Map<com.bapppis.core.creature.creatureEnums.Stats, Integer> statsBefore = new java.util.EnumMap<>(
                                        com.bapppis.core.creature.creatureEnums.Stats.class);
                                java.util.Map<com.bapppis.core.Resistances, Integer> resistBefore = new java.util.EnumMap<>(
                                        com.bapppis.core.Resistances.class);
                                for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats
                                        .values()) {
                                    statsBefore.put(stat, beforeTemplate.getStat(stat));
                                }
                                for (com.bapppis.core.Resistances res : com.bapppis.core.Resistances.values()) {
                                    resistBefore.put(res, beforeTemplate.getResistance(res));
                                }
                                com.bapppis.core.creature.creatureEnums.Size sizeBefore = beforeTemplate.getSize();

                                // Capture base field values before species modifications so we can compute
                                // deltas (for baseCrit, baseDodge, baseBlock, baseMagicResist, accuracy,
                                // magicAccuracy, hpDice) and apply them to the JSON-loaded creature later.
                                java.util.Map<String, Double> baseFieldsBefore = new java.util.HashMap<>();
                                try {
                                    String[] baseNames = new String[] { "baseCrit", "baseDodge", "baseBlock",
                                            "baseMagicResist", "accuracy", "magicAccuracy", "hpDice",
                                            "baseHp", "baseMaxMana", "baseMaxStamina", "baseHpRegen",
                                            "baseStaminaRegen", "baseManaRegen" };
                                    for (String fname : baseNames) {
                                        try {
                                            java.lang.reflect.Field f = Creature.class.getDeclaredField(fname);
                                            f.setAccessible(true);
                                            Object v = f.get(beforeTemplate);
                                            if (v instanceof Number) {
                                                baseFieldsBefore.put(fname, ((Number) v).doubleValue());
                                            } else {
                                                baseFieldsBefore.put(fname, 0.0);
                                            }
                                        } catch (NoSuchFieldException nsf) {
                                            // ignore missing fields
                                        }
                                    }
                                } catch (Exception e) {
                                    // ignore reflection failures
                                }

                                // Apply species modifications
                                beforeTemplate.applySpeciesModifications();

                                // Calculate pure stat deltas (INCLUDES property effects, which we'll subtract
                                // later)
                                java.util.Map<com.bapppis.core.creature.creatureEnums.Stats, Integer> statsAfter = new java.util.EnumMap<>(
                                        com.bapppis.core.creature.creatureEnums.Stats.class);
                                for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats
                                        .values()) {
                                    statsAfter.put(stat, beforeTemplate.getStat(stat));
                                }

                                // Calculate property stat modifier totals to subtract out
                                java.util.Map<com.bapppis.core.creature.creatureEnums.Stats, Integer> propertyEffects = new java.util.EnumMap<>(
                                        com.bapppis.core.creature.creatureEnums.Stats.class);
                                for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats
                                        .values()) {
                                    propertyEffects.put(stat, 0);
                                }

                                try {
                                    java.lang.reflect.Field pmField = Creature.class
                                            .getDeclaredField("propertyManager");
                                    pmField.setAccessible(true);
                                    PropertyManager templatePM = (PropertyManager) pmField.get(beforeTemplate);

                                    // Sum up stat modifiers from all properties
                                    for (Property prop : templatePM.getBuffs().values()) {
                                        if (prop.getStatModifiers() != null) {
                                            for (java.util.Map.Entry<com.bapppis.core.creature.creatureEnums.Stats, Integer> entry : prop
                                                    .getStatModifiers().entrySet()) {
                                                propertyEffects.put(entry.getKey(),
                                                        propertyEffects.get(entry.getKey()) + entry.getValue());
                                            }
                                        }
                                    }
                                    for (Property prop : templatePM.getDebuffs().values()) {
                                        if (prop.getStatModifiers() != null) {
                                            for (java.util.Map.Entry<com.bapppis.core.creature.creatureEnums.Stats, Integer> entry : prop
                                                    .getStatModifiers().entrySet()) {
                                                propertyEffects.put(entry.getKey(),
                                                        propertyEffects.get(entry.getKey()) + entry.getValue());
                                            }
                                        }
                                    }
                                    for (Property prop : templatePM.getTraits().values()) {
                                        if (prop.getStatModifiers() != null) {
                                            for (java.util.Map.Entry<com.bapppis.core.creature.creatureEnums.Stats, Integer> entry : prop
                                                    .getStatModifiers().entrySet()) {
                                                propertyEffects.put(entry.getKey(),
                                                        propertyEffects.get(entry.getKey()) + entry.getValue());
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // If we can't read properties, leave propertyEffects as zeros
                                }

                                // Calculate PURE stat deltas (species modifications only, excluding property
                                // effects)
                                java.util.Map<com.bapppis.core.creature.creatureEnums.Stats, Integer> pureStatDeltas = new java.util.EnumMap<>(
                                        com.bapppis.core.creature.creatureEnums.Stats.class);
                                for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats
                                        .values()) {
                                    int totalDelta = statsAfter.get(stat) - statsBefore.get(stat);
                                    int propEffect = propertyEffects.get(stat);
                                    pureStatDeltas.put(stat, totalDelta - propEffect);
                                }

                                // Compute base-field deltas after species modifications
                                java.util.Map<String, Double> baseFieldDeltas = new java.util.HashMap<>();
                                try {
                                    String[] baseNames = new String[] { "baseCrit", "baseDodge", "baseBlock",
                                            "baseMagicResist", "accuracy", "magicAccuracy", "hpDice",
                                            "baseHp", "baseMaxMana", "baseMaxStamina", "baseHpRegen",
                                            "baseStaminaRegen", "baseManaRegen" };
                                    for (String fname : baseNames) {
                                        try {
                                            java.lang.reflect.Field f = Creature.class.getDeclaredField(fname);
                                            f.setAccessible(true);
                                            Object afterVal = f.get(beforeTemplate); // beforeTemplate now contains
                                                                                     // post-mod
                                            double after = (afterVal instanceof Number)
                                                    ? ((Number) afterVal).doubleValue()
                                                    : 0.0;
                                            double before = baseFieldsBefore.getOrDefault(fname, 0.0);
                                            double delta = after - before;
                                            if (Math.abs(delta) > 0.0001) {
                                                baseFieldDeltas.put(fname, delta);
                                            }
                                        } catch (NoSuchFieldException nsf) {
                                            // ignore
                                        }
                                    }
                                } catch (Exception e) {
                                    // ignore reflection failures
                                }

                                // Load JSON as base type (Player/Enemy/NPC) to preserve type safety
                                if (resource.getPath().contains("players")) {
                                    creature = gson.fromJson(jsonObj, Player.class);
                                } else if (resource.getPath().contains("npcs")) {
                                    creature = gson.fromJson(jsonObj, NPC.class);
                                } else {
                                    creature = gson.fromJson(jsonObj, Enemy.class);
                                }

                                // IMPORTANT: Fill in missing stats with defaults BEFORE applying species
                                // modifications
                                // This ensures getStat() returns correct defaults (10 or 1 for LUCK) not 0
                                try {
                                    java.lang.reflect.Field statsField = Creature.class.getDeclaredField("stats");
                                    statsField.setAccessible(true);
                                    @SuppressWarnings("unchecked")
                                    EnumMap<com.bapppis.core.creature.creatureEnums.Stats, Integer> statMap = (EnumMap<com.bapppis.core.creature.creatureEnums.Stats, Integer>) statsField
                                            .get(creature);

                                    for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats
                                            .values()) {
                                        if (!statMap.containsKey(stat)) {
                                            if (stat == com.bapppis.core.creature.creatureEnums.Stats.LUCK) {
                                                statMap.put(stat, 1);
                                            } else {
                                                statMap.put(stat, 10);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // Should not happen
                                }

                                // Apply PURE stat deltas (species modifications without property effects)
                                for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats
                                        .values()) {
                                    int delta = pureStatDeltas.get(stat);
                                    if (delta != 0) {
                                        creature.modifyStat(stat, delta);
                                    }
                                }

                                // Copy resistance modifications
                                for (com.bapppis.core.Resistances res : com.bapppis.core.Resistances.values()) {
                                    int templateValue = beforeTemplate.getResistance(res);
                                    int beforeValue = resistBefore.get(res);
                                    int delta = templateValue - beforeValue;
                                    if (delta != 0) {
                                        creature.modifyResistance(res, delta);
                                    }
                                }

                                // Apply base-field deltas (if any) to the loaded creature so species can
                                // override
                                // base values (mirrors how stat/resistance deltas are applied)
                                // Apply deltas for base fields to the loaded creature
                                if (baseFieldDeltas.containsKey("baseCrit")) {
                                    creature.modifyBaseCrit(baseFieldDeltas.get("baseCrit").floatValue());
                                }
                                if (baseFieldDeltas.containsKey("baseDodge")) {
                                    creature.modifyBaseDodge(baseFieldDeltas.get("baseDodge").floatValue());
                                }
                                if (baseFieldDeltas.containsKey("baseBlock")) {
                                    creature.modifyBaseBlock(baseFieldDeltas.get("baseBlock").floatValue());
                                }
                                if (baseFieldDeltas.containsKey("baseMagicResist")) {
                                    creature.modifyBaseMagicResist(baseFieldDeltas.get("baseMagicResist").floatValue());
                                }
                                if (baseFieldDeltas.containsKey("accuracy")) {
                                    creature.modifyBaseAccuracy(baseFieldDeltas.get("accuracy").intValue());
                                }
                                if (baseFieldDeltas.containsKey("magicAccuracy")) {
                                    creature.modifyBaseMagicAccuracy(baseFieldDeltas.get("magicAccuracy").intValue());
                                }
                                if (baseFieldDeltas.containsKey("hpDice")) {
                                    int deltaHp = (int) Math.round(baseFieldDeltas.get("hpDice"));
                                    if (deltaHp != 0) {
                                        creature.setHpDice(creature.getHpDice() + deltaHp);
                                    }
                                }
                                if (baseFieldDeltas.containsKey("baseHp")) {
                                    int delta = (int) Math.round(baseFieldDeltas.get("baseHp"));
                                    if (delta != 0) {
                                        creature.modifyBaseHp(delta);
                                    }
                                }
                                if (baseFieldDeltas.containsKey("baseMaxMana")) {
                                    int delta = (int) Math.round(baseFieldDeltas.get("baseMaxMana"));
                                    if (delta != 0)
                                        creature.modifyBaseMaxMana(delta);
                                }
                                if (baseFieldDeltas.containsKey("baseMaxStamina")) {
                                    int delta = (int) Math.round(baseFieldDeltas.get("baseMaxStamina"));
                                    if (delta != 0)
                                        creature.modifyBaseMaxStamina(delta);
                                }
                                if (baseFieldDeltas.containsKey("baseHpRegen")) {
                                    int delta = (int) Math.round(baseFieldDeltas.get("baseHpRegen"));
                                    if (delta != 0)
                                        creature.modifyBaseHpRegen(delta);
                                }
                                if (baseFieldDeltas.containsKey("baseStaminaRegen")) {
                                    int delta = (int) Math.round(baseFieldDeltas.get("baseStaminaRegen"));
                                    if (delta != 0)
                                        creature.modifyBaseStaminaRegen(delta);
                                }
                                if (baseFieldDeltas.containsKey("baseManaRegen")) {
                                    int delta = (int) Math.round(baseFieldDeltas.get("baseManaRegen"));
                                    if (delta != 0)
                                        creature.modifyBaseManaRegen(delta);
                                }

                                // Copy size if changed — but only if JSON did NOT explicitly provide a size.
                                // JSON is intended to override species/type when present.
                                if (!jsonProvidedSize && beforeTemplate.getSize() != sizeBefore) {
                                    creature.setSize(beforeTemplate.getSize());
                                }

                                // Copy properties from species template (these will apply their own stat
                                // modifiers)
                                try {
                                    java.lang.reflect.Field pmField = Creature.class
                                            .getDeclaredField("propertyManager");
                                    pmField.setAccessible(true);
                                    PropertyManager templatePM = (PropertyManager) pmField.get(beforeTemplate);

                                    for (Property prop : templatePM.getBuffs().values()) {
                                        creature.addProperty(prop);
                                    }
                                    for (Property prop : templatePM.getDebuffs().values()) {
                                        creature.addProperty(prop);
                                    }
                                    for (Property prop : templatePM.getTraits().values()) {
                                        creature.addProperty(prop);
                                    }
                                } catch (Exception e) {
                                    // ignore if property copy fails
                                }
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
                            EnumMap<com.bapppis.core.creature.creatureEnums.Stats, Integer> statMap = null;
                            try {
                                java.lang.reflect.Field statsField = Creature.class.getDeclaredField("stats");
                                statsField.setAccessible(true);
                                Object statObj = statsField.get(creature);
                                if (statObj instanceof EnumMap) {
                                    @SuppressWarnings("unchecked")
                                    EnumMap<com.bapppis.core.creature.creatureEnums.Stats, Integer> tmp = (EnumMap<com.bapppis.core.creature.creatureEnums.Stats, Integer>) statObj;
                                    statMap = tmp;
                                }
                            } catch (Exception e) {
                                // Should not happen
                            }
                            if (statMap != null) {
                                for (com.bapppis.core.creature.creatureEnums.Stats stat : com.bapppis.core.creature.creatureEnums.Stats
                                        .values()) {
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
                            EnumMap<com.bapppis.core.Resistances, Integer> resistMap = null;
                            try {
                                java.lang.reflect.Field resistField = Creature.class.getDeclaredField("resistances");
                                resistField.setAccessible(true);
                                Object resistObj = resistField.get(creature);
                                if (resistObj instanceof EnumMap) {
                                    @SuppressWarnings("unchecked")
                                    EnumMap<com.bapppis.core.Resistances, Integer> tmp = (EnumMap<com.bapppis.core.Resistances, Integer>) resistObj;
                                    resistMap = tmp;
                                }
                            } catch (Exception e) {
                                // Should not happen
                            }
                            if (resistMap != null) {
                                for (com.bapppis.core.Resistances res : com.bapppis.core.Resistances.values()) {
                                    if (!resistMap.containsKey(res)) {
                                        if (res == com.bapppis.core.Resistances.TRUE) {
                                            creature.setResistance(res, 50);
                                        } else {
                                            creature.setResistance(res, 100);
                                        }
                                    }
                                }
                            }
                            // Ensure all ResBuildUp entries exist and default to 0 unless provided
                            try {
                                java.lang.reflect.Field rbuField = Creature.class.getDeclaredField("resBuildUp");
                                rbuField.setAccessible(true);
                                Object rbuObj = rbuField.get(creature);
                                if (rbuObj instanceof java.util.EnumMap) {
                                    @SuppressWarnings("unchecked")
                                    java.util.EnumMap<com.bapppis.core.ResBuildUp, Integer> rbuMap = (java.util.EnumMap<com.bapppis.core.ResBuildUp, Integer>) rbuObj;
                                    for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
                                        if (!rbuMap.containsKey(rb)) {
                                            rbuMap.put(rb, 0);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // ignore if reflection fails; creature will have defaults set in constructor
                            }
                            if (creature.getHpDice() == 0) {
                                creature.setHpDice(6);
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

                            // Remove properties requested in JSON (these remove properties
                            // that may have been added by creature type or species)
                            List<Integer> removeIds = getRemovePropertyIdsFromJson(resource.getPath(), gson);
                            if (removeIds != null) {
                                for (Integer rid : removeIds) {
                                    try {
                                        creature.removeProperty(rid);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            // Load starting inventory and equipment slots from JSON
                            applyStartingItemsFromJson(resource.getPath(), gson, creature);
                            
                            // Load spells from JSON
                            applySpellsFromJson(resource.getPath(), gson, creature);

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

    // copyCreatureFields removed: unused helper

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

    private static void applySpellsFromJson(String resourcePath, com.google.gson.Gson gson, Creature creature) {
        try (Reader reader = new InputStreamReader(
                CreatureLoader.class.getClassLoader().getResourceAsStream(resourcePath))) {
            com.google.gson.JsonObject obj = gson.fromJson(reader, com.google.gson.JsonObject.class);
            if (obj == null)
                return;
            // Spells array - can be objects with name+weight or simple strings/IDs
            if (obj.has("spells") && obj.get("spells").isJsonArray()) {
                for (com.google.gson.JsonElement el : obj.getAsJsonArray("spells")) {
                    try {
                        com.bapppis.core.spell.Spell spell = null;
                        int weight = 1; // Default weight
                        
                        if (el.isJsonObject()) {
                            // Object with name and weight: {"name": "Fireball", "weight": 3}
                            com.google.gson.JsonObject spellObj = el.getAsJsonObject();
                            String spellName = null;
                            
                            if (spellObj.has("name")) {
                                spellName = spellObj.get("name").getAsString();
                            } else if (spellObj.has("id")) {
                                int spellId = spellObj.get("id").getAsInt();
                                spell = com.bapppis.core.spell.SpellLoader.getSpellById(spellId);
                            }
                            
                            if (spellName != null) {
                                try {
                                    int spellId = Integer.parseInt(spellName);
                                    spell = com.bapppis.core.spell.SpellLoader.getSpellById(spellId);
                                } catch (NumberFormatException nfe) {
                                    spell = com.bapppis.core.spell.SpellLoader.getSpellByName(spellName);
                                }
                            }
                            
                            if (spellObj.has("weight")) {
                                weight = spellObj.get("weight").getAsInt();
                            }
                        } else if (el.isJsonPrimitive()) {
                            // Simple string or ID: "Fireball" or 50000
                            if (el.getAsJsonPrimitive().isNumber()) {
                                int spellId = el.getAsInt();
                                spell = com.bapppis.core.spell.SpellLoader.getSpellById(spellId);
                            } else if (el.getAsJsonPrimitive().isString()) {
                                String spellNameOrId = el.getAsString();
                                try {
                                    int spellId = Integer.parseInt(spellNameOrId);
                                    spell = com.bapppis.core.spell.SpellLoader.getSpellById(spellId);
                                } catch (NumberFormatException nfe) {
                                    spell = com.bapppis.core.spell.SpellLoader.getSpellByName(spellNameOrId);
                                }
                            }
                        }
                        
                        if (spell != null) {
                            creature.addSpell(spell);
                            // Store spell reference with weight
                            com.bapppis.core.spell.SpellReference ref = 
                                new com.bapppis.core.spell.SpellReference(spell.getName(), weight);
                            creature.getSpellReferences().add(ref);
                        }
                    } catch (Exception e) {
                        // ignore invalid spell entries
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

    // Helper to extract property IDs to remove from JSON ("removeProperties")
    private static List<Integer> getRemovePropertyIdsFromJson(String resourcePath, com.google.gson.Gson gson) {
        try (Reader reader = new InputStreamReader(
                CreatureLoader.class.getClassLoader().getResourceAsStream(resourcePath))) {
            com.google.gson.JsonObject obj = gson.fromJson(reader, com.google.gson.JsonObject.class);
            if (obj != null && obj.has("removeProperties")) {
                List<Integer> ids = new ArrayList<>();
                for (com.google.gson.JsonElement el : obj.getAsJsonArray("removeProperties")) {
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
                        // ignore invalid entries
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

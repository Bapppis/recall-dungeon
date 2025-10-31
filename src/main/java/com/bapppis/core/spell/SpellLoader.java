package com.bapppis.core.spell;

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
 * Loads spell data from JSON files in the resources/data/spells directory.
 */
public class SpellLoader {
    private static Map<Integer, Spell> spellIdMap = new HashMap<>();
    private static Map<String, Spell> spellNameMap = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Load all spell JSON files from resources/data/spells.
     */
    public static void loadSpells() {
        if (loaded)
            return;
        forceReload();
    }

    /**
     * Force reload of all spells, even if already loaded. Use in individual tests.
     */
    public static void forceReload() {
        spellIdMap.clear();
        spellNameMap.clear();
        loaded = false;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(com.bapppis.core.Resistances.class,
                        new com.bapppis.core.util.ResistancesDeserializer())
                .create();

        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("data/spells")
                .scan()) {
            java.util.Set<String> processedResourcePaths = new java.util.HashSet<>();
            for (Resource resource : scanResult.getAllResources()) {
                String relPath = resource.getPath();
                if (processedResourcePaths.contains(relPath))
                    continue;
                processedResourcePaths.add(relPath);
                if (relPath.endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        Spell spell = gson.fromJson(reader, Spell.class);
                        if (spell == null) {
                            System.err.println("Warning: failed to parse spell JSON from " + relPath);
                            continue;
                        }
                        if (spellIdMap.containsKey(spell.getId())) {
                            System.err.println("Warning: duplicate spell id " + spell.getId()
                                    + " in " + relPath + " (already loaded from another file)");
                        } else {
                            spellIdMap.put(spell.getId(), spell);
                        }
                        if (spell.getName() != null && !spell.getName().isBlank()) {
                            String normalizedName = spell.getName().toLowerCase().trim();
                            if (spellNameMap.containsKey(normalizedName)) {
                                System.err.println("Warning: duplicate spell name '" + spell.getName()
                                        + "' in " + relPath);
                            } else {
                                spellNameMap.put(normalizedName, spell);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading spell from " + relPath + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        loaded = true;
        System.out.println("Loaded " + spellIdMap.size() + " spells from JSON resources.");
    }

    /**
     * Get a spell by ID.
     */
    public static Spell getSpellById(int id) {
        if (!loaded) {
            loadSpells();
        }
        return spellIdMap.get(id);
    }

    /**
     * Get a spell by name (case-insensitive).
     */
    public static Spell getSpellByName(String name) {
        if (!loaded) {
            loadSpells();
        }
        if (name == null || name.isBlank()) {
            return null;
        }
        return spellNameMap.get(name.toLowerCase().trim());
    }

    /**
     * Get all loaded spells.
     */
    public static Map<Integer, Spell> getAllSpells() {
        if (!loaded) {
            loadSpells();
        }
        return new HashMap<>(spellIdMap);
    }
}

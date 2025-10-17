package com.bapppis.core.item;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ItemLoader {
    private static final HashMap<Integer, Item> itemIdMap = new HashMap<>();
    private static final HashMap<String, Item> itemNameMap = new HashMap<>();

    public static void loadItems() {
        itemIdMap.clear();
        itemNameMap.clear();

    com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
        .registerTypeAdapter(com.bapppis.core.Resistances.class,
            new com.bapppis.core.util.ResistancesDeserializer())
        .create();
    try (ScanResult scanResult = new ClassGraph()
        .acceptPaths("data/items") // Scan all item folders
        .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        Equipment item = gson.fromJson(reader, Equipment.class);
                        if (item != null) {
                            int id = item.getId();
                            String name = item.getName();
                            if (id > 0) {
                                itemIdMap.put(id, item);
                            }
                            if (name != null && !name.isEmpty()) {
                                String key = name.trim().toLowerCase();
                                String keyNoSpace = key.replaceAll("\\s+", "");
                                itemNameMap.put(key, item);
                                // register space-free variant if unique
                                if (!itemNameMap.containsKey(keyNoSpace)) {
                                    itemNameMap.put(keyNoSpace, item);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // System.out.println("Error loading item from: " + resource.getPath());
                        // e.printStackTrace();
                    }
                }
            }
        }
    }

    public static Item getItemById(int id) {
        return itemIdMap.get(id);
    }

    public static Item getItemByName(String name) {
        if (name == null) return null;
        String key = name.trim().toLowerCase();
        Item it = itemNameMap.get(key);
        if (it != null) return it;
        String keyNoSpace = key.replaceAll("\\s+", "");
        return itemNameMap.get(keyNoSpace);
    }

    public static List<Item> getAllItems() {
        return new ArrayList<>(itemIdMap.values());
    }

    public static java.util.List<String> getAllItemNames() {
        java.util.List<String> names = new java.util.ArrayList<>(itemNameMap.keySet());
        java.util.Collections.sort(names);
        return names;
    }
}

package com.bapppis.core.item;

import com.google.gson.Gson;
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

        Gson gson = new Gson();
        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("assets/items") // Scan all item folders
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
                                itemNameMap.put(name, item);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading item from: " + resource.getPath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static Item getItemById(int id) {
        return itemIdMap.get(id);
    }

    public static Item getItemByName(String name) {
        return itemNameMap.get(name);
    }

    public static List<Item> getAllItems() {
        return new ArrayList<>(itemIdMap.values());
    }
}

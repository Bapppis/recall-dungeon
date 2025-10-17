package com.bapppis.core.loot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static registry for loot pools so tests and code can access pools by id or name
 * similar to ItemLoader.getItemById/getItemByName.
 */
public class LootPoolRegistry {
    private static final Map<String, LootPool> poolById = new HashMap<>();
    private static final Map<String, LootPool> poolByName = new HashMap<>();

    public static void loadPools() {
        poolById.clear();
        poolByName.clear();
        try {
            List<LootPool> list = LootPoolLoader.loadPoolsFromResources("loot_pools");
            for (LootPool p : list) {
                if (p == null) continue;
                if (p.id != null) poolById.put(p.id, p);
                if (p.name != null) {
                    String key = p.name.toLowerCase();
                    String keyNoSpace = key.replaceAll("\\s+", "");
                    poolByName.put(key, p);
                    if (!poolByName.containsKey(keyNoSpace)) {
                        poolByName.put(keyNoSpace, p);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static LootPool getPoolById(String id) {
        return id == null ? null : poolById.get(id);
    }

    public static LootPool getPoolByName(String name) {
        if (name == null) return null;
        String key = name.trim().toLowerCase();
        LootPool p = poolByName.get(key);
        if (p != null) return p;
        // Try space-free variant
        String keyNoSpace = key.replaceAll("\\s+", "");
        return poolByName.get(keyNoSpace);
    }

    public static List<LootPool> getAllPools() {
        return List.copyOf(poolById.values());
    }
}

package com.bapppis.core.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;

public class LootManager {
    private final Map<String, LootPool> pools = new HashMap<>();
    private final Random rng = new Random();

    public void registerPool(LootPool pool) {
        if (pool == null) return;
        if (pool.id != null) pools.put(pool.id, pool);
        if (pool.name != null && !pool.name.isEmpty()) {
            pools.put(pool.name.toLowerCase(), pool);
        }
    }

    public void registerAll(List<LootPool> list) {
        for (LootPool p : list) registerPool(p);
    }

    public void loadDefaults() {
        registerAll(LootPoolLoader.loadPoolsFromResources("loot_pools"));
        // monster pools may also be loaded into the same manager
        registerAll(LootPoolLoader.loadPoolsFromResources("monster_pools"));
    }

    public List<Spawn> samplePool(String poolId) {
        LootPool pool = pools.get(poolId);
        List<Spawn> out = new ArrayList<>();
        if (pool == null) return out;

        // Simple strategy: pick one entry according to weights, respect count ranges
        int total = 0;
        for (LootPool.Entry e : pool.entries) total += effectiveWeight(e);
        if (total <= 0) return out;

        int pick = rng.nextInt(total);
        LootPool.Entry chosen = null;
        for (LootPool.Entry e : pool.entries) {
            pick -= effectiveWeight(e);
            if (pick < 0) { chosen = e; break; }
        }
        if (chosen == null) return out;

        int min = chosen.countMin != null ? chosen.countMin : 1;
        int max = chosen.countMax != null ? chosen.countMax : min;
        int count = min + (max > min ? rng.nextInt(max - min + 1) : 0);

        if ("item".equalsIgnoreCase(chosen.type)) {
            // Allow entries to reference an item by id or by name (fallback to name)
            String ref = chosen.id != null ? chosen.id : chosen.name;
            for (int i=0;i<count;i++) out.add(new Spawn("item", ref));
        } else if ("monster".equalsIgnoreCase(chosen.type)) {
            int groupMin = chosen.minGroup != null ? chosen.minGroup : 1;
            int groupMax = chosen.maxGroup != null ? chosen.maxGroup : groupMin;
            int group = groupMin + (groupMax > groupMin ? rng.nextInt(groupMax - groupMin + 1) : 0);
            for (int i=0;i<group;i++) out.add(new Spawn("monster", chosen.id));
        } else if ("pool".equalsIgnoreCase(chosen.type)) {
            // nested pool: sample referenced pool recursively
            // Allow pool reference by id or by name (we register pool names lowercased)
            String poolRef = chosen.id != null ? chosen.id : (chosen.name != null ? chosen.name.toLowerCase() : null);
            if (poolRef != null) out.addAll(samplePool(poolRef));
        }

        return out;
    }

    private int effectiveWeight(LootPool.Entry e) {
        if (e == null) return 0;
        if (e.weight != null) return Math.max(0, e.weight);
        return 1; // default minimal weight
    }

    public static class Spawn {
        public final String type;
        public final String id;
        public Spawn(String type, String id) { this.type = type; this.id = id; }
    }

    /**
     * Resolve a spawn reference (string) to an Item if possible.
     * Tries to parse numeric id first, then falls back to name lookup.
     */
    private Item resolveSpawnToItem(String ref) {
        if (ref == null) return null;
        // try numeric id
        try {
            int iid = Integer.parseInt(ref);
            return ItemLoader.getItemById(iid);
        } catch (NumberFormatException ex) {
            // not numeric — try by name
            return ItemLoader.getItemByName(ref);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Sample a pool and return resolved Items (skips unresolved spawns).
     */
    public List<Item> sampleItemsFromPool(String poolId) {
        List<Item> out = new ArrayList<>();
        List<Spawn> spawns = samplePool(poolId);
        for (Spawn s : spawns) {
            if (!"item".equalsIgnoreCase(s.type)) continue;
            Item it = resolveSpawnToItem(s.id);
            if (it != null) out.add(it);
        }
        return out;
    }
}

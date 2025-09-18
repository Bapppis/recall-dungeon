package com.bapppis.core.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LootManager {
    private final Map<String, LootPool> pools = new HashMap<>();
    private final Random rng = new Random();

    public void registerPool(LootPool pool) {
        if (pool != null && pool.id != null) pools.put(pool.id, pool);
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
            for (int i=0;i<count;i++) out.add(new Spawn("item", chosen.id));
        } else if ("monster".equalsIgnoreCase(chosen.type)) {
            int groupMin = chosen.minGroup != null ? chosen.minGroup : 1;
            int groupMax = chosen.maxGroup != null ? chosen.maxGroup : groupMin;
            int group = groupMin + (groupMax > groupMin ? rng.nextInt(groupMax - groupMin + 1) : 0);
            for (int i=0;i<group;i++) out.add(new Spawn("monster", chosen.id));
        } else if ("pool".equalsIgnoreCase(chosen.type)) {
            // nested pool: sample referenced pool recursively
            out.addAll(samplePool(chosen.id));
        }

        return out;
    }

    private int effectiveWeight(LootPool.Entry e) {
        if (e == null) return 0;
        if (e.weight != null) return Math.max(0, e.weight);
        if (Boolean.TRUE.equals(e.useItemRarity)) {
            // Simple mapping: common=100, uncommon=30, rare=7, legendary=1
            // Here we don't have access to Item objects; in the real game, lookup item rarity and map it.
            // Fallback weight:
            return 10;
        }
        return 1; // default minimal weight
    }

    public static class Spawn {
        public final String type;
        public final String id;
        public Spawn(String type, String id) { this.type = type; this.id = id; }
    }
}

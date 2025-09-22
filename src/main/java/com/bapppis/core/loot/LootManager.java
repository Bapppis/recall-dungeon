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
        // New strategy supporting guaranteed entries and independent chance entries.
        // 1) Process guaranteed entries first (always included unless chance is present and fails)
        for (LootPool.Entry e : pool.entries) {
            if (Boolean.TRUE.equals(e.guaranteed)) {
                // include this entry
                handleEntrySpawn(out, e);
            }
        }

        // 2) Independently evaluate entries that have a 'chance' (0.0 - 1.0)
        for (LootPool.Entry e : pool.entries) {
            if (e.chance != null && e.chance > 0.0 && e.chance <= 1.0) {
                if (rng.nextDouble() < e.chance) {
                    handleEntrySpawn(out, e);
                }
            }
        }

        // 3) Do a single weighted pick among non-guaranteed entries that do not have chance set
        List<LootPool.Entry> weightedCandidates = new ArrayList<>();
        for (LootPool.Entry e : pool.entries) {
            if (Boolean.TRUE.equals(e.guaranteed)) continue;
            if (e.chance != null && e.chance > 0.0 && e.chance <= 1.0) continue;
            weightedCandidates.add(e);
        }

        int total = 0;
        for (LootPool.Entry e : weightedCandidates) total += effectiveWeight(e);
        if (total <= 0) return out;

        int pick = rng.nextInt(total);
        LootPool.Entry chosen = null;
        for (LootPool.Entry e : weightedCandidates) {
            pick -= effectiveWeight(e);
            if (pick < 0) { chosen = e; break; }
        }
        if (chosen == null) return out;

        handleEntrySpawn(out, chosen);

        return out;
    }

    // Helper to append spawn(s) for an entry to the output list
    private void handleEntrySpawn(List<Spawn> out, LootPool.Entry chosen) {
        int min = chosen.countMin != null ? chosen.countMin : 1;
        int max = chosen.countMax != null ? chosen.countMax : min;
        int count = min + (max > min ? rng.nextInt(max - min + 1) : 0);

        if ("item".equalsIgnoreCase(chosen.type)) {
            String ref = chosen.id != null ? chosen.id : chosen.name;
            for (int i=0;i<count;i++) out.add(new Spawn("item", ref));
        } else if ("monster".equalsIgnoreCase(chosen.type)) {
            int groupMin = chosen.minGroup != null ? chosen.minGroup : 1;
            int groupMax = chosen.maxGroup != null ? chosen.maxGroup : groupMin;
            int group = groupMin + (groupMax > groupMin ? rng.nextInt(groupMax - groupMin + 1) : 0);
            for (int i=0;i<group;i++) out.add(new Spawn("monster", chosen.id));
        } else if ("pool".equalsIgnoreCase(chosen.type)) {
            String poolRef = chosen.id != null ? chosen.id : (chosen.name != null ? chosen.name.toLowerCase() : null);
            if (poolRef != null) out.addAll(samplePool(poolRef));
        }
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
}

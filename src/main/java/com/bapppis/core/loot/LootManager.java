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
        if (pool.id != null) {
            System.out.println("[DEBUG] Registering pool by id: " + pool.id + " (" + pool.name + ")");
            pools.put(pool.id, pool);
        }
        if (pool.name != null && !pool.name.isEmpty()) {
            String lowerName = pool.name.toLowerCase();
            System.out.println("[DEBUG] Registering pool by name: " + lowerName + " (original: " + pool.name + ")");
            pools.put(lowerName, pool);
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
        if (pool == null) {
            System.out.println("[DEBUG] samplePool: pool '" + poolId + "' not found");
            return out;
        }
        System.out.println("[DEBUG] samplePool: found pool '" + pool.name + "' with " + pool.entries.size() + " entries");
        // New strategy supporting guaranteed entries and independent chance entries.
        // 1) Process guaranteed entries first (always included unless chance is present and fails)
        for (LootPool.Entry e : pool.entries) {
            if (Boolean.TRUE.equals(e.guaranteed)) {
                // include this entry
                System.out.println("[DEBUG] Processing guaranteed entry: type=" + e.type + ", id=" + e.id);
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

    /**
     * Convenience: sample a pool by human name (case-insensitive). Returns an
     * empty list if poolName is unknown.
     */
    public List<Spawn> samplePoolByName(String poolName) {
        if (poolName == null) return new ArrayList<>();
        return samplePool(poolName.toLowerCase());
    }

    // Helper to append spawn(s) for an entry to the output list
    private void handleEntrySpawn(List<Spawn> out, LootPool.Entry chosen) {
        int min = chosen.countMin != null ? chosen.countMin : 1;
        int max = chosen.countMax != null ? chosen.countMax : min;
        int count = min + (max > min ? rng.nextInt(max - min + 1) : 0);

        if ("item".equalsIgnoreCase(chosen.type)) {
            // Try to resolve by id, then by name
            String ref = null;
            if (chosen.id != null) {
                ref = chosen.id;
                System.out.println("[DEBUG] handleEntrySpawn: item entry, id=" + ref);
                // Try to resolve as int id, fallback to name
                try {
                    int itemId = Integer.parseInt(ref);
                    if (com.bapppis.core.item.ItemLoader.getItemById(itemId) == null && chosen.name != null) {
                        System.out.println("[DEBUG] Item not found by id, trying name: " + chosen.name);
                        ref = chosen.name;
                    }
                } catch (NumberFormatException nfe) {
                    System.out.println("[DEBUG] Item id not numeric, trying as name: " + ref);
                    // Not an int, try as name
                    if (com.bapppis.core.item.ItemLoader.getItemByName(ref) == null && chosen.name != null) {
                        System.out.println("[DEBUG] Item not found by id, trying name: " + chosen.name);
                        ref = chosen.name;
                    }
                }
            } else if (chosen.name != null) {
                ref = chosen.name;
                System.out.println("[DEBUG] handleEntrySpawn: item entry, name=" + ref);
            }
            System.out.println("[DEBUG] Adding " + count + " x '" + ref + "' to spawns");
            for (int i=0;i<count;i++) out.add(new Spawn("item", ref));
        } else if ("monster".equalsIgnoreCase(chosen.type)) {
            // Try to resolve by id, then by name
            String ref = null;
            if (chosen.id != null) {
                ref = chosen.id;
                try {
                    int creatureId = Integer.parseInt(ref);
                    if (com.bapppis.core.creature.CreatureLoader.getCreatureById(creatureId) == null && chosen.name != null) {
                        ref = chosen.name;
                    }
                } catch (NumberFormatException nfe) {
                    if (com.bapppis.core.creature.CreatureLoader.getCreature(ref) == null && chosen.name != null) {
                        ref = chosen.name;
                    }
                }
            } else if (chosen.name != null) {
                ref = chosen.name;
            }
            int groupMin = chosen.minGroup != null ? chosen.minGroup : 1;
            int groupMax = chosen.maxGroup != null ? chosen.maxGroup : groupMin;
            int group = groupMin + (groupMax > groupMin ? rng.nextInt(groupMax - groupMin + 1) : 0);
            for (int i=0;i<group;i++) out.add(new Spawn("monster", ref));
        } else if ("pool".equalsIgnoreCase(chosen.type)) {
            // Try to resolve pool by id, then by name
            String poolRef = null;
            if (chosen.id != null) {
                poolRef = chosen.id;
                // Check if pool exists with this id
                System.out.println("[DEBUG] Looking for nested pool with id: " + poolRef);
                if (pools.get(poolRef) == null && chosen.name != null) {
                    // Try name as fallback
                    poolRef = chosen.name.toLowerCase();
                    System.out.println("[DEBUG] Pool not found by id, trying name: " + poolRef);
                }
            } else if (chosen.name != null) {
                poolRef = chosen.name.toLowerCase();
                System.out.println("[DEBUG] Using pool name: " + poolRef);
            }
            if (poolRef != null) {
                System.out.println("[DEBUG] Sampling nested pool: " + poolRef);
                List<Spawn> poolSpawns = samplePool(poolRef);
                System.out.println("[DEBUG] Nested pool returned " + poolSpawns.size() + " spawns");
                out.addAll(poolSpawns);
            }
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

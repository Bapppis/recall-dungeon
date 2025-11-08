package com.bapppis.core.loot;

import java.util.List;

public class LootPool {
    // id can be numeric-ish (e.g. "10000") or a string key. Keep as String for flexibility.
    public String id;
    // human-friendly name for the pool (optional). Allows lookup by name as well as id.
    public String name;
    // sprite name for visual representation (e.g., "common_treasure_chest")
    public String sprite;
    public List<Entry> entries;

    public static class Entry {
        public String type; // item | monster | pool
        public String id;   // referenced id
        public String name; // optional: referenced item name (if you prefer name lookup)
        public Integer weight; // optional explicit weight
        public Boolean guaranteed; // if true, always include this entry when sampling the pool
        public Double chance; // optional independent chance (0.0 - 1.0) to include this entry
        public Integer countMin;
        public Integer countMax;
        // monster-specific
        public Integer minGroup;
        public Integer maxGroup;
    }
}

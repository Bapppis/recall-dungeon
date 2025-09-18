package com.bapppis.core.loot;

import java.util.List;

public class LootPool {
    public String id;
    public List<Entry> entries;

    public static class Entry {
        public String type; // item | monster | pool
        public String id;   // referenced id
        public Integer weight; // optional explicit weight
        public Boolean useItemRarity; // optional: derive weight from item rarity
        public Integer countMin;
        public Integer countMax;
        // monster-specific
        public Integer minGroup;
        public Integer maxGroup;
    }
}

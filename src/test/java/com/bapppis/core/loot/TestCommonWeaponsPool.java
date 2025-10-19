package com.bapppis.core.loot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.item.ItemLoader;

public class TestCommonWeaponsPool {

    @Test
    public void testCommonWeaponsPoolGeneratesExpectedItems() {
        // Load items and pools
        AllLoaders.loadAll();
        LootManager manager = new LootManager();
        manager.loadDefaults();

        // Allowed refs in Common Weapons pool (from Common Weapons.json)
        Set<String> allowed = new HashSet<>();
        allowed.add("37002"); // Parrying Dagger
        allowed.add("37001"); // Rusty Iron Sword
        allowed.add("35000"); // Old Bow
        allowed.add("Parrying Dagger");
        allowed.add("Rusty Iron Sword");
        allowed.add("Old Bow");
        allowed.add("ParryingDagger"); // ID in loot pool
        allowed.add("RustyIronSword"); // ID in loot pool
        allowed.add("OldBow"); // ID in loot pool

        // Sample the pool multiple times to catch randomness
        for (int i = 0; i < 200; i++) {
            List<LootManager.Spawn> spawns = manager.samplePool("40000");
            // should not be empty and should only contain allowed references
            assertTrue(spawns.size() >= 1, "Expected at least one spawn");
            for (LootManager.Spawn s : spawns) {
                // System.out.println("Spawned: " + s.type + " -> " + s.id);
                assertTrue(allowed.contains(s.id), "Spawn produced unexpected id: " + s.id);
            }
        }
    }
}

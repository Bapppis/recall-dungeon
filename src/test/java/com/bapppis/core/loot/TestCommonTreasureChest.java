package com.bapppis.core.loot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;

public class TestCommonTreasureChest {

    @Test
    public void testChestBehavior() {
        AllLoaders.loadAll();
        LootManager manager = new LootManager();
        manager.loadDefaults();

        int iterations = 1000;
        AtomicInteger potions = new AtomicInteger(0);
        AtomicInteger twoPotions = new AtomicInteger(0);
        AtomicInteger weapons = new AtomicInteger(0);

        for (int i = 0; i < iterations; i++) {
            List<LootManager.Spawn> spawns = manager.samplePool("10002");
            boolean seenPotion = false;
            int potionCount = 0;
            boolean seenWeapon = false;
            for (LootManager.Spawn s : spawns) {
                // Try to resolve spawn into an Item by id or name
                com.bapppis.core.item.Item resolved = null;
                try {
                    int iid = Integer.parseInt(s.id);
                    resolved = com.bapppis.core.item.ItemLoader.getItemById(iid);
                } catch (Exception ex) {
                    resolved = com.bapppis.core.item.ItemLoader.getItemByName(s.id);
                }
                if (resolved != null) {
                    if (resolved.getType() == com.bapppis.core.item.ItemType.CONSUMABLE) {
                        seenPotion = true;
                        potionCount++;
                    }
                    if (resolved.getType() == com.bapppis.core.item.ItemType.WEAPON) {
                        seenWeapon = true;
                    }
                }
            }
            if (seenPotion)
                potions.incrementAndGet();
            if (potionCount >= 2)
                twoPotions.incrementAndGet();
            if (seenWeapon)
                weapons.incrementAndGet();
        }

    // System.out.println("Potions seen: " + potions.get() + " / " + iterations);
    // System.out.println("Two potions seen: " + twoPotions.get() + " / " + iterations);
    // System.out.println("Weapons seen: " + weapons.get() + " / " + iterations);

        // Basic sanity checks
        assertTrue(potions.get() >= iterations * 0.7, "Most chests should have at least one potion (guaranteed)");
    }
}

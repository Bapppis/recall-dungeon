package com.bapppis.core.monster;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import com.bapppis.core.AllLoaders;
import com.bapppis.core.loot.LootManager;

public class TestFloor0Enemies {

    @Test
    public void testFloor0Distribution() {
        // Ensure creatures are loaded so ids/names resolve if needed
        AllLoaders.loadAll();

        LootManager manager = new LootManager();
        manager.loadDefaults();

        int iterations = 1000;
        AtomicInteger spearman = new AtomicInteger(0);
        AtomicInteger swordsman = new AtomicInteger(0);
        AtomicInteger hound = new AtomicInteger(0);
        AtomicInteger goblin = new AtomicInteger(0);

        for (int i = 0; i < iterations; i++) {
            List<LootManager.Spawn> spawns = manager.samplePool("41000");
            for (LootManager.Spawn s : spawns) {
                // Check string IDs from monster pool
                if ("SkeletonSpearman".equals(s.id) || "Skeleton Spearman".equals(s.id)) {
                    spearman.incrementAndGet();
                } else if ("SkeletonSwordsman".equals(s.id) || "Skeleton Swordsman".equals(s.id)) {
                    swordsman.incrementAndGet();
                } else if ("GoblinBerserker".equals(s.id) || "Goblin Berserker".equals(s.id)) {
                    goblin.incrementAndGet();
                } else if ("DarkHound".equals(s.id) || "Dark Hound".equals(s.id)) {
                    hound.incrementAndGet();
                } else {
                    // Try to parse numeric id for backward compatibility
                    try {
                        int mid = Integer.parseInt(s.id);
                        if (mid == 19000)
                            spearman.incrementAndGet();
                        else if (mid == 19001)
                            swordsman.incrementAndGet();
                        else if (mid == 15000)
                            goblin.incrementAndGet();
                        else if (mid == 7000)
                            hound.incrementAndGet();
                    } catch (Exception ex) {
                        // ignore unrecognized ids
                    }
                }
            }
        }

    // System.out.println("Spearman: " + spearman.get());
    // System.out.println("Swordsman: " + swordsman.get());
    // System.out.println("Goblin: " + goblin.get());
    // System.out.println("Hound: " + hound.get());

        int skeletons = spearman.get() + swordsman.get();
    int total = skeletons + goblin.get() + hound.get();
    // System.out.println("Total observed: " + total);
    assertTrue(total == iterations, "Total observed should equal iterations when exactly one spawn per sample is added");
        // Expected rough ratios: skeletons ~ 20/36 (~55%), goblin ~5/36 (~14%), hound
        // ~1/36 (~3%)
        assertTrue(skeletons >= iterations * 0.45, "Skeletons should be the majority");
        assertTrue(goblin.get() >= iterations * 0.10, "Goblin should appear around ~14% of the time");
        assertTrue(hound.get() >= iterations * 0.02, "Dark Hound should appear occasionally (~3%)");
    }
}

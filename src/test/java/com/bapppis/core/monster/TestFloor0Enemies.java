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
            List<LootManager.Spawn> spawns = manager.samplePool("11000");
            for (LootManager.Spawn s : spawns) {
                // try to parse numeric id
                try {
                    int mid = Integer.parseInt(s.id);
                    if (mid == 6600)
                        spearman.incrementAndGet();
                    else if (mid == 6601)
                        swordsman.incrementAndGet();
                    else if (mid == 6400)
                        goblin.incrementAndGet();
                    else if (mid == 6000)
                        hound.incrementAndGet();
                } catch (Exception ex) {
                    // ignore non-numeric ids
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

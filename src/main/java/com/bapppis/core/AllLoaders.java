package com.bapppis.core;

import com.bapppis.core.property.PropertyLoader;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.loot.LootPoolLoader;
import com.bapppis.core.creature.CreatureLoader;

/**
 * Convenience helper to run all asset loaders in the correct order.
 * Call AllLoaders.loadAll() at game startup or at beginning of tests.
 */
public class AllLoaders {

    /**
     * Load properties, items, loot pools and creatures.
     * Order matters: properties and items should be loaded before creatures.
     */
    public static void loadAll() {
        // Synchronized loading to avoid races across tests/threads.
        synchronized (AllLoaders.class) {
            if (LoadedState.loaded) {
                return;
            }

            try {
                PropertyLoader.loadProperties();
            } catch (Exception e) {
                System.err.println("Warning: PropertyLoader.loadProperties() failed");
                e.printStackTrace();
            }

            try {
                ItemLoader.loadItems();
            } catch (Exception e) {
                System.err.println("Warning: ItemLoader.loadItems() failed");
                e.printStackTrace();
            }

            try {
                LootPoolLoader.loadPoolsFromResources("loot_pools");
            } catch (Exception e) {
                System.err.println("Warning: LootPoolLoader.loadPoolsFromResources() failed");
                e.printStackTrace();
            }

            try {
                CreatureLoader.loadCreatures();
            } catch (Exception e) {
                System.err.println("Warning: CreatureLoader.loadCreatures() failed");
                e.printStackTrace();
            }

            // Mark loaded so future calls are fast
            LoadedState.loaded = true;
        }
    }
}

class LoadedState {
    // volatile to ensure visibility across threads
    static volatile boolean loaded = false;
}

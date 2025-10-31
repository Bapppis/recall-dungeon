package com.bapppis.core;

import com.bapppis.core.property.PropertyLoader;
import com.bapppis.core.spell.SpellLoader;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.loot.LootPoolLoader;
import com.bapppis.core.creature.CreatureLoader;

public class AllLoaders {

    public static void loadAll() {
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
                SpellLoader.loadSpells();
            } catch (Exception e) {
                System.err.println("Warning: SpellLoader.loadSpells() failed");
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

            LoadedState.loaded = true;
        }
    }
}

class LoadedState {
    static volatile boolean loaded = false;
}

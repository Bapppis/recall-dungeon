package com.bapppis.core;

import com.bapppis.core.property.PropertyLoader;
import com.bapppis.core.spell.SpellLoader;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.loot.LootPoolLoader;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.playerClass.PlayerClassLoader;
import com.bapppis.core.creature.playerClass.TalentTreeLoader;

public class AllLoaders {

    private static PlayerClassLoader playerClassLoader;
    private static TalentTreeLoader talentTreeLoader;

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
                playerClassLoader = new PlayerClassLoader();
                playerClassLoader.loadAllPlayerClasses();
            } catch (Exception e) {
                System.err.println("Warning: PlayerClassLoader.loadAllPlayerClasses() failed");
                e.printStackTrace();
            }

            try {
                talentTreeLoader = new TalentTreeLoader();
            } catch (Exception e) {
                System.err.println("Warning: TalentTreeLoader initialization failed");
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

    public static PlayerClassLoader getPlayerClassLoader() {
        return playerClassLoader;
    }

    public static TalentTreeLoader getTalentTreeLoader() {
        return talentTreeLoader;
    }
}

class LoadedState {
    static volatile boolean loaded = false;
}

package com.bapppis.core.Creature.player;

import org.junit.jupiter.api.Test;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.item.ItemLoader;

public class TestBigglesCreation {
    @Test
    public void testBigglesCreation() {
        // Use AllLoaders to initialize all asset loaders
        AllLoaders.loadAll();

        Player biggles = CreatureLoader.getPlayerById(5000);
        /* biggles.removeProperty("Darksight");

        biggles.equipItem(ItemLoader.getItemByName("TestHelmet"));
        System.out.println(biggles.getTraits()); */
        biggles.setMaxHp(50);
        biggles.equipItem(ItemLoader.getItemByName("TestHelmet"));
        System.out.println(biggles.getDebuffs());
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.tickProperties();
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        System.out.println(biggles.getDebuffs());
        biggles.tickProperties();
        biggles.tickProperties();
        biggles.tickProperties();
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.unequipItemByName("TestHelmet");
        System.out.println(biggles.getAllEquipped());
        System.out.println(biggles.getDebuffs());
    }
}

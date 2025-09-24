package com.bapppis.core.Creature.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.Creature.Stats;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.property.PropertyManager;

public class TestBigglesCreation {
    // Test Biggles creation logic here
    @Test
    public void testBigglesCreation() {
    PropertyManager.loadProperties();
    ItemLoader.loadItems();
    CreatureLoader.loadCreatures();
    // Make the player Biggles
    Player biggles = CreatureLoader.getPlayerById(5000);
    org.junit.jupiter.api.Assertions.assertNotNull(biggles, "Biggles should be loaded as a Player");

        //assertBigglesDefaults(biggles);
        System.out.println(biggles.toString());

        /* biggles.removeProperty(4000);
        biggles.addProperty(PropertyManager.getProperty(2000)); // Add a debuff
        assertBigglesDebuffed(biggles);
        System.out.println(biggles.toString()); */

        //biggles.addXp(42);
        //biggles.setStat(Stats.CONSTITUTION, 7);
        biggles.equipItem(ItemLoader.getItemById(9800)); // Equip the weapon
        System.out.println(biggles.toString());
    }

    // make an assert function for Biggles
    private void assertBigglesDefaults(Player biggles) {
        assertEquals("Biggles The Unlucky", biggles.getName());
        assertEquals(5, biggles.getMaxHp());
        assertEquals(5, biggles.getCurrentHp());
        assertEquals(com.bapppis.core.creature.Creature.Size.SMALL, biggles.getSize());
        assertEquals(com.bapppis.core.creature.Creature.Type.PLAYER, biggles.getType());
        assertEquals(com.bapppis.core.creature.Creature.CreatureType.HUMANOID, biggles.getCreatureType());
        assertEquals(7, biggles.getStat(com.bapppis.core.creature.Creature.Stats.INTELLIGENCE));
        assertEquals(2, biggles.getStat(com.bapppis.core.creature.Creature.Stats.LUCK));
    }

    private void assertBigglesDebuffed(Player biggles) {
        assertEquals("Biggles The Unlucky", biggles.getName());
        assertEquals(5, biggles.getMaxHp());
        assertEquals(5, biggles.getCurrentHp());
        assertEquals(com.bapppis.core.creature.Creature.Size.SMALL, biggles.getSize());
        assertEquals(com.bapppis.core.creature.Creature.Type.PLAYER, biggles.getType());
        assertEquals(com.bapppis.core.creature.Creature.CreatureType.HUMANOID, biggles.getCreatureType());
        assertEquals(6, biggles.getStat(com.bapppis.core.creature.Creature.Stats.INTELLIGENCE));
        assertEquals(1, biggles.getStat(com.bapppis.core.creature.Creature.Stats.LUCK));
    }
}

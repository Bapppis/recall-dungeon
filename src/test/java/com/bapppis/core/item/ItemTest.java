package com.bapppis.core.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.property.PropertyManager;

public class ItemTest {
    // Test item functionality here
    @Test
    public void testItemFunctionality() {
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();
        // Make the test player Biggles
        Player biggles = (Player) CreatureLoader.getCreatureById(5000);
        assert biggles != null;
        //assertCaptainVossDefaults(captainVoss);
        //biggles.equipItem(ItemLoader.getItemById(7000)); // Equip Armor of Water
        biggles.addItem(ItemLoader.getItemById(7000)); // Add Armor of water to inventory
        biggles.addItem(ItemLoader.getItemById(7001)); // Add Armor of bones to inventory
        biggles.addItem(ItemLoader.getItemById(7250)); // Add Crusader Helmet to inventory
        biggles.addItem(ItemLoader.getItemById(7500)); // Add Legs of Speed to inventory
        biggles.addItem(ItemLoader.getItemById(7750)); // Add Tower Shield to inventory
        biggles.addItem(ItemLoader.getItemById(9800)); // Add Falchion of Doom to inventory
        System.out.println(biggles.toString());
        biggles.equipItem(biggles.getInventory().getArmors().get(1)); // Equip Armor of bones
        biggles.equipItem(biggles.getInventory().getHelmets().get(0)); // Equip Crusader Helmet
        biggles.equipItem(biggles.getInventory().getLegwear().get(0)); // Equip Legs of Speed
        biggles.equipItem(biggles.getInventory().getOffhands().get(0)); // Equip Tower Shield
        biggles.equipItem(biggles.getInventory().getWeapons().get(0)); // Equip Falchion of Doom and it unequips the shield
        System.out.println(biggles.toString());
        /* captainVoss.removeProperty(4001);
        biggles.addProperty(PropertyManager.getProperty(2000)); // Add a debuff
        assertBigglesDebuffed(biggles);
        System.out.println(biggles.toString()); */

    }

    // make an assert function for captain voss
    /* private void assertCaptainVossDefaults(Player captainVoss) {
        // Implement assertions for Captain Voss defaults
        assertEquals("Captain Aldric Voss", captainVoss.getName());
        assertEquals(30, captainVoss.getMaxHp());
        assertEquals(30, captainVoss.getCurrentHp());
        assertEquals(com.bapppis.core.creature.Creature.Size.MEDIUM, biggles.getSize());
        assertEquals(com.bapppis.core.creature.Creature.Type.PLAYER, biggles.getType());
        assertEquals(com.bapppis.core.creature.Creature.CreatureType.HUMANOID, biggles.getCreatureType());
        assertEquals(14, biggles.getStat(com.bapppis.core.creature.Creature.Stats.STRENGTH));
        assertEquals(11, biggles.getStat(com.bapppis.core.creature.Creature.Stats.DEXTERITY));
        assertEquals(13, biggles.getStat(com.bapppis.core.creature.Creature.Stats.CONSTITUTION));
        assertEquals(11, biggles.getStat(com.bapppis.core.creature.Creature.Stats.INTELLIGENCE));
        assertEquals(11, biggles.getStat(com.bapppis.core.creature.Creature.Stats.WISDOM));
        assertEquals(11, biggles.getStat(com.bapppis.core.creature.Creature.Stats.CHARISMA));
        assertEquals(1, biggles.getStat(com.bapppis.core.creature.Creature.Stats.LUCK));
    } */
}

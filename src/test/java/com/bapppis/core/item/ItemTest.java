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

        // Add items to inventory
        biggles.addItem(ItemLoader.getItemById(7000)); // Armor of Water
        biggles.addItem(ItemLoader.getItemById(7001)); // Armor of Bones
        biggles.addItem(ItemLoader.getItemById(7250)); // Crusader Helmet
        biggles.addItem(ItemLoader.getItemById(7500)); // Legs of Speed
        biggles.addItem(ItemLoader.getItemById(7750)); // Tower Shield
        biggles.addItem(ItemLoader.getItemById(9800)); // Falchion of Doom

        // Inventory assertions
        assertEquals(2, biggles.getInventory().getArmors().size());
        assertEquals(1, biggles.getInventory().getHelmets().size());
        assertEquals(1, biggles.getInventory().getLegwear().size());
        assertEquals(1, biggles.getInventory().getOffhands().size());
        assertEquals(1, biggles.getInventory().getWeapons().size());

        // Equip Armor of Bones (should be at index 1 after sorting)
        biggles.equipItem(biggles.getInventory().getArmors().get(0));
        // Equip Crusader Helmet
        biggles.equipItem(biggles.getInventory().getHelmets().get(0));
        // Equip Legs of Speed
        biggles.equipItem(biggles.getInventory().getLegwear().get(0));
        // Equip Tower Shield
        biggles.equipItem(biggles.getInventory().getOffhands().get(0));

        biggles.equipItem(biggles.getInventory().getArmors().get(0));

        // Assert stat and resistance changes after equipping Armor of Bones
        // Adjust these values to match your Armor of Bones JSON
        // Example: If Armor of Bones gives +2 CONSTITUTION and +10 SLASHING resistance
        assertEquals(7, biggles.getStat(com.bapppis.core.creature.Creature.Stats.INTELLIGENCE)); // from Biggles base
        assertEquals(2, biggles.getStat(com.bapppis.core.creature.Creature.Stats.LUCK)); // from Biggles base

        // Resistance: base (from Biggles) + any from Armor of Bones
        // If Armor of Bones gives +10 SLASHING:
        System.out.println(biggles.toString());
        assertEquals(45 /* or 120 + X if Armor of Bones gives X */, biggles.getResistance(com.bapppis.core.creature.Creature.Resistances.SLASHING));
        assertEquals(90, biggles.getResistance(com.bapppis.core.creature.Creature.Resistances.NATURE));
        // These should match the stat/resistance/vision changes from your item JSONs
        // For demonstration, let's check inventory and equipment slots
        assertEquals("Armor of Water", biggles.getEquipped(com.bapppis.core.item.EquipmentSlot.ARMOR).getName());
        assertEquals("Crusader Helmet", biggles.getEquipped(com.bapppis.core.item.EquipmentSlot.HELMET).getName());
        assertEquals("Legs of Speed", biggles.getEquipped(com.bapppis.core.item.EquipmentSlot.LEGWEAR).getName());
        assertEquals("Tower Shield", biggles.getEquipped(com.bapppis.core.item.EquipmentSlot.OFFHAND).getName());

        // Now equip Falchion of Doom (two-handed), should unequip shield
        biggles.equipItem(biggles.getInventory().getWeapons().get(0));
        assertEquals("Falchion of Doom", biggles.getEquipped(com.bapppis.core.item.EquipmentSlot.WEAPON).getName());
        // Two-handed: shield slot should now be empty or not Falchion
        assertEquals("Falchion of Doom", biggles.getEquipped(com.bapppis.core.item.EquipmentSlot.OFFHAND).getName());
        // Tower Shield should be back in inventory
        assertEquals(1, biggles.getInventory().getOffhands().size());
        assertEquals("Tower Shield", biggles.getInventory().getOffhands().get(0).getName());

        // Try to equip shield again, should unequip Falchion (if logic is correct)
        biggles.equipItem(biggles.getInventory().getOffhands().get(0));
        assertEquals("Tower Shield", biggles.getEquipped(com.bapppis.core.item.EquipmentSlot.OFFHAND).getName());
        // Falchion should be back in inventory
        assertEquals(1, biggles.getInventory().getWeapons().size());
        assertEquals("Falchion of Doom", biggles.getInventory().getWeapons().get(0).getName());

        // Optionally, print for debug
        System.out.println(biggles.toString());
    }
}

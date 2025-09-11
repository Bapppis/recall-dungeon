package com.bapppis.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Creature.Stats;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.item.EquipmentSlot;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.property.PropertyManager;

public class TestCreatureAttack {
    // Test player creation logic here
    @Test
    public void testPlayerCreation() {
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();

        // Make the player Biggles
        Player biggles = (Player) CreatureLoader.getCreatureById(5000);
        assert biggles != null;

        // Give Falchion of Doom to Biggles and equip it
        biggles.addItem(ItemLoader.getItemById(9800)); // Falchion of Doom
        biggles.equipItem(biggles.getInventory().getWeapons().get(0)); // Equip Falchion of Doom
        System.out.println(biggles.toString());
        System.out.println(biggles.getEquipped(EquipmentSlot.WEAPON).toString());

    }

    // make an assert function for biggles
    private void assertBigglesDefaults(Player biggles) {
        // Implement assertions for Biggles defaults

    }

    private void assertBigglesDebuffed(Player biggles) {
        // Implement assertions for Biggles debuffed state

    }
}

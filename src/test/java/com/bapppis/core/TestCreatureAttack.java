package com.bapppis.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Creature.Stats;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.item.EquipmentSlot;
import com.bapppis.core.item.Item;
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
        Player biggles = CreatureLoader.getPlayerById(5000);
        assert biggles != null;
        biggles.setStat(Stats.CONSTITUTION, 100);
        // Give Falchion of Doom to Biggles and equip it
        biggles.addItem(ItemLoader.getItemById(9800)); // Falchion of Doom
        biggles.equipItem(biggles.getInventory().getWeapons().get(1)); // Equip Falchion of Doom
        System.out.println(biggles.getCurrentHp() + " / " + biggles.getMaxHp());
        biggles.attack(biggles); // Self-attack for testing
        System.out.println(biggles.getCurrentHp() + " / " + biggles.getMaxHp());
        // Drink healing potion
        Item minorHealingPotion = ItemLoader.getItemById(8000); // Minor Healing Potion
        minorHealingPotion.onApply(biggles);
        System.out.println(biggles.getCurrentHp() + " / " + biggles.getMaxHp());
    }

    // make an assert function for biggles
    private void assertBigglesDefaults(Player biggles) {
        // Implement assertions for Biggles defaults

    }

    private void assertBigglesDebuffed(Player biggles) {
        // Implement assertions for Biggles debuffed state

    }
}

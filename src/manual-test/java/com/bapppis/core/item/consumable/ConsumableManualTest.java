package com.bapppis.core.item.consumable;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class ConsumableManualTest {

    @Test
    public void ConsumableManualTest() {
        AllLoaders.loadAll();

        // Load Biggles
        Player biggles = CreatureLoader.getPlayerById(5000);

        // Load the Health Regen 1 Potion (id 8005)
        Item potion = ItemLoader.getItemByName("HealthRegen1Potion");
        assertNotNull(potion, "Health Regen 1 Potion should be loaded");
        assertEquals("Health Regen 1 Potion", potion.getName());

        // Set max health to 50 for testing
        biggles.setMaxHp(50);
        biggles.setCurrentHp(10); // Set current health to 10
        // Print current and max hp and buffs
        System.out.println("Before using potion:");
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        System.out.println("Biggles Buffs: " + biggles.getBuffs());

        // Use the consumable on the player
        potion.onApply(biggles);
        biggles.tickProperties();
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.tickProperties();
        System.out.println("Biggles Buffs: " + biggles.getBuffs());
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.tickProperties();
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.tickProperties();
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.tickProperties();
        System.out.println("Biggles HP: " + biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        System.out.println("Biggles Buffs: " + biggles.getBuffs());

    }
}
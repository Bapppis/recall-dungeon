package com.bapppis.core.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;

public class NameConvenienceTest {

    @Test
    public void testItemAndCreatureByNameHelpers() {
        AllLoaders.loadAll();
        Player p = (Player) CreatureLoader.getCreatureById(5000);
        assertNotNull(p, "Player fixture should load");

        // Add an item by name (known: "Rusty Iron Sword")
        boolean added = p.addItemByName("Rusty Iron Sword");
        assertTrue(added, "Should add Rusty Iron Sword by name");

        // Equip it by name
        boolean equipped = p.equipItemByName("Rusty Iron Sword");
        assertTrue(equipped, "Should equip Rusty Iron Sword by name");

        // Consume a potion by name (use Healing Potion which exists in data)
        // First add it
        boolean addedPot = p.addItemByName("Healing Potion");
        assertTrue(addedPot, "Should add Healing Potion by name");
        boolean consumed = p.consumeItemByName("Healing Potion");
        assertTrue(consumed, "Should consume Healing Potion by name");

        // Drop an item by name (placeholder - will remove from inventory)
        boolean dropped = p.dropItemByName("Rusty Iron Sword");
        assertTrue(dropped, "Should drop Rusty Iron Sword by name (placeholder)");

        // Spawn a creature by id string (common goblin id is 15000 in data)
        com.bapppis.core.creature.Creature copy = CreatureLoader.spawnCreatureByName("15000");
        assertNotNull(copy, "Should be able to spawn creature by id string '15000'");
    }
}

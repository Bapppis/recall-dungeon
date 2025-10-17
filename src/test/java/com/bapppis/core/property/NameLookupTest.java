package com.bapppis.core.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.Stats;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;

public class NameLookupTest {

    @Test
    public void testNameLookupsAndCreatureAddByName() {
        // Ensure all assets loaded
        AllLoaders.loadAll();

        // Property lookup (case-insensitive)
        Property coward = PropertyLoader.getPropertyByName("Coward");
        assertNotNull(coward, "Property 'Coward' should be found by name");
        // Name lookup should be case-insensitive
        Property cowardLower = PropertyLoader.getPropertyByName("coward");
        assertNotNull(cowardLower);
        assertEquals(coward.getId(), cowardLower.getId());

        // Item lookup (case-insensitive)
        Item rusty = ItemLoader.getItemByName("Rusty Iron Sword");
        assertNotNull(rusty, "Item 'Rusty Iron Sword' should be found by name");
        Item rustyLower = ItemLoader.getItemByName("rusty iron sword");
        assertNotNull(rustyLower);
        assertEquals(rusty.getId(), rustyLower.getId());

        // Creature: get Biggles by id and then add property by name
        Creature biggles = CreatureLoader.getCreatureById(5000);
        assertNotNull(biggles, "Creature id 5000 (Biggles) should exist");

    // Ensure Coward is not already applied so this test is deterministic
    try { biggles.removeProperty(coward.getId()); } catch (Exception ignored) {}

    // Record base STR/DEX
    int baseStr = biggles.getStat(Stats.STRENGTH);
    int baseDex = biggles.getStat(Stats.DEXTERITY);

    // Add property by name (string) using the new convenience method
    boolean added = biggles.addProperty("Coward");
    assertTrue(added, "addProperty by name should return true when found");

    // After applying Coward, STR should be decreased (Coward JSON reduces STR by 2)
    int newStr = biggles.getStat(Stats.STRENGTH);
    int newDex = biggles.getStat(Stats.DEXTERITY);
    assertEquals(baseStr - 2, newStr, "STR should be reduced by Coward property");
    assertEquals(baseDex + 2, newDex, "DEX should be increased by Coward property");

    // Clean up: remove by id and verify stats restored
    biggles.removeProperty(coward.getId());
    assertEquals(baseStr, biggles.getStat(Stats.STRENGTH));
    assertEquals(baseDex, biggles.getStat(Stats.DEXTERITY));
    }
}

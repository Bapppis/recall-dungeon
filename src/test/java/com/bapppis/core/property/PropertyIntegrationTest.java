package com.bapppis.core.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;

public class PropertyIntegrationTest {

    @Test
    public void testAddRemoveCowardOnBiggles() {
        AllLoaders.loadAll();

        // Spawn Biggles (id 5000) via CreatureLoader utility if available, else instantiate
    Creature biggles = CreatureLoader.getCreatureById(5000);
        assertNotNull(biggles, "Biggles (5000) must be loadable");

        // Print initial stats and properties
        System.out.println("Initial Biggles:\n" + biggles);

        // Ensure Coward (3666) is not currently applied
        if (biggles.getTrait(3666) != null) {
            // If present, remove it to create a clean baseline
            biggles.removeProperty(3666);
        }

        assertNull(biggles.getTrait(3666), "Coward should not be present at test start");
        int baseSTR = biggles.getSTR();
        int baseDEX = biggles.getDEX();

        // Add Coward property (id 3666) and assert changes
        com.bapppis.core.property.Property coward = com.bapppis.core.property.PropertyLoader.getProperty(3666);
        assertNotNull(coward, "Coward property (3666) must be present in loader");
        // Debug: ensure property has expected id/type/stat modifiers
        System.out.println("Coward object: id=" + coward.getId() + ", type=" + coward.getType() + ", name=" + coward.getName());
        if (coward instanceof com.bapppis.core.property.PropertyImpl) {
            com.bapppis.core.property.PropertyImpl pi = (com.bapppis.core.property.PropertyImpl) coward;
            System.out.println("Coward statModifiers: " + pi.getStatModifiers());
            assertNotNull(pi.getStatModifiers(), "Coward.statModifiers must not be null");
            assertTrue(pi.getStatModifiers().containsKey(com.bapppis.core.creature.Creature.Stats.STRENGTH), "Coward must modify STRENGTH");
            assertTrue(pi.getStatModifiers().containsKey(com.bapppis.core.creature.Creature.Stats.DEXTERITY), "Coward must modify DEXTERITY");
        }

        biggles.addProperty(coward);
        System.out.println("After adding Coward:\n" + biggles);

        // Coward JSON decreases STR by 2 and increases DEX by 2
        assertEquals(baseSTR - 2, biggles.getSTR(), "STR should be decreased by 2 after Coward applied");
        assertEquals(baseDEX + 2, biggles.getDEX(), "DEX should be increased by 2 after Coward applied");

        // Remove Coward and assert stats revert
        biggles.removeProperty(3666);
        System.out.println("After removing Coward:\n" + biggles);

        assertEquals(baseSTR, biggles.getSTR(), "STR should be restored after Coward removed");
        assertEquals(baseDEX, biggles.getDEX(), "DEX should be restored after Coward removed");
    }
}

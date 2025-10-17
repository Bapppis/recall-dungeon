package com.bapppis.core.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;

public class PropertyManagerTest {

    @Test
    public void testRemoveByName() {
        AllLoaders.loadAll();
        Creature biggles = CreatureLoader.getCreatureById(5000);
        assertNotNull(biggles);

        // Ensure we can add the Necrotic Plague debuff by name
        boolean added = biggles.addProperty("Necrotic Plague");
        assertTrue(added, "Expected Necrotic Plague to be applied");
        // Confirm it's present in debuffs (id 2335 per data)
        assertNotNull(biggles.getDebuff(2335));

        // Remove by name and assert removal
        boolean removed = biggles.removeProperty("Necrotic Plague");
        assertTrue(removed, "Expected Necrotic Plague to be removed by name");
        assertNull(biggles.getDebuff(2335));
    }
}

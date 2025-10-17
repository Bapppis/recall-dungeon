package com.bapppis.core.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;

public class ManualPropertyTest {

    @Test
    public void testAddRemoveCowardOnBiggles() {
        AllLoaders.loadAll();

        // Spawn Biggles (id 5000) via CreatureLoader utility if available, else
        // instantiate
        Creature biggles = CreatureLoader.getCreatureById(5000);
        System.out.println(biggles);
        System.out.println("After Debuffs-------------------------------------");
        biggles.addProperty("Necrotic Plague");
        System.out.println(biggles);
        biggles.tickProperties();
        // after deleting the debuff, stats should revert to original values
        biggles.removeProperty(2335);
        System.out.println("After Removing Debuff-------------------------------------");
        System.out.println(biggles);
    }
}

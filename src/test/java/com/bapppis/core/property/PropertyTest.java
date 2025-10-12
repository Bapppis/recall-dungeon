package com.bapppis.core.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;

public class PropertyTest {

    @Test
    public void testAddRemoveCowardOnBiggles() {
        AllLoaders.loadAll();

        // Spawn Biggles (id 5000) via CreatureLoader utility if available, else
        // instantiate
        Creature biggles = CreatureLoader.getCreatureById(5000);
        biggles.setMaxHp(200);
        biggles.modifyHp(-199);
        System.out.println(biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        // Print all biggles' buffs
        biggles.addProperty(1011);
        System.out.println("Buff Test");
        System.out.println(biggles.printProperties());
        System.out.println("After a round");
        biggles.tickProperties();
        System.out.println(biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.tickProperties();
        biggles.tickProperties();
        biggles.tickProperties();
        biggles.tickProperties();
        System.out.println(biggles.getCurrentHp() + "/" + biggles.getMaxHp());
    }
}

package com.bapppis.core.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Creature.Resistances;

public class PropertyTest {

    @Test
    public void testAddRemoveCowardOnBiggles() {
        AllLoaders.loadAll();

        // Spawn Biggles (id 5000) via CreatureLoader utility if available, else
        // instantiate
        Creature biggles = CreatureLoader.getCreatureById(5000);
        /* biggles.setMaxHp(200);
        System.out.println(biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        // Print all biggles' buffs
        biggles.addProperty(2334);
        System.out.println(biggles.printProperties());
        System.out.println("After a round");
        biggles.tickProperties();
        System.out.println(biggles.getCurrentHp() + "/" + biggles.getMaxHp());
        biggles.tickProperties();
        biggles.tickProperties();
        biggles.tickProperties();
        biggles.tickProperties();
        System.out.println(biggles.getCurrentHp() + "/" + biggles.getMaxHp()); */

        // print darkness resistance
        System.out.println("Biggles darkness resistance: " + biggles.getResistance(Resistances.DARKNESS));
        biggles.addProperty(2333); // Afraid
        System.out.println("Biggles darkness resistance after Afraid: " + biggles.getResistance(Resistances.DARKNESS));
        biggles.removeProperty(2333); // Remove Afraid immediately for testing
        System.out.println("Biggles darkness resistance after removing Afraid: " + biggles.getResistance(Resistances.DARKNESS));
    }
}

package com.bapppis.core.Creature.player;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.AllLoaders;

public class TestPlayerCreation {
    // Test player creation logic here
    @Test
    public void testPlayerCreation() {
        AllLoaders.loadAll();
        // Make the player Voss
        Player captainVoss = CreatureLoader.getPlayerById(5001);
        assert captainVoss != null;

        System.out.println(captainVoss);
        System.out.println("Voss Traits: " + captainVoss.getTraits());

        Player biggles = CreatureLoader.getPlayerById(5000);
        assert biggles != null;

        System.out.println(biggles);
        System.out.println("Biggles Traits: " + biggles.getTraits());

        // Test Goblin (species-level creature) shows small format
        Creature goblin = CreatureLoader.getCreatureById(15000);
        assert goblin != null;
        System.out.println("\nGoblin small print test:");
        System.out.println(goblin);
    }
}

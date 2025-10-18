package com.bapppis.core.Creature.player;

import org.junit.jupiter.api.Test;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.player.Player;

public class TestBigglesCreation {
    @Test
    public void testBigglesCreation() {
        // Use AllLoaders to initialize all asset loaders
        AllLoaders.loadAll();

        Player biggles = CreatureLoader.getPlayerById(5000);

        System.out.println(biggles.toString());
    }
}

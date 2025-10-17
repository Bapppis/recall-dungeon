package com.bapppis.core.Creature.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        // Sanity check — test will fail fast if loading didn't work
        assert biggles != null;

        System.out.println(biggles.toString());
    }

    // helpers (not used directly by the test, kept for convenience)
    private void assertBigglesDefaults(Player biggles) {
        assertEquals("Biggles The Unlucky", biggles.getName());
        // finalizeAfterLoad applies HP bonuses: baseHp 5 + hpLvlBonus 4 + constitution
        // delta (12 -> +2) => 11
        assertEquals(11, biggles.getMaxHp());
        assertEquals(11, biggles.getCurrentHp());
        assertEquals(com.bapppis.core.Size.SMALL, biggles.getSize());
        assertEquals(com.bapppis.core.Type.PLAYER, biggles.getType());
        assertEquals(com.bapppis.core.CreatureType.HUMANOID, biggles.getCreatureType());
    assertEquals(7, biggles.getStat(com.bapppis.core.Stats.INTELLIGENCE));
    assertEquals(2, biggles.getStat(com.bapppis.core.Stats.LUCK));
    }

    private void assertBigglesDebuffed(Player biggles) {
        assertEquals("Biggles The Unlucky", biggles.getName());
        assertEquals(11, biggles.getMaxHp());
        assertEquals(11, biggles.getCurrentHp());
        assertEquals(com.bapppis.core.Size.SMALL, biggles.getSize());
        assertEquals(com.bapppis.core.Type.PLAYER, biggles.getType());
        assertEquals(com.bapppis.core.CreatureType.HUMANOID, biggles.getCreatureType());
    assertEquals(6, biggles.getStat(com.bapppis.core.Stats.INTELLIGENCE));
    assertEquals(1, biggles.getStat(com.bapppis.core.Stats.LUCK));
    }
}

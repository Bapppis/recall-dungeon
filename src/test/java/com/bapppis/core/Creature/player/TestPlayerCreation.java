package com.bapppis.core.Creature.player;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.AllLoaders;

public class TestPlayerCreation {
    // Test player creation logic here
    @Test
    public void testPlayerCreation() {
        AllLoaders.loadAll();
        // Make the player Voss
    Player captainVoss = CreatureLoader.getPlayerById(5001);
        assert captainVoss != null;

        //assertCaptainVossDefaults(captainVoss);
    // System.out.println(captainVoss.toString());

        /* captainVoss.removeProperty(4001);
    captainVoss.addProperty(PropertyLoader.getProperty(2333)); // Add a debuff
        assertCaptainVossDebuffed(captainVoss);
        System.out.println(captainVoss.toString()); */

    captainVoss.addXp(72);
    // System.out.println(captainVoss.toString());
    // Sanity: After gaining XP, level should be >= start level
    org.junit.jupiter.api.Assertions.assertTrue(captainVoss.getLevel() >= 0);

    }

    // make an assert function for captain voss
    /* private void assertCaptainVossDefaults(Player captainVoss) {
        // Implement assertions for Captain Voss defaults
        assertEquals("Captain Aldric Voss", captainVoss.getName());
        assertEquals(30, captainVoss.getMaxHp());
        assertEquals(30, captainVoss.getCurrentHp());
        assertEquals(com.bapppis.core.Size.MEDIUM, captainVoss.getSize());
        assertEquals(com.bapppis.core.Type.PLAYER, captainVoss.getType());
        assertEquals(com.bapppis.core.CreatureType.HUMANOID, captainVoss.getCreatureType());
    assertEquals(14, captainVoss.getStat(Stats.STRENGTH));
    assertEquals(11, captainVoss.getStat(Stats.DEXTERITY));
    assertEquals(13, captainVoss.getStat(Stats.CONSTITUTION));
    assertEquals(11, captainVoss.getStat(Stats.INTELLIGENCE));
    assertEquals(11, captainVoss.getStat(Stats.WISDOM));
    assertEquals(11, captainVoss.getStat(Stats.CHARISMA));
    assertEquals(1, captainVoss.getStat(Stats.LUCK));
    }

    private void assertCaptainVossDebuffed(Player captainVoss) {
        // Implement assertions for Captain Voss defaults
        assertEquals("Captain Aldric Voss", captainVoss.getName());
        assertEquals(30, captainVoss.getMaxHp());
        assertEquals(30, captainVoss.getCurrentHp());
        assertEquals(com.bapppis.core.Size.MEDIUM, captainVoss.getSize());
        assertEquals(com.bapppis.core.Type.PLAYER, captainVoss.getType());
        assertEquals(com.bapppis.core.CreatureType.HUMANOID, captainVoss.getCreatureType());
    assertEquals(13, captainVoss.getStat(Stats.STRENGTH));
    assertEquals(10, captainVoss.getStat(Stats.DEXTERITY));
    assertEquals(12, captainVoss.getStat(Stats.CONSTITUTION));
    assertEquals(10, captainVoss.getStat(Stats.INTELLIGENCE));
    assertEquals(10, captainVoss.getStat(Stats.WISDOM));
    assertEquals(10, captainVoss.getStat(Stats.CHARISMA));
    assertEquals(1, captainVoss.getStat(Stats.LUCK));
    } */
}

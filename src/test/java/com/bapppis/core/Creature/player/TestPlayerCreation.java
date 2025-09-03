package com.bapppis.core.Creature.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Creature.Stats;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.property.PropertyManager;

public class TestPlayerCreation {
    // Test player creation logic here
    @Test
    public void testPlayerCreation() {
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        // Make the player Voss
        Player captainVoss = (Player) CreatureLoader.getCreatureById(5001);
        assert captainVoss != null;

        //assertCaptainVossDefaults(captainVoss);
        System.out.println(captainVoss.toString());

        /* captainVoss.removeProperty(4001);
        captainVoss.addProperty(PropertyManager.getProperty(2000)); // Add a debuff
        assertCaptainVossDebuffed(captainVoss);
        System.out.println(captainVoss.toString()); */

        captainVoss.addXp(72);
        System.out.println(captainVoss.toString());

    }

    // make an assert function for captain voss
    private void assertCaptainVossDefaults(Player captainVoss) {
        // Implement assertions for Captain Voss defaults
        assertEquals("Captain Aldric Voss", captainVoss.getName());
        assertEquals(30, captainVoss.getMaxHp());
        assertEquals(30, captainVoss.getCurrentHp());
        assertEquals(com.bapppis.core.creature.Creature.Size.MEDIUM, captainVoss.getSize());
        assertEquals(com.bapppis.core.creature.Creature.Type.PLAYER, captainVoss.getType());
        assertEquals(com.bapppis.core.creature.Creature.CreatureType.HUMANOID, captainVoss.getCreatureType());
        assertEquals(14, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.STRENGTH));
        assertEquals(11, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.DEXTERITY));
        assertEquals(13, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.CONSTITUTION));
        assertEquals(11, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.INTELLIGENCE));
        assertEquals(11, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.WISDOM));
        assertEquals(11, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.CHARISMA));
        assertEquals(1, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.LUCK));
    }

    private void assertCaptainVossDebuffed(Player captainVoss) {
        // Implement assertions for Captain Voss defaults
        assertEquals("Captain Aldric Voss", captainVoss.getName());
        assertEquals(30, captainVoss.getMaxHp());
        assertEquals(30, captainVoss.getCurrentHp());
        assertEquals(com.bapppis.core.creature.Creature.Size.MEDIUM, captainVoss.getSize());
        assertEquals(com.bapppis.core.creature.Creature.Type.PLAYER, captainVoss.getType());
        assertEquals(com.bapppis.core.creature.Creature.CreatureType.HUMANOID, captainVoss.getCreatureType());
        assertEquals(13, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.STRENGTH));
        assertEquals(10, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.DEXTERITY));
        assertEquals(12, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.CONSTITUTION));
        assertEquals(10, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.INTELLIGENCE));
        assertEquals(10, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.WISDOM));
        assertEquals(10, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.CHARISMA));
        assertEquals(1, captainVoss.getStat(com.bapppis.core.creature.Creature.Stats.LUCK));
    }
}

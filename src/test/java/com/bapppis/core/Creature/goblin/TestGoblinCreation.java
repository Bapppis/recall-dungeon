package com.bapppis.core.Creature.goblin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.property.PropertyLoader;
import com.bapppis.core.AllLoaders;


public class TestGoblinCreation {
    @Test
    public void testGoblinCreationAndProperties() {
        // Load all assets (properties, items, loot pools, creatures)
    AllLoaders.loadAll();

        // Fetch goblin by name
        Creature goblin = CreatureLoader.getCreature("Billy the Goblin");
        assert goblin != null;

        // Defaults after applying Coward (STR -2, DEX +2)
        assertEquals("Billy the Goblin", goblin.getName());
    assertEquals(8, goblin.getStat(com.bapppis.core.Stats.STRENGTH));
    assertEquals(12, goblin.getStat(com.bapppis.core.Stats.DEXTERITY));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.CONSTITUTION));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.INTELLIGENCE));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.WISDOM));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.CHARISMA));
    assertEquals(1, goblin.getStat(com.bapppis.core.Stats.LUCK));

        System.out.println(goblin.toString());

    // Remove Coward (3666) and add Afraid (2333, no stat modifiers)
    goblin.removeProperty(3666);
         goblin.addProperty(PropertyLoader.getProperty(2333));

        // Back to base stats
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.STRENGTH));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.DEXTERITY));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.CONSTITUTION));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.INTELLIGENCE));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.WISDOM));
    assertEquals(10, goblin.getStat(com.bapppis.core.Stats.CHARISMA));
    assertEquals(1, goblin.getStat(com.bapppis.core.Stats.LUCK));

        // Goblin should be an enemy by default
        assertEquals(com.bapppis.core.Type.ENEMY, goblin.getType());

        System.out.println(goblin.toString());
    }
}

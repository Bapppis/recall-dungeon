package com.bapppis.core.Creature.goblin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.property.PropertyManager;

public class TestGoblinCreation {
    @Test
    public void testGoblinCreationAndProperties() {
        // Load properties and creatures
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();

        // Fetch goblin by name
        Creature goblin = CreatureLoader.getCreature("Billy the Goblin");
        assert goblin != null;

        // Defaults after applying Coward (STR -2, DEX +2)
        assertEquals("Billy the Goblin", goblin.getName());
        assertEquals(8, goblin.getStat(Creature.Stats.STRENGTH));
        assertEquals(12, goblin.getStat(Creature.Stats.DEXTERITY));
        assertEquals(10, goblin.getStat(Creature.Stats.CONSTITUTION));
        assertEquals(10, goblin.getStat(Creature.Stats.INTELLIGENCE));
        assertEquals(10, goblin.getStat(Creature.Stats.WISDOM));
        assertEquals(10, goblin.getStat(Creature.Stats.CHARISMA));
        assertEquals(1, goblin.getStat(Creature.Stats.LUCK));

        System.out.println(goblin.toString());

        // Remove Coward (4000) and add Afraid (2000, no stat modifiers)
        goblin.removeProperty(4000);
        goblin.addProperty(PropertyManager.getProperty(2000));

        // Back to base stats
        assertEquals(10, goblin.getStat(Creature.Stats.STRENGTH));
        assertEquals(10, goblin.getStat(Creature.Stats.DEXTERITY));
        assertEquals(10, goblin.getStat(Creature.Stats.CONSTITUTION));
        assertEquals(10, goblin.getStat(Creature.Stats.INTELLIGENCE));
        assertEquals(10, goblin.getStat(Creature.Stats.WISDOM));
        assertEquals(10, goblin.getStat(Creature.Stats.CHARISMA));
        assertEquals(1, goblin.getStat(Creature.Stats.LUCK));

        // Goblin should be an enemy by default
        assertEquals(Creature.Type.ENEMY, goblin.getType());

        System.out.println(goblin.toString());
    }
}

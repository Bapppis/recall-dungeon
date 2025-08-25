package com.bapppis.core.Creature.humanoid;


import org.junit.jupiter.api.Test;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.creatures.humanoid.Goblin;

import static org.junit.jupiter.api.Assertions.*;

public class GoblinTest {
    @Test
    public void testGoblinCreation() {
        System.out.println("---------------------Goblin Creation---------------------");
        Goblin goblin = new Goblin();
        assertEquals("Billy the Goblin", goblin.getName());
        assertEquals(10, goblin.getMaxHp());
        assertEquals(10, goblin.getCurrentHp());
        assertEquals(Creature.Size.SMALL, goblin.getSize());
        assertEquals(Creature.Type.ENEMY, goblin.getType());
        assertEquals(Creature.CreatureType.HUMANOID, goblin.getCreatureType());
        assertEquals("A small and cowardly goblin. Are you sure you want to fight it?", goblin.getDescription());
        assertEquals(-2, goblin.getStat(Creature.Stats.STRENGTH));
        assertEquals(2, goblin.getStat(Creature.Stats.DEXTERITY));
        assertEquals(100, goblin.getResistance(Creature.Resistances.SLASHING));
        System.out.println(goblin.toString());
    }
}

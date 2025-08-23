package com.bapppis.core.creatures;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CreatureTest {

    @Test
    public void testCreatureCreation() {
        System.out.println("---------------------Creature Creation---------------------");
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
    @Test
    public void testHpModification() {
        Goblin goblin = new Goblin();
        goblin.setCurrentHp(5);
        assertEquals(5, goblin.getCurrentHp());
        goblin.modifyHp(-3);
        assertEquals(2, goblin.getCurrentHp());
        goblin.modifyHp(10);
        assertEquals(10, goblin.getCurrentHp());
    }
}

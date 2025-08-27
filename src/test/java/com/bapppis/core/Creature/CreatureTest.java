package com.bapppis.core.Creature;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.creatures.Creature.Resistances;
import com.bapppis.core.creatures.Creature.Stats;
import com.bapppis.core.creatures.humanoid.goblinoid.Goblin;

public class CreatureTest {

    @Test
    public void testCreatureCreation() {
        System.out.println("---------------------Creature creation---------------------");
        Goblin goblin = new Goblin();
        System.out.println(goblin.toString());
        assertGoblinDefaults(goblin);
    }

    @Test
    public void testResistanceModification() {
        System.out.println("---------------------Resistance modification---------------------");
        Goblin goblin = new Goblin();
        goblin.setResistance(Resistances.FIRE, 200);
        assertResistance(goblin, Resistances.FIRE, 200);

        goblin.modifyResistance(Resistances.WATER, 70);
        assertResistance(goblin, Resistances.WATER, 170);

        goblin.modifyResistance(Resistances.TRUE, -90);
        assertResistance(goblin, Resistances.TRUE, 10);
    }

    @Test
    public void testStatModification() {
        System.out.println("---------------------Stat modification---------------------");
        Goblin goblin = new Goblin();
        goblin.setStat(Stats.STRENGTH, 20);
        assertStat(goblin, Stats.STRENGTH, 20);

        goblin.modifyStat(Stats.CHARISMA, 5);
        assertStat(goblin, Stats.CHARISMA, 15);

        goblin.modifyStat(Stats.LUCK, -101);
        assertStat(goblin, Stats.LUCK, -100);
    }

    // -------- Helper assertion methods --------

    private void assertGoblinDefaults(Goblin goblin) {
        assertEquals("Billy the Goblin", goblin.getName(), "Goblin name should be Billy the Goblin");
        assertEquals(10, goblin.getMaxHp(), "Max HP should be 10");
        assertEquals(10, goblin.getCurrentHp(), "Current HP should be 10");
        assertEquals(Creature.Size.SMALL, goblin.getSize(), "Goblin size should be SMALL");
        assertEquals(Creature.Type.ENEMY, goblin.getType(), "Goblin type should be ENEMY");
        assertEquals(Creature.CreatureType.HUMANOID, goblin.getCreatureType(), "Goblin creature type should be HUMANOID");
        assertEquals("A small and cowardly goblin. Are you sure you want to fight it?", goblin.getDescription(), "Goblin description mismatch");
        goblin.toString();
    }

    private void assertResistance(Goblin goblin, Resistances resistanceType, int expectedValue) {
        assertEquals(expectedValue, goblin.getResistance(resistanceType),
            "Expected " + resistanceType + " resistance to be " + expectedValue +
            " but was " + goblin.getResistance(resistanceType));
    }

    private void assertStat(Goblin goblin, Stats statType, int expectedValue) {
        assertEquals(expectedValue, goblin.getStat(statType),
            "Expected " + statType + " stat to be " + expectedValue +
            " but was " + goblin.getStat(statType));
    }
}
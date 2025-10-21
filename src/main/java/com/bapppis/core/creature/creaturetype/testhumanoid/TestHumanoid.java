package com.bapppis.core.creature.creaturetype.testhumanoid;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Player;
import com.bapppis.core.creature.creatureEnums.Size;
import com.bapppis.core.creature.creatureEnums.Stats;

/**
 * Test creature type for validating species modification system.
 * Applies various stat/resistance modifications and properties.
 */
public class TestHumanoid extends Player {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();

        // Stat modifications: mix of positive and negative
        modifyStat(Stats.STRENGTH, 2); // +2 STR
        modifyStat(Stats.DEXTERITY, -1); // -1 DEX
        modifyStat(Stats.WISDOM, 3); // +3 WIS

        // Resistance modifications
        modifyResistance(Resistances.FIRE, 20); // 100 -> 120
        modifyResistance(Resistances.ICE, -30); // 100 -> 70

        // Size modification
        setSize(Size.LARGE);

        // Add a trait with stat modifiers
        addProperty("TestTrait1"); // This should be created in properties
    }

    public TestHumanoid() {
        super();
    }
}

package com.bapppis.core.creature.creaturetype.testhumanoid;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Size;
import com.bapppis.core.creature.creatureEnums.Stats;

/**
 * Test species for validating species modification system.
 * Applies additional modifications on top of TestHumanoid.
 */
public class TestGoblin extends TestHumanoid {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();  // Apply TestHumanoid mods first
        
        // Additional stat modifications from species
        modifyStat(Stats.STRENGTH, -3);     // -3 STR (net: +2-3 = -1)
        modifyStat(Stats.DEXTERITY, 5);     // +5 DEX (net: -1+5 = +4)
        modifyStat(Stats.INTELLIGENCE, 2);  // +2 INT
        modifyStat(Stats.CHARISMA, -2);     // -2 CHA
        modifyStat(Stats.LUCK, 1);          // +1 LUCK
        
        // Additional resistance modifications
        modifyResistance(Resistances.DARKNESS, 30);   // 100 -> 130
        modifyResistance(Resistances.LIGHT, -20);     // 100 -> 80
        modifyResistance(Resistances.PIERCING, 10);   // 100 -> 110
        
        // Override size to SMALL
        setSize(Size.SMALL);
        
        // Add species-specific traits
        addProperty("TestTrait2");  // Another trait with stat modifiers
        addProperty("TestTrait3");  // Trait without stat modifiers
    }

    public TestGoblin() {
        super();
    }
}

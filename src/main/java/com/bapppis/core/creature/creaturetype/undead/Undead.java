package com.bapppis.core.creature.creaturetype.undead;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Creature;

public class Undead extends Creature {
    public Undead() {
        super();
    }

    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        // Modify species resistances additively (default is 100 if unset)
        /* modifyResistance(Resistances.LIGHTNING, -50); // 100 - 50 = 50
        modifyResistance(Resistances.ICE, 50); // 100 + 50 = 150 */
    }
}

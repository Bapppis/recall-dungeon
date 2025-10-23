package com.bapppis.core.creature.creaturetype.humanoid;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Creature;

public class Humanoid extends Creature {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        setSize(com.bapppis.core.creature.creatureEnums.Size.MEDIUM);
        addProperty("BleedImmunity");

    }

    public Humanoid() {
        super();
    }
}

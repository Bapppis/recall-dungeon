package com.bapppis.core.creature.creaturetype.beast;

import com.bapppis.core.creature.Creature;

public class Beast extends Creature {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        setSize(com.bapppis.core.creature.creatureEnums.Size.MEDIUM);
    }

    public Beast() {
        super();
    }
}

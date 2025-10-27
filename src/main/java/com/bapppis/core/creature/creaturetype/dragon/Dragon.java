package com.bapppis.core.creature.creaturetype.dragon;

import com.bapppis.core.creature.Creature;

public class Dragon extends Creature {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();

        this.modifyBaseMagicResist(15);
        addProperty("Darksight");
    }

    public Dragon() {
        super();
    }
}

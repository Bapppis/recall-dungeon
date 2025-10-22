package com.bapppis.core.creature.creaturetype.humanoid;

public class Human extends Humanoid {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        addProperty("HumanAdaptability");
    }

    public Human() {
        super();
    }
}

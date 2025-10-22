package com.bapppis.core.creature.creaturetype.beast;

public class Dog extends Beast {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        setSize(com.bapppis.core.creature.creatureEnums.Size.SMALL);

    }

    public Dog() {
        super();
    }
}

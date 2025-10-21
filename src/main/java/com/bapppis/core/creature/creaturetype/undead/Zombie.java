package com.bapppis.core.creature.creaturetype.undead;

public class Zombie extends Undead {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
    }
    public Zombie() {
        super();
        // Example: add 8 to CONSTITUTION
        // modifyStat(Stats.CONSTITUTION, 8);
        // Example: negate 10 from DEXTERITY
        // modifyStat(Stats.DEXTERITY, -10);
        // Example: add a property by name
        // addProperty("Relentless");
    }
}

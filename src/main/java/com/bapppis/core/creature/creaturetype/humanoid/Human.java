package com.bapppis.core.creature.creaturetype.humanoid;

public class Human extends Humanoid {
    public Human() {
        super();
        // Example: add 2 to all stats
        // modifyStat(Stats.STRENGTH, 2);
        // modifyStat(Stats.DEXTERITY, 2);
        // modifyStat(Stats.CONSTITUTION, 2);
        // Example: add a property by name
        addProperty("HumanAdaptability");
    }
}

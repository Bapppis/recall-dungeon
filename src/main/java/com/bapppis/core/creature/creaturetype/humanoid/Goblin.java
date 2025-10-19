package com.bapppis.core.creature.creaturetype.humanoid;

public class Goblin extends Humanoid {
    public Goblin() {
        super();
        // Example: add 5 to DEXTERITY
        // modifyStat(Stats.DEXTERITY, 5);
        // Example: negate 5 from STRENGTH
        // modifyStat(Stats.STRENGTH, -5);
        // Example: add a property by name
        // addProperty("Nimble");
        setSize(com.bapppis.core.Size.SMALL);
    }
}

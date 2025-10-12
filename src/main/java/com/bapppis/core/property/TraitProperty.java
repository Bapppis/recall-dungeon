package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;

public class TraitProperty extends Property {
    // No-arg constructor for Gson
    public TraitProperty() { super(); }

    public TraitProperty(Property base) { super(base); }

    @Override
    public void onApply(Creature creature) {
        super.onApply(creature);
        // Trait-specific behavior may go here
    }

    @Override
    public void onRemove(Creature creature) {
        super.onRemove(creature);
    }

    @Override
    public String toString() {
        return "Trait" + super.toString().replaceFirst("Property", "");
    }
}

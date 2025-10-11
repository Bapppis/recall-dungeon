package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;

public class TraitProperty extends PropertyImpl {
    public TraitProperty(PropertyImpl base) { super(base); }

    @Override
    public void onApply(Creature creature) {
        super.onApply(creature);
        // Trait-specific behavior may go here
    }

    @Override
    public void onRemove(Creature creature) {
        super.onRemove(creature);
    }
}

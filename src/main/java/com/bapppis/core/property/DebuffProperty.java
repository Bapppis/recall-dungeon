package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;

public class DebuffProperty extends PropertyImpl {
    public DebuffProperty(PropertyImpl base) { super(base); }

    @Override
    public void onApply(Creature creature) {
        super.onApply(creature);
        // Debuff-specific behavior can be added here in future
    }

    @Override
    public void onRemove(Creature creature) {
        super.onRemove(creature);
    }
}

package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;

public class BuffProperty extends PropertyImpl {
    public BuffProperty(PropertyImpl base) { super(base); }

    @Override
    public void onApply(Creature creature) {
        super.onApply(creature);
        // Buff-specific behavior can be added here in future
    }

    @Override
    public void onRemove(Creature creature) {
        super.onRemove(creature);
        // Buff-specific cleanup if needed
    }
}

package com.bapppis.core.property;

import com.bapppis.core.creatures.Creature;

// Make a property interface which has property and id

public interface Property {
    PropertyType getType();
    int getId();

    default void onApply(Creature creature) {
        // Default: do nothing
    }

    default void onRemove(Creature creature) {
        // Default: do nothing
    }
}
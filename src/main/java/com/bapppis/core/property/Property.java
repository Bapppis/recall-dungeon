package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;

// Make a property interface which has property and id

public interface Property {
    String getName();
    PropertyType getType();
    int getId();

    default void onApply(Creature creature) {
        // Default: do nothing
    }

    default void onRemove(Creature creature) {
        // Default: do nothing
    }
}
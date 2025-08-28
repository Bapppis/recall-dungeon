package com.bapppis.core.item;

import com.bapppis.core.creature.Creature;

public interface Item {

    ItemType getType();
    int getId();

    default void onApply(Creature creature) {
        // Default: do nothing
    }

    default void onRemove(Creature creature) {
        // Default: do nothing
    }

}

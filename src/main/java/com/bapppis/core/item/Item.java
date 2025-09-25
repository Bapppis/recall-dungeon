package com.bapppis.core.item;

import com.bapppis.core.creature.Creature;

public interface Item {
    String getName();
    String getDescription();
    String getTooltip();
    ItemType getType();
    int getId();
    EquipmentSlot getSlot();
    boolean isTwoHanded();
    void setSlot(EquipmentSlot slot);
    void setTwoHanded(boolean twoHanded);

    default void onApply(Creature creature) {
        // Default: do nothing
    }
    default void onRemove(Creature creature) {
        // Default: do nothing
    }
}

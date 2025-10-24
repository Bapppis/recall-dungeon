package com.bapppis.core.item;

import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.item.itemEnums.ItemType;

public class Misc extends Equipment {

    @Override
    public String toString() {
        return getName() + " (id=" + getId() + ", type=" + getType() + ")";
    }

    @Override public ItemType getType() { return ItemType.MISC; }
    @Override public EquipmentSlot getSlot() { return null; }
        @Override public void setSlot(EquipmentSlot slot) { }
}

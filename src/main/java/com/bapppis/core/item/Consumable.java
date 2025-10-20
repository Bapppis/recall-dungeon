package com.bapppis.core.item;

import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.item.itemEnums.ItemType;

public class Consumable extends Equipment {
    // healingDice is optional: present for healing potions, absent for property-based consumables
    private String healingDice;
    // properties field is inherited from Equipment, no need to redeclare

    public String getHealingDice() { return healingDice; }
    public void setHealingDice(String healingDice) { this.healingDice = healingDice; }

    @Override public ItemType getType() { return ItemType.CONSUMABLE; }
    @Override public EquipmentSlot getSlot() { return null; }
    @Override public void setSlot(EquipmentSlot slot) { }

    /**
     * Override onApply to prevent the duration-nulling behavior from Equipment.
     * Consumables should preserve the property's original duration.
     * This reimplements Item's default behavior without Equipment's modifications.
     */
    @Override
    public void onApply(com.bapppis.core.creature.Creature creature) {
        // Use Item's default implementation logic without Equipment's duration modification
        java.util.List<com.bapppis.core.property.Property> props = getProperties();
        if (props != null) {
            for (com.bapppis.core.property.Property p : props) {
                if (p != null) {
                    // Add property with its original duration (don't modify)
                    creature.addProperty(p);
                }
            }
        }
    }
}

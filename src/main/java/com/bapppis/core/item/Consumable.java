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
     * Override onApply to handle both healing and property application.
     * Consumables should preserve the property's original duration.
     */
    @Override
    public void onApply(com.bapppis.core.creature.Creature creature) {
        // Handle healing if healingDice is present
        if (healingDice != null && !healingDice.isBlank()) {
            int healAmount = com.bapppis.core.util.Dice.roll(healingDice);
            int newHp = Math.min(creature.getCurrentHp() + healAmount, creature.getMaxHp());
            creature.setCurrentHp(newHp);
        }
        
        // Handle properties
        java.util.List<com.bapppis.core.property.Property> props = getProperties();
        if (props != null) {
            for (com.bapppis.core.property.Property p : props) {
                if (p != null) {
                    // Add property with its original duration (don't modify)
                    creature.addProperty(p.copy());
                }
            }
        }
    }
}

package com.bapppis.core.item;

public class Consumable extends Equipment {
    // healingDice is optional: present for healing potions, absent for property-based consumables
    private String healingDice;
    // properties field is inherited from Equipment, no need to redeclare

    public String getHealingDice() { return healingDice; }
    public void setHealingDice(String healingDice) { this.healingDice = healingDice; }

    @Override public ItemType getType() { return ItemType.CONSUMABLE; }
    @Override public EquipmentSlot getSlot() { return null; }
    @Override public void setSlot(EquipmentSlot slot) { }
}

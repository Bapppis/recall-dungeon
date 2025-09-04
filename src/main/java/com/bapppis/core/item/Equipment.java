package com.bapppis.core.item;

import com.bapppis.core.creature.Creature;
import java.util.Map;

public class Equipment implements Item {
    private int id;
    private String name;
    private String description;
    private ItemType itemType;
    private EquipmentSlot equipmentSlot;
    private boolean twoHanded;
    private Rarity rarity;
    private Map<String, Integer> stats;
    private Map<String, Integer> resistances;

    public Equipment() {}

    @Override
    public int getId() { return id; }
    @Override
    public String getName() { return name; }
    @Override
    public String getDescription() { return description; }
    @Override
    public ItemType getType() { return itemType; }
    @Override
    public EquipmentSlot getSlot() { return equipmentSlot; }
    @Override
    public boolean isTwoHanded() { return twoHanded; }
    @Override
    public void setSlot(EquipmentSlot slot) { this.equipmentSlot = slot; }
    @Override
    public void setTwoHanded(boolean twoHanded) { this.twoHanded = twoHanded; }
    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public Map<String, Integer> getStats() { return stats; }
    public void setStats(Map<String, Integer> stats) { this.stats = stats; }
    public Map<String, Integer> getResistances() { return resistances; }
    public void setResistances(Map<String, Integer> resistances) { this.resistances = resistances; }

    @Override
    public void onApply(Creature creature) {}
    @Override
    public void onRemove(Creature creature) {}

    @Override
    public String toString() {
        return name + " (" + rarity + ") - " + description;
    }
}

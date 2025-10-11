package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;
import java.util.Map;

public class PropertyImpl implements Property {
    @com.google.gson.annotations.SerializedName("resistances")
    private Map<com.bapppis.core.creature.Creature.Resistances, Integer> resistanceModifiers;
    // Support for vision range modifier
    @com.google.gson.annotations.SerializedName("visionRange")
    private Integer visionRange;
    @Override
    public String toString() {
        return "id: " + id + ", " +
                " '" + name + '\'' + ", " +
                " '" + description + '\'';
    }
    private int id;
    private String name;
    private PropertyType type;
    private String description;
    private Map<Creature.Stats, Integer> statModifiers;

    public int getId() { return id; }
    public String getName() { return name; }
    public PropertyType getType() { return type; }
    public String getDescription() { return description; }
    public Map<Creature.Stats, Integer> getStatModifiers() { return statModifiers; }
    public Map<com.bapppis.core.creature.Creature.Resistances, Integer> getResistanceModifiers() { return resistanceModifiers; }
    public Integer getVisionRangeModifier() { return visionRange; }

    // Copy constructor to allow creating specific subclass instances
    public PropertyImpl(PropertyImpl other) {
        if (other == null) return;
        this.id = other.id;
        this.name = other.name;
        this.type = other.type;
        this.description = other.description;
        this.statModifiers = other.statModifiers;
        this.resistanceModifiers = other.resistanceModifiers;
        this.visionRange = other.visionRange;
    }

    @Override
    public void onApply(Creature creature) {
        if (statModifiers != null) {
            for (Map.Entry<Creature.Stats, Integer> entry : statModifiers.entrySet()) {
                creature.modifyStat(entry.getKey(), entry.getValue());
            }
        }
        if (resistanceModifiers != null) {
            for (Map.Entry<com.bapppis.core.creature.Creature.Resistances, Integer> entry : resistanceModifiers.entrySet()) {
                creature.modifyResistance(entry.getKey(), entry.getValue());
            }
        }
        if (visionRange != null) {
            creature.setVisionRange(creature.getVisionRange() + visionRange);
        }
    }

    @Override
    public void onRemove(Creature creature) {
        if (statModifiers != null) {
            for (Map.Entry<Creature.Stats, Integer> entry : statModifiers.entrySet()) {
                creature.modifyStat(entry.getKey(), -entry.getValue());
            }
        }
        if (resistanceModifiers != null) {
            for (Map.Entry<com.bapppis.core.creature.Creature.Resistances, Integer> entry : resistanceModifiers.entrySet()) {
                creature.modifyResistance(entry.getKey(), -entry.getValue());
            }
        }
        if (visionRange != null) {
            creature.setVisionRange(creature.getVisionRange() - visionRange);
        }
    }
}

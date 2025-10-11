package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Base Property class. Concrete types: {@link BuffProperty}, {@link DebuffProperty}, {@link TraitProperty}.
 *
 * This class contains the shared data shape and default apply/remove behaviour
 * that was previously implemented in PropertyImpl. Gson can deserialize into
 * a concrete subclass (PropertyImpl) which extends this base.
 */
public abstract class Property {
    @SerializedName("resistances")
    protected Map<Creature.Resistances, Integer> resistanceModifiers;

    @SerializedName("visionRange")
    protected Integer visionRange;

    protected int id;
    protected String name;
    protected PropertyType type;
    protected String description;
    protected Map<Creature.Stats, Integer> statModifiers;

    protected Property() {
        // default constructor for Gson
    }

    /** Copy constructor used by subclasses when creating specialized instances. */
    protected Property(Property other) {
        if (other == null) return;
        this.id = other.id;
        this.name = other.name;
        this.type = other.type;
        this.description = other.description;
        this.statModifiers = other.statModifiers;
        this.resistanceModifiers = other.resistanceModifiers;
        this.visionRange = other.visionRange;
    }

    public String getName() { return name; }
    public PropertyType getType() { return type; }
    public int getId() { return id; }
    public String getDescription() { return description; }
    public Map<Creature.Stats, Integer> getStatModifiers() { return statModifiers; }
    public Map<Creature.Resistances, Integer> getResistanceModifiers() { return resistanceModifiers; }
    public Integer getVisionRangeModifier() { return visionRange; }

    /** Default shared application behaviour: modify stats, resistances and vision range. */
    public void onApply(Creature creature) {
        if (statModifiers != null) {
            for (Map.Entry<Creature.Stats, Integer> entry : statModifiers.entrySet()) {
                creature.modifyStat(entry.getKey(), entry.getValue());
            }
        }
        if (resistanceModifiers != null) {
            for (Map.Entry<Creature.Resistances, Integer> entry : resistanceModifiers.entrySet()) {
                creature.modifyResistance(entry.getKey(), entry.getValue());
            }
        }
        if (visionRange != null) {
            creature.setVisionRange(creature.getVisionRange() + visionRange);
        }
    }

    /** Default shared removal behaviour: undo stat, resistance and vision changes. */
    public void onRemove(Creature creature) {
        if (statModifiers != null) {
            for (Map.Entry<Creature.Stats, Integer> entry : statModifiers.entrySet()) {
                creature.modifyStat(entry.getKey(), -entry.getValue());
            }
        }
        if (resistanceModifiers != null) {
            for (Map.Entry<Creature.Resistances, Integer> entry : resistanceModifiers.entrySet()) {
                creature.modifyResistance(entry.getKey(), -entry.getValue());
            }
        }
        if (visionRange != null) {
            creature.setVisionRange(creature.getVisionRange() - visionRange);
        }
    }
}
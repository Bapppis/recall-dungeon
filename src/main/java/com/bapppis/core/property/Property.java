package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

public abstract class Property {
    @SerializedName("resistances")
    private Map<Creature.Resistances, Integer> resistanceModifiers;

    @SerializedName("visionRange")
    private Integer visionRange;

    private int id;
    private String name;
    private PropertyType type;
    private String description;
    private Map<Creature.Stats, Integer> statModifiers;
    // Tooltip can be either a String or an array in JSON; preserve raw object and
    // provide a helper to produce a joined string like Equipment.getTooltip().
    private Object tooltip;

    protected Property() {
        // default constructor for Gson
    }

    /** Copy constructor used by subclasses when creating specialized instances. */
    protected Property(Property other) {
        if (other == null)
            return;
        this.id = other.id;
        this.name = other.name;
        this.type = other.type;
        this.description = other.description;
        this.statModifiers = other.statModifiers;
        this.resistanceModifiers = other.resistanceModifiers;
        this.visionRange = other.visionRange;
    }

    public String getName() {
        return name;
    }

    public PropertyType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Return tooltip text if present. Mirrors the logic in `Equipment.getTooltip()`
     * so property JSON can provide either a String or an array of lines.
     */
    public String getTooltip() {
        if (tooltip == null)
            return null;
        if (tooltip instanceof String)
            return (String) tooltip;
        if (tooltip instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) tooltip;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                Object o = list.get(i);
                if (o != null)
                    sb.append(o.toString());
                if (i < list.size() - 1)
                    sb.append('\n');
            }
            return sb.toString();
        }
        return tooltip.toString();
    }

    public Map<Creature.Stats, Integer> getStatModifiers() {
        return statModifiers;
    }

    public Map<Creature.Resistances, Integer> getResistanceModifiers() {
        return resistanceModifiers;
    }

    public Integer getVisionRangeModifier() {
        return visionRange;
    }

    /**
     * Default shared application behaviour: modify stats, resistances and vision
     * range.
     */
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

    /**
     * Default shared removal behaviour: undo stat, resistance and vision changes.
     */
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

    /**
     * Per-turn tick hook. Default implementation does nothing. Subclasses
     * (e.g. buff/debuff) can override to perform per-turn effects.
     */
    public void onTick(Creature creature) {
        // no-op by default
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Property[id=").append(id)
          .append(", name=").append(name == null ? "<unnamed>" : name)
          .append(", type=").append(type == null ? "<unknown>" : type.name())
          .append("]");
        if (statModifiers != null && !statModifiers.isEmpty()) {
            sb.append(" stats=").append(statModifiers.toString());
        }
        if (resistanceModifiers != null && !resistanceModifiers.isEmpty()) {
            sb.append(" resists=").append(resistanceModifiers.toString());
        }
        if (visionRange != null) {
            sb.append(" visionRange=").append(visionRange);
        }
        return sb.toString();
    }
}
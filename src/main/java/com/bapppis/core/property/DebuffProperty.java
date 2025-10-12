package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;

public class DebuffProperty extends Property {
    // No-arg constructor for Gson
    public DebuffProperty() {
        super();
    }

    public DebuffProperty(Property base) {
        super(base);
        try {
            if (base instanceof DebuffProperty) {
                DebuffProperty d = (DebuffProperty) base;
                this.duration = d.duration;
                this.hpRegen = d.hpRegen;
            }
        } catch (Exception ignored) {
        }
    }

    // Optional duration (in turns). Null means permanent.
    private Integer duration;
    private Integer hpRegen;

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getHpRegen() {
        return hpRegen;
    }

    public void setHpRegen(Integer hpRegen) {
        this.hpRegen = hpRegen;
    }

    @Override
    public void onApply(Creature creature) {
        super.onApply(creature);
        // Apply hpRegen delta (debuff may set a negative value to reduce regen)
        try {
            if (hpRegen != null && hpRegen != 0) {
                creature.modifyHpRegen(hpRegen);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRemove(Creature creature) {
        super.onRemove(creature);
        try {
            if (hpRegen != null && hpRegen != 0) {
                creature.modifyHpRegen(-hpRegen);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString() {
        return "Debuff" + super.toString().replaceFirst("Property", "") + (duration == null ? "" : (" duration=" + duration));
    }
}

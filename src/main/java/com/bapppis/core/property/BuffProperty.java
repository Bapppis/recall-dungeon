package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;

public class BuffProperty extends Property {
    // No-arg constructor for Gson
    public BuffProperty() {
        super();
    }

    public BuffProperty(Property base) {
        super(base);
        // copy subclass fields when wrapping a base Property
        try {
            if (base instanceof BuffProperty) {
                BuffProperty b = (BuffProperty) base;
                this.duration = b.duration;
                this.hpRegen = b.hpRegen;
            }
        } catch (Exception ignored) {
        }
    }

    // Optional duration (in turns). Gson will populate this from a "duration" field
    // in the property's JSON (e.g. "duration": 5). Null means permanent.
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
        // Apply hpRegen contribution for the duration of the buff
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
        // Remove hpRegen contribution when the buff expires or is removed
        try {
            if (hpRegen != null && hpRegen != 0) {
                creature.modifyHpRegen(-hpRegen);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString() {
        return "Buff" + super.toString().replaceFirst("Property", "")
                + (duration == null ? "" : (" duration=" + duration));
    }
}

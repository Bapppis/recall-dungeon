package com.bapppis.core.property.trait;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class HumanAdaptability implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.TRAIT;
    }

    @Override
    public String toString() {
        return "Human Adaptability";
    }

    @Override
    public int getId() {
        return 4001;
    }

    @Override
    public void onApply(Creature creature) {
        // Increase every stat by one but not luck
        for (Creature.Stats stat : Creature.Stats.values()) {
            if (stat != Creature.Stats.LUCK) {
                creature.modifyStat(stat, 1);
            }
        }
    }

    @Override
    public void onRemove(Creature creature) {
        for (Creature.Stats stat : Creature.Stats.values()) {
            if (stat != Creature.Stats.LUCK) {
                creature.modifyStat(stat, -1);
            }
        }
    }

}

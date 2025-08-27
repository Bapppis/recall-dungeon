package com.bapppis.core.property.trait;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.creatures.Creature.Stats;
import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class Coward implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.TRAIT;
    }

    @Override
    public String toString() {
        return "Coward";
    }
    @Override
    public int getId() {
        return 4000;
    }

    @Override
    public void onApply(Creature creature) {
        creature.modifyStat(Creature.Stats.DEXTERITY, 2);
        creature.modifyStat(Stats.STRENGTH, -2);
    }

    @Override
    public void onRemove(Creature creature) {
        creature.modifyStat(Creature.Stats.DEXTERITY, -2);
        creature.modifyStat(Stats.STRENGTH, 2);
    }
}

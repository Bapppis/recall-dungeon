package com.bapppis.core.creature.creaturetype.humanoid;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.util.StatUtil;

public class Goblin extends Humanoid {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        setSize(com.bapppis.core.creature.creatureEnums.Size.SMALL);
        StatUtil.increaseSTR(this, 1);
        StatUtil.increaseINT(this, 3);
        modifyStat(Stats.DEXTERITY, 5);

        addProperty("Darksight");
    }

    public Goblin() {
        super();
    }
}

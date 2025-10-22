package com.bapppis.core.creature.creaturetype.undead;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.util.ResistanceUtil;

public class Undead extends Creature {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        setSize(com.bapppis.core.creature.creatureEnums.Size.MEDIUM);
        ResistanceUtil.modifyDarknessResistance(this, -20);
        ResistanceUtil.modifyLightResistance(this, 20);
    }

    public Undead() {
        super();
    }
}

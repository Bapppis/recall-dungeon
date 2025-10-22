package com.bapppis.core.creature.creaturetype.humanoid;

import com.bapppis.core.util.ResistanceUtil;

public class Goblin extends Humanoid {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        setSize(com.bapppis.core.creature.creatureEnums.Size.SMALL);

        ResistanceUtil.modifyNatureResistance(this, 10);
        ResistanceUtil.modifyWindResistance(this, -10);

        addProperty("Darksight");
        addProperty("Coward");
    }

    public Goblin() {
        super();
    }
}

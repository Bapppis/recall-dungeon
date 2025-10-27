package com.bapppis.core.creature.creaturetype.dragon;

import com.bapppis.core.util.ResistanceUtil;

public class Kobold extends Dragon {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        setSize(com.bapppis.core.creature.creatureEnums.Size.SMALL);

        ResistanceUtil.modifyIceResistance(this, 10);
        ResistanceUtil.modifyFireResistance(this, -10);
        ResistanceUtil.modifySlashingResistance(this, -10);

        addProperty("Coward");
        // Later add a spell for these guys
    }

    public Kobold() {
        super();
    }
}

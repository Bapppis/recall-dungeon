package com.bapppis.core.creature.creaturetype.undead;
import com.bapppis.core.util.ResistanceUtil;

public class Skeleton extends Undead {
    @Override
    protected void applySpeciesModifications() {
        super.applySpeciesModifications();
        ResistanceUtil.modifyBludgeoningResistance(this, 20);
        ResistanceUtil.modifySlashingResistance(this, -20);

        this.modifyBaseMagicResist(5);
        // Remember to add bleed immunity
    }
    public Skeleton() {
        super();
    }
}

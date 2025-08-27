package com.bapppis.core.creatures.player.humanplayer;

import com.bapppis.core.creatures.player.Player;
import com.bapppis.core.property.trait.HumanAdaptability;

public class CaptainVoss extends Player {
    public CaptainVoss() {
        super();
        // Initialize Captain Voss specific attributes here
        this.setName("Captain Aldric Voss");
        this.setMaxHp(30);
        this.setCurrentHp(30);
        this.setSize(Size.MEDIUM);
        this.setCreatureType(CreatureType.HUMANOID);
        this.modifyStat(Stats.STRENGTH, 3);
        this.modifyStat(Stats.CONSTITUTION, 2);
        this.setDescription("Captain Voss, a seasoned warrior. Determined to find home and protect his homeland.");

        addTrait(new HumanAdaptability());
    }
}

package com.bapppis.core.creature.humanoid.goblinoid;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.property.trait.Coward;

public class Goblin extends Creature {

    public Goblin() {
        setName("Billy the Goblin");
        setMaxHp(10);
        setCurrentHp(10);
        setSize(Size.SMALL);
        setCreatureType(CreatureType.HUMANOID);
        setDescription("A small and cowardly goblin. Are you sure you want to fight it?");
        modifyResistance(Resistances.SLASHING, 50);

        addTrait(new Coward());
    }

}
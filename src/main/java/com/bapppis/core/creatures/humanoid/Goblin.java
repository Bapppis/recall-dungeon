package com.bapppis.core.creatures.humanoid;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.property.Coward;

public class Goblin extends Creature {

    public Goblin() {
        setName("Billy the Goblin");
        setMaxHp(10);
        setCurrentHp(10);
        setSize(Size.SMALL);
        setType(Type.ENEMY);
        setCreatureType(CreatureType.HUMANOID);
        setDescription("A small and cowardly goblin. Are you sure you want to fight it?");

        modifyStat(Stats.STRENGTH, -2);
        modifyStat(Stats.DEXTERITY, 2);
        modifyResistance(Resistances.SLASHING, 50);

        addProperty(new Coward());
    }

}
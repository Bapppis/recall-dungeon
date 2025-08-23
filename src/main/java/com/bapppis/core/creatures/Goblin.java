package com.bapppis.core.creatures;

import com.bapppis.core.properties.Coward;

public class Goblin extends Creature {

    public Goblin() {
        setName("Billy the Goblin");
        setMaxHp(10);
        setCurrentHp(10);
        setSize(Size.SMALL);
        setType(Type.ENEMY);
        setCreatureType(CreatureType.HUMANOID);
        setDescription("A small and cowardly goblin. Are you sure you want to fight it?");

        setStat(Stats.STRENGTH, -2);
        setStat(Stats.DEXTERITY, 2);
        setResistance(Resistances.SLASHING, 100);

        addProperty(new Coward());
    }

}
package com.bapppis.core.entities;

import com.bapppis.core.components.Coward;

public class Goblin extends Creature {

    public Goblin() {
        setName("Billy the Goblin");
        setMaxHp(10);
        setCurrentHp(10);
        setSize(Size.SMALL);
        setType(Type.ENEMY);
        setCreatureType(CreatureType.HUMANOID);

        addComponent(new Coward());
    }

}
package com.bapppis.core.creature;

import com.bapppis.core.Type;

public class Enemy extends Creature {
    public Enemy() {
        // Default Creature constructor already sets Type.ENEMY and base stats
        this.setType(Type.ENEMY);
    }
}

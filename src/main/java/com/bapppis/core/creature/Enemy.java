package com.bapppis.core.creature;

public class Enemy extends Creature {
    public Enemy() {
        // Default Creature constructor already sets Type.ENEMY and base stats
        this.setType(Type.ENEMY);
    }
}

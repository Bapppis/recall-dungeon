package com.bapppis.core.entities;

public class Goblin {
    // Goblin has hp that can be damaged hp starts 25
    private int hp;

    public Goblin() {
        this.hp = 25;
    }

    public int getHp() {
        return hp;
    }

    public void takeDamage(int damage) {
        hp -= damage;
    }

}

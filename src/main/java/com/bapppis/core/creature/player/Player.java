package com.bapppis.core.creature.player;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.item.Item;

public class Player extends Creature {
    // current position on the floor (null until placed/spawned)
    private Coordinate position;

    public Player() {
        /* this.setMaxHp(20);
        this.setCurrentHp(20);
        this.setType(Type.PLAYER); */
    }

    public static Player fromJson(String json) {
    com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
        .registerTypeAdapter(com.bapppis.core.Resistances.class,
            new com.bapppis.core.util.ResistancesDeserializer())
        .create();
        return gson.fromJson(json, Player.class);
    }

    public boolean addItem(Item item) {
        return getInventory().addItem(item);
    }

    public boolean removeItem(Item item) {
        return getInventory().removeItem(item);
    }

    // --- Position helpers ---
    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public void setPosition(int x, int y) {
        this.position = new Coordinate(x, y);
    }

    public int getX() {
        return position != null ? position.getX() : -1;
    }

    public int getY() {
        return position != null ? position.getY() : -1;
    }

    @Override
    public String toString() {
    return super.toString();
    }
}

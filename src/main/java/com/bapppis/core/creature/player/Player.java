package com.bapppis.core.creature.player;

import java.util.ArrayList;
import java.util.List;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.item.Item;
import com.bapppis.core.property.Property;
import com.google.gson.Gson;

public class Player extends Creature {
    //add item inventory
    private List<Item> inventory;

    public Player() {
        inventory = new ArrayList<>();
        this.setMaxHp(20);
        this.setCurrentHp(20);
        this.setType(Type.PLAYER);
    }

    public static Player fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Player.class);
    }

    public void addItem(Item item) {
        inventory.add(item);
    }

    public void removeItem(Item item) {
        inventory.remove(item);
    }

    public List<Item> getInventory() {
        return inventory;
    }

    @Override
    public String toString() {
        return super.toString() + ", Inventory: " + inventory;
    }
}

package com.bapppis.core.creatures.player;

import java.util.ArrayList;
import java.util.List;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.item.Item;

public class Player extends Creature {
    //add item inventory
    private List<Item> inventory;

    public Player() {
        inventory = new ArrayList<>();
        this.setMaxHp(20);
        this.setCurrentHp(20);
        this.setType(Type.PLAYER);
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
}

package com.bapppis.core.dungeon;

import java.util.HashMap;

public abstract class Dungeon {
    // hashmap to store the floors
    private HashMap<Integer, Floor> floors = new HashMap<>();

    // Add a floor with a specific key (floor number)
    public void addFloor(int id, Floor floor) {
        floors.put(id, floor);
    }

    public Floor getFloor(int id) {
        return floors.get(id);
    }
}
package com.bapppis.core.dungeon;

import java.util.HashMap;

public abstract class Dungeon {
    // hashmap to store the floors
    private HashMap<Integer, Floor> floors = new HashMap<>();

    // Add floors starting the key value from -10
    public void addFloor(Floor floor) {
        floors.put(-10 - floors.size(), floor);
    }

    public Floor getFloor(int id) {
        return floors.get(id);
    }
}

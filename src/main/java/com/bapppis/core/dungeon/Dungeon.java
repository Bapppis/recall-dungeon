package com.bapppis.core.dungeon;

import java.util.HashMap;

public abstract class Dungeon {
    private HashMap<Integer, Floor> floors = new HashMap<>();

    public void addFloor(int id, Floor floor) {
        floors.put(id, floor);
    }

    public Floor getFloor(int id) {
        return floors.get(id);
    }
}
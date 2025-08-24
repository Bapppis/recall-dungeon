package com.bapppis.core.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.event.Event;
import com.bapppis.core.item.Item;

public class Tile {
    private final Coordinate coordinate;
    private boolean isWall;
    private boolean isOccupied;
    private boolean isSpawn;
    private boolean isUpstairs;
    private boolean isDownstairs;
    private Event isEvent;
    private Item item;
    private List<Creature> occupants;
    private Tile left;
    private Tile right;
    private Tile up;
    private Tile down;

    public Tile(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.isWall = false;
        this.isOccupied = false;
        this.isSpawn = false;
        this.isUpstairs = false;
        this.isDownstairs = false;
        this.isEvent = null;
        this.item = null;
        this.occupants = new ArrayList<>();
        this.left = null;
        this.right = null;
        this.up = null;
        this.down = null;
    }

}

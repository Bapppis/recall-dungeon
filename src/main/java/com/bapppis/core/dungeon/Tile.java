package com.bapppis.core.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.bapppis.core.creatures.Creature;
import com.bapppis.core.event.Event;
import com.bapppis.core.item.Item;

public class Tile {
    private final Coordinate coordinate;
    private boolean isWall = false;
    private boolean isBreakableWall = false;
    private boolean isOccupied = false;
    private boolean isSpawn = false;
    private boolean isUpstairs = false;
    private boolean isDownstairs = false;
    private boolean isPit = false;
    private char symbol;
    private Event isEvent = null;
    private Item item = null;
    private List<Creature> occupants = new ArrayList<>();
    private Tile left = null;
    private Tile right = null;
    private Tile up = null;
    private Tile down;

    /*
     * Symbols legend
     * # - Wall
     * < - Breakable wall
     * . - Floor
     * @ - Spawn point
     * ^ - Up staircase
     * v - Down staircase
     * ! - Event
     * C - Chest (item)
     * + - Pit
     * M - Monster
     * P - Player
     */
    public Tile(Coordinate coordinate, char symbol) {
        this.coordinate = coordinate;
        this.symbol = symbol;

        switch (symbol) {
            case '#':
                this.isWall = true;
                this.isOccupied = true;
                break;
            case '<':
                this.isBreakableWall = true;
                this.isOccupied = true;
                break;
            case '@':
                this.isSpawn = true;
                break;
            case '^':
                this.isUpstairs = true;
                break;
            case 'v':
                this.isDownstairs = true;
                break;
            case '.':
                break;
            /*case '!':
                this.isEvent = new Event();
                break;
            case 'C':
                this.item = new Item();
                break;
            case '+':
                this.isPit = true;
                break;
            case 'M':
                this.occupants.add(new Creature());
                break;
            case 'P':
                this.occupants.add(new Player());
                break;*/
            default:
                throw new IllegalArgumentException("Unknown symbol: " + symbol);
        }
    }
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public char getSymbol() {
        return symbol;
    }
}

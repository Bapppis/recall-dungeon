package com.bapppis.core.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.event.Event;
import com.bapppis.core.item.Item;

public class Tile {
    private final Coordinate coordinate;
    private boolean isWall = false;
    private boolean isBreakableWall = false;
    private boolean isSpawn = false;
    private boolean isUpstairs = false;
    private boolean isDownstairs = false;
    private boolean isPit = false;
    private boolean isDiscovered = false;
    private char symbol;
    private Event isEvent = null;
    private List<Creature> occupants = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    private Tile left = null;
    private Tile right = null;
    private Tile up = null;
    private Tile down = null;

    /*
     * Symbols legend
     * # - Wall or undiscovered tile
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
                break;
            case '<':
                this.isBreakableWall = true;
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

    // Neighbor getters
    public Tile getLeft() { return left; }
    public Tile getRight() { return right; }
    public Tile getUp() { return up; }
    public Tile getDown() { return down; }

    // Neighbor setters
    public void setLeft(Tile left) { this.left = left; }
    public void setRight(Tile right) { this.right = right; }
    public void setUp(Tile up) { this.up = up; }
    public void setDown(Tile down) { this.down = down; }

    // Fog of war and wall logic
    public boolean isWall() {
        return isWall;
    }

    public boolean isDiscovered() {
        return isDiscovered;
    }

    public void setDiscovered(boolean discovered) {
        this.isDiscovered = discovered;
    }
}

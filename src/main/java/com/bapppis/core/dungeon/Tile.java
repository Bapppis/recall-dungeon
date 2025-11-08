package com.bapppis.core.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.event.Event;
import com.bapppis.core.item.Item;
import com.bapppis.core.loot.LootPool;

public class Tile {
    private final Coordinate coordinate;
    private boolean isWall = false;
    private boolean isBreakableWall = false;
    private boolean isBrokenWall = false;
    private boolean isSpawn = false;
    private boolean isUpstairs = false;
    private boolean isDownstairs = false;
    private boolean isPit = false;
    private boolean isGenFloor = false;
    private boolean isTreasureChest = false;
    private boolean isDiscovered = false;
    private char symbol;
    private Event isEvent = null;
    private boolean isOccupied = false;
    private List<Creature> occupants = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private LootPool loot = null;

    private Tile left = null;
    private Tile right = null;
    private Tile up = null;
    private Tile down = null;

    /*
     * Symbols legend
     * # - Wall or undiscovered tile
     * < - Breakable wall
     * 0 - Broken wall
     * . - Floor
     * : - GenFloor
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
            case '0':
                this.isBrokenWall = true;
            /* case 'C':
                this.isTreasureChest = true;
                break; */
            case ':':
                this.isGenFloor = true;
                break;
            case '.':
                break;
            /*
             * case '!':
             * this.isEvent = new Event();
             * break;
             * case '.':
             * break;
             * /*case '!':
             * this.isEvent = new Event();
             * break;
             * case '+':
             * this.isPit = true;
             * break;
             * case 'M':
             * this.occupants.add(new Creature());
             * break;
             * case 'P':
             * this.occupants.add(new Player());
             * break;
             */
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
    public Tile getLeft() {
        return left;
    }

    public Tile getRight() {
        return right;
    }

    public Tile getUp() {
        return up;
    }

    public Tile getDown() {
        return down;
    }

    // Neighbor setters
    public void setLeft(Tile left) {
        this.left = left;
    }

    public void setRight(Tile right) {
        this.right = right;
    }

    public void setUp(Tile up) {
        this.up = up;
    }

    public void setDown(Tile down) {
        this.down = down;
    }

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

    /**
     * Mark this tile as a solid wall and update its symbol.
     * This preserves neighbor links and other fields.
     */
    public void setAsWall() {
        this.isWall = true;
        this.isBreakableWall = false;
        this.isOccupied = true;
        this.symbol = '#';
    }

    public void breakWall() {
        this.isBrokenWall = true;
        this.isWall = false;
        this.isOccupied = false;
        this.symbol = '0';
    }

    public void setAsGenFloor() {
        this.isWall = false;
        this.isBreakableWall = false;
        this.symbol = ':';
    }

    public void spawnTreasureChest(LootPool loot) {
        this.loot = loot;
        this.isOccupied = true;
    }

    public LootPool getLoot() {
        return loot;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setSpawn() {
        this.isSpawn = true;
        this.symbol = '@';
    }

    public boolean isUpstairs() {
        return isUpstairs;
    }

    public boolean isDownstairs() {
        return isDownstairs;
    }
}

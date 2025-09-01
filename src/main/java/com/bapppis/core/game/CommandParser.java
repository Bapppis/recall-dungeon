package com.bapppis.core.game;

/* import java.io.InputStream;
import com.bapppis.core.dungeon.mapparser.MapParser;
import com.bapppis.core.creature.player.Player; */
import java.util.*;
import com.bapppis.core.dungeon.*;

public class CommandParser {
    private Map<String, Command> commandMap = new HashMap<>();

    public CommandParser() {
        /*
         * Command for seeing nearby tiles and what they are
         * Moving (finally)
         */
        // Register commands using lowercase names
        commandMap.put("move", new MoveCommand());
        commandMap.put("attack", new AttackCommand());
        commandMap.put("map", new MapCommand());
        commandMap.put("player", new PlayerCommand());
        // ... add more as needed
    }

    public void parseAndExecute(String input) {
        if (input == null || input.trim().isEmpty()) return;
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) return;

        // Convert command and arguments to lowercase to ignore caps
        String commandName = tokens[0].toLowerCase();
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }
        Command command = commandMap.get(commandName);
        if (command != null) {
            command.execute(args);
        } else {
            System.out.println("Unknown command: " + commandName);
        }
    }
}

interface Command {
    void execute(String[] args);
}

class MoveCommand implements Command {
    public void execute(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: move <direction>");
            return;
        }
        String dir = args[0];
        int dx = 0, dy = 0;
        switch (dir) {
            case "right": case "r": case "east": case "e":
                dx = 1; dy = 0; break;
            case "left": case "l": case "west": case "w":
                dx = -1; dy = 0; break;
            case "up": case "u": case "north": case "n":
                dx = 0; dy = -1; break;
            case "down": case "d": case "south": case "s":
                dx = 0; dy = 1; break;
            case "upright": case "ur": case "northeast": case "ne":
                dx = 1; dy = -1; break;
            case "upleft": case "ul": case "northwest": case "nw":
                dx = -1; dy = -1; break;
            case "downright": case "dr": case "southeast": case "se":
                dx = 1; dy = 1; break;
            case "downleft": case "dl": case "southwest": case "sw":
                dx = -1; dy = 1; break;
            default:
                System.out.println("Unknown direction: " + dir);
                return;
        }

        Floor floor = GameState.getCurrentFloor();
        if (floor == null) {
            System.out.println("No floor loaded. Load a map first.");
            return;
        }
        var player = GameState.getPlayer();
        if (player.getPosition() == null) {
            System.out.println("Player is not on the map. Use 'player <id>' or generate a map.");
            return;
        }

        int nx = player.getX() + dx;
        int ny = player.getY() + dy;
        Coordinate target = new Coordinate(nx, ny);
        Tile next = floor.getTile(target);
        if (next == null) {
            System.out.println("You can't move outside the map.");
            return;
        }
        char sym = next.getSymbol();
        if (sym == '#' || sym == '<') {
            System.out.println("A wall blocks your way.");
            return;
        }

        player.setPosition(target);
        System.out.println("You move " + dir + " to " + target);
        MapPrinter.printWithPlayer(floor, player);
    }
}

class AttackCommand implements Command {
    public void execute(String[] args) {
        System.out.println("You attack " + (args.length > 0 ? args[0] : "something"));
    }
}

class MapCommand implements Command {
    public void execute(String[] args) {
        Floor floor = GameState.getCurrentFloor();
        if (floor == null) {
            System.out.println("No floor loaded. Use: mapgen [floor-file]");
            return;
        }
        MapPrinter.printWithPlayer(floor, GameState.getPlayer());
    }
}

class PlayerCommand implements Command {
    public void execute(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: player <id>");
            return;
        }
        try {
            int id = Integer.parseInt(args[0]);
            Game.selectPlayerById(id);
            System.out.println("Selected player id " + id + ".");
            // If a floor is already loaded, spawn the player at a valid tile and reprint
            Floor floor = GameState.getCurrentFloor();
            if (floor != null) {
                // Find a spawn '@' or fallback '.'
                Coordinate spawn = null;
                for (Map.Entry<Coordinate, Tile> entry : floor.getTiles().entrySet()) {
                    Tile t = entry.getValue();
                    if (t.getSymbol() == '@' || t.getSymbol() == '.') {
                        spawn = entry.getKey();
                        if (t.getSymbol() == '@') break;
                    }
                }
                if (spawn != null) {
                    GameState.getPlayer().setPosition(spawn);
                    System.out.println("Player spawned at " + spawn);
                    MapPrinter.printWithPlayer(floor, GameState.getPlayer());
                }
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Player id must be a number.");
        }
    }
}
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
        System.out.println("You move " + (args.length > 0 ? args[0] : "somewhere"));
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
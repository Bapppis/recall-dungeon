package com.bapppis.core.game;

import java.io.InputStream;
import java.util.*;

import com.bapppis.core.dungeon.mapparser.MapParser;

public class CommandParser {
    private Map<String, Command> commandMap = new HashMap<>();

    public CommandParser() {
        // Register commands using lowercase names
        commandMap.put("move", new MoveCommand());
        commandMap.put("attack", new AttackCommand());
        commandMap.put("mapgen", new MapGenCommand());
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

class MapGenCommand implements Command {
    public void execute(String[] args) {
        System.out.println("Generating map...");
        MapParser mapParser = new MapParser();
        String resourceName = "floor(20x20).txt";
        InputStream is = CommandParser.class.getClassLoader().getResourceAsStream(resourceName);
        mapParser.parseStream(is);
    }
}
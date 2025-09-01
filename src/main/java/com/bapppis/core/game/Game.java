package com.bapppis.core.game;

import java.io.InputStream;
import java.util.Scanner;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.MapPrinter;
import com.bapppis.core.dungeon.Tile;
import com.bapppis.core.dungeon.mapparser.MapParser;

public class Game {

    public void initialize() {
        com.bapppis.core.property.PropertyManager.loadProperties();
        com.bapppis.core.creature.CreatureLoader.loadCreatures();
        loadMap();

        System.out.println("Game initialized.");
        try (Scanner scanner = new Scanner(System.in)) {
            CommandParser commandParser = new CommandParser();
            while (true) {
                System.out.println("Please enter a command");
                System.out.print("> ");
                String input = scanner.nextLine();
                commandParser.parseAndExecute(input);
            }
        }
    }
    private void loadMap() {
        try {
            // 1) Parse a floor from resources
            MapParser parser = new MapParser();
            String resourceName = "assets/floors/floor(20x20).txt";
            /* String resourceName = "assets/floors/floor(50x50).txt"; */
            try (InputStream is = Game.class.getClassLoader().getResourceAsStream(resourceName)) {
                if (is == null) {
                    System.out.println("[Demo] Floor resource not found: " + resourceName);
                    return;
                }
                Floor floor = parser.parseStream(is);
                GameState.setCurrentFloor(floor);
            }
        } catch (Exception e) {
            System.out.println("[Demo] Failed to load demo map: " + e.getMessage());
        }
    }
    private void selectPlayer(int id) {
        // Delegate to the public static selector using the provided id
        selectPlayerById(id);
    }

    // Public helper so commands can select a player by id at runtime
    public static void selectPlayerById(int id) {
        Creature c = CreatureLoader.getCreatureById(id);
        Player player;
        if (c instanceof Player) {
            player = (Player) c;
        } else if (c != null) {
            System.out.println("[Player] Creature id " + id + " is not a Player. Using default player.");
            player = GameState.getPlayer();
        } else {
            System.out.println("[Player] No creature found for id " + id + ". Using default player.");
            player = GameState.getPlayer();
        }
        GameState.setPlayer(player);
    }

    // Static helper to spawn the current player on the current floor and optionally print the map
    public static void respawnPlayerOnCurrentFloor(boolean printAfter) {
        Floor floor = GameState.getCurrentFloor();
        if (floor == null) {
            System.out.println("[Player] No floor loaded; cannot spawn.");
            return;
        }
        Player player = GameState.getPlayer();
        Coordinate spawn = null;
        for (var entry : floor.getTiles().entrySet()) {
            Tile t = entry.getValue();
            if (t.getSymbol() == '@' || t.getSymbol() == '.') {
                spawn = entry.getKey();
                if (t.getSymbol() == '@') break; // prefer '@'
            }
        }
        if (spawn != null) {
            player.setPosition(spawn);
            System.out.println("Player spawned at " + spawn);
            if (printAfter) {
                MapPrinter.printWithPlayer(floor, player);
            }
        } else {
            System.out.println("[Player] No suitable spawn found; player position unset.");
        }
    }
}

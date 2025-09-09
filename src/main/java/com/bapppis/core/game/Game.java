
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
import com.bapppis.core.dungeon.Dungeon;
public class Game {

    private Dungeon dungeon;
    private int[] currentFloorRef = new int[]{0}; // Start at floor 0, use array for mutability

    public void initialize() {
        com.bapppis.core.property.PropertyManager.loadProperties();
        com.bapppis.core.creature.CreatureLoader.loadCreatures();
        com.bapppis.core.item.ItemLoader.loadItems();
        loadDungeon();

        System.out.println("Game initialized.");
        try (Scanner scanner = new Scanner(System.in)) {
            CommandParser commandParser = new CommandParser(dungeon, currentFloorRef);
            while (true) {
                System.out.println("You are on floor " + currentFloorRef[0]);
                System.out.println("Please enter a command (move, look, up, down, etc.)");
                System.out.print("> ");
                String input = scanner.nextLine();
                commandParser.parseAndExecute(input);
            }
        }
    }

    private void loadDungeon() {
        dungeon = new Dungeon() {}; // Use anonymous subclass since Dungeon is abstract
        MapParser parser = new MapParser();
        for (int i = -10; i <= 10; i++) {
            String resourceName = "assets/floors/floor" + i + "/floor" + i + ".txt";
            try (InputStream is = Game.class.getClassLoader().getResourceAsStream(resourceName)) {
                if (is == null) {
                    System.out.println("[Dungeon] Floor resource not found: " + resourceName);
                    continue;
                }
                Floor floor = parser.parseStream(is);
                dungeon.addFloor(i, floor);
                if (i == 0) {
                    GameState.setCurrentFloor(floor); // Start at floor 0
                }
            } catch (Exception e) {
                System.out.println("[Dungeon] Failed to load floor " + i + ": " + e.getMessage());
            }
        }
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
            // Reset all tiles to undiscovered
            for (Tile t : floor.getTiles().values()) {
                t.setDiscovered(false);
            }
            // Reveal tiles around the player before first print
            floor.revealTilesWithVision(spawn.getX(), spawn.getY(), player.getVisionRange());
            if (printAfter) {
                MapPrinter.printWithPlayer(floor, player);
            }
        } else {
            System.out.println("[Player] No suitable spawn found; player position unset.");
        }
    }
}

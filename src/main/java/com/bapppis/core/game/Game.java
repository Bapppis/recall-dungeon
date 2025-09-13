
package com.bapppis.core.game;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public Game() {
        // no-op default constructor
    }

    public Game(com.bapppis.core.creature.player.Player player) {
        if (player == null) return;
        selectPlayer(player);
    }

    public void initialize() {
        loadDungeon();

        System.out.println("Game initialized.");
        // Start a background loop to consume commands submitted from GUI
        // If a player was pre-selected, ensure they are spawned on the current floor
        if (GameState.getPlayer() != null && GameState.getCurrentFloor() != null) {
            respawnPlayerOnCurrentFloor(false);
        }
        if (running.compareAndSet(false, true)) {
            CommandParser commandParser = new CommandParser(dungeon, currentFloorRef);
            Thread loop = new Thread(() -> {
                while (running.get()) {
                    try {
                        String cmd = commandQueue.take();
                        if (cmd == null) continue;
                        if (cmd.equalsIgnoreCase("__shutdown__")) break;
                        commandParser.parseAndExecute(cmd);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.out.println("[Game] Error executing command: " + e.getMessage());
                    }
                }
                running.set(false);
            }, "Game-Command-Loop");
            loop.setDaemon(true);
            loop.start();
        }
    }

    public void submitCommand(String command) {
        if (!running.get()) return;
        if (command == null) return;
        commandQueue.offer(command);
    }

    public void shutdown() {
        if (!running.get()) return;
        running.set(false);
        commandQueue.offer("__shutdown__");
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

    public static void selectPlayer(Player player) {
        if (player == null) {
            System.out.println("[Player] Given player is null. Using default player.");
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

package com.bapppis.core.game;

// import java.io.InputStream; // Commented out - no longer using text file loading
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.MapPrinter;
import com.bapppis.core.dungeon.Tile;
// import com.bapppis.core.dungeon.mapparser.MapParser; // Commented out - no longer using text file loading
import com.bapppis.core.dungeon.Dungeon;
import com.bapppis.core.dungeon.generator.BSPRoomGenerator;

public class Game {

    private Dungeon dungeon;
    private int[] currentFloorRef = new int[] { 0 }; // Start at floor 0, use array for mutability
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public Game() {
        // no-op default constructor
    }

    public Game(com.bapppis.core.creature.Player player) {
        if (player == null)
            return;
        selectPlayer(player);
    }

    public void initialize() {
        loadDungeon();

        // System.out.println("Game initialized.");
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
                        if (cmd == null)
                            continue;
                        if (cmd.equalsIgnoreCase("__shutdown__"))
                            break;
                        commandParser.parseAndExecute(cmd);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        // System.out.println("[Game] Error executing command: " + e.getMessage());
                    }
                }
                running.set(false);
            }, "Game-Command-Loop");
            loop.setDaemon(true);
            loop.start();
        }
    }

    public void submitCommand(String command) {
        if (!running.get())
            return;
        if (command == null)
            return;
        commandQueue.offer(command);
    }

    public void shutdown() {
        if (!running.get())
            return;
        running.set(false);
        commandQueue.offer("__shutdown__");
    }

    private void loadDungeon() {
        dungeon = new Dungeon() {
        }; // Use anonymous subclass since Dungeon is abstract

        // Commented out text file loading - now using procedural generation
        /*
         * MapParser parser = new MapParser();
         * for (int i = -10; i <= 10; i++) {
         * String resourceName = "data/floors/floor" + i + "/floor" + i + ".txt";
         * try (InputStream is =
         * Game.class.getClassLoader().getResourceAsStream(resourceName)) {
         * if (is == null) {
         * // System.out.println("[Dungeon] Floor resource not found: " + resourceName);
         * continue;
         * }
         * Floor floor = parser.parseStream(is);
         * dungeon.addFloor(i, floor);
         * if (i == 0) {
         * GameState.setCurrentFloor(floor); // Start at floor 0
         * }
         * } catch (Exception e) {
         * // System.out.println("[Dungeon] Failed to load floor " + i + ": " +
         * e.getMessage());
         * }
         * }
         */

        // Procedural generation using BSPRoomGenerator
        BSPRoomGenerator generator = new BSPRoomGenerator();
        long baseSeed = System.currentTimeMillis(); // Random seed based on current time

        for (int i = -10; i <= 10; i++) {
            // Determine floor size based on depth
            int minSize, maxSize;
            if (i >= -4 && i <= 4) {
                // Floors 0 to ±4: 20x20 to 30x30
                minSize = 20;
                maxSize = 30;
            } else {
                // Floors ±5 to ±10: 30x30 to 40x40
                minSize = 30;
                maxSize = 40;
            }

            // Generate random size within range
            java.util.Random random = new java.util.Random(baseSeed + i);
            int width = minSize + random.nextInt(maxSize - minSize + 1);
            int height = minSize + random.nextInt(maxSize - minSize + 1);

            // Generate floor
            Floor floor = generator.generate(width, height, i, baseSeed + i);
            dungeon.addFloor(i, floor);

            if (i == 0) {
                GameState.setCurrentFloor(floor); // Start at floor 0
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
            // System.out.println("[Player] Creature id " + id + " is not a Player. Using
            // default player.");
            player = GameState.getPlayer();
        } else {
            // System.out.println("[Player] No creature found for id " + id + ". Using
            // default player.");
            player = GameState.getPlayer();
        }
        GameState.setPlayer(player);
    }

    public static void selectPlayer(Player player) {
        if (player == null) {
            // System.out.println("[Player] Given player is null. Using default player.");
            player = GameState.getPlayer();
        }
        GameState.setPlayer(player);
    }

    // Static helper to spawn the current player on the current floor and optionally
    // print the map
    public static void respawnPlayerOnCurrentFloor(boolean printAfter) {
        Floor floor = GameState.getCurrentFloor();
        if (floor == null) {
            // System.out.println("[Player] No floor loaded; cannot spawn.");
            return;
        }
        Player player = GameState.getPlayer();
        Coordinate spawn = null;
        for (var entry : floor.getTiles().entrySet()) {
            Tile t = entry.getValue();
            if (t.getSymbol() == '@' || t.getSymbol() == '.') {
                spawn = entry.getKey();
                if (t.getSymbol() == '@')
                    break; // prefer '@'
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
            // System.out.println("[Player] No suitable spawn found; player position
            // unset.");
        }
    }

    /**
     * Pass a turn: tick properties on all creatures on the current floor.
     * This includes the player and any NPCs/enemies.
     * Then trigger AI movement for all enemies on the floor.
     */
    public static void passTurn() {
        Floor floor = GameState.getCurrentFloor();
        if (floor == null)
            return;

        Player player = GameState.getPlayer();
        if (player != null) {
            player.tickProperties();
        }

        // Tick properties on all creatures occupying tiles on the current floor
        // (NPCs, enemies, etc.)
        if (floor.getTiles() != null) {
            for (Tile tile : floor.getTiles().values()) {
                if (tile != null && tile.getOccupants() != null) {
                    for (Creature occupant : tile.getOccupants()) {
                        // Skip player since we already ticked them
                        if (occupant != null && !(occupant instanceof Player)) {
                            occupant.tickProperties();
                        }
                    }
                }
            }
        }

        // AI movement for enemies
        if (floor.getTiles() != null) {
            for (Tile tile : floor.getTiles().values()) {
                if (tile != null && tile.getOccupants() != null) {
                    // Create a copy of occupants list to avoid concurrent modification
                    java.util.List<Creature> occupants = new java.util.ArrayList<>(tile.getOccupants());
                    for (Creature occupant : occupants) {
                        if (occupant instanceof com.bapppis.core.creature.Enemy) {
                            com.bapppis.core.creature.Enemy enemy = (com.bapppis.core.creature.Enemy) occupant;
                            enemy.takeAITurn();
                        }
                    }
                }
            }
        }
    }
}

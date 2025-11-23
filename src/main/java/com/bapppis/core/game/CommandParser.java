package com.bapppis.core.game;

/* import java.io.InputStream;
import com.bapppis.core.dungeon.mapparser.MapParser;
import com.bapppis.core.creature.player.Player; */
import java.util.*;

import com.bapppis.core.creature.Player;
import com.bapppis.core.dungeon.*;

public class CommandParser {
    private Map<String, Command> commandMap = new HashMap<>();

    public CommandParser(Dungeon dungeon, int[] currentFloorRef) {
        // Register commands using lowercase names
        commandMap.put("move", new MoveCommand());
        commandMap.put("attack", new AttackCommand());
        commandMap.put("map", new MapCommand());
        commandMap.put("player", new PlayerCommand());
        commandMap.put("look", new LookCommand());
        commandMap.put("up", new UpCommand(dungeon, currentFloorRef));
        commandMap.put("down", new DownCommand(dungeon, currentFloorRef));
        commandMap.put("wait", new WaitCommand());
        commandMap.put("pass", new WaitCommand()); // Alias for wait
        commandMap.put("interact", new InteractCommand());
        commandMap.put("e", new InteractCommand()); // Shortcut for interact
        // ... add more as needed
    }

    class UpCommand implements Command {
        private Dungeon dungeon;
        private int[] currentFloorRef;

        public UpCommand(Dungeon dungeon, int[] currentFloorRef) {
            this.dungeon = dungeon;
            this.currentFloorRef = currentFloorRef;
        }

        public void execute(String[] args) {
            Player player = GameState.getPlayer();
            Floor floor = dungeon.getFloor(currentFloorRef[0]);
            Tile playerTile = floor.getTile(player.getPosition());
            if (playerTile != null && playerTile.getSymbol() == '^') {
                // After floor 0, upstairs can only go up (positive floors)
                if (currentFloorRef[0] < 0) {
                    System.out.println("You cannot go up from here. You chose the downward path at floor 0.");
                    return;
                }
                if (currentFloorRef[0] < 10 && dungeon.getFloor(currentFloorRef[0] + 1) != null) {
                    currentFloorRef[0]++;
                    System.out.println("You ascend to floor " + currentFloorRef[0]);
                    Floor newFloor = dungeon.getFloor(currentFloorRef[0]);
                    // Remove player from previous floor occupants before switching floors
                    Floor oldFloor = GameState.getCurrentFloor();
                    if (oldFloor != null) {
                        Tile oldTile = oldFloor.getTile(player.getPosition());
                        if (oldTile != null) {
                            oldTile.getOccupants().remove(player);
                        }
                    }
                    GameState.setCurrentFloor(newFloor);
                    // Move player to downstairs 'v' on new floor (opposite of upstairs used)
                    Coordinate spawn = null;
                    for (var entry : newFloor.getTiles().entrySet()) {
                        if (entry.getValue().getSymbol() == 'v') {
                            spawn = entry.getKey();
                            break;
                        }
                    }
                    if (spawn != null) {
                        player.setPosition(spawn);
                        // Reset all tiles to undiscovered
                        for (Tile t : newFloor.getTiles().values()) {
                            t.setDiscovered(false);
                        }
                        // Reveal tiles around the player
                        newFloor.revealTilesWithVision(spawn.getX(), spawn.getY(), player.getVisionRange());
                        MapPrinter.printWithPlayer(newFloor, player);
                    } else {
                        System.out.println("No 'v' downstairs found on this floor. Position unchanged.");
                    }
                } else {
                    System.out.println("You can't go higher!");
                }
            } else {
                System.out.println("You must be standing on an up staircase (^) to go up.");
            }
        }
    }

    class DownCommand implements Command {
        private Dungeon dungeon;
        private int[] currentFloorRef;

        public DownCommand(Dungeon dungeon, int[] currentFloorRef) {
            this.dungeon = dungeon;
            this.currentFloorRef = currentFloorRef;
        }

        public void execute(String[] args) {
            Player player = GameState.getPlayer();
            Floor floor = dungeon.getFloor(currentFloorRef[0]);
            Tile playerTile = floor.getTile(player.getPosition());
            if (playerTile != null && playerTile.getSymbol() == 'v') {
                // After floor 0, downstairs can only go down (negative floors)
                if (currentFloorRef[0] > 0) {
                    System.out.println("You cannot go down from here. You chose the upward path at floor 0.");
                    return;
                }
                if (currentFloorRef[0] > -10 && dungeon.getFloor(currentFloorRef[0] - 1) != null) {
                    currentFloorRef[0]--;
                    System.out.println("You descend to floor " + currentFloorRef[0]);
                    Floor newFloor = dungeon.getFloor(currentFloorRef[0]);
                    // Remove player from previous floor occupants before switching floors
                    Floor oldFloor = GameState.getCurrentFloor();
                    if (oldFloor != null) {
                        Tile oldTile = oldFloor.getTile(player.getPosition());
                        if (oldTile != null) {
                            oldTile.getOccupants().remove(player);
                        }
                    }
                    GameState.setCurrentFloor(newFloor);
                    // Move player to upstairs '^' on new floor (opposite of downstairs used)
                    Coordinate spawn = null;
                    for (var entry : newFloor.getTiles().entrySet()) {
                        if (entry.getValue().getSymbol() == '^') {
                            spawn = entry.getKey();
                            break;
                        }
                    }
                    if (spawn != null) {
                        player.setPosition(spawn);
                        // Reset all tiles to undiscovered
                        for (Tile t : newFloor.getTiles().values()) {
                            t.setDiscovered(false);
                        }
                        // Reveal tiles around the player
                        newFloor.revealTilesWithVision(spawn.getX(), spawn.getY(), player.getVisionRange());
                        MapPrinter.printWithPlayer(newFloor, player);
                    } else {
                        System.out.println("No '^' upstairs found on this floor. Position unchanged.");
                    }
                } else {
                    System.out.println("You can't go lower!");
                }
            } else {
                System.out.println("You must be standing on a down staircase (v) to go down.");
            }
        }
    }

    public void parseAndExecute(String input) {
        if (input == null || input.trim().isEmpty())
            return;
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0)
            return;

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

class LookCommand implements Command {
    private static final String[] directions = {
            "north", "east", "south", "west"
    };
    private static final int[][] deltas = {
            { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 }
    };

    public void execute(String[] args) {
        Floor floor = GameState.getCurrentFloor();
        var player = GameState.getPlayer();
        if (floor == null || player.getPosition() == null) {
            System.out.println("No map or player loaded.");
            return;
        }
        int px = player.getX();
        int py = player.getY();
        if (args.length < 1) {
            // Print all directions
            for (int i = 0; i < directions.length; i++) {
                int dx = deltas[i][0];
                int dy = deltas[i][1];
                Coordinate coord = new Coordinate(px + dx, py + dy);
                Tile tile = floor.getTile(coord);
                String desc = describeTile(tile);
                System.out.println("To the " + directions[i] + " you see " + desc);
            }
        } else {
            String dir = args[0];
            int idx = -1;
            for (int i = 0; i < directions.length; i++) {
                if (dir.equals(directions[i])) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) {
                System.out.println("Unknown direction: " + dir);
                return;
            }
            int dx = deltas[idx][0];
            int dy = deltas[idx][1];
            Coordinate coord = new Coordinate(px + dx, py + dy);
            Tile tile = floor.getTile(coord);
            String desc = describeTile(tile);
            System.out.println("To the " + directions[idx] + " you see " + desc);
        }
    }

    private String describeTile(Tile tile) {
        if (tile == null) {
            return "nothing";
        }

        char sym = tile.getSymbol();
        switch (sym) {
            case '#':
                return "a wall";
            case '<':
                return "a breakable wall";
            case '.':
                return "the floor";
            case '@':
                return "a spawn point";
            case '^':
                return "an up staircase";
            case 'v':
                return "a down staircase";
            case '!':
                return "an event";
            case 'C':
                // Check if chest has loot
                if (tile.getLoot() != null) {
                    return "a treasure chest";
                } else {
                    return "an empty chest";
                }
            case '%':
                // Corpse - check if it has loot
                if (tile.getItems() != null && !tile.getItems().isEmpty()) {
                    return "a corpse with loot";
                } else {
                    return "an empty corpse";
                }
            case '*':
                // Dropped items
                if (tile.getItems() != null && !tile.getItems().isEmpty()) {
                    if (tile.getItems().size() == 1) {
                        return tile.getItems().get(0).getName() + " on the ground";
                    } else {
                        return "items on the ground (" + tile.getItems().size() + " items)";
                    }
                }
                return "something on the ground";
            case '+':
                return "a pit";
            case 'M':
                return "a monster";
            case 'P':
                return "a player";
            default:
                return "something";
        }
    }
}

class InteractCommand implements Command {
    private static final String[] directions = {
            "north", "east", "south", "west"
    };
    private static final int[][] deltas = {
            { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 }
    };

    public void execute(String[] args) {
        Floor floor = GameState.getCurrentFloor();
        var player = GameState.getPlayer();
        if (floor == null || player.getPosition() == null) {
            System.out.println("No map or player loaded.");
            return;
        }

        if (args.length < 1) {
            System.out.println("Usage: interact <direction> (or: e <direction>)");
            System.out.println("Directions: north, east, south, west (or n, e, s, w)");
            return;
        }

        String dir = args[0];
        int idx = -1;

        // Map direction names to indices
        for (int i = 0; i < directions.length; i++) {
            if (dir.equals(directions[i]) || dir.equals(directions[i].substring(0, 1))) {
                idx = i;
                break;
            }
        }

        if (idx == -1) {
            System.out.println("Unknown direction: " + dir);
            return;
        }

        int px = player.getX();
        int py = player.getY();
        int dx = deltas[idx][0];
        int dy = deltas[idx][1];
        Coordinate coord = new Coordinate(px + dx, py + dy);
        Tile tile = floor.getTile(coord);

        if (tile == null) {
            System.out.println("There's nothing to interact with there.");
            return;
        }

        char sym = tile.getSymbol();

        // Debug: Show what we're interacting with
        System.out.println(
                "[DEBUG] Interacting with tile at " + coord + ", symbol: '" + sym + "', sprite: " + tile.getSprite());
        if (tile.getLoot() != null) {
            System.out.println("[DEBUG] Tile has loot pool: " + tile.getLoot().name);
        }
        if (tile.getItems() != null && !tile.getItems().isEmpty()) {
            System.out.println("[DEBUG] Tile has " + tile.getItems().size() + " items");
        }

        // Handle corpses and dropped items
        if (sym == '%' || sym == '*') {
            if (tile.getItems() == null || tile.getItems().isEmpty()) {
                System.out.println("There's nothing to loot here.");
                return;
            }

            // Show the loot transfer dialog instead of console interaction
            GameState.showLootTransferDialog(coord);
            return;
        }

        // Handle chests
        if (sym == 'C') {
            System.out.println("[DEBUG] Chest detected at " + coord);
            com.bapppis.core.loot.LootPool lootPool = tile.getLoot();
            System.out.println(
                    "[DEBUG] Loot pool: " + (lootPool != null ? lootPool.name + " (ID: " + lootPool.id + ")" : "null"));
            if (lootPool == null) {
                System.out.println("This chest is empty.");
                return;
            }

            // Sample the loot pool and spawn items into the chest tile
            com.bapppis.core.loot.LootManager lootManager = new com.bapppis.core.loot.LootManager();
            lootManager.loadDefaults();
            lootManager.registerPool(lootPool);
            System.out.println("[DEBUG] Sampling loot pool: " + lootPool.id);
            java.util.List<com.bapppis.core.loot.LootManager.Spawn> spawns = lootManager.samplePool(lootPool.id);
            System.out.println("[DEBUG] Got " + (spawns != null ? spawns.size() : 0) + " spawns");

            if (spawns == null || spawns.isEmpty()) {
                System.out.println("The chest is empty.");
                return;
            }

            // Add items to the chest tile so the loot dialog can show them
            int itemsAdded = 0;
            for (com.bapppis.core.loot.LootManager.Spawn spawn : spawns) {
                System.out.println("[DEBUG] Spawn: type=" + spawn.type + ", id=" + spawn.id);
                if ("item".equalsIgnoreCase(spawn.type)) {
                    com.bapppis.core.item.Item item = null;
                    if (spawn.id != null) {
                        try {
                            int itemId = Integer.parseInt(spawn.id);
                            item = com.bapppis.core.item.ItemLoader.getItemById(itemId);
                        } catch (NumberFormatException e) {
                            item = com.bapppis.core.item.ItemLoader.getItemByName(spawn.id);
                        }
                    }
                    if (item != null) {
                        tile.getItems().add(item);
                        itemsAdded++;
                        System.out.println("[DEBUG] Added item: " + item.getName());
                    } else {
                        System.out.println("[DEBUG] Failed to load item: " + spawn.id);
                    }
                }
            }

            System.out.println("[DEBUG] Total items added to chest: " + itemsAdded);

            // Clear the loot pool so it doesn't spawn again
            tile.spawnTreasureChest(null);

            // Show the loot transfer dialog
            GameState.showLootTransferDialog(coord);
            return;
        }

        // Handle walls
        if (sym == '#' || sym == '<') {
            System.out.println("There's nothing to interact with this wall.");
            return;
        }

        // Handle floor
        if (sym == '.' || sym == '@') {
            System.out.println("There's nothing to interact here.");
            return;
        }

        // Handle stairs
        if (sym == '^' || sym == 'v') {
            System.out.println("Use 'up' or 'down' commands to use stairs.");
            return;
        }

        // Nothing interactive at this tile
        System.out.println("There's nothing to interact with there.");
    }
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
            case "right":
            case "r":
            case "east":
            case "e":
                dx = 1;
                dy = 0;
                break;
            case "left":
            case "l":
            case "west":
            case "w":
                dx = -1;
                dy = 0;
                break;
            case "up":
            case "u":
            case "north":
            case "n":
                dx = 0;
                dy = -1;
                break;
            case "down":
            case "d":
            case "south":
            case "s":
                dx = 0;
                dy = 1;
                break;
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

        // Check if player is over-encumbered
        if (player.getInventory().isOverEncumbered()) {
            System.out.println("You are over-encumbered and cannot move! Drop some items to continue.");
            System.out.println("Current load: " + player.getInventory().getCurrentLoad() + "/"
                    + player.getInventory().getMaxCapacity());
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

        // Check if tile is occupied
        if (next.isOccupied()) {
            return;
        }

        // Check if it's a wall (in case isOccupied isn't set properly)
        char sym = next.getSymbol();
        if (sym == '#' || sym == '<') {
            return;
        }

        player.setPosition(target);
        // System.out.println("You move " + dir + " to " + target);
        // Reveal tiles around the player after moving
        floor.revealTilesWithVision(target.getX(), target.getY(), player.getVisionRange());
        // MapPrinter.printWithPlayer(floor, player);

        // Check for adjacent enemies and trigger combat
        checkForAdjacentEnemies(floor, player);

        // Pass a turn after successful movement
        Game.passTurn();
    }

    private void checkForAdjacentEnemies(Floor floor, Player player) {
        if (player.getPosition() == null)
            return;

        int[][] directions = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

        for (int[] dir : directions) {
            int checkX = player.getX() + dir[0];
            int checkY = player.getY() + dir[1];
            Coordinate checkCoord = new Coordinate(checkX, checkY);
            Tile tile = floor.getTile(checkCoord);

            if (tile != null && !tile.getOccupants().isEmpty()) {
                for (var occupant : tile.getOccupants()) {
                    if (occupant instanceof com.bapppis.core.creature.Enemy) {
                        com.bapppis.core.creature.Enemy enemy = (com.bapppis.core.creature.Enemy) occupant;
                        GameState.setInCombat(true);
                        GameState.setCombatEnemy(enemy);
                        return;
                    }
                }
            }
        }
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

class WaitCommand implements Command {
    public void execute(String[] args) {
        System.out.println("You wait...");
        Game.passTurn();
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
            // Always use the main respawn logic so vision/fog is correct
            Game.respawnPlayerOnCurrentFloor(true);
        } catch (NumberFormatException nfe) {
            System.out.println("Player id must be a number.");
        }
    }
}
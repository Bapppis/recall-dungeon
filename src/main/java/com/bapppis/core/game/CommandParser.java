package com.bapppis.core.game;

/* import java.io.InputStream;
import com.bapppis.core.dungeon.mapparser.MapParser;
import com.bapppis.core.creature.player.Player; */
import java.util.*;

import com.bapppis.core.creature.player.Player;
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
            if (currentFloorRef[0] < 10 && dungeon.getFloor(currentFloorRef[0] + 1) != null) {
                currentFloorRef[0]++;
                System.out.println("You ascend to floor " + currentFloorRef[0]);
                Floor newFloor = dungeon.getFloor(currentFloorRef[0]);
                GameState.setCurrentFloor(newFloor);
                // Move player to '@' symbol on new floor
                Coordinate spawn = null;
                for (var entry : newFloor.getTiles().entrySet()) {
                    if (entry.getValue().getSymbol() == '@') {
                        spawn = entry.getKey();
                        break;
                    }
                }
                if (spawn != null) {
                    player.setPosition(spawn);
                    System.out.println("Player spawned at " + spawn);
                } else {
                    System.out.println("No '@' spawn found on this floor. Position unchanged.");
                }
                // Reveal tiles around the player as on spawn
                Game.respawnPlayerOnCurrentFloor(true);
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
            if (currentFloorRef[0] > -10 && dungeon.getFloor(currentFloorRef[0] - 1) != null) {
                currentFloorRef[0]--;
                System.out.println("You descend to floor " + currentFloorRef[0]);
                Floor newFloor = dungeon.getFloor(currentFloorRef[0]);
                GameState.setCurrentFloor(newFloor);
                // Move player to '@' symbol on new floor
                Coordinate spawn = null;
                for (var entry : newFloor.getTiles().entrySet()) {
                    if (entry.getValue().getSymbol() == '@') {
                        spawn = entry.getKey();
                        break;
                    }
                }
                if (spawn != null) {
                    player.setPosition(spawn);
                    System.out.println("Player spawned at " + spawn);
                } else {
                    System.out.println("No '@' spawn found on this floor. Position unchanged.");
                }
                // Reveal tiles around the player as on spawn
                Game.respawnPlayerOnCurrentFloor(true);
            } else {
                System.out.println("You can't go lower!");
            }
        } else {
            System.out.println("You must be standing on a down staircase (v) to go down.");
        }
    }
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

class LookCommand implements Command {
    private static final String[] directions = {
        "north", "east", "south", "west"
    };
    private static final int[][] deltas = {
        {0, -1}, {1, 0}, {0, 1}, {-1, 0}
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
                String desc;
                if (tile == null) {
                    desc = "nothing";
                } else {
                    char sym = tile.getSymbol();
                    switch (sym) {
                        case '#': desc = "a wall"; break;
                        case '.': desc = "the floor"; break;
                        case '<': desc = "stairs up"; break;
                        case '>': desc = "stairs down"; break;
                        case '@': desc = "a spawn point"; break;
                        default:
                            desc = "something";
                            break;
                    }
                }
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
            String desc;
            if (tile == null) {
                desc = "nothing";
            } else {
                char sym = tile.getSymbol();
                switch (sym) {
                    case '#': desc = "a wall"; break;
                    case '<': desc = "a breakable wall"; break;
                    case '.': desc = "the floor"; break;
                    case '@': desc = "a spawn point"; break;
                    case '^': desc = "an up staircase"; break;
                    case 'v': desc = "a down staircase"; break;
                    case '!': desc = "an event"; break;
                    case 'C': desc = "a chest"; break;
                    case '+': desc = "a pit"; break;
                    case 'M': desc = "a monster"; break;
                    case 'P': desc = "a player"; break;
                    default:
                        desc = "something";
                        break;
                }
            }
            System.out.println("To the " + directions[idx] + " you see " + desc);
        }
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
            case "right": case "r": case "east": case "e":
                dx = 1; dy = 0; break;
            case "left": case "l": case "west": case "w":
                dx = -1; dy = 0; break;
            case "up": case "u": case "north": case "n":
                dx = 0; dy = -1; break;
            case "down": case "d": case "south": case "s":
                dx = 0; dy = 1; break;
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
    // System.out.println("You move " + dir + " to " + target);
    // Reveal tiles around the player after moving
    floor.revealTilesWithVision(target.getX(), target.getY(), player.getVisionRange());
    // MapPrinter.printWithPlayer(floor, player);
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
            // Always use the main respawn logic so vision/fog is correct
            Game.respawnPlayerOnCurrentFloor(true);
        } catch (NumberFormatException nfe) {
            System.out.println("Player id must be a number.");
        }
    }
}
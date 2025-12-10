package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Type;
import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.util.LevelUtil;

import java.util.ArrayList;
import java.util.List;

public class Enemy extends Creature {
    private Integer enemyXp;
    private Coordinate position;

    // AI state tracking
    private EnemyAIState aiState = EnemyAIState.PATROLLING;
    private Coordinate lastSeenPlayerPosition = null;
    private List<Coordinate> patrolRoute = new ArrayList<>();
    private int patrolIndex = 0;
    private int alertCooldown = 0; // Turns remaining in ALERT state
    private int stuckCounter = 0; // Consecutive turns unable to move toward waypoint
    private Coordinate spawnPosition = null; // Enemy's original spawn position for route regeneration
    private int routeCompletions = 0; // Number of times enemy has completed full patrol route

    public Enemy() {
        this.setType(Type.ENEMY);
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        // Update tile occupancy: remove from old tile occupants, add to new tile
        // occupants
        try {
            com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState.getCurrentFloor();
            if (floor != null) {
                // Remove from previous tile occupants
                if (this.position != null) {
                    com.bapppis.core.dungeon.Tile old = floor.getTile(this.position);
                    if (old != null) {
                        old.getOccupants().remove(this);
                    }
                }
                // Set new position
                this.position = position;
                // Add to new tile occupants
                if (this.position != null) {
                    com.bapppis.core.dungeon.Tile now = floor.getTile(this.position);
                    if (now != null && !now.getOccupants().contains(this)) {
                        now.getOccupants().add(this);
                    }
                }
                return;
            }
        } catch (Exception ignored) {
            // Fall back to simple set if anything goes wrong
        }
        this.position = position;
    }

    public void setPosition(int x, int y) {
        setPosition(new Coordinate(x, y));
    }

    public int getX() {
        return position != null ? position.getX() : -1;
    }

    public int getY() {
        return position != null ? position.getY() : -1;
    }

    public EnemyAIState getAIState() {
        return aiState;
    }

    public void setAIState(EnemyAIState state) {
        this.aiState = state;
    }

    public List<Coordinate> getPatrolRoute() {
        return patrolRoute;
    }

    /**
     * Initialize a simple patrol route for this enemy.
     * Creates a route with 2-4 random waypoints within a radius of the spawn
     * position.
     * 
     * @param spawnPosition the enemy's initial spawn position
     * @param patrolRadius  maximum distance from spawn for patrol waypoints
     *                      (typically 3-5)
     */
    public void initializePatrolRoute(Coordinate spawnPosition, int patrolRadius) {
        System.out.println("DEBUG InitPatrol: Initializing for " + getName() + " at " + spawnPosition + ", radius="
                + patrolRadius);
        this.spawnPosition = spawnPosition; // Store spawn for route regeneration
        patrolRoute.clear();
        patrolIndex = 0;

        com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState.getCurrentFloor();
        if (floor == null || spawnPosition == null) {
            System.out.println("  Floor or spawn is null!");
            return;
        }

        // Generate 2-4 waypoints around spawn (don't include spawn itself to avoid
        // teleporting)
        java.util.Random random = new java.util.Random();
        int waypointCount = 2 + random.nextInt(3); // 2-4 waypoints
        System.out.println("  Attempting " + waypointCount + " waypoints");

        for (int i = 0; i < waypointCount; i++) {
            // Try to find a valid waypoint within patrol radius
            for (int attempt = 0; attempt < 10; attempt++) {
                int offsetX = random.nextInt(patrolRadius * 2 + 1) - patrolRadius;
                int offsetY = random.nextInt(patrolRadius * 2 + 1) - patrolRadius;

                int wpX = spawnPosition.getX() + offsetX;
                int wpY = spawnPosition.getY() + offsetY;

                Coordinate waypoint = new Coordinate(wpX, wpY);
                com.bapppis.core.dungeon.Tile tile = floor.getTile(waypoint);

                // Check if tile is valid (exists, not a wall)
                // Note: We allow occupied tiles since the enemy itself occupies spawn
                if (tile != null && !tile.isWall()) {
                    // Make sure it's not already in route and not spawn position
                    boolean isDuplicate = spawnPosition.equals(waypoint);
                    for (Coordinate existing : patrolRoute) {
                        if (existing.equals(waypoint)) {
                            isDuplicate = true;
                            break;
                        }
                    }

                    // Check if waypoint is reachable (has line of sight from spawn)
                    boolean isReachable = floor.hasLineOfSight(spawnPosition.getX(), spawnPosition.getY(),
                            wpX, wpY, false);

                    if (!isDuplicate && isReachable) {
                        patrolRoute.add(waypoint);
                        System.out.println("  Added waypoint " + (i + 1) + ": " + waypoint);
                        break;
                    } else if (!isDuplicate && !isReachable) {
                        System.out.println("  Skipped " + waypoint + " (blocked by walls)");
                    }
                }
            }
        }

        // If we couldn't find enough waypoints, try cardinal directions
        if (patrolRoute.size() < 2) {
            System.out.println("  Only " + patrolRoute.size() + " waypoint(s), trying cardinal directions...");
            // Try to add waypoints in cardinal directions
            int[][] directions = { { 0, -2 }, { 2, 0 }, { 0, 2 }, { -2, 0 } };
            for (int[] dir : directions) {
                if (patrolRoute.size() >= 2)
                    break; // Got enough waypoints

                int wpX = spawnPosition.getX() + dir[0];
                int wpY = spawnPosition.getY() + dir[1];
                Coordinate waypoint = new Coordinate(wpX, wpY);
                com.bapppis.core.dungeon.Tile tile = floor.getTile(waypoint);

                if (tile != null && !tile.isWall() && !spawnPosition.equals(waypoint)) {
                    boolean isDuplicate = false;
                    for (Coordinate existing : patrolRoute) {
                        if (existing.equals(waypoint)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        patrolRoute.add(waypoint);
                        System.out.println("  Added cardinal waypoint: " + waypoint);
                    }
                }
            }
        }
        System.out.println("  Final patrol route size: " + patrolRoute.size());
    }

    /**
     * Take an AI turn based on current state.
     *
     * PATROLLING: Follow patrol route, check for player in vision range
     * CHASING: Actively pursue player while maintaining line of sight
     * INVESTIGATING: Move to last seen player position
     * ALERT: Briefly look around before resuming patrol
     *
     * Does not move during combat.
     * If adjacent to player (distance 1), initiates combat instead of moving.
     */
    public void takeAITurn() {
        // Don't move during combat
        if (com.bapppis.core.game.GameState.isInCombat()) {
            return;
        }

        // Check if enemy has a position
        if (position == null) {
            return;
        }

        // Get current floor and player
        com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState.getCurrentFloor();
        com.bapppis.core.creature.Player player = com.bapppis.core.game.GameState.getPlayer();

        if (floor == null || player == null || player.getPosition() == null) {
            return;
        }

        // Check if player is within vision range and has line of sight
        int distanceToPlayer = Coordinate.manhattanDistance(this.position, player.getPosition());
        boolean canSeePlayer = distanceToPlayer <= this.getVisionRange()
                && floor.hasLineOfSight(position.getX(), position.getY(),
                        player.getPosition().getX(), player.getPosition().getY(), true);

        // If adjacent to player, start combat regardless of state
        if (distanceToPlayer == 1) {
            com.bapppis.core.game.GameState.setInCombat(true);
            com.bapppis.core.game.GameState.setCombatEnemy(this);
            return;
        }

        // State machine logic
        switch (aiState) {
            case PATROLLING:
                handlePatrolling(floor, player, canSeePlayer);
                break;

            case CHASING:
                handleChasing(floor, player, canSeePlayer);
                break;

            case INVESTIGATING:
                handleInvestigating(floor, player, canSeePlayer);
                break;

            case ALERT:
                handleAlert(floor, player, canSeePlayer);
                break;
        }
    }

    /**
     * PATROLLING state: Move along patrol route, check for player
     */
    private void handlePatrolling(com.bapppis.core.dungeon.Floor floor,
            com.bapppis.core.creature.Player player, boolean canSeePlayer) {

        if (canSeePlayer) {
            // Spotted player! Switch to chasing
            aiState = EnemyAIState.CHASING;
            lastSeenPlayerPosition = new Coordinate(player.getPosition().getX(), player.getPosition().getY());
            routeCompletions = 0; // Reset completion counter when leaving patrol
            return;
        }

        // Continue patrolling
        System.out.println("DEBUG Patrol: Enemy " + getName() + " at " + position);
        System.out.println("  Patrol route size: " + patrolRoute.size());

        if (patrolRoute.isEmpty()) {
            // No patrol route - initialize one now
            System.out.println("  Route empty, initializing...");
            if (position != null) {
                initializePatrolRoute(position, 4);
                System.out.println("  After init, route size: " + patrolRoute.size());
            }
            return;
        }

        Coordinate targetWaypoint = patrolRoute.get(patrolIndex);
        System.out.println("  Current waypoint index: " + patrolIndex + ", target: " + targetWaypoint);
        System.out.println("  Route completions: " + routeCompletions);

        // Move toward target waypoint first
        if (!position.equals(targetWaypoint)) {
            Coordinate positionBefore = new Coordinate(position.getX(), position.getY());
            System.out.println("  Moving toward " + targetWaypoint + " (distance: "
                    + Coordinate.manhattanDistance(position, targetWaypoint) + ")");
            moveToward(floor, targetWaypoint);

            // Check if we actually moved
            if (position.equals(positionBefore)) {
                // Stuck! Increment counter
                stuckCounter++;
                System.out.println("  Stuck counter: " + stuckCounter + "/5");

                if (stuckCounter >= 5) {
                    // Waypoint is unreachable, skip to next
                    System.out.println("  Waypoint unreachable after 5 attempts, skipping to next");
                    int oldIndex = patrolIndex;
                    patrolIndex = (patrolIndex + 1) % patrolRoute.size();
                    stuckCounter = 0;
                    
                    // Check if we just completed a full cycle by wrapping around
                    if (oldIndex > patrolIndex) {
                        routeCompletions++;
                        System.out.println("  Route completion #" + routeCompletions + " (from stuck skip)");
                        
                        // Regenerate route after 2 completions
                        if (routeCompletions >= 2 && spawnPosition != null) {
                            System.out.println("  Regenerating patrol route (2 completions reached)");
                            initializePatrolRoute(spawnPosition, 4);
                            routeCompletions = 0;
                        }
                    }
                }
            } else {
                // Successfully moved, reset stuck counter
                stuckCounter = 0;

                // After moving, check if we've now reached the waypoint
                if (position.equals(targetWaypoint)) {
                    int oldIndex = patrolIndex;
                    patrolIndex = (patrolIndex + 1) % patrolRoute.size();
                    System.out.println("  Reached waypoint! Next target: " + patrolRoute.get(patrolIndex));
                    
                    // Check if we just completed a full cycle by wrapping to index 0
                    if (oldIndex > patrolIndex) {
                        routeCompletions++;
                        System.out.println("  Route completion #" + routeCompletions);
                        
                        // Regenerate route after 2 completions
                        if (routeCompletions >= 2 && spawnPosition != null) {
                            System.out.println("  Regenerating patrol route (2 completions reached)");
                            initializePatrolRoute(spawnPosition, 4);
                            routeCompletions = 0;
                        }
                    }
                }
            }
        } else {
            // Already at waypoint, advance to next
            int oldIndex = patrolIndex;
            patrolIndex = (patrolIndex + 1) % patrolRoute.size();
            stuckCounter = 0;
            System.out.println("  At waypoint, advancing to next: " + patrolRoute.get(patrolIndex));
            
            // Check if we just completed a full cycle by wrapping to index 0
            if (oldIndex > patrolIndex) {
                routeCompletions++;
                System.out.println("  Route completion #" + routeCompletions);
                
                // Regenerate route after 2 completions
                if (routeCompletions >= 2 && spawnPosition != null) {
                    System.out.println("  Regenerating patrol route (2 completions reached)");
                    initializePatrolRoute(spawnPosition, 4);
                    routeCompletions = 0;
                }
            }
        }
    }

    /**
     * CHASING state: Actively pursue player while maintaining line of sight
     */
    private void handleChasing(com.bapppis.core.dungeon.Floor floor,
            com.bapppis.core.creature.Player player, boolean canSeePlayer) {

        if (canSeePlayer) {
            // Update last seen position and chase
            lastSeenPlayerPosition = new Coordinate(player.getPosition().getX(), player.getPosition().getY());
            moveToward(floor, player.getPosition());
        } else {
            // Lost sight of player - switch to investigating
            aiState = EnemyAIState.INVESTIGATING;
            // lastSeenPlayerPosition already set from last time we saw them
        }
    }

    /**
     * INVESTIGATING state: Move to last known player position
     */
    private void handleInvestigating(com.bapppis.core.dungeon.Floor floor,
            com.bapppis.core.creature.Player player, boolean canSeePlayer) {

        if (canSeePlayer) {
            // Found player again! Resume chasing
            aiState = EnemyAIState.CHASING;
            lastSeenPlayerPosition = new Coordinate(player.getPosition().getX(), player.getPosition().getY());
            return;
        }

        // Move toward last seen position
        if (lastSeenPlayerPosition != null) {
            if (position.equals(lastSeenPlayerPosition)) {
                // Reached last known position but player isn't here
                aiState = EnemyAIState.ALERT;
                alertCooldown = 2; // Look around for 2 turns
            } else {
                moveToward(floor, lastSeenPlayerPosition);
            }
        } else {
            // No last seen position? Resume patrolling
            aiState = EnemyAIState.PATROLLING;
        }
    }

    /**
     * ALERT state: Briefly look around before resuming patrol
     */
    private void handleAlert(com.bapppis.core.dungeon.Floor floor,
            com.bapppis.core.creature.Player player, boolean canSeePlayer) {

        if (canSeePlayer) {
            // Spotted player during alert! Resume chasing
            aiState = EnemyAIState.CHASING;
            lastSeenPlayerPosition = new Coordinate(player.getPosition().getX(), player.getPosition().getY());
            alertCooldown = 0;
            return;
        }

        // Count down alert cooldown
        alertCooldown--;

        if (alertCooldown <= 0) {
            // Alert time expired, resume patrol
            aiState = EnemyAIState.PATROLLING;
            lastSeenPlayerPosition = null;
        }
        // Don't move during alert - just stand still and look around
    }

    /**
     * Move one step toward target position using greedy pathfinding.
     * Chooses the adjacent tile that minimizes Manhattan distance to target.
     */
    private void moveToward(com.bapppis.core.dungeon.Floor floor, Coordinate target) {
        Coordinate oldPosition = new Coordinate(position.getX(), position.getY());
        int bestX = position.getX();
        int bestY = position.getY();
        int bestDistance = Coordinate.manhattanDistance(position, target);

        // Check all four adjacent directions: north, east, south, west
        int[][] directions = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
        String[] dirNames = { "North", "East", "South", "West" };

        for (int i = 0; i < directions.length; i++) {
            int[] dir = directions[i];
            int newX = position.getX() + dir[0];
            int newY = position.getY() + dir[1];
            Coordinate candidate = new Coordinate(newX, newY);

            // Calculate distance from this candidate to target
            int candidateDistance = Coordinate.manhattanDistance(candidate, target);

            com.bapppis.core.dungeon.Tile tile = floor.getTile(candidate);

            // Debug: why can't we move here?
            if (candidateDistance < bestDistance) {
                if (tile == null) {
                    System.out.println("      " + dirNames[i] + " " + candidate + ": no tile");
                } else if (tile.isWall()) {
                    System.out.println("      " + dirNames[i] + " " + candidate + ": WALL");
                } else if (tile.isOccupied()) {
                    System.out
                            .println("      " + dirNames[i] + " " + candidate + ": OCCUPIED by " + tile.getOccupants());
                } else {
                    System.out
                            .println("      " + dirNames[i] + " " + candidate + ": OK, distance=" + candidateDistance);
                    bestX = newX;
                    bestY = newY;
                    bestDistance = candidateDistance;
                }
            }
        }

        // Move if we found a better position
        if (bestX != position.getX() || bestY != position.getY()) {
            setPosition(bestX, bestY);
            System.out.println("    Moved from " + oldPosition + " to " + position);
        } else {
            System.out.println("    Could not move from " + position + " (all tiles blocked or no improvement)");
        }
    }

    public Integer getEnemyXp() {
        return enemyXp;
    }

    public void setEnemyXp(Integer enemyXp) {
        this.enemyXp = enemyXp;
    }

    @Override
    public String toString() {
        // Detailed enemy printout (combat-relevant info)
        StringBuilder sb = new StringBuilder();
        sb.append("Enemy: ").append(getName() == null ? "<unnamed>" : getName()).append(" (Id:").append(getId())
                .append(")\n");
        sb.append("Level: ").append(getLevel()).append(" ")
                .append(getSize() == null ? "" : getSize().name()).append(" ");

        // Print species if available (not base types)
        String species = getClass().getSimpleName();
        if (!species.equals("Player") && !species.equals("Enemy") && !species.equals("NPC")
                && !species.equals("CreatureType") && !species.equals("Creature")) {
            sb.append(species.toUpperCase());
        } else if (getCreatureType() != null) {
            sb.append(getCreatureType().name());
        }
        sb.append("\n");

        sb.append("XP Reward: ").append(enemyXp != null ? enemyXp : "N/A").append("\n");
        sb.append("Vision Range: ").append(getVisionRange()).append("\n");
        sb.append("HP: ").append(getCurrentHp()).append("/").append(getMaxHp()).append("\n");
        sb.append("Mana: ").append(getCurrentMana()).append("/").append(getMaxMana()).append("\n");
        sb.append("Stamina: ").append(getCurrentStamina()).append("/").append(getMaxStamina()).append("\n");
        sb.append("Stats: ")
                .append("STR ").append(getSTR()).append("  ")
                .append("DEX ").append(getDEX()).append("  ")
                .append("CON ").append(getCON()).append("  ")
                .append("INT ").append(getINT()).append("  ")
                .append("WIS ").append(getWIS()).append("  ")
                .append("CHA ").append(getCHA()).append("  ")
                .append("LUCK ").append(getLUCK()).append("\n");
        sb.append("Resists: ");
        boolean first = true;
        for (Resistances res : Resistances.values()) {
            if (!first)
                sb.append(", ");
            sb.append(res.name()).append("=").append(getResistance(res)).append("%");
            first = false;
        }
        sb.append("\n");
        sb.append("Crit: ").append(getCrit()).append("%  ")
                .append("Dodge: ").append(getDodge()).append("%  ")
                .append("Block: ").append(getBlock()).append("%  ")
                .append("MagicResist: ").append(getMagicResist()).append("%\n");
        sb.append("Accuracy: ").append(getAccuracy()).append("  ")
                .append("MagicAccuracy: ").append(getMagicAccuracy()).append("\n");
        sb.append("Equipment:\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item equipped = getEquipped(slot);
            sb.append("  ").append(slot.name()).append(": ");
            sb.append(equipped == null ? "Empty" : equipped.getName()).append("\n");
        }
        sb.append("Properties: ")
                .append("Buffs=").append(getBuffs().size()).append(" ")
                .append("Debuffs=").append(getDebuffs().size()).append(" ")
                .append("Traits=").append(getTraits().size()).append("\n");
        return sb.toString();
    }
}

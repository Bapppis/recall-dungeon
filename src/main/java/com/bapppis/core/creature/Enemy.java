package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Type;
import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.util.LevelUtil;

public class Enemy extends Creature {
    private Integer enemyXp;
    private Coordinate position;

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

    /**
     * Take an AI turn: if player is within vision range, move one step toward them.
     * Uses greedy pathfinding: chooses the adjacent tile that minimizes Manhattan
     * distance to player.
     * Only moves if the target tile is walkable and unoccupied.
     * Does not move during combat.
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

        // Check if player is within vision range
        int distance = Coordinate.manhattanDistance(this.position, player.getPosition());
        if (distance > this.getVisionRange()) {
            return; // Player not in vision range
        }

        // Check line of sight - enemies can't see through occupied tiles (walls, chests, etc.)
        if (!floor.hasLineOfSight(position.getX(), position.getY(), 
                                   player.getPosition().getX(), player.getPosition().getY(), true)) {
            return; // No line of sight to player
        }

        // Find the best adjacent tile to move toward player
        int bestX = position.getX();
        int bestY = position.getY();
        int bestDistance = distance;

        // Check all four adjacent directions: north, east, south, west
        int[][] directions = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

        for (int[] dir : directions) {
            int newX = position.getX() + dir[0];
            int newY = position.getY() + dir[1];
            Coordinate candidate = new Coordinate(newX, newY);

            // Calculate distance from this candidate to player
            int candidateDistance = Coordinate.manhattanDistance(candidate, player.getPosition());

            // Check if this tile is better (closer to player)
            if (candidateDistance < bestDistance) {
                com.bapppis.core.dungeon.Tile tile = floor.getTile(candidate);

                // Check if tile exists and is walkable (not wall, not occupied)
                if (tile != null && !tile.isWall() && !tile.isOccupied()) {
                    bestX = newX;
                    bestY = newY;
                    bestDistance = candidateDistance;
                }
            }
        }

        // Move if we found a better position
        if (bestX != position.getX() || bestY != position.getY()) {
            setPosition(bestX, bestY);
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

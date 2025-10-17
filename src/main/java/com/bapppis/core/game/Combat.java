
package com.bapppis.core.game;

import java.util.Scanner;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.util.DebugLog;

public class Combat {
    // Quick local toggle example (uncomment to enable debug output for this class):
    // DebugLog.DEBUG = true; // <-- uncomment during local debugging to see Combat debug
    @SuppressWarnings("resource")
    public static void startCombat(Creature player, Creature enemy) {
        // Default interactive combat (keeps existing behavior)
        DebugLog.debug("Combat starts: " + player.getName() + " vs " + enemy.getName());
        Scanner scanner = new Scanner(System.in);
        while (player.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
            // Player's turn: prompt for action
            String input = "";
            while (true) {
                System.out.print("Your turn! Type 'attack' or 'flee': ");
                input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("attack") || input.equals("flee")) {
                    break;
                } else {
                    System.out.println("Unknown action. Please type 'attack' or 'flee'.");
                }
            }
            if (input.equals("flee")) {
                System.out.println("You fled from combat!");
                break;
            } else if (input.equals("attack")) {
                player.attack(enemy);
            }
            if (enemy.getCurrentHp() <= 0) {
                DebugLog.debug(enemy.getName() + " is defeated!");
                handleEnemyDefeated(player, enemy);
                break;
            }

            // Enemy's turn (if not dead)
            enemy.attack(player);
            if (player.getCurrentHp() <= 0) {
                DebugLog.debug(player.getName() + " is defeated!");
                break;
            }

            // Print status after each round
            DebugLog.debug(player.getName() + ": " + player.getCurrentHp() + " HP");
            DebugLog.debug(enemy.getName() + ": " + enemy.getCurrentHp() + " HP");
        }
        DebugLog.debug("Combat ends.");
    }

    /**
     * Non-interactive combat mode: player automatically attacks each round.
     * Useful for tests and simulations.
     */
    public static void startCombat(Creature player, Creature enemy, boolean autoAttack) {
        if (!autoAttack) {
            startCombat(player, enemy);
            return;
        }
        DebugLog.debug("Combat starts (auto): " + player.getName() + " vs " + enemy.getName());
        while (player.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
            // Player auto-attacks
            player.attack(enemy);
            if (enemy.getCurrentHp() <= 0) {
                DebugLog.debug(enemy.getName() + " is defeated!");
                handleEnemyDefeated(player, enemy);
                break;
            }

            // Enemy's turn (if not dead)
            enemy.attack(player);
            if (player.getCurrentHp() <= 0) {
                DebugLog.debug(player.getName() + " is defeated!");
                break;
            }
        }
        DebugLog.debug("Combat ends.");
    }

    private static void handleEnemyDefeated(Creature player, Creature enemy) {
        // Transfer enemyXp from enemy to player by adding to player's XP
        try {
            Integer enemyXp = enemy.getEnemyXp();
            if (enemyXp != null && enemyXp > 0) {
                player.addXp(enemyXp);
                DebugLog.debug(player.getName() + " gains " + enemyXp + " XP (now " + player.getXp() + ").");
            }
        } catch (Exception ignored) {
        }
    }
}

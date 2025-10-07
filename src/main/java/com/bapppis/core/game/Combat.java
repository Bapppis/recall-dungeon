
package com.bapppis.core.game;

import java.util.Scanner;

import com.bapppis.core.creature.Creature;

public class Combat {
    public static void startCombat(Creature player, Creature enemy) {
        // Default interactive combat (keeps existing behavior)
        System.out.println("Combat starts: " + player.getName() + " vs " + enemy.getName());
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
                System.out.println(enemy.getName() + " is defeated!");
                handleEnemyDefeated(player, enemy);
                break;
            }

            // Enemy's turn (if not dead)
            enemy.attack(player);
            if (player.getCurrentHp() <= 0) {
                System.out.println(player.getName() + " is defeated!");
                break;
            }

            // Print status after each round
            System.out.println(player.getName() + ": " + player.getCurrentHp() + " HP");
            System.out.println(enemy.getName() + ": " + enemy.getCurrentHp() + " HP");
        }
        System.out.println("Combat ends.");
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
        System.out.println("Combat starts (auto): " + player.getName() + " vs " + enemy.getName());
        while (player.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
            // Player auto-attacks
            player.attack(enemy);
            if (enemy.getCurrentHp() <= 0) {
                System.out.println(enemy.getName() + " is defeated!");
                handleEnemyDefeated(player, enemy);
                break;
            }

            // Enemy's turn (if not dead)
            enemy.attack(player);
            if (player.getCurrentHp() <= 0) {
                System.out.println(player.getName() + " is defeated!");
                break;
            }
        }
        System.out.println("Combat ends.");
    }

    private static void handleEnemyDefeated(Creature player, Creature enemy) {
        // Transfer enemyXp from enemy to player by adding to player's XP
        try {
            Integer enemyXp = enemy.getEnemyXp();
            if (enemyXp != null && enemyXp > 0) {
                player.addXp(enemyXp);
                System.out.println(player.getName() + " gains " + enemyXp + " XP (now " + player.getXp() + ").");
            }
        } catch (Exception ignored) {
        }
    }
}

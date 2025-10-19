
package com.bapppis.core.game;

import java.util.Scanner;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.Enemy;
import com.bapppis.core.creature.Player;
import com.bapppis.core.util.DebugLog;

public class Combat {
    @SuppressWarnings("resource")
    public static void startCombat(Creature player, Creature enemy) {
        DebugLog.debug("Combat starts: " + player.getName() + " vs " + enemy.getName());
        Scanner scanner = new Scanner(System.in);

        while (player.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
            String input = "";
            while (true) {
                System.out.print("Your turn! Type 'attack' or 'flee': ");
                input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("attack") || input.equals("flee")) {
                    break;
                }
                System.out.println("Unknown action. Please type 'attack' or 'flee'.");
            }

            if (input.equals("flee")) {
                System.out.println("You fled from combat!");
                break;
            }

            player.attack(enemy);
            if (enemy.getCurrentHp() <= 0) {
                DebugLog.debug(enemy.getName() + " is defeated!");
                handleEnemyDefeated(player, enemy);
                break;
            }

            enemy.attack(player);
            if (player.getCurrentHp() <= 0) {
                DebugLog.debug(player.getName() + " is defeated!");
                break;
            }

            DebugLog.debug(player.getName() + ": " + player.getCurrentHp() + " HP");
            DebugLog.debug(enemy.getName() + ": " + enemy.getCurrentHp() + " HP");
        }
        DebugLog.debug("Combat ends.");
    }

    public static void startCombat(Creature player, Creature enemy, boolean autoAttack) {
        if (!autoAttack) {
            startCombat(player, enemy);
            return;
        }

        DebugLog.debug("Combat starts (auto): " + player.getName() + " vs " + enemy.getName());
        while (player.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
            player.attack(enemy);
            if (enemy.getCurrentHp() <= 0) {
                DebugLog.debug(enemy.getName() + " is defeated!");
                handleEnemyDefeated(player, enemy);
                break;
            }

            enemy.attack(player);
            if (player.getCurrentHp() <= 0) {
                DebugLog.debug(player.getName() + " is defeated!");
                break;
            }
        }
        DebugLog.debug("Combat ends.");
    }

    private static void handleEnemyDefeated(Creature player, Creature enemy) {
        if (player instanceof Player && enemy instanceof Enemy) {
            Integer enemyXp = ((Enemy) enemy).getEnemyXp();
            if (enemyXp != null && enemyXp > 0) {
                ((Player) player).addXp(enemyXp);
                DebugLog.debug(player.getName() + " gains " + enemyXp + " XP (now " + ((Player) player).getXp() + ").");
            }
        }
    }
}

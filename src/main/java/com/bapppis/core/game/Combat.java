
package com.bapppis.core.game;

import java.util.Scanner;

import com.bapppis.core.creature.Creature;

public class Combat {
    public static void startCombat(Creature player, Creature enemy) {
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
}

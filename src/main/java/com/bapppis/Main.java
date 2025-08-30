package com.bapppis;

import com.bapppis.core.game.Game;

public class Main {
    public static void main(String[] args) {
        System.out.println("---------------------------------------------------------");
        System.out.println("Welcome to the world of Aurum!");
        System.out.println("---------------------------------------------------------");

        // Initialize and start the game
        Game game = new Game();
        game.initialize();
    }
}
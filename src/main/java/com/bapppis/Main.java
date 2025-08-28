package com.bapppis;

import com.bapppis.core.game.Game;

public class Main {
    // Quick property loading test for production assets
    public static void testPropertyLoading() {
        com.bapppis.core.property.PropertyManager.loadProperties();
        com.bapppis.core.property.Property property4001 = com.bapppis.core.property.PropertyManager.getProperty(4001);
        if (property4001 != null) {
            System.out.println("[PropertyManager] Property 4001: " + property4001.getName());
        } else {
            System.out.println("[PropertyManager] Property 4001 not found.");
        }
    }
    public static void main(String[] args) {
        System.out.println("---------------------------------------------------------");
        System.out.println("Welcome to the world of Aurum!");
        System.out.println("---------------------------------------------------------");

    // Run property loading test
    testPropertyLoading();

    Game game = new Game();
    game.initialize();
    }
}
package com.bapppis;

import com.bapppis.core.entities.Goblin;

public class Main {
    public static void main(String[] args) {
        System.out.println("---------------------------------------------------------");
        System.out.println("Welcome to the world of Aurum!");
        System.out.println("---------------------------------------------------------");
        System.out.println("Creating a new Goblin...");
        Goblin goblin = new Goblin();
        System.out.println("Goblin created: \n" + goblin);
    }
}
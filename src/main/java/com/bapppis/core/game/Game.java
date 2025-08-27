package com.bapppis.core.game;

import java.util.Scanner;

public class Game {

    public void initialize() {
        System.out.println("Game initialized.");
        try (Scanner scanner = new Scanner(System.in)) {
            CommandParser commandParser = new CommandParser();
            while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            commandParser.parseAndExecute(input);
            }
        }
    }
}

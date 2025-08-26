package com.bapppis;

import java.io.InputStream;
import java.util.Scanner;

import com.bapppis.core.dungeon.mapparser.MapParser;
import com.bapppis.core.game.CommandParser;

public class Main {
    public static void main(String[] args) {
        System.out.println("---------------------------------------------------------");
        System.out.println("Welcome to the world of Aurum!");
        System.out.println("---------------------------------------------------------");

        /*MapParser mapParser = new MapParser();
        String resourceName = "floor(20x20).txt";
        InputStream is = Main.class.getClassLoader().getResourceAsStream(resourceName);map
        mapParser.parseStream(is);*/

        Scanner scanner = new Scanner(System.in);
        CommandParser commandParser = new CommandParser();
        while (true) {
        System.out.print("> ");
        String input = scanner.nextLine();
        commandParser.parseAndExecute(input);
        }
    }
}
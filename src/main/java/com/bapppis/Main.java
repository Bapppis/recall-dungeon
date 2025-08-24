package com.bapppis;

import java.io.InputStream;

import com.bapppis.core.dungeon.mapparser.MapParser;

public class Main {
    public static void main(String[] args) {
        System.out.println("---------------------------------------------------------");
        System.out.println("Welcome to the world of Aurum!");
        System.out.println("---------------------------------------------------------");

        MapParser mapParser = new MapParser();
        String resourceName = "testFloor(20x20).txt";
        InputStream is = Main.class.getClassLoader().getResourceAsStream(resourceName);
        mapParser.parseStream(is);
    }
}
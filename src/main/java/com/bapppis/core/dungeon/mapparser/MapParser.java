package com.bapppis.core.dungeon.mapparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapParser {
    public void parseStream(InputStream inputStream) {
        System.out.println("Parsing map from input stream...");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            int symbol;
            while ((symbol = reader.read()) != -1) {
                System.out.println((char) symbol);
            }
        } catch (IOException e) {
            System.err.println("Error reading stream");
            e.printStackTrace();
        }
    }
}
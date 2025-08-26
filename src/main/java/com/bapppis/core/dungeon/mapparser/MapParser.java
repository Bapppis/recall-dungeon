package com.bapppis.core.dungeon.mapparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.Tile;

public class MapParser {
    public void parseStream(InputStream inputStream) {
        Floor floor = new Floor() {};
        System.out.println("Parsing map from input stream...");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            int x = 0, y = 0;
            int symbol;
            while ((symbol = reader.read()) != -1) {
                char ch = (char) symbol;
                if (ch == '\r') {
                    // Skip carriage return, don't increment x or y
                    continue;
                }
                if (ch == '\n') {
                    y++;
                    x = 0;
                } else {
                    Coordinate coord = new Coordinate(x, y);
                    Tile tile = new Tile(coord, ch);
                    //System.out.println("Current coordinate: " + coord + ", Symbol: " + ch);
                    floor.addTile(coord, tile);
                    x++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading stream");
            e.printStackTrace();
        }
        System.out.println(floor.toString());
    }
}
package com.bapppis.core.dungeon.mapparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.Tile;
import com.bapppis.core.dungeon.MapPrinter;
import com.bapppis.core.game.GameState;

public class MapParser {
    public Floor parseStream(InputStream inputStream) {
        Floor floor = new Floor() {};
        System.out.println("Parsing map from input stream...");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            int x = 0, y = 0;
            int maxX = 0; // width - 1
            int maxY = 0; // height - 1
            int symbol;
            while ((symbol = reader.read()) != -1) {
                char ch = (char) symbol;
                if (ch == '\r') {
                    // Skip carriage return, don't increment x or y
                    continue;
                }
                if (ch == '\n') {
                    // end of line
                    if (x > maxX) maxX = x; // x is length of the line read so far
                    y++;
                    x = 0;
                } else {
                    Coordinate coord = new Coordinate(x, y);
                    Tile tile = new Tile(coord, ch);
                    //System.out.println("Current coordinate: " + coord + ", Symbol: " + ch);
                    floor.addTile(coord, tile);
                    x++;
                    if (y > maxY) maxY = y;
                }
            }
            // Handle last line if file doesn't end with a newline
            if (x > 0) {
                if (x > maxX) maxX = x;
            }
            // Compute final dimensions for bounds checks
            int width = maxX;         // columns
            int height = maxY + 1;    // rows
            System.out.println("Parsed dimensions -> width=" + width + ", height=" + height);

            // Second pass: link neighbors for each tile using bounds-safe lookups
            for (int yy = 0; yy < height; yy++) {
                for (int xx = 0; xx < width; xx++) {
                    Tile t = floor.getTile(new Coordinate(xx, yy));
                    if (t == null) continue; // outside row length

                    // Right
                    if (BoundsUtil.inBounds(xx + 1, yy, width, height)) {
                        Tile right = floor.getTile(new Coordinate(xx + 1, yy));
                        if (right != null) {
                            t.setRight(right);
                            right.setLeft(t);
                        }
                    }
                    // Up
                    if (BoundsUtil.inBounds(xx, yy + 1, width, height)) {
                        Tile up = floor.getTile(new Coordinate(xx, yy + 1));
                        if (up != null) {
                            t.setUp(up);
                            up.setDown(t);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading stream");
            e.printStackTrace();
        }
    MapPrinter.printWithPlayer(floor, GameState.getPlayer());
    return floor;
    }
}

// Local bounds helper for adjacency logic
class BoundsUtil {
    static boolean inBounds(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }
}
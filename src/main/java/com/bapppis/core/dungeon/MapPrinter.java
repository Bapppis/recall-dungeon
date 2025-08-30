package com.bapppis.core.dungeon;

import com.bapppis.core.creature.player.Player;

public class MapPrinter {
    public static void printWithPlayer(Floor floor, Player player) {
        if (floor == null) {
            System.out.println("[No floor loaded]");
            return;
        }
        int maxY = 0;
        int maxX = 0;
        for (Coordinate c : floor.getTiles().keySet()) {
            if (c.getY() > maxY) maxY = c.getY();
            if (c.getX() > maxX) maxX = c.getX();
        }
        int height = maxY + 1;
        int width = maxX + 1;

        int px = -1, py = -1;
        if (player != null && player.getPosition() != null) {
            px = player.getX();
            py = player.getY();
        }

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == px && y == py) {
                    sb.append('P');
                } else {
                    Tile t = floor.getTile(new Coordinate(x, y));
                    sb.append(t != null ? t.getSymbol() : '.');
                }
            }
            sb.append('\n');
        }
        System.out.print(sb.toString());
    }
}

package com.bapppis.core.game;

import com.bapppis.core.creature.Player;
import com.bapppis.core.dungeon.Floor;

public final class GameState {
    private static Floor currentFloor;
    private static Player player;
    private static boolean inCombat = false;

    private GameState() {}

    public static Floor getCurrentFloor() {
        return currentFloor;
    }

    public static void setCurrentFloor(Floor floor) {
        currentFloor = floor;
    }

    public static Player getPlayer() {
        if (player == null) {
            player = new Player();
        }
        return player;
    }

    public static void setPlayer(Player p) {
        player = p;
    }

    public static boolean isInCombat() {
        return inCombat;
    }

    public static void setInCombat(boolean combat) {
        inCombat = combat;
    }
}

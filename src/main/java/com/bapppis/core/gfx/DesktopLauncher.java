package com.bapppis.core.gfx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Recall Dungeon");
        // Increase default window size so side panels and combat UI fit comfortably
        config.setWindowedMode(1280, 800);
        new Lwjgl3Application(new RecallDungeon(), config);
    }
}
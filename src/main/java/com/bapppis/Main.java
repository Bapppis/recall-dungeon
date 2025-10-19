package com.bapppis;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.game.Combat;
import com.bapppis.core.game.Game;
import com.bapppis.core.item.ItemLoader;

public class Main {
    public static void main(String[] args) {
        System.out.println("---------------------------------------------------------");
        System.out.println("Welcome to the world of Aurum!");
        System.out.println("---------------------------------------------------------");

    com.bapppis.core.AllLoaders.loadAll();
        // Testing combat
        Player biggles = (Player) CreatureLoader.getCreatureById(5000);
        // Give Falchion of Doom to Biggles and equip it
        biggles.addItem(ItemLoader.getItemById(9800)); // Falchion of Doom
        biggles.equipItem(biggles.getInventory().getWeapons().get(0)); // Equip Falchion of Doom

        Creature goblin = CreatureLoader.getCreatureById(6400);
        Combat.startCombat(biggles, goblin);
        /* Game game = new Game();
        game.initialize(); */
    }
}
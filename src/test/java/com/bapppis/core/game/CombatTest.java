package com.bapppis.core.game;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.item.ItemLoader;

public class CombatTest {
    @Test
    public void testCombatLoop() {
        // Implement your test logic here
            AllLoaders.loadAll();

    Player biggles = CreatureLoader.getPlayerById(5000);
        // Give Falchion of Doom to Biggles and equip it
        biggles.addItem(ItemLoader.getItemById(37000)); // Falchion of Doom
        biggles.equipItem(biggles.getInventory().getWeapons().get(0)); // Equip Falchion of Doom

    Creature goblin = CreatureLoader.getCreatureById(15000); // Goblin
    Combat.startCombat(biggles, goblin, true);
    }
}

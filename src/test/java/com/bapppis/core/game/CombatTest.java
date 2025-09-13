package com.bapppis.core.game;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.property.PropertyManager;

public class CombatTest {
    @Test
    public void testCombatLoop() {
        // Implement your test logic here
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();

    Player biggles = CreatureLoader.getPlayerById(5000);
        // Give Falchion of Doom to Biggles and equip it
        biggles.addItem(ItemLoader.getItemById(9800)); // Falchion of Doom
        biggles.equipItem(biggles.getInventory().getWeapons().get(0)); // Equip Falchion of Doom

    Creature goblin = CreatureLoader.getCreatureById(6400); // Goblin
        Combat.startCombat(biggles, goblin);
    }
}

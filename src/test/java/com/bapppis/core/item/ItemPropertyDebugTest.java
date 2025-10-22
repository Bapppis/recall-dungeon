package com.bapppis.core.item;

import com.bapppis.core.creature.Player;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.property.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemPropertyDebugTest {
    @BeforeEach
    public void setUp() {
        ItemLoader.forceReload();
        CreatureLoader.forceReload();
        PropertyLoader.forceReload();
    }

    @Test
    public void debugPropertyResolution() {
        // Check consumables
        Item healingPotion = ItemLoader.getItemByName("Test Healing Potion");
        System.out.println("Test Healing Potion: " + healingPotion);
        System.out.println("Test Healing Potion type: " + (healingPotion != null ? healingPotion.getType() : "null"));

        Player player = CreatureLoader.getPlayerById(5002);
        System.out.println("\nInventory consumables before add: " + player.getInventory().getConsumables().size());

        boolean added = player.addItemByName("Test Healing Potion");
        System.out.println("addItemByName result: " + added);
        System.out.println("Inventory consumables after add: " + player.getInventory().getConsumables().size());

        player.getInventory().getConsumables().forEach(c -> System.out.println("  - " + c.getName()));

        System.out.println("\nTrying to consume...");
        boolean consumed = player.consumeItemByName("Test Healing Potion");
        System.out.println("consumeItemByName result: " + consumed);
    }
}

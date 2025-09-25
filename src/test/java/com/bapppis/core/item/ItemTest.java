package com.bapppis.core.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.property.PropertyManager;

public class ItemTest {
    // Test item functionality here
    @Test
    public void testItemFunctionality() {
        PropertyManager.loadProperties();
        ItemLoader.loadItems();
        //CreatureLoader.loadCreatures();
        Item testSword = ItemLoader.getItemById(9800);
        System.out.println(testSword.toString());
    }
}

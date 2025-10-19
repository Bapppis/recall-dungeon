package com.bapppis.core.item.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.AllLoaders;

public class weaponTest {
    // Test item functionality here
    @Test
    public void testItemFunctionality() {
        AllLoaders.loadAll();
        //Player biggles = CreatureLoader.getPlayerById(5000);
        Item testWeapon = ItemLoader.getItemById(37001); // Rusty Iron Sword
        // System.out.println(testWeapon.toString());
        // Minimal assertion to keep test meaningful
        assertEquals("Rusty Iron Sword", testWeapon.getName());

    }
}

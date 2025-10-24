package com.bapppis.core.item.weapon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.util.ItemPrinter;

public class TestWeapon {
    @BeforeEach
    public void setup() {
        // Force reload all game data before each test to ensure test isolation
        AllLoaders.loadAll();
        ItemLoader.forceReload();
    }

    @Test
    public void testFalchionOfDoom() {
        Item falchion = ItemLoader.getItemByName("FalchionOfDoom"); // Falchion of Doom
        ItemPrinter.printDetailed(falchion);
    }

    @Test
    public void testOldBow() {
        Item oldBow = ItemLoader.getItemByName("OldBow"); // Old Bow
        ItemPrinter.printDetailed(oldBow);
    }

}

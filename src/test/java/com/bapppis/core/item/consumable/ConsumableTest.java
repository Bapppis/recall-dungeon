package com.bapppis.core.item.consumable;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Player;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class ConsumableTest {

    @Test
    public void testConsumableAppliesPropertyToCreature() {
        AllLoaders.loadAll();

        // Create a test player
        Player player = new Player();
        player.setName("TestPlayer");

        // Load the Health Regen 1 Potion (id 28005)
        Item potion = ItemLoader.getItemById(28005);
        assertNotNull(potion, "Health Regen 1 Potion should be loaded");
        assertEquals("Health Regen 1 Potion", potion.getName());

        // Use the consumable on the player
        potion.onApply(player);

        // The property should now be present on the player
        Property regen = player.getBuff(1011); // Health Regen 1
        assertNotNull(regen, "Player should have Health Regen 1 buff after using potion");
        assertEquals("Health Regen 1", regen.getName());
        assertEquals(PropertyType.BUFF, regen.getType());
        assertEquals(2, regen.getHpRegen());
        assertEquals(5, regen.getDuration());
    }

    // Optionally, test that removing the property works
    @Test
    public void testConsumablePropertyRemoval() {
        // Force reload to ensure latest data
        ItemLoader.forceReload();
        com.bapppis.core.property.PropertyLoader.forceReload();
        com.bapppis.core.creature.CreatureLoader.forceReload();

        Player player = new Player();
        player.setName("TestPlayer");
        Item potion = ItemLoader.getItemById(28005);
        assertNotNull(potion);
        potion.onApply(player);
        assertNotNull(player.getBuff(1011));
        // Remove the property
        player.removeProperty(1011);
        assertNull(player.getBuff(1011), "Buff should be removed from player");
    }
}

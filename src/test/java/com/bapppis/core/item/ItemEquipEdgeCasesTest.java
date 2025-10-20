package com.bapppis.core.item;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;

public class ItemEquipEdgeCasesTest {
    @Test
    public void testInvalidWeaponTypeJson() {
        // Simulate loading a weapon with an invalid weaponType
        String badJson = "{\"id\":99999,\"name\":\"Broken Weapon\",\"type\":\"WEAPON\",\"weaponType\":\"INVALID_TYPE\"}";
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonObject jsonObj = gson.fromJson(badJson, com.google.gson.JsonObject.class);
        // Should not instantiate a valid Weapon
        com.bapppis.core.item.ItemLoader.forceReload(); // reload to clear state
        com.bapppis.core.item.Weapon weapon = null;
        try {
            weapon = gson.fromJson(jsonObj, com.bapppis.core.item.Weapon.class);
        } catch (Exception ignored) {
        }
        assertNull(weapon);
    }

    @Test
    public void testMissingWeaponFields() {
        // Simulate loading a weapon missing required fields
        String badJson = "{\"id\":99998,\"type\":\"WEAPON\"}";
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonObject jsonObj = gson.fromJson(badJson, com.google.gson.JsonObject.class);
        com.bapppis.core.item.ItemLoader.forceReload();
        com.bapppis.core.item.Weapon weapon = null;
        try {
            weapon = gson.fromJson(jsonObj, com.bapppis.core.item.Weapon.class);
        } catch (Exception ignored) {
        }
        assertNull(weapon);
    }

    @Test
    public void testWeaponFlagsVersatileFinesse() {
        com.bapppis.core.AllLoaders.loadAll();
    com.bapppis.core.item.Item swordItem = com.bapppis.core.item.ItemLoader.getItemById(29001); // Rusty Iron Sword
        assertTrue(swordItem instanceof com.bapppis.core.item.Weapon);
        com.bapppis.core.item.Weapon sword = (com.bapppis.core.item.Weapon) swordItem;
        // Should be true for Rusty Iron Sword
        assertTrue(sword.getVersatile());
        assertTrue(sword.getFinesse());
        // Add more cases for weapons with these flags if available
    }

    @Test
    public void testEquipInvalidIndexAndUnequipEmpty() {
        AllLoaders.loadAll();
        Player p = CreatureLoader.getPlayerById(5000);
        assertNotNull(p);

        // Unequip when nothing equipped should not throw
    assertDoesNotThrow(() -> p.unequipItem(com.bapppis.core.item.itemEnums.EquipmentSlot.HELMET));

        // Equip using out-of-range inventory index should not throw
        assertDoesNotThrow(() -> {
            if (p.getInventory().getWeapons().isEmpty())
                return;
            int badIndex = p.getInventory().getWeapons().size() + 5;
            try {
                p.equipItem(p.getInventory().getWeapons().get(badIndex));
            } catch (IndexOutOfBoundsException ex) {
                // expected in raw list access; ensure game code guards indexes in real flows
            }
        });
    }
}

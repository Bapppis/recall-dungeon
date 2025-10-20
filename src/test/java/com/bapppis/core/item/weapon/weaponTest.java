
package com.bapppis.core.item.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.AllLoaders;

public class weaponTest {
    @Test
    public void testMeleeWeaponInstantiation() {
        AllLoaders.loadAll();
        Item swordItem = ItemLoader.getItemById(37001); // Rusty Iron Sword (SLASH)
        assertEquals("Rusty Iron Sword", swordItem.getName());
        assertTrue(swordItem instanceof com.bapppis.core.item.Weapon);
        com.bapppis.core.item.Weapon sword = (com.bapppis.core.item.Weapon) swordItem;
        assertEquals("SLASH", sword.getWeaponType().name());
        assertEquals("MELEE", sword.getWeaponClass().name());
    }

    @Test
    public void testRangedWeaponInstantiation() {
        AllLoaders.loadAll();
        Item bowItem = ItemLoader.getItemById(35000); // Old Bow (PIERCE)
        assertEquals("Old Bow", bowItem.getName());
        assertTrue(bowItem instanceof com.bapppis.core.item.Weapon);
        com.bapppis.core.item.Weapon bow = (com.bapppis.core.item.Weapon) bowItem;
        assertEquals("PIERCE", bow.getWeaponType().name());
        assertEquals("RANGED", bow.getWeaponClass().name());
        assertEquals(true, bow.isTwoHanded());
    }

    /* @Test
    public void testMagicWeaponInstantiation() {
        AllLoaders.loadAll();
        Item staffItem = ItemLoader.getItemById(39001); // Example Staff (STAFF)
        assertTrue(staffItem instanceof com.bapppis.core.item.Weapon);
        com.bapppis.core.item.Weapon staff = (com.bapppis.core.item.Weapon) staffItem;
        assertEquals("STAFF", staff.getWeaponType().name());
        assertEquals("MAGIC", staff.getWeaponClass().name());
        assertNotNull(staff.getDamageType());
    }

    @Test
    public void testMagicPhysicalWeaponInstantiation() {
        AllLoaders.loadAll();
        Item magicPhysicalItem = ItemLoader.getItemById(39010); // Example MagicPhysicalWeapon
        assertTrue(magicPhysicalItem instanceof com.bapppis.core.item.Weapon);
        com.bapppis.core.item.Weapon magicPhysical = (com.bapppis.core.item.Weapon) magicPhysicalItem;
        assertEquals("MAGIC_PHYSICAL", magicPhysical.getWeaponType().name());
        assertEquals("MAGIC", magicPhysical.getWeaponClass().name());
        assertNotNull(magicPhysical.getDamageType());
    } */

    @Test
    public void testWeaponVersatileFinesseFlags() {
        AllLoaders.loadAll();
        Item swordItem = ItemLoader.getItemById(37001); // Rusty Iron Sword
        assertTrue(swordItem instanceof com.bapppis.core.item.Weapon);
        com.bapppis.core.item.Weapon sword = (com.bapppis.core.item.Weapon) swordItem;
        assertEquals(true, sword.getVersatile());
        assertEquals(true, sword.getFinesse());
        // Add more assertions for weapons with these flags if available
    }
}

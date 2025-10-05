package com.bapppis.core.Creature;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.item.Equipment;
import com.bapppis.core.item.EquipmentSlot;

/**
 * Tests for equipping versatile weapons one- and two-handed.
 */
public class EquipVersatileTest {

    // Minimal test creature implementation for equip tests
    static class TestCreature extends Creature {
        public TestCreature() {
            super();
        }
        }

    // Helper to build a simple versatile Equipment instance without needing JSON
    private Equipment makeVersatileWeapon(String name) {
        Equipment eq = new Equipment();
        // Use setters that exist on Equipment
        eq.setSlot(EquipmentSlot.WEAPON);
        eq.setTwoHanded(false);
        eq.setVersatile(true);
        return eq;
    }

    @Test
    public void equipVersatileOneHanded() {
        TestCreature c = new TestCreature();
        Equipment v = makeVersatileWeapon("Versatile Sword");

        // Equip normally (defaults to one-handed)
        c.equipItem(v);

        assertSame(v, c.getEquipped(EquipmentSlot.WEAPON), "Weapon slot should hold the versatile item when one-handed");
        assertNotSame(v, c.getEquipped(EquipmentSlot.OFFHAND), "Offhand should not reference the versatile item when one-handed");
    }

    @Test
    public void equipVersatileTwoHanded() {
        TestCreature c = new TestCreature();
        Equipment v = makeVersatileWeapon("Versatile Spear");

        // Equip explicitly two-handed
        c.equipItem(v, true);

        assertSame(v, c.getEquipped(EquipmentSlot.WEAPON), "Weapon slot should hold the versatile item when two-handed");
        assertSame(v, c.getEquipped(EquipmentSlot.OFFHAND), "Offhand should reference the same instance when equipped two-handed");
    }

    @Test
    public void equipVersatileTwoHandedUnequipsOffhand() {
        TestCreature c = new TestCreature();
        Equipment v = makeVersatileWeapon("Versatile Axe");
        Equipment off = new Equipment();
        off.setSlot(EquipmentSlot.OFFHAND);
        off.setTwoHanded(false);

        // Put something in the offhand first
        c.equipItem(off);
        assertSame(off, c.getEquipped(EquipmentSlot.OFFHAND));

        // Now equip the versatile as two-handed
        c.equipItem(v, true);

        assertSame(v, c.getEquipped(EquipmentSlot.WEAPON), "Weapon slot should hold the versatile item when two-handed");
        assertSame(v, c.getEquipped(EquipmentSlot.OFFHAND), "Offhand should reference the same instance when equipped two-handed");
        // The previous offhand should have been removed from equipment (unequipped)
        assertNotSame(off, c.getEquipped(EquipmentSlot.OFFHAND), "Previous offhand should no longer be equipped");
    }
}

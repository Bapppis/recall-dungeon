package com.bapppis.core.item;

import com.bapppis.core.creature.Creature;
import java.util.EnumMap;
import java.util.Map;

public class EquipmentUtils {
    /**
     * Returns a map of all equipped items by slot.
     */
    public static Map<EquipmentSlot, com.bapppis.core.item.Item> getAllEquipped(Creature creature) {
        EnumMap<EquipmentSlot, com.bapppis.core.item.Item> equipped = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            com.bapppis.core.item.Item item = creature.getEquipped(slot);
            if (item != null) equipped.put(slot, item);
        }
        return equipped;
    }

    /**
     * Returns a map of equipped armor items (helmet, armor, legwear).
     */
    public static Map<EquipmentSlot, com.bapppis.core.item.Item> getEquippedArmors(Creature creature) {
        EnumMap<EquipmentSlot, com.bapppis.core.item.Item> armors = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HELMET, EquipmentSlot.ARMOR, EquipmentSlot.LEGWEAR }) {
            com.bapppis.core.item.Item item = creature.getEquipped(slot);
            if (item != null) armors.put(slot, item);
        }
        return armors;
    }

    /**
     * Returns a map of equipped weapon/offhand items.
     */
    public static Map<EquipmentSlot, com.bapppis.core.item.Item> getEquippedWeapons(Creature creature) {
        EnumMap<EquipmentSlot, com.bapppis.core.item.Item> weapons = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.WEAPON, EquipmentSlot.OFFHAND }) {
            com.bapppis.core.item.Item item = creature.getEquipped(slot);
            if (item != null) weapons.put(slot, item);
        }
        return weapons;
    }

    /**
     * Prints all equipped items by slot.
     */
    public static void printEquipped(Creature creature) {
        System.out.println("Equipped Items:");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            com.bapppis.core.item.Item item = creature.getEquipped(slot);
            System.out.println("  " + slot + ": " + (item == null ? "Empty" : item.getName()));
        }
    }

    /**
     * Prints equipped armors (helmet, armor, legwear).
     */
    public static void printEquippedArmors(Creature creature) {
        System.out.println("Equipped Armors:");
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HELMET, EquipmentSlot.ARMOR, EquipmentSlot.LEGWEAR }) {
            com.bapppis.core.item.Item item = creature.getEquipped(slot);
            System.out.println("  " + slot + ": " + (item == null ? "Empty" : item.getName()));
        }
    }

    /**
     * Prints equipped weapons (weapon, offhand).
     */
    public static void printEquippedWeapons(Creature creature) {
        System.out.println("Equipped Weapons:");
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.WEAPON, EquipmentSlot.OFFHAND }) {
            com.bapppis.core.item.Item item = creature.getEquipped(slot);
            System.out.println("  " + slot + ": " + (item == null ? "Empty" : item.getName()));
        }
    }
}

package com.bapppis.core.creature;

import com.bapppis.core.item.*;
import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.item.itemEnums.ItemType;
import com.bapppis.core.item.itemEnums.WeaponClass;
import com.bapppis.core.creature.creatureEnums.Stats;

import java.util.*;

public class Inventory {
    private final List<Item> items = new ArrayList<>();
    private transient Creature owner; // Reference to owner for capacity calculation (transient to avoid Gson circular
                                      // reference)

    public void setOwner(Creature owner) {
        this.owner = owner;
    }

    public int getMaxCapacity() {
        if (owner == null)
            return 10; // Default if no owner
        int strBonus = owner.getStatBonus(Stats.STRENGTH);
        return Math.max(5, 10 + strBonus);
    }

    public int getCurrentLoad() {
        if (owner == null)
            return items.size();
        // Count items not equipped
        int count = 0;
        for (Item item : items) {
            boolean isEquipped = false;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (owner.getEquipped(slot) == item) {
                    isEquipped = true;
                    break;
                }
            }
            if (!isEquipped) {
                count++;
            }
        }
        return count;
    }

    public boolean isOverEncumbered() {
        return getCurrentLoad() > getMaxCapacity();
    }

    public boolean addItem(Item item) {
        if (item == null) {
            System.err.println("[Inventory] Tried to add null item!");
            return false;
        }
        items.add(item);
        sortItems();
        return true;
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    public List<Item> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    // Type-filtered getters for backward compatibility
    public List<Item> getWeapons() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof Equipment && ((Equipment) item).getSlot() == EquipmentSlot.WEAPON) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Item> getOffhands() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof Equipment && ((Equipment) item).getSlot() == EquipmentSlot.OFFHAND) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Item> getHelmets() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof Equipment && ((Equipment) item).getSlot() == EquipmentSlot.HELMET) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Item> getArmors() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof Equipment && ((Equipment) item).getSlot() == EquipmentSlot.ARMOR) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Item> getLegwear() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof Equipment && ((Equipment) item).getSlot() == EquipmentSlot.LEGWEAR) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Item> getConsumables() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getType() == ItemType.CONSUMABLE) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Item> getMisc() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getType() == ItemType.MISC) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private int getCategoryOrder(Item item) {
        if (item == null)
            return 999;

        // Weapons: melee (0), ranged (1), magic (2)
        if (item instanceof Weapon) {
            Weapon weapon = (Weapon) item;
            WeaponClass wc = weapon.getWeaponClass();
            if (wc == WeaponClass.MELEE)
                return 0;
            if (wc == WeaponClass.RANGED)
                return 1;
            if (wc == WeaponClass.MAGIC)
                return 2;
            return 0; // Default to melee if unknown
        }

        // Equipment by slot
        if (item instanceof Equipment) {
            EquipmentSlot slot = ((Equipment) item).getSlot();
            if (slot == EquipmentSlot.OFFHAND)
                return 3;
            if (slot == EquipmentSlot.HELMET)
                return 4;
            if (slot == EquipmentSlot.ARMOR)
                return 5;
            if (slot == EquipmentSlot.LEGWEAR)
                return 6;
        }

        // Non-equipment items
        if (item.getType() == ItemType.CONSUMABLE)
            return 7;
        if (item.getType() == ItemType.MISC)
            return 8;

        return 999; // Unknown items last
    }

    private void sortItems() {
        items.sort(Comparator
                .comparing((Item i) -> getCategoryOrder(i))
                .thenComparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
    }
}

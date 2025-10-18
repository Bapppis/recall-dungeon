package com.bapppis.core.creature;

import com.bapppis.core.item.*;
import java.util.*;

public class Inventory {
    private final List<Item> weapons = new ArrayList<>();
    private final List<Item> offhands = new ArrayList<>();
    private final List<Item> helmets = new ArrayList<>();
    private final List<Item> armors = new ArrayList<>();
    private final List<Item> legwear = new ArrayList<>();
    private final List<Item> consumables = new ArrayList<>();
    private final List<Item> misc = new ArrayList<>();
    private static final int MAX_PER_TYPE = 5;

    public boolean addItem(Item item) {
        if (item == null) {
            System.err.println("[Inventory] Tried to add null item!");
            return false;
        }
        List<Item> target = getContainer(item);
        if (target == null || target.size() >= MAX_PER_TYPE) {
            System.err.println("[Inventory] No container for item type: " + (item != null ? item.getClass().getSimpleName() : "null"));
            return false;
        }
        target.add(item);
        sortContainer(target);
        return true;
    }

    public boolean removeItem(Item item) {
        List<Item> target = getContainer(item);
        if (target == null) return false;
        return target.remove(item);
    }

    public List<Item> getWeapons() { return Collections.unmodifiableList(weapons); }
    public List<Item> getOffhands() { return Collections.unmodifiableList(offhands); }
    public List<Item> getHelmets() { return Collections.unmodifiableList(helmets); }
    public List<Item> getArmors() { return Collections.unmodifiableList(armors); }
    public List<Item> getLegwear() { return Collections.unmodifiableList(legwear); }
    public List<Item> getConsumables() { return Collections.unmodifiableList(consumables); }
    public List<Item> getMisc() { return Collections.unmodifiableList(misc); }

    private List<Item> getContainer(Item item) {
        if (item == null) return null;
        if (item.getType() == ItemType.CONSUMABLE) return consumables;
        if (item.getType() == ItemType.MISC) return misc;
        if (item.getSlot() == EquipmentSlot.WEAPON) return weapons;
        if (item.getSlot() == EquipmentSlot.OFFHAND) return offhands;
        if (item.getSlot() == EquipmentSlot.HELMET) return helmets;
        if (item.getSlot() == EquipmentSlot.ARMOR) return armors;
        if (item.getSlot() == EquipmentSlot.LEGWEAR) return legwear;
        return null;
    }

    private void sortContainer(List<Item> items) {
        items.sort(Comparator
            .comparing((Item i) -> {
                if (i instanceof Equipment) {
                    Rarity r = ((Equipment) i).getRarity();
                    return r != null ? r.ordinal() : 0;
                }
                return 0;
            })
            .thenComparing(Item::getName));
    }
}

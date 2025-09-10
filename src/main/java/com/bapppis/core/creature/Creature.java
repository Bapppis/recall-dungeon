package com.bapppis.core.creature;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;

import com.bapppis.core.property.Property;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.EquipmentSlot;

public abstract class Creature {
    // --- Fields ---
    private int id; // id set by Gson, no setter
    private String name;
    private int visionRange = 1; // default vision range
    private int level;
    private int xp;
    private int baseHp;
    private int maxHp;
    private int currentHp;
    private int hpLvlBonus; // Additional HP gained per level
    private Size size;
    private Type type;
    private CreatureType creatureType;
    private EnumMap<Stats, Integer> stats;
    private EnumMap<Resistances, Integer> resistances;
    private HashMap<Integer, Property> buffs = new HashMap<>();
    private HashMap<Integer, Property> debuffs = new HashMap<>();
    private HashMap<Integer, Property> immunities = new HashMap<>();
    private HashMap<Integer, Property> traits = new HashMap<>();
    private String description;
    private EnumMap<EquipmentSlot, Item> equipment = new EnumMap<>(EquipmentSlot.class);
    private Inventory inventory = new Inventory();
    public Inventory getInventory() {
        return inventory;
    }

    // --- Enums ---
    public enum Size {
        SMALL,
        MEDIUM,
        LARGE,
        HUGE,
        GARGANTUAN,
    }

    public enum Type {
        PLAYER,
        NPC,
        ENEMY,
    }

    public enum CreatureType {
        BEAST,
        CONSTRUCT,
        DRAGON,
        ELEMENTAL,
        HUMANOID,
        PLANT,
        UNDEAD,
        UNKNOWN,
    }

    public enum Stats {
        STRENGTH,
        DEXTERITY,
        CONSTITUTION,
        INTELLIGENCE,
        WISDOM,
        CHARISMA,
        LUCK,
    }

    public enum Resistances {
        FIRE,
        WATER,
        WIND,
        ICE,
        NATURE,
        LIGHTNING,
        LIGHT,
        DARKNESS,
        BLUDGEONING,
        PIERCING,
        SLASHING,
        TRUE,
    }

    // --- Constructor ---
    public Creature() {
        stats = new EnumMap<>(Stats.class);
        for (Stats stat : Stats.values()) {
            if (stat == Stats.LUCK) {
                stats.put(stat, 1); // Luck default is 1
            } else {
                stats.put(stat, 10); // other stats default to 10
            }
        }
        resistances = new EnumMap<>(Resistances.class);
        for (Resistances res : Resistances.values()) {
            resistances.put(res, 100); // default resistance 100%
        }
        size = Size.MEDIUM; // default size
        type = Type.ENEMY; // default type
    }

    // --- Getters and Setters ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVisionRange() {
        return visionRange;
    }

    public void setVisionRange(int visionRange) {
        this.visionRange = visionRange;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void addXp(int xp) {
        int currentXp = this.xp + xp;
        if (this.level >= 30) {
            this.level = 30;
            this.xp = 0;
        } else if (currentXp >= ((this.level + 1) * 10)) {
            this.level++;
            this.updateMaxHp();
            currentXp -= ((this.level) * 10);
            addXp(currentXp); // Recursively add remaining XP
        } else {
            this.xp = this.xp + xp;
        }
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        if (maxHp < 1) {
            maxHp = 1; // Ensure maxHp is at least 1
        }
        if (currentHp > maxHp) {
            currentHp = maxHp; // Adjust currentHp if it exceeds new maxHp
        }
        this.maxHp = maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int hp) {
        if (hp < 0) {
            hp = 0;
        }
        if (this.currentHp >= this.maxHp) {
            this.currentHp = this.maxHp;
        } else {
            this.currentHp = this.currentHp + hp;
        }
    }
    public int getHpLvlBonus() {
        return hpLvlBonus;
    }

    public void setHpLvlBonus(int hpLvlBonus) {
        this.hpLvlBonus = hpLvlBonus;
    }
    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CreatureType getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(CreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<Integer, Property> getBuffs() {
        return buffs;
    }

    public HashMap<Integer, Property> getDebuffs() {
        return debuffs;
    }

    public HashMap<Integer, Property> getImmunities() {
        return immunities;
    }

    public HashMap<Integer, Property> getTraits() {
        return traits;
    }

    public Property getBuff(int id) {
        return buffs.get(id);
    }

    public Property getDebuff(int id) {
        return debuffs.get(id);
    }

    public Property getImmunity(int id) {
        return immunities.get(id);
    }

    public Property getTrait(int id) {
        return traits.get(id);
    }

    public int getStat(Stats stat) {
        return stats.getOrDefault(stat, 0);
    }

    public void setStat(Stats stat, int value) {
        stats.put(stat, value);
        if (stat == Stats.CONSTITUTION) {
            alterHp();
        }
    }

    public void modifyStat(Stats stat, int amount) {
        stats.put(stat, getStat(stat) + amount);
        if (stat == Stats.CONSTITUTION) {
            alterHp();
        }
    }

    public int getResistance(Resistances resistance) {
        return resistances.getOrDefault(resistance, 0);
    }

    public void setResistance(Resistances resistance, int value) {
        resistances.put(resistance, value);
    }

    public void modifyResistance(Resistances resistance, int amount) {
        resistances.put(resistance, getResistance(resistance) + amount);
    }

    public Item getEquipped(EquipmentSlot slot) {
        return equipment.get(slot);
    }

    public void equipItem(Item item) {
        EquipmentSlot slot = item.getSlot();
        // Remove any existing item in the slot first
        Item oldItem = null;
        if (item.isTwoHanded()) {
            oldItem = equipment.get(EquipmentSlot.WEAPON);
            if (oldItem != null) unequipItem(EquipmentSlot.WEAPON);
            oldItem = equipment.get(EquipmentSlot.OFFHAND);
            if (oldItem != null) unequipItem(EquipmentSlot.OFFHAND);
            equipment.put(EquipmentSlot.WEAPON, item);
            equipment.put(EquipmentSlot.OFFHAND, item);
        } else {
            oldItem = equipment.get(slot);
            if (oldItem != null) unequipItem(slot);
            equipment.put(slot, item);
        }
        // Remove from inventory if present
        getInventory().removeItem(item);
        // Apply stat and resistance effects if present
        applyItemEffects(item);
    }

    public void unequipItem(EquipmentSlot slot) {
        Item item = equipment.remove(slot);
        if (item != null) {
            removeItemEffects(item);
            // Try to add back to inventory
            boolean added = getInventory().addItem(item);
            if (!added) {
                // Optionally: print or log that inventory is full
                System.out.println("Inventory full! Could not add " + item.getName() + " back to inventory.");
            }
        }
    }

    private void applyItemEffects(Item item) {
        // Only apply if item is Equipment (has stats/resistances)
        if (item instanceof com.bapppis.core.item.Equipment) {
            com.bapppis.core.item.Equipment eq = (com.bapppis.core.item.Equipment) item;
            if (eq.getStats() != null) {
                for (java.util.Map.Entry<String, Integer> entry : eq.getStats().entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("VISION_RANGE")) {
                        setVisionRange(getVisionRange() + entry.getValue());
                    } else {
                        try {
                            Stats stat = Stats.valueOf(entry.getKey());
                            modifyStat(stat, entry.getValue());
                        } catch (IllegalArgumentException e) {
                            // Ignore unknown stat
                        }
                    }
                }
            }
            if (eq.getResistances() != null) {
                for (java.util.Map.Entry<String, Integer> entry : eq.getResistances().entrySet()) {
                    try {
                        Resistances res = Resistances.valueOf(entry.getKey());
                        modifyResistance(res, entry.getValue());
                    } catch (IllegalArgumentException e) {
                        // Ignore unknown resistance
                    }
                }
            }
        }
    }

    private void removeItemEffects(Item item) {
        // Only remove if item is Equipment (has stats/resistances)
        if (item instanceof com.bapppis.core.item.Equipment) {
            com.bapppis.core.item.Equipment eq = (com.bapppis.core.item.Equipment) item;
            if (eq.getStats() != null) {
                for (java.util.Map.Entry<String, Integer> entry : eq.getStats().entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("VISION_RANGE")) {
                        setVisionRange(getVisionRange() - entry.getValue());
                    } else {
                        try {
                            Stats stat = Stats.valueOf(entry.getKey());
                            modifyStat(stat, -entry.getValue());
                        } catch (IllegalArgumentException e) {
                            // Ignore unknown stat
                        }
                    }
                }
            }
            if (eq.getResistances() != null) {
                for (java.util.Map.Entry<String, Integer> entry : eq.getResistances().entrySet()) {
                    try {
                        Resistances res = Resistances.valueOf(entry.getKey());
                        modifyResistance(res, -entry.getValue());
                    } catch (IllegalArgumentException e) {
                        // Ignore unknown resistance
                    }
                }
            }
        }
    }

    public void addProperty(Property property) {
        int id = property.getId();
        if (id >= 1000 && id < 2000) {
            buffs.put(id, property);
        } else if (id >= 2000 && id < 3000) {
            debuffs.put(id, property);
        } else if (id >= 3000 && id < 4000) {
            immunities.put(id, property);
        } else if (id >= 4000 && id < 5000) {
            traits.put(id, property);
        }
        // Apply property effects
        property.onApply(this);
    }

    public void removeProperty(int id) {
        Property property = null;
        if (id >= 1000 && id < 2000) {
            property = buffs.get(id);
            buffs.remove(id);
        } else if (id >= 2000 && id < 3000) {
            property = debuffs.get(id);
            debuffs.remove(id);
        } else if (id >= 3000 && id < 4000) {
            property = immunities.get(id);
            immunities.remove(id);
        } else if (id >= 4000 && id < 5000) {
            property = traits.get(id);
            traits.remove(id);
        }
        if (property != null) {
            property.onRemove(this);
        }
    }

    public void printStatusEffects() {
        System.out.println("Buffs:");
        for (Property buff : buffs.values()) {
            System.out.println(" - " + buff);
        }

        System.out.println("Debuffs:");
        for (Property debuff : debuffs.values()) {
            System.out.println(" - " + debuff);
        }

        System.out.println("Immunities:");
        for (Property immunity : immunities.values()) {
            System.out.println(" - " + immunity);
        }

        System.out.println("Traits:");
        for (Property trait : traits.values()) {
            System.out.println(" - " + trait);
        }
    }

    public String printProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append("Buffs:\n");
        for (Property buff : buffs.values()) {
            sb.append(" - ").append(buff).append("\n");
        }
        sb.append("Debuffs:\n");
        for (Property debuff : debuffs.values()) {
            sb.append(" - ").append(debuff).append("\n");
        }
        sb.append("Immunities:\n");
        for (Property immunity : immunities.values()) {
            sb.append(" - ").append(immunity).append("\n");
        }
        sb.append("Traits:\n");
        for (Property trait : traits.values()) {
            sb.append(" - ").append(trait).append("\n");
        }
        return sb.toString();
    }

    public void updateMaxHp() {
        if (this.getStat(Stats.CONSTITUTION) >= 10) {
            int bonusHp = this.hpLvlBonus + (this.getStat(Stats.CONSTITUTION) - 10); // Each point above 10 gives +1 HP plus level bonus
            this.setMaxHp(this.maxHp + bonusHp);
            this.modifyHp(bonusHp);
        } else {
            int bonusHp = (this.hpLvlBonus - (10 - this.getStat(Stats.CONSTITUTION))); // Each point below 10 gives -1 max HP
            if(bonusHp < 0) bonusHp = 1; // Prevent reducing maxHp below base due to low CON
            this.setMaxHp(this.maxHp + bonusHp);
            this.modifyHp(bonusHp);
        }
    }

    public void modifyHp(int amount) {
        currentHp += amount;
        if (currentHp > maxHp) {
            currentHp = maxHp;
        } else if (currentHp < 0) {
            currentHp = 0;
        }
    }
    public void alterHp() {
        if ((this.getStat(Stats.CONSTITUTION) >= 10)) {
            int newMaxHp = this.baseHp + ((this.level + 1) * (this.hpLvlBonus + (this.getStat(Stats.CONSTITUTION) - 10)));
            this.setMaxHp(newMaxHp);
            this.modifyHp(newMaxHp - this.currentHp);
        } else {
            int newMaxHp = this.baseHp + ((this.level + 1) * (this.hpLvlBonus - (10 - this.getStat(Stats.CONSTITUTION))));
            this.setMaxHp(newMaxHp);
            this.modifyHp(newMaxHp - this.currentHp);
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Creature: ").append(name).append("\n");
        sb.append("Id: ").append(id).append("\n");
        sb.append("Vision Range: ").append(visionRange).append("\n");
        sb.append("Level: ").append(level).append("\n");
        sb.append("XP: ").append(xp).append("\n");
        sb.append("Base HP: ").append(baseHp).append("\n");
        sb.append("Max HP: ").append(maxHp).append("\n");
        sb.append("Current HP: ").append(currentHp).append("\n");
        sb.append("HP Level Bonus: ").append(hpLvlBonus).append("\n");
        sb.append("Size: ").append(size).append("\n");
        sb.append("Type: ").append(type).append("\n");
        sb.append("Creature Type: ").append(creatureType).append("\n");
        sb.append("Stats: ").append(stats).append("\n");
        sb.append("-----------------\n");
        sb.append("Resistances:\n");
        for (Entry<Resistances, Integer> entry : resistances.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("%\n");
        }
        sb.append("-----------------\n");
        sb.append("Equipment:\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item equipped = equipment.get(slot);
            sb.append(slot.name()).append(": ");
            if (equipped != null) {
                sb.append(equipped.getName());
            } else {
                sb.append("Empty");
            }
            sb.append("\n");
        }
        sb.append("-----------------\n");
        sb.append(printProperties());
        sb.append("-----------------\n");
        sb.append("Inventory:\n");
        sb.append("Weapons: ").append(listInventoryItems(inventory.getWeapons())).append("\n");
        sb.append("Offhands: ").append(listInventoryItems(inventory.getOffhands())).append("\n");
        sb.append("Helmets: ").append(listInventoryItems(inventory.getHelmets())).append("\n");
        sb.append("Armor: ").append(listInventoryItems(inventory.getArmors())).append("\n");
        sb.append("Legwear: ").append(listInventoryItems(inventory.getLegwear())).append("\n");
        sb.append("Consumables: ").append(listInventoryItems(inventory.getConsumables())).append("\n");
        sb.append("Misc: ").append(listInventoryItems(inventory.getMisc())).append("\n");
        return sb.toString();
    }

    private String listInventoryItems(java.util.List<Item> items) {
        if (items.isEmpty()) return "Empty";
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append(item.getName()).append(", ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2); // Remove trailing comma
        return sb.toString();
    }
}
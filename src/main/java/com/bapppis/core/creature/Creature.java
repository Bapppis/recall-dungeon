package com.bapppis.core.creature;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;

//import com.google.gson.Gson;
import com.bapppis.core.property.Property;

public abstract class Creature {
    // Getter and setter for level
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // Getter and setter for xp
    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    // Getter and setter for baseHp
    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    // Add a property to the correct map based on id prefix
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

    // Remove a property from the correct map based on id prefix
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

    // Properties for all creatures including the player
    private int id; // id set by Gson, no setter
    private String name;
    private int level;
    private int xp;
    private int baseHp;
    private int maxHp;
    private int currentHp;

    // Getter for id
    public int getId() {
        return id;
    }

    // Enum for size, small, medium, large
    public enum Size {
        SMALL,
        MEDIUM,
        LARGE,
        HUGE,
        GARGANTUAN,
    }

    private Size size;

    public enum Type {
        PLAYER,
        NPC,
        ENEMY,
    }

    private Type type;

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

    private CreatureType creatureType;

    // Creature stats default values are 10, except for Luck which is 1
    public enum Stats {
        STRENGTH,
        DEXTERITY,
        CONSTITUTION,
        INTELLIGENCE,
        WISDOM,
        CHARISMA,
        LUCK,
    }

    // Stats stored in EnumMap
    private EnumMap<Stats, Integer> stats;

    // Creature resistances default values are 100 (%)
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

    // Resistances stored in EnumMap
    private EnumMap<Resistances, Integer> resistances;

    // All the properties separated by categories
    private HashMap<Integer, Property> buffs = new HashMap<>();
    private HashMap<Integer, Property> debuffs = new HashMap<>();
    private HashMap<Integer, Property> immunities = new HashMap<>();
    private HashMap<Integer, Property> traits = new HashMap<>();

    private String description;

    // Constructor: sets stats to 10 and resistances to 100 by default, and
    // initializes properties
    public Creature() {
        stats = new EnumMap<>(Stats.class);
        for (Stats stat : Stats.values()) {
            if (stat == Stats.LUCK) {
                stats.put(stat, 1); // Luck default is 1
            } else {
                stats.put(stat, 10); // other stats default to 10
            }
        }
        updateMaxHp();
        resistances = new EnumMap<>(Resistances.class);
        for (Resistances res : Resistances.values()) {
            resistances.put(res, 100); // default resistance 100%
        }
        size = Size.MEDIUM; // default size
        type = Type.ENEMY; // default type
    }

    /*
     * public static Creature fromJson(String json) {
     * Gson gson = new Gson();
     * return gson.fromJson(json, Creature.class);
     * }
     */

    // Getters and setters for stats
    public int getStat(Stats stat) {
        return stats.getOrDefault(stat, 0);
    }

    public void setStat(Stats stat, int value) {
        stats.put(stat, value);
        if (stat == Stats.CONSTITUTION) {
            updateMaxHp();
        }
    }

    // Add or subtract from a specific stat
    public void modifyStat(Stats stat, int amount) {
        stats.put(stat, getStat(stat) + amount);
        if (stat == Stats.CONSTITUTION) {
            updateMaxHp();
        }
    }

    // Getters and setters for resistances
    public int getResistance(Resistances resistance) {
        return resistances.getOrDefault(resistance, 0);
    }

    public void setResistance(Resistances resistance, int value) {
        resistances.put(resistance, value);
    }

    public void modifyResistance(Resistances resistance, int amount) {
        resistances.put(resistance, getResistance(resistance) + amount);
    }

    // Other getters and setters for name, hp, size, type, etc.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void updateMaxHp() {
        if (this.getStat(Stats.CONSTITUTION) >= 10) {
            int bonusHp = (this.getStat(Stats.CONSTITUTION) - 10) * 5; // Each point above 10 gives +5 max HP
            this.setMaxHp(this.getBaseHp() + bonusHp);
            this.modifyHp(bonusHp);
        } else {
            int penaltyHp = (10 - this.getStat(Stats.CONSTITUTION)) * 5; // Each point below 10 gives -5 max HP
            this.setMaxHp(this.getBaseHp() - penaltyHp);
            this.modifyHp(-penaltyHp);
        }
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
        }
        else {
            this.currentHp = this.currentHp + hp;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Creature: ").append(name).append("\n");
        sb.append("Id: ").append(id).append("\n");
        sb.append("Base HP: ").append(baseHp).append("\n");
        sb.append("Max HP: ").append(maxHp).append("\n");
        sb.append("Current HP: ").append(currentHp).append("\n");
        sb.append("Size: ").append(size).append("\n");
        sb.append("Type: ").append(type).append("\n");
        sb.append("Creature Type: ").append(creatureType).append("\n");
        sb.append("Stats: ").append(stats).append("\n");
        sb.append("-----------------\n");
        for (Entry<Resistances, Integer> entry : resistances.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("%\n");
        }
        sb.append("-----------------\n");
        sb.append(printProperties());
        return sb.toString();
    }
}
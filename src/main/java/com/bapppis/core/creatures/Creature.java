package com.bapppis.core.creatures;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;

import com.bapppis.core.property.Property;

public abstract class Creature {

    // Properties for all creatures including the player
    private String name;
    private int maxHp;
    private int currentHp;

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

    // Constructor: sets stats to 10 and resistances to 100 by default, and initializes properties
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

    // Getters and setters for stats
    public int getStat(Stats stat) {
        return stats.getOrDefault(stat, 0);
    }

    public void setStat(Stats stat, int value) {
        stats.put(stat, value);
    }

    // Add or subtract from a specific stat
    public void modifyStat(Stats stat, int amount) {
        stats.put(stat, getStat(stat) + amount);
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

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.min(currentHp, maxHp);
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

    public void addBuff(Property buff) {
        buffs.put(buff.getId(), buff);
        buff.onApply(this);
    }


    public void addDebuff(Property debuff) {
        debuffs.put(debuff.getId(), debuff);
        debuff.onApply(this);
    }


    public void addImmunity(Property immunity) {
        immunities.put(immunity.getId(), immunity);
        immunity.onApply(this);
    }

    public void addTrait(Property trait) {
        traits.put(trait.getId(), trait);
        trait.onApply(this);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Creature: ").append(name).append("\n");
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
        sb.append("Buffs: ").append(buffs.values()).append("\n");
        sb.append("Debuffs: ").append(debuffs.values()).append("\n");
        sb.append("Immunities: ").append(immunities.values()).append("\n");
        sb.append("Traits: ").append(traits.values()).append("\n");
        return sb.toString();
    }
}
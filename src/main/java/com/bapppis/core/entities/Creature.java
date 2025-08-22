package com.bapppis.core.entities;

import com.bapppis.core.components.Component;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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

    // List of components that can be added to creatures
    private List<Component> components;

    // Constructor: sets stats to 10 and resistances to 100 by default, and initializes components
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

        components = new ArrayList<>();
    }

    // Getters and setters for stats
    public int getStat(Stats stat) {
        return stats.getOrDefault(stat, 0);
    }

    public void setStat(Stats stat, int value) {
        stats.put(stat, value);
    }

    // Getters and setters for resistances
    public int getResistance(Resistances resistance) {
        return resistances.getOrDefault(resistance, 0);
    }

    public void setResistance(Resistances resistance, int value) {
        resistances.put(resistance, value);
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
        this.currentHp = currentHp;
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

    // Components management
    public void addComponent(Component component) {
        components.add(component);
    }

    public List<Component> getComponents() {
        return components;
    }

    public boolean hasComponent(Class<? extends Component> componentClass) {
        for (Component c : components) {
            if (componentClass.isInstance(c)) {
                return true;
            }
        }
        return false;
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
        sb.append("Components: ").append(components).append("\n");
        return sb.toString();
    }
}
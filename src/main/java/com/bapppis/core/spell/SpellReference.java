package com.bapppis.core.spell;

/**
 * Represents a reference to a spell with a weight for creatures.
 * Used in creature JSON files to specify spells and their usage frequency.
 */
public class SpellReference {
    public String name;
    public Integer weight;

    public SpellReference() {
    }

    public SpellReference(String name, Integer weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public Integer getWeight() {
        return weight == null ? 1 : weight;
    }

    @Override
    public String toString() {
        return "SpellReference[name='" + name + "', weight=" + getWeight() + "]";
    }
}

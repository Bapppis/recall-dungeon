package com.bapppis.core.spell;

/**
 * Base class for all spells in Recall Dungeon.
 * Extend this class to implement specific spell logic.
 */
public abstract class Spell {
    private int id;
    private String name;
    private String description;
    private int manaCost;

    public Spell(int id, String name, String description, int manaCost) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getManaCost() {
        return manaCost;
    }

    /**
     * Implement the effect of the spell here.
     * @param caster The creature casting the spell
     * @param target The target of the spell (can be null for self-cast)
     */
    public abstract void cast(com.bapppis.core.creature.Creature caster, com.bapppis.core.creature.Creature target);
}

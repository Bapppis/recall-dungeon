package com.bapppis.core.spell;

import java.util.List;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.creatureEnums.Stats;

/**
 * Represents a spell that can be cast by creatures.
 * Spells work similarly to magic attacks, rolling against dodge and magic resist.
 * They can deal up to 4 different elemental damage types and apply properties on hit.
 */
public class Spell {
    public int id;
    public String name;
    public String description;
    public Integer times;
    public int manaCost;
    public Resistances damageType;
    public Resistances damageType2;
    public Resistances damageType3;
    public Resistances damageType4;
    public String damageDice;
    public String damageDice2;
    public String damageDice3;
    public String damageDice4;
    public Float damageMult;
    public String critMod;
    public Integer accuracy;
    public String onHitProperty;
    public String buffProperty;
    public List<Stats> statBonuses;
    public String tooltip;

    public Float buildUpMod;
    public Float buildUpMod2;
    public Float buildUpMod3;
    public Float buildUpMod4;

    public Spell() {

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

    public int getTimes() {
        return times == null ? 1 : times;
    }

    public float getDamageMult() {
        return damageMult == null ? 1.0f : damageMult;
    }

    public int getCritMod() {
        if (critMod == null || critMod.isBlank())
            return 0;
        String s = critMod.trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            if (s.startsWith("+")) {
                try {
                    return Integer.parseInt(s.substring(1));
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
            return 0;
        }
    }

    public int getAccuracy() {
        return accuracy == null ? 0 : accuracy.intValue();
    }

    public List<Stats> getStatBonuses() {
        return statBonuses;
    }

    public float getBuildUpMod() {
        return buildUpMod == null ? (1.0f / getTimes()) : buildUpMod;
    }

    public float getBuildUpMod2() {
        return buildUpMod2 == null ? (1.0f / getTimes()) : buildUpMod2;
    }

    public float getBuildUpMod3() {
        return buildUpMod3 == null ? (1.0f / getTimes()) : buildUpMod3;
    }

    public float getBuildUpMod4() {
        return buildUpMod4 == null ? (1.0f / getTimes()) : buildUpMod4;
    }

    /**
     * Determine the best stat bonus from the statBonuses list.
     * Returns the highest stat value among the specified stats.
     */
    public int getBestStatBonus(Creature caster) {
        if (statBonuses == null || statBonuses.isEmpty()) {
            return 0;
        }
        int bestStat = 0;
        for (Stats stat : statBonuses) {
            int statValue = caster.getStat(stat);
            if (statValue > bestStat) {
                bestStat = statValue;
            }
        }
        return bestStat;
    }

    /**
     * Check if this is a buff-only spell (no damage, only applies buffProperty to caster).
     */
    public boolean isBuffOnly() {
        return buffProperty != null && !buffProperty.isBlank()
                && (damageDice == null || damageDice.isBlank())
                && (damageDice2 == null || damageDice2.isBlank())
                && (damageDice3 == null || damageDice3.isBlank())
                && (damageDice4 == null || damageDice4.isBlank());
    }

    /**
     * Check if this spell has any damage components.
     */
    public boolean hasDamage() {
        return (damageDice != null && !damageDice.isBlank())
                || (damageDice2 != null && !damageDice2.isBlank())
                || (damageDice3 != null && !damageDice3.isBlank())
                || (damageDice4 != null && !damageDice4.isBlank());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Spell[");
        if (name != null)
            sb.append("name='").append(name).append("', ");
        sb.append("id=").append(id).append(", ");
        sb.append("times=").append(getTimes()).append(", ");
        sb.append("manaCost=").append(manaCost).append(", ");
        if (damageDice != null && !damageDice.isBlank())
            sb.append("damage1='").append(damageDice).append("' (").append(damageType).append("), ");
        if (damageDice2 != null && !damageDice2.isBlank())
            sb.append("damage2='").append(damageDice2).append("' (").append(damageType2).append("), ");
        if (damageDice3 != null && !damageDice3.isBlank())
            sb.append("damage3='").append(damageDice3).append("' (").append(damageType3).append("), ");
        if (damageDice4 != null && !damageDice4.isBlank())
            sb.append("damage4='").append(damageDice4).append("' (").append(damageType4).append("), ");
        sb.append("damageMult=").append(getDamageMult()).append(", ");
        if (critMod != null && !critMod.isBlank())
            sb.append("critMod=").append(critMod).append(", ");
        if (accuracy != null)
            sb.append("accuracy=").append(accuracy).append(", ");
        if (onHitProperty != null && !onHitProperty.isBlank())
            sb.append("onHitProperty='").append(onHitProperty).append("', ");
        if (buffProperty != null && !buffProperty.isBlank())
            sb.append("buffProperty='").append(buffProperty).append("', ");
        if (statBonuses != null && !statBonuses.isEmpty())
            sb.append("statBonuses=").append(statBonuses);
        sb.append("]");
        return sb.toString();
    }
}

package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.util.Dice;

public class Attack {

    public String name;
    public Integer times;
    public String physicalDamageDice;
    public String physicalDamageDice2;
    // Optional property to apply when the attack hits (physical side)
    public String physicalOnHitProperty;
    public String magicDamageDice;
    public String magicDamageDice2;
    // Optional property to apply when the attack hits (magic side)
    public String magicOnHitProperty;
    public Resistances magicDamageType;
    public float damageMultiplier;
    public float magicDamageMultiplier;
    public Resistances damageType;
    public Integer weight;
    public String critMod;
    public Integer accuracy;
    public Integer magicAccuracy;
    public float physBuildUpMod = 1.0f;

    public float magicBuildUpMod = 1.0f;

    public int getTimes() {
        return times == null ? 1 : times;
    }

    public int getWeight() {
        return weight == null ? 1 : weight;
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

    public int getMagicAccuracy() {
        return magicAccuracy == null ? 0 : magicAccuracy.intValue();
    }

    public float getPhysBuildUpMod() {
        return physBuildUpMod;
    }

    public float getMagicBuildUpMod() {
        return magicBuildUpMod;
    }

    public Resistances getDamageTypeEnum() {
        return this.damageType;
    }

    public Resistances getMagicDamageTypeEnum() {
        return this.magicDamageType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Attack[");
        if (name != null)
            sb.append("name='").append(name).append("', ");
        sb.append("times=").append(getTimes()).append(", ");
        if (physicalDamageDice != null && !physicalDamageDice.isBlank())
            sb.append("phys='").append(physicalDamageDice).append("', ");
        if (damageMultiplier != 0)
            sb.append("physMultiplier=").append(damageMultiplier).append(", ");
        if (magicDamageDice != null && !magicDamageDice.isBlank())
            sb.append("magic='").append(magicDamageDice).append("', ");
        if (magicDamageType != null)
            sb.append("magicType=").append(magicDamageType.name()).append(", ");
        if (magicDamageMultiplier != 0)
            sb.append("magicStatBonus=").append(magicDamageMultiplier).append(", ");
        if (damageType != null)
            sb.append("damageType=").append(damageType.name()).append(", ");
        if (critMod != null && !critMod.isBlank())
            sb.append("critMod=").append(critMod).append(", ");
        if (accuracy != null)
            sb.append("accuracy=").append(accuracy).append(", ");
        if (magicAccuracy != null)
            sb.append("magicAccuracy=").append(magicAccuracy).append(", ");
        sb.append("physBuildUpMod=").append(physBuildUpMod).append(", ");
        sb.append("magicBuildUpMod=").append(magicBuildUpMod).append(", ");
        sb.append("weight=").append(getWeight());
        sb.append("]");
        return sb.toString();
    }

    public int rollPhysicalDamage(int statBonus) {
        int total = 0;
        for (int i = 0; i < getTimes(); i++) {
            if (physicalDamageDice != null && !physicalDamageDice.isBlank()) {
                total += Dice.roll(physicalDamageDice);
            }
            total += Math.max(0, statBonus);
        }
        return total;
    }

    /**
     * Roll the magic component of this attack.
     */
    public int rollMagicDamage() {
        int total = 0;
        for (int i = 0; i < getTimes(); i++) {
            if (magicDamageDice != null && !magicDamageDice.isBlank()) {
                total += Dice.roll(magicDamageDice);
            }
        }
        return total;
    }
}
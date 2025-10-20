package com.bapppis.core.util;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.item.Weapon;

public final class WeaponUtil {
    private WeaponUtil() {}

    public static int determineWeaponStatBonus(Creature attacker, Weapon weapon) {
        if (attacker == null || weapon == null) return 0;
        try {
            if (weapon.getFinesse()) {
                return Math.max(0, Math.max(attacker.getStatBonus(Stats.STRENGTH), attacker.getStatBonus(Stats.DEXTERITY)));
            }
            switch (weapon.getWeaponClass()) {
                case MELEE:
                    return Math.max(0, attacker.getStatBonus(Stats.STRENGTH));
                case RANGED:
                    return Math.max(0, attacker.getStatBonus(Stats.DEXTERITY));
                case MAGIC:
                    return Math.max(0, attacker.getStatBonus(Stats.INTELLIGENCE));
                default:
                    return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}

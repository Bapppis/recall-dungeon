package com.bapppis.core.item.ranged;

import com.bapppis.core.Resistances;
import com.bapppis.core.Stats;
import com.bapppis.core.item.Weapon;
import com.bapppis.core.item.WeaponClass;

public abstract class RangedWeapon extends Weapon {
    public RangedWeapon() {
        setWeaponClass(WeaponClass.RANGED);
        setDamageType(Resistances.PIERCING);
    }
    // All ranged weapons use dexterity
    public Stats getPrimaryStat() {
        return Stats.DEXTERITY;
    }
}

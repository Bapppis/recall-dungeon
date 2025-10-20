package com.bapppis.core.item.melee;

import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.item.Weapon;
import com.bapppis.core.item.itemEnums.WeaponClass;

public abstract class MeleeWeapon extends Weapon {
    public MeleeWeapon() {
        setWeaponClass(WeaponClass.MELEE);
    }
    // By default, melee weapons use strength unless finesse
    public Stats getPrimaryStat() {
        return getFinesse() ? Stats.DEXTERITY : Stats.STRENGTH;
    }
}

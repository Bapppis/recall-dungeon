package com.bapppis.core.item.weapon.magic;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.item.Weapon;
import com.bapppis.core.item.itemEnums.WeaponClass;

public abstract class MagicWeapon extends Weapon {
    public MagicWeapon() {
        setWeaponClass(WeaponClass.MAGIC);
    }
    // Magic weapons can have dictated stat bonuses
    public abstract Stats getPrimaryStat();
    // Elemental damage type
    public abstract Resistances getElementalDamageType();
    // Optionally, can have a secondary damage type
    public Resistances getSecondaryDamageType() {
        return null;
    }
}

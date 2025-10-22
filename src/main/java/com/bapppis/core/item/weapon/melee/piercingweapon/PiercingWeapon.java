package com.bapppis.core.item.weapon.melee.piercingweapon;

import com.bapppis.core.Resistances;
import com.bapppis.core.item.weapon.melee.MeleeWeapon;

public class PiercingWeapon extends MeleeWeapon {
    public PiercingWeapon() {
        setDamageType(Resistances.PIERCING);
    }
}

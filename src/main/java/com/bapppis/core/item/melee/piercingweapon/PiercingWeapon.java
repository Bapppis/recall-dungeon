package com.bapppis.core.item.melee.piercingweapon;

import com.bapppis.core.Resistances;
import com.bapppis.core.item.melee.MeleeWeapon;

public class PiercingWeapon extends MeleeWeapon {
    public PiercingWeapon() {
        setDamageType(Resistances.PIERCING);
    }
}

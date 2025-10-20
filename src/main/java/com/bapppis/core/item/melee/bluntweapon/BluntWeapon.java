package com.bapppis.core.item.melee.bluntweapon;

import com.bapppis.core.Resistances;
import com.bapppis.core.item.melee.MeleeWeapon;

public class BluntWeapon extends MeleeWeapon {
    public BluntWeapon() {
        setDamageType(Resistances.BLUDGEONING);
    }
}

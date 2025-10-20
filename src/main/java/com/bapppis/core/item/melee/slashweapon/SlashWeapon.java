package com.bapppis.core.item.melee.slashweapon;

import com.bapppis.core.Resistances;
import com.bapppis.core.item.melee.MeleeWeapon;

public class SlashWeapon extends MeleeWeapon {
    public SlashWeapon() {
        setDamageType(Resistances.SLASHING);
    }
}

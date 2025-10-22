package com.bapppis.core.item.weapon.melee.slashweapon;

import com.bapppis.core.Resistances;
import com.bapppis.core.item.weapon.melee.MeleeWeapon;

public class SlashWeapon extends MeleeWeapon {
    public SlashWeapon() {
        setDamageType(Resistances.SLASHING);
    }
}

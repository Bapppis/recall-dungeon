package com.bapppis.core.item.magic.magicphysicalweapon;

import com.bapppis.core.item.itemEnums.WeaponType;
import com.bapppis.core.item.magic.MagicWeapon;
import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;

public class MagicPhysicalWeapon extends MagicWeapon {
    public MagicPhysicalWeapon() {
        super();
        setWeaponType(WeaponType.MAGIC_PHYSICAL);
    }

    @Override
    public Stats getPrimaryStat() {
        // Example: MagicPhysical weapons use INTELLIGENCE
        return Stats.INTELLIGENCE;
    }

    @Override
    public Resistances getElementalDamageType() {
        // Use damageType field from Weapon
        return getDamageType();
    }
    // Add any specific logic for MagicPhysicalWeapon here
}

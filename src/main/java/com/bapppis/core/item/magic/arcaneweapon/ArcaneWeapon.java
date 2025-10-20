
package com.bapppis.core.item.magic.arcaneweapon;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.item.magic.MagicWeapon;

public class ArcaneWeapon extends MagicWeapon {
    private Stats primaryStat;
    private Resistances elementalDamageType;
    private Resistances secondaryDamageType;
    public ArcaneWeapon(Stats primaryStat, Resistances elementalDamageType, Resistances secondaryDamageType) {
        this.primaryStat = primaryStat;
        this.elementalDamageType = elementalDamageType;
        this.secondaryDamageType = secondaryDamageType;
    }
    @Override
    public Stats getPrimaryStat() {
        return primaryStat;
    }
    @Override
    public Resistances getElementalDamageType() {
        return elementalDamageType;
    }
    @Override
    public Resistances getSecondaryDamageType() {
        return secondaryDamageType;
    }
}

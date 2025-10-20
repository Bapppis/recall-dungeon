package com.bapppis.core.item.magic.staff;

import com.bapppis.core.Resistances;
import com.bapppis.core.Stats;
import com.bapppis.core.item.magic.MagicWeapon;

public class Staff extends MagicWeapon {
    private Stats primaryStat;
    private Resistances elementalDamageType;
    public Staff(Stats primaryStat, Resistances elementalDamageType) {
        this.primaryStat = primaryStat;
        this.elementalDamageType = elementalDamageType;
    }
    @Override
    public Stats getPrimaryStat() {
        return primaryStat;
    }
    @Override
    public Resistances getElementalDamageType() {
        return elementalDamageType;
    }
}

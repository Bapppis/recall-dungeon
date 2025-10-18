package com.bapppis.core.item;

import com.bapppis.core.creature.Attack;
import com.bapppis.core.Resistances;
import com.bapppis.core.Stats;
import java.util.List;

public class Weapon extends Equipment {
    private boolean twoHanded;
    public boolean isTwoHanded() { return twoHanded; }
    public void setTwoHanded(boolean twoHanded) { this.twoHanded = twoHanded; }

    // Weapon-specific fields
    private List<Attack> attacks;
    private List<Attack> versatileAttacks;
    private boolean versatile = false;
    private boolean finesse = false;
    private WeaponClass weaponClass = WeaponClass.MELEE;
    private Resistances damageType;
    private Resistances magicElement;
    private Stats magicStatBonus;
    private List<Stats> magicStatBonuses;




    public List<Attack> getAttacks() { return attacks; }
    public void setAttacks(List<Attack> attacks) { this.attacks = attacks; }
    public List<Attack> getVersatileAttacks() { return versatileAttacks; }
    public void setVersatileAttacks(List<Attack> versatileAttacks) { this.versatileAttacks = versatileAttacks; }
    public boolean getVersatile() { return versatile || (versatileAttacks != null && !versatileAttacks.isEmpty()); }
    public void setVersatile(boolean versatile) { this.versatile = versatile; }
    public boolean getFinesse() { return finesse; }
    public void setFinesse(boolean finesse) { this.finesse = finesse; }
    public WeaponClass getWeaponClass() { return weaponClass; }
    public void setWeaponClass(WeaponClass weaponClass) { this.weaponClass = weaponClass; }
    public Resistances getDamageType() { return damageType; }
    public void setDamageType(Resistances damageType) { this.damageType = damageType; }
    public Resistances getMagicElement() { return magicElement; }
    public void setMagicElement(Resistances magicElement) { this.magicElement = magicElement; }
    public Stats getMagicStatBonus() { return magicStatBonus; }
    public void setMagicStatBonus(Stats magicStatBonus) { this.magicStatBonus = magicStatBonus; }
    public List<Stats> getMagicStatBonuses() { return magicStatBonuses; }
    public void setMagicStatBonuses(List<Stats> magicStatBonuses) { this.magicStatBonuses = magicStatBonuses; }


}

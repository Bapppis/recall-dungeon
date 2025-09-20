package com.bapppis.core.item;

import com.bapppis.core.creature.Creature;
import java.util.Map;

public class Equipment implements Item {
    private String physicalDamageDice;
    private String magicDamageDice;

    public String getPhysicalDamageDice() {
        return physicalDamageDice;
    }

    public void setPhysicalDamageDice(String dice) {
        this.physicalDamageDice = dice;
    }

    public String getMagicDamageDice() {
        return magicDamageDice;
    }

    public void setMagicDamageDice(String dice) {
        this.magicDamageDice = dice;
    }
    private int id;
    private String name;
    private String description;
    private ItemType itemType;
    private EquipmentSlot equipmentSlot;
    private boolean twoHanded;
    private Rarity rarity;
    private Map<String, Integer> stats;
    private Map<String, Integer> resistances;
    private boolean finesse = false;
    // Indicates if the weapon is versatile (can be used one- or two-handed)
    private boolean versatile = false;

    // Weapon class (melee, ranged, magic)
    private WeaponClass weaponClass = WeaponClass.MELEE;

    // Always required: one of BLUDGEONING, PIERCING, SLASHING
    private com.bapppis.core.creature.Creature.Resistances damageType;

    // Optional: magical element (can be null if not present)
    private com.bapppis.core.creature.Creature.Resistances magicElement;

    public com.bapppis.core.creature.Creature.Resistances getDamageType() {
        return damageType;
    }

    public void setDamageType(com.bapppis.core.creature.Creature.Resistances damageType) {
        this.damageType = damageType;
    }

    public com.bapppis.core.creature.Creature.Resistances getMagicElement() {
        return magicElement;
    }

    public void setMagicElement(com.bapppis.core.creature.Creature.Resistances magicElement) {
        this.magicElement = magicElement;
    }

    public WeaponClass getWeaponClass() {
        return weaponClass;
    }

    public void setWeaponClass(WeaponClass weaponClass) {
        this.weaponClass = weaponClass;
    }

    public boolean isFinesse() {
        return finesse;
    }

    public void setFinesse(boolean finesse) {
        this.finesse = finesse;
    }

    public boolean isVersatile() {
        return versatile;
    }
    public void setVersatile(boolean versatile) {
        this.versatile = versatile;
    }
    public Equipment() {}

    @Override
    public int getId() { return id; }
    @Override
    public String getName() { return name; }
    @Override
    public String getDescription() { return description; }
    @Override
    public ItemType getType() { return itemType; }
    @Override
    public EquipmentSlot getSlot() { return equipmentSlot; }
    @Override
    public boolean isTwoHanded() { return twoHanded; }
    @Override
    public void setSlot(EquipmentSlot slot) { this.equipmentSlot = slot; }
    @Override
    public void setTwoHanded(boolean twoHanded) { this.twoHanded = twoHanded; }
    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public Map<String, Integer> getStats() { return stats; }
    public void setStats(Map<String, Integer> stats) { this.stats = stats; }
    public Map<String, Integer> getResistances() { return resistances; }
    public void setResistances(Map<String, Integer> resistances) { this.resistances = resistances; }

    @Override
    public void onApply(Creature creature) {}
    @Override
    public void onRemove(Creature creature) {}

    @Override
    public String toString() {
    return "id: " + id + "\n" +
        "name: '" + name + "'\n" +
        "description: '" + description + "'\n" +
        "type: " + itemType + "\n" +
        "slot: " + equipmentSlot + "\n" +
        "twoHanded: " + twoHanded + "\n" +
        "rarity: " + rarity + "\n" +
        "stats: " + stats + "\n" +
        "resistances: " + resistances + "\n" +
        (weaponClass != null ? "weaponClass: " + weaponClass + "\n" : "") +
        (damageType != null ? "damageType: " + damageType + "\n" : "") +
        (magicElement != null ? "magicElement: " + magicElement + "\n" : "") +
        (physicalDamageDice != null ? "physicalDamageDice: " + physicalDamageDice + "\n" : "") +
        (magicDamageDice != null ? "magicDamageDice: " + magicDamageDice + "\n" : "");
    }
}

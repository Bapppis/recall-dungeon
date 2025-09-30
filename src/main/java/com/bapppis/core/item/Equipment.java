package com.bapppis.core.item;

import com.bapppis.core.creature.Attack;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.util.Dice;

import java.util.List;
import java.util.Map;

public class Equipment implements Item {
    private List<Attack> attacks;

    public List<Attack> getAttacks() {
        return attacks;
    }

    private String healingDice;

    public String getHealingDice() {
        return healingDice;
    }

    public void setHealingDice(String healingDice) {
        this.healingDice = healingDice;
    }

    private int id;
    private String name;
    private String description;
    // Backwards-compatible: `tooltip` in JSON can be either a single String or an array of Strings.
    // We keep the raw value here and normalize in getTooltip().
    private Object tooltip;
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

    public Equipment() {
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTooltip() {
        if (tooltip == null) return null;
        if (tooltip instanceof String) return (String) tooltip;
        // If the JSON provided an array, Gson will deserialize it as a java.util.List.
        if (tooltip instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) tooltip;
            // Join lines, preserving empty strings as paragraph breaks
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                Object o = list.get(i);
                if (o != null) sb.append(o.toString());
                if (i < list.size() - 1) sb.append('\n');
            }
            return sb.toString();
        }
        // Fallback to toString()
        return tooltip.toString();
    }

    @Override
    public ItemType getType() {
        return itemType;
    }

    @Override
    public EquipmentSlot getSlot() {
        return equipmentSlot;
    }

    @Override
    public boolean isTwoHanded() {
        return twoHanded;
    }

    @Override
    public void setSlot(EquipmentSlot slot) {
        this.equipmentSlot = slot;
    }

    @Override
    public void setTwoHanded(boolean twoHanded) {
        this.twoHanded = twoHanded;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    // Optional defensive/offensive chance modifiers applied while equipped
    private float crit = 0f; // Crit chance between 0 and 100
    private float dodge = 0f; // Dodge chance between 0 and 100
    private float block = 0f; // Block chance between 0 and 100

    public float getCrit() {
        return crit;
    }

    public void setCrit(float crit) {
        this.crit = Math.max(0f, Math.min(100f, crit));
    }

    public float getDodge() {
        return dodge;
    }

    public void setDodge(float dodge) {
        this.dodge = Math.max(0f, Math.min(100f, dodge));
    }

    public float getBlock() {
        return block;
    }

    public void setBlock(float block) {
        this.block = Math.max(0f, Math.min(100f, block));
    }

    public void setStats(Map<String, Integer> stats) {
        this.stats = stats;
    }

    public Map<String, Integer> getResistances() {
        return resistances;
    }

    public void setResistances(Map<String, Integer> resistances) {
        this.resistances = resistances;
    }

    @Override
    public void onApply(Creature creature) {
        if (healingDice != null && !healingDice.isEmpty()) {
            int heal = Dice.roll(healingDice);
            creature.setCurrentHp(Math.min(creature.getMaxHp(), creature.getCurrentHp() + heal));
        }
        // Apply crit/dodge/block modifiers: add raw values to the creature so that
        // unequipping will correctly restore the original raw values. Effective
        // clamping is applied during combat checks.
        if (crit != 0f) {
            creature.setCrit(creature.getCrit() + crit);
        }
        if (dodge != 0f) {
            creature.setDodge(creature.getDodge() + dodge);
        }
        if (block != 0f) {
            creature.setBlock(creature.getBlock() + block);
        }
    }

    @Override
    public void onRemove(Creature creature) {
        // Remove crit/dodge/block modifiers
        if (crit != 0f) {
            creature.setCrit(creature.getCrit() - crit);
        }
        if (dodge != 0f) {
            creature.setDodge(creature.getDodge() - dodge);
        }
        if (block != 0f) {
            creature.setBlock(creature.getBlock() - block);
        }
    }

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
                (magicElement != null ? "magicElement: " + magicElement + "\n" : "");
    }

    // We rely on a manually authored `description` field for UI/tooltips.
}

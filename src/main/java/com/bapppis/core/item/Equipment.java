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

    // Optional attacks used when a versatile weapon is wielded two-handed.
    private List<Attack> versatileAttacks;

    public List<Attack> getVersatileAttacks() {
        return versatileAttacks;
    }

    public void setVersatileAttacks(List<Attack> versatileAttacks) {
        this.versatileAttacks = versatileAttacks;
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

    private com.bapppis.core.creature.Creature.Stats magicStatBonus;
    // Optional: allow multiple stats to serve as magic stat bonus candidates
    private java.util.List<com.bapppis.core.creature.Creature.Stats> magicStatBonuses;

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

    public com.bapppis.core.creature.Creature.Stats getMagicStatBonus() {
        return magicStatBonus;
    }

    public void setMagicStatBonus(com.bapppis.core.creature.Creature.Stats magicStatBonus) {
        this.magicStatBonus = magicStatBonus;
    }

    public java.util.List<com.bapppis.core.creature.Creature.Stats> getMagicStatBonuses() {
        return magicStatBonuses;
    }

    public void setMagicStatBonuses(java.util.List<com.bapppis.core.creature.Creature.Stats> magicStatBonuses) {
        this.magicStatBonuses = magicStatBonuses;
    }

    public WeaponClass getWeaponClass() {
        return weaponClass;
    }

    public void setWeaponClass(WeaponClass weaponClass) {
        this.weaponClass = weaponClass;
    }

    public boolean getFinesse() {
        return finesse;
    }

    public void setFinesse(boolean finesse) {
        this.finesse = finesse;
    }

    public boolean getVersatile() {
        return versatile || (versatileAttacks != null && !versatileAttacks.isEmpty());
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
        if (tooltip == null)
            return null;
        if (tooltip instanceof String)
            return (String) tooltip;
        // If the JSON provided an array, Gson will deserialize it as a java.util.List.
        if (tooltip instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) tooltip;
            // Join lines, preserving empty strings as paragraph breaks
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                Object o = list.get(i);
                if (o != null)
                    sb.append(o.toString());
                if (i < list.size() - 1)
                    sb.append('\n');
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

    // Regens
    private int hpRegen = 0;
    private int staminaRegen = 0;
    private int manaRegen = 0;
    // Optional defensive/offensive chance modifiers applied while equipped
    private float crit = 0f; // Crit chance between 0 and 100
    private float dodge = 0f; // Dodge chance between 0 and 100
    private float block = 0f; // Block chance between 0 and 100
    private float magicResist = 0f; // Magic resist between 0 and 100

    public int getHpRegen() {
        return hpRegen;
    }

    public void setHpRegen(int hpRegen) {
        this.hpRegen = Math.max(0, hpRegen);
    }

    public int getStaminaRegen() {
        return staminaRegen;
    }

    public void setStaminaRegen(int staminaRegen) {
        this.staminaRegen = Math.max(0, staminaRegen);
    }

    public int getManaRegen() {
        return manaRegen;
    }

    public void setManaRegen(int manaRegen) {
        this.manaRegen = Math.max(0, manaRegen);
    }

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

    public float getMagicResist() {
        return magicResist;
    }

    public void setMagicResist(float magicResist) {
        this.magicResist = Math.max(0f, Math.min(100f, magicResist));
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
        if (magicResist != 0f) {
            creature.setMagicResist(creature.getMagicResist() - magicResist);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(id).append('\n');
        sb.append("name: '").append(name).append("'\n");
        if (description != null && !description.isEmpty())
            sb.append("description: '").append(description).append("'\n");
        sb.append("type: ").append(itemType).append('\n');
        sb.append("slot: ").append(equipmentSlot).append('\n');
        sb.append("twoHanded: ").append(twoHanded).append('\n');
        sb.append("versatile: ").append(getVersatile()).append('\n');
        sb.append("rarity: ").append(rarity).append('\n');
        String tt = getTooltip();
        if (tt != null && !tt.isEmpty())
            sb.append("tooltip: ").append(tt.replace("\n", "\\n")).append('\n');
        if (healingDice != null && !healingDice.isEmpty())
            sb.append("healingDice: ").append(healingDice).append('\n');
        if (stats != null && !stats.isEmpty())
            sb.append("stats: ").append(stats).append('\n');
        if (resistances != null && !resistances.isEmpty())
            sb.append("resistances: ").append(resistances).append('\n');
        sb.append("finesse: ").append(finesse).append('\n');
        sb.append("crit: ").append(crit).append('\n');
        sb.append("dodge: ").append(dodge).append('\n');
        sb.append("block: ").append(block).append('\n');
        if (weaponClass != null)
            sb.append("weaponClass: ").append(weaponClass).append('\n');
        if (damageType != null)
            sb.append("damageType: ").append(damageType).append('\n');
        if (magicElement != null)
            sb.append("magicElement: ").append(magicElement).append('\n');
        if (magicStatBonus != null)
            sb.append("magicStatBonus: ").append(magicStatBonus).append('\n');
        if (magicStatBonuses != null && !magicStatBonuses.isEmpty())
            sb.append("magicStatBonuses: ").append(magicStatBonuses).append('\n');

        if (attacks != null && !attacks.isEmpty()) {
            sb.append("attacks:\n");
            for (Attack a : attacks) {
                if (a == null) {
                    sb.append("  - null\n");
                } else {
                    String aStr = a.toString();
                    // indent multi-line attack strings
                    String[] parts = aStr.split("\n");
                    sb.append("  - ").append(parts[0]).append('\n');
                    for (int i = 1; i < parts.length; i++)
                        sb.append("      ").append(parts[i]).append('\n');
                }
            }
        }

        if (versatileAttacks != null && !versatileAttacks.isEmpty()) {
            sb.append("versatileAttacks:\n");
            for (Attack a : versatileAttacks) {
                if (a == null) {
                    sb.append("  - null\n");
                } else {
                    String aStr = a.toString();
                    String[] parts = aStr.split("\n");
                    sb.append("  - ").append(parts[0]).append('\n');
                    for (int i = 1; i < parts.length; i++)
                        sb.append("      ").append(parts[i]).append('\n');
                }
            }
        }

        return sb.toString();
    }
}

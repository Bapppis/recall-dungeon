package com.bapppis.core.property;

import com.bapppis.core.Resistances;
import com.bapppis.core.ResBuildUp;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.util.Dice;
import com.bapppis.core.util.ResistanceUtil;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Property {
    @SerializedName("resistances")
    private Map<Resistances, Integer> resistanceModifiers;

    @SerializedName("visionRange")
    private Integer visionRange;

    private int id;
    private String name;
    private PropertyType type;
    private String description;
    private Map<Stats, Integer> statModifiers;
    private Object tooltip;
    private Integer duration;
    private Resistances damageType;
    private String damageDice;
    private Integer hpRegen;
    private Integer manaRegen;
    private Integer staminaRegen;
    private Float crit;
    private Float dodge;
    private Float block;
    private Float magicResist;
    private Integer accuracy;
    private Integer magicAccuracy;
    private Integer maxHp;
    private Integer maxStamina;
    private Integer maxMana;
    private Float maxHpPercentage;
    private Float maxStaminaPercentage;
    private Float maxManaPercentage;
    private transient Integer appliedMaxHpDelta = null;
    private transient Integer appliedMaxStaminaDelta = null;
    private transient Integer appliedMaxManaDelta = null;
    private transient java.util.EnumMap<ResBuildUp, Integer> appliedResBuildUpPrev = null;
    @SerializedName("resBuildUp")
    private Map<ResBuildUp, Integer> resBuildUpModifiers;

    protected Property() {
    }

    protected Property(Property other) {
        if (other == null)
            return;
        this.id = other.id;
        this.name = other.name;
        this.type = other.type;
        this.description = other.description;
        this.damageType = other.damageType;
        this.damageDice = other.damageDice;
        this.statModifiers = other.statModifiers;
        this.resistanceModifiers = other.resistanceModifiers;
    this.resBuildUpModifiers = other.resBuildUpModifiers;
        this.visionRange = other.visionRange;
        this.tooltip = other.tooltip;
        this.duration = other.duration;
        this.hpRegen = other.hpRegen;
        this.manaRegen = other.manaRegen;
        this.staminaRegen = other.staminaRegen;
        this.crit = other.crit;
        this.dodge = other.dodge;
        this.block = other.block;
        this.magicResist = other.magicResist;
        this.maxHp = other.maxHp;
        this.maxStamina = other.maxStamina;
        this.maxMana = other.maxMana;
        this.maxHpPercentage = other.maxHpPercentage;
        this.maxStaminaPercentage = other.maxStaminaPercentage;
        this.maxManaPercentage = other.maxManaPercentage;
        // Do NOT copy transient applied deltas - they are per-instance
        this.appliedMaxHpDelta = null;
        this.appliedMaxStaminaDelta = null;
        this.appliedMaxManaDelta = null;
    }

    /** Public factory to create a per-creature copy. */
    public Property copy() {
        return new Property(this);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Resistances getDamageType() {
        return damageType;
    }

    public void setDamageType(Resistances damageType) {
        this.damageType = damageType;
    }

    public String getDamageDice() {
        return damageDice;
    }

    public void setDamageDice(String damageDice) {
        this.damageDice = damageDice;
    }

    public Integer getHpRegen() {
        return hpRegen;
    }

    public void setHpRegen(Integer hpRegen) {
        this.hpRegen = hpRegen;
    }

    public Integer getManaRegen() {
        return manaRegen;
    }

    public void setManaRegen(Integer manaRegen) {
        this.manaRegen = manaRegen;
    }

    public Integer getStaminaRegen() {
        return staminaRegen;
    }

    public void setStaminaRegen(Integer staminaRegen) {
        this.staminaRegen = staminaRegen;
    }

    public Float getCrit() {
        return crit;
    }

    public void setCrit(Float crit) {
        this.crit = crit;
    }

    public Float getDodge() {
        return dodge;
    }

    public void setDodge(Float dodge) {
        this.dodge = dodge;
    }

    public Float getBlock() {
        return block;
    }

    public void setBlock(Float block) {
        this.block = block;
    }

    public Float getMagicResist() {
        return magicResist;
    }

    public void setMagicResist(Float magicResist) {
        this.magicResist = magicResist;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getMagicAccuracy() {
        return magicAccuracy;
    }

    public void setMagicAccuracy(Integer magicAccuracy) {
        this.magicAccuracy = magicAccuracy;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(Integer maxHp) {
        this.maxHp = maxHp;
    }

    public Integer getMaxStamina() {
        return maxStamina;
    }

    public void setMaxStamina(Integer maxStamina) {
        this.maxStamina = maxStamina;
    }

    public Integer getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(Integer maxMana) {
        this.maxMana = maxMana;
    }

    public Float getMaxHpPercentage() {
        return maxHpPercentage;
    }

    public void setMaxHpPercentage(Float maxHpPercentage) {
        this.maxHpPercentage = maxHpPercentage;
    }

    public Float getMaxStaminaPercentage() {
        return maxStaminaPercentage;
    }

    public void setMaxStaminaPercentage(Float maxStaminaPercentage) {
        this.maxStaminaPercentage = maxStaminaPercentage;
    }

    public Float getMaxManaPercentage() {
        return maxManaPercentage;
    }

    public void setMaxManaPercentage(Float maxManaPercentage) {
        this.maxManaPercentage = maxManaPercentage;
    }

    /**
     * Return tooltip text if present. Mirrors the logic in `Equipment.getTooltip()`
     * so property JSON can provide either a String or an array of lines.
     */
    public String getTooltip() {
        if (tooltip == null)
            return null;
        if (tooltip instanceof String)
            return (String) tooltip;
        if (tooltip instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) tooltip;
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
        return tooltip.toString();
    }

    public Map<Stats, Integer> getStatModifiers() {
        return statModifiers;
    }

    public Map<Resistances, Integer> getResistanceModifiers() {
        return resistanceModifiers;
    }

    public Map<ResBuildUp, Integer> getResBuildUpModifiers() { return resBuildUpModifiers; }

    public Integer getVisionRangeModifier() {
        return visionRange;
    }

    /**
     * Default shared application behaviour: modify stats, resistances and vision
     * range.
     */
    public void onApply(Creature creature) {
        if (statModifiers != null) {
            for (Map.Entry<Stats, Integer> entry : statModifiers.entrySet()) {
                creature.modifyStat(entry.getKey(), entry.getValue());
            }
        }
        if (resistanceModifiers != null) {
            for (Map.Entry<Resistances, Integer> entry : resistanceModifiers.entrySet()) {
                creature.modifyResistance(entry.getKey(), entry.getValue());
            }
        }
        if (visionRange != null) {
            creature.setVisionRange(creature.getVisionRange() + visionRange);
        }
        // Apply regen deltas if provided (can be negative for debuffs)
        if (hpRegen != null && hpRegen != 0) {
            creature.modifyHpRegen(hpRegen);
        }
        if (manaRegen != null && manaRegen != 0) {
            creature.modifyManaRegen(manaRegen);
        }
        if (staminaRegen != null && staminaRegen != 0) {
            creature.modifyStaminaRegen(staminaRegen);
        }
        // Apply derived stat deltas
        if (crit != null && crit != 0f) {
            creature.modifyPropertyCrit(crit);
        }
        if (dodge != null && dodge != 0f) {
            creature.modifyPropertyDodge(dodge);
        }
        if (block != null && block != 0f) {
            creature.modifyPropertyBlock(block);
        }
        if (magicResist != null && magicResist != 0f) {
            creature.modifyPropertyMagicResist(magicResist);
        }
        // Apply accuracy modifiers if present
        if (accuracy != null && accuracy != 0) {
            creature.modifyPropertyAccuracy(accuracy);
        }
        if (magicAccuracy != null && magicAccuracy != 0) {
            creature.modifyPropertyMagicAccuracy(magicAccuracy);
        }
        // Apply max pool deltas
        if (maxHp != null && maxHp != 0) {
            creature.setMaxHp(creature.getMaxHp() + maxHp);
        }
        // Apply percentage multiplier to maxHp if present. Compute delta as
        // floor(currentMaxHp * (percentage - 1)). Store delta in transient
        // field so we can revert exactly on removal.
        if (maxHpPercentage != null) {
            int current = creature.getMaxHp();
            double multiplier = maxHpPercentage.doubleValue();
            // Desired new max = floor(current * multiplier)
            int newMax = (int) Math.floor(current * multiplier);
            int delta = newMax - current;
            if (delta != 0) {
                creature.setMaxHp(current + delta);
                this.appliedMaxHpDelta = delta;
            } else {
                this.appliedMaxHpDelta = 0;
            }
        }
        if (maxStamina != null && maxStamina != 0) {
            creature.setMaxStamina(creature.getMaxStamina() + maxStamina);
        }
        if (maxStaminaPercentage != null) {
            int current = creature.getMaxStamina();
            double multiplier = maxStaminaPercentage.doubleValue();
            int newMax = (int) Math.floor(current * multiplier);
            int delta = newMax - current;
            if (delta != 0) {
                creature.setMaxStamina(current + delta);
                this.appliedMaxStaminaDelta = delta;
            } else {
                this.appliedMaxStaminaDelta = 0;
            }
        }
        if (maxMana != null && maxMana != 0) {
            creature.setMaxMana(creature.getMaxMana() + maxMana);
        }
        if (maxManaPercentage != null) {
            int current = creature.getMaxMana();
            double multiplier = maxManaPercentage.doubleValue();
            int newMax = (int) Math.floor(current * multiplier);
            int delta = newMax - current;
            if (delta != 0) {
                creature.setMaxMana(current + delta);
                this.appliedMaxManaDelta = delta;
            } else {
                this.appliedMaxManaDelta = 0;
            }
        }
        // Apply ResBuildUp modifiers (absolute or delta). If value == -1 => immunity.
        if (resBuildUpModifiers != null && !resBuildUpModifiers.isEmpty()) {
            if (this.appliedResBuildUpPrev == null) this.appliedResBuildUpPrev = new java.util.EnumMap<>(ResBuildUp.class);
            for (Map.Entry<ResBuildUp, Integer> e : resBuildUpModifiers.entrySet()) {
                ResBuildUp key = e.getKey();
                Integer val = e.getValue();
                if (val == null) continue;
                if (val == -1) {
                    // save previous absolute and set immunity
                    this.appliedResBuildUpPrev.put(key, creature.getResBuildUp(key));
                    creature.setResBuildUpAbsolute(key, -1);
                } else {
                    creature.modifyResBuildUp(key, val);
                }
            }
        }
    }

    /**
     * Default shared removal behaviour: undo stat, resistance and vision changes.
     */
    public void onRemove(Creature creature) {
        if (statModifiers != null) {
            for (Map.Entry<Stats, Integer> entry : statModifiers.entrySet()) {
                creature.modifyStat(entry.getKey(), -entry.getValue());
            }
        }
        if (resistanceModifiers != null) {
            for (Map.Entry<Resistances, Integer> entry : resistanceModifiers.entrySet()) {
                creature.modifyResistance(entry.getKey(), -entry.getValue());
            }
        }
        if (visionRange != null) {
            creature.setVisionRange(creature.getVisionRange() - visionRange);
        }
        if (hpRegen != null && hpRegen != 0) {
            creature.modifyHpRegen(-hpRegen);
        }
        if (manaRegen != null && manaRegen != 0) {
            creature.modifyManaRegen(-manaRegen);
        }
        if (staminaRegen != null && staminaRegen != 0) {
            creature.modifyStaminaRegen(-staminaRegen);
        }
        // Revert derived stat deltas
        if (crit != null && crit != 0f) {
            creature.modifyPropertyCrit(-crit);
        }
        if (dodge != null && dodge != 0f) {
            creature.modifyPropertyDodge(-dodge);
        }
        if (block != null && block != 0f) {
            creature.modifyPropertyBlock(-block);
        }
        // Revert property accuracy modifiers if any (handled in onApply when present)
        // No-op here because Property currently models statModifiers as enum-keyed map
        // and
        // the per-property accuracy fields were not modeled separately.
        if (magicResist != null && magicResist != 0f) {
            creature.modifyPropertyMagicResist(-magicResist);
        }
        if (accuracy != null && accuracy != 0) {
            creature.modifyPropertyAccuracy(-accuracy);
        }
        if (magicAccuracy != null && magicAccuracy != 0) {
            creature.modifyPropertyMagicAccuracy(-magicAccuracy);
        }
        // Revert max pool deltas
        if (maxHp != null && maxHp != 0) {
            creature.setMaxHp(creature.getMaxHp() - maxHp);
        }
        // Revert percentage-applied deltas if they were computed when applied.
        if (this.appliedMaxHpDelta != null) {
            creature.setMaxHp(creature.getMaxHp() - this.appliedMaxHpDelta);
            this.appliedMaxHpDelta = null;
        }
        if (maxStamina != null && maxStamina != 0) {
            creature.setMaxStamina(creature.getMaxStamina() - maxStamina);
        }
        if (this.appliedMaxStaminaDelta != null) {
            creature.setMaxStamina(creature.getMaxStamina() - this.appliedMaxStaminaDelta);
            this.appliedMaxStaminaDelta = null;
        }
        if (maxMana != null && maxMana != 0) {
            creature.setMaxMana(creature.getMaxMana() - maxMana);
        }
        if (this.appliedMaxManaDelta != null) {
            creature.setMaxMana(creature.getMaxMana() - this.appliedMaxManaDelta);
            this.appliedMaxManaDelta = null;
        }
        // Revert any ResBuildUp changes applied by this property.
        if (resBuildUpModifiers != null && !resBuildUpModifiers.isEmpty()) {
            for (Map.Entry<ResBuildUp, Integer> e : resBuildUpModifiers.entrySet()) {
                ResBuildUp key = e.getKey();
                Integer val = e.getValue();
                if (val == null) continue;
                if (val == -1) {
                    int prev = 0;
                    if (this.appliedResBuildUpPrev != null && this.appliedResBuildUpPrev.containsKey(key)) prev = this.appliedResBuildUpPrev.get(key);
                    creature.setResBuildUpAbsolute(key, prev);
                } else {
                    creature.modifyResBuildUp(key, -val);
                }
            }
            this.appliedResBuildUpPrev = null;
        }
    }

    /**
     * Per-turn tick hook. Default implementation does nothing. Subclasses
     * (e.g. buff/debuff) can override to perform per-turn effects.
     */
    public void onTick(Creature creature) {
        if (damageDice != null && !damageDice.isBlank()) {
            try {
                int raw = Dice.roll(damageDice); // adjust if Dice API differs
                int after = ResistanceUtil.getDamageAfterResistance(creature, raw, damageType);
                if (after > 0) {
                    creature.modifyHp(-after);
                }
            } catch (Exception ignored) {
                // Swallow to avoid ticking from crashing the game; log if desired.
            }
        }
        // Also apply flat hpRegen (positive heals, negative damages) per tick.
        if (hpRegen != null && hpRegen != 0) {
            creature.modifyHp(hpRegen);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Property[id=").append(id)
                .append(", name=").append(name == null ? "<unnamed>" : name)
                .append(", type=").append(type == null ? "<unknown>" : type.name())
                .append("]");
        if (damageDice != null && !damageDice.isEmpty()) {
            sb.append(" damageDice=").append(damageDice);
        }
        if (damageType != null) {
            sb.append(" damageType=").append(damageType.name());
        }
        if (statModifiers != null && !statModifiers.isEmpty()) {
            sb.append(" stats=").append(statModifiers.toString());
        }
        if (resistanceModifiers != null && !resistanceModifiers.isEmpty()) {
            sb.append(" resists=").append(resistanceModifiers.toString());
        }
        if (visionRange != null) {
            sb.append(" visionRange=").append(visionRange);
        }
        if (duration != null) {
            sb.append(" duration=").append(duration);
        }
        if (hpRegen != null && hpRegen != 0) {
            sb.append(" hpRegen=").append(hpRegen);
        }
        if (manaRegen != null && manaRegen != 0) {
            sb.append(" manaRegen=").append(manaRegen);
        }
        if (staminaRegen != null && staminaRegen != 0) {
            sb.append(" staminaRegen=").append(staminaRegen);
        }
        return sb.toString();
    }
}
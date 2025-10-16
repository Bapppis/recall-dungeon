package com.bapppis.core.creature;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import com.bapppis.core.util.AttackUtil;
import com.bapppis.core.util.WeaponUtil;
import com.bapppis.core.util.ResistanceUtil;
import com.bapppis.core.util.LevelUtil;
import com.bapppis.core.util.StatUtil;

import com.bapppis.core.property.Property;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.Equipment;
import com.bapppis.core.item.EquipmentSlot;

public abstract class Creature {
    // --- Fields ---
    private int id;
    private String name;
    private int visionRange = 2; // default vision range
    private int level;
    private int xp;
    private Integer enemyXp;
    private int baseHp;
    private int maxHp;
    private int currentHp;
    private int hpLvlBonus;
    private int currentMana;
    private int currentStamina;
    private int maxMana = 100;
    private int baseMaxMana = 100;
    private int maxStamina = 100;
    private int baseMaxStamina = 100;
    private int baseHpRegen = 0;
    private int baseStaminaRegen;
    private int baseManaRegen = 0;
    private Size size;
    private Type type;
    private CreatureType creatureType;
    private EnumMap<Stats, Integer> stats;
    private EnumMap<Resistances, Integer> resistances;
    private float baseCrit;
    private float baseDodge;
    private float baseBlock;
    private float baseMagicResist;
    private int hpRegen;
    private int staminaRegen;
    private int manaRegen;
    private float crit;
    private float dodge;
    private float block;
    private float magicResist;
    // Base accuracy bonuses (added to to-hit rolls). Renamed from
    // rollAttack/MagicBonus
    private int accuracy;
    private int magicAccuracy;
    // Equipment aggregates (sum of all equipped item contributions)
    private final java.util.EnumMap<Stats, Integer> equipmentStats = new java.util.EnumMap<>(Stats.class);
    private final java.util.EnumMap<Resistances, Integer> equipmentResists = new java.util.EnumMap<>(Resistances.class);
    private int equipmentHpRegen = 0; // Sum of all equipped item HP regen contributions
    private int equipmentStaminaRegen = 0; // Sum of all equipped item stamina regen contributions
    private int equipmentManaRegen = 0; // Sum of all equipped item mana regen contributions
    private float equipmentCrit = 0f;
    private float equipmentDodge = 0f;
    private float equipmentBlock = 0f;
    private float equipmentMagicResist = 0f;
    private int equipmentAccuracy = 0;
    private int equipmentMagicAccuracy = 0;
    // Property-derived accumulators (sum of applied property deltas)
    private float propertyCrit = 0f;
    private float propertyDodge = 0f;
    private float propertyBlock = 0f;
    private float propertyMagicResist = 0f;
    private int propertyAccuracy = 0;
    private int propertyMagicAccuracy = 0;
    private int statPoints = 0;
    // Cached per-stat bonuses (e.g. STR 14 -> +4). LUCK bonus equals the raw
    // luck value.
    private final java.util.EnumMap<Stats, Integer> statBonuses = new java.util.EnumMap<>(Stats.class);
    private final PropertyManager propertyManager = new PropertyManager(this);
    private String description;
    private EnumMap<EquipmentSlot, Item> equipment = new EnumMap<>(EquipmentSlot.class);
    private Inventory inventory = new Inventory();
    // Attacks defined on creatures (Gson will fill this from JSON)
    private java.util.List<Attack> attacks = new java.util.ArrayList<>();
    // Optional sprite key loaded from JSON (e.g. "player_biggles",
    // "monster_goblin")
    private String sprite;

    public String getSprite() {
        return sprite;
    }

    // --- Enums ---
    public enum Size {
        SMALL,
        MEDIUM,
        LARGE,
        HUGE,
        GARGANTUAN,
    }

    public enum Type {
        PLAYER,
        NPC,
        ENEMY,
    }

    public enum CreatureType {
        BEAST,
        CONSTRUCT,
        DRAGON,
        ELEMENTAL,
        HUMANOID,
        PLANT,
        UNDEAD,
        UNKNOWN,
    }

    public enum Stats {
        STRENGTH,
        DEXTERITY,
        CONSTITUTION,
        INTELLIGENCE,
        WISDOM,
        CHARISMA,
        LUCK,
    }

    public enum Resistances {
        FIRE,
        WATER,
        WIND,
        ICE,
        NATURE,
        LIGHTNING,
        LIGHT,
        DARKNESS,
        BLUDGEONING,
        PIERCING,
        SLASHING,
        TRUE,
    }

    // --- Constructor ---
    public Creature() {
        stats = new EnumMap<>(Stats.class);
        for (Stats stat : Stats.values()) {
            if (stat == Stats.LUCK) {
                stats.put(stat, 1); // Luck default is 1
            } else {
                stats.put(stat, 10); // other stats default to 10
            }
        }
        // Initialize cached stat bonuses before derived stats that may rely on them
        recalcStatBonuses();
        resistances = new EnumMap<>(Resistances.class);
        for (Resistances res : Resistances.values()) {
            resistances.put(res, 100); // default resistance 100%
        }
        size = Size.MEDIUM; // default size
        type = Type.ENEMY; // default type
        recalcDerivedStats();
    }

    // --- Getters and Setters ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVisionRange() {
        return visionRange;
    }

    public void setVisionRange(int visionRange) {
        this.visionRange = visionRange;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void addXp(int xp) {
        int currentXp = this.xp + xp;
        if (this.level >= LevelUtil.getMaxLevel()) {
            this.level = LevelUtil.getMaxLevel();
            this.xp = 0;
            return;
        }
        if (currentXp >= LevelUtil.xpForNextLevel(this.level)) {
            levelUp(currentXp);
            return;
        }
        this.xp = currentXp;
    }

    public void levelUp(int xp) {
        xp -= LevelUtil.xpForNextLevel(this.level);
        this.level++;

        this.addStatPoint();
        this.addStatPoint();
        this.updateMaxHp();

        this.xp = 0;
        addXp(xp);
    }

    public Integer getEnemyXp() {
        return enemyXp;
    }

    public void setEnemyXp(Integer enemyXp) {
        this.enemyXp = enemyXp;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        if (maxHp < 1) {
            maxHp = 1; // Ensure maxHp is at least 1
        }
        // Preserve the currentHp/maxHp ratio, rounding down
        double ratio = this.maxHp > 0 ? (double) currentHp / this.maxHp : 1.0;
        this.maxHp = maxHp;
        this.currentHp = Math.max(1, (int) (this.maxHp * ratio));
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int hp) {
        this.currentHp = Math.max(0, hp);
    }

    public int getHpLvlBonus() {
        return hpLvlBonus;
    }

    public void setHpLvlBonus(int hpLvlBonus) {
        this.hpLvlBonus = hpLvlBonus;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getBaseMaxMana() {
        return baseMaxMana;
    }

    public int getCurrentStamina() {
        return currentStamina;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public int getBaseMaxStamina() {
        return baseMaxStamina;
    }

    public void setMaxMana(int maxMana) {
        if (maxMana < 0) {
            maxMana = 0; // Ensure maxMana is at least 0
        }
        // Preserve the currentMana/maxMana ratio, rounding down
        double ratio = this.maxMana > 0 ? (double) currentMana / this.maxMana : 1.0;
        this.maxMana = maxMana;
        this.currentMana = Math.max(1, (int) (this.maxMana * ratio));
    }

    public void setCurrentMana(int mana) {
        this.currentMana = Math.max(0, Math.min(mana, this.maxMana));
    }

    // Delta-style modifier that clamps into [0, maxMana]
    public void modifyMana(int amount) {
        this.currentMana = Math.max(0, Math.min(this.currentMana + amount, this.maxMana));
    }

    public void updateMaxMana() {
        // Compute the new max mana from a stable base value so repeated calls don't
        // compound.
        int delta = this.getStatBonus(Stats.INTELLIGENCE);
        double factor = 1.0;
        if (delta < 0) {
            factor = Math.pow(0.9, -delta);
        } else if (delta > 0) {
            factor = Math.pow(1.1, delta);
        }
        // Always round down and never go below 25. Use baseMaxMana as the stable
        // source.
        int newMax = (int) Math.floor(this.getBaseMaxMana() * factor);
        newMax = Math.max(25, newMax);
        // Use setMaxMana which preserves currentMana/maxMana ratio
        this.setMaxMana(newMax);
    }

    public void setMaxStamina(int maxStamina) {
        if (maxStamina < 0) {
            maxStamina = 0; // Ensure maxStamina is at least 0
        }
        // Preserve the currentStamina/maxStamina ratio, rounding down
        double ratio = this.maxStamina > 0 ? (double) currentStamina / this.maxStamina : 1.0;
        this.maxStamina = maxStamina;
        this.currentStamina = Math.max(0, (int) (this.maxStamina * ratio));
        // Recompute baseStaminaRegen from maxStamina so regen follows a percentile of
        // max
        this.baseStaminaRegen = Math.max(1, Math.floorDiv(this.maxStamina, 5));
        // Recompute derived stats (including staminaRegen) whenever max stamina changes
        recalcDerivedStats();
    }

    public void updateMaxStamina() {
        // Compute the new max stamina from a stable base value so repeated calls don't
        // compound.
        int delta = this.getStatBonus(Stats.CONSTITUTION);
        double factor = 1.0;
        if (delta < 0) {
            factor = Math.pow(0.9, -delta);
        } else if (delta > 0) {
            factor = Math.pow(1.1, delta);
        }
        // Always round down and never go below 25. Use baseMaxStamina as the stable
        // source.
        int newMax = (int) Math.floor(this.getBaseMaxStamina() * factor);
        newMax = Math.max(25, newMax);
        // Use setMaxStamina which preserves currentStamina/maxStamina ratio
        this.setMaxStamina(newMax);
    }

    public void setCurrentStamina(int stamina) {
        this.currentStamina = Math.max(0, Math.min(stamina, this.maxStamina));
        // Recompute derived values when stamina changes
        recalcDerivedStats();
    }

    // Delta-style modifier that clamps into [0, maxStamina]
    public void modifyStamina(int amount) {
        this.currentStamina = Math.max(0, Math.min(this.currentStamina + amount, this.maxStamina));
        // Recompute derived values when stamina changes
        recalcDerivedStats();
    }

    public int getBaseHpRegen() {
        return baseHpRegen;
    }

    public int getBaseStaminaRegen() {
        return baseStaminaRegen;
    }

    public int getBaseManaRegen() {
        return baseManaRegen;
    }

    public int getHpRegen() {
        return hpRegen + equipmentHpRegen;
    }

    public int setHpRegen(int hpRegen) {
        this.hpRegen = hpRegen;
        return this.hpRegen;
    }

    public void modifyHpRegen(int delta) {
        this.hpRegen += delta;
    }

    public void modifyManaRegen(int delta) {
        this.manaRegen += delta;
    }

    public void modifyStaminaRegen(int delta) {
        this.staminaRegen += delta;
    }

    public int getStaminaRegen() {
        return staminaRegen + equipmentStaminaRegen;
    }

    public int setStaminaRegen(int staminaRegen) {
        this.staminaRegen = staminaRegen;
        return this.staminaRegen;
    }

    public int getManaRegen() {
        return manaRegen + equipmentManaRegen;
    }

    public int setManaRegen(int manaRegen) {
        this.manaRegen = manaRegen;
        return this.manaRegen;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CreatureType getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(CreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<Integer, Property> getBuffs() {
        return propertyManager.getBuffs();
    }

    public HashMap<Integer, Property> getDebuffs() {
        return propertyManager.getDebuffs();
    }

    public HashMap<Integer, Property> getTraits() {
        return propertyManager.getTraits();
    }

    public Property getBuff(int id) {
        return propertyManager.getBuffs().get(id);
    }

    public Property getDebuff(int id) {
        return propertyManager.getDebuffs().get(id);
    }

    public Property getTrait(int id) {
        return propertyManager.getTraits().get(id);
    }

    public int getStat(Stats stat) {
        return stats.getOrDefault(stat, 0);
    }

    // --- Per-stat convenience getters ---
    public int getSTR() {
        return getStat(Stats.STRENGTH);
    }

    public int getDEX() {
        return getStat(Stats.DEXTERITY);
    }

    public int getCON() {
        return getStat(Stats.CONSTITUTION);
    }

    public int getINT() {
        return getStat(Stats.INTELLIGENCE);
    }

    public int getWIS() {
        return getStat(Stats.WISDOM);
    }

    public int getCHA() {
        return getStat(Stats.CHARISMA);
    }

    public int getLUCK() {
        return getStat(Stats.LUCK);
    }

    public void setStat(Stats stat, int value) {
        stats.put(stat, value);
        // Keep cached bonuses in sync whenever a stat changes
        recalcStatBonuses();
        if (stat == Stats.CONSTITUTION) {
            recalcMaxHp();
            // Recompute max stamina when CON changes
            updateMaxStamina();
            // Recompute derived stats that depend on CON (e.g., magicResist)
            recalcDerivedStats();
        }
        if (stat == Stats.INTELLIGENCE) {
            // Recompute max mana when INT changes
            updateMaxMana();
        }
        if (stat == Stats.WISDOM) {
            // WIS affects magic resist; recompute derived stats
            recalcDerivedStats();
        }
        if (stat == Stats.LUCK) {
            // Recompute crit when luck changes
            recalcDerivedStats();
        }
    }

    public void modifyStat(Stats stat, int amount) {
        stats.put(stat, getStat(stat) + amount);
        // Keep cached bonuses in sync whenever a stat changes
        recalcStatBonuses();
        if (stat == Stats.CONSTITUTION) {
            recalcMaxHp();
            // Recompute max stamina when CON changes
            updateMaxStamina();
            // Recompute derived stats that depend on CON (e.g., magicResist)
            recalcDerivedStats();
        }
        if (stat == Stats.INTELLIGENCE) {
            // Recompute max mana when INT changes
            updateMaxMana();
        }
        if (stat == Stats.WISDOM) {
            // WIS affects magic resist; recompute derived stats
            recalcDerivedStats();
        }
        if (stat == Stats.LUCK) {
            // Recompute crit when luck changes
            recalcDerivedStats();
        }
    }

    // --- Stat helper convenience methods ---
    /**
     * Generic increase/decrease helpers that delegate to modifyStat so all
     * derived values and cached bonuses remain consistent.
     */
    public void increaseStat(Stats stat) {
        StatUtil.increaseStat(this, stat, 1);
    }

    public void increaseStat(Stats stat, int amount) {
        StatUtil.increaseStat(this, stat, amount);
    }

    public void decreaseStat(Stats stat) {
        StatUtil.decreaseStat(this, stat, 1);
    }

    public void decreaseStat(Stats stat, int amount) {
        StatUtil.decreaseStat(this, stat, amount);
    }

    public int getResistance(Resistances resistance) {
        return resistances.getOrDefault(resistance, 0);
    }

    public void setResistance(Resistances resistance, int value) {
        resistances.put(resistance, value);
    }

    public void modifyResistance(Resistances resistance, int amount) {
        resistances.put(resistance, getResistance(resistance) + amount);
    }

    public float getBaseCrit() {
        return baseCrit;
    }

    public float getBaseDodge() {
        return baseDodge;
    }

    public float getBaseBlock() {
        return baseBlock;
    }

    public float getBaseMagicResist() {
        return baseMagicResist;
    }

    public float getCrit() {
        return this.crit + this.equipmentCrit + this.propertyCrit;
    }

    // Store raw crit value (can be negative or >100). Clamping to 0-100 is done
    // only when the value is used for probability checks.
    public float setCrit(float crit) {
        this.crit = crit;
        return this.crit;
    }

    public void updateCrit() {
        // Compute the derived crit (no compounding): baseCrit (from JSON) plus
        // luck-derived bonus. Equipment modifiers are applied separately via
        // equipmentCrit and added in getCrit().
        float bonusCrit = 5f * this.getStatBonus(Stats.LUCK);
        this.crit = this.baseCrit + bonusCrit;
    }

    public float getDodge() {
        return this.dodge + this.equipmentDodge + this.propertyDodge;
    }

    // Store raw dodge value (can be negative or >100). When used for to-hit
    // checks the effective dodge will be clamped to 0-80.
    public float setDodge(float dodge) {
        this.dodge = dodge;
        return this.dodge;
    }

    public float getBlock() {
        return this.block + this.equipmentBlock + this.propertyBlock;
    }

    // Store raw block value (can be negative or >100). When used for block
    // checks the effective block will be clamped to 0-80.
    public float setBlock(float block) {
        this.block = block;
        return this.block;
    }

    public float getMagicResist() {
        return this.magicResist + this.equipmentMagicResist + this.propertyMagicResist;
    }

    /**
     * Total accuracy (base + equipment + property) added to physical to-hit rolls.
     */
    public int getAccuracy() {
        return accuracy + equipmentAccuracy + propertyAccuracy;
    }

    /**
     * Total magic accuracy (base + equipment + property) added to magic to-hit
     * rolls.
     */
    public int getMagicAccuracy() {
        return magicAccuracy + equipmentMagicAccuracy + propertyMagicAccuracy;
    }

    // Methods for properties to modify property-derived accumulators
    public void modifyPropertyCrit(float delta) {
        this.propertyCrit += delta;
    }

    public void modifyPropertyDodge(float delta) {
        this.propertyDodge += delta;
    }

    public void modifyPropertyBlock(float delta) {
        this.propertyBlock += delta;
    }

    public void modifyPropertyMagicResist(float delta) {
        this.propertyMagicResist += delta;
    }

    public float setMagicResist(float magicResist) {
        this.magicResist = magicResist;
        return this.magicResist;
    }

    // (removed unused helper getStatPoints)

    public void setStatPoints(int statPoints, int amount) {
        this.statPoints = statPoints + amount;
    }

    public void addStatPoint() {
        this.statPoints += 1;
    }

    public void removeStatPoint() {
        if (this.statPoints > 0) {
            this.statPoints -= 1;
        }
    }

    public void spendStatPoint(Stats stat) {
        if (this.statPoints > 0) {
            this.statPoints -= 1;
            increaseStat(stat);
        }
    }

    public void recalcDerivedStats() {
        // Ensure cached stat bonuses are up-to-date before computing derived values
        recalcStatBonuses();
        // Base-derived values:
        // - Crit and Block mirror the raw base values from JSON (equipment added via
        // equipment* accumulators)
        // - Dodge is baseDodge plus a DEX-derived delta (2.5 per point over/under 10)
        // Crit includes luck scaling here so that any time derived stats are
        // recomputed (for example when equipment changes) the luck bonus is
        // preserved and equipmentCrit is still added via getCrit().
        float bonusCrit = 5f * this.getStatBonus(Stats.LUCK);
        this.crit = this.baseCrit + bonusCrit;
        this.block = this.baseBlock;
        float dodgeDelta = 2.5f * this.getStatBonus(Stats.DEXTERITY);
        this.dodge = this.baseDodge + dodgeDelta;
        float magicResistDelta = 5.0f * this.getStatBonus(Stats.WISDOM);
        int wisBonus = this.getStatBonus(Stats.WISDOM);
        int extraStamina = (int) Math.floor(2.5 * wisBonus);
        extraStamina = Math.max(1, extraStamina);
        this.staminaRegen = this.baseStaminaRegen + extraStamina;
        magicResistDelta += 2.5f * this.getStatBonus(Stats.CONSTITUTION);
        this.magicResist = this.baseMagicResist + magicResistDelta;
    }

    /**
     * Recompute and cache per-stat bonuses used throughout creature logic.
     * - For most stats: bonus = stat - 10
     * - For LUCK: bonus = stat
     */
    public void recalcStatBonuses() {
        for (Stats s : Stats.values()) {
            int raw = this.getStat(s);
            int bonus = (s == Stats.LUCK) ? raw : (raw - 10);
            statBonuses.put(s, bonus);
        }
    }

    /**
     * Get the cached stat bonus for a stat. Returns 0 if missing.
     */
    public int getStatBonus(Stats s) {
        return statBonuses.getOrDefault(s, 0);
    }

    public void attack(Creature target) {
        // Prefer weapon attacks (if equipped and weapon defines attacks), otherwise use
        // creature attacks
        Attack chosen = null;
        Equipment weapon = null;
        if (this.getEquipped(EquipmentSlot.WEAPON) instanceof Equipment) {
            weapon = (Equipment) this.getEquipped(EquipmentSlot.WEAPON);
            // If the weapon is versatile and is currently wielded two-handed (it also
            // occupies the OFFHAND slot), prefer the versatileAttacks list. Otherwise
            // fall back to the weapon's regular attacks.
            java.util.List<Attack> weaponAttackList = null;
            try {
                boolean wieldedTwoHanded = (this.getEquipped(EquipmentSlot.OFFHAND) == weapon) || weapon.isTwoHanded();
                if (weapon.getVersatile() && wieldedTwoHanded) {
                    if (weapon.getVersatileAttacks() != null && !weapon.getVersatileAttacks().isEmpty()) {
                        weaponAttackList = weapon.getVersatileAttacks();
                    }
                }
            } catch (Exception ignored) {
            }

            if (weaponAttackList == null) {
                if (weapon.getAttacks() != null && !weapon.getAttacks().isEmpty()) {
                    weaponAttackList = weapon.getAttacks();
                }
            }

            if (weaponAttackList != null && !weaponAttackList.isEmpty()) {
                chosen = AttackUtil.chooseAttackFromList(weaponAttackList);
                // stat bonus depends on weapon class/finesse
                int statBonus = WeaponUtil.determineWeaponStatBonus(this, weapon) * 5;
                com.bapppis.core.combat.AttackEngine.applyAttackToTarget(this, chosen, statBonus, target,
                        weapon.getDamageType(), weapon.getMagicElement(), weapon);
                return;
            }
        }

        // No weapon attacks — use creature's own attacks if present
        if (this.attacks != null && !this.attacks.isEmpty()) {
            chosen = AttackUtil.chooseAttackFromList(this.attacks);
            // For creature attacks, default to strength for physical damage
            int statBonus = Math.max(0, this.getStatBonus(Stats.STRENGTH)) * 5;
            Resistances physType = ResistanceUtil.parse(chosen == null ? null : chosen.damageType);
            Resistances magType = ResistanceUtil.parse(chosen == null ? null : chosen.magicDamageType);
            com.bapppis.core.combat.AttackEngine.applyAttackToTarget(this, chosen, statBonus, target, physType,
                    magType, null);
            return;
        }
    }

    // parseResistance removed; use ResistanceUtil.parse(...) where needed

    // --- Equipment & Inventory ---
    public Inventory getInventory() {
        return inventory;
    }

    public Item getEquipped(EquipmentSlot slot) {
        return equipment.get(slot);
    }

    // Package-private helpers used by EquipmentManager to mutate equipment map
    // and update equipment-level accumulators without exposing them publicly.
    Item removeEquipped(EquipmentSlot slot) {
        return equipment.remove(slot);
    }

    void putEquipped(EquipmentSlot slot, Item item) {
        equipment.put(slot, item);
    }

    void adjustEquipmentStat(Stats stat, int delta) {
        int prev = equipmentStats.getOrDefault(stat, 0);
        equipmentStats.put(stat, prev + delta);
    }

    void adjustEquipmentResist(Resistances res, int delta) {
        int prev = equipmentResists.getOrDefault(res, 0);
        equipmentResists.put(res, prev + delta);
    }

    void adjustEquipmentHpRegen(int delta) {
        this.equipmentHpRegen += delta;
    }

    void adjustEquipmentStaminaRegen(int delta) {
        this.equipmentStaminaRegen += delta;
    }

    void adjustEquipmentManaRegen(int delta) {
        this.equipmentManaRegen += delta;
    }

    void adjustEquipmentCrit(float delta) {
        this.equipmentCrit += delta;
    }

    void adjustEquipmentDodge(float delta) {
        this.equipmentDodge += delta;
    }

    void adjustEquipmentBlock(float delta) {
        this.equipmentBlock += delta;
    }

    void adjustEquipmentMagicResist(float delta) {
        this.equipmentMagicResist += delta;
    }

    /* Equipment accuracy helpers (package-private for EquipmentManager) */
    void adjustEquipmentAccuracy(int delta) {
        this.equipmentAccuracy += delta;
    }

    void adjustEquipmentMagicAccuracy(int delta) {
        this.equipmentMagicAccuracy += delta;
    }

    /* Property-driven accuracy modifiers (used by Property.onApply/onRemove) */
    public void modifyPropertyAccuracy(int delta) {
        this.propertyAccuracy += delta;
    }

    public void modifyPropertyMagicAccuracy(int delta) {
        this.propertyMagicAccuracy += delta;
    }

    public void equipItem(Item item) {
        // Default behavior: treat two-handed items as two-handed; versatile items
        // default to one-handed unless caller explicitly requests two-handed.
        boolean requestTwoHanded = false;
        if (item instanceof com.bapppis.core.item.Equipment) {
            try {
                com.bapppis.core.item.Equipment eq = (com.bapppis.core.item.Equipment) item;
                requestTwoHanded = eq.isTwoHanded();
            } catch (Exception ignored) {
            }
        }
        EquipmentManager.equip(this, item, requestTwoHanded);
    }

    /**
     * Equip an item and optionally treat it as two-handed. If the item is
     * inherently two-handed, it will always be equipped two-handed. If the
     * item is versatile and requestTwoHanded is true it will be equipped into
     * both WEAPON and OFFHAND slots (reusing the existing two-handed logic).
     */
    public void equipItem(Item item, boolean requestTwoHanded) {
        EquipmentManager.equip(this, item, requestTwoHanded);
    }

    public void unequipItem(EquipmentSlot slot) {
        EquipmentManager.unequip(this, slot);
    }

    public void addProperty(Property property) {
        propertyManager.add(property);
    }

    /**
     * Convenience overload: look up a property by id via PropertyLoader and apply
     * it.
     * If the id is unknown this is a no-op.
     */
    public void addProperty(int id) {
        propertyManager.addById(id);
    }

    /**
     * Convenience overload: look up a property by its human name (case-insensitive)
     * and apply it. If the name is numeric this will fall back to id lookup.
     * Returns true if a property was found and applied, false otherwise.
     */
    public boolean addProperty(String name) {
        return propertyManager.addByName(name);
    }

    public void removeProperty(int id) {
        propertyManager.remove(id);
    }

    public void printStatusEffects() {
        propertyManager.printStatusEffects();
    }

    public String printProperties() {
        // Preserve the existing single-column output for callers that rely on it.
        return propertyManager.printProperties();
    }

    /**
     * Return a formatted properties dump with the given number of columns per
     * row. Columns will be aligned by padding the text so the output is easy to
     * scan in a console. If columns &lt;= 1 the output will fall back to the
     * single-column `printProperties()` format.
     */
    public String printProperties(int columns) {
        if (columns <= 1) {
            return printProperties();
        }

        return propertyManager.formatPropertiesInColumns(propertyManager.getBuffs().values(), columns)
                + propertyManager.formatPropertiesInColumns(propertyManager.getDebuffs().values(), columns)
                + propertyManager.formatPropertiesInColumns(propertyManager.getTraits().values(), columns);
    }

    /**
     * Helper that lays out the provided properties collection into `columns`
     * aligned columns. Each property uses its `toString()` as the cell content.
     */
    private String formatPropertiesInColumns(java.util.Collection<Property> props, int columns) {
        StringBuilder sb = new StringBuilder();
        if (props == null || props.isEmpty()) {
            sb.append("  (none)\n");
            return sb.toString();
        }

        java.util.List<String> cells = new java.util.ArrayList<>();
        int maxCellLen = 0;
        for (Property p : props) {
            String s = formatPropertySummary(p);
            // For multi-column compact output include a one-line tooltip snippet (first
            // line)
            try {
                String tt = p.getTooltip();
                if (tt != null && !tt.isBlank()) {
                    String first = tt.split("\\n")[0];
                    // append a short snippet, truncating if very long
                    String snippet = first.length() > 40 ? first.substring(0, 37) + "..." : first;
                    s = s + " - " + snippet;
                }
            } catch (Exception ignored) {
            }
            cells.add(s);
            if (s.length() > maxCellLen)
                maxCellLen = s.length();
        }

        // Pad each cell to maxCellLen and arrange into rows
        int idx = 0;
        for (String cell : cells) {
            String padded = String.format("  %-" + maxCellLen + "s", cell);
            sb.append(padded);
            idx++;
            if (idx % columns == 0) {
                sb.append('\n');
            } else {
                sb.append("   "); // small gap between columns
            }
        }
        if (idx % columns != 0) {
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * Build a compact, human-friendly single-line summary for a property.
     * Format: "Name (id)[: description] [duration=5] [stats=...] [resists=...]"
     */
    private String formatPropertySummary(Property p) {
        // Delegate to PropertyManager formatting helper to keep code centralised.
        return propertyManager.formatPropertySummary(p);
    }

    public void updateMaxHp() {
        int delta = this.getStatBonus(Stats.CONSTITUTION);
        int bonusHp = this.hpLvlBonus + delta;
        bonusHp = Math.max(1, bonusHp);
        this.setMaxHp(this.maxHp + bonusHp);
        this.modifyHp(bonusHp);
    }

    public void modifyHp(int amount) {
        currentHp += amount;
        if (currentHp > maxHp) {
            currentHp = maxHp;
        } else if (currentHp < 0) {
            currentHp = 0;
        }
    }

    public void tickProperties() {
        propertyManager.tick();
    }

    public void recalcMaxHp() {
        // Preserve currentHp/maxHp ratio when maxHp changes
        double ratio = this.maxHp > 0 ? (double) currentHp / this.maxHp : 1.0;
        int conBonus = this.getStatBonus(Stats.CONSTITUTION);
        int newMaxHp = this.baseHp + ((this.level + 1) * (this.hpLvlBonus + conBonus));
        this.maxHp = Math.max(1, newMaxHp);
        this.currentHp = Math.max(1, (int) (this.maxHp * ratio));
    }

    /**
     * Finalize creature fields after loading from JSON.
     * - Resets max HP to base, then calls updateMaxHp() (level/CON scaling).
     * - Recomputes max mana from INT via updateMaxMana() (does not force current
     * mana to max).
     * - Sets stamina to max.
     * - Converts stored levels into XP so addXp() applies level-up bonuses.
     * Call this once after Gson has populated fields and after properties/items
     * (starting equipment) have been applied.
     */
    public void finalizeAfterLoad() {
        int baseHp = this.getBaseHp();
        this.setMaxHp(baseHp); // Reset max HP to base before applying properties
        // Recompute max/current HP correctly
        this.updateMaxHp();

        if (this.baseMaxStamina <= 0) {
            this.baseMaxStamina = this.maxStamina;
        }
        updateMaxStamina();

        if (this.baseStaminaRegen <= 0) {
            this.baseStaminaRegen = Math.max(1, Math.floorDiv(this.baseMaxStamina, 5));
            this.staminaRegen = this.baseStaminaRegen;
        }

        if (this.baseMaxMana <= 0) {
            this.baseMaxMana = this.maxMana;
        }

        updateMaxMana();

        this.currentMana = this.maxMana;
        this.currentStamina = this.maxStamina;

        updateCrit();

        int tempXp = this.getXp() + LevelUtil.totalXpForLevel(this.getLevel());
        this.setLevel(0);
        this.setXp(0);
        this.addXp(tempXp);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Creature: ").append(name == null ? "<unnamed>" : name).append(" (Id:").append(id).append(")\n");
        sb.append("Level: ").append(level).append(" (").append("XP: ").append(getXp()).append("/")
                .append(LevelUtil.xpForNextLevel(level)).append(") ")
                .append(creatureType == null ? "" : creatureType.name()).append("\n");

        // Health / resources
        sb.append("HP: ").append(currentHp).append("/").append(maxHp).append("  (HP regen: ").append(getHpRegen())
                .append("/s)").append("\n");
        sb.append("Mana: ").append(currentMana).append("/").append(maxMana).append("  (Mana regen: ")
                .append(getManaRegen()).append("/s)").append("\n");
        sb.append("Stamina: ").append(currentStamina).append("/").append(maxStamina).append("  (Stamina regen: ")
                .append(getStaminaRegen()).append("/s)").append("\n");

        // Basic stats on one line for quick scanning
        sb.append("Stats: ")
                .append("STR ").append(getSTR()).append("  ")
                .append("DEX ").append(getDEX()).append("  ")
                .append("CON ").append(getCON()).append("  ")
                .append("INT ").append(getINT()).append("  ")
                .append("WIS ").append(getWIS()).append("  ")
                .append("CHA ").append(getCHA()).append("  ")
                .append("LUCK ").append(getLUCK()).append("\n");

        // Resistances (condensed)
        sb.append("Resists: ");
        boolean first = true;
        for (Entry<Resistances, Integer> entry : resistances.entrySet()) {
            if (!first)
                sb.append(", ");
            sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append("%");
            first = false;
        }
        sb.append("\n");

        // Combat chances
        sb.append("Crit: ").append(getCrit()).append("%  ")
                .append("Dodge: ").append(getDodge()).append("%  ")
                .append("Block: ").append(getBlock()).append("%  ")
                .append("MagicResist: ").append(getMagicResist()).append("%\n");
        sb.append("Accuracy: ").append(getAccuracy()).append("  ")
                .append("MagicAccuracy: ").append(getMagicAccuracy()).append("\n");

        // Equipped items (one per slot)
        sb.append("Equipment:\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item equipped = equipment.get(slot);
            sb.append("  ").append(slot.name()).append(": ");
            sb.append(equipped == null ? "Empty" : equipped.getName()).append("\n");
        }

        // Properties summary and inventory counts
        sb.append("Properties: ")
                .append("Buffs=").append(propertyManager.getBuffs().size()).append(" ")
                .append("Debuffs=").append(propertyManager.getDebuffs().size()).append(" ")
                .append("Traits=").append(propertyManager.getTraits().size()).append("\n");

        sb.append("Inventory counts: ")
                .append("Weapons=").append(inventory.getWeapons().size()).append(", ")
                .append("Offhands=").append(inventory.getOffhands().size()).append(", ")
                .append("Helmets=").append(inventory.getHelmets().size()).append(", ")
                .append("Armor=").append(inventory.getArmors().size()).append(", ")
                .append("Consumables=").append(inventory.getConsumables().size()).append(", ")
                .append("Misc=").append(inventory.getMisc().size()).append("\n");

        return sb.toString();
    }
}
package com.bapppis.core.creature;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import com.bapppis.core.util.Dice;
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
    private int statPoints = 0;
    // Cached per-stat bonuses (e.g. STR 14 -> +4). LUCK bonus equals the raw
    // luck value.
    private final java.util.EnumMap<Stats, Integer> statBonuses = new java.util.EnumMap<>(Stats.class);
    private HashMap<Integer, Property> buffs = new HashMap<>();
    private HashMap<Integer, Property> debuffs = new HashMap<>();
    private HashMap<Integer, Property> traits = new HashMap<>();
    private String description;
    private EnumMap<EquipmentSlot, Item> equipment = new EnumMap<>(EquipmentSlot.class);
    private Inventory inventory = new Inventory();
    // Attacks defined on creatures (Gson will fill this from JSON)
    private java.util.List<Attack> attacks = new java.util.ArrayList<>();
    // Optional sprite key loaded from JSON (e.g. "player_biggles",
    // "monster_goblin")
    private String sprite;

    // Optional test hook to receive detailed attack reports
    public static Consumer<AttackReport> attackListener = null;

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

    // --- Nested Types ---
    public static class AttackReport {
        public String attackName;
        public int physRaw;
        public int magRaw;
        /** total physical after crits but before resistances */
        public int physAfterCritBeforeResist;
        public int physAfter;
        public int magAfter;
        public int times;
        public String damageType;
        public String magicType;
        /** number of individual hit crits in this attack invocation */
        public int critCount;
        public boolean isCrit;
        public Creature attacker;
        public Creature target;
        // Weapon/attack magic debug fields
        public int magicStatBonus; // the raw stat bonus used from the weapon (e.g., INT bonus)
        public int magicStatExtra; // extra magic damage added after multiplier and floor
        public float magicDamageMultiplier; // attack.magicDamageMultiplier
        // Weapon/attack physical debug fields
        public int physStatBase; // base stat bonus used for physical damage (determineStatBonusForWeapon)
        public int physStatExtra; // extra physical damage added after multiplier and floor
        public float physDamageMultiplier; // attack.damageMultiplier
        // Chosen stat names for clarity in tests
        public String magicStatChosen;
        public String physStatChosen;
        // New diagnostic fields for dual-resolution system
        public int physCritCount; // number of critical physical hits
        public int magicCritCount; // number of critical magic hits
        public int physAttempts; // number of physical hit attempts
        public int physMissDodge; // count of physical misses due to dodge
        public int physMissBlock; // count of physical misses due to block
        public int magicAttempts; // number of magic hit attempts
        public int magicMissDodge; // magic misses due to dodge
        public int magicMissResist; // magic misses due to magicResist avoidance
        public boolean dualRoll; // true if both physical and magic parts rolled separately
        public boolean trueDamage; // true if physicalType classified as TRUE
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
        return buffs;
    }

    public HashMap<Integer, Property> getDebuffs() {
        return debuffs;
    }

    public HashMap<Integer, Property> getTraits() {
        return traits;
    }

    public Property getBuff(int id) {
        return buffs.get(id);
    }

    public Property getDebuff(int id) {
        return debuffs.get(id);
    }

    public Property getTrait(int id) {
        return traits.get(id);
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
        com.bapppis.core.util.DebugLog.debug("modifyStat called: " + stat + " amount=" + amount + " on " + this.getName());
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
        return this.crit + this.equipmentCrit;
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
        return this.dodge + this.equipmentDodge;
    }

    // Store raw dodge value (can be negative or >100). When used for to-hit
    // checks the effective dodge will be clamped to 0-80.
    public float setDodge(float dodge) {
        this.dodge = dodge;
        return this.dodge;
    }

    public float getBlock() {
        return this.block + this.equipmentBlock;
    }

    // Store raw block value (can be negative or >100). When used for block
    // checks the effective block will be clamped to 0-80.
    public float setBlock(float block) {
        this.block = block;
        return this.block;
    }

    public float getMagicResist() {
        return this.magicResist + this.equipmentMagicResist;
    }

    public float setMagicResist(float magicResist) {
        this.magicResist = magicResist;
        return this.magicResist;
    }

    private int getStatPoints() {
        return statPoints;
    }

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
                applyAttackToTarget(chosen, statBonus, target, weapon.getDamageType(), weapon.getMagicElement(),
                        weapon);
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
            applyAttackToTarget(chosen, statBonus, target, physType, magType, null);
            return;
        }
    }

    // helper logic moved to util: WeaponUtil.determineWeaponStatBonus and
    // AttackUtil.chooseAttackFromList

    private void applyAttackToTarget(Attack attack, int statBonus, Creature target, Resistances physicalType,
            Resistances magicType, Equipment weapon) {
        if (attack == null || target == null)
            return;
        // New unified resolution: potentially separate physical and magical rolls.
        // 1. Resolve physical component (if any and not TRUE magic-only attack)
        int totalPhysBeforeResist = 0;
        int physRaw = 0;
        int physCritCount = 0;
        int physAfter = 0;
        int physAttempts = 0;
        int physMissDodge = 0;
        int physMissBlock = 0;
        int times = attack.getTimes();
        float baseCrit = this.getCrit();
        int critMod = 0;
        try {
            critMod = attack.getCritMod();
        } catch (Exception ignored) {
        }
        float critChance = Math.max(0f, Math.min(100f, baseCrit + critMod));

        boolean hasPhysical = physicalType != null
                && ResistanceUtil.classify(physicalType) == ResistanceUtil.Kind.PHYSICAL;
        boolean isTrue = physicalType != null && ResistanceUtil.classify(physicalType) == ResistanceUtil.Kind.TRUE;
        if (hasPhysical || isTrue) {
            for (int i = 0; i < times; i++) {
                physAttempts++;
                float rawRoll = ThreadLocalRandom.current().nextFloat() * 100f;
                int toHit = Math.round(rawRoll) + statBonus;
                float effectiveDodge = Math.max(0f, Math.min(100f, target.getDodge()));
                float effectiveBlock = Math.max(0f, Math.min(100f, target.getBlock()));
                if (isTrue) {
                    // TRUE damage ignores block; only dodge check
                    if (toHit <= effectiveDodge) {
                        System.out.println("Missed (dodge TRUE): " + this.getName() + " -> " + target.getName());
                        physMissDodge++;
                        continue;
                    }
                } else {
                    // Physical: dodge + block partition (same as before)
                    float totalAvoid = Math.min(100f, effectiveDodge + effectiveBlock);
                    if (toHit <= totalAvoid) {
                        if (effectiveDodge >= effectiveBlock) {
                            if (toHit <= effectiveBlock) {
                                System.out.println("Missed (block): " + this.getName() + " -> " + target.getName());
                                physMissBlock++;
                                continue;
                            } else {
                                System.out.println("Missed (dodge): " + this.getName() + " -> " + target.getName());
                                physMissDodge++;
                                continue;
                            }
                        } else {
                            if (toHit <= effectiveDodge) {
                                System.out.println("Missed (dodge): " + this.getName() + " -> " + target.getName());
                                physMissDodge++;
                                continue;
                            } else {
                                System.out.println("Missed (block): " + this.getName() + " -> " + target.getName());
                                physMissBlock++;
                                continue;
                            }
                        }
                    }
                }
                int hit = 0;
                if (attack.physicalDamageDice != null && !attack.physicalDamageDice.isBlank()) {
                    hit = Dice.roll(attack.physicalDamageDice);
                }
                hit += Math.max(0, statBonus);
                physRaw += hit;
                float effectiveCrit = critChance;
                boolean crit = ThreadLocalRandom.current().nextFloat() < (effectiveCrit / 100f);
                if (crit) {
                    physCritCount++;
                    hit *= 2;
                }
                totalPhysBeforeResist += hit;
            }
            // Weapon scaling for physical part
            int physStatBase = 0;
            int physStatExtra = 0; // removed unused physStatChosenName
            try {
                if (weapon != null) {
                    physStatBase = WeaponUtil.determineWeaponStatBonus(this, weapon);
                    try {
                        // Determine stat name originally; no longer stored
                        if (weapon.getFinesse()) {
                            int str = this.getStatBonus(Stats.STRENGTH);
                            int dex = this.getStatBonus(Stats.DEXTERITY);
                            // choose higher (not stored)
                            @SuppressWarnings("unused")
                            String ignoredName = (str >= dex) ? Stats.STRENGTH.name() : Stats.DEXTERITY.name();
                        } else {
                            switch (weapon.getWeaponClass()) {
                                case MELEE:
                                    break;
                                case RANGED:
                                    break;
                                case MAGIC:
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    double physMult = Math.max(0.0, attack.damageMultiplier);
                    physStatExtra = (int) Math.floor(physStatBase * 5.0 * physMult);
                    if (physStatExtra != 0)
                        totalPhysBeforeResist += physStatExtra;
                }
            } catch (Exception ignored) {
            }
            int resistValue = (physicalType == null ? 100 : target.getResistance(physicalType));
            physAfter = Math.floorDiv(totalPhysBeforeResist * resistValue, 100);
            if (physAfter > 0)
                target.modifyHp(-physAfter);
            // Listener/report for physical stored later (combine with magic below if
            // needed)
        }

        // 2. Resolve magic component IF weapon has magic element or attack has
        // magicDamageDice and magicType present
        int magRaw = 0;
        int magAfter = 0;
        int magicCritCount = 0;
        int magicBeforeResist = 0;
        int magicStatBonus = 0;
        int magicStatExtra = 0;
        float magicMult = attack.magicDamageMultiplier;
        String magicStatChosenName = null;
        int magicAttempts = 0;
        int magicMissDodge = 0;
        int magicMissResist = 0;
        boolean hasMagicComponent = (weapon != null && weapon.getMagicElement() != null)
                || (magicType != null && attack.magicDamageDice != null && !attack.magicDamageDice.isBlank());
        if (hasMagicComponent && magicType != null) {
            int magicTimes = attack.getTimes();
            for (int i = 0; i < magicTimes; i++) {
                magicAttempts++;
                float rawRoll = ThreadLocalRandom.current().nextFloat() * 100f;
                int toHit = Math.round(rawRoll) + statBonus; // reuse statBonus for now
                float effectiveDodge = Math.max(0f, Math.min(100f, target.getDodge()));
                float effectiveMagicResist = Math.max(0f, Math.min(100f, target.getMagicResist()));
                // Avoidance window is dodge + magicResist (no block)
                float totalAvoid = Math.min(100f, effectiveDodge + effectiveMagicResist);
                if (toHit <= totalAvoid) {
                    if (effectiveDodge >= effectiveMagicResist) {
                        if (toHit <= effectiveMagicResist) {
                            System.out.println("Missed (magicResist): " + this.getName() + " -> " + target.getName());
                            magicMissResist++;
                        } else {
                            System.out.println("Missed (dodge magic): " + this.getName() + " -> " + target.getName());
                            magicMissDodge++;
                        }
                    } else {
                        if (toHit <= effectiveDodge) {
                            System.out.println("Missed (dodge magic): " + this.getName() + " -> " + target.getName());
                            magicMissDodge++;
                        } else {
                            System.out.println("Missed (magicResist): " + this.getName() + " -> " + target.getName());
                            magicMissResist++;
                        }
                    }
                    continue;
                }
                int hit = 0;
                if (attack.magicDamageDice != null && !attack.magicDamageDice.isBlank()) {
                    hit = Dice.roll(attack.magicDamageDice);
                }
                hit += Math.max(0, statBonus); // allow stat to influence base magic as before via weapon scaling
                magRaw += hit;
                // Magic crit logic: reuse critChance (could later differentiate)
                float effectiveCrit = critChance;
                boolean crit = ThreadLocalRandom.current().nextFloat() < (effectiveCrit / 100f);
                if (crit) {
                    magicCritCount++;
                    hit *= 2;
                }
                magicBeforeResist += hit;
            }
            // Weapon magic stat scaling
            try {
                if (weapon != null) {
                    com.bapppis.core.creature.Creature.Stats chosen = null;
                    int best = Integer.MIN_VALUE;
                    if (weapon.getMagicStatBonuses() != null && !weapon.getMagicStatBonuses().isEmpty()) {
                        for (com.bapppis.core.creature.Creature.Stats s : weapon.getMagicStatBonuses()) {
                            int b = this.getStatBonus(s);
                            if (b > best) {
                                best = b;
                                chosen = s;
                            }
                        }
                    } else if (weapon.getMagicStatBonus() != null) {
                        chosen = weapon.getMagicStatBonus();
                        best = this.getStatBonus(chosen);
                    }
                    if (chosen != null) {
                        magicStatBonus = best;
                        int extra = (int) Math.floor(best * 5.0 * Math.max(0.0, magicMult));
                        magicStatExtra = extra;
                        if (extra != 0)
                            magicBeforeResist += extra;
                        magicStatChosenName = chosen.name();
                    }
                }
            } catch (Exception ignored) {
            }
            int resistValue = target.getResistance(magicType);
            magAfter = Math.floorDiv(magicBeforeResist * resistValue, 100);
            if (magAfter > 0)
                target.modifyHp(-magAfter);
        }

        // Build and send report if listener active
        try {
            if (attackListener != null) {
                AttackReport rpt = new AttackReport();
                rpt.attackName = attack.name;
                rpt.physRaw = physRaw;
                rpt.magRaw = magRaw;
                rpt.physAfterCritBeforeResist = totalPhysBeforeResist;
                rpt.physAfter = physAfter;
                rpt.magAfter = magAfter;
                rpt.times = attack.getTimes();
                rpt.damageType = (physicalType == null ? null : physicalType.name());
                rpt.magicType = (magicType == null ? null : magicType.name());
                rpt.critCount = physCritCount + magicCritCount; // legacy aggregate
                rpt.isCrit = (physCritCount + magicCritCount) > 0;
                rpt.physCritCount = physCritCount;
                rpt.magicCritCount = magicCritCount;
                rpt.physAttempts = physAttempts;
                rpt.physMissDodge = physMissDodge;
                rpt.physMissBlock = physMissBlock;
                rpt.magicAttempts = magicAttempts;
                rpt.magicMissDodge = magicMissDodge;
                rpt.magicMissResist = magicMissResist;
                rpt.dualRoll = hasPhysical && hasMagicComponent;
                rpt.trueDamage = isTrue;
                rpt.attacker = this;
                rpt.target = target;
                rpt.magicStatBonus = magicStatBonus;
                rpt.magicStatExtra = magicStatExtra;
                rpt.magicDamageMultiplier = magicMult;
                rpt.magicStatChosen = magicStatChosenName;
                // Physical debug approximations (we no longer expose physStatBase separately
                // here)
                rpt.physStatBase = 0; // could compute again if needed
                rpt.physStatExtra = 0; // ditto
                rpt.physDamageMultiplier = attack.damageMultiplier;
                rpt.physStatChosen = null; // omitted
                attackListener.accept(rpt);
            }
        } catch (Exception ignored) {
        }

        // Logging summary
        if (hasPhysical || isTrue) {
            System.out.println("Attack: " + attack.name + " Physical After: " + physAfter
                    + (magAfter > 0 ? (", Magic After: " + magAfter) : ""));
        } else if (magAfter > 0) {
            System.out.println("Attack: " + attack.name + " Magic After: " + magAfter);
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
        equipItem(item, requestTwoHanded);
    }

    /**
     * Equip an item and optionally treat it as two-handed. If the item is
     * inherently two-handed, it will always be equipped two-handed. If the
     * item is versatile and requestTwoHanded is true it will be equipped into
     * both WEAPON and OFFHAND slots (reusing the existing two-handed logic).
     */
    public void equipItem(Item item, boolean requestTwoHanded) {
        EquipmentSlot slot = item.getSlot();
        // Remove any existing item in the slot first
        Item oldItem = null;
        boolean willBeTwoHanded = false;
        try {
            if (item.isTwoHanded()) {
                willBeTwoHanded = true;
            } else if (item instanceof com.bapppis.core.item.Equipment) {
                com.bapppis.core.item.Equipment eq = (com.bapppis.core.item.Equipment) item;
                if (eq.getVersatile() && requestTwoHanded) {
                    willBeTwoHanded = true;
                }
            }
        } catch (Exception ignored) {
        }

        if (willBeTwoHanded) {
            oldItem = equipment.get(EquipmentSlot.WEAPON);
            if (oldItem != null)
                unequipItem(EquipmentSlot.WEAPON);
            oldItem = equipment.get(EquipmentSlot.OFFHAND);
            if (oldItem != null)
                unequipItem(EquipmentSlot.OFFHAND);
            equipment.put(EquipmentSlot.WEAPON, item);
            equipment.put(EquipmentSlot.OFFHAND, item);
        } else {
            oldItem = equipment.get(slot);
            if (oldItem != null)
                unequipItem(slot);
            equipment.put(slot, item);
        }
        // Remove from inventory if present
        getInventory().removeItem(item);
        // Apply stat and resistance effects if present
        applyItemEffects(item);
    }

    public void unequipItem(EquipmentSlot slot) {
        Item item = equipment.remove(slot);
        if (item == null)
            return;

        // If the item is two-handed, it may occupy both WEAPON and OFFHAND slots.
        // Remove any other slot that references the same instance to avoid leaving a
        // ghost.
        try {
            if (item.isTwoHanded()) {
                EquipmentSlot other = (slot == EquipmentSlot.WEAPON) ? EquipmentSlot.OFFHAND : EquipmentSlot.WEAPON;
                Item otherItem = equipment.get(other);
                if (otherItem != null && otherItem == item) {
                    // Remove the other slot without triggering effects/add again
                    equipment.remove(other);
                }
            }
        } catch (Exception e) {
            // Defensive: if item doesn't implement isTwoHanded or similar, ignore
        }

        // Remove effects once
        removeItemEffects(item);

        // Try to add back to inventory, but only if there's not already an item with
        // the same id
        boolean alreadyInInventory = false;
        try {
            int id = item.getId();
            // Check each inventory container for matching id
            for (Item it : getInventory().getWeapons())
                if (it.getId() == id) {
                    alreadyInInventory = true;
                    break;
                }
            if (!alreadyInInventory)
                for (Item it : getInventory().getOffhands())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : getInventory().getHelmets())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : getInventory().getArmors())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : getInventory().getLegwear())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : getInventory().getConsumables())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : getInventory().getMisc())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
        } catch (Exception e) {
            // If anything goes wrong checking ids, fall back to allowing add
            alreadyInInventory = false;
        }

        if (!alreadyInInventory) {
            boolean added = getInventory().addItem(item);
            if (!added) {
                System.out.println("Inventory full! Could not add " + item.getName() + " back to inventory.");
            }
        }
    }

    private void applyItemEffects(Item item) {
        if (!(item instanceof com.bapppis.core.item.Equipment))
            return;
        com.bapppis.core.item.Equipment eq = (com.bapppis.core.item.Equipment) item;

        // Stats
        if (eq.getStats() != null) {
            for (java.util.Map.Entry<String, Integer> entry : eq.getStats().entrySet()) {
                if (entry.getKey().equalsIgnoreCase("VISION_RANGE")) {
                    setVisionRange(getVisionRange() + entry.getValue());
                } else {
                    try {
                        Stats stat = Stats.valueOf(entry.getKey());
                        int prev = equipmentStats.getOrDefault(stat, 0);
                        equipmentStats.put(stat, prev + entry.getValue());
                        modifyStat(stat, entry.getValue());
                    } catch (IllegalArgumentException e) {
                        // ignore unknown stats
                    }
                }
            }
        }

        // Resistances
        if (eq.getResistances() != null) {
            for (java.util.Map.Entry<String, Integer> entry : eq.getResistances().entrySet()) {
                try {
                    Resistances res = Resistances.valueOf(entry.getKey());
                    int prev = equipmentResists.getOrDefault(res, 0);
                    equipmentResists.put(res, prev + entry.getValue());
                    modifyResistance(res, entry.getValue());
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }

        // crit/dodge/block aggregates
        try {
            int eqHpRegen = eq.getHpRegen();
            int eqStaminaRegen = eq.getStaminaRegen();
            int eqManaRegen = eq.getManaRegen();
            float eqCrit = eq.getCrit();
            float eqDodge = eq.getDodge();
            float eqBlock = eq.getBlock();
            float eqMagicResist = eq.getMagicResist();
            if (eqHpRegen != 0)
                equipmentHpRegen += eqHpRegen;
            if (eqStaminaRegen != 0)
                equipmentStaminaRegen += eqStaminaRegen;
            if (eqManaRegen != 0)
                equipmentManaRegen += eqManaRegen;
            if (eqCrit != 0f)
                equipmentCrit += eqCrit;
            if (eqDodge != 0f)
                equipmentDodge += eqDodge;
            if (eqBlock != 0f)
                equipmentBlock += eqBlock;
            if (eqMagicResist != 0f)
                equipmentMagicResist += eqMagicResist;
        } catch (Exception ignored) {
        }

        // Recompute derived stats now that stats/equipment changed
        recalcDerivedStats();
    }

    private void removeItemEffects(Item item) {
        if (!(item instanceof com.bapppis.core.item.Equipment))
            return;
        com.bapppis.core.item.Equipment eq = (com.bapppis.core.item.Equipment) item;

        // Stats
        if (eq.getStats() != null) {
            for (java.util.Map.Entry<String, Integer> entry : eq.getStats().entrySet()) {
                if (entry.getKey().equalsIgnoreCase("VISION_RANGE")) {
                    setVisionRange(getVisionRange() - entry.getValue());
                } else {
                    try {
                        Stats stat = Stats.valueOf(entry.getKey());
                        int prev = equipmentStats.getOrDefault(stat, 0);
                        equipmentStats.put(stat, prev - entry.getValue());
                        modifyStat(stat, -entry.getValue());
                    } catch (IllegalArgumentException e) {
                        // ignore unknown stats
                    }
                }
            }
        }

        // Resistances
        if (eq.getResistances() != null) {
            for (java.util.Map.Entry<String, Integer> entry : eq.getResistances().entrySet()) {
                try {
                    Resistances res = Resistances.valueOf(entry.getKey());
                    int prev = equipmentResists.getOrDefault(res, 0);
                    equipmentResists.put(res, prev - entry.getValue());
                    modifyResistance(res, -entry.getValue());
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }

        // crit/dodge/block aggregates
        try {
            int eqHpRegen = eq.getHpRegen();
            int eqStaminaRegen = eq.getStaminaRegen();
            int eqManaRegen = eq.getManaRegen();
            float eqCrit = eq.getCrit();
            float eqDodge = eq.getDodge();
            float eqBlock = eq.getBlock();
            float eqMagicResist = eq.getMagicResist();
            if (eqHpRegen != 0)
                equipmentHpRegen += eqHpRegen;
            if (eqStaminaRegen != 0)
                equipmentStaminaRegen += eqStaminaRegen;
            if (eqManaRegen != 0)
                equipmentManaRegen += eqManaRegen;
            if (eqCrit != 0f)
                equipmentCrit += eqCrit;
            if (eqDodge != 0f)
                equipmentDodge += eqDodge;
            if (eqBlock != 0f)
                equipmentBlock += eqBlock;
            if (eqMagicResist != 0f)
                equipmentMagicResist += eqMagicResist;
        } catch (Exception ignored) {
        }

        // Recompute derived stats now that stats/equipment changed
        recalcDerivedStats();
    }

    public void addProperty(Property property) {
        int id = property.getId();
    // Debug: print active property ids and current STR/DEX before applying
    com.bapppis.core.util.DebugLog.debug("addProperty called for id=" + id + " name='" + property.getName() + "' on " + this.getName());
    com.bapppis.core.util.DebugLog.debug(" Active traits=" + this.traits.keySet() + " active buffs=" + this.buffs.keySet() + " active debuffs=" + this.debuffs.keySet());
    com.bapppis.core.util.DebugLog.debug(" Stats before: STR=" + this.getSTR() + " DEX=" + this.getDEX());
        // Only apply if not already present to avoid double-applying effects
        if (id >= 1000 && id < 2333) {
            if (buffs.containsKey(id)) return;
            buffs.put(id, property);
        } else if (id >= 2333 && id < 3666) {
            if (debuffs.containsKey(id)) return;
            debuffs.put(id, property);
        } else if (id >= 3666 && id < 5000) {
            if (traits.containsKey(id)) return;
            traits.put(id, property);
        }
        // Apply property effects
        property.onApply(this);
        // Debug: print stats after applying and the property lists
        com.bapppis.core.util.DebugLog.debug(" Stats after: STR=" + this.getSTR() + " DEX=" + this.getDEX());
        com.bapppis.core.util.DebugLog.debug(" Active traits=" + this.traits.keySet() + " active buffs=" + this.buffs.keySet() + " active debuffs=" + this.debuffs.keySet());
    }

    public void removeProperty(int id) {
        Property property = null;
        if (id >= 1000 && id < 2333) {
            property = buffs.get(id);
            buffs.remove(id);
        } else if (id >= 2333 && id < 3666) {
            property = debuffs.get(id);
            debuffs.remove(id);
        } else if (id >= 3666 && id < 5000) {
            property = traits.get(id);
            traits.remove(id);
        }
        if (property != null) {
            property.onRemove(this);
        }
    }

    public void printStatusEffects() {
        System.out.println("Buffs:");
        for (Property buff : buffs.values()) {
            System.out.println(" - " + buff);
        }

        System.out.println("Debuffs:");
        for (Property debuff : debuffs.values()) {
            System.out.println(" - " + debuff);
        }
        System.out.println("Traits:");
        for (Property trait : traits.values()) {
            System.out.println(" - " + trait);
        }
    }

    public String printProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append("Buffs:\n");
        for (Property buff : buffs.values()) {
            sb.append(" - ").append(buff).append("\n");
        }
        sb.append("Debuffs:\n");
        for (Property debuff : debuffs.values()) {
            sb.append(" - ").append(debuff).append("\n");
        }
        sb.append("Traits:\n");
        for (Property trait : traits.values()) {
            sb.append(" - ").append(trait).append("\n");
        }
        return sb.toString();
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

    public void passTurn() {
        // Regen health/mana/stamina
        this.modifyHp(this.getHpRegen());
        this.modifyMana(this.getManaRegen());
        this.modifyStamina(this.getStaminaRegen());
    }

    /*
     * public void passTurn() {
     * // Apply buffs/debuffs effects
     * for (Property buff : buffs.values()) {
     * buff.onTurn(this);
     * }
     * for (Property debuff : debuffs.values()) {
     * debuff.onTurn(this);
     * }
     * // Decrease duration and remove expired
     * java.util.List<Integer> toRemove = new java.util.ArrayList<>();
     * for (Property buff : buffs.values()) {
     * if (buff.getDuration() > 0) {
     * buff.setDuration(buff.getDuration() - 1);
     * if (buff.getDuration() == 0) {
     * toRemove.add(buff.getId());
     * }
     * }
     * }
     * for (Property debuff : debuffs.values()) {
     * if (debuff.getDuration() > 0) {
     * debuff.setDuration(debuff.getDuration() - 1);
     * if (debuff.getDuration() == 0) {
     * toRemove.add(debuff.getId());
     * }
     * }
     * }
     * for (int id : toRemove) {
     * removeProperty(id);
     * }
     * }
     */

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

        // Set mana/stamina to max (recalc maxMana from INT and clamp)
        // Initialize baseMaxMana/baseMaxStamina only if they were not provided by JSON
        // (i.e., still zero or negative). If the JSON contains
        // baseMaxMana/baseMaxStamina
        // we respect those values. After ensuring base values, recompute derived maxes
        // and set current to max.

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

        // Equipped items (one per slot)
        sb.append("Equipment:\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item equipped = equipment.get(slot);
            sb.append("  ").append(slot.name()).append(": ");
            sb.append(equipped == null ? "Empty" : equipped.getName()).append("\n");
        }

        // Properties summary and inventory counts
        sb.append("Properties: ")
                .append("Buffs=").append(buffs.size()).append(" ")
                .append("Debuffs=").append(debuffs.size()).append(" ")
                .append("Traits=").append(traits.size()).append("\n");

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
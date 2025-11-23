
package com.bapppis.core.creature;

import java.util.EnumMap;
import java.util.HashMap;
import com.bapppis.core.util.WeaponUtil;
import com.bapppis.core.util.StatUtil;
import com.bapppis.core.util.ResistanceUtil;

import com.bapppis.core.property.Property;
import com.bapppis.core.spell.Spell;
import com.google.gson.Gson;
import com.bapppis.core.Resistances;
import com.bapppis.core.ResBuildUp;
import com.bapppis.core.creature.creatureEnums.CreatureType;
import com.bapppis.core.creature.creatureEnums.Size;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.creature.creatureEnums.Type;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.Weapon;
import com.bapppis.core.item.itemEnums.EquipmentSlot;

public abstract class Creature {
    private int id;
    private String name;
    private int visionRange = 2;
    private int level;
    private int xp;
    private int baseHp;
    private int maxHp;
    private int currentHp;
    private int hpDice;
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
    private java.util.EnumMap<ResBuildUp, Integer> resBuildUp;
    private java.util.EnumSet<ResBuildUp> freshResBuildUps = java.util.EnumSet.noneOf(ResBuildUp.class);
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
    private int accuracy;
    private int magicAccuracy;
    private final java.util.EnumMap<Stats, Integer> equipmentStats = new java.util.EnumMap<>(Stats.class);
    private final java.util.EnumMap<Resistances, Integer> equipmentResists = new java.util.EnumMap<>(Resistances.class);
    private int equipmentHpRegen = 0;
    private int equipmentStaminaRegen = 0;
    private int equipmentManaRegen = 0;
    private float equipmentCrit = 0f;
    private float equipmentDodge = 0f;
    private float equipmentBlock = 0f;
    private float equipmentMagicResist = 0f;
    private int equipmentAccuracy = 0;
    private int equipmentMagicAccuracy = 0;
    private float propertyCrit = 0f;
    private float propertyDodge = 0f;
    private float propertyBlock = 0f;
    private float propertyMagicResist = 0f;
    private int propertyAccuracy = 0;
    private int propertyMagicAccuracy = 0;
    private final java.util.EnumMap<Stats, Integer> statBonuses = new java.util.EnumMap<>(Stats.class);
    private PropertyManager propertyManager = new PropertyManager(this);
    private String description;
    private EnumMap<EquipmentSlot, Item> equipment = new EnumMap<>(EquipmentSlot.class);
    private Inventory inventory = new Inventory();
    private java.util.List<Attack> attacks = new java.util.ArrayList<>();
    private java.util.List<Spell> spells = new java.util.ArrayList<>();
    private java.util.List<com.bapppis.core.spell.SpellReference> spellReferences = new java.util.ArrayList<>();
    private String sprite;
    private String deathSprite; // Sprite to use when this creature dies (becomes a corpse)
    private String lootPool; // Loot pool ID for this creature's drops

    // --- Base stat modifier helpers ---
    public void modifyBaseCrit(float delta) {
        this.baseCrit += delta;
        updateCrit();
    }

    public void modifyBaseDodge(float delta) {
        this.baseDodge += delta;
        recalcDerivedStats();
    }

    public void modifyBaseBlock(float delta) {
        this.baseBlock += delta;
        recalcDerivedStats();
    }

    public void modifyBaseMagicResist(float delta) {
        this.baseMagicResist += delta;
        recalcDerivedStats();
    }

    public void modifyBaseAccuracy(int delta) {
        this.accuracy += delta;
    }

    public void modifyBaseMagicAccuracy(int delta) {
        this.magicAccuracy += delta;
    }

    public String getSprite() {
        return sprite;
    }

    public String getDeathSprite() {
        return deathSprite;
    }

    public String getLootPool() {
        return lootPool;
    }

    // --- Additional base field modifiers ---
    public void modifyBaseHp(int delta) {
        this.baseHp += delta;
        recalcMaxHp();
    }

    public void modifyBaseMaxMana(int delta) {
        this.baseMaxMana += delta;
        updateMaxMana();
    }

    public void modifyBaseMaxStamina(int delta) {
        this.baseMaxStamina += delta;
        updateMaxStamina();
    }

    public void modifyBaseHpRegen(int delta) {
        this.baseHpRegen += delta;
        // hpRegen uses equipment and property additions; keep base in sync
        this.hpRegen = Math.max(0, this.baseHpRegen);
    }

    public void modifyBaseStaminaRegen(int delta) {
        this.baseStaminaRegen += delta;
        this.staminaRegen = Math.max(0, this.baseStaminaRegen);
    }

    public void modifyBaseManaRegen(int delta) {
        this.baseManaRegen += delta;
        this.manaRegen = Math.max(0, this.baseManaRegen);
    }

    public Creature() {
        stats = new EnumMap<>(Stats.class);
        for (Stats stat : Stats.values()) {
            if (stat == Stats.LUCK) {
                stats.put(stat, 1); // Luck default is 1
            } else {
                stats.put(stat, 10); // other stats default to 10
            }
        }
        recalcStatBonuses();
        resistances = new EnumMap<>(Resistances.class);
        for (Resistances res : Resistances.values()) {
            // TRUE damage defaults to 50% resistance, other types default to 100%
            if (res == Resistances.TRUE) {
                resistances.put(res, 50);
            } else {
                resistances.put(res, 100); // default resistance 100%
            }
        }
        // Initialize ResBuildUp values to 0 for every enum entry
        resBuildUp = new java.util.EnumMap<>(ResBuildUp.class);
        for (ResBuildUp rb : ResBuildUp.values()) {
            resBuildUp.put(rb, 0);
        }
        size = Size.MEDIUM; // default size
        type = Type.ENEMY; // default type
        level = 0; // default level
        xp = 0; // default xp
        recalcDerivedStats();
        
        // Set inventory owner
        inventory.setOwner(this);
    }

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
            maxHp = 1;
        }
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

    public int getHpDice() {
        return hpDice;
    }

    public void setHpDice(int hpDice) {
        this.hpDice = hpDice;
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
            maxMana = 0;
        }
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
        int delta = this.getStatBonus(Stats.INTELLIGENCE);
        double factor = 1.0;
        if (delta < 0) {
            factor = Math.pow(0.9, -delta);
        } else if (delta > 0) {
            factor = Math.pow(1.1, delta);
        }
        int newMax = (int) Math.floor(this.getBaseMaxMana() * factor);
        newMax = Math.max(25, newMax);
        this.setMaxMana(newMax);
    }

    public void setMaxStamina(int maxStamina) {
        if (maxStamina < 0) {
            maxStamina = 0;
        }
        double ratio = this.maxStamina > 0 ? (double) currentStamina / this.maxStamina : 1.0;
        this.maxStamina = maxStamina;
        this.currentStamina = Math.max(0, (int) (this.maxStamina * ratio));
        this.baseStaminaRegen = Math.max(1, Math.floorDiv(this.maxStamina, 5));
        recalcDerivedStats();
    }

    public void updateMaxStamina() {
        int delta = this.getStatBonus(Stats.CONSTITUTION);
        double factor = 1.0;
        if (delta < 0) {
            factor = Math.pow(0.9, -delta);
        } else if (delta > 0) {
            factor = Math.pow(1.1, delta);
        }
        int newMax = (int) Math.floor(this.getBaseMaxStamina() * factor);
        newMax = Math.max(25, newMax);
        this.setMaxStamina(newMax);
    }

    public void setCurrentStamina(int stamina) {
        this.currentStamina = Math.max(0, Math.min(stamina, this.maxStamina));
        recalcDerivedStats();
    }

    public void modifyStamina(int amount) {
        this.currentStamina = Math.max(0, Math.min(this.currentStamina + amount, this.maxStamina));
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
        // Ensure all derived stats are consistent after any stat change
        // (covers STR/DEX/CHA which may affect derived values indirectly)
        recalcDerivedStats();
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
        // Ensure all derived stats are consistent after any stat change
        // (covers STR/DEX/CHA which may affect derived values indirectly)
        recalcDerivedStats();
    }

    // --- Stat helper convenience methods ---
    /**
     * Generic increase/decrease helpers that delegate to modifyStat so all
     * derived values and cached bonuses remain consistent.
     */

    public int getResistance(Resistances resistance) {
        return resistances.getOrDefault(resistance, 0);
    }

    public void setResistance(Resistances resistance, int value) {
        resistances.put(resistance, value);
    }

    public void modifyResistance(Resistances resistance, int amount) {
        // Adds the amount to the current resistance value, defaulting to 100 if unset
        int base = resistances.containsKey(resistance) ? resistances.get(resistance) : 100;
        resistances.put(resistance, base + amount);
    }

    // --- ResBuildUp helpers ---
    public int getResBuildUp(ResBuildUp key) {
        return resBuildUp.getOrDefault(key, 0);
    }

    /**
     * Mark a ResBuildUp as freshly increased so it will be skipped by the next
     * decay pass. Safe to call even if the EnumSet is uninitialized.
     */
    public void markResBuildUpFresh(ResBuildUp key) {
        if (key == null)
            return;
        if (freshResBuildUps == null)
            freshResBuildUps = java.util.EnumSet.noneOf(ResBuildUp.class);
        freshResBuildUps.add(key);
    }

    /**
     * Test whether a ResBuildUp was marked fresh; if so, clear the flag and
     * return true. Used by the decay routine to skip one tick of decay.
     */
    public boolean testAndClearResBuildUpFresh(ResBuildUp key) {
        if (key == null || freshResBuildUps == null)
            return false;
        return freshResBuildUps.remove(key);
    }

    /**
     * Set an absolute value for the buildup. Allowed: 0..100 or -1 for immunity.
     */
    public void setResBuildUpAbsolute(ResBuildUp key, int value) {
        if (value == -1) {
            resBuildUp.put(key, -1);
            return;
        }
        int v = Math.max(0, Math.min(100, value));
        resBuildUp.put(key, v);
    }

    /**
     * Modify buildup by delta. If current value is -1 (immune) no change will
     * occur.
     * Result is clamped to 0..100.
     * After modification, checks for overload (buildup >= 100%) and triggers
     * debuff if applicable.
     */
    public void modifyResBuildUp(ResBuildUp key, int delta) {
        int cur = resBuildUp.getOrDefault(key, 0);
        if (cur == -1)
            return; // immune, cannot be changed while immune
        int next = cur + delta;
        next = Math.max(0, Math.min(100, next));
        resBuildUp.put(key, next);
        // Check for overload after every modification
        com.bapppis.core.util.ResistanceUtil.checkResOverload(this);
    }

    public boolean isResBuildUpImmune(ResBuildUp key) {
        return resBuildUp.getOrDefault(key, 0) == -1;
    }

    /**
     * Sets the resistance to an absolute value, ignoring previous value.
     */
    public void setResistanceAbsolute(Resistances resistance, int value) {
        resistances.put(resistance, value);
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

    public float setDodge(float dodge) {
        this.dodge = dodge;
        return this.dodge;
    }

    public float getBlock() {
        return this.block + this.equipmentBlock + this.propertyBlock;
    }

    public float setBlock(float block) {
        this.block = block;
        return this.block;
    }

    public float getMagicResist() {
        return this.magicResist + this.equipmentMagicResist + this.propertyMagicResist;
    }

    public int getAccuracy() {
        return accuracy + equipmentAccuracy + propertyAccuracy;
    }

    public int getMagicAccuracy() {
        return magicAccuracy + equipmentMagicAccuracy + propertyMagicAccuracy;
    }

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

    public void recalcStatBonuses() {
        for (Stats s : Stats.values()) {
            int raw = this.getStat(s);
            int bonus = (s == Stats.LUCK) ? raw : (raw - 10);
            statBonuses.put(s, bonus);
        }
    }

    public int getStatBonus(Stats s) {
        return statBonuses.getOrDefault(s, 0);
    }

    public void attack(Creature target) {
        // Build weighted pool: weapon attacks, creature attacks, and spells
        java.util.List<Object> actionPool = new java.util.ArrayList<>();
        java.util.List<Integer> weights = new java.util.ArrayList<>();

        // Add weapon attacks to pool
        Item equipped = this.getEquipped(EquipmentSlot.WEAPON);
        Weapon weapon = null;
        if (equipped instanceof Weapon) {
            weapon = (Weapon) equipped;
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
                for (Attack atk : weaponAttackList) {
                    actionPool.add(atk);
                    weights.add(atk.getWeight());
                }
            }
        }

        // Add creature's natural attacks to pool
        if (this.attacks != null && !this.attacks.isEmpty()) {
            for (Attack atk : this.attacks) {
                actionPool.add(atk);
                weights.add(atk.getWeight());
            }
        }

        // Add creature's spells to pool (only if creature has enough mana)
        // Use spell references to get the weight defined in creature JSON
        if (this.spellReferences != null && !this.spellReferences.isEmpty()) {
            for (com.bapppis.core.spell.SpellReference spellRef : this.spellReferences) {
                // Find the actual spell by name
                com.bapppis.core.spell.Spell spell = null;
                for (com.bapppis.core.spell.Spell s : this.spells) {
                    if (s.getName().equals(spellRef.getName())) {
                        spell = s;
                        break;
                    }
                }
                if (spell != null && this.getCurrentMana() >= spell.getManaCost()) {
                    actionPool.add(spell);
                    weights.add(spellRef.getWeight());
                }
            }
        }

        // If no actions available, return
        if (actionPool.isEmpty()) {
            return;
        }

        // Weighted random selection from pool
        int totalWeight = 0;
        for (int w : weights) {
            totalWeight += w;
        }

        int pick = java.util.concurrent.ThreadLocalRandom.current().nextInt(Math.max(1, totalWeight));
        Object chosen = null;
        for (int i = 0; i < actionPool.size(); i++) {
            pick -= weights.get(i);
            if (pick < 0) {
                chosen = actionPool.get(i);
                break;
            }
        }
        if (chosen == null && !actionPool.isEmpty()) {
            chosen = actionPool.get(0);
        }

        // Execute the chosen action
        if (chosen instanceof Spell) {
            // Cast spell
            Spell spell = (Spell) chosen;
            com.bapppis.core.spell.SpellEngine.castSpell(this, spell, target);
        } else if (chosen instanceof Attack) {
            // Execute attack
            Attack attack = (Attack) chosen;
            int statBonus;
            Resistances physType = attack.getDamageTypeEnum();
            Resistances magType = attack.getMagicDamageTypeEnum();

            if (weapon != null
                    && actionPool.indexOf(attack) < (weapon.getAttacks() != null ? weapon.getAttacks().size() : 0)) {
                // This is a weapon attack
                statBonus = WeaponUtil.determineWeaponStatBonus(this, weapon) * 5;
                com.bapppis.core.combat.AttackEngine.applyAttackToTarget(this, attack, statBonus, target,
                        weapon.getDamageType(), weapon.getMagicElement(), weapon);
            } else {
                // This is a natural attack
                statBonus = Math.max(0, this.getStatBonus(Stats.STRENGTH)) * 5;
                com.bapppis.core.combat.AttackEngine.applyAttackToTarget(this, attack, statBonus, target, physType,
                        magType, null);
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public java.util.List<Spell> getSpells() {
        return spells;
    }

    public java.util.List<com.bapppis.core.spell.SpellReference> getSpellReferences() {
        return spellReferences;
    }

    public void addSpell(Spell spell) {
        if (spell != null && !spells.contains(spell)) {
            spells.add(spell);
        }
    }

    public void removeSpell(Spell spell) {
        spells.remove(spell);
    }

    public Item getEquipped(EquipmentSlot slot) {
        return equipment.get(slot);
    }

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

    void adjustEquipmentAccuracy(int delta) {
        this.equipmentAccuracy += delta;
    }

    void adjustEquipmentMagicAccuracy(int delta) {
        this.equipmentMagicAccuracy += delta;
    }

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
        if (item instanceof com.bapppis.core.item.Weapon) {
            try {
                com.bapppis.core.item.Weapon weapon = (com.bapppis.core.item.Weapon) item;
                requestTwoHanded = weapon.isTwoHanded();
            } catch (Exception ignored) {
            }
        }
        ItemManager.equip(this, item, requestTwoHanded);
    }

    public boolean addItemByName(String name) {
        if (name == null || name.isBlank())
            return false;
        String t = name.trim();
        com.bapppis.core.item.Item template = null;
        try {
            int id = Integer.parseInt(t);
            template = com.bapppis.core.item.ItemLoader.getItemById(id);
        } catch (NumberFormatException ignored) {
        }
        if (template == null)
            template = com.bapppis.core.item.ItemLoader.getItemByName(name);
        if (template == null)
            return false;
        try {
            Gson g = new Gson();
            com.bapppis.core.item.Item copy = g.fromJson(g.toJson(template), template.getClass());

            // Copy transient properties field manually since GSON won't copy it
            if (template instanceof com.bapppis.core.item.Equipment
                    && copy instanceof com.bapppis.core.item.Equipment) {
                com.bapppis.core.item.Equipment templateEq = (com.bapppis.core.item.Equipment) template;
                com.bapppis.core.item.Equipment copyEq = (com.bapppis.core.item.Equipment) copy;
                if (templateEq.getProperties() != null) {
                    copyEq.setProperties(templateEq.getProperties());
                }
            }
            // Copy transient properties field for consumables too
            if (template.getProperties() != null) {
                copy.setProperties(template.getProperties());
            }

            return this.getInventory().addItem(copy);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equipItemByName(String name) {
        if (name == null || name.isBlank())
            return false;
        boolean added = addItemByName(name);
        if (!added)
            return false;
        // Find the newly added item in inventory (by name, case-insensitive) and equip
        // it
        try {
            for (com.bapppis.core.item.Item it : getInventory().getWeapons()) {
                if (it.getName() != null && it.getName().equalsIgnoreCase(name.trim())) {
                    equipItem(it);
                    return true;
                }
            }
            for (com.bapppis.core.item.Item it : getInventory().getOffhands()) {
                if (it.getName() != null && it.getName().equalsIgnoreCase(name.trim())) {
                    equipItem(it);
                    return true;
                }
            }
            for (com.bapppis.core.item.Item it : getInventory().getHelmets()) {
                if (it.getName() != null && it.getName().equalsIgnoreCase(name.trim())) {
                    equipItem(it);
                    return true;
                }
            }
            for (com.bapppis.core.item.Item it : getInventory().getArmors()) {
                if (it.getName() != null && it.getName().equalsIgnoreCase(name.trim())) {
                    equipItem(it);
                    return true;
                }
            }
            for (com.bapppis.core.item.Item it : getInventory().getLegwear()) {
                if (it.getName() != null && it.getName().equalsIgnoreCase(name.trim())) {
                    equipItem(it);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean consumeItemByName(String name) {
        if (name == null || name.isBlank())
            return false;
        String t = name.trim();
        try {
            // try numeric id match first
            try {
                int id = Integer.parseInt(t);
                for (com.bapppis.core.item.Item it : getInventory().getConsumables()) {
                    if (it.getId() == id) {
                        try {
                            it.onApply(this);
                        } catch (Exception ignored) {
                        }
                        getInventory().removeItem(it);
                        return true;
                    }
                }
            } catch (NumberFormatException ignored) {
            }

            for (com.bapppis.core.item.Item it : getInventory().getConsumables()) {
                if (it.getName() != null && it.getName().equalsIgnoreCase(t)) {
                    try {
                        it.onApply(this);
                    } catch (Exception ignored) {
                    }
                    getInventory().removeItem(it);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean dropItemByName(String name) {
        if (name == null || name.isBlank())
            return false;
        String t = name.trim();
        try {
            try {
                int id = Integer.parseInt(t);
                // search all containers
                java.util.List<java.util.List<com.bapppis.core.item.Item>> containers = java.util.Arrays.asList(
                        getInventory().getWeapons(), getInventory().getOffhands(), getInventory().getHelmets(),
                        getInventory().getArmors(), getInventory().getLegwear(), getInventory().getConsumables(),
                        getInventory().getMisc());
                for (java.util.List<com.bapppis.core.item.Item> cont : containers) {
                    for (com.bapppis.core.item.Item it : cont) {
                        if (it.getId() == id) {
                            getInventory().removeItem(it);
                            // System.out.println("Dropped: " + it.getName() + " (placeholder, no world
                            // placement)");
                            return true;
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }

            java.util.List<java.util.List<com.bapppis.core.item.Item>> containers = java.util.Arrays.asList(
                    getInventory().getWeapons(), getInventory().getOffhands(), getInventory().getHelmets(),
                    getInventory().getArmors(), getInventory().getLegwear(), getInventory().getConsumables(),
                    getInventory().getMisc());
            for (java.util.List<com.bapppis.core.item.Item> cont : containers) {
                for (com.bapppis.core.item.Item it : cont) {
                    if (it.getName() != null && it.getName().equalsIgnoreCase(t)) {
                        getInventory().removeItem(it);
                        // System.out.println("Dropped: " + it.getName() + " (placeholder, no world
                        // placement)");
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean unequipItemByName(String name) {
        if (name == null || name.isBlank())
            return false;
        String t = name.replaceAll("\\s+", "").toLowerCase();
        try {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                Item eq = this.getEquipped(slot);
                if (eq != null && eq.getName() != null) {
                    String eqName = eq.getName().replaceAll("\\s+", "").toLowerCase();
                    if (eqName.equals(t)) {
                        this.unequipItem(slot);
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public void equipItem(Item item, boolean requestTwoHanded) {
        ItemManager.equip(this, item, requestTwoHanded);
    }

    public void unequipItem(EquipmentSlot slot) {
        ItemManager.unequip(this, slot);
    }

    public void addProperty(Property property) {
        propertyManager.add(property);
    }

    public void addProperty(int id) {
        propertyManager.addById(id);
    }

    public boolean addProperty(String name) {
        return propertyManager.addByName(name);
    }

    public void removeProperty(int id) {
        propertyManager.remove(id);
    }

    public void printStatusEffects() {
        propertyManager.printStatusEffects();
    }

    public boolean removeProperty(String name) {
        return propertyManager.removeByName(name);
    }

    public String printProperties() {
        // Preserve the existing single-column output for callers that rely on it.
        return propertyManager.printProperties();
    }

    public String printProperties(int columns) {
        if (columns <= 1) {
            return printProperties();
        }

        return propertyManager.formatPropertiesInColumns(propertyManager.getBuffs().values(), columns)
                + propertyManager.formatPropertiesInColumns(propertyManager.getDebuffs().values(), columns)
                + propertyManager.formatPropertiesInColumns(propertyManager.getTraits().values(), columns);
    }

    public void updateMaxHp() {
        int conBonus = this.getStatBonus(Stats.CONSTITUTION);
        int bonusHp = (this.level + 1) * (this.hpDice + conBonus);
        bonusHp = Math.max(1, bonusHp);
        this.setMaxHp(this.baseHp + bonusHp);
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
        // Decay build-ups via centralized util so behavior is consistent and testable
        ResistanceUtil.decayResBuildUps(this);
    }

    public void recalcMaxHp() {
        // Preserve currentHp/maxHp ratio when maxHp changes
        double ratio = this.maxHp > 0 ? (double) currentHp / this.maxHp : 1.0;
        int conBonus = this.getStatBonus(Stats.CONSTITUTION);
        int newMaxHp = this.baseHp + ((this.level + 1) * (this.hpDice + conBonus));
        this.maxHp = Math.max(1, newMaxHp);
        this.currentHp = Math.max(1, (int) (this.maxHp * ratio));
    }

    /**
     * Hook method called after JSON data is loaded but before finalization.
     * Override in species/type subclasses to apply modifications that should
     * take priority over JSON values (e.g., resistance/stat adjustments).
     * Default implementation does nothing.
     */
    protected void applySpeciesModifications() {
        // Default: no modifications
    }

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
    }

    public void printAllFields() {
        System.out.println("Creature: " + getName() + " (Id:" + getId() + ")");
        System.out.println("Class: " + getClass().getSimpleName());
        System.out.println("Level: " + getLevel());
        System.out.println("Type: " + getType());
        System.out.println("CreatureType: " + getCreatureType());
        System.out.println("Size: " + getSize());
        System.out.println("XP: " + getXp());
        System.out.println("VisionRange: " + getVisionRange());
        System.out.println("HP: " + getCurrentHp() + "/" + getMaxHp() + " (Base: " + getBaseHp() + " + Dice: "
                + getHpDice() + " + ConBonus: " + (getStatBonus(Stats.CONSTITUTION)) + " * (Level+1))");
        System.out.println("Mana: " + getCurrentMana() + "/" + getMaxMana() + " (Base: " + getBaseMaxMana() + ")");
        System.out.println(
                "Stamina: " + getCurrentStamina() + "/" + getMaxStamina() + " (Base: " + getBaseMaxStamina() + ")");
        System.out.println("Stats: " + stats);
        System.out.println("Resistances: " + resistances);
        System.out.println("Crit: " + crit);
        System.out.println("Dodge: " + dodge);
        System.out.println("Block: " + block);
        System.out.println("MagicResist: " + magicResist);
        System.out.println("Accuracy: " + accuracy + " MagicAccuracy: " + magicAccuracy);
        System.out.println("Equipment: " + equipment);
        System.out.println("Inventory: " + inventory);
        System.out.println("Properties: Buffs=" + getBuffs().size() + " Debuffs=" + getDebuffs().size() + " Traits="
                + getTraits().size());
        System.out.println("PropertyManager: " + propertyManager);
        System.out.println("Description: " + getDescription());
        System.out.println("Sprite: " + getSprite());
        System.out.println("Attacks: " + attacks);
        // Add more fields if needed for deeper inspection
    }

    @Override
    public String toString() {
        // Small print: "SMALL Goblin (HUMANOID)" or similar
        StringBuilder sb = new StringBuilder();
        if (getSize() != null) {
            sb.append(getSize().name()).append(" ");
        }
        if (getName() != null) {
            sb.append(getName());
        } else {
            sb.append("Creature");
        }
        // Print species if available (not base types)
        String species = getClass().getSimpleName();
        if (!species.equals("Player") && !species.equals("Enemy") && !species.equals("NPC")
                && !species.equals("CreatureType") && !species.equals("Creature")) {
            sb.append(" (").append(species.toUpperCase()).append(")");
        } else if (getCreatureType() != null) {
            sb.append(" (").append(getCreatureType().name()).append(")");
        }
        return sb.toString();
    }
}
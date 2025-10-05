package com.bapppis.core.creature;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import com.bapppis.core.util.Dice;

import com.bapppis.core.property.Property;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.Equipment;
import com.bapppis.core.item.EquipmentSlot;

public abstract class Creature {
    // --- Fields ---
    private int id; // id set by Gson, no setter
    private String name;
    private int visionRange = 2; // default vision range
    private int level;
    private int xp;
    private int baseHp;
    private int maxHp;
    private int currentHp;
    private int hpLvlBonus; // Additional HP gained per level
    private int currentMana;
    private int currentStamina;
    private int maxMana = 100;
    private int baseMaxMana = 100;
    private int maxStamina = 100;
    private int baseMaxStamina = 100;
    private Size size;
    private Type type;
    private CreatureType creatureType;
    private EnumMap<Stats, Integer> stats;
    private EnumMap<Resistances, Integer> resistances;
    private float baseCrit;
    private float baseDodge;
    private float baseBlock;
    private float crit; // base-derived critical hit chance percentage (0-100)
    private float dodge; // base-derived dodge chance percentage (0-100)
    private float block; // base-derived block chance percentage (0-100)
    // Equipment aggregates (sum of all equipped item contributions)
    private final java.util.EnumMap<Stats, Integer> equipmentStats = new java.util.EnumMap<>(Stats.class);
    private final java.util.EnumMap<Resistances, Integer> equipmentResists = new java.util.EnumMap<>(Resistances.class);
    private float equipmentCrit = 0f;
    private float equipmentDodge = 0f;
    private float equipmentBlock = 0f;
    // Cached per-stat bonuses (e.g. STR 14 -> +4, WIS 7 -> -3). Luck is special:
    // its bonus equals the raw luck value (Luck 1 -> 1, Luck 0 -> 0).
    private final java.util.EnumMap<Stats, Integer> statBonuses = new java.util.EnumMap<>(Stats.class);
    private HashMap<Integer, Property> buffs = new HashMap<>();
    private HashMap<Integer, Property> debuffs = new HashMap<>();
    private HashMap<Integer, Property> immunities = new HashMap<>();
    private HashMap<Integer, Property> traits = new HashMap<>();
    private String description;
    private EnumMap<EquipmentSlot, Item> equipment = new EnumMap<>(EquipmentSlot.class);
    private Inventory inventory = new Inventory();
    // Attacks defined on creatures (Gson will fill this from JSON)
    private java.util.List<Attack> attacks = new java.util.ArrayList<>();
    // Optional sprite key loaded from JSON (e.g. "player_biggles",
    // "monster_goblin")
    private String sprite;

    // Test hook: optional consumer to receive detailed attack reports when an
    // attack occurs.
    // Tests can set Creature.attackListener to capture raw rolls and metadata.
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
        if (this.level >= 30) {
            this.level = 30;
            this.xp = 0;
        } else if (currentXp >= ((this.level + 1) * 10)) {
            this.level++;
            this.updateMaxHp();
            currentXp -= ((this.level) * 10);
            addXp(currentXp); // Recursively add remaining XP
        } else {
            this.xp = this.xp + xp;
        }
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

    public void alterHp(int amount) {
        this.currentHp = Math.max(0, this.currentHp + amount);
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

    public void alterMana(int amount) {
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
    }

    public void alterStamina(int amount) {
        this.currentStamina = Math.max(0, Math.min(this.currentStamina + amount, this.maxStamina));
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

    public HashMap<Integer, Property> getImmunities() {
        return immunities;
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

    public Property getImmunity(int id) {
        return immunities.get(id);
    }

    public Property getTrait(int id) {
        return traits.get(id);
    }

    public int getStat(Stats stat) {
        return stats.getOrDefault(stat, 0);
    }

    public void setStat(Stats stat, int value) {
        stats.put(stat, value);
        // Keep cached bonuses in sync whenever a stat changes
        recalcStatBonuses();
        if (stat == Stats.CONSTITUTION) {
            alterHp();
            // Recompute max stamina when CON changes
            updateMaxStamina();
        }
        if (stat == Stats.INTELLIGENCE) {
            // Recompute max mana when INT changes
            updateMaxMana();
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
            alterHp();
            // Recompute max stamina when CON changes
            updateMaxStamina();
        }
        if (stat == Stats.INTELLIGENCE) {
            // Recompute max mana when INT changes
            updateMaxMana();
        }
        if (stat == Stats.LUCK) {
            // Recompute crit when luck changes
            recalcDerivedStats();
        }
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
            if (weapon.getAttacks() != null && !weapon.getAttacks().isEmpty()) {
                chosen = chooseAttackFromList(weapon.getAttacks());
                // stat bonus depends on weapon class/finesse
                int statBonus = determineStatBonusForWeapon(weapon) * 5;
                applyAttackToTarget(chosen, statBonus, target, weapon.getDamageType(), weapon.getMagicElement());
                return;
            }
        }

        // No weapon attacks — use creature's own attacks if present
        if (this.attacks != null && !this.attacks.isEmpty()) {
            chosen = chooseAttackFromList(this.attacks);
            // For creature attacks, default to strength for physical damage
            int statBonus = Math.max(0, this.getStatBonus(Stats.STRENGTH)) * 5;
            Resistances physType = parseResistance(chosen == null ? null : chosen.damageType);
            Resistances magType = parseResistance(chosen == null ? null : chosen.magicDamageType);
            applyAttackToTarget(chosen, statBonus, target, physType, magType);
            return;
        }
    }

    private int determineStatBonusForWeapon(Equipment weapon) {
        if (weapon.isFinesse()) {
            return Math.max(0, Math.max(this.getStatBonus(Stats.STRENGTH), this.getStatBonus(Stats.DEXTERITY)));
        }
        switch (weapon.getWeaponClass()) {
            case MELEE:
                return Math.max(0, this.getStatBonus(Stats.STRENGTH));
            case RANGED:
                return Math.max(0, this.getStatBonus(Stats.DEXTERITY));
            case MAGIC:
                return Math.max(0, this.getStatBonus(Stats.INTELLIGENCE));
            default:
                return 0;
        }
    }

    private Attack chooseAttackFromList(java.util.List<Attack> list) {
        if (list == null || list.isEmpty())
            return null;
        int total = 0;
        for (Attack a : list)
            total += a.getWeight();
        int pick = ThreadLocalRandom.current().nextInt(Math.max(1, total));
        for (Attack a : list) {
            pick -= a.getWeight();
            if (pick < 0)
                return a;
        }
        return list.get(0);
    }

    private void applyAttackToTarget(Attack attack, int statBonus, Creature target, Resistances physicalType,
            Resistances magicType) {
        if (attack == null || target == null)
            return;
        // Roll physical damage per-hit and check crit per hit
        int totalPhysBeforeResist = 0; // after applying per-hit crits
        int physRaw = 0; // raw sum before crits
        int critCount = 0;
        int times = attack.getTimes();
        float baseCrit = this.getCrit();
        int mod = 0;
        try {
            mod = attack.getCritMod();
        } catch (Exception e) {
            mod = 0;
        }
        // Effective crit chance used in checks is clamped to 0-100
        float critChance = Math.max(0f, Math.min(100f, baseCrit + mod));
        for (int i = 0; i < times; i++) {
            float rawToHitRoll = ThreadLocalRandom.current().nextFloat() * 100f;
            int toHitRoll = Math.round(rawToHitRoll) + statBonus; // rounded to nearest whole number
            // Effective dodge/block are clamped to 0-100 for checks
            float effectiveDodge = Math.max(0f, Math.min(100f, target.getDodge()));
            float effectiveBlock = Math.max(0f, Math.min(100f, target.getBlock()));

            // Partition the avoidance window so ranges don't overlap. The higher
            // of dodge/block occupies the upper portion of the avoidance window.
            float totalAvoid = Math.min(100f, effectiveDodge + effectiveBlock);
            if (toHitRoll <= totalAvoid) {
                // We're in the avoidance region; decide whether it's block or dodge
                if (effectiveDodge >= effectiveBlock) {
                    // Block is the lower subrange [0..effectiveBlock], dodge is above it
                    if (toHitRoll <= effectiveBlock) {
                        System.out.println("Missed (block): " + this.getName() + " -> " + target.getName()
                                + " | attack='" + attack.name + "' roll=" + String.format("%.2f", rawToHitRoll)
                                + " rounded=" + toHitRoll + " statBonus=" + statBonus
                                + " block=" + String.format("%.2f", effectiveBlock));
                        continue;
                    } else {
                        System.out.println("Missed (dodge): " + this.getName() + " -> " + target.getName()
                                + " | attack='" + attack.name + "' roll=" + String.format("%.2f", rawToHitRoll)
                                + " rounded=" + toHitRoll + " statBonus=" + statBonus
                                + " dodge=" + String.format("%.2f", effectiveDodge));
                        continue;
                    }
                } else {
                    // Dodge is the lower subrange [0..effectiveDodge], block is above it
                    if (toHitRoll <= effectiveDodge) {
                        System.out.println("Missed (dodge): " + this.getName() + " -> " + target.getName()
                                + " | attack='" + attack.name + "' roll=" + String.format("%.2f", rawToHitRoll)
                                + " rounded=" + toHitRoll + " statBonus=" + statBonus
                                + " dodge=" + String.format("%.2f", effectiveDodge));
                        continue;
                    } else {
                        System.out.println("Missed (block): " + this.getName() + " -> " + target.getName()
                                + " | attack='" + attack.name + "' roll=" + String.format("%.2f", rawToHitRoll)
                                + " rounded=" + toHitRoll + " statBonus=" + statBonus
                                + " block=" + String.format("%.2f", effectiveBlock));
                        continue;
                    }
                }
            }
            int hit = 0;
            if (attack.physicalDamageDice != null && !attack.physicalDamageDice.isBlank()) {
                // For per-hit dice, roll once per hit.
                hit = Dice.roll(attack.physicalDamageDice);
            }
            hit += Math.max(0, statBonus);
            // Only successful hits count toward physRaw (pre-crit raw) and totals
            physRaw += hit;
            // Convert critChance (0-100) into a 0.0-1.0 probability and compare with
            // nextFloat()
            // Effective crit chance is clamped 0-100 and converted to 0.0-1.0
            float effectiveCrit = Math.max(0f, Math.min(100f, critChance));
            boolean hitCrit = ThreadLocalRandom.current().nextFloat() < (effectiveCrit / 100f);
            if (hitCrit) {
                critCount++;
                hit = hit * 2; // double this hit
                System.out.println("Critical hit! " + attack.name + " single hit doubled to: " + hit
                        + " | roll=" + String.format("%.2f", rawToHitRoll) + " rounded=" + toHitRoll + " statBonus="
                        + statBonus);
            }
            totalPhysBeforeResist += hit;
        }

        int physAfter = Math.floorDiv(
                totalPhysBeforeResist
                        * (physicalType == null ? 100 : target.getResistance(physicalType)),
                100);
        int mag = attack.rollMagicDamage();
        int magAfter = magicType != null ? Math.floorDiv(mag * target.getResistance(magicType), 100) : 0;

        // If a test listener is set, populate and send an AttackReport
        try {
            if (attackListener != null) {
                AttackReport rpt = new AttackReport();
                rpt.attackName = attack.name;
                rpt.physRaw = physRaw;
                rpt.magRaw = mag;
                rpt.physAfterCritBeforeResist = totalPhysBeforeResist;
                rpt.physAfter = physAfter;
                rpt.magAfter = magAfter;
                rpt.times = attack.getTimes();
                rpt.damageType = (physicalType == null ? null : physicalType.name());
                rpt.magicType = (magicType == null ? null : magicType.name());
                rpt.critCount = critCount;
                rpt.isCrit = critCount > 0;
                rpt.attacker = this;
                rpt.target = target;
                attackListener.accept(rpt);
            }
        } catch (Exception e) {
            // ignore listener failures during gameplay
        }

        if (magAfter > 0) {
            System.out.println("Attack: " + attack.name + " Rolled physical(total after crits): "
                    + totalPhysBeforeResist + ", magic: " + mag + ", After: "
                    + physAfter + ", " + magAfter);
            target.alterHp(-physAfter);
            target.alterHp(-magAfter);
        } else {
            System.out.println("Attack: " + attack.name + " Rolled physical(total after crits): "
                    + totalPhysBeforeResist + ", After: " + physAfter);
            target.alterHp(-physAfter);
        }
    }

    private Resistances parseResistance(String s) {
        if (s == null)
            return null;
        try {
            return Resistances.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

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
                if (eq.isVersatile() && requestTwoHanded) {
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
            float eqCrit = eq.getCrit();
            float eqDodge = eq.getDodge();
            float eqBlock = eq.getBlock();
            if (eqCrit != 0f)
                equipmentCrit += eqCrit;
            if (eqDodge != 0f)
                equipmentDodge += eqDodge;
            if (eqBlock != 0f)
                equipmentBlock += eqBlock;
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
            float eqCrit = eq.getCrit();
            float eqDodge = eq.getDodge();
            float eqBlock = eq.getBlock();
            if (eqCrit != 0f)
                equipmentCrit -= eqCrit;
            if (eqDodge != 0f)
                equipmentDodge -= eqDodge;
            if (eqBlock != 0f)
                equipmentBlock -= eqBlock;
        } catch (Exception ignored) {
        }

        // Recompute derived stats now that stats/equipment changed
        recalcDerivedStats();
    }

    public void addProperty(Property property) {
        int id = property.getId();
        if (id >= 1000 && id < 2000) {
            buffs.put(id, property);
        } else if (id >= 2000 && id < 3000) {
            debuffs.put(id, property);
        } else if (id >= 3000 && id < 4000) {
            immunities.put(id, property);
        } else if (id >= 4000 && id < 5000) {
            traits.put(id, property);
        }
        // Apply property effects
        property.onApply(this);
    }

    public void removeProperty(int id) {
        Property property = null;
        if (id >= 1000 && id < 2000) {
            property = buffs.get(id);
            buffs.remove(id);
        } else if (id >= 2000 && id < 3000) {
            property = debuffs.get(id);
            debuffs.remove(id);
        } else if (id >= 3000 && id < 4000) {
            property = immunities.get(id);
            immunities.remove(id);
        } else if (id >= 4000 && id < 5000) {
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

        System.out.println("Immunities:");
        for (Property immunity : immunities.values()) {
            System.out.println(" - " + immunity);
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
        sb.append("Immunities:\n");
        for (Property immunity : immunities.values()) {
            sb.append(" - ").append(immunity).append("\n");
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

    public void alterHp() {
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
        if (this.baseMaxMana <= 0) {
            this.baseMaxMana = this.maxMana;
        }
        updateMaxMana();

        if (this.baseMaxStamina <= 0) {
            this.baseMaxStamina = this.maxStamina;
        }
        updateMaxStamina();

        this.currentMana = this.maxMana;
        this.currentStamina = this.maxStamina;

        updateCrit();

        int tempXp = this.getXp();
        int lvl = this.getLevel();
        for (int i = lvl; i > 0; i--) {
            tempXp += i * 10;
        }
        this.setLevel(0);
        this.setXp(0);
        this.addXp(tempXp);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Creature: ").append(name).append("\n");
        sb.append("Id: ").append(id).append("\n");
        sb.append("Vision Range: ").append(visionRange).append("\n");
        sb.append("Level: ").append(level).append("\n");
        sb.append("XP: ").append(xp).append("\n");
        sb.append("Base HP: ").append(baseHp).append("\n");
        sb.append("HP Level Bonus: ").append(hpLvlBonus).append("\n");
        sb.append("HP: ").append(currentHp).append("/").append(maxHp).append("\n");
        sb.append("Mana: ").append(currentMana).append("/").append(maxMana).append("\n");
        sb.append("Stamina: ").append(currentStamina).append("/").append(maxStamina).append("\n");
        sb.append("Size: ").append(size).append("\n");
        sb.append("Type: ").append(type).append("\n");
        sb.append("Creature Type: ").append(creatureType).append("\n");
        sb.append("Stats: ").append(stats).append("\n");
        sb.append("-----------------\n");
        sb.append("Resistances:\n");
        for (Entry<Resistances, Integer> entry : resistances.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("%\n");
        }
        sb.append("-----------------\n");
        // Show effective chances including equipment modifiers
        sb.append("Critical Hit Chance: ").append(getCrit()).append("%\n");
        sb.append("Dodge Chance: ").append(getDodge()).append("%\n");
        sb.append("Block Chance: ").append(getBlock()).append("%\n");
        sb.append("Equipment:\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item equipped = equipment.get(slot);
            sb.append(slot.name()).append(": ");
            if (equipped != null) {
                sb.append(equipped.getName());
            } else {
                sb.append("Empty");
            }
            sb.append("\n");
        }
        sb.append("-----------------\n");
        sb.append(printProperties());
        sb.append("-----------------\n");
        sb.append("Inventory:\n");
        sb.append("Weapons: ").append(listInventoryItems(inventory.getWeapons())).append("\n");
        sb.append("Offhands: ").append(listInventoryItems(inventory.getOffhands())).append("\n");
        sb.append("Helmets: ").append(listInventoryItems(inventory.getHelmets())).append("\n");
        sb.append("Armor: ").append(listInventoryItems(inventory.getArmors())).append("\n");
        sb.append("Legwear: ").append(listInventoryItems(inventory.getLegwear())).append("\n");
        sb.append("Consumables: ").append(listInventoryItems(inventory.getConsumables())).append("\n");
        sb.append("Misc: ").append(listInventoryItems(inventory.getMisc())).append("\n");
        return sb.toString();
    }

    private String listInventoryItems(java.util.List<Item> items) {
        if (items.isEmpty())
            return "Empty";
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append(item.getName()).append(", ");
        }
        if (sb.length() > 2)
            sb.setLength(sb.length() - 2); // Remove trailing comma
        return sb.toString();
    }
}
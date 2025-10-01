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
    private Resistances defaultDamageType = Resistances.BLUDGEONING;
    private Size size;
    private Type type;
    private CreatureType creatureType;
    private EnumMap<Stats, Integer> stats;
    private EnumMap<Resistances, Integer> resistances;
    private float baseCrit;
    private float baseDodge;
    private float baseBlock;
    private float crit; // base-derived critical hit chance percentage (0-100)
    private float dodge; // base-derived dodge chance percentage (0-80)
    private float block; // base-derived block chance percentage (0-80)
    // Equipment aggregates (sum of all equipped item contributions)
    private final java.util.EnumMap<Stats, Integer> equipmentStats = new java.util.EnumMap<>(Stats.class);
    private final java.util.EnumMap<Resistances, Integer> equipmentResists = new java.util.EnumMap<>(Resistances.class);
    private float equipmentCrit = 0f;
    private float equipmentDodge = 0f;
    private float equipmentBlock = 0f;
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

    public Inventory getInventory() {
        return inventory;
    }

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

    // Dice rolling moved to com.bapppis.core.util.Dice.roll(String)

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

    public Resistances getDefaultDamageType() {
        return defaultDamageType;
    }

    public void setDefaultDamageType(Resistances defaultDamageType) {
        this.defaultDamageType = defaultDamageType;
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
        if (stat == Stats.CONSTITUTION) {
            alterHp();
        }
    }

    public void modifyStat(Stats stat, int amount) {
        stats.put(stat, getStat(stat) + amount);
        if (stat == Stats.CONSTITUTION) {
            alterHp();
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
        float dodgeDelta = 2.5f * (this.getStat(Stats.DEXTERITY) - 10);
        this.dodge = this.baseDodge + dodgeDelta;

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
                int statBonus = determineStatBonusForWeapon(weapon);
                applyAttackToTarget(chosen, statBonus, target, weapon.getDamageType(), weapon.getMagicElement());
                return;
            }
        }

        // No weapon attacks — use creature's own attacks if present
        if (this.attacks != null && !this.attacks.isEmpty()) {
            chosen = chooseAttackFromList(this.attacks);
            // For creature attacks, default to strength for physical damage
            int statBonus = Math.max(1, this.getStat(Stats.STRENGTH) - 10);
            Resistances physType = parseResistance(chosen == null ? null : chosen.damageType);
            Resistances magType = parseResistance(chosen == null ? null : chosen.magicDamageType);
            applyAttackToTarget(chosen, statBonus, target, physType, magType);
            return;
        }

        // Fallback: old unarmed behavior
        if (this.defaultDamageType == Resistances.BLUDGEONING || this.defaultDamageType == Resistances.PIERCING) {
            int strMod = Math.max(1, this.getStat(Stats.STRENGTH) - 10);
            int damage = Dice.roll("1d" + strMod);
            System.out.println("Unarmed attack: Rolled damage: " + damage);
            target.alterHp(-damage);
        } else if (this.defaultDamageType == Resistances.SLASHING) {
            int dexMod = Math.max(1, this.getStat(Stats.DEXTERITY) - 10);
            int damage = Dice.roll("1d" + dexMod);
            System.out.println("Unarmed attack: Rolled damage: " + damage);
            target.alterHp(-damage);
        } else {
            // Fallback logic
        }
    }

    private int determineStatBonusForWeapon(Equipment weapon) {
        if (weapon.isFinesse()) {
            return Math.max(1, Math.max(this.getStat(Stats.STRENGTH) - 10, this.getStat(Stats.DEXTERITY) - 10));
        }
        switch (weapon.getWeaponClass()) {
            case MELEE:
                return Math.max(1, this.getStat(Stats.STRENGTH) - 10);
            case RANGED:
                return Math.max(1, this.getStat(Stats.DEXTERITY) - 10);
            case MAGIC:
                return Math.max(1, this.getStat(Stats.INTELLIGENCE) - 10);
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
            // First roll to-hit against target dodge (0.0-100.0). If roll <= dodge, the hit
            // misses.
            float toHitRoll = ThreadLocalRandom.current().nextFloat() * 100f;
            // Effective dodge is clamped to 0-80 when checking to-hit
            float effectiveDodge = Math.max(0f, Math.min(80f, target.getDodge()));
            if (toHitRoll <= effectiveDodge) {
                // Missed due to dodge
                System.out.println("Missed (dodge): " + this.getName() + " -> " + target.getName() + " | attack='"
                        + attack.name + "' roll=" + String.format("%.2f", toHitRoll) + " dodge="
                        + String.format("%.2f", effectiveDodge));
                continue;
            }
            // Next roll against block. If roll <= block, the hit is blocked and does no
            // damage.
            float blockRoll = ThreadLocalRandom.current().nextFloat() * 100f;
            // Effective block is clamped to 0-80 when checking block
            float effectiveBlock = Math.max(0f, Math.min(80f, target.getBlock()));
            if (blockRoll <= effectiveBlock) {
                // Hit was blocked
                System.out.println("Missed (block): " + this.getName() + " -> " + target.getName() + " | attack='"
                        + attack.name + "' roll=" + String.format("%.2f", blockRoll) + " block="
                        + String.format("%.2f", effectiveBlock));
                continue;
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
                System.out.println("Critical hit! " + attack.name + " single hit doubled to: " + hit);
            }
            totalPhysBeforeResist += hit;
        }

        int physAfter = Math.floorDiv(
                totalPhysBeforeResist
                        * target.getResistance(physicalType == null ? this.defaultDamageType : physicalType),
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
                rpt.damageType = (physicalType == null ? this.defaultDamageType.name() : physicalType.name());
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

    public Item getEquipped(EquipmentSlot slot) {
        return equipment.get(slot);
    }

    public void equipItem(Item item) {
        EquipmentSlot slot = item.getSlot();
        // Remove any existing item in the slot first
        Item oldItem = null;
        if (item.isTwoHanded()) {
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
            if (eqCrit != 0f) equipmentCrit += eqCrit;
            if (eqDodge != 0f) equipmentDodge += eqDodge;
            if (eqBlock != 0f) equipmentBlock += eqBlock;
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
            if (eqCrit != 0f) equipmentCrit -= eqCrit;
            if (eqDodge != 0f) equipmentDodge -= eqDodge;
            if (eqBlock != 0f) equipmentBlock -= eqBlock;
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
        int delta = this.getStat(Stats.CONSTITUTION) - 10;
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
        int newMaxHp;
        if ((this.getStat(Stats.CONSTITUTION) >= 10)) {
            newMaxHp = this.baseHp + ((this.level + 1) * (this.hpLvlBonus + (this.getStat(Stats.CONSTITUTION) - 10)));
        } else {
            newMaxHp = this.baseHp + ((this.level + 1) * (this.hpLvlBonus - (10 - this.getStat(Stats.CONSTITUTION))));
        }
        this.maxHp = Math.max(1, newMaxHp);
        this.currentHp = Math.max(1, (int) (this.maxHp * ratio));
    }

    /**
     * Finalize creature fields after loading from JSON.
     * This includes resetting max HP to base, updating derived HP via
     * updateMaxHp(),
     * and converting stored levels into XP so that level-up bonuses are applied
     * by invoking addXp(tempXp).
     *
     * Call this once after Gson has populated fields and after properties/items
     * (starting equipment) have been applied.
     */
    public void finalizeAfterLoad() {
        int baseHp = this.getBaseHp();
        this.setMaxHp(baseHp); // Reset max HP to base before applying properties
        // Recompute max/current HP correctly
        this.updateMaxHp();

        // Convert existing levels into XP and add them so addXp() handles level-up
        // logic
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
        sb.append("Max HP: ").append(maxHp).append("\n");
        sb.append("Current HP: ").append(currentHp).append("\n");
        sb.append("HP Level Bonus: ").append(hpLvlBonus).append("\n");
        sb.append("Default Damage Type: ").append(defaultDamageType).append("\n");
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
        sb.append("Critical Hit Chance: ").append(crit).append("%\n");
        sb.append("Dodge Chance: ").append(dodge).append("%\n");
        sb.append("Block Chance: ").append(block).append("%\n");
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
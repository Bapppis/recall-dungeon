# Recall Dungeon System Reference

This document provides a technical overview of how core systems work in Recall Dungeon, including creatures, items, properties, combat mechanics, and utility classes. Use this as a reference for stat interactions, asset conventions, and engine logic.

---

## Table of Contents
1. [Combat System](#combat-system)
2. [Creature System](#creature-system)
3. [Item System](#item-system)
4. [Property System](#property-system)
5. [Utility Classes](#utility-classes)
6. [Engine Logic Overview](#engine-logic-overview)

---

## Combat System

### Overview
Combat in Recall Dungeon is turn-based and uses a sophisticated hit/miss/crit system with physical and magical damage components. Each attack can have multiple hits, and each hit is resolved independently.

### Attack Resolution Flow
1. **Attack Selection**: Creature chooses an attack from its `attacks` list (weighted random selection)
2. **Per-Hit Resolution**: For each hit in `attack.times`:
   - Roll to-hit (0-100)
   - Check against target's avoidance (dodge + block/magicResist)
   - On success: roll damage dice, check for crit, apply buildup
3. **Damage Application**: Sum all successful hits and apply resistance reduction
4. **HP Modification**: Reduce target's HP by final damage amount

### To-Hit Mechanics

#### Physical Attacks
- **To-Hit Roll**: `roll(0-100) + statBonus + attacker.accuracy + attack.accuracy`
- **Avoidance Window**: `min(100, target.dodge + target.block)`
  - Dodge and block are partitioned to avoid overlap
  - If `toHit <= dodge`, the attack is dodged
  - If `toHit > dodge` but `toHit <= (dodge + block)`, the attack is blocked
- **TRUE Damage**: Ignores block, only checks dodge
- **Success**: If `toHit > avoidanceWindow`, the hit lands

#### Magical Attacks
- **To-Hit Roll**: `roll(0-100) + (magicStatBonus × 5) + attacker.magicAccuracy + attack.magicAccuracy`
- **Avoidance Window**: `min(100, target.dodge + target.magicResist)`
  - Magic resist replaces block for magical attacks
  - Partitioned the same way (dodge first, then magic resist)
- **Success**: If `toHit > avoidanceWindow`, the hit lands

### Damage Calculation

#### Rolling Damage
- **Physical Damage**: `Dice.roll(attack.physicalDamageDice) + max(0, statBonus)` per hit
  - Example: `"2d6"` rolls two six-sided dice
  - StatBonus added to each individual hit
- **Magical Damage**: `Dice.roll(attack.magicDamageDice) + max(0, magicToHitBonus)` per hit
  - Example: `"1d8"` rolls one eight-sided die
  - Magic to-hit bonus derived from creature's magic stat (INT/WIS/CHA)

#### Damage Bonuses
- **Physical Stat Bonus**:
  - Determined by weapon's stat requirements (STR for most melee, DEX for finesse/ranged)
  - `weaponStatBonus = creature.getStat(weapon.primaryStat)`
  - Added to each individual hit before crit
- **Physical Multiplier** (from Attack):
  - `physStatExtra = floor(physStatBase × 5.0 × attack.damageMultiplier)`
  - Applied after all hits are summed
- **Magic Stat Bonus**:
  - Weapon can specify `magicStatBonuses` (e.g., INT, WIS, CHA)
  - System picks the highest stat from the list
  - `magicStatBonus × 5` added to to-hit roll
  - `magicStatExtra = floor(magicStatBonus × 5.0 × attack.magicDamageMultiplier)`
  - Applied after all hits are summed

#### Resistance Reduction
- **Formula**: `finalDamage = floor(damageBeforeResist × (targetResistance / 100))`
- **Resistance Values**:
  - 100 = no reduction (normal damage)
  - 50 = half damage
  - 150 = 1.5× damage (weakness)
  - 0 = immunity
- **Separate Calculations**: Physical and magical damage use different resistance types
  - Physical: BLUDGEONING, PIERCING, SLASHING, TRUE
  - Magical: FIRE, WATER, WIND, ICE, NATURE, LIGHTNING, LIGHT, DARKNESS

### Critical Hits

#### Crit Chance Calculation
- **Base Formula**: `crit = baseCrit + 5 × (LUCK − 1)`
- **Equipment Bonus**: `crit += equipmentCrit`
- **Property Bonus**: `crit += propertyCrit`
- **Attack Modifier**: `effectiveCrit = crit + attack.critMod`
- **Clamping**: Crit chance is clamped to [0, 100] when used

#### Crit Resolution
- **Per-Hit Basis**: Each successful hit rolls independently for crit
- **Roll**: `random(0.0, 1.0) < (effectiveCrit / 100.0)`
- **Effect**: If crit succeeds, damage for that hit is **doubled**
- **Separate Tracking**: Physical and magical hits track crit counts independently

### Multi-Hit Attacks

#### Times Property
- **Definition**: `attack.times` specifies how many individual hits to attempt
- **Independent Resolution**: Each hit:
  - Rolls to-hit separately (can miss/dodge/block individually)
  - Rolls damage separately
  - Rolls crit separately
- **Damage Accumulation**: All successful hits are summed before resistance reduction

#### Miss Tracking
- **Physical Misses**:
  - `physMissDodge`: hits that failed due to dodge
  - `physMissBlock`: hits that failed due to block
- **Magical Misses**:
  - `magicMissDodge`: hits that failed due to dodge
  - `magicMissResist`: hits that failed due to magic resist

### Buildup System

#### What is Buildup?
Buildup represents the accumulation of elemental or physical stress on a creature. When buildup reaches 100%, it triggers a **resistance overload** that applies a debuff and resets the buildup to 0.

#### Buildup Types (ResBuildUp enum)
- **Magical**: FIRE, WATER, WIND, ICE, NATURE, LIGHTNING, LIGHT, DARKNESS
- **Physical**: BLUDGEONING, PIERCING, SLASHING
- **Special**: TRUE

#### Adding Buildup (Per-Hit)
- **Trigger**: Every successful hit (physical or magical) adds buildup
- **Base Amount**: `BASE_BUILD_UP = 20`
- **Resistance Factor**: `resFactor = max(0, targetResistance) / 100.0`
- **Attack Modifier**: Attacks specify `PhysBuildUpMod` and `MagicBuildUpMod` (default 1.0)
- **Formula**: `buildUp = floor(BASE_BUILD_UP × attackMod × resFactor)`
- **Example**:
  - Target has 100 FIRE resistance, attack has 1.0 fire buildup mod
  - BuildUp added: `floor(20 × 1.0 × 1.0) = 20`
- **Application**: `ResistanceUtil.addBuildUp(target, damageType, buildUpMod)`

#### Buildup Accumulation
- **Range**: 0-100 (percentage)
- **Immunity**: Buildup can be set to -1 for complete immunity (no accumulation possible)
- **Fresh Flag**: Newly added buildup is marked "fresh" to skip the next decay tick
- **Clamping**: Buildup is clamped to [0, 100] on modification

#### Resistance Overload (ResOverload)
- **Trigger**: When any buildup reaches ≥100%
- **Effect**:
  1. Apply resistance-specific debuff property (e.g., SLASHING → Bleed1, ID 2334)
  2. Reset that buildup to 0
- **Auto-Check**: `Creature.modifyResBuildUp()` automatically calls `ResistanceUtil.checkResOverload()` after every modification
- **Placeholder Mapping**: Resistances without configured properties return -1 (reset buildup but don't apply property)
- **Current Mappings**:
  - SLASHING → Bleed1 (Property ID 2334)
  - All others → -1 (placeholder, to be implemented)

#### Buildup Decay
- **Purpose**: Buildup gradually decreases over time if not refreshed
- **Formula**: `decay = (200 - targetResistance) / 10` per tick
- **Example**:
  - 100 resistance: `(200 - 100) / 10 = 10` decay per tick
  - 50 resistance: `(200 - 50) / 10 = 15` decay per tick
- **Fresh Protection**: Buildup marked "fresh" skips one decay tick
- **Method**: `ResistanceUtil.decayResBuildUps(creature)`

### Dual-Element Weapons
- **Physical Component**: Uses physical damage dice, physical resistance, physical buildup
- **Magical Component**: Uses magic damage dice, magic resistance, magic buildup
- **Independent Resolution**: Both components roll separately with their own to-hit, damage, crit, and buildup
- **Magic Element Priority**: For magic buildup, prefers `weapon.magicElement` over `attack.magicDamageType`

### Secondary Damage System
Weapons can have secondary damage types that apply automatically when primary hits land:

#### Secondary Physical Damage
- **Fields**: `weapon.damageType2` + `attack.physicalDamageDice2`
- **Trigger**: Applies for each successful primary physical hit
- **To-Hit**: Uses primary hit success (no separate roll)
- **Damage**: Rolls `physicalDamageDice2` only (NO stat bonus added)
- **Crit**: Crits when the corresponding primary hit crits
- **Buildup**: Does NOT apply buildup
- **Resistance**: Reduced by target's `damageType2` resistance

#### Secondary Magic Damage
- **Fields**: `weapon.magicElement2` + `attack.magicDamageDice2`
- **Trigger**: Applies for each successful primary magic hit
- **To-Hit**: Uses primary magic hit success (no separate roll)
- **Damage**: Rolls `magicDamageDice2` only (NO stat bonus added)
- **Crit**: Crits when the corresponding primary magic hit crits
- **Buildup**: Does NOT apply buildup
- **Resistance**: Reduced by target's `magicElement2` resistance

#### Example: Morningstar
```json
{
  "damageType": "PIERCING",
  "damageType2": "BLUDGEONING",
  "attacks": [{
    "physicalDamageDice": "1d6",
    "physicalDamageDice2": "1d6"
  }]
}
```
- Primary hit: 1d6 PIERCING + STR bonus + buildup
- Secondary hit: 1d6 BLUDGEONING (no bonus, no buildup)
- Both use same crit result

### Attack Reports
- **AttackEngine.attackListener**: Optional consumer that receives detailed `AttackReport` objects
- **Report Contents**:
  - Raw damage rolls (before crit, before resistance)
  - Damage after crit (before resistance)
  - Final damage (after resistance)
  - Crit counts (physical and magical separately)
  - Miss counts by type (dodge, block, magic resist)
  - Stat bonuses and multipliers
  - Dual-roll flag (whether attack had both physical and magical components)
- **Use Cases**: Unit tests, combat logs, damage analysis

---

## Creature System

### Stat Interactions & Calculations
- **HP (maxHp, currentHp):**
  - `maxHp` is set directly or recalculated from base + level/CON.
  - `currentHp` is clamped to [0, maxHp].
- **Mana (maxMana, currentMana):**
  - `maxMana` is set directly or recalculated from INT (floored factor, min 25).
  - `currentMana` is clamped to [0, maxMana].
- **Stamina (maxStamina, currentStamina):**
  - `maxStamina` is set directly or recalculated from CON (floored factor, min 25).
  - `currentStamina` is clamped to [0, maxStamina].
- **Crit:**
  - `crit = baseCrit + 5 × (LUCK − 1)`
- **Block:**
  - `block = baseBlock`
- **Dodge:**
  - `dodge = baseDodge + 2.5 × (DEXTERITY − 10)`
- **Magic Resist:**
  - `magicResist = baseMagicResist + 5 × (WISDOM − 10) + 2.5 × (CONSTITUTION − 10)`
- **Stamina Regen:**
  - `staminaRegen = baseStaminaRegen + max(1, floor(2.5 × WISDOM))`
- **Stat Bonuses:**
  - All stat changes (via `setStat`/`modifyStat`) automatically update derived values.
- **Equipment & Properties:**
  - Equipment and properties can add to crit, dodge, block, magicResist, accuracy, and regen via accumulators.
- **Clamping:**
  - Crit: 0–100
  - Dodge/Block: 0–80

### Creature Asset Conventions
- Creature JSONs require: `id`, `name`, `maxHp`, `currentHp`, `size`, `creatureType`, `stats`, `baseCrit`, `baseDodge`, `baseBlock`, `description`, `properties`.
- Creature IDs are grouped by type and species (see `creatures/README.md`).
- Properties are applied by ID via `PropertyManager`.

---

## Item System
- Items are defined in JSON with unique IDs, type, stats, and effects.
- Equipment slots: `helmet`, `armor`, `legwear`, `weapon`, `offhand`.
- Items can modify stats, resistances, and combat chances when equipped.
- Item types and rarities are defined in enums (`ItemType`, `Rarity`).
- Weapons have subtypes (melee, ranged, magic) and can define custom attacks.

---

## Property System
- Properties include buffs, debuffs, traits, and immunities.
- Each property has a unique ID and type (`PropertyType`).
- Properties can modify stats, resistances, regen, and combat chances.
- Properties are managed by `PropertyManager` and applied at load/finalization.

---

## Utility Classes
- **StatUtil:** Provides helpers for stat modification, clamping, and derived value calculation.
- **AttackUtil:** Handles attack logic, hit/miss, and damage calculation.
- **WeaponUtil:** Manages weapon-specific logic and stat effects.
- **ResistanceUtil:** Calculates resistance values and effects.
- **LevelUtil:** Manages XP, level-up, and stat scaling.
- **DebugLog:** Centralized logging for debugging and diagnostics.

---

## Engine Logic Overview
- All assets (creatures, items, properties) are loaded from JSON and indexed by ID.
- Stat changes trigger automatic recalculation of derived values (HP, mana, stamina, crit, dodge, etc.).
- Equipment and properties stack additively with base stats.
- Combat chances (crit, dodge, block) are derived from base values and stat bonuses.
- All calculations are unclamped until use-time (clamping occurs when values are used in combat).

---

## References
- See `creatures/README.md` for asset conventions and ID ranges.
- See `items/README.md` for item conventions.
- See `property/PropertyType.java` for property types.
- See `util/StatUtil.java` for stat math and helpers.

---

Feel free to expand this file with more details or code examples as needed.

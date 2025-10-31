# Recall Dungeon System Reference

This document provides a technical overview of how core systems work in Recall Dungeon, including creatures, items, properties, combat mechanics, and utility classes. Use this as a reference for stat interactions, asset conventions, and engine logic.

---

## Table of Contents

1. [Combat System](#combat-system)
2. [Spell System](#spell-system)
3. [Creature System](#creature-system)
4. [Item System](#item-system)
5. [Property System](#property-system)
6. [Utility Classes](#utility-classes)
7. [Engine Logic Overview](#engine-logic-overview)

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
  "attacks": [
    {
      "physicalDamageDice": "1d6",
      "physicalDamageDice2": "1d6"
    }
  ]
}
```

- Primary hit: 1d6 PIERCING + STR bonus + buildup
- Secondary hit: 1d6 BLUDGEONING (no bonus, no buildup)
- Both use same crit result

### Property-on-Hit System

Attacks can apply properties (buffs/debuffs) conditionally when they hit:

#### Physical Property-on-Hit

- **Field**: `attack.physicalOnHitProperty` (property name as string)
- **Trigger**: Single attempt per attack when at least one primary physical hit succeeds
- **To-Hit Roll**: `roll(0-100) + weaponStatBonus` (does NOT include accuracy or attack accuracy)
- **Avoidance**: Same dodge/block calculation as primary physical hits
- **Success**: Calls `target.addProperty(propertyName)` if to-hit succeeds
- **Tracking**: `AttackReport.physPropertyAttempted`, `AttackReport.physPropertyApplied`

#### Magical Property-on-Hit

- **Field**: `attack.magicOnHitProperty` (property name as string)
- **Trigger**: Single attempt per attack when at least one primary magic hit succeeds
- **To-Hit Roll**: `roll(0-100) + (magicStatBonus × 5)` (does NOT include magic accuracy or attack magic accuracy)
- **Avoidance**: Same dodge/magicResist calculation as primary magic hits
- **Success**: Calls `target.addProperty(propertyName)` if to-hit succeeds
- **Tracking**: `AttackReport.magPropertyAttempted`, `AttackReport.magPropertyApplied`

#### Important Notes

- Property application happens **once per attack**, not per hit (even for multi-hit attacks)
- Property to-hit uses **stat bonus only** (no accuracy bonuses)
- Property attempt only occurs if **at least one primary hit succeeded**
- Property can fail to apply even if primary hits landed (separate to-hit check)
- Properties referenced by name are loaded via `PropertyLoader` at runtime

#### Example: Morningstar with Dizzy

```json
{
  "name": "Critical Blow",
  "physicalDamageDice": "1d6+1",
  "physicalOnHitProperty": "Dizzy"
}
```

- Primary hit: 1d6+1 PIERCING + STR bonus + buildup
- If primary hit succeeds: attempt to apply "Dizzy" with one extra to-hit roll
- Dizzy property: -10% block, -4 DEX for 2 turns

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

## Spell System

### Overview

Spells are magical abilities that creatures can cast by expending mana. They work similarly to magic attacks, rolling against dodge and magic resist, and can deal elemental damage, apply properties, or provide buffs. Spells are defined in JSON files and loaded via `SpellLoader`.

### Spell Architecture

#### Core Components

- **Spell.java**: POJO containing all spell data fields
- **SpellLoader.java**: Loads spells from `data/spells/*.json` and provides lookup by ID or name
- **SpellEngine.java**: Handles all spell casting mechanics, damage calculation, and property application
- **SpellReference.java**: Maps spell names to weights in creature JSON for weighted attack pool

#### Integration with Creatures

- **Creature.spells**: List of `Spell` objects available to the creature
- **Creature.spellReferences**: List of `SpellReference` objects containing spell names and weights
- **Weighted Action Pool**: Spells are included in the creature's attack pool if mana is sufficient
- **Weight System**: Spell usage frequency determined by weight in creature JSON, not spell definition

### Spell Fields

#### Core Fields

- **id** (int): Unique identifier (50000-50999 range)
- **name** (String): Display name
- **description** (String): Lore description
- **manaCost** (int): Mana required to cast (checked before casting)
- **times** (Integer, default 1): Number of individual hits to attempt per cast

#### Damage Components (up to 4)

Spells can define up to 4 different damage types with independent dice:

- **damageType/2/3/4** (Resistances): Elemental damage type (FIRE, ICE, LIGHTNING, etc.)
- **damageDice/2/3/4** (String): Dice notation (e.g., "3d6", "2d4+2")
- **damageMult** (Float, default 1.0): Multiplier applied to all damage components
- **buildUpMod/2/3/4** (Float, default 1.0/times): Buildup modifier per damage type

#### Combat Modifiers

- **critMod** (String, e.g., "+10"): Added to caster's base crit chance
- **accuracy** (Integer, default 0): Added to to-hit roll
- **statBonuses** (List<Stats>): List of stats to choose best from (e.g., ["INTELLIGENCE", "WISDOM"])

#### Property Effects

- **onHitProperty** (String): Property name to apply if at least one damage component hits
- **buffProperty** (String): Property name to apply immediately to caster (buff-only spells)

#### UI Fields

- **tooltip** (String): Description shown in UI

### Spell Casting Mechanics

#### Mana System

- **Mana Check**: Before casting, verifies `caster.currentMana >= spell.manaCost`
- **Mana Deduction**: Immediately deducts mana cost on cast (regardless of hit/miss)
- **No Refund**: Missed spells do not refund mana
- **Enemy AI**: Enemies only include spells in attack pool if they have sufficient mana
- **Player Casting**: Players can manually select and cast spells if mana available

#### Stat Bonus Selection

- **Best Stat**: Spell uses highest stat value from `statBonuses` list
- **Calculation**: `statBonus = bestStat × 5`
- **Example**: Spell with `["INTELLIGENCE", "WISDOM"]` uses whichever stat is higher
- **Empty List**: If no stats specified, statBonus = 0

#### To-Hit Resolution (Per Hit)

Spells roll to-hit similar to magic attacks:

- **Roll**: `random(0-100) + statBonus + spell.accuracy`
- **Dodge Check**: `random(0-100) < target.dodge` → miss
- **Magic Resist Check**: `random(0-100) < target.magicResist` → miss (if not dodged)
- **Success**: Hit lands if both checks pass

#### Damage Calculation (Per Hit)

1. **Base Damage**: Roll dice (e.g., `Dice.roll("3d6")`)
2. **Add Stat Bonus**: `baseDamage + statBonus`
3. **Apply Multiplier**: `(baseDamage + statBonus) × damageMult`
4. **Check Crit**: Roll vs. `caster.crit + spell.critMod` → damage ×2 if crit
5. **Apply Resistance**: `damage × (targetResistance / 100)`
6. **Apply to Target**: `target.modifyHp(-finalDamage)`

#### Multi-Element Casting

- **Sequential Resolution**: Each damage component (1-4) resolves independently
- **Per-Component Rolls**: Each element makes `times` number of attacks
- **Example**: `Elemental Chaos` with 3 elements and `times=1`:
  - Cast 1: FIRE hit, ICE hit, LIGHTNING hit (3 separate rolls)
  - Cast 2: FIRE hit, ICE hit, LIGHTNING hit (3 more separate rolls)
  - Each roll has independent to-hit, damage, and crit
- **Buildup Per Element**: Each damage type applies its own buildup with its own modifier

### Buildup System

#### Buildup Application

- **Per-Hit Basis**: Each successful hit adds buildup to target
- **Formula**: `buildUp = BASE_BUILD_UP × buildUpMod × (targetResistance / 100)`
- **BASE_BUILD_UP**: 20 (constant)
- **Resistance Factor**: Higher resistance = more buildup (same as attacks)
- **Example**:
  - Target has 100 FIRE resistance, spell has 1.0 fire buildup mod
  - BuildUp added: `floor(20 × 1.0 × 1.0) = 20` per hit

#### Independent Tracking

- Each element (FIRE, ICE, LIGHTNING, etc.) has separate buildup accumulation
- `ResistanceUtil.addBuildUp(target, damageType, buildUpMod)` called per hit
- Overload triggers independently for each element type

### Property Application

#### On-Hit Properties

- **Trigger**: Single attempt per spell cast if **at least one damage hit succeeded**
- **To-Hit Roll**: `random(0-100) + statBonus` (no accuracy bonus)
- **Avoidance**: Rolls separately against dodge and magic resist
- **Success**: Calls `target.addProperty(spell.onHitProperty)`
- **Example**: Fireball's `"onHitProperty": "Burning1"` attempts to apply burn debuff if spell hit

#### Buff Properties

- **Trigger**: Immediate on cast (no roll required)
- **Target**: Always applies to caster, not target
- **Buff-Only Spells**: Spells with `buffProperty` but no damage dice
- **Example**: Shield of Light applies `"BleedImmunity"` to caster instantly
- **No Mana Refund**: Even buff-only spells deduct mana

### Spell Types

#### Offensive Spells

- Have at least one damage component (`damageDice/2/3/4`)
- Require target creature
- Roll to-hit against target's defenses
- Can apply `onHitProperty` debuff
- **Example**: Fireball (3d6 FIRE damage + Burning1)

#### Multi-Element Spells

- Define 2-4 damage components with different elements
- Each element resolves independently (separate rolls)
- Each element applies its own buildup
- Mana cost typically higher due to multiple effects
- **Example**: Elemental Chaos (2d4 FIRE + 2d4 ICE + 2d4 LIGHTNING)

#### Buff-Only Spells

- No damage dice defined
- Have `buffProperty` set
- Apply immediately to caster without rolling
- Do not require target parameter
- **Example**: Shield of Light (grants BleedImmunity)

### Creature Integration

#### Spell Lists

- **Creature.spells**: List of actual `Spell` objects the creature knows
- **Creature.spellReferences**: List of `SpellReference` objects (name + weight)
- **Loading**: `CreatureLoader.applySpellsFromJson()` parses creature JSON
- **Formats Supported**:
  - Simple: `"spells": ["Fireball"]` (weight defaults to 1)
  - Weighted: `"spells": [{"name": "Fireball", "weight": 3}]`

#### Weighted Attack Pool

- **Creature.attack()**: Builds weighted action pool from all attack sources
- **Pool Contents**: Weapon attacks + natural attacks + spells (if mana sufficient)
- **Mana Check**: Only adds spell to pool if `creature.currentMana >= spell.manaCost`
- **Weight Source**: Uses `spellReference.getWeight()`, not spell itself
- **Selection**: Random weighted selection from pool
- **Execution**: Calls `SpellEngine.castSpell(this, spell, target)` if spell selected

#### Enemy AI Behavior

- Automatically includes spells in attack pool if mana available
- Treats spell as another attack option (weighted with weapon/natural attacks)
- No special spell-only turns or casting restrictions
- Mana regeneration follows creature's mana regen stat

#### Player Behavior

- Can select spells manually from spell list (future UI feature)
- Spells also available in weighted attack pool like enemies
- Same mana checking and casting mechanics as enemies

### Weight Architecture

#### Separation of Concerns

- **Spell Definition**: Defines mechanics (damage, elements, properties)
- **Creature Usage**: Defines frequency (weight in attack pool)
- **Rationale**: Same spell can have different weights for different creatures
- **Example**: Kobold might cast Fireball weight=3, Wizard might cast it weight=10

#### SpellReference Pattern

```java
public class SpellReference {
    private String name;  // Spell name to look up
    private Integer weight;  // Weight in attack pool

    public int getWeight() {
        return weight == null ? 1 : weight;
    }
}
```

#### Creature JSON Format

```json
{
  "name": "Kobold Warrior",
  "spells": [
    { "name": "Fireball", "weight": 3 },
    { "name": "Shield of Light", "weight": 1 }
  ]
}
```

### Spell Loading

#### SpellLoader

- **Load Time**: Called in `AllLoaders.loadAll()` after `PropertyLoader`
- **Source Path**: `data/spells/` directory in resources
- **File Format**: One JSON file per spell
- **Lookup Methods**:
  - `getSpellById(int id)`: Lookup by numeric ID
  - `getSpellByName(String name)`: Lookup by string name
- **Caching**: Loads once and caches (call `forceReload()` in tests if needed)

#### Example Spell JSON

```json
{
  "id": 50000,
  "name": "Fireball",
  "description": "A blazing sphere of fire that explodes on impact.",
  "times": 1,
  "manaCost": 20,
  "damageType": "FIRE",
  "damageDice": "3d6",
  "damageMult": 1.0,
  "critMod": "+10",
  "accuracy": 5,
  "onHitProperty": "Burning1",
  "statBonuses": ["INTELLIGENCE", "WISDOM"],
  "buildUpMod": 1.0,
  "tooltip": "Launches a fireball that deals 3d6 + INT/WIS fire damage."
}
```

### Testing Hooks

#### SpellEngine (Deterministic Testing)

- Currently uses `Random` internally (not mockable)
- Future: Consider adding `RandomProvider` interface for deterministic tests
- Current Testing: Use statistical validation across many casts

#### SpellLoader (Test Isolation)

- **Production**: Loads once and caches
- **Testing**: Call `SpellLoader.forceReload()` for fresh data per test
- **Test Data**: Can create temporary spell JSON files for isolated tests

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

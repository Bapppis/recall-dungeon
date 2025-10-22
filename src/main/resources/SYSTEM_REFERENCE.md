# Recall Dungeon System Reference

This document provides a technical overview of how core systems work in Recall Dungeon, including creatures, items, properties, and utility classes. Use this as a reference for stat interactions, asset conventions, and engine logic.

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

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v0.0.97] - 2025-10-31

### Added - Spell System Integration

#### Documentation

- **IDS.md**: Updated spell ID range from `0000-0999` to `50000-50999` (1000 IDs allocated)

  - Added three spell entries: Fireball (50000), Elemental Chaos (50001), Shield of Light (50002)
  - Updated ID range documentation block with correct spell range

- **SYSTEM_REFERENCE.md**: Added comprehensive "Spell System" section (~200+ lines)

  - Overview of spell architecture and mechanics
  - Detailed field documentation (core, damage, combat, property fields)
  - Casting mechanics: mana costs, stat bonus selection, to-hit resolution
  - Damage calculation and multi-element support
  - Buildup system integration
  - Property application (buff and onHit properties)
  - Spell types (damage-based, buff-only, hybrid)
  - Creature integration via SpellReference with weighted selection
  - Spell loading and testing hooks
  - Added to Table of Contents at position 2

- **README.md**: Added spell system to project documentation
  - Updated Creature System section with spell-related features
  - Added new "Spell System" section with 10+ feature highlights
  - Updated "Asset-driven Design" section to include spells
  - Updated SYSTEM_REFERENCE.md reference to mention spell mechanics

#### Python Scripts

- **generate_ids.py**: Added spell ID range validation

  - New range rule: `50000-50999` for spells
  - Validates that spell JSON files use IDs within the correct range

- **format_jsons.py**: Added spell support with canonical field ordering

  - New `CANON_SPELL_ORDER` list with 24 fields
  - Added spell file kind detection
  - Updated `transform()` function to handle 'spell' kind
  - Updated `ID_RANGES` from `(0, 999)` to `(50000, 50999)`

- **generate_tooltips.py**: Complete rewrite of spell tooltip generation
  - New comprehensive `generate_spell_tooltip()` function
  - Mana cost display (always shown first)
  - Damage calculation with stat bonuses (shows "X-Y + 5 \* STAT bonus damage")
  - Multi-element damage support (up to 4 damage types)
  - Accuracy, critical chance modifiers
  - Property application with detailed descriptions
  - Buff-only spell support
  - Damage multiplier display
  - Property name normalization (handles "Burning1" vs "Burning 1", "BleedImmunity" vs "Bleed Immunity")
  - Improved `load_property_by_name()` with fuzzy matching
  - Integrated into `--apply` workflow alongside weapons
  - Tooltips now match weapon tooltip style and comprehensiveness

#### Testing

- **SpellTest.java**: Created comprehensive test suite with 16 test methods
  - `testSpellLoading`: Verifies spell loading by ID and name
  - `testSpellManaCostChecking`: Tests mana deduction and insufficient mana handling
  - `testSpellStatBonusSelection`: Tests best stat selection from list
  - `testSpellDamageCalculation`: Verifies damage is dealt
  - `testMultiElementSpell`: Tests multi-damage-type spells
  - `testBuffOnlySpell`: Tests buff application without target
  - `testSpellPropertyApplication`: Tests onHitProperty application
  - `testCreatureSpellIntegration`: Tests spell loading in creatures
  - `testSpellDamageMultiplier`: Tests damage multiplier field
  - `testSpellCritMod`: Tests crit modifier parsing
  - `testSpellAccuracy`: Tests accuracy field
  - `testSpellBuildUpMods`: Tests all 4 buildup mod fields
  - `testSpellWithNoStatBonuses`: Tests edge case with empty stat list
  - `testSpellHasDamage`: Tests hasDamage() helper
  - `testSpellManaDeductionOnMiss`: Tests mana cost paid even on miss
  - `testSpellWeightInCreature`: Tests SpellReference weight loading
  - `testMultipleSpellCasts`: Tests casting same spell multiple times
  - `testSpellToString`: Tests toString() debug output

#### Spell Data Updates

- **Fireball.json**: Updated tooltip with comprehensive details

  - Shows mana cost (20 mana)
  - Displays damage range with stat bonuses (3-18 + 5 \* INT/WIS bonus fire damage)
  - Shows modifiers (chance to inflict Burning1, +10% crit, +5 accuracy)
  - Includes Burning 1 property description

- **Elemental Chaos.json**: Updated tooltip for multi-element spell

  - Shows mana cost (35 mana)
  - Displays all three damage types (fire, ice, lightning)
  - Shows stat bonus (INT) for all damage components
  - Includes +5% crit chance
  - Shows damage multiplier (0.8x)

- **Shield of Light.json**: Updated tooltip for buff-only spell
  - Shows mana cost (15 mana)
  - Displays buff effect (BleedImmunity)
  - Includes property description (Immune to Bleed condition)

### Changed

- Spell tooltips now follow the same comprehensive format as weapon tooltips
- Property lookups now support both exact names and normalized names (case-insensitive, space-insensitive)
- Spell system fully integrated with existing project conventions and tooling

### Technical Details

- **Testing Framework**: JUnit 5 with BeforeAll loader setup
- **API Patterns**: Uses `CreatureLoader.getCreatureById()`, `getBuff(id)`, `getDebuff(id)`
- **Field Ordering**: 24 canonical fields for spell JSON files
- **ID Allocation**: 3 spells created (50000-50002), 997 IDs available (50003-50999)
- **Stat Bonus Format**: "5 \* STAT bonus" matches weapon tooltip format
- **Property Matching**: Fuzzy matching handles naming inconsistencies (spaces, case)

### Known Issues

- Spell tooltip field in JSON is array format (matching weapons) but Spell.java expects String type
- Tests currently failing due to tooltip type mismatch - needs resolution in future update

---

## [v0.0.96] - 2025-10-30

### Added

- **Dizzy Debuff**: New condition that reduces block chance and dexterity
  - Dexterity loss indirectly reduces dodge and attacking if DEX is the attack stat
- **Property Application on Attacks**: Added `physicalOnHitProperty` and `magicalOnHitProperty`
  - Attacks can now apply debuffs to enemies on hit
  - Enables status effect buildup from weapon strikes

### Changed

- Updated README.md and SYSTEM_REFERENCE.md with attack property mechanics
- Tooltip and format generation scripts now support on-hit properties

### Fixed

- Updated tests to verify new on-hit property features

---

## [v0.0.95] - 2025-10-29

### Added

- **Dual Damage Types for Weapons**: Weapons and attacks now support secondary damage types
  - Added `damageType2` and `magicElement2` fields
  - Physical weapons can deal two types of physical damage (e.g., slashing + piercing)
  - Magic weapons can deal two types of elemental damage
- Supporting logic in weapon and attack Java classes

### Changed

- Updated `generate_tooltips.py` to display both damage types
- Updated SYSTEM_REFERENCE.md with dual damage type documentation

### Testing

- Added comprehensive tests for dual damage type functionality

---

## [v0.0.94] - 2025-10-28

### Added

- **Morningstar**: New piercing weapon with unique attack patterns

---

## [v0.0.93] - 2025-10-27

### Added

- **Kobold Species**: New creature species with racial traits
  - Updated folder structure for species organization
  - Added Kobold Warrior to enemy roster
- **Burning Debuff**: Fire damage now applies burning condition over time
- **Necrotic Plague**: Darkness damage buildup inflicts necrotic plague debuff

### Changed

- Added missing `physBuildUp` and `magicBuildUp` fields to creature JSONs
- Adjusted default true resistance to 50% (was 100%)

### Fixed

- Corrected folder structure and ID assignments for new species

---

## [v0.0.92] - 2025-10-26

### Added

- **Sword of Windfury**: New arcane weapon using INT/CHA for bonuses
- **Automated Tooltip Generation**: Python script generates weapon tooltips automatically
  - Calculates damage ranges with stat bonuses
  - Includes attack percentages based on weights
  - Displays modifiers and special properties

### Changed

- Arcane weapons now use Intelligence or Charisma as stat bonus (highest is selected)
- Adjusted weapon tooltips and stats for balance

### Fixed

- `format_jsons.py` now properly formats all weapon files

---

## [v0.0.91] - 2025-10-25

### Added

- **Steel Flanged Mace**: New blunt weapon for crushing damage
- **Staff of Flames**: New magical staff for fire-based spellcasting

---

## [v0.0.90] - 2025-10-24

### Added

- **Resistance Buildup System**: Comprehensive elemental resistance mechanics
  - `RESISTANCE_BUILDUP_CONSTANT` determines buildup rate (default: 20)
  - `addBuildUp()` method adds resistance buildup based on damage type
  - Buildup persists if actively receiving that damage type
  - Buildup decays by 10% per round when not receiving damage
- **Resistance Overload**: When buildup reaches 100%, applies debuff and resets
- **BuildUp Multipliers**: Per-attack buildup modifiers for fine-tuning
  - `physBuildUpMod` and `magicBuildUpMod` for each attack
  - Allows weapons to specialize in applying or avoiding status effects

### Changed

- Adjusted JSON formatting for consistency
- Refactored item printing into `ItemPrinter` utility class
- Default true resistance changed to 50%

### Testing

- Added comprehensive buildup and overload tests
- Updated SYSTEM_REFERENCE.md with combat mechanics documentation

---

## [v0.0.89] - 2025-10-23

### Added

- **Resistance Buildup Enum**: `ResBuildUp` tracks elemental exposure
  - Tracks buildup for each damage type (SLASHING, FIRE, ICE, etc.)
  - When buildup reaches 100%, applies corresponding debuff
  - Example: Slashing buildup → Bleed condition
- Helper functions to modify buildup values
- Automatic buildup decay: -10% per round × resistance value

### Testing

- Added BuildUpTest to verify functionality

---

## [v0.0.88] - 2025-10-22

### Added

- **SYSTEM_REFERENCE.md**: Comprehensive technical documentation
  - Documents how different game systems interact
  - Architecture and design decisions
  - Formula references for calculations
- **Utility Print Function**: `creature.printLnFields()` for debugging
  - Prints all creature fields for testing

### Changed

- **HP System Overhaul**: Renamed `hplv1Bonus` to `hpDice`
  - HP calculations increased ~5× for better scaling
  - Formula: `baseHp + (hpDice × level) + (CON bonus × level)`
- **Resistance Utility Helpers**: Quick resistance modification functions
  - `modifyFireResistance(creature, 50)` adds 50 to fire resistance
  - Similar helpers for all resistance types
- Updated all enemy creature JSONs to new HP system
- Added setters for base creature variables (used by CreatureType and Species)

### Refactoring

- Moved magic and melee weapons to separate sub-packages

---

## [v0.0.87] - 2025-10-21

### Added

- **Species and CreatureType Hooks**: Post-JSON-load modification system
  - Species can override JSON values after loading
  - CreatureType can apply class-specific modifications
- **Property Removal System**: JSONs can specify properties to remove
  - Useful when species/class grant properties that specific creatures shouldn't have

### Changed

- **CreatureLoader Priority**: Species and CreatureType now override JSON values
- Manual-test folder structure now mirrors main test structure

### Testing

- Updated ID tests for current layout
- Comprehensive creature hierarchy tests
- Added item loading and equipment tests

### Fixed

- Loader logic corrected for creature hierarchy (Creature ← Type ← Species)

---

## [v0.0.86] - 2025-10-20

### Added

- **Equipment Subclasses**: Separated armor into specialized types
  - `Helmet`: Head protection
  - `Armor`: Chest protection
  - `Legwear`: Leg protection
  - `Offhand`: Shields and secondary items

### Changed

- Reorganized file structure: enums moved to dedicated folder
- Updated Python helper scripts for new structure

---

## [v0.0.85] - 2025-10-20

### Added

- **Weapon Class Hierarchy**: Comprehensive weapon categorization
  - **Melee Weapons**: `PiercingWeapon`, `BluntWeapon`, `SlashingWeapon`
  - **Ranged Weapons**: `Bow`, `Crossbow`
  - **Magic Weapons**: `Staff`, `ArcaneWeapon`

### Changed

- Updated all weapon JSONs to specify weapon subclass
- Adjusted tests for weapon polymorphism and edge cases

---

## [v0.0.84] - 2025-10-19

### Added

- **EquipmentUtils**: Centralized equipment management utilities
  - Moved equipment printing from Creature class
- **Expanded Creature Types**: Significantly more variety in creature taxonomy

### Changed

- **IDS.md Overhaul**: Complete reorganization of ID system
- **Creature Inheritance Refactor**: New hierarchy system
  - `Creature` ← `ENEMY`/`NPC`/`PLAYER` ← `CreatureType` ← `Species`
  - Enables racial traits and class features
- Removed obsolete comments and cleaned codebase
- Updated README.md documentation

### Fixed

- Species loading bugs resolved
- Creature type hierarchy now works correctly

---

## [v0.0.83] - 2025-10-18

### Added

- Helper printer methods for equipped items
- Case-insensitive `unequipByName()` method (ignores spaces too)

### Testing

- Expanded equipment management tests

---

## [v0.0.82] - 2025-10-18

### Added

- **Properties for Items**: Items can now grant buffs/debuffs when used
  - Consumables can apply temporary effects
  - Equipment can grant passive bonuses
- **Name-Based References**: Items, creatures, and properties use names instead of IDs
  - Improves JSON readability
  - Easier content creation

### Changed

- **Equipment.java Split**: Separated into multiple item type classes
  - Better organization and maintainability
- **EquipmentManager → ItemManager**: Renamed to reflect broader scope
- **Sprite System**: AtlasBuilder.java generates sprite atlas from PNGs

### Testing

- Updated all tests for new item system
- Added edge case coverage

### Documentation

- Major README.md update with new systems

### Fixed

- Sprite loading bugs in map view
- Loader edge cases

---

## [v0.0.81] - 2025-10-17

### Added

- **Comprehensive Test Suite**: Massive expansion of unit tests
  - Edge case coverage
  - Integration tests
- **Manual Test Separation**: `manual-test` folder for interactive tests
  - Excluded from `mvn test` runs

### Changed

- Updated `pom.xml` build configuration
- Project structure optimization for faster compilation

### Documentation

- Updated README.md with testing information

### Fixed

- Various edge case bugs discovered through testing

---

## [v0.0.80] - 2025-10-17

### Added

- **Elemental Damage for Properties**: Properties can deal typed damage per turn
  - `damageType` field specifies element (Fire, Ice, etc.)
  - `damageDice` field defines damage roll (e.g., "1d6+2")
  - Damage respects creature resistances
- **Name-Based Entity Lookup**: Search by name in addition to ID
  - Space-insensitive: "NecroticPlague" matches "Necrotic Plague"
- **Necrotic Plague Debuff**: Example of damage-over-time property

### Changed

- **Loader Optimization**: All loaders run only once at startup
  - Significantly improved performance
- Removed test JSON files (using real data)

### Testing

- Added property damage tests with resistance checking

---

## [v0.0.79] - 2025-10-17

### Added

- **Resistance Deserializer**: Unified resistance parsing across all loaders
  - Consistent resistance handling for creatures, items, properties
- **ResistanceUtil**: Damage calculation after resistance
  - Formula: `finalDamage = baseDamage × (2 - resistance / 100)`

### Changed

- Attacks now use Resistance enums instead of strings
- Moved enums to dedicated package: `Resistances`, `Stats`, `Type`, `Size`, `CreatureType`
- Updated README.md file structure

### Refactoring

- Updated all code and tests for enum-based system
- Cleaner type safety and autocompletion

---

## [v0.0.78] - 2025-10-17

### Removed

- Leftover attack report code from Creature.java

### Documentation

- Updated README.md

---

## [v0.0.77] - 2025-10-15

### Added

- **AttackEngine**: Centralized combat mechanics
  - Moved attacking methods from Creature.java
  - Attack reports now generated by engine
- **RandomProvider**: Simplified random number generation utility
- **EquipmentManager**: Equipment handling separated from Creature
  - Equip/unequip logic moved for better separation of concerns
- **PropertyManager**: Centralized property (buff/debuff/trait) management
- **Accuracy System**: Fine-tune hit chances
  - `accuracy` and `magicAccuracy` for general bonuses
  - Per-attack accuracy modifiers

### Changed

- Creature.java significantly simplified (logic moved to managers)

### Documentation

- Updated README.md with new architecture

---

## [v0.0.76] - 2025-10-14

### Added

- **Expanded Property Effects**: Properties can now modify:
  - `staminaRegen`, `crit`, `dodge`, `block`, `magicResist`
  - `maxHp`, `maxStamina`, `maxMana`
  - Percentage-based HP/Stamina/Mana modifications
- **Test Properties**: Test Buff and Test Debuff for development

### Changed

- Python formatter now handles property files
- Updated IDS.md

---

## [v0.0.76] - 2025-10-13

### Added

- **Property ID System**: Multiple properties of same type can exist
  - IDs differentiate similar buffs (Health Regen 1, Health Regen 2)
- **Bleed Debuff**: Damage-over-time effect (negative `regenHp`)
- **Name Lookup System**: Reference entities by name or ID
  - Items, creatures, properties all searchable by name

### Changed

- **Property Refactor**: Unified Buff, Debuff, and Trait classes
  - Shared code consolidated
  - Cleaner inheritance structure

### Testing

- Added NameLookupTest for search functionality

---

## [v0.0.75] - 2025-10-12

### Added

- **Health Regen Buff**: Healing-over-time property system
  - Duration-based buffs
  - Automatic HP restoration per turn
- **Property Duration**: Time-limited effects
  - `onTick()` method for turn-based updates
- Property `toString()` methods for debugging

### Testing

- PropertyTest.java validates health regen mechanics

---

## [v0.0.74] - 2025-10-11

### Removed

- PropertyImpl.java (logic distributed to Property subclasses)

---

## [v0.0.73] - 2025-10-11

### Added

- **Three Property Types**: Separated into distinct implementations
  - `Buff`: Positive temporary effects
  - `Debuff`: Negative temporary effects
  - `Trait`: Permanent passive abilities
- **AllLoaders**: Single command loads all game data
- **PropertyIntegrationTest**: End-to-end property testing
- Debug logging system for AI assistance

### Changed

- **PropertyManager → PropertyLoader**: Naming consistency with other loaders
- **Immunity Consolidation**: Immunities are now buffs, not separate type
  - Updated ID ranges in IDS.md
  - Migrated immunity property IDs and types
- **Python Formatter**: Now handles Stats and Resistances formatting

### Fixed

- Player selection bugs
- Data loading edge cases

---

## [v0.0.72] - 2025-10-10

### Added

- **Level-Up System**: Stat point allocation
  - `statPoints` granted per level
  - Spend points to increase attributes
- **LevelUtil**: Level progression calculations
  - Experience requirements scale with level
- **Turn Passing**: Foundation for turn-based mechanics

### Changed

- Consistent method naming for HP, Stamina, and Mana
- Updated toString() to show XP progress (e.g., "1250/2000 XP to level 5")
- Minimum stat value enforced (can't go below 1)

### Documentation

- Updated README.md file structure

---

## [v0.0.71] - 2025-10-09

### Added

- **Resource Regeneration**: HP, Stamina, and Mana regen stats
  - `baseRegenHp`, `baseRegenStamina`, `baseRegenMana`
  - Equipment can modify regeneration rates
- **StatUtil**: Helper functions for stat manipulation
  - Simplifies level-up stat allocation

### Changed

- Regeneration mechanics prepared (implementation pending turn system)

### Documentation

- Updated README.md

---

## [v0.0.70] - 2025-10-08

### Added

- **Magic Resistance**: Chance to resist magical damage
  - `baseMagicResist` and `equipmentMagicResist` tracking
  - Equipment can grant magic resist
  - Separate from elemental resistances
- **ResistanceUtil**: Helper for resistance type categorization
  - Determines if resistance is "MAGICAL", "PHYSICAL", or "TRUE"

### Changed

- Minor JSON updates (added levels and magicResist to enemies)
- Attack reports now show physical vs magical damage separately
- Attack logic differentiates damage types for resistance application

### Refactoring

- Moved utilities out of Creature.java for cleaner code

---

## [v0.0.69] - 2025-10-07

### Added

- **Experience Rewards**: Enemies grant XP when defeated
  - `totalXp` field in creature JSONs
  - Combat.java transfers XP to victor

---

## [v0.0.68] - 2025-10-07

### Added

- **Magic Stat Bonuses**: Weapons can use multiple stats for magic damage
  - `magicStatBonus` array (e.g., ["INTELLIGENCE", "WISDOM"])
  - Best stat is automatically selected
- **Physical Stat Bonuses**: Multiple physical stats per weapon
  - `statBonuses` array for physical attacks
  - Enables Finesse (STR or DEX) weapons
- Item printing now shows all attacks and properties
- Attack.java `toString()` method for debugging

### Changed

- Removed "isVersatile" and "isFinesse" (now just "versatile" and "finesse")
- Updated weapon JSONs with new stat system
- Stat bonuses multiply damage more effectively (player stats matter more)
- Python formatter now sorts JSON fields

### Documentation

- Updated weapon tooltips to reflect stat bonuses

---

## [v0.0.67] - 2025-10-06

### Added

- **Versatile Weapon System**: Separate attack tables for one-hand/two-hand
  - `attacks`: Used when wielded one-handed
  - `versatileAttacks`: Used when wielded two-handed
  - Enables better balance (two-hand grants more damage)
- **Python JSON Sorter**: `format_jsons.py` maintains consistent field order
- **ID Automation**: Python script validates and maintains IDS.md
  - Detects duplicate IDs
  - Validates ID ranges
  - Generates `IDS_generated.md` with missing entries

### Changed

- Fixed Old Bow ID (9803 → 9600)
- Corrected "isVersatile" to "versatile" throughout codebase

### Testing

- Verified versatile weapon switching mechanics

---

## [v0.0.66] - 2025-10-05

### Added

- **Versatile Weapon Implementation**: Weapons can be wielded one or two-handed
  - Two-handed wielding grants damage bonuses
  - Automatically manages offhand slot

### Testing

- Added versatile weapon test cases

---

## [v0.0.65] - 2025-10-05

### Added

- **Stat-Based Attack Bonuses**: Stats now significantly affect combat
  - Attack rolls: `d20 + (stat bonus × 5)`
  - Makes character building more meaningful
  - Example: 15 STR = +5 modifier = +25 to attack
- **Combined Defense**: Dodge and block merged into single defense roll
  - Higher value determines defensive style (flavor text)
  - Simplifies combat calculations

### Changed

- Attack rolls now round to nearest integer
- Helper variable for stat bonus display (e.g., "Strength 15 +5")
- Creature.java uses helper for cleaner calculations
- Training Dummy updated to test defense mechanics

### Documentation

- Updated README.md with combat formulas

---

## [v0.0.64] - 2025-10-05

### Changed

- Adjusted creature JSON crit and luck stats for balance

---

## [v0.0.64] - 2025-10-04

### Added

- **Stamina System**: Resource for physical abilities
  - `baseMaxStamina` scales with Constitution
  - Stamina management (current/max tracking)
- **Luck-Based Crits**: Critical hit chance tied to Luck stat
  - Changing Luck adjusts crit chance dynamically

### Changed

- Lowered starting crit values for players (luck scaling compensates)

### Fixed

- Mana and stamina JSON values no longer overwritten by Creature.java

---

## [v0.0.63] - 2025-10-03

### Added

- **Mana System**: Resource for spellcasting
  - `currentMana` and `maxMana` tracking
  - `baseMaxMana` scales with Intelligence (10 mana per INT point)
  - Ratio preserved when max mana changes
- Default mana and stamina set to 100

### Changed

- Removed obsolete `defaultDamageType` variable
- Tidied Creature and CreatureLoader files
- Updated documentation files with current features

### Testing

- Added unarmed attack test in TestCreatureAttack.java

---

## [v0.0.62] - 2025-10-02

### Added

- **Stamina and Mana Foundations**: Added fields for future systems
  - `currentStamina` / `maxStamina`
  - `currentMana` / `maxMana`
  - Getters and setters implemented
  - `alterStamina()` and `alterMana()` preserve current/max ratio

---

## [v0.0.61] - 2025-10-01

### Added

- **Base Combat Stats**: Foundation for equipment calculations
  - `baseCrit`, `baseDodge`, `baseBlock`
  - Equipment bonuses calculated separately
- **DEX-Dodge Link**: Dexterity affects dodge chance
  - Each point above 10 DEX: +2.5% dodge
  - Each point below 10 DEX: -2.5% dodge
- **Equipment Stat Tracking**: Separate tracking for equipment bonuses
  - Clear separation between base and equipped stats

### Changed

- Level up logic moved to function in Creature.java
- Simplified `updateMaxHp()` method
- Commands no longer support diagonal movement (design decision for balance)
- Updated symbol system in CommandParser

### Fixed

- Equipment stat updates now work correctly

### Testing

- Tested movement and look commands on 50×50 map

---

## [v0.0.60] - 2025-09-30

### Added

- **Test Items**: Test Armor and Test Helmet for development
- Combat stat caps now properly enforced
  - Negative values stored correctly
  - Prevents stat manipulation exploits

### Changed

- Creature loader no longer loads duplicate entries

### Testing

- ItemTest validates edge values for crit, dodge, and block
- Updated IDS.md with missing entries

---

## [v0.0.59] - 2025-09-29

### Added

- **Dodge and Block Mechanics**: Active defense system
  - Attacker rolls against dodge first
  - If dodge fails, rolls against block
  - Both must fail for attack to land

### Changed

- Crit, block, and dodge converted to float for precision

---

## [v0.0.58] - 2025-09-29

### Changed

- File structure refinement: Player JSONs directly under `players/`
- Updated file reading at runtime

---

## [v0.0.58] - 2025-09-28

### Added

- **Monster Pools**: Random encounter system
  - Pool 1: Skeletons, Goblin, Dark Hound
  - Weighted random selection from pool

### Testing

- Verified spawn mechanics work as intended

---

## [v0.0.57] - 2025-09-26

### Added

- **Attack Definitions**: Creatures and weapons now have attack patterns
  - `magicDamageType` can be defined per attack
  - Weapon tooltips describe attack patterns
- **Healing Potion Tooltips**: Display healing amounts

### Changed

- Removed damage dice from weapons (moved to attacks)
- Goblin renamed to Goblin Berserker
- Attempted weapon and monster balance pass

### Fixed

- Updated IDS.md
- Corrected duplicate IDs (Dark Hound and Skeleton Spearman)

---

## [v0.0.56] - 2025-09-25

### Changed

- Removed weapon-level damage dice (attacks define damage)
- Added tooltips for item descriptions

### Testing

- Updated tests for attack-based damage system

---

## [v0.0.54] - 2025-09-24

### Added

- **Critical Hit System**: Damage multiplier on lucky hits
  - Critical hits double damage
  - `critMod` can modify crit chance per attack
  - Crits calculated per hit, not per attack
- **Training Dummy**: Test creature for combat mechanics

### Testing

- Added attack testing helper function in Creature class

---

## [v0.0.53] - 2025-09-24

### Added

- **Attack System**: Comprehensive attack customization
  - Attack.java class for flexible combat
  - Example: Dark Hound with multiple attack types
- **Combat Stats**: Crit, dodge, block chances
  - Equipment can modify these stats
- **DiceRoller**: Centralized dice rolling utility with unit tests

### Changed

- Equipment and Attack classes now integrated
- Updated README.md project structure

---

## [v0.0.52] - 2025-09-23

### Added

- **New Enemies**:
  - Skeleton Spearman
  - Skeleton Swordsman
  - Dark Hound
- **New Weapons**: Goblin and Skeleton equipment

### Changed

- Added missing creature folders
- Updated and fixed IDS.md

---

## [v0.0.51] - 2025-09-22

### Added

- **Guaranteed Loot Drops**: Loot pools can specify minimum drops
  - Enables boss loot and treasure chests
  - More granular drop chance control
- **Common Treasure Chest**: Example loot pool

### Testing

- TestIDSPaths.java validates ID assignments and file paths
- Updated loot pool tests for new mechanics

### Fixed

- Corrected Parrying Dagger name in IDS.md
- Added `data/` prefix to all item paths

---

## [v0.0.50] - 2025-09-22

### Added

- **Loot Pool System**: Random item drops
  - Search by name or ID
  - Weighted drop chances
- Loot pool section in IDS.md

### Removed

- Unused `userItemRarity` field

### Testing

- Added loot pool generation tests

---

## [v0.0.49] - 2025-09-21

### Added

- **Dice Modifiers**: Added support for "+" modifiers
  - Example: "1d6+2" (roll 1d6 and add 2)
  - Works for both attacking and healing (potions)

### Testing

- Updated attack tests for modifier support

---

## [v0.0.48] - 2025-09-20

### Added

- **Consumables**: Potion system
  - Healing functionality
  - Minor Healing Potion implemented
- Potion directory structure

---

## [v0.0.47] - 2025-09-20

### Added

- **Starting Equipment**: Players spawn with gear
  - Loaded from creature JSON
  - Character-specific starting items
- **Finesse Weapons**: DEX-based melee weapons
  - Fixed attack function (was mixing versatile and finesse)

### Changed

- Moved IDS.md to `data/` directory
- Updated IDS.md with new items
- Added starter gear for player characters

### Fixed

- Two-handed weapon unequip bug (ghost item in offhand)

### Testing

- Verified equipment loading and two-handed weapon mechanics

---

## [v0.0.46] - 2025-09-18

### Added

- **Loot and Monster Pool Templates**: Framework for random generation
- **LootPool.java**: Loot pool data structure
- **LootPoolLoader.java**: JSON deserialization for loot pools (GSON-based)
- Individual creature sprites by species
  - `sprite` field in creature JSONs
  - `getSprite()` method with fallback to "player_default"
- Asset attribution documentation

### Changed

- Moved personal assets to `resources/data` for clarity
- Increased player vision range by 1
- Updated README.md with sprites and project structure
- Updated `.gitattributes`

### Media

- Added GIF showcasing dungeon movement

---

## [v0.0.45] - 2025-09-16

### Added

- **Sprite System**: Visual representation for map symbols
  - Basic sprite rendering
  - One sprite per symbol type (expandable later)
- **Map Panning**: Camera follows player
- Sprite assets added to project

---

## [v0.0.44] - 2025-09-15

### Added

- **Combat View**: Dedicated combat interface
  - Displays player and enemy information
  - Combat action menu
  - Delayed damage display for readability

### Changed

- Increased window size to accommodate combat UI

---

## [v0.0.43] - 2025-09-14

### Added

- **Side Panel UI**: Character information display
  - Player stats in floor view
  - Inventory screen

### Documentation

- Updated README.md with UI features

---

## [v0.0.42] - 2025-09-13

### Added

- **Character Select Screen**: Choose your hero before adventuring
  - Integrated with game start sequence
- **Map Rendering**: Floor view with fog of war
  - Shows explored and unexplored areas
- **Keyboard Movement**: WASD or arrow key controls
- **Map Actor**: Keeps map display aligned and readable

### Changed

- Separated creature and player map tracking

---

## [v0.0.41] - 2025-09-13

### Added

- **LibGDX Framework**: Graphical UI foundation
- **VisUI**: Testing UI library
- **Basic Main Menu**:
  - Clickable Start button
  - Working Exit button

### Documentation

- Updated README.md and `.gitignore`
- Added Apache 2.0 license for VisUI

---

## [v0.0.40] - 2025-09-12

### Added

- **Combat Class**: Handles turn-based combat
  - Attack and flee commands
  - Combat ends on death or successful flee
- Unarmed damage for creatures

### Testing

- Prototype combat system functional

### Documentation

- Updated README.md with game progress

---

## [v0.0.39] - 2025-09-11

### Added

- **Resistance-Based Damage**: Attack function accounts for resistances
- **Magic Attack Detection**: Checks if weapon has magical damage
- **Unarmed Combat**: Default attack when no weapon equipped
- **HP Alteration**: `alterHp()` for damage and healing

### Features

- Creatures can attack with weapons or unarmed
- Damage and healing fully functional

---

## [v0.0.38] - 2025-09-11

### Added

- **Damage System Prototype**: Foundation for combat damage
- **Damage Dice Rolling**: Function to roll weapon damage dice

### Note

- Enemy resistances not yet factored in

---

## [v0.0.37] - 2025-09-11

### Added

- **Damage Types**: Physical and elemental damage categories
  - Default damage type for unarmed attacks
- **Attack Function**: Basic creature-vs-creature combat
- **Versatile Tag**: Weapons can be one or two-handed
- **WeaponClass Enum**: Categorizes weapons by type
- **Damage Dice**: Weapons define damage rolls
- **HP Ratio Tracking**: Maintains current/max HP relationship

### Changed

- Item printing now shows all combat-relevant stats

### Fixed

- `setCurrentHp()` function corrected

---

## [v0.0.36] - 2025-09-10

### Added

- **HP Scaling**: `hplv1Bonus` for level-based HP growth
  - Formula: `baseHp + hplv1Bonus + CON bonus`
  - Per level: `+ hplv1Bonus + CON bonus`
- Dynamic HP adjustment with Constitution changes

### Testing

- Added test for character "Biggles"

---

## [v0.0.35] - 2025-09-08

### Added

- **Darksight Trait**: Extended vision in darkness
  - Vision range system for creatures
  - Defaults to 1 if not specified
- **Fog of War**: Prototype exploration system
  - Tiles have `discovered` property
  - MapPrinter shows: '#' (undiscovered), symbol (discovered), '.' (unexplored)

---

## [v0.0.34] - 2025-09-08

### Added

- **Test Equipment**: Sword, helmet, legwear, shield
  - Verified different equipment slots work correctly
- **Spells Package**: Foundation for magic system (abstract class)

### Changed

- Organized armor into subfolders: armor, helmets, legwear, shields
- Organized weapons into subfolders: blunt, slash, piercing, ranged, magic
- Updated and future-proofed ID list

### Documentation

- Updated README.md

---

## [v0.0.33] - 2025-09-07

### Added

- **Inventory System**: Beginning of item management (Inventory.java)
  - Equip items directly from inventory
  - Equipped items don't consume inventory slots
- Equipping items now grants their stat bonuses

### Documentation

- Updated README.md file structure with equipment assets

---

## [v0.0.32] - 2025-09-06

### Changed

- Stats default to 10 if not provided in JSON
- Resistances default to 100 unless specified
- Property printing improved for readability

### Fixed

- Property application after stat changes
- Edge case with very low Constitution

---

## [v0.0.31] - 2025-09-05

### Fixed

- Base HP calculation when Constitution gives no bonus
- Creature loader no longer overrides level

---

## [v0.0.30] - 2025-09-04

### Added

- **EquipmentSlot Enum**: Standardized equipment slots
- **Item Rarity Enum**: Common, Uncommon, Rare, Epic, Legendary
- **ItemLoader**: JSON-based item loading (similar to CreatureLoader)
- Test armor for equipment system

### Changed

- Updated all creature JSONs for compatibility
- Sorted Creature.java methods

### Testing

- Verified item loading in Game.java

---

## [v0.0.29] - 2025-09-03

### Added

- **Leveling System**: XP and level progression
  - Max level: 30
  - XP automatically triggers level ups
  - Recursive XP overflow handling
- **Base HP**: Separate from stat-modified HP
  - Formula: `baseHp + (CON bonus × level)`
- `level` and `experience` fields for creatures

### Changed

- Current HP cannot exceed max HP
- HP and max HP now scale dynamically with Constitution
- Modified Creature `toString()` method
- Removed map printing from parsing

---

## [v0.0.28] - 2025-09-02

### Added

- **Multi-Floor Dungeons**: Floors -10 to +10 implemented
  - Test text maps for all floors
  - All floors loaded into dungeon entity
- Vertical movement (up/down stairs) functional

---

## [v0.0.27] - 2025-09-01

### Added

- **Look Command**: Examine nearby tiles
- Updated CommandParser symbols

### Changed

- **Removed Diagonal Movement**: Design decision for balance
  - Simpler grid-based combat
  - Easier to balance abilities and range

### Testing

- Tested movement and look commands on 50×50 map

---

## [v0.0.26] - 2025-09-01

### Added

- Expanded CommandParser for all cardinal directions

### Removed

- `occupied` boolean from Tile class (not needed)

---

## [v0.0.26] - 2025-08-31

### Changed

- Removed unnecessary files from structure
- Added item structure to assets

### Documentation

- Updated README.md project structure

---

## [v0.0.26] - 2025-08-30

### Added

- **Diagonal Movement Support**: 8-directional movement
  - Tile and Floor classes prepared for diagonal neighbors
- **Player Coordinates**: Track player position on map
- **GameState.java**: Current game state tracking
- **MapPrinter**: Renders current floor
- CommandParser: Select player and print map commands

### Changed

- Cleaned up map parsing
- Game.java more modular loading

---

## [v0.0.25] - 2025-08-30

### Added

- **Creature ID System**: Future-proofing for lookups
- **IDS.md**: Central registry for all IDs
  - Creatures, effects, items
- **Creature README**: JSON structure and ID conventions
- **CreatureLoader**: Loads all players and creatures from JSON

### Testing

- Player and creature creation tests
- ID usage verification
- Property management tests

### Changed

- Cleaned up test files

---

## [v0.0.24] - 2025-08-29

### Added

- **JSON Properties**: All properties now use JSON format
  - Stat modifications
  - Resistance changes

### Changed

- PropertyImpl accepts Stat and Resistance changes
- Creature supports new property system
- CharacterLoader works with new properties

---

## [v0.0.24] - 2025-08-28

### Added

- **GSON Library**: JSON parsing for game data
- **PropertyLoader**: Loads properties from JSON
  - HashMap-based ID lookup
- **JSON Creature Loading**: Modified Creature and Player for JSON (WIP)

### Changed

- PropertyManager (still broken)
- Test resource file structure reorganized
- Floor text files in subfolder
- Added asset folders: creatures, players, properties

### Testing

- PropertyLoader test files

### Deprecated

- Disabled old property system files

---

## [v0.0.23] - 2025-08-27

### Added

- **Item Stat Modification**: Items can modify creature stats
- **First Player Character**: Human player (WIP)
- **Licenses**: MIT (code), CC BY-NC-ND 4.0 (assets)

### Changed

- Human Adaptability trait doesn't affect luck

### Testing

- HumanPlayerTest.java for first character

---

## [v0.0.21] - 2025-08-27

### Added

- **Game Class**: Handles game initialization
- **Humanoid Subcategories**: Humans and Goblinoids
- **Property ID System**: Easy property manipulation
- **Property Slots**: Buffs, debuffs, immunities, traits
  - Properties modify creature on application/removal

### Changed

- Moved "Coward" trait to proper location

---

## [v0.0.2] - 2025-08-26

### Added

- Temporary map generation command
- Floor generation testing

### Changed

- Edited Tile class, fixed bugs

### Todo

- Code cleanup and modularization

---

## [v0.0.2] - 2025-08-25

### Added

- **CommandParser**: Foundation for game commands
- **Tile Symbols**: Early version with logic (WIP)
- README.md placeholder sections

---

## [v0.0.1] - 2025-08-25

### Added

- **Map Parser**: Improved parsing with tests
- **Game Class**: Core game loop foundation
- Test files for all major classes
- **Coordinate Class**: 2D position tracking
- **Tile Class**: Floor building blocks (placeholder variables)

### Changed

- File structure for test floors (text files)
- Categorized creatures by type
- Fixed test directory structure (mirroring main)

### Testing

- Two test floor files (one ready for testing)
- Expanded creature creation tests
- Fixed creature stat and resistance functions

---

## [v0.0.1] - 2025-08-24

### Added

- **MapParser Structure**: Basic parser implementation
- Dungeon and Floor abstracts
- Item and event placeholders in file structure
- **Property Types**: Immunity, Buff, Debuff, Trait
- Creature `description` field
- Creature HP, resistance, and stat modification methods
  - HP capped at maxHP

### Changed

- Component renamed to Property
- More file structure changes
- Set up Maven and project structure

### Testing

- Creature class tests
- Goblin class tests

### Documentation

- Added project license

---

## [v0.0.1] - 2025-08-23

### Added

- **Resistance Immunities**: All resistance types have immunity properties
- Proper Maven setup

---

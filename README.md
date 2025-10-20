# Recall Dungeon

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![LibGDX](https://img.shields.io/badge/LibGDX-1.13.1-red.svg)](https://libgdx.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Assets: CC BY-NC-ND 4.0](https://img.shields.io/badge/Assets-CC%20BY--NC--ND%204.0-lightgrey.svg)](ASSETS-LICENSE)

![Demo — tiles & sprites](https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExZnJkcW56Ymt6Zml5ZWl0eXZzZmVzc3FkaTE4YzViZ3VqcTlpbzFqdSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/dfahiJN1ducOisBcLV/giphy.gif)

<sub>GIF embedded from GIPHY — <a href="https://giphy.com/gifs/dfahiJN1ducOisBcLV">via GIPHY</a></sub>

A Java-based roguelike dungeon crawler built with Maven and LibGDX.

**Explore the mysterious dungeon where you have awakened.** Decide whether you will go deeper into the dungeon or try to escape to the surface!

This project includes a custom Java-based "Recall Engine" designed specifically for grid-based dungeon gameplay. All game logic, entity systems, map parsing, and command handling are part of a custom game engine I'm developing from scratch.

Now includes a LibGDX desktop client (LWJGL3) with Scene2D/VisUI for menus and rendering.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Combat and Stats](#combat-and-stats)
- [Loading and Finalization](#loading-and-finalization)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running the Project](#running-the-project)
- [Running Tests](#running-tests)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Story](#story)

---

## Features

### Turn-based Dungeon Exploration

- Procedurally loaded dungeon floors with fog-of-war (undiscovered tiles are hidden until explored)
- Player vision range determines how much of the map is revealed
- Creatures have multiple attacks that are decided semi-randomly via weighted randomness

### Creature System

- **Creature type hierarchy**: Specialized creature classes for different entity types
  - **Player**: Player character with extended functionality
  - **Enemy**: Hostile creatures with AI behavior
  - **NPC**: Non-hostile characters for dialogue and quests
- **Creature variants**: Multiple creature types organized by family
  - Beast, Construct, Humanoid, Undead, etc.
- Creatures have stats (Strength, Dexterity, Constitution, etc.) and resistances (Fire, Ice, Bludgeoning, etc.)
- Level and XP system with customizable XP progression
- Runtime utilities: `com.bapppis.core.util` contains helper classes used by gameplay and tests. Notable ones:
  - `StatUtil` — static helpers to increase/decrease stats safely (clamps decreases to not go below zero).
  - `Dice` — rolls dice strings (e.g., "2d6").
  - `AttackUtil` / `WeaponUtil` / `ResistanceUtil` — helpers that choose attacks, compute weapon stat bonuses, and classify/parse resistances.

### Inventory & Equipment

- Comprehensive inventory system for items, weapons, armor, and consumables
- Equipment slots: weapon, offhand, armor, helmet, legwear
- **Weapon hierarchy**: Specialized weapon classes for different combat styles
  - **Melee Weapons**: Slash (swords, axes), Piercing (daggers, spears), Blunt (maces, hammers)
  - **Ranged Weapons**: Bows and Crossbows with ammunition support
  - **Magic Weapons**: Staffs, Arcane weapons, and Magic-Physical hybrid weapons
  - Support for two-handed and versatile weapons
- **Equipment hierarchy**: Specialized equipment classes (Armor, Helmet, Legwear, Offhand)
- Weapons have physical and optional magical damage types (with dice notation, e.g., 2d6)
- Weapons have multiple attacks decided semi-randomly via weighted randomness
- Items and equipment can modify stats, resistances, and traits
- **Consumables system**: Potions and consumables apply buffs, debuffs, and properties to creatures
  - Properties are automatically applied when consumables are used
  - Comprehensive test coverage ensures correct property application
- **Dual-lookup system**: Items and properties can be referenced by both ID and name in JSON files
  - Supports flexible data authoring (use meaningful names instead of numeric IDs)
  - Loaders automatically resolve names to objects at runtime
- Loot and monster pools for unique experiences in every playthrough

### Combat System

- Turn-based combat loop between player and enemies. The combat resolution was refactored to separate physical and magical components and to make testing deterministic.
- Player chooses actions (attack, flee, etc.) each turn (the game also exposes a non-interactive mode for automated tests).
- Dice-based damage calculation (e.g., 2d6 for physical, 1d8 for magic).
- Attack resolution (current model):
  - Each attempted hit rolls a single to-hit value (0–100) and compares it to the target's avoidance window.
  - Physical hits use a dodge+block partition (block prevents damage like armor); TRUE-type physical damage ignores block and only checks dodge.
  - Magical hits use dodge + magic resist as the avoidance window (no block).
  - Weapons with a magic element can trigger a dual-resolution attack: physical and magical parts are resolved independently and reported separately.
  - Crits are rolled per successful hit and are included in detailed `AttackEngine.AttackReport` objects emitted via `AttackEngine.attackListener` (useful for tests).

### Properties & Traits

- Buffs, debuffs, immunities, and traits can be applied to creatures
- Properties can modify stats, resistances, and vision range

### Asset-driven Design

- Creatures, items, floors, and properties are loaded from JSON files for easy customization and extension
- Extensible mod support for the future

### Rendering & Sprites

- Tile and creature rendering: ASCII map characters are mapped to sprite regions so the LibGDX map view renders tiles and creatures using graphics instead of plain text.
- Per-character sprite binding: player and creature JSON may include a `"sprite"` field to select a specific sprite for that character (for example, different starting characters can show different player sprites).
- Loading strategy: the renderer prefers a packed `TextureAtlas` (region names from `tiles.json`) and falls back to individual PNG files in `src/main/resources/assets/sprite_pngs/` when an atlas region is not available.
- Default behavior: when a player character has no sprite defined or loading fails, the runtime falls back to a built-in `player_default` sprite.
- Asset locations: runtime sprite art and per-PNG fallbacks are kept in `src/main/resources/assets/`. Full third-party packs are archived under `src/main/resources/assets/thirdparty/` and are noted in `ATTRIBUTION.md`.

### Extensible Command System

- Command parser for player input (move, look, up, down, etc.)
- Easily extendable for new commands and actions

---

## Tech Stack

- **Java 17** - Core programming language
- **Maven 3.8+** - Build automation and dependency management
- **LibGDX 1.13.1** - Game development framework
  - LWJGL3 backend for desktop
  - Scene2D for UI rendering
- **VisUI 1.5.5** - UI widgets and skin for Scene2D
- **Gson** - JSON serialization/deserialization for game data
- **ClassGraph** - Classpath scanning for resource loading
- **JUnit 5** - Unit and integration testing

---

## Combat and Stats

### Attack Resolution

- **Single to-hit roll per attack** (0–100) compared against target's avoidance window
- **Physical attacks:**
  - Use dodge + block as avoidance window (partitioned to avoid overlap)
  - Block prevents damage entirely (like armor deflection)
  - TRUE-type physical damage ignores block, only checks dodge
- **Magical attacks:**
  - Use dodge + magic resist as avoidance window (no block)
  - Magic resist reduces magical damage
- **Dual-element weapons:**
  - Physical and magical components resolved independently
  - Separate damage calculations and reports for each type
- **Critical hits:**
  - Rolled per successful hit
  - Crit chance clamped to 0–100 at check time
  - Detailed crit information in `AttackEngine.AttackReport`

### Stat Mechanics

- **Dexterity → Dodge:**
  - Formula: `Dodge = baseDodge + 2.5 × (DEX − 10)`
  - Negative DEX reduces dodge; positive DEX increases it
- **Intelligence → Max Mana:**
  - Max mana scales ±10% per INT point relative to 10
  - Result floored with minimum of 25
  - Current mana preserves fraction when max changes
- **Wisdom → Stamina Regeneration:**
  - Base regen: `floor(maxStamina / 5)`
  - Bonus: `floor(2.5 × (WIS - 10))` with minimum of 1
  - Equipment provides additional regen

### Raw vs Effective Values

- Raw crit/dodge/block values stored unclamped (include equipment modifiers)
- Values clamped only when used for probability checks (0–100)
- Partitioning logic ensures deterministic outcomes

### Data-Driven Combat

- **No implicit default attacks** - creatures must define all attacks in JSON
- Unarmed attacks require explicit JSON entries
- Attack weights determine selection probability

---

## Loading and Finalization

### Loader Pipeline

1. **Items load first** - Ensures starting equipment IDs can be resolved
2. **Properties load** - Buffs, debuffs, immunities, and traits
3. **Creatures and players load** from `src/main/resources/data/creatures/**`
   - Starting inventory applied
   - Equipment slots populated (helmet, armor, legwear, weapon, offhand)
   - Properties applied by ID or name
4. **Dual-lookup resolution:**
   - Items can be referenced by numeric ID or string name
   - Properties can be referenced by numeric ID or string name
   - Loaders automatically resolve names to objects at runtime

### Load-Once Optimization

- **Production mode:** Loaders cache data after first load for performance
- **Test mode:** Individual tests can call `forceReload()` to get fresh data
- **Maven test suite:** Loads only once across all tests for speed

### `finalizeAfterLoad()` Responsibilities

- Resets HP to base and applies level/CON scaling
- Recomputes max mana from INT and preserves current mana ratio
- Sets stamina to max
- Initializes stamina regen from base + WIS components
- Converts stored levels to XP so level-up bonuses apply via `addXp()`

### Developer/Testing Hooks

- **`AttackEngine.attackListener`** - Set to `Consumer<AttackEngine.AttackReport>` to receive detailed per-attack reports
  - Raw rolls, crit counts, after-resist damage
  - Per-component diagnostics for physical and magical damage
  - Essential for deterministic unit tests
- **Non-interactive combat mode** - Supports programmatic combat runs for CI/testing

---

## Project Structure

```
recall-dungeon/
├── LICENSE                          # MIT License (code)
├── ASSETS-LICENSE                   # CC BY-NC-ND 4.0 (project assets)
├── ATTRIBUTION.md                   # Third-party asset attributions
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── src/
│   ├── main/
│   │   ├── java/com/bapppis/
│   │   │   ├── Main.java            # Entry point (legacy console mode)
│   │   │   └── core/
│   │   │       ├── AllLoaders.java  # Centralized loader initialization
│   │   │       ├── CreatureType.java
│   │   │       ├── Resistances.java
│   │   │       ├── Size.java
│   │   │       ├── Stats.java
│   │   │       ├── Type.java        # Damage types (FIRE, ICE, PHYSICAL, etc.)
│   │   │       ├── combat/          # Combat engine and mechanics
│   │   │       │   ├── AttackEngine.java
│   │   │       │   ├── RandomProvider.java
│   │   │       │   └── DefaultRandomProvider.java
│   │   │       ├── creature/        # Creature system
│   │   │       │   ├── Creature.java
│   │   │       │   ├── CreatureLoader.java
│   │   │       │   ├── Enemy.java
│   │   │       │   ├── NPC.java
│   │   │       │   ├── Player.java
│   │   │       │   ├── Attack.java
│   │   │       │   ├── ItemManager.java
│   │   │       │   ├── Inventory.java
│   │   │       │   ├── PropertyManager.java
│   │   │       │   ├── creatureEnums/   # Creature-related enums
│   │   │       │   │   ├── CreatureType.java
│   │   │       │   │   ├── Size.java
│   │   │       │   │   ├── Stats.java
│   │   │       │   │   └── Type.java
│   │   │       │   └── creaturetype/    # Biological creature type implementations
│   │   │       │       ├── aberration/
│   │   │       │       ├── beast/       # Beast species (Dog, etc.)
│   │   │       │       ├── celestial/
│   │   │       │       ├── construct/   # Construct species (TrainingDummy, etc.)
│   │   │       │       ├── dragon/
│   │   │       │       ├── elemental/
│   │   │       │       ├── fey/
│   │   │       │       ├── fiend/
│   │   │       │       ├── giant/
│   │   │       │       ├── humanoid/    # Humanoid species (Human, Goblin, etc.)
│   │   │       │       ├── monstrosity/
│   │   │       │       ├── ooze/
│   │   │       │       ├── plant/
│   │   │       │       ├── playertype/
│   │   │       │       ├── undead/      # Undead species (Skeleton, Zombie, etc.)
│   │   │       │       └── unknown/
│   │   │       ├── dungeon/         # Dungeon generation and management
│   │   │       │   ├── Dungeon.java
│   │   │       │   ├── Floor.java
│   │   │       │   └── mapparser/
│   │   │       ├── event/           # Game events and event system
│   │   │       ├── game/            # Game loop and state management
│   │   │       │   ├── Game.java
│   │   │       │   └── GameState.java
│   │   │       ├── gfx/             # LibGDX graphics layer
│   │   │       │   ├── DesktopLauncher.java
│   │   │       │   ├── RecallDungeon.java
│   │   │       │   └── MapActor.java
│   │   │       ├── item/            # Item system
│   │   │       │   ├── Item.java
│   │   │       │   ├── ItemLoader.java
│   │   │       │   ├── Equipment.java
│   │   │       │   ├── EquipmentUtils.java
│   │   │       │   ├── Consumable.java
│   │   │       │   ├── Misc.java
│   │   │       │   ├── Weapon.java
│   │   │       │   ├── equipment/   # Equipment subclasses
│   │   │       │   │   ├── Armor.java
│   │   │       │   │   ├── Helmet.java
│   │   │       │   │   ├── Legwear.java
│   │   │       │   │   └── Offhand.java
│   │   │       │   ├── enums/       # Item-related enums
│   │   │       │   │   ├── EquipmentSlot.java
│   │   │       │   │   ├── ItemType.java
│   │   │       │   │   ├── Rarity.java
│   │   │       │   │   ├── WeaponClass.java
│   │   │       │   │   └── WeaponType.java
│   │   │       │   ├── melee/       # Melee weapon subclasses
│   │   │       │   │   ├── MeleeWeapon.java
│   │   │       │   │   ├── bluntweapon/
│   │   │       │   │   │   └── BluntWeapon.java
│   │   │       │   │   ├── piercingweapon/
│   │   │       │   │   │   └── PiercingWeapon.java
│   │   │       │   │   └── slashweapon/
│   │   │       │   │       └── SlashWeapon.java
│   │   │       │   ├── ranged/      # Ranged weapon subclasses
│   │   │       │   │   ├── RangedWeapon.java
│   │   │       │   │   ├── bow/
│   │   │       │   │   │   └── Bow.java
│   │   │       │   │   └── crossbow/
│   │   │       │   │       └── Crossbow.java
│   │   │       │   └── magic/       # Magic weapon subclasses
│   │   │       │       ├── MagicWeapon.java
│   │   │       │       ├── arcaneweapon/
│   │   │       │       │   └── ArcaneWeapon.java
│   │   │       │       ├── magicphysicalweapon/
│   │   │       │       │   └── MagicPhysicalWeapon.java
│   │   │       │       └── staff/
│   │   │       │           └── Staff.java
│   │   │       ├── loot/            # Loot and treasure system
│   │   │       │   ├── LootManager.java
│   │   │       │   ├── LootPool.java
│   │   │       │   └── LootPoolLoader.java
│   │   │       ├── property/        # Buffs, debuffs, traits, immunities
│   │   │       │   ├── Property.java
│   │   │       │   ├── PropertyLoader.java
│   │   │       │   ├── PropertyType.java
│   │   │       │   └── ResistanceMapDeserializer.java
│   │   │       ├── spell/           # Spell system (WIP)
│   │   │       └── util/            # Utility classes
│   │   │           ├── AttackUtil.java
│   │   │           ├── DebugLog.java
│   │   │           ├── Dice.java
│   │   │           ├── LevelUtil.java
│   │   │           ├── ResistanceUtil.java
│   │   │           ├── ResistancesDeserializer.java
│   │   │           ├── StatUtil.java
│   │   │           └── WeaponUtil.java
│   │   └── resources/
│   │       ├── assets/              # Runtime graphics and UI assets
│   │       │   ├── uiskin.json      # VisUI skin definition
│   │       │   ├── uiskin.atlas     # Texture atlas for UI
│   │       │   ├── uiskin.png       # UI sprite sheet
│   │       │   ├── sprite_pngs/     # Individual sprite fallbacks
│   │       │   └── thirdparty/      # Third-party sprite packs
│   │       └── data/                # Game data (JSON)
│   │           ├── creatures/       # Creature definitions
│   │           │   ├── beasts/
│   │           │   ├── constructs/
│   │           │   ├── dragons/
│   │           │   ├── elementals/
│   │           │   ├── humanoids/
│   │           │   ├── plants/
│   │           │   ├── players/
│   │           │   ├── undead/
│   │           │   └── unknown/
│   │           ├── floors/          # Floor/map layouts
│   │           │   ├── floor(20x20).txt
│   │           │   └── floor(50x50).txt
│   │           ├── items/           # Item definitions
│   │           │   ├── armor/
│   │           │   │   ├── armor/   # Body armor
│   │           │   │   ├── helmets/
│   │           │   │   ├── legwear/
│   │           │   │   └── shields/
│   │           │   ├── consumables/
│   │           │   │   └── potions/
│   │           │   └── weapons/
│   │           │       ├── melee weapons/
│   │           │       │   ├── blunt weapons/
│   │           │       │   ├── piercing weapons/
│   │           │       │   └── slash weapons/
│   │           │       ├── ranged weapons/
│   │           │       └── magic weapons/
│   │           ├── properties/      # Property definitions
│   │           │   ├── buff/
│   │           │   ├── debuff/
│   │           │   ├── immunity/
│   │           │   └── trait/
│   │           ├── loot_pools/      # Loot pool definitions
│   │           └── monster_pools/   # Monster pool definitions
│   ├── test/
│   │   ├── java/com/bapppis/core/  # Unit and integration tests
│   │   │   ├── Creature/
│   │   │   │   ├── EquipVersatileTest.java
│   │   │   │   ├── beast/
│   │   │   │   ├── construct/
│   │   │   │   ├── dragon/
│   │   │   │   ├── elemental/
│   │   │   │   ├── humanoid/
│   │   │   │   ├── plant/
│   │   │   │   ├── player/
│   │   │   │   ├── undead/
│   │   │   │   └── unknown/
│   │   │   ├── TestCreatureAttack.java
│   │   │   ├── dungeon/
│   │   │   │   └── mapparser/
│   │   │   ├── game/
│   │   │   │   ├── CombatTest.java
│   │   │   │   ├── GameTest.java
│   │   │   │   └── ...
│   │   │   ├── integration/
│   │   │   ├── item/
│   │   │   │   ├── ItemTest.java
│   │   │   │   ├── ItemEquipEdgeCasesTest.java
│   │   │   │   ├── armor/
│   │   │   │   │   └── ArmorTest.java
│   │   │   │   ├── consumable/
│   │   │   │   │   └── ConsumableTest.java
│   │   │   │   ├── equipment/
│   │   │   │   ├── melee/
│   │   │   │   ├── ranged/
│   │   │   │   ├── magic/
│   │   │   │   └── weapon/
│   │   │   │       └── weaponTest.java
│   │   │   ├── loaders/
│   │   │   ├── loot/
│   │   │   ├── monster/
│   │   │   ├── property/
│   │   │   └── util/
│   │   └── resources/              # Test fixtures and data
│   │       └── assets/
│   └── manual-test/                # Manual/interactive tests
│       └── java/                   # (excluded from default test suite)
├── scripts/                        # Build and utility scripts
└── target/                         # Maven build output
```

### Key Directories

- **`src/main/java/com/bapppis/core/`** - Core game engine code
- **`src/main/resources/data/`** - JSON game data (creatures, items, properties, floors)
- **`src/main/resources/assets/`** - Graphics, sprites, and UI assets
- **`src/test/java/`** - Automated test suite (47+ tests)
- **`src/manual-test/java/`** - Manual/interactive tests (opt-in via Maven profile)

---

## Creature System Architecture

The game uses a sophisticated inheritance hierarchy for creatures that combines class-based inheritance with JSON data-driven instantiation.

### Creature Hierarchy

```
Creature (base class)
├── CreatureType Classes (e.g., Humanoid, Beast, Undead)
│   └── Species Classes (e.g., Human, Goblin, Dog, Skeleton)
│
└── Instance Classes
    ├── Player (playable characters)
    ├── Enemy (hostile creatures)
    └── NPC (non-player characters)
```

### How It Works

1. **Base Creature Class** - `Creature.java`

   - Contains all core creature functionality
   - Stats (STR, DEX, CON, INT, WIS, CHA, LUCK)
   - Resistances (FIRE, ICE, BLUDGEONING, etc.)
   - Properties (traits, buffs, debuffs)
   - Equipment and inventory management
   - Combat mechanics

2. **CreatureType Classes** - Broad biological categories

   - **Humanoid** - Two-legged, intelligent creatures
   - **Beast** - Animals and monsters
   - **Undead** - Reanimated creatures
   - **Construct** - Artificial beings
   - **Dragon** - Draconic creatures
   - **Elemental** - Beings of pure elemental energy
   - **Plant** - Living vegetation

   Each type can add default properties/modifiers in its constructor.

   **Example:** `Humanoid.java` adds the "Darksight" property to all humanoids.

3. **Species Classes** - Specific creature subtypes

   - Extend their CreatureType class
   - Add species-specific properties and modifiers
   - Located in: `com.bapppis.core.creature.creaturetype.<type>.<Species>`

   **Example:** `Human.java` extends `Humanoid` and adds "Human Adaptability" trait.

   ```
   Humanoid (CreatureType)
   ├── Human (adds Human Adaptability)
   ├── Goblin (sets size to SMALL)
   ├── Orc
   └── Elf
   ```

4. **Instance Classes** - Determine role in game

   - **Player** - Controllable characters
   - **Enemy** - Hostile creatures
   - **NPC** - Friendly or neutral characters

   These determine game mechanics, not biological traits.

### Loading Process

When a creature is loaded from JSON:

1. **JSON defines the template:**

   ```json
   {
     "id": 5001,
     "name": "Captain Voss",
     "creatureType": "HUMANOID",
     "species": "Human",
     "stats": { "STR": 12, "DEX": 8 },
     "properties": ["HumanAdaptability"]
   }
   ```

2. **CreatureLoader processes it:**

   - Reads `creatureType` and `species` from JSON
   - Instantiates the species class: `com.bapppis.core.creature.creaturetype.humanoid.Human`
   - Creates a "template" instance to capture constructor-added properties
   - Creates the actual Player/Enemy/NPC instance
   - **Priority 1 (Lowest):** Applies JSON data (id, name, stats, equipment, etc.)
   - **Priority 2 (Highest):** Copies species-specific data from template (size, properties)
   - Result: Captain Voss has data from JSON **overridden** by species defaults where applicable

3. **Priority Order (Highest to Lowest):**

   ```
   1. Species (e.g., Human) - Highest priority
      ↓ (sets size, adds species-specific properties)
   2. CreatureType (e.g., Humanoid) - Medium priority
      ↓ (adds type-specific properties via species inheritance)
   3. JSON data - Lowest priority
      ↓ (provides base values: id, name, stats, equipment)
   4. Instance Class (Player/Enemy/NPC) - Role only
      (determines game mechanics, not inherited traits)
   ```

4. **Example: Goblin vs JSON**

   - JSON might specify `"size": "MEDIUM"`
   - But `Goblin` constructor sets `setSize(Size.SMALL)`
   - **Result:** Goblin is SMALL (species overrides JSON) ✅

5. **Property Combination:**
   ```
   Humanoid constructor → adds "Darksight"
   Human constructor → adds "Human Adaptability"
   JSON → adds any additional properties
   Final creature → has ALL properties combined (additive, not replacement)
   ```

### Creating New Species

To add a new species (e.g., Elf):

1. **Create the species class:**

   ```java
   package com.bapppis.core.creature.creaturetype.humanoid;

   public class Elf extends Humanoid {
       public Elf() {
           super(); // Inherits Darksight from Humanoid
           addProperty("ElvenGrace"); // Add species-specific trait
           // Set any species-specific defaults
       }
   }
   ```

2. **Create creature JSON files:**

   ```json
   {
     "id": 6500,
     "name": "Wood Elf Scout",
     "creatureType": "HUMANOID",
     "species": "Elf",
     "stats": { "DEX": 16, "WIS": 14 }
   }
   ```

3. **That's it!** The loader automatically:
   - Finds the `Elf` class
   - Applies Humanoid → Elf → JSON properties in order
   - Creates a fully functional creature

### Benefits of This System

✅ **Code reuse** - Common traits defined once in species constructors
✅ **Smart priority** - Species can override JSON defaults (e.g., Goblin size always SMALL)
✅ **Type safety** - Compile-time checking of species classes
✅ **Easy extension** - Add new species without modifying loader code
✅ **Clear hierarchy** - Biological traits (species) separate from role (Player/Enemy)
✅ **Additive properties** - Properties from all sources combine rather than replace

### Example: Captain Voss (Human Player)

```
Properties on Captain Voss:
1. Darksight (from Humanoid constructor)
2. Human Adaptability (from Human constructor)
3. Any additional traits from JSON

Class hierarchy:
Creature → Humanoid → Human (template)
                      ↓
                    Player (instance)
```

---

## Prerequisites

- **Java 17 or higher** - [Download OpenJDK](https://openjdk.org/projects/jdk/17/)
- **Maven 3.8 or higher** - [Download Maven](https://maven.apache.org/download.cgi)
- **Git** - [Download Git](https://git-scm.com/downloads)

---

## Setup & Installation

### Quick Start

```sh
# 1. Clone the repository
git clone https://github.com/Bapppis/recall-dungeon.git
cd recall-dungeon

# 2. Build the project
mvn clean install

# 3. Run the game
mvn exec:java -Dexec.mainClass="com.bapppis.core.gfx.DesktopLauncher"
```

### Detailed Setup

1. **Clone the repository:**

   ```sh
   git clone https://github.com/Bapppis/recall-dungeon.git
   cd recall-dungeon
   ```

2. **Verify Java and Maven versions:**

   ```sh
   java -version    # Should show Java 17 or higher
   mvn -version     # Should show Maven 3.8 or higher
   ```

3. **Build the project:**

   ```sh
   mvn clean install
   ```

4. **Run tests (optional):**

   ```sh
   mvn test
   ```

5. **Launch the game:**
   ```sh
   mvn exec:java -Dexec.mainClass="com.bapppis.core.gfx.DesktopLauncher"
   ```

---

## Running the Project

### Desktop (LibGDX LWJGL3)

```sh
# Build the project
mvn clean package -DskipTests

# Run the desktop client
mvn exec:java -Dexec.mainClass="com.bapppis.core.gfx.DesktopLauncher"
```

### Tips

- Ensure the working directory is the project root so LibGDX can find `assets/`
- VS Code users: Use `.vscode/launch.json` with `"cwd": "${workspaceFolder}"`
- IntelliJ users: Set working directory to `$PROJECT_DIR$` in run configuration
- Windows PowerShell: When running a single JUnit test method, use quotes:
  ```powershell
  mvn -Dtest="ClassName#methodName" test
  ```

---

## Running Tests

### Run all tests

```sh
mvn test
```

### Run specific test class

```sh
mvn -Dtest=ConsumableTest test
```

### Run specific test method

```sh
mvn -Dtest="ConsumableTest#testConsumableAppliesPropertyToCreature" test
```

### Manual / Interactive Tests

Some tests are long-running or print interactive/debug output and are intentionally excluded from the default test suite. These tests live under `src/manual-test/java` and only compile/run when you explicitly enable the `manual-tests` Maven profile.

**Run default tests only** (manual tests excluded):

```powershell
mvn test
```

**Run manual tests as well** (activates the `manual-tests` profile):

```powershell
mvn -P manual-tests test
```

**Note:** Manual tests often print human-readable summaries to the console for manual inspection. They won't run in CI unless the profile is enabled.

### Enabling Debug Output

Console debug/logging is centralized behind `com.bapppis.core.util.DebugLog`. To enable debug output:

1. Open `src/main/java/com/bapppis/core/util/DebugLog.java`
2. Set the `DEBUG` flag to `true`
3. Rebuild the project

---

## Roadmap

### Core Gameplay

- [ ] Class system for players with unique abilities and playstyles
- [ ] Skills and abilities usable in and out of combat
- [ ] Resource management - Mana, stamina tied to class abilities and spells
- [ ] Meaningful stats - Every stat (STR, DEX, CON, INT, WIS, CHA, LUCK) has clear gameplay impact
- [ ] Character variety - Multiple starting characters, each feeling different and balanced
- [ ] Randomized dungeon layouts - Procedural generation for infinite replayability

### Combat & Systems

- [ ] Enhanced status effects - Bleeding, stunned, shocked, frozen, etc.
- [ ] Element buildup system - Elements accumulate to trigger status effects
- [ ] Intelligent AI - Enemies move, search for player, use tactics in combat
- [ ] Dual game modes:
  - Story mode - Handcrafted floors, narrative elements, scripted encounters
  - Roguelike mode - Randomized floors, loot, and enemies for high replayability

### World & Interaction

- [ ] Interactive grid world - Environmental interactions, secrets, puzzles
- [ ] Traps - Hidden dangers that require perception and caution
- [ ] Events and encounters - Random events, NPCs, story moments
- [ ] Expanded loot system - More items, sets, unique legendary gear

### Presentation

- [ ] Sprite variety - Diverse environment tiles and objects
- [ ] Equipment visuals - Gear, armor, weapons affect player appearance
- [ ] Animations - Character movement, attacks, environmental effects
- [ ] Sound and music - Audio design and soundtrack
- [ ] Self-made sprites - Original art to give the game unique visual identity

### Long-term Goals

- Interesting story and lore for the world of Aurum
- Balanced and fun gameplay that rewards skill and strategy
- A huge compendium of armor, weapons, items, and monsters
- A game that proudly calls itself a roguelike dungeon crawler

---

## Contributing

This is a personal portfolio project developed solo to demonstrate individual capabilities. **I am not seeking contributions at this time.**

### Why Solo?

- I want to realize my own crazy ideas and learn from both victories and defeats
- This project showcases my personal coding style, architecture decisions, and problem-solving approach
- Building everything from scratch helps me understand systems at a deeper level

### Feedback Welcome!

While I'm not accepting code contributions, I'm always happy to hear:

- Feedback on gameplay or design
- Ideas or suggestions (I might implement them if they align with my vision!)
- Questions about the codebase or architecture
- Bug reports or issues you encounter

### Contact

- **Email:** Heinonen.sasha@gmail.com
- **Discord:** Bappis
- **GitHub Issues:** Feel free to open issues for bugs or discussions

---

## License

This project uses a dual-license approach for code and assets:

### Code License: MIT

All source code (`.java` files) is licensed under the **[MIT License](LICENSE)**.

**TL;DR:** You can freely use, modify, and distribute the code, including for commercial purposes, as long as you include the original copyright notice and license text.

<details>
<summary>Click to view full MIT License text</summary>

```
MIT License

Copyright (c) 2025 Sasha Sebastian Heinonen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

</details>

### Assets License: CC BY-NC-ND 4.0

Project-owned non-code assets are licensed under **[Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)](ASSETS-LICENSE)**.

**TL;DR:** You can share these assets with attribution, but you **cannot**:

- Use them commercially (sell or profit from them)
- Create derivative works (modifications)
- Include them in products you sell

**This includes:**

- JSON game data (`src/main/resources/data/`)
- Original sprites and graphics (some files in `src/main/resources/assets/`)
- Floor definitions and maps
- Creature, item, and property definitions

### Third-Party Assets

This repository includes third-party assets with their own licenses:

- **VisUI** (Apache License 2.0) - UI library and skin

  - **Commercial use allowed** with proper attribution
  - Include Apache 2.0 license text when distributing

- **Third-party sprite packs** (see `ATTRIBUTION.md` and `src/main/resources/assets/thirdparty/`)
  - Each pack has its own license terms
  - Review individual license files before use
  - Follow attribution requirements when distributing

### Important Notes for Commercial Use

If you plan to create a **commercial product** based on this project:

1. ✅ **Code (MIT):** Freely use and modify with attribution
2. ✅ **VisUI (Apache 2.0):** Allowed in commercial products with attribution
3. ❌ **Project assets (CC BY-NC-ND 4.0):** **NOT allowed** in commercial products
   - You must replace these with commercially-licensed assets
   - Or obtain separate commercial licensing
   - Or re-license them if you own the rights
4. ⚠️ **Third-party sprite packs:** Check individual license terms in `ATTRIBUTION.md`

### Asset File Locations

- **Project-owned JSON data:** `src/main/resources/data/` (CC BY-NC-ND 4.0)
- **Runtime graphics/UI:** `src/main/resources/assets/` (mixed licenses)
- **Third-party sprites:** `src/main/resources/assets/thirdparty/` (see `ATTRIBUTION.md`)
- **Full license texts:** See `LICENSE` (MIT), `ASSETS-LICENSE` (CC BY-NC-ND 4.0), and individual files in `assets/thirdparty/`

---

## Story

### The World of Aurum

Welcome to **Aurum**, a world of mystery and ancient magic. You awaken in the depths of a strange dungeon, disoriented and confused. Yet something feels familiar... as if you've been here before.

### Your Journey Begins

Strange visions flash through your mind—fragments of memories that aren't quite yours, yet somehow belong to you. This dungeon, these corridors, the monsters lurking in the shadows... you've faced them before. But how? And why can't you remember?

### A Choice of Paths

Two paths lie before you:

**Descend deeper** into the dungeon's depths. Uncover the truth behind these recurring visions and put an end to whatever dark force is causing this cycle of awakening and forgetting.

**Ascend to the surface** or escape this nightmare. Face the guardians that protect the way up and fight for your freedom, even if the answers remain buried below.

### The Recall

They call it the **Recall**—this phenomenon of awakening with fragments of past lives, past deaths, past victories and failures. Some say it's a curse. Others believe it's a gift. But everyone who experiences it knows one thing:

**Nothing down here stays forgotten forever.**

What will you choose, traveler? Will you break the cycle or embrace it?

---

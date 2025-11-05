# Asset ID Index

This file lists all known asset IDs, their names, and their resource paths. **Keep this file updated** when adding or editing assets.

---

## ID System Overview

The Recall Dungeon asset ID system is designed for scalability, clarity, and maintainability. Each major asset category and subcategory is assigned a large, memorable range of IDs. See the full system documentation at the end of this file.

**Primary lookup is by ID.** Names are for display only. Duplicate IDs are flagged (last loaded wins at runtime). Missing IDs are flagged and should be assigned. Paths listed here correspond to resources under `src/main/resources/data/**`.

---

## Spells (50000-50999)

- 50000 — Fireball — data/spells/Fireball.json
- 50001 — Elemental Chaos — data/spells/Elemental Chaos.json
- 50002 — Shield of Light — data/spells/Shield of Light.json
- 50500 — Serpent's Fang — data/spells/Serpent's Fang.json

## Player Classes (60000-60999)

Player classes define character archetypes with stat bonuses, resistances, granted traits, unlocked spells, and level-based progression. Only Player creatures can have classes.

- 60000 — Paladin — data/creatures/player_classes/Paladin.json

## Talent Trees (70000-70999)

Talent trees define progression paths within player classes. Each tree contains nodes with choices that grant stats, resistances, spells, and traits. Trees are tied to specific class IDs.

- 70000 — Paladin Talents — data/creatures/talent_trees/PaladinTalents.json

## Properties (1000-4999)

### Buffs (1000-2332)

- 1000 — Afraid Immunity — data/properties/buff/AfraidImmunity.json
- 1001 — Bleed Immunity — data/properties/buff/BleedImmunity.json
- 1002 — Blind Immunity — data/properties/buff/BlindImmunity.json
- 1003 — Burning Immunity — data/properties/buff/BurningImmunity.json
- 1004 — Concussion Immunity — data/properties/buff/ConcussionImmunity.json
- 1005 — Corrosion Immunity — data/properties/buff/CorrosionImmunity.json
- 1006 — Exposed Immunity — data/properties/buff/ExposedImmunity.json
- 1007 — Freeze Immunity — data/properties/buff/FreezeImmunity.json
- 1008 — InstaKill Immunity — data/properties/buff/InstaKillImmunity.json
- 1009 — Jolt Immunity — data/properties/buff/JoltImmunity.json
- 1010 — Silence Immunity — data/properties/buff/SilenceImmunity.json
- 1011 — Health Regen 1 — data/properties/buff/HealthRegen1.json
- 1012 — Health Regen 2 — data/properties/buff/HealthRegen2.json
- 1013 — Mana Regen 1 — data/properties/buff/ManaRegen1.json
- 1014 — Mana Regen 2 — data/properties/buff/ManaRegen2.json
- 2332 — Test Buff — data/properties/buff/TestBuff.json

### Debuffs (2333-3665)

- 2333 — Afraid — data/properties/debuff/Afraid.json
- 2334 — Bleed — data/properties/debuff/Bleed1.json
- 2335 — Necrotic Plague — data/properties/debuff/NecroticPlague.json
- 2336 — Burning 1 — data/properties/debuff/Burning1.json
- 2337 — Dizzy — data/properties/debuff/Dizzy.json
- 2338 — Poisoned 1 — data/properties/debuff/Poisoned1.json
- 2339 — Poisoned 2 — data/properties/debuff/Poisoned2.json
- 3665 — Test Debuff — data/properties/debuff/TestDebuff.json

### Traits (3666-4999)

- 3666 — Coward — data/properties/trait/Coward.json
- 3667 — Human Adaptability — data/properties/trait/HumanAdaptability.json
- 3668 — Darksight — data/properties/trait/Darksight.json
- 4001 — TestTrait1 — data/properties/trait/TestTrait1.json
- 4002 — TestTrait2 — data/properties/trait/TestTrait2.json
- 4003 — TestTrait3 — data/properties/trait/TestTrait3.json
- 4004 — TestTrait4 — data/properties/trait/TestTrait4.json
- 4005 — TestTrait5 — data/properties/trait/TestTrait5.json

## Creatures (5000-19999)

- 60000 — Paladin — data/creatures/player_classes/Paladin.json
- 60001 — Rogue — data/creatures/player_classes/Rogue.json
- 70000 — Paladin Talents — data/creatures/player_classes/talent_trees/PaladinTalents.json
- 70000 — Paladin Talents — data/creatures/talent_trees/PaladinTalents.json
- 70000 — Rogue Talents — data/creatures/player_classes/talent_trees/RogueTalents.json

### Players (5000-5499)

- 5000 — Biggles The Unlucky — data/creatures/players/BigglesTheUnlucky.json
- 5001 — Captain Aldric Voss — data/creatures/players/CaptainVoss.json
- 5002 — Biggles The Test — data/creatures/players/BigglesTheTest.json

### Neutrals (5500-5999)

- [none indexed yet]

### Enemies (6000-19999)

Enemy creatures are organized by type with dedicated ID ranges. Each creature type has 1000 IDs divided into space for up to 10 species (100 IDs per species):

#### Aberrations (6000-6999)

Each species gets 100 IDs. Examples: aberration-species-1 (6000-6099), aberration-species-2 (6100-6199), etc.

- [none indexed yet]

#### Beasts (7000-7999)

Each species gets 100 IDs. Examples: Dog (7000-7099), feline (7100-7199), avian (7200-7299), etc.

##### Dog (7000-7099)

- 7000 — Dark Hound — data/creatures/beasts/dogs/Dark Hound.json

#### Celestials (8000-8999)

Each species gets 100 IDs. Examples: angel (8000-8099), archon (8100-8199), etc.

- [none indexed yet]

#### Constructs (9000-9999)

Each species gets 100 IDs. Examples: TrainingDummy (9000-9099), golem (9100-9199), animated-object (9200-9299), etc.

##### TrainingDummy (9000-9099)

- 9000 — Training Dummy — data/creatures/constructs/training-dummies/Training Dummy.json

#### Dragons (10000-10999)

Each species gets 100 IDs. Examples: chromatic (10000-10099), metallic (10100-10199), gem (10200-10299), etc.

##### Kobold (10000-10099)

- 10000 — Kobold Warrior — data/creatures/dragons/kobolds/Kobold Warrior.json

- [other dragon species examples: chromatic (10000-10099), metallic (10100-10199), gem (10200-10299)]

#### Elementals (11000-11999)

Each species gets 100 IDs. Examples: fire (11000-11099), water (11100-11199), earth (11200-11299), air (11300-11399), etc.

- [none indexed yet]

#### Fey (12000-12999)

Each species gets 100 IDs. Examples: sprite (12000-12099), dryad (12100-12199), pixie (12200-12299), etc.

- [none indexed yet]

#### Fiends (13000-13999)

Each species gets 100 IDs. Examples: demon (13000-13099), devil (13100-13199), yugoloth (13200-13299), etc.

- [none indexed yet]

#### Giants (14000-14999)

Each species gets 100 IDs. Examples: hill-giant (14000-14099), stone-giant (14100-14199), frost-giant (14200-14299), etc.

- [none indexed yet]

#### Humanoids (15000-15999)

Each species gets 100 IDs. Examples: Goblin (15000-15099), Human (15100-15199), orc (15200-15299), elf (15300-15399), dwarf (15400-15499), etc.

##### Goblin (15000-15099)

- 15000 — Goblin Berserker — data/creatures/humanoids/goblins/Goblin Berserker.json

##### Human (15100-15199)

- [none indexed yet]

#### Monstrosities (16000-16999)

Each species gets 100 IDs. Examples: chimera (16000-16099), hydra (16100-16199), manticore (16200-16299), etc.

- [none indexed yet]

#### Oozes (17000-17999)

Each species gets 100 IDs. Examples: slime (17000-17099), jelly (17100-17199), pudding (17200-17299), etc.

- [none indexed yet]

#### Plants (18000-18999)

Each species gets 100 IDs. Examples: treant (18000-18099), vine-creature (18100-18199), fungus (18200-18299), etc.

- [none indexed yet]

#### Undead (19000-19999)

Each species gets 100 IDs. Examples: Skeleton (19000-19099), Zombie (19100-19199), vampire (19200-19299), ghost (19300-19399), lich (19400-19499), etc.

##### Skeleton (19000-19099)

- 19000 — Skeleton Spearman — data/creatures/undead/skeletons/Skeleton Spearman.json
- 19001 — Skeleton Swordsman — data/creatures/undead/skeletons/Skeleton Swordsman.json

##### Zombie (19100-19199)

- [none indexed yet]

#### Unknown Type (Reserved: use Players/Neutrals if uncertain)

- [none indexed yet]

## Items (20000-39999)

### Armor (20000-27999)

Armor items are organized by slot with generous ID ranges:

#### Chest Armor (20000-21999)

- 20000 — Armor of Water — data/items/armor/armor/Armor of Water.json
- 20001 — Armor of Bones — data/items/armor/armor/Armor of Bones.json
- 20002 — Broken Steel Armor — data/items/armor/armor/Broken Steel Armor.json
- 20003 — Leather Armor — data/items/armor/armor/Leather Armor.json
- 21999 — Test Armor — data/items/armor/armor/Test Armor.json

#### Helmets (22000-23999)

- 22000 — Crusader Helmet — data/items/armor/helmets/Crusader Helmet.json
- 22001 — Green Hood — data/items/armor/helmets/Green Hood.json
- 23999 — Test Helmet — data/items/armor/helmets/Test Helmet.json

#### Legwear (24000-25999)

- 24000 — Legs of Speed — data/items/armor/legwear/Legs of Speed.json
- 24001 — Leather Leggings — data/items/armor/legwear/Leather Leggings.json
- 24999 — Test Boots — data/items/armor/legwear/TestBoots.json

#### Shields (26000-27999)

- 26000 — Tower Shield — data/items/armor/shields/Tower Shield.json
- 26001 — Heater Shield — data/items/armor/shields/Heater Shield.json
- 26999 — Test Shield — data/items/armor/shields/TestShield.json

### Consumables (28000-28999)

- 28000 — Minor Healing Potion — data/items/consumables/potions/Minor Healing Potion.json
- 28001 — Healing Potion — data/items/consumables/potions/Healing Potion.json
- 28002 — Lesser Blue Healing Potion — data/items/consumables/potions/Lesser Blue Healing Potion.json
- 28003 — Blue Healing Potion — data/items/consumables/potions/Blue Healing Potion.json
- 28004 — Perfect Healing Potion — data/items/consumables/potions/Perfect Healing Potion.json
- 28005 — Health Regen 1 Potion — data/items/consumables/potions/Health Regen 1 Potion.json
- 28998 — Test Buff Potion — data/items/consumables/potions/TestBuffPotion.json
- 28999 — Test Healing Potion — data/items/consumables/potions/TestHealingPotion.json

### Weapons (29000-39999)

Weapons are organized by type (Melee, Ranged, Magic) with subcategories:

#### Melee Weapons (29000-33999)

##### Slash Weapons (29000-30332)

- 29000 — Falchion of Doom — data/items/weapons/melee weapons/slash weapons/Falchion of Doom.json
- 29001 — Rusty Iron Sword — data/items/weapons/melee weapons/slash weapons/Rusty Iron Sword.json
- 29002 — Parrying Dagger — data/items/weapons/melee weapons/slash weapons/Parrying Dagger.json
- 29999 — Test Sword — data/items/weapons/melee weapons/slash weapons/TestSword.json

##### Piercing Weapons (30333-31665)

- 30333 — Rusty Iron Spear — data/items/weapons/melee weapons/piercing weapons/Rusty Iron Spear.json
- 30334 — Morningstar — data/items/weapons/melee weapons/piercing weapons/Morningstar.json

##### Blunt Weapons (31666-32999)

- 31666 — Steel Flanged Mace — data/items/weapons/melee weapons/blunt weapons/Steel Flanged Mace.json

#### Ranged Weapons (34000-36999)

##### Bows (34000-35499)

- 34000 — Old Bow — data/items/weapons/ranged weapons/Old Bow.json

##### Crossbows (35500-36999)

- [none indexed yet]

#### Magic Weapons (37000-39999)

##### Staves (37000-37999)

- 37000 — Staff Of Flames — data/items/weapons/magic weapons/staves/Staff of Flames.json

##### Arcane Weapons (38000-38999)

- 38000 — Sword of Windfury — data/items/weapons/magic weapons/arcane weapons/Sword of Windfury.json

##### Magic Physical Weapons (39000-39999)

- [none indexed yet]

## Loot Pools (40000-40999)

- 40000 — Common Weapons — data/loot_pools/Common Weapons.json
- 40001 — Common Potions — data/loot_pools/Common Potions.json
- 40002 — Common Treasure Chest — data/loot_pools/Common Treasure Chest.json

## Monster Pools (41000-41999)

- 41000 — Floor 0 Enemies — data/monster_pools/Floor 0 Enemies.json

## Reserved (42000-49999)

Reserved for future game systems (quests, abilities, locations, etc.)

---

# ID System Documentation

## Overview

This section describes the ID allocation system for all game assets in Recall Dungeon.

### Design Principles

1. **Generous Range Allocation**: Each category and subcategory gets a large, dedicated ID range to avoid running out of space as the game grows.
2. **Logical Grouping**: IDs are organized hierarchically (major categories, subcategories, further subdivisions).
3. **Easy Mental Math**: Ranges are round numbers and consistent.
4. **Future-Proof**: Reserved space (42000-49999) for future expansion.

## Complete ID Ranges

```
┌─────────────────────────────────────────────────────────────┐
│ SPELLS (50000-50999)                                        │
│ - 1000 IDs total                                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ PROPERTIES (1000-4999)                                      │
│ ├─ Buffs       (1000-2332) - 1333 IDs                      │
│ ├─ Debuffs     (2333-3665) - 1333 IDs                      │
│ └─ Traits      (3666-4999) - 1334 IDs                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ CREATURES (5000-19999)                                      │
│ ├─ Players     (5000-5499) - 500 IDs                       │
│ ├─ Neutrals    (5500-5999) - 500 IDs                       │
│ └─ Enemies     (6000-19999) - 14000 IDs                    │
│    ├─ Aberrations    (6000-6999)  - 1000 IDs (10 species, 100 IDs each)
│    ├─ Beasts         (7000-7999)  - 1000 IDs (10 species, 100 IDs each)
│    │  └─ Dog (7000-7099)
│    ├─ Celestials     (8000-8999)  - 1000 IDs (10 species, 100 IDs each)
│    ├─ Constructs     (9000-9999)  - 1000 IDs (10 species, 100 IDs each)
│    │  └─ TrainingDummy (9000-9099)
│    ├─ Dragons        (10000-10999) - 1000 IDs (10 species, 100 IDs each)
│    ├─ Elementals     (11000-11999) - 1000 IDs (10 species, 100 IDs each)
│    ├─ Fey            (12000-12999) - 1000 IDs (10 species, 100 IDs each)
│    ├─ Fiends         (13000-13999) - 1000 IDs (10 species, 100 IDs each)
│    ├─ Giants         (14000-14999) - 1000 IDs (10 species, 100 IDs each)
│    ├─ Humanoids      (15000-15999) - 1000 IDs (10 species, 100 IDs each)
│    │  ├─ Goblin (15000-15099)
│    │  └─ Human (15100-15199)
│    ├─ Monstrosities  (16000-16999) - 1000 IDs (10 species, 100 IDs each)
│    ├─ Oozes          (17000-17999) - 1000 IDs (10 species, 100 IDs each)
│    ├─ Plants         (18000-18999) - 1000 IDs (10 species, 100 IDs each)
│    └─ Undead         (19000-19999) - 1000 IDs (10 species, 100 IDs each)
│       ├─ Skeleton (19000-19099)
│       └─ Zombie (19100-19199)
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ ITEMS (20000-39999)                                         │
│ ├─ Armor       (20000-27999) - 8000 IDs                    │
│ │  ├─ Body Armor     (20000-21999) - 2000 IDs             │
│ │  ├─ Helmets        (22000-23999) - 2000 IDs             │
│ │  ├─ Legwear        (24000-25999) - 2000 IDs             │
│ │  └─ Shields        (26000-27999) - 2000 IDs             │
│ ├─ Consumables (28000-28999) - 1000 IDs                    │
│ └─ Weapons     (29000-39999) - 11000 IDs                   │
│    ├─ Melee Weapons  (29000-33999) - 5000 IDs             │
│    │  ├─ Slash       (29000-30332) - 1333 IDs             │
│    │  ├─ Piercing    (30333-31665) - 1333 IDs             │
│    │  └─ Blunt       (31666-32999) - 1334 IDs             │
│    ├─ Ranged Weapons (34000-36999) - 3000 IDs             │
│    │  ├─ Bows        (34000-35499) - 1500 IDs             │
│    │  └─ Crossbows   (35500-36999) - 1500 IDs             │
│    └─ Magic Weapons  (37000-39999) - 3000 IDs             │
│       ├─ Staffs      (37000-37999) - 1000 IDs             │
│       ├─ Arcane      (38000-38999) - 1000 IDs             │
│       └─ Magic Physical (39000-39999) - 1000 IDs          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ LOOT POOLS (40000-40999)                                    │
│ - 1000 IDs total                                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ MONSTER POOLS (41000-41999)                                 │
│ - 1000 IDs total                                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ RESERVED (42000-49999)                                      │
│ - 8000 IDs for future game systems                         │
│   (quests, abilities, locations, achievements, etc.)        │
└─────────────────────────────────────────────────────────────┘
```

## How to Assign New IDs

1. **Identify the Category**: Determine which major category your asset belongs to (Spell, Property, Creature, Item, etc.)
2. **Find the Subcategory**: Within that category, identify the appropriate subcategory (e.g., Items → Consumables).
3. **For Enemy Creatures - Find the Species**: Each enemy type is divided into 10 species ranges (100 IDs each). For example:
   - Beasts: Dog (7000-7099), feline (7100-7199), avian (7200-7299), etc.
   - Constructs: TrainingDummy (9000-9099), golem (9100-9199), etc.
   - Humanoids: Goblin (15000-15099), Human (15100-15199), orc (15200-15299), etc.
   - Undead: Skeleton (19000-19099), Zombie (19100-19199), vampire (19200-19299), etc.
   - Assign creatures to the appropriate species range, or create a new species if needed.
   - The "species" field in the creature JSON must match the species class name (e.g., "Dog", "Skeleton", "Goblin").
4. **Find the Next Available ID**: Check this file for the highest used ID in that range and use the next number.
5. **Update this file**: Add your new asset to the appropriate section in sorted order.
6. **Set ID in JSON**: Add the ID to your asset's JSON file.

## Special Cases

**Test Assets**: Use the highest ID in their range (e.g., Test Armor: 21999). This keeps them separated from production assets and easy to identify.

**Creature Type Unknown**: Avoid using the "Unknown" type. Use Players or Neutrals for unique/friendly/neutral creatures. If truly unknown, document why.

**Running Out of IDs**: If a subcategory exhausts its range, use reserved space (42000-49999) and document the overflow in this file.

## Migration Notes

**Recent Changes (October 2025):**

- The ID system was overhauled to accommodate all 14 creature types, increase ID ranges, and improve organization.
- All asset JSON files were updated with new IDs. See git history for details.

## Benefits of New System

- **Scalability**: Each enemy type can have up to 1000 variants.
- **Organization**: IDs clearly indicate category (e.g., 7000s = Beasts).
- **Consistency**: All subcategories follow similar patterns.
- **Future-Proof**: 8000 reserved IDs for new systems.
- **Easy to Remember**: Round numbers make it easy to know what range to use.

## Best Practices

1. Always update this file when adding a new asset.
2. Use sequential IDs within a range.
3. Document test assets clearly with high IDs in range.
4. Check for conflicts before committing (use search tools).
5. Keep this file sorted within each section for easy scanning.

## Automated Validation (Future)

Consider implementing:

- Pre-commit hook to check for duplicate IDs
- Build-time validation of ID ranges
- Tool to find next available ID in a range
- Generator for new asset templates with auto-assigned IDs

## Monster_pools


## Loot_pools

- 40002 — Common Treasure Chest — data/loot_pools/Common Treasure Chest.json
- 40001 — Common Potions — data/loot_pools/Common Potions.json
- 40000 — Common Weapons — data/loot_pools/Common Weapons.json
- 41000 — Floor 0 Enemies — data/monster_pools/Floor 0 Enemies.json

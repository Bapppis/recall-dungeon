# Asset ID Index

This file lists known asset ids and their counterparts (name and path). Keep it updated when adding/editing assets.

Notes
- Primary lookup is by id. Names are for display.
- Duplicate ids are flagged (later load wins at runtime).
- Missing ids are flagged and should be assigned.
 - Paths listed here correspond to resources under `src/main/resources/data/**`, which is the root scanned by the loader at runtime.

## Spells (000-999)
- [none indexed yet]

## Properties (1000-4999)
### Buff (1000-2332)
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

### Debuff (2333-3665)
- 2333 — Afraid — data/properties/debuff/Afraid.json

### Trait (3666–4999)
- 3666 — Coward — data/properties/trait/Coward.json
- 3667 — Human Adaptability — data/properties/trait/HumanAdaptability.json
- 3668 — Darksight — data/properties/trait/Darksight.json

## Creatures (5000-6999)
- 6601 — Skeleton Swordsman — data/creatures/undead/Skeleton Swordsman.json
### Players (5000-5499)
### Players (5000-5499)
- 5001 — Captain Aldric Voss — data/creatures/players/CaptainVoss.json
- 5000 — Biggles The Unlucky — data/creatures/players/BigglesTheUnlucky.json

### Neutrals (5500-5999)

### Enemies (6000-6999)
#### Beasts (6000-6099)
- 6000 — Dark Hound — data/creatures/beasts/Dark Hound.json

#### Constructs (6100-6199)
- 6100 — Training Dummy — data/creatures/constructs/Training Dummy.json

#### Dragons (6200-6299)
- [none indexed yet]

#### Elementals (6300-6399)
- [none indexed yet]

#### Humanoids (6400-6499)
- 6400 — Goblin Berserker — data/creatures/humanoids/goblinoid/Goblin Berserker.json

#### Plants (6500-6599)
- [none indexed yet]

#### Undead (6600-6699)
- 6600 — Skeleton Spearman — data/creatures/undead/Skeleton Spearman.json
- 6601 — Skeleton Swordsman — data/creatures/undead/Skeleton Swordsman.json

#### Unknown (6700-6799)
- [none indexed yet]

## Items (7000-9999)
### Armor (7000-7999)
#### Armor (7000-7249)
- 7000 — Armor of Water — data/items/armor/armor/Armor of Water.json
- 7001 — Armor of Bones — data/items/armor/armor/Armor of Bones.json
- 7002 — Broken Steel Armor — data/items/armor/armor/Broken Steel Armor.json
- 7003 — Leather Armor — data/items/armor/armor/Leather Armor.json
- 7249 — Test Armor — data/items/armor/armor/Test Armor.json

#### Helmets (7250-7499)
- 7250 — Crusader Helmet — data/items/armor/helmets/Crusader Helmet.json
- 7251 — Green Hood — data/items/armor/helmets/Green Hood.json
- 7499 — Test Helmet — data/items/armor/helmets/Green Hood.json

#### Legwear (7500-7749)
- 7500 — Legs of Speed — data/items/armor/legwear/Legs of Speed.json
- 7501 — Leather Leggings — data/items/armor/legwear/Leather Leggings.json

#### Shields (7750-7999)
- 7750 — Tower Shield — data/items/armor/shields/Tower Shield.json
- 7751 — Heater Shield — data/items/armor/shields/Heater Shield.json

### Consumables(8000-8999)
- 8000 — Minor Healing Potion — data/items/consumables/potions/Minor Healing Potion.json
- 8001 — Healing Potion — data/items/consumables/potions/Healing Potion.json
- 8002 — Lesser Blue Healing Potion — data/items/consumables/potions/Lesser Blue Healing Potion.json
- 8003 — Blue Healing Potion — data/items/consumables/potions/Blue Healing Potion.json
- 8004 — Perfect Healing Potion — data/items/consumables/potions/Perfect Healing Potion.json

### Weapons (9000-9999)
#### Blunt Weapons (9000-9199)
- [none indexed yet]

#### Magic Weapons (9200-9399)
- [none indexed yet]

#### Piercing Weapons (9400-9599)
- 9400 — Rusty Iron Spear — data/items/weapons/melee weapons/piercing weapons/Rusty Iron Spear.json

#### Ranged Weapons (9600-9799)
- 9600 — Old Bow — data/items/weapons/ranged weapons/Old Bow.json

#### Slash Weapons (9800-9999)
- 9800 — Falchion of Doom — data/items/weapons/melee weapons/slash weapons/Falchion of Doom.json
- 9801 — Rusty Iron Sword — data/items/weapons/melee weapons/slash weapons/Rusty Iron Sword.json
- 9802 — Parrying Dagger — data/items/weapons/melee weapons/slash weapons/Parrying Dagger.json

### Loot Pools (10000-10999)
- 10000 — Common Weapons — data/loot_pools/Common Weapons.json
- 10001 — Common Potions — data/loot_pools/Common Potions.json
- 10002 — Common Treasure Chest — data/loot_pools/Common Treasure Chest.json

### Monster Pools (11000-11999)
- 11000 — Floor 0 Enemies — data/monster_pools/Floor 0 Enemies.json

## Maintenance tips
- Enforce unique ids (consider an automated test).
- Prefer id lookups in code; keep this file as a human-friendly reference.
- When adding a creature, choose ids by range (e.g., players 5000+, enemies 6000+).

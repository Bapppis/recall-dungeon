# Asset ID Index

This file lists known asset ids and their counterparts (name and path). Keep it updated when adding/editing assets.

Notes
- Primary lookup is by id. Names are for display.
- Duplicate ids are flagged (later load wins at runtime).
- Missing ids are flagged and should be assigned.

## Spells (000-999)
- [none indexed yet]

## Properties (1000-4999)
### Buff (1000–1999)
- [none indexed yet]

### Debuff (2000–2999)
- 2000 — Afraid — data/properties/debuff/Afraid.json

### Immunity (3000–3999)
- 3000 — Afraid Immunity — data/properties/immunity/AfraidImmunity.json
- 3001 — Bleed Immunity — data/properties/immunity/BleedImmunity.json
- 3002 — Blind Immunity — data/properties/immunity/BlindImmunity.json
- 3003 — Burning Immunity — data/properties/immunity/BurningImmunity.json
- 3004 — Concussion Immunity — data/properties/immunity/ConcussionImmunity.json
- 3005 — Corrosion Immunity — data/properties/immunity/CorrosionImmunity.json
- 3006 — Exposed Immunity — data/properties/immunity/ExposedImmunity.json
- 3007 — Freeze Immunity — data/properties/immunity/FreezeImmunity.json
- 3008 — InstaKill Immunity — data/properties/immunity/InstaKillImmunity.json
- 3009 — Jolt Immunity — data/properties/immunity/JoltImmunity.json
- 3010 — Silence Immunity — data/properties/immunity/SilenceImmunity.json

### Trait (4000–4999)
- 4000 — Coward — data/properties/trait/Coward.json
- 4001 — Human Adaptability — data/properties/trait/HumanAdaptability.json
- 4002 — Darksight — data/properties/trait/Darksight.json

## Creatures (5000-6999)
### Players (5000-5499)
- 5001 — Captain Aldric Voss — data/creatures/players/humanplayers/CaptainVoss.json
- 5000 — Biggles The Unlucky — data/creatures/players/humanplayers/BigglesTheUnlucky.json

### Neutrals (5500-5999)

### Enemies (6000-6999)
#### Beasts (6000-6099)
- 6000 — Dark Hound — data/creatures/beasts/Dark Hound.json

#### Constructs (6100-6199)
- 6100 — Training Dummy — data/creatures/constructs/Training Dummyjson

#### Dragons (6200-6299)
- [none indexed yet]

#### Elementals (6300-6399)
- [none indexed yet]

#### Humanoids (6400-6499)
- 6400 — Goblin — data/creatures/humanoids/goblinoid/Goblin.json

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

#### Helmets (7250-7499)
- 7250 — Crusader Helmet — data/items/armor/helmets/Crusader Helmet.json

#### Legwear (7500-7749)
- 7500 — Legs of Speed — data/items/armor/legwear/Legs of Speed.json

#### Shields (7750-7999)
- 7750 — Tower Shield — data/items/armor/shields/Tower Shield.json

### Consumables(8000-8999)
- [none indexed yet]
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
- 9803 — Old Bow — data/items/weapons/ranged weapons/Old Bow.json

#### Slash Weapons (9800-9999)
- 9800 — Falchion of Doom — data/items/weapons/melee weapons/slash weapons/Falchion of Doom.json
- 9801 — Rusty Iron Sword — data/items/weapons/melee weapons/slash weapons/Rusty Iron Sword.json
- 9802 — Parrying Dagger — data/items/weapons/melee weapons/slash weapons/Parrying Dagger.json

### Loot Pools (10000-10999)
- 10000 — Common Melee Weapons — data/loot_pools/Common Melee Weapons.json
- 10001 — Common Weapons — data/loot_pools/Common Weapons.json
- 10002 — Common Potions — data/loot_pools/Common Potions.json
- 10003 — Common Treasure Chest — data/loot_pools/Common Treasure Chest.json

## Maintenance tips
- Enforce unique ids (consider an automated test).
- Prefer id lookups in code; keep this file as a human-friendly reference.
- When adding a creature, choose ids by range (e.g., players 5000+, enemies 6000+).

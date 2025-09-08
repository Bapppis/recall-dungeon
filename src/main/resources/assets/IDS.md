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
- 2000 — Afraid — properties/debuff/Afraid.json

### Immunity (3000–3999)
- 3000 — Afraid Immunity — properties/immunity/AfraidImmunity.json
- 3001 — Bleed Immunity — properties/immunity/BleedImmunity.json
- 3002 — Blind Immunity — properties/immunity/BlindImmunity.json
- 3003 — Burning Immunity — properties/immunity/BurningImmunity.json
- 3004 — Concussion Immunity — properties/immunity/ConcussionImmunity.json
- 3005 — Corrosion Immunity — properties/immunity/CorrosionImmunity.json
- 3006 — Exposed Immunity — properties/immunity/ExposedImmunity.json
- 3007 — Freeze Immunity — properties/immunity/FreezeImmunity.json
- 3008 — InstaKill Immunity — properties/immunity/InstaKillImmunity.json
- 3009 — Jolt Immunity — properties/immunity/JoltImmunity.json
- 3010 — Silence Immunity — properties/immunity/SilenceImmunity.json

### Trait (4000–4999)
- 4000 — Coward — properties/trait/Coward.json
- 4001 — Human Adaptability — properties/trait/HumanAdaptability.json

## Creatures (5000-6999)
### Players (5000-5499)
- 5001 — Captain Aldric Voss — creatures/players/humanplayers/CaptainVoss.json
- 5000 — Biggles The Unlucky — creatures/players/humanplayers/BigglesTheUnlucky.json

### Neutrals (5500-5999)

### Enemies (6000-6999)
#### Beasts (6000-6099)
- [none indexed yet]

#### Constructs (6100-6199)
- [none indexed yet]

#### Dragons (6200-6299)
- [none indexed yet]

#### Elementals (6300-6399)
- [none indexed yet]

#### Humanoids (6400-6499)
- 6400 — Goblin — creatures/humanoids/goblinoid/Goblin.json

#### Plants (6500-6599)
- [none indexed yet]

#### Undead (6600-6699)
- [none indexed yet]

#### Unknown (6700-6799)
- [none indexed yet]

## Items (7000-9999)
### Armor (7000-7999)
#### Armor (7000-7249)
 - 7000 — Armor of Water — items/armor/armor/Armor of Water.json
 - 7001 — Armor of Bones — items/armor/armor/Armor of Bones.json

#### Helmets (7250-7499)
 - 7250 — Crusader Helmet — items/armor/helmets/Crusader Helmet.json

#### Legwear (7500-7749)
 - 7500 — Legs of Speed — items/armor/legwear/Legs of Speed.json

#### Shields (7750-7999)
 - 7750 — Tower Shield — items/armor/shields/Tower Shield.json

### Consumables(8000-8999)
- [none indexed yet]

### Weapons (9000-9999)
#### Blunt Weapons (9000-9199)
- [none indexed yet]

#### Magic Weapons (9200-9399)
- [none indexed yet]

#### Piercing Weapons (9400-9599)
- [none indexed yet]

#### Ranged Weapons (9600-9799)
- [none indexed yet]

#### Slash Weapons (9800-9999)
 - 9800 — Falchion of Doom — items/weapons/slash weapons/Falchion of Doom.json

## Maintenance tips
- Enforce unique ids (consider an automated test).
- Prefer id lookups in code; keep this file as a human-friendly reference.
- When adding a creature, choose ids by range (e.g., players 5000+, enemies 6000+).

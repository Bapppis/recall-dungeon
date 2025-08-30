# Asset ID Index

This file lists known asset ids and their counterparts (name and path). Keep it updated when adding/editing assets.

Notes
- Primary lookup is by id. Names are for display.
- Duplicate ids are flagged (later load wins at runtime).
- Missing ids are flagged and should be assigned.

## Creatures
- 5001 — Captain Aldric Voss — creatures/players/humanplayers/CaptainVoss.json
- 5000 — Biggles The Unlucky — creatures/players/humanplayers/BigglesTheUnlucky.json
- [missing id] — Billy the Goblin — creatures/humanoids/goblinoid/Goblin.json

## Properties
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

### Buff (1000–1999)
- [none indexed yet]

## Maintenance tips
- Enforce unique ids (consider an automated test).
- Prefer id lookups in code; keep this file as a human-friendly reference.
- When adding a creature, choose ids by range (e.g., players 5000+, enemies 6000+).

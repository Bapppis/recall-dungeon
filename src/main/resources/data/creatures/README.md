# Creatures assets guide

This folder contains all creature definitions (players, enemies, NPCs, etc.). The loader scans this entire `data/creatures` tree recursively.

## How loading works
- The loader scans all subfolders under `data/creatures`.
- Files ending with `.json` are deserialized with Gson:
  - If the resource path contains `players/`, it is deserialized as `Player`.
  - Otherwise, it is deserialized as an `Enemy` (or base `Creature`).
- If a `properties` array is present (list of integers), each id is resolved via `PropertyManager` and applied to the creature.
- Creatures are indexed by:
  - id (primary): `CreatureLoader.getCreatureById(int)`
  - name (optional): `CreatureLoader.getCreature(String)`
- Duplicate ids on the same relative path are skipped to avoid duplicate-indexing when both source and compiled resources appear on the classpath.

## Base combat chances
- Use `baseCrit`, `baseDodge`, and `baseBlock` in creature JSON to set baseline combat chances.
- Dodge is further derived from Dexterity at runtime: `dodge = baseDodge + 2.5 × (DEX − 10)`.
- Raw values are stored unclamped; clamping occurs only at use-time (crit 0–100, dodge/block 0–80).

## Starting items and equipment
- `inventory`: array of item ids to add to the creature's inventory on spawn.
- Equipment slots: `helmet`, `armor`, `legwear`, `weapon`, `offhand` accept item ids to equip at spawn.

## Attacks
- Define creature attacks explicitly in creature JSON. There is no implicit “default” unarmed attack.
- If a weapon is equipped and defines its own attacks, those are preferred over creature attacks at runtime.

## Creature id conventions
Use ids to group types; names are for display only.
- 5000–5999: Players
- 6000–6999: Enemies / NPCs
- You can reserve sub‑ranges per species/faction if helpful (e.g., 6100–6149 goblins), but keep the top‑level range intact.

Note: This is a convention for consistency; it is not currently enforced in code.

## Minimal JSON structure
Required/commonly used fields (keep names exactly):
- `id`: integer (unique)
- `name`: string (recommended)
- `maxHp`: integer
- `currentHp`: integer
- `size`: one of SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN
- `creatureType`: one of BEAST, CONSTRUCT, DRAGON, ELEMENTAL, HUMANOID, PLANT, UNDEAD, UNKNOWN
- `stats`: object with keys STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA, LUCK
- `baseCrit`, `baseDodge`, `baseBlock`: floats for baseline combat chances
- `description`: string (optional)
- `properties`: array of integers (optional) – ids that refer to buffs/debuffs/immunities/traits

### Example (player)
{
  "id": 5001,
  "name": "Captain Aldric Voss",
  "maxHp": 30,
  "currentHp": 30,
  "size": "MEDIUM",
  "creatureType": "HUMANOID",
  "stats": {
    "STRENGTH": 13,
    "CONSTITUTION": 12,
    "DEXTERITY": 10,
    "INTELLIGENCE": 10,
    "WISDOM": 10,
    "CHARISMA": 10,
    "LUCK": 1
  },
  "description": "Captain Voss, a seasoned warrior.",
  "properties": [4001]
}

## Tips
- Keep ids unique across all creatures.
- Prefer id lookups for game logic; use names for UI only.
- If you rename or move a file, the id does not change.
- Missing stats default to 10 (LUCK to 1); missing resistances default to 100%.
- During finalization, HP and mana are recomputed (HP from base + level/CON; mana from INT with a floored factor and min 25). Stamina is set to max and stored levels are converted to XP so level-up bonuses apply via XP.

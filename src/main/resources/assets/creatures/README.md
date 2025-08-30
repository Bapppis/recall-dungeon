# Creatures assets guide

This folder contains all creature definitions (players, enemies, NPCs, etc.). The loader scans this entire `assets/creatures` tree recursively.

## How loading works
- The loader scans all subfolders under `assets/creatures`.
- Files ending with `.json` are deserialized with Gson:
  - If the resource path contains `players/`, it is deserialized as `Player`.
  - Otherwise, it is deserialized as a base `Creature`.
- If a `properties` array is present (list of integers), each id is resolved via `PropertyManager` and applied to the creature.
- Creatures are indexed by:
  - id (primary): `CreatureLoader.getCreatureById(int)`
  - name (optional): `CreatureLoader.getCreature(String)`
- Duplicate ids will overwrite the previous entry and log a warning.

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

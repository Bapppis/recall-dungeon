# python scripts/format_jsons.py
#!/usr/bin/env python3
"""Format all JSON files under src/main/resources/data with 2-space indent.

Now also enforces a canonical key ordering based on the item's canonical example
(Falchion of Doom). Any keys not present in the canonical list are appended in
alphabetical order after the known sequence. For attack objects inside an
"attacks" array a specialized ordering is applied.

This keeps diffs stable and makes visual comparison easier.
"""
import json
from pathlib import Path
from typing import Dict, Any, List

try:
    # Use resolve() to get absolute path to project root
    root = Path(__file__).resolve().parent.parent
    data_dir = root / 'src' / 'main' / 'resources' / 'data'
    print(f"Resolved data_dir: {data_dir}")
    if not data_dir.exists():
        raise FileNotFoundError(f"Data directory not found: {data_dir}")
except Exception as e:
    print(f"Error determining data directory: {e}")
    exit(1)

# ID RANGES (from IDS.md)
ID_RANGES = {
    "spells": (0, 999),
    "properties_buff": (1000, 2332),
    "properties_debuff": (2333, 3665),
    "properties_trait": (3666, 4999),
    "creatures_players": (5000, 5499),
    "creatures_neutrals": (5500, 5999),
    "creatures_enemies_aberrations": (6000, 6999),
    "creatures_enemies_beasts": (7000, 7999),
    "creatures_enemies_celestials": (8000, 8999),
    "creatures_enemies_constructs": (9000, 9999),
    "creatures_enemies_dragons": (10000, 10999),
    "creatures_enemies_elementals": (11000, 11999),
    "creatures_enemies_fey": (12000, 12999),
    "creatures_enemies_fiends": (13000, 13999),
    "creatures_enemies_giants": (14000, 14999),
    "creatures_enemies_humanoids": (15000, 15999),
    "creatures_enemies_monstrosities": (16000, 16999),
    "creatures_enemies_oozes": (17000, 17999),
    "creatures_enemies_plants": (18000, 18999),
    "creatures_enemies_undead": (19000, 19999),
    "items_armor_chest": (20000, 21999),
    "items_armor_helmets": (22000, 23999),
    "items_armor_legwear": (24000, 25999),
    "items_armor_shields": (26000, 27999),
    "items_consumables": (28000, 28999),
    "items_weapons_melee_slash": (29000, 30332),
    "items_weapons_melee_piercing": (30333, 31665),
    "items_weapons_melee_blunt": (31666, 32999),
    "items_weapons_ranged_bows": (34000, 35499),
    "items_weapons_ranged_crossbows": (35500, 36999),
    "items_weapons_magic_staffs": (37000, 37999),
    "items_weapons_magic_arcane": (38000, 38999),
    "items_weapons_magic_physical": (39000, 39999),
    "loot_pools": (40000, 40999),
    "monster_pools": (41000, 41999),
    "reserved": (42000, 49999)
}

# Canonical top-level ordering derived from Falchion of Doom.json.
CANON_TOP_ORDER: List[str] = [
    "id",
    "name",
    "description",
    "rarity",
    "itemType",
    "equipmentSlot",
    "weaponClass",
    "twoHanded",
    "finesse",
    "versatile",
    "damageType",
    "magicElement",
    "magicStatBonuses",  # multi-stat support (array)
    "magicStatBonus",
    "stats",
    "resistances",
    "dodge",
    "crit",
    "block",
    "accuracy",
    "magicAccuracy",
    "tooltip",
    "attacks",
]

# Canonical ordering for attack objects (fields optional; absent ones skipped).
CANON_ATTACK_ORDER: List[str] = [
    "name",
    "times",
    "accuracy",
    "magicAccuracy",
    "physicalDamageDice",
    "magicDamageDice",
    "damageMultiplier",
    "magicDamageMultiplier",
    "critMod",
    "weight",
    "damageType",
]

# Canonical ordering for creature objects (derived from BigglesTheUnlucky.json)
CANON_CREATURE_ORDER: List[str] = [
    "id",
    "name",
    "description",
    "creatureType",
    "size",
    "level",
    "xp",
    "enemyXp",
    "visionRange",
    "stats",
    "resistances",
    "baseBlock",
    "baseCrit",
    "baseDodge",
    "baseMagicResist",
    "accuracy",
    "magicAccuracy",
    "baseHp",
    "baseMaxMana",
    "baseMaxStamina",
    "baseHpRegen",
    "baseStaminaRegen",
    "baseManaRegen",
    "hpLvlBonus",
    "helmet",
    "armor",
    "legwear",
    "weapon",
    "offhand",
    "inventory",
    "properties",
    "attacks",
    "sprite"
]

# Canonical ordering for creature stats and resistances
CANON_CREATURE_STATS: List[str] = [
    "STRENGTH",
    "DEXTERITY",
    "CONSTITUTION",
    "INTELLIGENCE",
    "WISDOM",
    "CHARISMA",
    "LUCK",
]

CANON_CREATURE_RESISTS: List[str] = [
    "FIRE",
    "WATER",
    "WIND",
    "ICE",
    "NATURE",
    "LIGHTNING",
    "LIGHT",
    "DARKNESS",
    "BLUDGEONING",
    "PIERCING",
    "SLASHING",
    "TRUE",
]

# Canonical top-level ordering for properties (derived from TestDebuff.json)
CANON_PROPERTY_ORDER: List[str] = [
    "id",
    "name",
    "description",
    "duration",
    "damageType",
    "damageDice",
    "maxHpPercentage",
    "maxStaminaPercentage",
    "maxManaPercentage",
    "maxHp",
    "maxStamina",
    "maxMana",
    "hpRegen",
    "staminaRegen",
    "manaRegen",
    "crit",
    "dodge",
    "block",
    "magicResist",
    "accuracy",
    "magicAccuracy",
    "stats",
    "resistances",
    "tooltip",
    "type",
]


def order_keys(obj: Dict[str, Any], order: List[str]) -> Dict[str, Any]:
    """Return new dict with keys inserted following 'order'; remaining appended alphabetically."""
    ordered = {}
    for key in order:
        if key in obj:
            ordered[key] = obj[key]
    # Any remaining keys not in canonical list
    remaining = sorted(k for k in obj.keys() if k not in ordered)
    for k in remaining:
        ordered[k] = obj[k]
    return ordered


def transform(obj: Any, kind: str = None) -> Any:
    """Recursively transform an object, applying ordering rules at known structures.

    The `kind` parameter is used to select which canonical ordering to apply:
      - 'item' applies CANON_TOP_ORDER
      - 'creature' applies CANON_CREATURE_ORDER
      - None applies no top-level ordering (only attack/inner ordering)
    """
    if isinstance(obj, dict):
        # Apply only the ordering appropriate for this file kind.
        if kind == 'item' and any(k in obj for k in CANON_TOP_ORDER):
            obj = order_keys(obj, CANON_TOP_ORDER)

        if kind == 'creature' and any(k in obj for k in CANON_CREATURE_ORDER):
            obj = order_keys(obj, CANON_CREATURE_ORDER)

        if kind == 'property' and any(k in obj for k in CANON_PROPERTY_ORDER):
            obj = order_keys(obj, CANON_PROPERTY_ORDER)

        # Recurse into values
        for k, v in list(obj.items()):
            if k == "attacks" and isinstance(v, list):
                new_attacks = []
                for attack in v:
                    if isinstance(attack, dict):
                        attack = order_keys(attack, CANON_ATTACK_ORDER)
                    new_attacks.append(transform(attack, kind))
                obj[k] = new_attacks
            else:
                obj[k] = transform(v, kind)

        # Additionally sort nested simple dicts like stats/resistances.
        # For creature and property files, use canonical ordering; otherwise fall back to alphabetical.
        if kind in ('creature', 'property'):
            if 'stats' in obj and isinstance(obj['stats'], dict):
                ordered_stats = {}
                for k in CANON_CREATURE_STATS:
                    if k in obj['stats']:
                        ordered_stats[k] = obj['stats'][k]
                # Any remaining keys appended alphabetically
                for k in sorted(k for k in obj['stats'].keys() if k not in ordered_stats):
                    ordered_stats[k] = obj['stats'][k]
                obj['stats'] = ordered_stats

            if 'resistances' in obj and isinstance(obj['resistances'], dict):
                ordered_res = {}
                for k in CANON_CREATURE_RESISTS:
                    if k in obj['resistances']:
                        ordered_res[k] = obj['resistances'][k]
                for k in sorted(k for k in obj['resistances'].keys() if k not in ordered_res):
                    ordered_res[k] = obj['resistances'][k]
                obj['resistances'] = ordered_res
        else:
            for simple_key in ("stats", "resistances"):
                if simple_key in obj and isinstance(obj[simple_key], dict):
                    obj[simple_key] = {k: obj[simple_key][k] for k in sorted(obj[simple_key].keys())}

        return obj
    elif isinstance(obj, list):
        return [transform(e, kind) for e in obj]
    else:
        return obj


changed: List[str] = []
json_files = list(data_dir.rglob('*.json'))
if not json_files:
    print(f"Warning: No JSON files found in {data_dir}")
for p in json_files:
    try:
        print(f"Processing: {p}")
        text = p.read_text(encoding='utf-8')
        obj = json.loads(text)
        # Determine file kind by path segments (items vs creatures)
        rel = p.relative_to(data_dir)
        parts = rel.parts
        if parts and parts[0] == 'items':
            kind = 'item'
        elif parts and parts[0] == 'creatures':
            kind = 'creature'
        elif parts and parts[0] == 'properties':
            kind = 'property'
        else:
            kind = None
        print(f"  Detected kind: {kind}")

        transformed = transform(obj, kind)
        new_text = json.dumps(transformed, ensure_ascii=False, indent=2) + "\n"
        if new_text != text:
            p.write_text(new_text, encoding='utf-8')
            changed.append(str(p.relative_to(root)))
            print(f"  File formatted and updated.")
        else:
            print(f"  No changes needed.")
    except Exception as e:
        print(f"Skipping {p}: {p}\n  Error: {e}")

if changed:
    print('Reformatted & re-ordered files:')
    for c in changed:
        print(c)
else:
    print('No JSON files needed formatting/order changes.')

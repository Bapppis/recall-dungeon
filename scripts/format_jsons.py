# python .\scripts\format_jsons.py --regenerate-tooltips
# python .\scripts\generate_tooltips.py --apply
# python scripts/format_jsons.py
#!/usr/bin/env python3
"""Format all JSON files under src/main/resources/data with 2-space indent.

Now also enforces a canonical key ordering based on the item's canonical example
(Falchion of Doom). Any keys not present in the canonical list are appended in
alphabetical order after the known sequence. For attack objects inside an
"attacks" array a specialized ordering is applied.

This keeps diffs stable and makes visual comparison easier.

Optional: Use --regenerate-tooltips to automatically generate weapon tooltips.
"""
import json
import re
import sys
from pathlib import Path
from typing import Dict, Any, List

# Stat abbreviations for tooltip display
STAT_ABBREV = {
    "STRENGTH": "STR",
    "DEXTERITY": "DEX",
    "CONSTITUTION": "CON",
    "INTELLIGENCE": "INT",
    "WISDOM": "WIS",
    "CHARISMA": "CHA",
    "LUCK": "LUCK",
}

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
    "spells": (50000, 50999),
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
    "weaponType",
    "damageType",
    "damageType2",
    "magicElement",
    "magicElement2",
    "magicStatBonuses",  # multi-stat support (array)
    "magicStatBonus",
    "twoHanded",
    "finesse",
    "versatile",
    "dodge",
    "crit",
    "block",
    "magicResist",
    "accuracy",
    "magicAccuracy",
    "stats",
    "resistances",
    "attacks",
    "tooltip",
]

# Canonical ordering for attack objects (fields optional; absent ones skipped).
CANON_ATTACK_ORDER: List[str] = [
    "name",
    "damageType",
    "times",
    "physicalDamageDice",
    "physicalDamageDice2",
    "magicDamageDice",
    "magicDamageDice2",
    "accuracy",
    "magicAccuracy",
    "physBuildUpMod",
    "magicBuildUpMod",
    "damageMultiplier",
    "magicDamageMultiplier",
    "critMod",
    "physicalOnHitProperty",
    "magicOnHitProperty",
    "weight",
]

# Canonical ordering for creature objects (derived from BigglesTheUnlucky.json)
CANON_CREATURE_ORDER: List[str] = [
    "id",
    "name",
    "description",
    "species",
    "creatureType",
    "size",
    "level",
    "xp",
    "enemyXp",
    "visionRange",
    "baseBlock",
    "baseCrit",
    "baseDodge",
    "baseMagicResist",
    "accuracy",
    "magicAccuracy",
    "baseHp",
    "hpDice",
    "baseMaxMana",
    "baseMaxStamina",
    "baseHpRegen",
    "baseStaminaRegen",
    "baseManaRegen",
    "stats",
    "resistances",
    "helmet",
    "armor",
    "legwear",
    "weapon",
    "offhand",
    "inventory",
    "removeProperties",
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
    "type",
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
    "resBuildUp",
    "tooltip",
]

# Canonical top-level ordering for spells
CANON_SPELL_ORDER: List[str] = [
    "id",
    "name",
    "description",
    "times",
    "manaCost",
    "damageType",
    "damageDice",
    "damageType2",
    "damageDice2",
    "damageType3",
    "damageDice3",
    "damageType4",
    "damageDice4",
    "damageMult",
    "critMod",
    "accuracy",
    "onHitProperty",
    "buffProperty",
    "statBonuses",
    "buildUpMod",
    "buildUpMod2",
    "buildUpMod3",
    "buildUpMod4",
    "tooltip",
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
      - 'property' applies CANON_PROPERTY_ORDER
      - 'spell' applies CANON_SPELL_ORDER
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

        if kind == 'spell' and any(k in obj for k in CANON_SPELL_ORDER):
            obj = order_keys(obj, CANON_SPELL_ORDER)

        # Recurse into values
        for k, v in list(obj.items()):
            # Apply canonical ordering to both 'attacks' and 'versatileAttacks' arrays
            if k in ("attacks", "versatileAttacks") and isinstance(v, list):
                new_attacks = []
                for attack in v:
                    if isinstance(attack, dict):
                        # Normalize a few legacy key names to the canonical camelCase keys
                        # so we can rename fields like PhysBuildUpMod -> physBuildUpMod
                        rename_map = {
                            'PhysBuildUpMod': 'physBuildUpMod',
                            'MagicBuildUpMod': 'magicBuildUpMod'
                        }
                        for old_k, new_k in rename_map.items():
                            if old_k in attack and new_k not in attack:
                                attack[new_k] = attack.pop(old_k)

                        # Apply canonical ordering before transforming, then transform,
                        # and re-apply ordering to ensure nested transforms don't change key order.
                        attack = order_keys(attack, CANON_ATTACK_ORDER)
                        attack = transform(attack, kind)
                        attack = order_keys(attack, CANON_ATTACK_ORDER)
                    else:
                        attack = transform(attack, kind)
                    new_attacks.append(attack)
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


def parse_dice(dice_str: str):
    """Parse dice notation and return (min_damage, max_damage)."""
    if not dice_str or dice_str.strip() == "":
        return (0, 0)
    
    dice_str = dice_str.strip()
    match = re.match(r'(\d+)d(\d+)([+-]\d+)?', dice_str)
    if not match:
        return (0, 0)
    
    num_dice = int(match.group(1))
    die_size = int(match.group(2))
    modifier = int(match.group(3)) if match.group(3) else 0
    
    min_dmg = num_dice * 1 + modifier
    max_dmg = num_dice * die_size + modifier
    
    return (min_dmg, max_dmg)


def get_damage_type_display(damage_type: str) -> str:
    """Convert damage type enum to display name."""
    type_map = {
        "SLASHING": "slashing", "PIERCING": "piercing", "BLUDGEONING": "bludgeoning",
        "FIRE": "fire", "WATER": "water", "WIND": "wind", "ICE": "ice",
        "NATURE": "nature", "LIGHTNING": "lightning", "LIGHT": "light",
        "DARKNESS": "darkness", "TRUE": "true",
    }
    return type_map.get(damage_type, damage_type.lower())


def get_primary_stat(weapon: Dict[str, Any]) -> str:
    """Determine the primary physical damage stat for this weapon."""
    weapon_class = weapon.get("weaponClass", "MELEE")
    finesse = weapon.get("finesse", False)
    
    if finesse:
        return "STR or DEX"
    if weapon_class == "RANGED":
        return "DEX"
    return "STR"


def get_magic_stat(weapon: Dict[str, Any]) -> str:
    """Get the magic stat bonus(es) for this weapon."""
    magic_stat_bonuses = weapon.get("magicStatBonuses", [])
    
    if magic_stat_bonuses:
        if len(magic_stat_bonuses) == 1:
            return STAT_ABBREV.get(magic_stat_bonuses[0], magic_stat_bonuses[0])
        else:
            return " or ".join(STAT_ABBREV.get(s, s) for s in magic_stat_bonuses)
    
    weapon_class = weapon.get("weaponClass", "MELEE")
    if weapon_class == "MAGIC":
        weapon_type = weapon.get("weaponType", "")
        if weapon_type == "ARCANE":
            return "CHA or INT"
        return "INT"
    
    return "INT"


def load_property_by_name(property_name: str) -> Dict[str, Any]:
    """Load a property JSON by name from the properties folder."""
    try:
        properties_dir = root / 'src' / 'main' / 'resources' / 'data' / 'properties'
        # Search all subdirectories for property files
        for prop_file in properties_dir.rglob("*.json"):
            try:
                with open(prop_file, "r", encoding="utf-8") as f:
                    prop = json.load(f)
                    if prop.get("name") == property_name:
                        return prop
            except Exception:
                continue
    except Exception:
        pass
    return None


def generate_weapon_tooltip(weapon: Dict[str, Any]) -> List[str]:
    """Generate tooltip lines for a weapon based on its stats and attacks."""
    lines = []
    
    # Equipment bonuses
    if weapon.get("crit"):
        lines.append(f"You gain {int(weapon['crit'])}% critical chance while wielding this weapon.")
        lines.append("")
    
    if weapon.get("block"):
        lines.append(f"You gain {int(weapon['block'])}% block chance while wielding this weapon.")
        lines.append("")
    
    if weapon.get("dodge"):
        lines.append(f"You gain {int(weapon['dodge'])}% dodge chance while wielding this weapon.")
        lines.append("")
    
    if weapon.get("magicResist"):
        lines.append(f"You gain {int(weapon['magicResist'])}% magic resistance while wielding this weapon.")
        lines.append("")
    
    stats = weapon.get("stats", {})
    if stats:
        stat_parts = []
        for stat, value in stats.items():
            stat_abbrev = STAT_ABBREV.get(stat, stat)
            if value > 0:
                stat_parts.append(f"+{value} {stat_abbrev}")
            elif value < 0:
                stat_parts.append(f"{value} {stat_abbrev}")
        
        if stat_parts:
            lines.append(f"You gain {', '.join(stat_parts)} while wielding this weapon.")
            lines.append("")
    
    resistances = weapon.get("resistances", {})
    if resistances:
        for res, value in resistances.items():
            if value != 0:
                res_display = get_damage_type_display(res)
                if value > 0:
                    lines.append(f"You take {value}% more {res_display} damage while wielding this weapon.")
                else:
                    lines.append(f"You take {abs(value)}% less {res_display} damage while wielding this weapon.")
                lines.append("")
    
    acc = weapon.get("accuracy")
    mag_acc = weapon.get("magicAccuracy")
    
    if acc and mag_acc and acc == mag_acc:
        lines.append(f"You gain +{acc} accuracy and magic accuracy while wielding this weapon.")
        lines.append("")
    else:
        if acc:
            lines.append(f"You gain +{acc} accuracy while wielding this weapon.")
            lines.append("")
        if mag_acc:
            lines.append(f"You gain +{mag_acc} magic accuracy while wielding this weapon.")
            lines.append("")
    
    # Attacks
    attacks = weapon.get("attacks", [])
    if not attacks:
        if lines and lines[-1] == "":
            lines.pop()
        return lines
    
    total_weight = sum(atk.get("weight", 1) for atk in attacks)
    
    for attack in attacks:
        attack_name = attack.get("name", "Unknown Attack")
        weight = attack.get("weight", 1)
        percentage = int(round(100 * weight / total_weight))
        
        times = attack.get("times", 1)
        phys_dice = attack.get("physicalDamageDice")
        magic_dice = attack.get("magicDamageDice")
        
        damage_parts = []
        
        if phys_dice:
            phys_min, phys_max = parse_dice(phys_dice)
            phys_stat = get_primary_stat(weapon)
            phys_damage_type = get_damage_type_display(attack.get("damageType", weapon.get("damageType", "")))
            damage_parts.append(
                f"{phys_min * times}-{phys_max * times} + 5 * {phys_stat} bonus {phys_damage_type} damage"
            )
        
        if magic_dice:
            magic_min, magic_max = parse_dice(magic_dice)
            magic_stat = get_magic_stat(weapon)
            magic_element = weapon.get("magicElement", attack.get("magicDamageType", ""))
            magic_damage_type = get_damage_type_display(magic_element) if magic_element else "magic"
            damage_parts.append(
                f"{magic_min * times}-{magic_max * times} + 5 * {magic_stat} bonus {magic_damage_type} damage"
            )
        
        attack_line = f"{attack_name} ({percentage}%): Deals " + " and ".join(damage_parts) + "."
        
        modifiers = []
        
        # Check for property-on-hit
        phys_property = attack.get("physicalOnHitProperty")
        magic_property = attack.get("magicOnHitProperty")
        
        if phys_property or magic_property:
            property_name = phys_property or magic_property
            modifiers.append(f"a chance to inflict the {property_name} condition")
        
        crit_mod = attack.get("critMod")
        if crit_mod:
            crit_val = crit_mod.strip().lstrip("+")
            if crit_val:
                modifiers.append(f"a +{crit_val}% critical chance")
        
        attack_acc = attack.get("accuracy")
        if attack_acc:
            modifiers.append(f"+{attack_acc} accuracy")
        
        attack_mag_acc = attack.get("magicAccuracy")
        if attack_mag_acc:
            modifiers.append(f"+{attack_mag_acc} magic accuracy")
        
        if modifiers:
            attack_line += " This attack has " + " and ".join(modifiers) + "."
        
        lines.append(attack_line)
        lines.append("")
    
    # Collect all unique properties mentioned in attacks
    property_names = set()
    for attack in attacks:
        phys_prop = attack.get("physicalOnHitProperty")
        magic_prop = attack.get("magicOnHitProperty")
        if phys_prop:
            property_names.add(phys_prop)
        if magic_prop:
            property_names.add(magic_prop)
    
    # Add property tooltips at the end
    for prop_name in sorted(property_names):
        prop_data = load_property_by_name(prop_name)
        if prop_data:
            # Use tooltip if available, otherwise fall back to description
            prop_tooltip = prop_data.get("tooltip")
            if prop_tooltip:
                # If tooltip is a list, join with space; if string, use as-is
                if isinstance(prop_tooltip, list):
                    tooltip_text = " ".join(prop_tooltip)
                else:
                    tooltip_text = prop_tooltip
                lines.append(f"{prop_name}: {tooltip_text}")
                lines.append("")
            else:
                # Fallback to description if no tooltip
                desc = prop_data.get("description", "")
                if desc:
                    lines.append(f"{prop_name}: {desc}")
                    lines.append("")
    
    if lines and lines[-1] == "":
        lines.pop()
    
    return lines


changed: List[str] = []
regenerate_tooltips = '--regenerate-tooltips' in sys.argv
json_files = list(data_dir.rglob('*.json'))
if not json_files:
    print(f"Warning: No JSON files found in {data_dir}")
for p in json_files:
    try:
        text = p.read_text(encoding='utf-8')
        obj = json.loads(text)
        # Determine file kind by path segments (items vs creatures vs spells)
        rel = p.relative_to(data_dir)
        parts = rel.parts
        if parts and parts[0] == 'items':
            kind = 'item'
        elif parts and parts[0] == 'creatures':
            kind = 'creature'
        elif parts and parts[0] == 'properties':
            kind = 'property'
        elif parts and parts[0] == 'spells':
            kind = 'spell'
        else:
            kind = None

        # Regenerate tooltips for weapons if requested
        if regenerate_tooltips and kind == 'item' and obj.get('itemType') == 'WEAPON':
            generated_tooltip = generate_weapon_tooltip(obj)
            if generated_tooltip:
                obj['tooltip'] = generated_tooltip

        transformed = transform(obj, kind)
        new_text = json.dumps(transformed, ensure_ascii=False, indent=2) + "\n"
        if new_text != text:
            # Only print when a file actually changed
            print(f"Processing: {p}")
            print(f"  Detected kind: {kind}")
            p.write_text(new_text, encoding='utf-8')
            changed.append(str(p.relative_to(root)))
            print(f"  File formatted and updated.")
    except Exception as e:
        print(f"Skipping {p}: {p}\n  Error: {e}")

if changed:
    print('Reformatted & re-ordered files:')
    for c in changed:
        print(c)
else:
    print('No JSON files needed formatting/order changes.')

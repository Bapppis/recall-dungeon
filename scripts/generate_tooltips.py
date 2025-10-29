# python .\scripts\generate_tooltips.py
#!/usr/bin/env python3
"""
Weapon tooltip generator prototype.

Generates tooltip text from weapon JSON data automatically.
"""
import json
import re
import sys
from pathlib import Path
from typing import Dict, Any, List, Tuple


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


def parse_dice(dice_str: str) -> Tuple[int, int]:
    """
    Parse dice notation and return (min_damage, max_damage).
    
    Examples:
        "1d6" -> (1, 6)
        "2d6" -> (2, 12)
        "1d3+2" -> (3, 5)
        "2d4-1" -> (1, 7)
    """
    if not dice_str or dice_str.strip() == "":
        return (0, 0)
    
    dice_str = dice_str.strip()
    
    # Pattern: XdY or XdY+Z or XdY-Z
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
        "SLASHING": "slashing",
        "PIERCING": "piercing",
        "BLUDGEONING": "bludgeoning",
        "FIRE": "fire",
        "WATER": "water",
        "WIND": "wind",
        "ICE": "ice",
        "NATURE": "nature",
        "LIGHTNING": "lightning",
        "LIGHT": "light",
        "DARKNESS": "darkness",
        "TRUE": "true",
    }
    return type_map.get(damage_type, damage_type.lower())


def get_primary_stat(weapon: Dict[str, Any]) -> str:
    """Determine the primary physical damage stat for this weapon."""
    weapon_class = weapon.get("weaponClass", "MELEE")
    finesse = weapon.get("finesse", False)
    
    if finesse:
        return "STR or DEX"  # Finesse uses higher of the two
    
    if weapon_class == "MELEE":
        return "STR"
    elif weapon_class == "RANGED":
        return "DEX"
    elif weapon_class == "MAGIC":
        # Mirror runtime rules from WeaponUtil:
        # - ARCANE magic weapons use the higher of CHARISMA or INTELLIGENCE
        # - other magic weapons use INT
        weapon_type = weapon.get("weaponType", "")
        if weapon_type == "ARCANE":
            return "CHA or INT"
        return "INT"
    
    return "STR"


def get_magic_stat(weapon: Dict[str, Any]) -> str:
    """Get the magic stat bonus(es) for this weapon."""
    magic_stat_bonuses = weapon.get("magicStatBonuses", [])
    
    if magic_stat_bonuses:
        if len(magic_stat_bonuses) == 1:
            stat = magic_stat_bonuses[0]
            return STAT_ABBREV.get(stat, stat)  # Abbreviate
        else:
            # Multiple stats - show all (rare case)
            return " or ".join(STAT_ABBREV.get(s, s) for s in magic_stat_bonuses)
    
    # Default fallback based on weapon class
    weapon_class = weapon.get("weaponClass", "MELEE")
    if weapon_class == "MAGIC":
        weapon_type = weapon.get("weaponType", "")
        if weapon_type == "ARCANE":
            return "CHA or INT"  # Arcane uses higher of the two
        return "INT"  # Default magic stat
    
    return "INT"


def calculate_attack_damage_range(attack: Dict[str, Any], times: int, multiplier: float) -> Tuple[int, int]:
    """Calculate total damage range for an attack considering times and multiplier."""
    dice = attack.get("physicalDamageDice") or attack.get("magicDamageDice", "")
    if not dice:
        return (0, 0)
    
    min_dmg, max_dmg = parse_dice(dice)
    
    # Multiply by number of times (each hit adds base damage + stat bonus)
    # The stat bonus is added per-hit, so damage range is per-hit × times
    min_total = min_dmg * times
    max_total = max_dmg * times
    
    return (min_total, max_total)


def generate_weapon_tooltip(weapon: Dict[str, Any]) -> List[str]:
    """Generate tooltip lines for a weapon based on its stats and attacks."""
    lines = []
    
    # Section 1: Equipment bonuses (stats, combat bonuses)
    
    # Crit
    if weapon.get("crit"):
        crit_val = weapon["crit"]
        if isinstance(crit_val, (int, float)) and crit_val != 0:
            lines.append(f"You gain {int(crit_val)}% critical chance while wielding this weapon.")
            lines.append("")
    
    # Block
    if weapon.get("block"):
        block_val = weapon["block"]
        if isinstance(block_val, (int, float)) and block_val != 0:
            lines.append(f"You gain {int(block_val)}% block chance while wielding this weapon.")
            lines.append("")
    
    # Dodge
    if weapon.get("dodge"):
        dodge_val = weapon["dodge"]
        if isinstance(dodge_val, (int, float)) and dodge_val != 0:
            lines.append(f"You gain {int(dodge_val)}% dodge chance while wielding this weapon.")
            lines.append("")
    
    # Magic Resist
    if weapon.get("magicResist"):
        mr_val = weapon["magicResist"]
        if isinstance(mr_val, (int, float)) and mr_val != 0:
            lines.append(f"You gain {int(mr_val)}% magic resistance while wielding this weapon.")
            lines.append("")
    
    # Stats
    stats = weapon.get("stats", {})
    if stats:
        stat_parts = []
        for stat, value in stats.items():
            stat_abbrev = STAT_ABBREV.get(stat, stat)  # Abbreviate
            if value > 0:
                stat_parts.append(f"+{value} {stat_abbrev}")
            elif value < 0:
                stat_parts.append(f"{value} {stat_abbrev}")
        
        if stat_parts:
            stat_text = ", ".join(stat_parts)
            lines.append(f"You gain {stat_text} while wielding this weapon.")
            lines.append("")
    
    # Resistances
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
    
    # Accuracy
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
    
    # Section 2: Attacks
    attacks = weapon.get("attacks", [])
    versatile_attacks = weapon.get("versatileAttacks", [])
    
    if not attacks:
        return lines
    
    # Calculate attack percentages
    total_weight = sum(atk.get("weight", 1) for atk in attacks)
    
    for idx, attack in enumerate(attacks):
        attack_name = attack.get("name", "Unknown Attack")
        weight = attack.get("weight", 1)
        percentage = int(round(100 * weight / total_weight))
        
        times = attack.get("times", 1)
        phys_dice = attack.get("physicalDamageDice")
        phys_dice2 = attack.get("physicalDamageDice2")
        magic_dice = attack.get("magicDamageDice")
        magic_dice2 = attack.get("magicDamageDice2")
        phys_mult = attack.get("damageMultiplier", 1.0)
        magic_mult = attack.get("magicDamageMultiplier", 1.0)
        
        # Check if there's a corresponding versatile attack
        versatile_attack = None
        if idx < len(versatile_attacks):
            versatile_attack = versatile_attacks[idx]
        
        damage_parts = []
        
        # Primary physical damage
        if phys_dice:
            phys_min, phys_max = parse_dice(phys_dice)
            phys_min_total = phys_min * times
            phys_max_total = phys_max * times
            
            # Check versatile variant
            vers_phys_dice = versatile_attack.get("physicalDamageDice") if versatile_attack else None
            vers_suffix = ""
            if vers_phys_dice and vers_phys_dice != phys_dice:
                vers_times = versatile_attack.get("times", 1)
                vers_min, vers_max = parse_dice(vers_phys_dice)
                vers_min_total = vers_min * vers_times
                vers_max_total = vers_max * vers_times
                if vers_min_total != phys_min_total or vers_max_total != phys_max_total:
                    vers_suffix = f" ({vers_min_total}-{vers_max_total})"
            
            phys_stat = get_primary_stat(weapon)
            phys_damage_type = get_damage_type_display(attack.get("damageType", weapon.get("damageType", "")))
            
            # Build primary physical damage string
            phys_dmg_str = f"{phys_min_total}-{phys_max_total}{vers_suffix} + 5 * {phys_stat} bonus {phys_damage_type} damage"
            
            # Add secondary physical damage if present
            if phys_dice2 and weapon.get("damageType2"):
                phys2_min, phys2_max = parse_dice(phys_dice2)
                phys2_min_total = phys2_min * times
                phys2_max_total = phys2_max * times
                
                # Check versatile variant for secondary
                vers_phys2_dice = versatile_attack.get("physicalDamageDice2") if versatile_attack else None
                vers2_suffix = ""
                if vers_phys2_dice and vers_phys2_dice != phys_dice2:
                    vers2_times = versatile_attack.get("times", 1)
                    vers2_min, vers2_max = parse_dice(vers_phys2_dice)
                    vers2_min_total = vers2_min * vers2_times
                    vers2_max_total = vers2_max * vers2_times
                    if vers2_min_total != phys2_min_total or vers2_max_total != phys2_max_total:
                        vers2_suffix = f" ({vers2_min_total}-{vers2_max_total})"
                
                phys2_damage_type = get_damage_type_display(weapon.get("damageType2", ""))
                phys_dmg_str += f" and {phys2_min_total}-{phys2_max_total}{vers2_suffix} {phys2_damage_type} damage"
            
            damage_parts.append(phys_dmg_str)
        
        # Primary magic damage
        if magic_dice:
            magic_min, magic_max = parse_dice(magic_dice)
            magic_min_total = magic_min * times
            magic_max_total = magic_max * times
            
            # Check versatile variant
            vers_magic_dice = versatile_attack.get("magicDamageDice") if versatile_attack else None
            vers_suffix = ""
            if vers_magic_dice and vers_magic_dice != magic_dice:
                vers_times = versatile_attack.get("times", 1)
                vers_min, vers_max = parse_dice(vers_magic_dice)
                vers_min_total = vers_min * vers_times
                vers_max_total = vers_max * vers_times
                if vers_min_total != magic_min_total or vers_max_total != magic_max_total:
                    vers_suffix = f" ({vers_min_total}-{vers_max_total})"
            
            magic_stat = get_magic_stat(weapon)
            magic_element = weapon.get("magicElement", attack.get("magicDamageType", ""))
            magic_damage_type = get_damage_type_display(magic_element) if magic_element else "magic"
            
            # Build primary magic damage string
            magic_dmg_str = f"{magic_min_total}-{magic_max_total}{vers_suffix} + 5 * {magic_stat} bonus {magic_damage_type} damage"
            
            # Add secondary magic damage if present
            if magic_dice2 and weapon.get("magicElement2"):
                magic2_min, magic2_max = parse_dice(magic_dice2)
                magic2_min_total = magic2_min * times
                magic2_max_total = magic2_max * times
                
                # Check versatile variant for secondary magic
                vers_magic2_dice = versatile_attack.get("magicDamageDice2") if versatile_attack else None
                vers2_suffix = ""
                if vers_magic2_dice and vers_magic2_dice != magic_dice2:
                    vers2_times = versatile_attack.get("times", 1)
                    vers2_min, vers2_max = parse_dice(vers_magic2_dice)
                    vers2_min_total = vers2_min * vers2_times
                    vers2_max_total = vers2_max * vers2_times
                    if vers2_min_total != magic2_min_total or vers2_max_total != magic2_max_total:
                        vers2_suffix = f" ({vers2_min_total}-{vers2_max_total})"
                
                magic2_damage_type = get_damage_type_display(weapon.get("magicElement2", ""))
                magic_dmg_str += f" and {magic2_min_total}-{magic2_max_total}{vers2_suffix} {magic2_damage_type} damage"
            
            damage_parts.append(magic_dmg_str)
        
        # Build attack line
        attack_line = f"{attack_name} ({percentage}%): Deals " + " and ".join(damage_parts) + "."
        
        # Add modifiers
        modifiers = []
        
        crit_mod = attack.get("critMod")
        vers_crit_mod = versatile_attack.get("critMod") if versatile_attack else None
        
        if crit_mod or vers_crit_mod:
            # Parse crit mods (could be "+10" or "10")
            crit_val = crit_mod.strip().lstrip("+") if crit_mod else "0"
            vers_crit_val = vers_crit_mod.strip().lstrip("+") if vers_crit_mod else None
            
            if vers_crit_val and vers_crit_val != crit_val:
                modifiers.append(f"a +{crit_val}% (+{vers_crit_val}%) critical chance")
            elif crit_val != "0":
                modifiers.append(f"a +{crit_val}% critical chance")
        
        attack_acc = attack.get("accuracy")
        vers_acc = versatile_attack.get("accuracy") if versatile_attack else None
        
        if attack_acc or vers_acc:
            if vers_acc and vers_acc != attack_acc:
                modifiers.append(f"+{attack_acc or 0} (+{vers_acc}) accuracy")
            elif attack_acc:
                modifiers.append(f"+{attack_acc} accuracy")
        
        attack_mag_acc = attack.get("magicAccuracy")
        vers_mag_acc = versatile_attack.get("magicAccuracy") if versatile_attack else None
        
        if attack_mag_acc or vers_mag_acc:
            if vers_mag_acc and vers_mag_acc != attack_mag_acc:
                modifiers.append(f"+{attack_mag_acc or 0} (+{vers_mag_acc}) magic accuracy")
            elif attack_mag_acc:
                modifiers.append(f"+{attack_mag_acc} magic accuracy")
        
        if modifiers:
            attack_line += " This attack has " + " and ".join(modifiers) + "."
        
        lines.append(attack_line)
        lines.append("")
    
    # Remove trailing blank line
    if lines and lines[-1] == "":
        lines.pop()
    
    return lines


def main():
    """Test the tooltip generator on Falchion of Doom."""
    # Load Falchion of Doom
    project_root = Path(__file__).resolve().parent.parent
    falchion_path = (
        project_root
        / "src"
        / "main"
        / "resources"
        / "data"
        / "items"
        / "weapons"
        / "melee weapons"
        / "slash weapons"
        / "Falchion of Doom.json"
    )
    
    if not falchion_path.exists():
        print(f"Error: Could not find {falchion_path}")
        return
    
    with open(falchion_path, "r", encoding="utf-8") as f:
        weapon = json.load(f)
    
    print("=" * 80)
    print(f"WEAPON: {weapon.get('name', 'Unknown')}")
    print("=" * 80)
    print()
    
    print("CURRENT TOOLTIP (manual):")
    print("-" * 80)
    current_tooltip = weapon.get("tooltip", [])
    if isinstance(current_tooltip, list):
        for line in current_tooltip:
            print(line)
    else:
        print(current_tooltip)
    print()
    
    print("=" * 80)
    print("GENERATED TOOLTIP (automatic):")
    print("-" * 80)
    generated = generate_weapon_tooltip(weapon)
    for line in generated:
        print(line)
    print()
    
    print("=" * 80)
    print("COMPARISON:")
    print("-" * 80)
    if isinstance(current_tooltip, list):
        if current_tooltip == generated:
            print("✓ PERFECT MATCH - Generated tooltip matches manual tooltip exactly!")
        else:
            print("✗ DIFFERENCES FOUND")
            print()
            print(f"  Current lines: {len(current_tooltip)}")
            print(f"  Generated lines: {len(generated)}")
            print()
            
            # Show line-by-line diff
            max_lines = max(len(current_tooltip), len(generated))
            for i in range(max_lines):
                current_line = current_tooltip[i] if i < len(current_tooltip) else "<missing>"
                generated_line = generated[i] if i < len(generated) else "<missing>"
                
                if current_line == generated_line:
                    print(f"  ✓ Line {i+1}: (same)")
                else:
                    print(f"  ✗ Line {i+1}:")
                    print(f"      Current:   {current_line}")
                    print(f"      Generated: {generated_line}")
    # If called with --apply, regenerate tooltips for all weapons in the data tree
    project_root = Path(__file__).resolve().parent.parent
    if '--apply' in sys.argv or '--regenerate' in sys.argv:
        print('\nApplying generated tooltips to all weapon JSON files...')
        data_dir = project_root / 'src' / 'main' / 'resources' / 'data' / 'items' / 'weapons'
        files = list(data_dir.rglob('*.json'))
        updated = []
        for f in files:
            try:
                with open(f, 'r', encoding='utf-8') as fh:
                    obj = json.load(fh)
                if not isinstance(obj, dict):
                    continue
                if obj.get('itemType') != 'WEAPON':
                    continue
                gen = generate_weapon_tooltip(obj)
                if gen:
                    obj['tooltip'] = gen
                    with open(f, 'w', encoding='utf-8') as fh:
                        fh.write(json.dumps(obj, ensure_ascii=False, indent=2) + '\n')
                    updated.append(str(f.relative_to(project_root)))
            except Exception as e:
                print(f'Failed to process {f}: {e}')
        print('\nUpdated tooltips for the following files:')
        for u in updated:
            print(u)
        return


if __name__ == "__main__":
    main()

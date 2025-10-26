# Weapon Tooltip Generator

## Overview

The tooltip generator automatically creates weapon tooltips from the weapon's JSON data, eliminating manual tooltip maintenance and ensuring consistency.

## Usage

### Basic Formatting (No Tooltip Changes)
```powershell
python .\scripts\format_jsons.py
```

### With Tooltip Regeneration
```powershell
python .\scripts\format_jsons.py --regenerate-tooltips
```

### Standalone Tooltip Generator (Test/Verify)
```powershell
python .\scripts\generate_tooltips.py
```
This will show a comparison between current and generated tooltips for Falchion of Doom.

## What It Generates

### Equipment Bonuses
- Critical chance (`crit`)
- Block chance (`block`)
- Dodge chance (`dodge`)
- Magic resistance (`magicResist`)
- Stat bonuses (`stats`) - auto-abbreviated (STRENGTH → STR, etc.)
- Elemental resistances (`resistances`)
- Accuracy and magic accuracy

### Attack Descriptions
- Attack percentages (calculated from weights)
- Damage ranges (parsed from dice notation)
- Physical and magical damage components
- Stat scaling (auto-detects primary stat: STR for melee, DEX for ranged/finesse, INT/CHA for magic)
- Attack modifiers (crit bonuses, accuracy bonuses)

## Features

### Automatic Calculations
- **Dice parsing**: `1d6` → 1-6, `2d6` → 2-12, `1d3+2` → 3-5
- **Multi-hit**: `times` multiplies damage ranges correctly
- **Percentages**: Attack weights auto-calculate to percentages
- **Stat detection**: Weapon class/type determines which stats apply

### Edge Cases Handled
- Finesse weapons (STR or DEX)
- Arcane magic weapons (CHA or INT) 
- Dual-element weapons (physical + magical damage)
- Combined accuracy bonuses (when accuracy == magicAccuracy)
- Multiple stat bonuses from equipment
- Negative resistances (less damage taken)

## Example Output

```
You gain 15% critical chance while wielding this weapon.

You gain +2 STR while wielding this weapon.

Double Slash (40%): Deals 2-12 + 5 * STR bonus slashing damage and 2-8 + 5 * CHA bonus darkness damage.

Big Slash (40%): Deals 2-12 + 5 * STR bonus slashing damage and 1-8 + 5 * CHA bonus darkness damage. This attack has a +10% critical chance.
```

## Benefits

✅ **Single source of truth**: Tooltips reflect actual weapon data  
✅ **No manual maintenance**: Changes to stats auto-update tooltips  
✅ **Consistency**: All weapons use identical formatting  
✅ **Bug detection**: Generator caught incorrect damage calculation in Shadow Slashes  
✅ **Complete coverage**: Includes fields that were missing from manual tooltips (e.g., magicResist)  

## Integration

The tooltip generator is integrated into `format_jsons.py`:
- Optional flag: `--regenerate-tooltips`
- Only affects weapon JSONs (`itemType == "WEAPON"`)
- Runs before JSON transformation/ordering
- Preserves blank-line formatting style

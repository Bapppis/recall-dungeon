# Test Items - ID Range Errors

## Overview
During test development, some test items were created with IDs that fall outside their expected category ranges. This document tracks these errors and provides guidance for fixing them.

## Current Range Violations

### 1. Test Shield (ID 24999)
- **Current ID**: 24999
- **Current Path**: `data/items/armor/shields/TestShield.json`
- **Current Range**: Legwear (24000-25999)
- **Expected Range**: Shields (26000-27999)
- **Recommended Fix**: Change ID to 26999

### 2. Test Boots (ID 26999)
- **Current ID**: 26999
- **Current Path**: `data/items/armor/legwear/TestBoots.json`
- **Current Range**: Shields (26000-27999)
- **Expected Range**: Legwear (24000-25999)
- **Recommended Fix**: Change ID to 24999

## Why These Errors Exist
These test items were created specifically for the comprehensive `ItemEquipmentTest` test suite. The IDs were chosen to be at the end of ranges (using 999) to keep them separate from production items, but the shield and boots IDs were accidentally swapped.

## Impact
- **Functional Impact**: None - the items work correctly in tests
- **Organizational Impact**: IDs don't follow the project's ID convention system
- **Validation Impact**: The `generate_ids.py` script now reports these as violations

## Fix Options

### Option 1: Swap the IDs (Recommended)
Change TestShield from 24999 → 26999 and TestBoots from 26999 → 24999.

**Pros:**
- Follows ID conventions
- Minimal changes needed
- Clears validation errors

**Cons:**
- Requires updating test expectations if they reference IDs directly

### Option 2: Leave As-Is
Keep current IDs and document as known exceptions.

**Pros:**
- No code changes needed
- Tests continue to work

**Cons:**
- Validation script continues to report errors
- Breaks ID convention system

## Recommendation
**Fix the IDs** by swapping them. The test suite uses name-based lookups (`addItemByName("Test Shield")`), so changing IDs won't break tests.

## All Test Items (For Reference)

### Properties
- 2332 — TestBuff — `data/properties/buff/TestBuff.json`
- 3665 — TestDebuff — `data/properties/debuff/TestDebuff.json`
- 4001 — TestTrait1 — `data/properties/trait/TestTrait1.json`
- 4002 — TestTrait2 — `data/properties/trait/TestTrait2.json`
- 4003 — TestTrait3 — `data/properties/trait/TestTrait3.json`
- 4004 — TestTrait4 — `data/properties/trait/TestTrait4.json`
- 4005 — TestTrait5 — `data/properties/trait/TestTrait5.json`

### Creatures
- 5002 — Biggles The Test — `data/creatures/players/BigglesTheTest.json`

### Items
- 21999 — Test Armor — `data/items/armor/armor/Test Armor.json` ✓
- 23999 — Test Helmet — `data/items/armor/helmets/Test Helmet.json` ✓
- **24999 — Test Shield — `data/items/armor/shields/TestShield.json` ⚠️ (Should be 26000-27999)**
- **26999 — Test Boots — `data/items/armor/legwear/TestBoots.json` ⚠️ (Should be 24000-25999)**
- 28998 — Test Buff Potion — `data/items/consumables/potions/TestBuffPotion.json` ✓
- 28999 — Test Healing Potion — `data/items/consumables/potions/TestHealingPotion.json` ✓
- 29999 — Test Sword — `data/items/weapons/melee weapons/slash weapons/TestSword.json` ✓

✓ = Within correct range
⚠️ = Range violation

## Validation
Run the ID validation script to check for violations:

```powershell
python .\scripts\generate_ids.py --fail-on-range-errors --fail-on-duplicates
```

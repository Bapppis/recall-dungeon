package com.bapppis.core.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.ResBuildUp;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.item.Weapon;
import com.bapppis.core.combat.AttackEngine;
import com.bapppis.core.creature.Attack;
import com.bapppis.core.Resistances;

public class SecondaryDamageAndBuildupTest {

  @Test
  public void testMorningstarSecondaryDamage() {
    AllLoaders.loadAll();

    // Create test creatures
    Player attacker = CreatureLoader.getPlayerById(5000); // Biggles
    Creature target = CreatureLoader.getCreatureById(15000); // Goblin
    // Reset templates to a clean state for the test (clear HP, resists, buildup
    // etc.)
    attacker.finalizeAfterLoad();
    target.finalizeAfterLoad();
    for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
      target.setResBuildUpAbsolute(rb, 0);
    }

    // Give Morningstar to attacker and equip it
    Weapon morningstar = (Weapon) ItemLoader.getItemById(30334); // Morningstar
    attacker.addItem(morningstar);
    attacker.equipItem(attacker.getInventory().getWeapons().get(0));

    // Use a deterministic custom attack so the test is not flaky:
    // primary = 1d1 (will roll 1) + stat bonus, secondary = 1d1 (will roll 1)
    Attack atk = new Attack();
    atk.name = "Deterministic Crush";
    atk.times = 1;
    atk.physicalDamageDice = "1d1";
    atk.physicalDamageDice2 = "1d1";
    atk.damageType = Resistances.PIERCING;
    atk.physBuildUpMod = 1.0f; // ensure buildup is applied

    // Ensure target takes full damage (zero dodge/block and normal resistances)
    target.setResistance(Resistances.PIERCING, 100);
    target.setResistance(Resistances.BLUDGEONING, 100);
    target.setDodge(0f);
    target.setBlock(0f);

    // Calculate stat bonus from weapon and apply it
    int statBonus = com.bapppis.core.util.WeaponUtil.determineWeaponStatBonus(attacker, morningstar) * 5;

    int initialHp = target.getCurrentHp();
    AttackEngine.applyAttackToTarget(attacker, atk, statBonus, target, morningstar.getDamageType(),
        morningstar.getMagicElement(), morningstar);

    // Primary (1) + statBonus, secondary (1) -> at least some damage
    assertTrue(target.getCurrentHp() < initialHp,
        "Target should have taken damage from deterministic Morningstar attack");

    // Buildup should be applied to the target for the primary damage type
    assertTrue(target.getResBuildUp(ResBuildUp.PIERCING) > 0,
        "Primary piercing damage should apply piercing buildup to target");
    // Secondary uses no buildup (we didn't set a buildup on secondary)
    assertEquals(0, target.getResBuildUp(ResBuildUp.BLUDGEONING),
        "Secondary bludgeoning damage should NOT apply bludgeoning buildup to target");
  }

  @Test
  public void testPhysicalBuildupApplication() {
    AllLoaders.loadAll();

    Player attacker = CreatureLoader.getPlayerById(5000); // Biggles
    Creature target = CreatureLoader.getCreatureById(15000); // Goblin
    attacker.finalizeAfterLoad();
    target.finalizeAfterLoad();
    for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
      target.setResBuildUpAbsolute(rb, 0);
    }

    // Give Falchion of Doom (pure slashing weapon) to attacker
    attacker.addItem(ItemLoader.getItemById(29000)); // Falchion of Doom
    attacker.equipItem(attacker.getInventory().getWeapons().get(0));

    // Reset any existing buildup on target
    target.setResBuildUpAbsolute(ResBuildUp.SLASHING, 0);

    // Record initial buildup on target
    int initialBuildup = target.getResBuildUp(ResBuildUp.SLASHING);

    // Perform attack
    Weapon falchion = (Weapon) attacker.getEquipped(com.bapppis.core.item.itemEnums.EquipmentSlot.WEAPON);
    int statBonus = com.bapppis.core.util.WeaponUtil.determineWeaponStatBonus(attacker, falchion) * 5;
    AttackEngine.applyAttackToTarget(attacker, falchion.getAttacks().get(0), statBonus, target,
        falchion.getDamageType(), falchion.getMagicElement(), falchion);

    // Verify buildup was applied to target
    assertTrue(target.getResBuildUp(ResBuildUp.SLASHING) > initialBuildup,
        "Physical attack should apply buildup to the target");
  }

  @Test
  public void testSecondaryDamageNoStatBonus() {
    AllLoaders.loadAll();

    Player attacker = CreatureLoader.getPlayerById(5000); // Biggles
    Creature target = CreatureLoader.getCreatureById(15000); // Goblin
    attacker.finalizeAfterLoad();
    target.finalizeAfterLoad();
    for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
      target.setResBuildUpAbsolute(rb, 0);
    }

    // Give Morningstar to attacker
    Weapon morningstar = (Weapon) ItemLoader.getItemById(30334);
    attacker.addItem(morningstar);
    attacker.equipItem(attacker.getInventory().getWeapons().get(0));

    // Set attacker STR to a high value to ensure stat bonus would be significant
    attacker.setStat(com.bapppis.core.creature.creatureEnums.Stats.STRENGTH, 20); // Should give +5 STR bonus

    // Set target resistances to 100 (no reduction) for both damage types to see raw
    // damage
    target.setResistance(com.bapppis.core.Resistances.PIERCING, 100);
    target.setResistance(com.bapppis.core.Resistances.BLUDGEONING, 100);

    // Use a deterministic attack so we can assert exact values: 1d1 primary, 1d1
    // secondary
    Attack atk = new Attack();
    atk.name = "Deterministic DPS";
    atk.times = 1;
    atk.physicalDamageDice = "1d1";
    atk.physicalDamageDice2 = "1d1";
    atk.damageType = Resistances.PIERCING;

    // Set STR high and compute stat bonus
    attacker.setStat(com.bapppis.core.creature.creatureEnums.Stats.STRENGTH, 20);
    int statBonus = com.bapppis.core.util.WeaponUtil.determineWeaponStatBonus(attacker, morningstar) * 5;

    // Ensure target is easy to hit and takes raw damage
    target.setResistance(Resistances.PIERCING, 100);
    target.setResistance(Resistances.BLUDGEONING, 100);
    target.setDodge(0f);
    target.setBlock(0f);
    // Ensure high accuracy so toHit always beats avoid
    attacker.modifyBaseAccuracy(100);
    // Set attacker crit to -10 to cancel the Morningstar's +10 crit bonus and make
    // damage deterministic
    attacker.setCrit(-10f);

    // Use attack listener to capture damage from report
    java.util.concurrent.atomic.AtomicInteger capturedPrimaryDmg = new java.util.concurrent.atomic.AtomicInteger(0);
    java.util.concurrent.atomic.AtomicInteger capturedSecondaryDmg = new java.util.concurrent.atomic.AtomicInteger(0);

    for (int i = 0; i < 5; i++) {
      target.setCurrentHp(target.getMaxHp());
      capturedPrimaryDmg.set(0);
      capturedSecondaryDmg.set(0);

      AttackEngine.attackListener = (rpt) -> {
        capturedPrimaryDmg.set(rpt.physAfter);
        capturedSecondaryDmg.set(rpt.phys2After);
      };
      AttackEngine.applyAttackToTarget(attacker, atk, statBonus, target, Resistances.PIERCING, null, morningstar);
      AttackEngine.attackListener = null;

      // Primary: 1d1 (rolls 1) + statBonus = 51 (no crit)
      // Secondary: 1d1 (rolls 1) = 1 (no crit, no stat bonus)
      assertEquals(statBonus + 1, capturedPrimaryDmg.get(),
          "Primary damage should be statBonus + 1 (1d1+statBonus with no crit)");
      assertEquals(1, capturedSecondaryDmg.get(),
          "Secondary damage should be 1 (1d1 with no stat bonus and no crit)");
    }
  }

  @Test
  public void testSecondaryDamageResistanceApplication() {
    AllLoaders.loadAll();

    Player attacker = CreatureLoader.getPlayerById(5000);
    Creature target = CreatureLoader.getCreatureById(15000);
    attacker.finalizeAfterLoad();
    target.finalizeAfterLoad();
    for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
      target.setResBuildUpAbsolute(rb, 0);
    }

    // Give Morningstar to attacker
    Weapon morningstar = (Weapon) ItemLoader.getItemById(30334);
    attacker.addItem(morningstar);
    attacker.equipItem(attacker.getInventory().getWeapons().get(0));

    // Set different resistances for primary and secondary damage types
    target.setResistance(com.bapppis.core.Resistances.PIERCING, 100); // Normal piercing
    target.setResistance(com.bapppis.core.Resistances.BLUDGEONING, 50); // Half bludgeoning

    // Deterministic attack: primary 1d1, secondary 1d1
    Attack atk = new Attack();
    atk.name = "Deterministic Resist Test";
    atk.times = 1;
    atk.physicalDamageDice = "1d1";
    atk.physicalDamageDice2 = "1d1";
    atk.damageType = Resistances.PIERCING;

    int statBonus = com.bapppis.core.util.WeaponUtil.determineWeaponStatBonus(attacker, morningstar) * 5;

    int initialHp = target.getCurrentHp();
    // Ensure hits by giving attacker high accuracy
    attacker.modifyBaseAccuracy(100);
    AttackEngine.applyAttackToTarget(attacker, atk, statBonus, target, Resistances.PIERCING, null, morningstar);
    int damageTaken = initialHp - target.getCurrentHp();

    assertTrue(damageTaken > 0, "Target should take damage with mixed resistances (deterministic)");
  }

  @Test
  public void testCritMirroringSecondaryDamage() {
    AllLoaders.loadAll();

    Player attacker = CreatureLoader.getPlayerById(5000);
    Creature target = CreatureLoader.getCreatureById(15000);
    attacker.finalizeAfterLoad();
    target.finalizeAfterLoad();
    for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
      target.setResBuildUpAbsolute(rb, 0);
    }

    // Give Morningstar to attacker
    Weapon morningstar = (Weapon) ItemLoader.getItemById(30334);
    attacker.addItem(morningstar);
    attacker.equipItem(attacker.getInventory().getWeapons().get(0));

    // Make a deterministic attack (1d1 primary, 1d1 secondary), force a crit and
    // ensure both parts crit
    target.setResistance(com.bapppis.core.Resistances.PIERCING, 100);
    target.setResistance(com.bapppis.core.Resistances.BLUDGEONING, 100);
    target.setDodge(0f);
    target.setBlock(0f);

    Attack atk = new Attack();
    atk.name = "Deterministic Crit";
    atk.times = 1;
    atk.physicalDamageDice = "1d1";
    atk.physicalDamageDice2 = "1d1";
    atk.damageType = Resistances.PIERCING;

    // Force attacker to always crit and always hit
    attacker.setCrit(100f);
    attacker.modifyBaseAccuracy(100);

    int statBonus = 0; // make calculation simple
    target.setCurrentHp(target.getMaxHp());

    // Use attack listener to capture damage from report
    java.util.concurrent.atomic.AtomicInteger capturedPrimaryDmg = new java.util.concurrent.atomic.AtomicInteger(0);
    java.util.concurrent.atomic.AtomicInteger capturedSecondaryDmg = new java.util.concurrent.atomic.AtomicInteger(0);
    java.util.concurrent.atomic.AtomicInteger capturedCritCount = new java.util.concurrent.atomic.AtomicInteger(0);

    AttackEngine.attackListener = (rpt) -> {
      capturedPrimaryDmg.set(rpt.physAfter);
      capturedSecondaryDmg.set(rpt.phys2After);
      capturedCritCount.set(rpt.physCritCount);
    };
    AttackEngine.applyAttackToTarget(attacker, atk, statBonus, target, Resistances.PIERCING, null, morningstar);
    AttackEngine.attackListener = null;

    // Expect both primary (1) and secondary (1) to crit -> doubled each: (1*2) +
    // (1*2) = 4
    // Primary: 1d1 with crit = 2
    // Secondary: 1d1 with crit = 2
    assertEquals(1, capturedCritCount.get(), "Should have exactly 1 crit");
    assertEquals(2, capturedPrimaryDmg.get(), "Primary damage with crit should be 2 (1*2)");
    assertEquals(2, capturedSecondaryDmg.get(), "Secondary damage with crit should be 2 (1*2, mirroring primary crit)");
  }
}
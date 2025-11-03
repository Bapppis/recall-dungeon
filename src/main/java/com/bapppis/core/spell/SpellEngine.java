package com.bapppis.core.spell;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.util.Dice;
import com.bapppis.core.util.ResistanceUtil;

import java.util.Random;

/**
 * Handles spell casting mechanics, including damage calculation, property
 * application,
 * and buff application. Spells roll against dodge and magic resist similar to
 * magic attacks.
 */
public class SpellEngine {
  private static final Random rng = new Random();

  /**
   * Cast a spell from caster to target.
   * Buff-only spells apply immediately to caster without rolling.
   * Damage/debuff spells roll against target's dodge and magic resist.
   * 
   * @param caster The creature casting the spell
   * @param spell  The spell being cast
   * @param target The target creature (can be null for buff-only spells)
   * @return true if spell was successfully cast
   */
  public static boolean castSpell(Creature caster, Spell spell, Creature target) {
    if (spell == null || caster == null) {
      return false;
    }

    // Check mana and stamina costs (spells may use mana, stamina or both)
    if (spell.getManaCost() > 0 && caster.getCurrentMana() < spell.getManaCost()) {
      return false;
    }
    if (spell.getStaminaCost() > 0 && caster.getCurrentStamina() < spell.getStaminaCost()) {
      return false;
    }

    // Deduct costs (only if > 0)
    if (spell.getManaCost() > 0) {
      caster.modifyMana(-spell.getManaCost());
    }
    if (spell.getStaminaCost() > 0) {
      caster.modifyStamina(-spell.getStaminaCost());
    }

    // Handle buff-only spells (no target needed)
    if (spell.isBuffOnly()) {
      if (spell.buffProperty != null && !spell.buffProperty.isBlank()) {
        caster.addProperty(spell.buffProperty);
      }
      return true;
    }

    // Damage/debuff spells require a target
    if (target == null) {
      return false;
    }

    // Get best stat bonus from statBonuses list
    int statBonus = spell.getBestStatBonus(caster) * 5;

    // Track if at least one damage component hit
    boolean anyDamageHit = false;
    int totalDamage = 0;

    // Process up to 4 damage components
    if (spell.damageDice != null && !spell.damageDice.isBlank() && spell.damageType != null) {
      int damage = rollAndApplySpellDamage(caster, spell, target, spell.damageDice, spell.damageType,
          statBonus, spell.getBuildUpMod(), 1);
      if (damage > 0) {
        anyDamageHit = true;
        totalDamage += damage;
      }
    }

    if (spell.damageDice2 != null && !spell.damageDice2.isBlank() && spell.damageType2 != null) {
      int damage = rollAndApplySpellDamage(caster, spell, target, spell.damageDice2, spell.damageType2,
          statBonus, spell.getBuildUpMod2(), 2);
      if (damage > 0) {
        anyDamageHit = true;
        totalDamage += damage;
      }
    }

    if (spell.damageDice3 != null && !spell.damageDice3.isBlank() && spell.damageType3 != null) {
      int damage = rollAndApplySpellDamage(caster, spell, target, spell.damageDice3, spell.damageType3,
          statBonus, spell.getBuildUpMod3(), 3);
      if (damage > 0) {
        anyDamageHit = true;
        totalDamage += damage;
      }
    }

    if (spell.damageDice4 != null && !spell.damageDice4.isBlank() && spell.damageType4 != null) {
      int damage = rollAndApplySpellDamage(caster, spell, target, spell.damageDice4, spell.damageType4,
          statBonus, spell.getBuildUpMod4(), 4);
      if (damage > 0) {
        anyDamageHit = true;
        totalDamage += damage;
      }
    }

    // Apply onHitProperty if at least one damage hit
    if (anyDamageHit && spell.onHitProperty != null && !spell.onHitProperty.isBlank()) {
      // Roll to apply property (similar to weapon property-on-hit)
      float rawProp = rng.nextFloat() * 100f;
      int propToHit = Math.round(rawProp) + statBonus;

      // Check if property hits against dodge and magic resist
      boolean propHit = true;
      float dodgeRoll = rng.nextFloat() * 100f;
      if (dodgeRoll < target.getDodge()) {
        propHit = false;
      }
      if (propHit) {
        float resistRoll = rng.nextFloat() * 100f;
        if (resistRoll < target.getMagicResist()) {
          propHit = false;
        }
      }

      if (propHit) {
        target.addProperty(spell.onHitProperty);
      }
    }

    return true;
  }

  /**
   * Roll damage for one spell damage component and apply it to target.
   * Rolls against dodge and magic resist like magic attacks.
   */
  private static int rollAndApplySpellDamage(Creature caster, Spell spell, Creature target,
      String damageDice, Resistances damageType,
      int statBonus, float buildUpMod, int componentIndex) {
    int totalDamage = 0;
    int times = spell.getTimes();
    float damageMult = spell.getDamageMult();
    int critMod = spell.getCritMod();
    int accuracy = spell.getAccuracy();

    for (int i = 0; i < times; i++) {
      // Roll to hit against dodge and magic resist
      float rawRoll = rng.nextFloat() * 100f;
      int toHit = Math.round(rawRoll) + statBonus + accuracy;

      boolean hit = true;

      // Check dodge
      float dodgeRoll = rng.nextFloat() * 100f;
      if (dodgeRoll < target.getDodge()) {
        hit = false;
      }

      // Check magic resist if not dodged
      if (hit) {
        float resistRoll = rng.nextFloat() * 100f;
        if (resistRoll < target.getMagicResist()) {
          hit = false;
        }
      }

      if (hit) {
        // Roll damage
        int baseDamage = Dice.roll(damageDice);
        int damageWithStat = Math.round((baseDamage + statBonus) * damageMult);

        // Check for crit
        boolean isCrit = false;
        float critRoll = rng.nextFloat() * 100f;
        float critChance = caster.getCrit() + critMod;
        if (critRoll < critChance) {
          isCrit = true;
          damageWithStat *= 2;
        }

        // Apply resistance using ResistanceUtil
        int finalDamage = ResistanceUtil.getDamageAfterResistance(target, damageWithStat, damageType);

        // Apply damage to target
        target.modifyHp(-finalDamage);
        totalDamage += finalDamage;

        // Apply buildup using ResistanceUtil
        try {
          ResistanceUtil.addBuildUp(target, damageType, buildUpMod);
        } catch (Exception ignored) {
        }
      }
    }

    return totalDamage;
  }
}

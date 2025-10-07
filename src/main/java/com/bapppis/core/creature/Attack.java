package com.bapppis.core.creature;
import com.bapppis.core.util.Dice;

public class Attack {

  public String name;
  public Integer times;
  public String physicalDamageDice;
  public String magicDamageDice;
  // Optional override for the magic damage resistance type (e.g. DARKNESS, FIRE)
  public String magicDamageType;
  public float damageMultiplier;
  public float magicDamageMultiplier;
  public String damageType;
  public Integer weight;
  // Optional per-attack crit modifier, can be written in JSON as "+5" or "-3" (string)
  public String critMod;

  public int getTimes() {
    return times == null ? 1 : times;
  }

  public int getWeight() {
    return weight == null ? 1 : weight;
  }

  /**
   * Returns the per-attack crit modifier as an integer. Accepts formats like "+5", "-2", or "3".
   */
  public int getCritMod() {
    if (critMod == null || critMod.isBlank())
      return 0;
    String s = critMod.trim();
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      // Be defensive: strip leading '+' if present then try again
      if (s.startsWith("+")) {
        try {
          return Integer.parseInt(s.substring(1));
        } catch (NumberFormatException ex) {
          return 0;
        }
      }
      return 0;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Attack[");
    if (name != null) sb.append("name='").append(name).append("', ");
    sb.append("times=").append(getTimes()).append(", ");
    if (physicalDamageDice != null && !physicalDamageDice.isBlank()) sb.append("phys='").append(physicalDamageDice).append("', ");
    if (damageMultiplier != 0) sb.append("physMultiplier=").append(damageMultiplier).append(", ");
    if (magicDamageDice != null && !magicDamageDice.isBlank()) sb.append("magic='").append(magicDamageDice).append("', ");
    if (magicDamageType != null && !magicDamageType.isBlank()) sb.append("magicType=").append(magicDamageType).append(", ");
    if (magicDamageMultiplier != 0) sb.append("magicStatBonus=").append(magicDamageMultiplier).append(", ");
    if (damageType != null) sb.append("damageType=").append(damageType).append(", ");
    if (critMod != null && !critMod.isBlank()) sb.append("critMod=").append(critMod).append(", ");
    sb.append("weight=").append(getWeight());
    sb.append("]");
    return sb.toString();
  }



  /**
   * Roll the physical component of this attack, including stat bonus per hit.
   * statBonus is added per-hit (caller decides meaning: STR/DEX/INT depending on context).
   */
  public int rollPhysicalDamage(int statBonus) {
    int total = 0;
    for (int i = 0; i < getTimes(); i++) {
      if (physicalDamageDice != null && !physicalDamageDice.isBlank()) {
        total += Dice.roll(physicalDamageDice);
      }
      total += Math.max(0, statBonus);
    }
    return total;
  }

  /**
   * Roll the magic component of this attack.
   */
  public int rollMagicDamage() {
    int total = 0;
    for (int i = 0; i < getTimes(); i++) {
      if (magicDamageDice != null && !magicDamageDice.isBlank()) {
        total += Dice.roll(magicDamageDice);
      }
    }
    return total;
  }

  // Description is now manually authored in JSON and surfaced directly.

}

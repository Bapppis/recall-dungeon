package com.bapppis.core.creature;
import com.bapppis.core.util.Dice;

public class Attack {

  public String name;
  public Integer times;
  public String physicalDamageDice;
  public String magicDamageDice;
  public Double critChance;
  public String damageType;
  public Integer weight;

  public int getTimes() {
    return times == null ? 1 : times;
  }

  public int getWeight() {
    return weight == null ? 1 : weight;
  }

  public double getCritChance() {
    return critChance == null ? 0.0 : critChance;
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

}

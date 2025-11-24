package com.bapppis.core.combat;

import com.bapppis.core.creature.Attack;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.Resistances;
import com.bapppis.core.item.Weapon;
import com.bapppis.core.util.Dice;
import com.bapppis.core.util.ResistanceUtil;

public final class AttackEngine {

    public static java.util.function.Consumer<AttackReport> attackListener = null;

    private AttackEngine() {
    }

    public static class AttackReport {
        public String attackName;
        public int physRaw;
        public int magRaw;
        public int physAfterCritBeforeResist;
        public int physAfter;
        public int magAfter;
        public int phys2After; // Secondary physical damage
        public int mag2After; // Secondary magic damage
        public int totalDamageDealt; // Total damage dealt (physAfter + magAfter + phys2After + mag2After)
        public int times;
        public String damageType;
        public String magicType;
        public int critCount;
        public boolean isCrit;
        public Creature attacker;
        public Creature target;
        public int magicStatBonus;
        public int magicStatExtra;
        public float magicDamageMultiplier;
        public int physStatBase;
        public int physStatExtra;
        public float physDamageMultiplier;
        public String magicStatChosen;
        public String physStatChosen;
        public int physCritCount;
        public int magicCritCount;
        public int physAttempts;
        public int physMissDodge;
        public int physMissBlock;
        public int physPropertyAttempted;
        public int physPropertyApplied;
        public int magPropertyAttempted;
        public int magPropertyApplied;
        public String physPropertyName;
        public String magPropertyName;
        public int magicAttempts;
        public int magicMissDodge;
        public int magicMissResist;
        public boolean dualRoll;
        public boolean trueDamage;
    }

    public static void applyAttackToTarget(Creature attacker, Attack attack, int statBonus, Creature target,
            Resistances physicalType, Resistances magicType, Weapon weapon) {
        applyAttackToTarget(attacker, attack, statBonus, target, physicalType, magicType, weapon,
                new DefaultRandomProvider());
    }

    public static void applyAttackToTarget(Creature attacker, Attack attack, int statBonus, Creature target,
            Resistances physicalType, Resistances magicType, Weapon weapon, RandomProvider rng) {
        if (attack == null || target == null || attacker == null)
            return;

        int totalPhysBeforeResist = 0;
        int physRaw = 0;
        int physCritCount = 0;
        int physAfter = 0;
        int physAttempts = 0;
        int physMissDodge = 0;
        int physMissBlock = 0;
        int phys2After = 0; // Secondary physical damage
        int physPropertyAttempted = 0;
        int physPropertyApplied = 0;
        boolean[] physHitCrits = new boolean[100]; // Track which hits crit (max 100 hits per attack)
        int times = attack.getTimes();
        float baseCrit = attacker.getCrit();
        int critMod = 0;
        try {
            critMod = attack.getCritMod();
        } catch (Exception ignored) {
        }
        float critChance = Math.max(0f, Math.min(100f, baseCrit + critMod));

        boolean hasPhysical = physicalType != null
                && ResistanceUtil.classify(physicalType) == ResistanceUtil.Kind.PHYSICAL;
        boolean isTrue = physicalType != null && ResistanceUtil.classify(physicalType) == ResistanceUtil.Kind.TRUE;
        if (hasPhysical || isTrue) {
            int successfulHitIndex = 0;
            for (int i = 0; i < times; i++) {
                physAttempts++;
                float rawRoll = rng.nextFloat() * 100f;
                int toHit = Math.round(rawRoll) + statBonus + attacker.getAccuracy() + attack.getAccuracy();
                float effectiveDodge = Math.max(0f, Math.min(100f, target.getDodge()));
                float effectiveBlock = Math.max(0f, Math.min(100f, target.getBlock()));
                if (isTrue) {
                    if (toHit <= effectiveDodge) {
                        physMissDodge++;
                        continue;
                    }
                } else {
                    float totalAvoid = Math.min(100f, effectiveDodge + effectiveBlock);
                    if (toHit <= totalAvoid) {
                        if (effectiveDodge >= effectiveBlock) {
                            if (toHit <= effectiveBlock) {
                                // System.out.println("Missed (block): " + attacker.getName() + " -> " +
                                // target.getName());
                                physMissBlock++;
                                continue;
                            } else {
                                // System.out.println("Missed (dodge): " + attacker.getName() + " -> " +
                                // target.getName());
                                physMissDodge++;
                                continue;
                            }
                        } else {
                            if (toHit <= effectiveDodge) {
                                // System.out.println("Missed (dodge): " + attacker.getName() + " -> " +
                                // target.getName());
                                physMissDodge++;
                                continue;
                            } else {
                                // System.out.println("Missed (block): " + attacker.getName() + " -> " +
                                // target.getName());
                                physMissBlock++;
                                continue;
                            }
                        }
                    }
                }
                int hit = 0;
                if (attack.physicalDamageDice != null && !attack.physicalDamageDice.isBlank()) {
                    hit = Dice.roll(attack.physicalDamageDice) * 5;
                }
                // Stat bonus will be added per-hit with multiplier
                int statBonusForHit = (int) Math.floor(statBonus * 5.0 * Math.max(0.0, attack.damageMultiplier));
                hit += statBonusForHit;
                physRaw += hit;
                float effectiveCrit = critChance;
                boolean crit = rng.nextFloat() < (effectiveCrit / 100f);
                if (crit) {
                    physCritCount++;
                    hit *= 2;
                }
                physHitCrits[successfulHitIndex] = crit;
                successfulHitIndex++;
                totalPhysBeforeResist += hit;
                // For every successful physical hit, add build-up to the target using the
                // attack's damageType and the physical build-up modifier.
                try {
                    ResistanceUtil.addBuildUp(target, attack.getDamageTypeEnum(), attack.getPhysBuildUpMod());
                } catch (Exception ignored) {
                }
            }
            // No additional stat bonus needed - already applied per-hit
            // Use ResistanceUtil to calculate damage after resistance (handles nulls
            // safely)
            physAfter = ResistanceUtil.getDamageAfterResistance(target, totalPhysBeforeResist, physicalType);
            if (physAfter > 0) {
                target.modifyHp(-physAfter);
            }

            // Calculate successful primary hits once for both secondary damage and property application
            int successfulPrimaryHits = physAttempts - physMissDodge - physMissBlock;

            // Apply secondary physical damage if present (weapon.damageType2 and
            // attack.physicalDamageDice2)
            // Secondary damage: no stat bonus, no buildup, applies only when primary hits
            // Secondary damage crits when the corresponding primary hit crit
            if (weapon != null && weapon.getDamageType2() != null
                    && attack.physicalDamageDice2 != null && !attack.physicalDamageDice2.isBlank()) {
                int totalPhys2BeforeResist = 0;
                for (int i = 0; i < successfulPrimaryHits; i++) {
                    int hit2 = Dice.roll(attack.physicalDamageDice2) * 5;
                    // Apply crit if the corresponding primary hit crit
                    if (physHitCrits[i]) {
                        hit2 *= 2;
                    }
                    totalPhys2BeforeResist += hit2;
                }
                phys2After = ResistanceUtil.getDamageAfterResistance(target, totalPhys2BeforeResist,
                        weapon.getDamageType2());
                if (phys2After > 0) {
                    target.modifyHp(-phys2After);
                }
            }

            // Attempt physical-on-hit property once per attack (if configured and primary
            // hit occurred)
            try {
                if (attack.physicalOnHitProperty != null && !attack.physicalOnHitProperty.isBlank()
                        && successfulPrimaryHits > 0) {
                    float rawProp = rng.nextFloat() * 100f;
                    // Use statBonus only. Do NOT add accuracy or attack accuracy.
                    int propToHit = Math.round(rawProp) + statBonus;
                    boolean propHit = true;
                    if (isTrue) {
                        if (propToHit <= Math.max(0f, Math.min(100f, target.getDodge()))) {
                            propHit = false;
                        }
                    } else {
                        float effectiveDodge = Math.max(0f, Math.min(100f, target.getDodge()));
                        float effectiveBlock = Math.max(0f, Math.min(100f, target.getBlock()));
                        float totalAvoid = Math.min(100f, effectiveDodge + effectiveBlock);
                        if (propToHit <= totalAvoid) {
                            propHit = false;
                        }
                    }
                    physPropertyAttempted = 1;
                    if (propHit) {
                        try {
                            boolean added = target.addProperty(attack.physicalOnHitProperty);
                            if (added)
                                physPropertyApplied = 1;
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        int magRaw = 0;
        int magAfter = 0;
        int magicCritCount = 0;
        int magicBeforeResist = 0;
        int magicStatBonus = 0;
        int magicStatExtra = 0;
        int mag2After = 0; // Secondary magic damage
        int magPropertyAttempted = 0;
        int magPropertyApplied = 0;
        boolean[] magicHitCrits = new boolean[100]; // Track which magic hits crit (max 100 hits per attack)
        float magicMult = attack.magicDamageMultiplier;
        String magicStatChosenName = null;
        int magicAttempts = 0;
        int magicMissDodge = 0;
        int magicMissResist = 0;
        boolean hasMagicComponent = (weapon != null && weapon.getMagicElement() != null)
                || (magicType != null && attack.magicDamageDice != null && !attack.magicDamageDice.isBlank());
        if (hasMagicComponent && magicType != null) {
            try {
                if (weapon != null) {
                    com.bapppis.core.creature.creatureEnums.Stats chosen = null;
                    int best = Integer.MIN_VALUE;
                    if (weapon.getMagicStatBonuses() != null && !weapon.getMagicStatBonuses().isEmpty()) {
                        for (com.bapppis.core.creature.creatureEnums.Stats s : weapon.getMagicStatBonuses()) {
                            int b = attacker.getStatBonus(s);
                            if (b > best) {
                                best = b;
                                chosen = s;
                            }
                        }
                    } else if (weapon.getMagicStatBonus() != null) {
                        chosen = weapon.getMagicStatBonus();
                        best = attacker.getStatBonus(chosen);
                    }
                    if (chosen != null) {
                        magicStatBonus = best;
                        magicStatExtra = 0; // Stat bonus applied per-hit now
                        magicStatChosenName = chosen.name();
                    }
                } else {
                    magicStatBonus = attacker.getStatBonus(com.bapppis.core.creature.creatureEnums.Stats.INTELLIGENCE);
                }
            } catch (Exception ignored) {
            }

            int magicToHitBonus = Math.max(0, magicStatBonus) * 5;

            int magicTimes = attack.getTimes();
            int successfulMagicHitIndex = 0;
            for (int i = 0; i < magicTimes; i++) {
                magicAttempts++;
                float rawRoll = rng.nextFloat() * 100f;
                int toHit = Math.round(rawRoll) + magicToHitBonus + attacker.getMagicAccuracy()
                        + attack.getMagicAccuracy();
                float effectiveDodge = Math.max(0f, Math.min(100f, target.getDodge()));
                float effectiveMagicResist = Math.max(0f, Math.min(100f, target.getMagicResist()));
                float totalAvoid = Math.min(100f, effectiveDodge + effectiveMagicResist);
                if (toHit <= totalAvoid) {
                    if (effectiveDodge >= effectiveMagicResist) {
                        if (toHit <= effectiveMagicResist) {
                            // System.out
                            // .println("Missed (magicResist): " + attacker.getName() + " -> " +
                            // target.getName());
                            magicMissResist++;
                        } else {
                            // System.out
                            // .println("Missed (dodge magic): " + attacker.getName() + " -> " +
                            // target.getName());
                            magicMissDodge++;
                        }
                    } else {
                        if (toHit <= effectiveDodge) {
                            // System.out
                            // .println("Missed (dodge magic): " + attacker.getName() + " -> " +
                            // target.getName());
                            magicMissDodge++;
                        } else {
                            // System.out
                            // .println("Missed (magicResist): " + attacker.getName() + " -> " +
                            // target.getName());
                            magicMissResist++;
                        }
                    }
                    continue;
                }
                int hit = 0;
                if (attack.magicDamageDice != null && !attack.magicDamageDice.isBlank()) {
                    hit = Dice.roll(attack.magicDamageDice) * 5;
                }
                // Add stat bonus per-hit with multiplier
                int magicStatBonusForHit = (int) Math.floor(magicStatBonus * 5.0 * Math.max(0.0, magicMult));
                hit += magicStatBonusForHit;
                magRaw += hit;
                float effectiveCrit = critChance;
                boolean crit = rng.nextFloat() < (effectiveCrit / 100f);
                if (crit) {
                    magicCritCount++;
                    hit *= 2;
                }
                magicHitCrits[successfulMagicHitIndex] = crit;
                successfulMagicHitIndex++;
                magicBeforeResist += hit;
                // For every successful magic hit, add build-up to the target. Prefer the
                // weapon's magic element when available; otherwise fall back to the
                // attack's magic damage type. Use the attack's magic build-up modifier.
                try {
                    com.bapppis.core.Resistances buildRes = null;
                    if (weapon != null && weapon.getMagicElement() != null) {
                        buildRes = weapon.getMagicElement();
                    } else {
                        buildRes = attack.getMagicDamageTypeEnum();
                    }
                    ResistanceUtil.addBuildUp(target, buildRes, attack.getMagicBuildUpMod());
                } catch (Exception ignored) {
                }
            }

            // Use ResistanceUtil for magic damage as well. This keeps logic centralized.
            magAfter = ResistanceUtil.getDamageAfterResistance(target, magicBeforeResist, magicType);
            if (magAfter > 0) {
                target.modifyHp(-magAfter);
            }

            // Calculate successful magic hits once for both secondary damage and property application
            int successfulMagicHits = magicAttempts - magicMissDodge - magicMissResist;

            // Apply secondary magic damage if present (weapon.magicElement2 and
            // attack.magicDamageDice2)
            // Secondary magic damage: no stat bonus, no buildup, applies only when primary
            // magic hits
            // Secondary magic damage crits when the corresponding primary magic hit crit
            if (weapon != null && weapon.getMagicElement2() != null
                    && attack.magicDamageDice2 != null && !attack.magicDamageDice2.isBlank()) {
                int totalMag2BeforeResist = 0;
                for (int i = 0; i < successfulMagicHits; i++) {
                    int hit2 = Dice.roll(attack.magicDamageDice2) * 5; // Option 2: Scale by 5
                    // Apply crit if the corresponding primary magic hit crit
                    if (magicHitCrits[i]) {
                        hit2 *= 2;
                    }
                    totalMag2BeforeResist += hit2;
                }
                mag2After = ResistanceUtil.getDamageAfterResistance(target, totalMag2BeforeResist,
                        weapon.getMagicElement2());
                if (mag2After > 0) {
                    target.modifyHp(-mag2After);
                }
            }

            // Attempt magic-on-hit property once per attack (if configured and primary
            // magic hit occurred)
            try {
                if (attack.magicOnHitProperty != null && !attack.magicOnHitProperty.isBlank()
                        && successfulMagicHits > 0) {
                    float rawProp = rng.nextFloat() * 100f;
                    // Use magicToHitBonus only. Do NOT add magicAccuracy or attack.magicAccuracy.
                    int propToHit = Math.round(rawProp) + Math.max(0, magicStatBonus) * 5;
                    boolean propHit = true;
                    float effectiveDodge = Math.max(0f, Math.min(100f, target.getDodge()));
                    float effectiveMagicResist = Math.max(0f, Math.min(100f, target.getMagicResist()));
                    float totalAvoid = Math.min(100f, effectiveDodge + effectiveMagicResist);
                    if (propToHit <= totalAvoid) {
                        propHit = false;
                    }
                    magPropertyAttempted = 1;
                    if (propHit) {
                        try {
                            boolean added = target.addProperty(attack.magicOnHitProperty);
                            if (added)
                                magPropertyApplied = 1;
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        try {
            if (AttackEngine.attackListener != null) {
                AttackReport rpt = new AttackReport();
                rpt.attackName = attack.name;
                rpt.physRaw = physRaw;
                rpt.magRaw = magRaw;
                rpt.physAfterCritBeforeResist = totalPhysBeforeResist;
                rpt.physAfter = physAfter;
                rpt.magAfter = magAfter;
                rpt.phys2After = phys2After; // Include secondary physical damage
                rpt.mag2After = mag2After; // Include secondary magic damage
                rpt.totalDamageDealt = physAfter + magAfter + phys2After + mag2After;
                rpt.times = attack.getTimes();
                rpt.damageType = (physicalType == null ? null : physicalType.name());
                rpt.magicType = (magicType == null ? null : magicType.name());
                rpt.critCount = physCritCount + magicCritCount;
                rpt.isCrit = (physCritCount + magicCritCount) > 0;
                rpt.physCritCount = physCritCount;
                rpt.magicCritCount = magicCritCount;
                rpt.physAttempts = physAttempts;
                rpt.physMissDodge = physMissDodge;
                rpt.physMissBlock = physMissBlock;
                rpt.magicAttempts = magicAttempts;
                rpt.magicMissDodge = magicMissDodge;
                rpt.magicMissResist = magicMissResist;
                rpt.dualRoll = hasPhysical && hasMagicComponent;
                rpt.trueDamage = isTrue;
                rpt.attacker = attacker;
                rpt.target = target;
                rpt.magicStatBonus = magicStatBonus;
                rpt.magicStatExtra = magicStatExtra;
                rpt.magicDamageMultiplier = magicMult;
                rpt.magicStatChosen = magicStatChosenName;
                rpt.physStatBase = 0;
                rpt.physStatExtra = 0;
                rpt.physDamageMultiplier = attack.damageMultiplier;
                rpt.physStatChosen = null;
                rpt.physPropertyAttempted = physPropertyAttempted;
                rpt.physPropertyApplied = physPropertyApplied;
                rpt.magPropertyAttempted = magPropertyAttempted;
                rpt.magPropertyApplied = magPropertyApplied;
                rpt.physPropertyName = attack.physicalOnHitProperty;
                rpt.magPropertyName = attack.magicOnHitProperty;
                AttackEngine.attackListener.accept(rpt);
            }
        } catch (Exception ignored) {
        }

        if (hasPhysical || isTrue) {
            // System.out.println("Attack: " + attack.name + " Physical After: " + physAfter
            // + (magAfter > 0 ? (", Magic After: " + magAfter) : ""));
        } else if (magAfter > 0) {
            // System.out.println("Attack: " + attack.name + " Magic After: " + magAfter);
        }
    }

}

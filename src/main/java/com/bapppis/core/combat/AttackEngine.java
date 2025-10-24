package com.bapppis.core.combat;

import com.bapppis.core.creature.Attack;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.Resistances;
import com.bapppis.core.item.Weapon;
import com.bapppis.core.util.Dice;
import com.bapppis.core.util.ResistanceUtil;
import com.bapppis.core.util.WeaponUtil;

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
                                // System.out.println("Missed (block): " + attacker.getName() + " -> " + target.getName());
                                physMissBlock++;
                                continue;
                            } else {
                                // System.out.println("Missed (dodge): " + attacker.getName() + " -> " + target.getName());
                                physMissDodge++;
                                continue;
                            }
                        } else {
                            if (toHit <= effectiveDodge) {
                                // System.out.println("Missed (dodge): " + attacker.getName() + " -> " + target.getName());
                                physMissDodge++;
                                continue;
                            } else {
                                // System.out.println("Missed (block): " + attacker.getName() + " -> " + target.getName());
                                physMissBlock++;
                                continue;
                            }
                        }
                    }
                }
                int hit = 0;
                if (attack.physicalDamageDice != null && !attack.physicalDamageDice.isBlank()) {
                    hit = Dice.roll(attack.physicalDamageDice);
                }
                hit += Math.max(0, statBonus);
                physRaw += hit;
                float effectiveCrit = critChance;
                boolean crit = rng.nextFloat() < (effectiveCrit / 100f);
                if (crit) {
                    physCritCount++;
                    hit *= 2;
                }
                totalPhysBeforeResist += hit;
                // For every successful physical hit, add build-up to the target using the
                // attack's damageType and the physical build-up modifier.
                try {
                    ResistanceUtil.addBuildUp(target, attack.getDamageTypeEnum(), attack.getPhysBuildUpMod());
                } catch (Exception ignored) {
                }
            }
            int physStatBase = 0;
            int physStatExtra = 0;
            try {
                if (weapon != null) {
                    physStatBase = WeaponUtil.determineWeaponStatBonus(attacker, weapon);
                    double physMult = Math.max(0.0, attack.damageMultiplier);
                    physStatExtra = (int) Math.floor(physStatBase * 5.0 * physMult);
                    if (physStatExtra != 0)
                        totalPhysBeforeResist += physStatExtra;
                }
            } catch (Exception ignored) {
            }
            // Use ResistanceUtil to calculate damage after resistance (handles nulls safely)
            physAfter = ResistanceUtil.getDamageAfterResistance(target, totalPhysBeforeResist, physicalType);
            if (physAfter > 0) {
                target.modifyHp(-physAfter);
            }
        }

        int magRaw = 0;
        int magAfter = 0;
        int magicCritCount = 0;
        int magicBeforeResist = 0;
        int magicStatBonus = 0;
        int magicStatExtra = 0;
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
                        int extra = (int) Math.floor(best * 5.0 * Math.max(0.0, magicMult));
                        magicStatExtra = extra;
                        if (extra != 0)
                            magicBeforeResist += extra;
                        magicStatChosenName = chosen.name();
                    }
                }
            } catch (Exception ignored) {
            }

            int magicToHitBonus = Math.max(0, magicStatBonus) * 5;

            int magicTimes = attack.getTimes();
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
                //         .println("Missed (magicResist): " + attacker.getName() + " -> " + target.getName());
                            magicMissResist++;
                        } else {
                // System.out
                //         .println("Missed (dodge magic): " + attacker.getName() + " -> " + target.getName());
                            magicMissDodge++;
                        }
                    } else {
                        if (toHit <= effectiveDodge) {
                // System.out
                //         .println("Missed (dodge magic): " + attacker.getName() + " -> " + target.getName());
                            magicMissDodge++;
                        } else {
                // System.out
                //         .println("Missed (magicResist): " + attacker.getName() + " -> " + target.getName());
                            magicMissResist++;
                        }
                    }
                    continue;
                }
                int hit = 0;
                if (attack.magicDamageDice != null && !attack.magicDamageDice.isBlank()) {
                    hit = Dice.roll(attack.magicDamageDice);
                }
                hit += Math.max(0, magicToHitBonus);
                magRaw += hit;
                float effectiveCrit = critChance;
                boolean crit = rng.nextFloat() < (effectiveCrit / 100f);
                if (crit) {
                    magicCritCount++;
                    hit *= 2;
                }
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
                AttackEngine.attackListener.accept(rpt);
            }
        } catch (Exception ignored) {
        }

        if (hasPhysical || isTrue) {
        // System.out.println("Attack: " + attack.name + " Physical After: " + physAfter
        //         + (magAfter > 0 ? (", Magic After: " + magAfter) : ""));
        } else if (magAfter > 0) {
            // System.out.println("Attack: " + attack.name + " Magic After: " + magAfter);
        }
    }

}

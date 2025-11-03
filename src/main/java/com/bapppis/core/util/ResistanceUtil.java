package com.bapppis.core.util;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Creature;
// Note: ResBuildUp used directly via fully-qualified name below to avoid adding another import

public final class ResistanceUtil {

    public static final int BASE_BUILD_UP = 20;

    public enum Kind {
        MAGICAL,
        PHYSICAL,
        TRUE
    }

    private ResistanceUtil() {
    }

    // --- Helper methods for modifying resistances ---
    public static void modifyFireResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.FIRE, amount);
    }

    public static void modifyWaterResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.WATER, amount);
    }

    public static void modifyWindResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.WIND, amount);
    }

    public static void modifyIceResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.ICE, amount);
    }

    public static void modifyNatureResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.NATURE, amount);
    }

    public static void modifyLightningResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.LIGHTNING, amount);
    }

    public static void modifyLightResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.LIGHT, amount);
    }

    public static void modifyDarknessResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.DARKNESS, amount);
    }

    public static void modifyBludgeoningResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.BLUDGEONING, amount);
    }

    public static void modifyPiercingResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.PIERCING, amount);
    }

    public static void modifySlashingResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.SLASHING, amount);
    }

    public static void modifyTrueResistance(Creature c, int amount) {
        if (c != null)
            c.modifyResistance(Resistances.TRUE, amount);
    }

    // --- Helper methods for modifying ResBuildUp values ---
    public static void modifyFireBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.FIRE, amount);
    }

    public static void modifyWaterBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.WATER, amount);
    }

    public static void modifyWindBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.WIND, amount);
    }

    public static void modifyIceBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.ICE, amount);
    }

    public static void modifyNatureBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.NATURE, amount);
    }

    public static void modifyLightningBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.LIGHTNING, amount);
    }

    public static void modifyLightBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.LIGHT, amount);
    }

    public static void modifyDarknessBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.DARKNESS, amount);
    }

    public static void modifyBludgeoningBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.BLUDGEONING, amount);
    }

    public static void modifyPiercingBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.PIERCING, amount);
    }

    public static void modifySlashingBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.SLASHING, amount);
    }

    public static void modifyTrueBuildUp(Creature c, int amount) {
        if (c != null)
            c.modifyResBuildUp(com.bapppis.core.ResBuildUp.TRUE, amount);
    }

    public static Kind classify(Resistances r) {
        if (r == null)
            return Kind.MAGICAL;
        switch (r) {
            case BLUDGEONING:
            case PIERCING:
            case SLASHING:
                return Kind.PHYSICAL;
            case TRUE:
                return Kind.TRUE;
            default:
                return Kind.MAGICAL;
        }
    }

    public static Kind classify(String s) {
        if (s == null)
            return Kind.MAGICAL;
        try {
            return classify(Resistances.valueOf(s));
        } catch (IllegalArgumentException e) {
            return Kind.MAGICAL;
        }
    }

    public static Resistances parse(String s) {
        if (s == null)
            return null;
        try {
            return Resistances.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int getDamageAfterResistance(Creature c, int damage, Resistances resistance) {
        if (c == null)
            return damage;
        if (resistance == null)
            return damage;
        try {
            int resVal = c.getResistance(resistance);
            return Math.floorDiv(damage * resVal, 100);
        } catch (Exception e) {
            return damage;
        }
    }

    public static void addBuildUp(Creature c, Resistances res, float mult) {
        if (c == null || res == null)
            return;

        int resistanceVal;
        try {
            resistanceVal = c.getResistance(res);
        } catch (Exception e) {
            resistanceVal = 100;
        }

        // resistance factor: 100 -> 1.0, 50 -> 0.5, etc.
        double resFactor = Math.max(0, resistanceVal) / 100.0;
        int amount = (int) Math.floor(BASE_BUILD_UP * (double) mult * resFactor);
        if (amount <= 0)
            return;

        try {
            com.bapppis.core.ResBuildUp rb = com.bapppis.core.ResBuildUp.valueOf(res.name());
            c.modifyResBuildUp(rb, amount);
            // Mark as fresh so the next decay pass will skip this entry once.
            c.markResBuildUpFresh(rb);
        } catch (IllegalArgumentException ignored) {
            // no matching ResBuildUp enum -> nothing to do
        }
    }

    public static void printResBuildUps(Creature c) {
        if (c == null)
            return;
        for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
            int v = c.getResBuildUp(rb);
            String label = rb.name().toLowerCase();
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            if (v == -1) {
                System.out.println(label + ": Immune");
            } else {
                System.out.println(label + ": " + v + "%");
            }
        }
    }

    /**
     * Check if a creature's buildup has reached 100% for any resistance type.
     * If so, apply the corresponding debuff property, reset that buildup to 0,
     * and return true. Otherwise return false.
     *
     * This is called after buildup modifications to trigger "overload" effects.
     *
     * @param c the creature to check
     * @return true if an overload was triggered, false otherwise
     */
    public static boolean checkResOverload(Creature c) {
        if (c == null)
            return false;
        boolean triggered = false;
        for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
            int cur = c.getResBuildUp(rb);
            if (cur >= 100) {
                // Overload! Apply debuff and reset buildup
                int propertyId = getOverloadPropertyId(rb);
                if (propertyId != -1) {
                    c.addProperty(propertyId);
                }
                c.setResBuildUpAbsolute(rb, 0);
                triggered = true;
            }
        }
        return triggered;
    }

    /**
     * Map each ResBuildUp type to a property ID that should be applied when
     * buildup reaches 100%. Returns -1 if no property is configured yet.
     *
     * TODO: Replace placeholder IDs with actual property IDs once all overload
     * debuffs are created.
     */
    private static int getOverloadPropertyId(com.bapppis.core.ResBuildUp rb) {
        switch (rb) {
            case FIRE:
                return 2336;
            case WATER:
                return -1; // TODO: Create "Drenched" or similar property
            case WIND:
                return -1; // TODO: Create "Staggered" or similar property
            case ICE:
                return -1; // TODO: Create "Frozen" property
            case NATURE:
                return 2338; // TODO: Create "Poisoned" property
            case LIGHTNING:
                return -1; // TODO: Create "Jolted" or "Shocked" property
            case LIGHT:
                return -1; // TODO: Create "Blinded" property
            case DARKNESS:
                return 2335;
            case BLUDGEONING:
                return -1; // TODO: Create "Concussed" property
            case PIERCING:
                return -1; // TODO: Create "Punctured" or similar property
            case SLASHING:
                return 2334;
            case TRUE:
                return -1; // TODO: Create "True Damage Overload" property (if applicable)
            default:
                return -1;
        }
    }

    /**
     * Decay ResBuildUp values for a creature. For each ResBuildUp that is not
     * immune (-1), reduce it by a per-tick amount computed from the creature's
     * corresponding resistance. The amount is computed as:
     * decay = floor(10.0 * (100.0 / resistance))
     * Falls back to resistance 100 if there is no matching Resistances entry.
     */
    public static void decayResBuildUps(Creature c) {
        if (c == null)
            return;
        for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
            // If this buildup was freshly added during the last tick, skip
            // decay once and clear the fresh flag.
            if (c.testAndClearResBuildUpFresh(rb))
                continue;

            int cur = c.getResBuildUp(rb);
            if (cur == -1)
                continue; // immune
            // map ResBuildUp name to Resistances if possible
            Resistances mapRes = null;
            try {
                mapRes = Resistances.valueOf(rb.name());
            } catch (Exception ignored) {
                mapRes = null;
            }
            int resistanceVal = 100;
            if (mapRes != null) {
                resistanceVal = c.getResistance(mapRes);
            }
            // decay = floor((200 - resistance) / 10), clamped to >= 0
            int decay = (200 - resistanceVal) / 10;
            if (decay < 0)
                decay = 0;
            int next = Math.max(0, cur - decay);
            c.setResBuildUpAbsolute(rb, next);
        }
    }
}

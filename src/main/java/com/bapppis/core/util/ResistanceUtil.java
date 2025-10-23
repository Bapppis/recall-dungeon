package com.bapppis.core.util;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Creature;
// Note: ResBuildUp used directly via fully-qualified name below to avoid adding another import

public final class ResistanceUtil {

    public enum Kind {
        MAGICAL,
        PHYSICAL,
        TRUE
    }

    private ResistanceUtil() {
    }
        // --- Helper methods for modifying resistances ---
        public static void modifyFireResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.FIRE, amount);
        }

        public static void modifyWaterResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.WATER, amount);
        }

        public static void modifyWindResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.WIND, amount);
        }

        public static void modifyIceResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.ICE, amount);
        }

        public static void modifyNatureResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.NATURE, amount);
        }

        public static void modifyLightningResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.LIGHTNING, amount);
        }

        public static void modifyLightResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.LIGHT, amount);
        }

        public static void modifyDarknessResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.DARKNESS, amount);
        }

        public static void modifyBludgeoningResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.BLUDGEONING, amount);
        }

        public static void modifyPiercingResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.PIERCING, amount);
        }

        public static void modifySlashingResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.SLASHING, amount);
        }

        public static void modifyTrueResistance(Creature c, int amount) {
            if (c != null) c.modifyResistance(Resistances.TRUE, amount);
        }

        // --- Helper methods for modifying ResBuildUp values ---
        public static void modifyFireBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.FIRE, amount);
        }

        public static void modifyWaterBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.WATER, amount);
        }

        public static void modifyWindBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.WIND, amount);
        }

        public static void modifyIceBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.ICE, amount);
        }

        public static void modifyNatureBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.NATURE, amount);
        }

        public static void modifyLightningBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.LIGHTNING, amount);
        }

        public static void modifyLightBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.LIGHT, amount);
        }

        public static void modifyDarknessBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.DARKNESS, amount);
        }

        public static void modifyBludgeoningBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.BLUDGEONING, amount);
        }

        public static void modifyPiercingBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.PIERCING, amount);
        }

        public static void modifySlashingBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.SLASHING, amount);
        }

        public static void modifyTrueBuildUp(Creature c, int amount) {
            if (c != null) c.modifyResBuildUp(com.bapppis.core.ResBuildUp.TRUE, amount);
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
        if (c == null) return damage;
        if (resistance == null) return damage;
        try {
            int resVal = c.getResistance(resistance);
            return Math.floorDiv(damage * resVal, 100);
        } catch (Exception e) {
            return damage;
        }
    }

    public static void printResBuildUps(Creature c) {
        if (c == null) return;
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
     * Decay ResBuildUp values for a creature. For each ResBuildUp that is not
     * immune (-1), reduce it by a per-tick amount computed from the creature's
     * corresponding resistance. The amount is computed as:
     *   decay = floor(10.0 * (100.0 / resistance))
     * Falls back to resistance 100 if there is no matching Resistances entry.
     */
    public static void decayResBuildUps(Creature c) {
        if (c == null) return;
        for (com.bapppis.core.ResBuildUp rb : com.bapppis.core.ResBuildUp.values()) {
            int cur = c.getResBuildUp(rb);
            if (cur == -1) continue; // immune
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
            if (decay < 0) decay = 0;
            int next = Math.max(0, cur - decay);
            c.setResBuildUpAbsolute(rb, next);
        }
    }
}

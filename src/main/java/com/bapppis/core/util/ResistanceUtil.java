package com.bapppis.core.util;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Creature;

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
}

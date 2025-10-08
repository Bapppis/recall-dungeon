package com.bapppis.core.util;

import com.bapppis.core.creature.Creature.Resistances;

/**
 * Utility to classify a low-level Resistance into a higher-level kind used by
 * combat logic and UI.
 */
public final class ResistanceUtil {

    public enum Kind {
        MAGICAL,
        PHYSICAL,
        TRUE
    }

    private ResistanceUtil() {
        // utility
    }

    /**
     * Classify a Resistances enum value.
     * - BLUDGEONING, PIERCING, SLASHING => PHYSICAL
     * - TRUE => TRUE
     * - everything else => MAGICAL
     */
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

    /**
     * Convenience overload: accepts a string (e.g. from JSON or external input).
     * Returns MAGICAL on null/unknown values.
     */
    public static Kind classify(String s) {
        if (s == null)
            return Kind.MAGICAL;
        try {
            return classify(Resistances.valueOf(s));
        } catch (IllegalArgumentException e) {
            return Kind.MAGICAL;
        }
    }
    
    /**
     * Safe parse for resistance name strings. Returns null on unknown/null input.
     */
    public static Resistances parse(String s) {
        if (s == null) return null;
        try {
            return Resistances.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

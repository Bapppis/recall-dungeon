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

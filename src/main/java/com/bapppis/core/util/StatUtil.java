package com.bapppis.core.util;

import com.bapppis.core.creature.Creature;

/**
 * Small helper utilities for manipulating creature stats from external call sites
 * (tests, consoles, dev tools). Methods ensure stats never drop below zero and
 * delegate to Creature's existing APIs so derived fields remain consistent.
 */
public final class StatUtil {
    private StatUtil() {}

    public static void increaseSTR(Creature c) { increaseSTR(c, 1); }
    public static void increaseSTR(Creature c, int amt) { increaseStat(c, Creature.Stats.STRENGTH, amt); }
    public static void decreaseSTR(Creature c) { decreaseSTR(c, 1); }
    public static void decreaseSTR(Creature c, int amt) { decreaseStat(c, Creature.Stats.STRENGTH, amt); }

    public static void increaseDEX(Creature c) { increaseDEX(c, 1); }
    public static void increaseDEX(Creature c, int amt) { increaseStat(c, Creature.Stats.DEXTERITY, amt); }
    public static void decreaseDEX(Creature c) { decreaseDEX(c, 1); }
    public static void decreaseDEX(Creature c, int amt) { decreaseStat(c, Creature.Stats.DEXTERITY, amt); }

    public static void increaseCON(Creature c) { increaseCON(c, 1); }
    public static void increaseCON(Creature c, int amt) { increaseStat(c, Creature.Stats.CONSTITUTION, amt); }
    public static void decreaseCON(Creature c) { decreaseCON(c, 1); }
    public static void decreaseCON(Creature c, int amt) { decreaseStat(c, Creature.Stats.CONSTITUTION, amt); }

    public static void increaseINT(Creature c) { increaseINT(c, 1); }
    public static void increaseINT(Creature c, int amt) { increaseStat(c, Creature.Stats.INTELLIGENCE, amt); }
    public static void decreaseINT(Creature c) { decreaseINT(c, 1); }
    public static void decreaseINT(Creature c, int amt) { decreaseStat(c, Creature.Stats.INTELLIGENCE, amt); }

    public static void increaseWIS(Creature c) { increaseWIS(c, 1); }
    public static void increaseWIS(Creature c, int amt) { increaseStat(c, Creature.Stats.WISDOM, amt); }
    public static void decreaseWIS(Creature c) { decreaseWIS(c, 1); }
    public static void decreaseWIS(Creature c, int amt) { decreaseStat(c, Creature.Stats.WISDOM, amt); }

    public static void increaseCHA(Creature c) { increaseCHA(c, 1); }
    public static void increaseCHA(Creature c, int amt) { increaseStat(c, Creature.Stats.CHARISMA, amt); }
    public static void decreaseCHA(Creature c) { decreaseCHA(c, 1); }
    public static void decreaseCHA(Creature c, int amt) { decreaseStat(c, Creature.Stats.CHARISMA, amt); }

    public static void increaseLUCK(Creature c) { increaseLUCK(c, 1); }
    public static void increaseLUCK(Creature c, int amt) { increaseStat(c, Creature.Stats.LUCK, amt); }
    public static void decreaseLUCK(Creature c) { decreaseLUCK(c, 1); }
    public static void decreaseLUCK(Creature c, int amt) { decreaseStat(c, Creature.Stats.LUCK, amt); }

    // Generic helpers
    public static void increaseStat(Creature c, Creature.Stats stat, int amount) {
        if (c == null || amount <= 0) return;
        c.modifyStat(stat, Math.max(0, amount));
    }

    public static void decreaseStat(Creature c, Creature.Stats stat, int amount) {
        if (c == null || amount <= 0) return;
        // Ensure stat doesn't drop below 1: compute current and clamp
        int current = c.getStat(stat);
        int minAllowed = 1;
        int maxSub = Math.max(0, current - minAllowed); // most we can subtract
        int toSubtract = Math.min(Math.max(0, amount), maxSub);
        if (toSubtract == 0) return;
        c.modifyStat(stat, -toSubtract);
    }
}

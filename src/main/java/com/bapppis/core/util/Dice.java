package com.bapppis.core.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Dice {
    private Dice() {}

    /**
     * Rolls dice in NdM format, optionally with a modifier like +K or -K.
     * Examples: "2d6", "3d4+2", "1d8-1".
     * Returns 0 if input is invalid or null.
     */
    public static int roll(String dice) {
        if (dice == null) return 0;
        Pattern p = Pattern.compile("^(\\d+)d(\\d+)([+-]\\d+)?$");
        Matcher m = p.matcher(dice.trim());
        if (!m.matches()) return 0;
        int num = Integer.parseInt(m.group(1));
        int sides = Integer.parseInt(m.group(2));
        int modifier = 0;
        if (m.group(3) != null) modifier = Integer.parseInt(m.group(3));
        int total = 0;
        for (int i = 0; i < num; i++) {
            total += ThreadLocalRandom.current().nextInt(1, sides + 1);
        }
        return total + modifier;
    }
}

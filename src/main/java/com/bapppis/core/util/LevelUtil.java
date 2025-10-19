package com.bapppis.core.util;

import com.bapppis.core.creature.Creature;

public final class LevelUtil {
    public static final int MAX_LEVEL = 30;

    public static int xpForNextLevel(int level) {
        if (level < 0)
            throw new IllegalArgumentException("level must be >= 0");
        if (level >= MAX_LEVEL) {
            return 0;
        }
        return 20 * level + 10;
    }

    public static int getMaxLevel() {
        return MAX_LEVEL;
    }

    public static int creatureXpForNextLevel(Creature c) {
        if (c.getLevel() < 0)
            throw new IllegalArgumentException("level must be >= 0");
        if (c.getLevel() >= MAX_LEVEL) {
            return 0;
        }
        return 20 * c.getLevel() + 10;
    }

    public static int totalXpForLevel(int targetLevel) {
        if (targetLevel < 0)
            throw new IllegalArgumentException("targetLevel must be >= 0");
        if (targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }
        int sumL = targetLevel * (targetLevel - 1) / 2;
        return 20 * sumL + 10 * targetLevel;
    }
}

package com.bapppis.core.util;

import com.bapppis.core.creature.Creature;

public final class LevelUtil {
    /** Highest attainable level (inclusive). */
    public static final int MAX_LEVEL = 30;

    /**
     * XP required to go from `level` -> `level+1`.
     * Formula: currentLevel*10 + (currentLevel+1)*10 = 20*level + 10
     * Examples: 0->1 = 10, 1->2 = 30, 2->3 = 50
     */
    public static int xpForNextLevel(int level) {
        if (level < 0)
            throw new IllegalArgumentException("level must be >= 0");
        // If already at max level, there is no next-level XP requirement
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
        // If already at max level, there is no next-level XP requirement
        if (c.getLevel() >= MAX_LEVEL) {
            return 0;
        }
        return 20 * c.getLevel() + 10;
    }

    /**
     * Cumulative XP required to reach targetLevel from level 0.
     * Example: totalXpForLevel(2) = xp(0->1) + xp(1->2) = 10 + 30 = 40
     */
    public static int totalXpForLevel(int targetLevel) {
        if (targetLevel < 0)
            throw new IllegalArgumentException("targetLevel must be >= 0");
        if (targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }
        // sum_{L=0}^{targetLevel-1} (20L + 10)
        int sumL = targetLevel * (targetLevel - 1) / 2;
        return 20 * sumL + 10 * targetLevel;
    }
}

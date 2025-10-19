package com.bapppis.core.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.bapppis.core.creature.Attack;

public final class AttackUtil {
    private AttackUtil() {}

    public static Attack chooseAttackFromList(List<Attack> list) {
        if (list == null || list.isEmpty()) return null;
        int total = 0;
        for (Attack a : list) total += a.getWeight();
        int pick = ThreadLocalRandom.current().nextInt(Math.max(1, total));
        for (Attack a : list) {
            pick -= a.getWeight();
            if (pick < 0) return a;
        }
        return list.get(0);
    }
}

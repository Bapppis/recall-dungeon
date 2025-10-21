package com.bapppis.core.Creature;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;

public class TestEnemy {
    @Test
    public void testEnemyCreation() {
        AllLoaders.loadAll();

        /* Creature enemy = CreatureLoader.getCreature("SkeletonSpearman");
        Creature enemy2 = CreatureLoader.getCreature("SkeletonSwordsman");

        System.out.println(enemy);
        System.out.println(enemy2); */

        /* Creature skeleton1 = CreatureLoader.getCreature("SkeletonSwordsman");

        System.out.println(skeleton1); */

        Creature goblin = CreatureLoader.getCreature("GoblinBerserker");

        System.out.println(goblin);
        System.out.println("\nProperties:");
        System.out.println(goblin.printProperties());
    }

}

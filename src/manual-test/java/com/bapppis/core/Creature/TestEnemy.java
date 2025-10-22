package com.bapppis.core.Creature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.item.ItemLoader;

public class TestEnemy {
    @BeforeEach
    public void setup() {
        // Force reload all game data before each test to ensure test isolation
        AllLoaders.loadAll();
        CreatureLoader.forceReload();
        ItemLoader.forceReload();
    }

    @Test
    public void testGoblinCreation() {
        Creature goblin = CreatureLoader.getCreature("GoblinBerserker");
        //System.out.println(goblin);
        //goblin.printAllFields();
    }

    @Test
    public void testSkeletonCreation() {
        Creature skeleton = CreatureLoader.getCreature("SkeletonSpearman");
        Creature skeleton2 = CreatureLoader.getCreature("SkeletonSwordsman");
        System.out.println(skeleton);
        System.out.println(skeleton2);
        // skeleton.printAllFields();
    }

    @Test
    public void testDogCreation() {
        Creature dog = CreatureLoader.getCreature("DarkHound");
        System.out.println(dog);
        //dog.printAllFields();
    }
}

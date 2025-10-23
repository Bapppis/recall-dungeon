package com.bapppis.core.Creature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.util.ResistanceUtil;

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
        ResistanceUtil.modifyFireBuildUp(goblin, 50);
        ResistanceUtil.modifyWaterBuildUp(goblin, -50);
        ResistanceUtil.modifySlashingBuildUp(goblin, 50);
        ResistanceUtil.printResBuildUps(goblin);
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

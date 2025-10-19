package com.bapppis.core.Creature;
import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;

public class TestEnemy {
    @Test
    public void testEnemyCreation() {
        AllLoaders.loadAll();

        Creature enemy = CreatureLoader.getCreature("DarkHound");
        System.out.println(enemy);
    }

}

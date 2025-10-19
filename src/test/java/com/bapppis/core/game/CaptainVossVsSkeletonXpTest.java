package com.bapppis.core.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Enemy;
import com.bapppis.core.creature.Player;

public class CaptainVossVsSkeletonXpTest {

    @Test
    public void testXpTransferOnKill() {
        AllLoaders.loadAll();

        Player voss = CreatureLoader.getPlayerById(5001);
        Enemy skeleton = (Enemy) CreatureLoader.getCreatureById(19001);
        assert voss != null;
        assert skeleton != null;

        skeleton.setCurrentHp(1);

        int before = voss.getXp();

        Combat.startCombat(voss, skeleton, true);

        int after = voss.getXp();
        Integer expectedGain = skeleton.getEnemyXp() == null ? 0 : skeleton.getEnemyXp();
        assertEquals(before + expectedGain.intValue(), after);
    }
}

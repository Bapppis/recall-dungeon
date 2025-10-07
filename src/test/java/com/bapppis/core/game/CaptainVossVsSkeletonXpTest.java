package com.bapppis.core.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;

public class CaptainVossVsSkeletonXpTest {

    @Test
    public void testXpTransferOnKill() {
        // Load data
        CreatureLoader.loadCreatures();

        // Get Captain Voss (player) and Skeleton Swordsman (enemy)
        Player voss = CreatureLoader.getPlayerById(5001);
        Creature skeleton = CreatureLoader.getCreatureById(6601);
        assert voss != null;
        assert skeleton != null;

        // Ensure deterministic kill: reduce skeleton HP to 1 so a single player attack
        // ends it.
        skeleton.setCurrentHp(1);

        // Record starting XP for player
        int before = voss.getXp();

        // Run combat - this will transfer totalXp on enemy defeat (auto-attack to avoid
        // interactive prompt)
        // Level and xp before combat
        System.out.println("Before Combat: Voss Level " + voss.getLevel() + ", XP " + voss.getXp());
        Combat.startCombat(voss, skeleton, true);

        // After combat, player's XP should have increased by skeleton.enemyXp
        int after = voss.getXp();
        Integer expectedGain = skeleton.getEnemyXp() == null ? 0 : skeleton.getEnemyXp();
        assertEquals(before + expectedGain.intValue(), after);
        System.out.println("After Combat: Voss Level " + voss.getLevel() + ", XP " + voss.getXp());
    }
}

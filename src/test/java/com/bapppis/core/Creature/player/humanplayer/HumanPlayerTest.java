package com.bapppis.core.Creature.player.humanplayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creatures.Creature.CreatureType;
import com.bapppis.core.creatures.Creature.Size;
import com.bapppis.core.creatures.Creature.Stats;
import com.bapppis.core.creatures.player.humanplayer.CaptainVoss;

public class HumanPlayerTest {

    @Test
    public void testHumanPlayerCreation() {
        System.out.println("---------------------Human Player Creation---------------------");
        CaptainVoss captainVoss = new CaptainVoss();
        System.out.println(captainVoss.toString());
        assertCaptainVossDefaults(captainVoss);
    }

    private void assertCaptainVossDefaults(CaptainVoss captainVoss) {
        // Check default values for Captain Voss
        assertEquals("Captain Aldric Voss", captainVoss.getName());
        assertEquals(30, captainVoss.getMaxHp());
        assertEquals(30, captainVoss.getCurrentHp());
        assertEquals(Size.MEDIUM, captainVoss.getSize());
        assertEquals(CreatureType.HUMANOID, captainVoss.getCreatureType());
        assertEquals(14, captainVoss.getStat(Stats.STRENGTH));
        assertEquals(13, captainVoss.getStat(Stats.CONSTITUTION));
        assertEquals("Captain Voss, a seasoned warrior. Determined to find home and protect his homeland.", captainVoss.getDescription());
    }

}

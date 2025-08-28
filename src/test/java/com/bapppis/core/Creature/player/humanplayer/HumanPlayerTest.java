package com.bapppis.core.Creature.player.humanplayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.bapppis.core.creature.Creature.CreatureType;
import com.bapppis.core.creature.Creature.Size;
import com.bapppis.core.creature.Creature.Stats;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.property.PropertyManagerTest;
import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;

public class HumanPlayerTest {

    @Test
    public void testHumanPlayerCreationFromJson() throws Exception {
        System.out.println("---------------------Human Player Creation from JSON---------------------");
        PropertyManagerTest propertyManagerTest = new PropertyManagerTest();
        // Load all properties with propertymanagertest
        propertyManagerTest.testLoadTestProperties();
        Gson gson = new Gson();
        try (Reader reader = new InputStreamReader(
                getClass().getResourceAsStream("/assets/creatures/players/humanplayers/CaptainVossTest.json"))) {
            Player captainVoss = gson.fromJson(reader, Player.class);
            System.out.println(captainVoss.toString());
            assertCaptainVossDefaults(captainVoss);
        }
    }

    private void assertCaptainVossDefaults(Player captainVoss) {
        assertEquals("Captain Aldric Voss", captainVoss.getName());
        assertEquals(30, captainVoss.getMaxHp());
        assertEquals(30, captainVoss.getCurrentHp());
        assertEquals(Size.MEDIUM, captainVoss.getSize());
        assertEquals(CreatureType.HUMANOID, captainVoss.getCreatureType());
        assertEquals(13, captainVoss.getStat(Stats.STRENGTH));
        assertEquals(12, captainVoss.getStat(Stats.CONSTITUTION));
        assertEquals("Captain Voss, a seasoned warrior. Determined to find home and protect his homeland.", captainVoss.getDescription());
    }
}

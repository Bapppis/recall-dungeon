package com.bapppis.core.property.trait;

import com.bapppis.core.property.PropertyImpl;
import com.bapppis.core.creature.Creature.Stats;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HumanAdaptabilityPropertyTest {
    @Test
        public void testHumanAdaptabilityPropertyLoading() throws Exception {
            // Load HumanAdaptabilityTest.json directly from test resources
            String resourcePath = "/assets/properties/trait/HumanAdaptabilityTest.json";
            try (java.io.Reader reader = new java.io.InputStreamReader(
                    getClass().getResourceAsStream(resourcePath))) {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                PropertyImpl humanAdaptability = gson.fromJson(reader, PropertyImpl.class);
                assertNotNull(humanAdaptability);
                assertEquals(4001, humanAdaptability.getId());
                assertEquals("Human Adaptability", humanAdaptability.getName());
                assertEquals(1, humanAdaptability.getStatModifiers().get(Stats.STRENGTH));
                assertEquals(1, humanAdaptability.getStatModifiers().get(Stats.DEXTERITY));
                assertEquals(1, humanAdaptability.getStatModifiers().get(Stats.CONSTITUTION));
                assertEquals(1, humanAdaptability.getStatModifiers().get(Stats.INTELLIGENCE));
                assertEquals(1, humanAdaptability.getStatModifiers().get(Stats.WISDOM));
                assertEquals(1, humanAdaptability.getStatModifiers().get(Stats.CHARISMA));
            }
    }
}

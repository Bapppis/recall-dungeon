package com.bapppis.core.property;

import com.bapppis.core.creature.Creature.Stats;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyManagerTest {
    @Test
    public void testLoadTestProperties() {
        HashMap<Integer, PropertyImpl> testPropertyMap = new HashMap<>();
        Gson gson = new Gson();
        String basePath = "/assets/properties/";
        scanTestPropertiesRecursive(basePath, gson, testPropertyMap);
        // Assert HumanAdaptabilityTest property loaded
        PropertyImpl humanAdaptability = testPropertyMap.get(4001);
        assertNotNull(humanAdaptability);
        assertEquals("Human Adaptability", humanAdaptability.getName());
        assertEquals(1, humanAdaptability.getStatModifiers().get(Stats.STRENGTH));

        // Test getPropertiesByIds method
        java.util.List<Integer> ids = java.util.Arrays.asList(4001);
        PropertyManager.loadProperties(); // Ensure main manager loads
        java.util.List<Property> properties = PropertyManager.getPropertiesByIds(ids);
        assertEquals(1, properties.size());
        assertEquals(4001, properties.get(0).getId());
    }

    private void scanTestPropertiesRecursive(String path, Gson gson, HashMap<Integer, PropertyImpl> map) {
        File dir = new File(PropertyManagerTest.class.getResource(path).getFile());
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scanTestPropertiesRecursive(path + file.getName() + "/", gson, map);
            } else if (file.getName().endsWith(".json")) {
                try (Reader reader = new InputStreamReader(PropertyManagerTest.class.getResourceAsStream(path + file.getName()))) {
                    PropertyImpl property = gson.fromJson(reader, PropertyImpl.class);
                    map.put(property.getId(), property);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package com.bapppis.core.property;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyManagerTest {
    private static HashMap<Integer, Property> testPropertyMap = new HashMap<>();

    public static void testLoadProperties() {
        Gson gson = new Gson();
        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("assets/properties/buff", "assets/properties/debuff", "assets/properties/immunity", "assets/properties/trait")
                .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        PropertyImpl property = gson.fromJson(reader, PropertyImpl.class);
                        testPropertyMap.put(property.getId(), property);
                        System.out.println("Loaded test property: " + property.getId() + " from " + resource.getPath());
                    } catch (Exception e) {
                        System.out.println("Error loading test property from: " + resource.getPath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static List<Property> testGetPropertiesByIds(List<Integer> ids) {
        List<Property> properties = new ArrayList<>();
        for (Integer id : ids) {
            Property prop = testGetProperty(id);
            if (prop != null) properties.add(prop);
        }
        return properties;
    }

    // Removed recursive loader; now using ClassGraph resource scanning

    public static Property testGetProperty(int id) {
        return testPropertyMap.get(id);
    }

    @Test
    public void testLoadTestProperties() {
        testLoadProperties();
        PropertyImpl humanAdaptability = (PropertyImpl) testGetProperty(4001);
        assertNotNull(humanAdaptability);
        assertEquals("Human Adaptability", humanAdaptability.getName());
        assertEquals(1, humanAdaptability.getStatModifiers().get(com.bapppis.core.creature.Creature.Stats.STRENGTH));

        // Test testGetPropertiesByIds method
        List<Integer> ids = java.util.Arrays.asList(4001);
        List<Property> properties = testGetPropertiesByIds(ids);
        assertEquals(1, properties.size());
        assertEquals(4001, properties.get(0).getId());
    }
}

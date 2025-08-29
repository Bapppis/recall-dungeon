package com.bapppis.core.Creature;

import com.bapppis.core.creature.Creature;
import com.bapppis.core.property.PropertyManagerTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;


public class TestCreatureLoader {
    public static final HashMap<String, Creature> testCreatureMap = new HashMap<>();

    @Test
    public void testLoadCreatures() {
        // Load test properties first
        PropertyManagerTest.testLoadProperties();
        // Load test creatures
        TestCreatureLoader.loadTestCreatures();
        /*Creature voss = TestCreatureLoader.getTestCreature("Captain Aldric Voss");
        assertNotNull(voss);
        assertEquals("Captain Aldric Voss", voss.getName());
        assertEquals(30, voss.getMaxHp());
        assertEquals(30, voss.getCurrentHp());
        assertEquals(Creature.Size.MEDIUM, voss.getSize());
        assertEquals(Creature.CreatureType.HUMANOID, voss.getCreatureType());
        assertEquals(14, voss.getStat(Creature.Stats.STRENGTH));
        assertEquals(13, voss.getStat(Creature.Stats.CONSTITUTION));
        assertEquals("Captain Voss,         assertNotNull(voss.getTrait(4001));
        assertEquals("Human Adaptability", voss.getTrait(4001).getName());
        System.out.println(voss.toString());*/
        Creature biggles = TestCreatureLoader.getTestCreature("Biggles The Unlucky");
        System.out.println(biggles.toString());
        biggles.removeProperty(2000);
        System.out.println(biggles.toString());
    }

    public static void loadTestCreatures() {
        com.google.gson.GsonBuilder gsonBuilder = new com.google.gson.GsonBuilder();
        gsonBuilder.registerTypeAdapter(
            new com.google.gson.reflect.TypeToken<java.util.Map<com.bapppis.core.creature.Creature.Resistances, Integer>>(){}.getType(),
            new com.bapppis.core.property.ResistanceMapDeserializer()
        );
        com.google.gson.Gson gson = gsonBuilder.create();
        io.github.classgraph.ScanResult scanResult = new io.github.classgraph.ClassGraph()
                .acceptPaths("assets/creatures/players/humanplayers")
                .scan();
        for (io.github.classgraph.Resource resource : scanResult.getAllResources()) {
            if (resource.getPath().endsWith(".json")) {
                try (java.io.Reader reader = new java.io.InputStreamReader(resource.open())) {
                    Creature creature;
                    if (resource.getPath().contains("players")) {
                        creature = gson.fromJson(reader, com.bapppis.core.creature.player.Player.class);
                    } else {
                        creature = gson.fromJson(reader, Creature.class);
                    }
                    // Assign properties from test property manager
                    java.io.Reader propReader = new java.io.InputStreamReader(TestCreatureLoader.class.getClassLoader().getResourceAsStream(resource.getPath()));
                    com.google.gson.JsonObject obj = gson.fromJson(propReader, com.google.gson.JsonObject.class);
                    if (obj != null && obj.has("properties")) {
                        for (com.google.gson.JsonElement el : obj.getAsJsonArray("properties")) {
                            int id = el.getAsInt();
                            com.bapppis.core.property.Property prop = com.bapppis.core.property.PropertyManagerTest.testGetProperty(id);
                            if (prop != null) {
                                creature.addProperty(prop);
                            }
                        }
                    }
                    if (creature != null && creature.getName() != null) {
                        testCreatureMap.put(creature.getName(), creature);
                    }
                } catch (Exception e) {
                    System.out.println("Error loading test creature from: " + resource.getPath());
                    e.printStackTrace();
                }
            }
        }
    }

    public static Creature getTestCreature(String name) {
        return testCreatureMap.get(name);
    }

    public static void assertNotNull(Object obj) {
        assert obj != null : "Object should not be null";
    }
}

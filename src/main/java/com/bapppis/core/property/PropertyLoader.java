package com.bapppis.core.property;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import com.google.gson.reflect.TypeToken;

public class PropertyLoader {
    private static final HashMap<Integer, Property> propertyMap = new HashMap<>();
    private static final HashMap<String, Property> propertyNameMap = new HashMap<>();
    private static boolean loaded = false;

    public static void loadProperties() {
        if (loaded) return;
        forceReload();
    }

    /**
     * Force reload of all properties, even if already loaded. Use in individual tests.
     */
    public static void forceReload() {
        propertyMap.clear();
        propertyNameMap.clear();
        loaded = false;

        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(com.bapppis.core.Resistances.class,
                new com.bapppis.core.util.ResistancesDeserializer())
            .create();
        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("data/properties/buff", "data/properties/debuff", "data/properties/trait")
                .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try {
                        // Read the resource fully into a String so we can parse it
                        String json;
                        try (java.io.InputStream is = resource.open();
                                java.util.Scanner s = new java.util.Scanner(is,
                                        java.nio.charset.StandardCharsets.UTF_8.name())) {
                            s.useDelimiter("\\A");
                            json = s.hasNext() ? s.next() : "";
                        }

                        // Read JSON into a generic map first to inspect the `type` field without
                        // binding
                        java.util.Map<String, Object> temp = gson.fromJson(json,
                                new TypeToken<java.util.Map<String, Object>>() {
                                }.getType());
                        if (temp != null) {
                            PropertyType t = null;
                            if (temp.containsKey("type") && temp.get("type") != null) {
                                try {
                                    t = PropertyType.valueOf(temp.get("type").toString());
                                } catch (IllegalArgumentException ignored) {
                                    t = null;
                                }
                            }

                            if (t == null) {
                                // Fallback: infer from the resource path (folder names)
                                String path = resource.getPath();
                                if (path.contains("/buff/") || path.contains("\\buff\\")) {
                                    t = PropertyType.BUFF;
                                } else if (path.contains("/debuff/") || path.contains("\\debuff\\")) {
                                    t = PropertyType.DEBUFF;
                                } else if (path.contains("/trait/") || path.contains("\\trait\\")) {
                                    t = PropertyType.TRAIT;
                                }
                            }

                            // Deserialize into unified Property class
                            Property propInstance = gson.fromJson(json, Property.class);
                            if (propInstance == null) {
                                propInstance = new Property();
                            }
                            // Ensure type is set (either from JSON or inferred path)
                            if (propInstance.getType() == null && t != null) {
                                try {
                                    propInstance.setType(t);
                                } catch (Exception ignored) {
                                }
                            }
                            propertyMap.put(propInstance.getId(), propInstance);
                            // normalized name map (trim + lowercase)
                            try {
                                String n = propInstance.getName();
                                if (n != null && !n.isBlank()) {
                                    String key = n.trim().toLowerCase();
                                    String keyNoSpace = key.replaceAll("\\s+", "");

                                    // keep first occurrence by name; also register space-free variant
                                    propertyNameMap.putIfAbsent(key, propInstance);
                                    propertyNameMap.putIfAbsent(keyNoSpace, propInstance);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        // System.out.println("Loaded property: " + property.getId() + " from " +
                        // resource.getPath());
                    } catch (Exception e) {
                        // System.out.println("Error loading property from: " + resource.getPath());
                        // e.printStackTrace();
                    }
                }
            }
        }
        loaded = true;
    }

    // Accessors

    public static Property getProperty(int id) {
        return propertyMap.get(id);
    }

    public static Property getPropertyByName(String name) {
        if (name == null)
            return null;
        String key = name.trim().toLowerCase();
        Property p = propertyNameMap.get(key);
        if (p != null) return p;
        String keyNoSpace = key.replaceAll("\\s+", "");
        return propertyNameMap.get(keyNoSpace);
    }

    public static java.util.List<String> getAllPropertyNames() {
        java.util.List<String> names = new java.util.ArrayList<>(propertyNameMap.keySet());
        java.util.Collections.sort(names);
        return names;
    }

    public static Collection<Property> getAllProperties() {
        return propertyMap.values();
    }

    public static List<Property> getPropertiesByIds(List<Integer> ids) {
        List<Property> properties = new ArrayList<>();
        for (Integer id : ids) {
            Property prop = getProperty(id);
            if (prop != null)
                properties.add(prop);
        }
        return properties;
    }
}

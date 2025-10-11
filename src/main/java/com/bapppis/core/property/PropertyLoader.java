package com.bapppis.core.property;

import com.google.gson.Gson;
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

    public static void loadProperties() {
        Gson gson = new Gson();
    try (ScanResult scanResult = new ClassGraph()
        .acceptPaths("data/properties/buff", "data/properties/debuff", "data/properties/trait")
        .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try {
                        // Read the resource fully into a String so we can parse it multiple times
                        String json;
                        try (java.io.InputStream is = resource.open();
                             java.util.Scanner s = new java.util.Scanner(is, java.nio.charset.StandardCharsets.UTF_8.name())) {
                            s.useDelimiter("\\A");
                            json = s.hasNext() ? s.next() : "";
                        }

                        // Read JSON into a generic map first to inspect the `type` field without binding
                        java.util.Map<String, Object> temp = gson.fromJson(json, new TypeToken<java.util.Map<String, Object>>(){}.getType());
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

                            Property propInstance = null;
                            // Parse the JSON string into the appropriate concrete class now that we know the type
                            if (t == PropertyType.BUFF) {
                                BuffProperty bp = gson.fromJson(json, BuffProperty.class);
                                propInstance = (bp != null) ? bp : new BuffProperty();
                            } else if (t == PropertyType.DEBUFF) {
                                DebuffProperty dp = gson.fromJson(json, DebuffProperty.class);
                                propInstance = (dp != null) ? dp : new DebuffProperty();
                            } else if (t == PropertyType.TRAIT) {
                                TraitProperty tp = gson.fromJson(json, TraitProperty.class);
                                propInstance = (tp != null) ? tp : new TraitProperty();
                            } else {
                                // If we couldn't classify, fall back to trait as a safe default
                                TraitProperty tp = gson.fromJson(json, TraitProperty.class);
                                propInstance = (tp != null) ? tp : new TraitProperty();
                            }

                            propertyMap.put(propInstance.getId(), propInstance);
                        }
                        //System.out.println("Loaded property: " + property.getId() + " from " + resource.getPath());
                    } catch (Exception e) {
                        System.out.println("Error loading property from: " + resource.getPath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Removed recursive loader; now using explicit file paths

    public static Property getProperty(int id) {
        return propertyMap.get(id);
    }

    public static Collection<Property> getAllProperties() {
        return propertyMap.values();
    }

    public static List<Property> getPropertiesByIds(List<Integer> ids) {
        List<Property> properties = new ArrayList<>();
        for (Integer id : ids) {
            Property prop = getProperty(id);
            if (prop != null) properties.add(prop);
        }
        return properties;
    }
}

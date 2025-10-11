package com.bapppis.core.property;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.InputStreamReader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.io.Reader;

public class PropertyLoader {
    private static final HashMap<Integer, Property> propertyMap = new HashMap<>();

    public static void loadProperties() {
        Gson gson = new Gson();
    try (ScanResult scanResult = new ClassGraph()
        .acceptPaths("data/properties/buff", "data/properties/debuff", "data/properties/trait")
        .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        PropertyImpl property = gson.fromJson(reader, PropertyImpl.class);
                        if (property != null) {
                            Property propInstance = null;
                            // Prefer explicit type from JSON
                            PropertyType t = property.getType();
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

                            if (t == PropertyType.BUFF) {
                                propInstance = new BuffProperty(property);
                            } else if (t == PropertyType.DEBUFF) {
                                propInstance = new DebuffProperty(property);
                            } else if (t == PropertyType.TRAIT) {
                                propInstance = new TraitProperty(property);
                            } else {
                                // Fallback to raw impl if we can't classify
                                propInstance = property;
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

package com.bapppis.core.property;

import com.google.gson.Gson;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.File;
import java.io.Reader;

public class PropertyManager {
    private static HashMap<Integer, Property> propertyMap = new HashMap<>();

    public static void loadProperties() {
        Gson gson = new Gson();
        // Recursively load all JSON files from assets/properties and subfolders
        String basePath = "/assets/properties/";
        loadPropertiesRecursive(basePath, gson);
    }

    public static java.util.List<Property> getPropertiesByIds(java.util.List<Integer> ids) {
        java.util.List<Property> properties = new java.util.ArrayList<>();
        for (Integer id : ids) {
            Property prop = getProperty(id);
            if (prop != null) properties.add(prop);
        }
        return properties;
    }

    private static void loadPropertiesRecursive(String path, Gson gson) {
        File dir = new File(PropertyManager.class.getResource(path).getFile());
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                loadPropertiesRecursive(path + file.getName() + "/", gson);
            } else if (file.getName().endsWith(".json")) {
                try (Reader reader = new InputStreamReader(PropertyManager.class.getResourceAsStream(path + file.getName()))) {
                    PropertyImpl property = gson.fromJson(reader, PropertyImpl.class);
                    propertyMap.put(property.getId(), property);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Property getProperty(int id) {
        return propertyMap.get(id);
    }
}

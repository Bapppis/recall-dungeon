package com.bapppis.core.loot;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LootPoolLoader {
    private static final Gson GSON = new Gson();

    public static List<LootPool> loadPoolsFromResources(String resourceDir) {
        List<LootPool> result = new ArrayList<>();
        try {
            // Try to load from classpath resources under /data/<resourceDir>/
            String prefix = "/data/" + resourceDir + "/";
            InputStream idx = LootPoolLoader.class.getResourceAsStream(prefix);
            // Classpath listing is not reliable cross-platform; instead read from file system relative to project when available
            File fsDir = new File("src/main/resources/data/" + resourceDir);
            if (fsDir.exists() && fsDir.isDirectory()) {
                File[] files = fsDir.listFiles((d, name) -> name.endsWith(".json"));
                if (files != null) {
                    for (File f : files) {
                        try (JsonReader jr = new JsonReader(new FileReader(f))) {
                            LootPool p = GSON.fromJson(jr, LootPool.class);
                            if (p != null) result.add(p);
                        }
                    }
                }
            } else if (idx != null) {
                // Fallback: try to load single known resources; not implemented here.
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}

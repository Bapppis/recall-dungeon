package com.bapppis.core.property;

import com.bapppis.core.Resistances;
import com.bapppis.core.util.ResistanceUtil;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Gson deserializer for a map of Resistances -> Integer used in property JSON.
 * This implementation tolerates unknown or misspelled keys by using
 * ResistanceUtil.parse(...) and skipping entries that don't map to an enum.
 */
public class ResistanceMapDeserializer implements JsonDeserializer<Map<Resistances, Integer>> {
    @Override
    public Map<Resistances, Integer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<Resistances, Integer> map = new HashMap<>();
        if (json == null || json.isJsonNull()) return map;
        JsonObject obj = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            Resistances res = ResistanceUtil.parse(key);
            if (res == null) {
                // tolerate unknown keys by skipping rather than throwing
                continue;
            }
            try {
                int value = entry.getValue().getAsInt();
                map.put(res, value);
            } catch (Exception e) {
                // malformed value; skip
                continue;
            }
        }
        return map;
    }
}

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
                continue;
            }
            try {
                int value = entry.getValue().getAsInt();
                map.put(res, value);
            } catch (Exception e) {
                continue;
            }
        }
        return map;
    }
}

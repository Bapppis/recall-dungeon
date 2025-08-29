package com.bapppis.core.property;

import com.bapppis.core.creature.Creature;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ResistanceMapDeserializer implements JsonDeserializer<Map<Creature.Resistances, Integer>> {
    @Override
    public Map<Creature.Resistances, Integer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<Creature.Resistances, Integer> map = new HashMap<>();
        JsonObject obj = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            try {
                Creature.Resistances res = Creature.Resistances.valueOf(entry.getKey());
                int value = entry.getValue().getAsInt();
                map.put(res, value);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Unknown resistance: " + entry.getKey());
            }
        }
        return map;
    }
}

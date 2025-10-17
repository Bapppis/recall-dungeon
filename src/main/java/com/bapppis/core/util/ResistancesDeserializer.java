package com.bapppis.core.util;

import java.lang.reflect.Type;

import com.bapppis.core.Resistances;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Gson deserializer for `Resistances` that uses ResistanceUtil.parse
 * to tolerate unknown or null string values (returns null on unknown).
 */
public class ResistancesDeserializer implements JsonDeserializer<Resistances> {

    @Override
    public Resistances deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull())
            return null;
        try {
            String s = json.getAsString();
            return ResistanceUtil.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.bapppis.core.properties.immunity;

import com.bapppis.core.properties.Property;
import com.bapppis.core.properties.PropertyType;

public class CorrosionImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Corrosion Immunity";
    }

}

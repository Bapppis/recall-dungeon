package com.bapppis.core.property.immunity;

import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class JoltImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Jolt Immunity";
    }

}

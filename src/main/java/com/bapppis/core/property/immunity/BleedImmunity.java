package com.bapppis.core.property.immunity;

import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class BleedImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Bleed Immunity";
    }

}

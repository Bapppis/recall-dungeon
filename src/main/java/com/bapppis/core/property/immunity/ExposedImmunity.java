package com.bapppis.core.property.immunity;

import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class ExposedImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Exposed Immunity";
    }  

    @Override
    public int getId() {
        return 3006;
    }

}

package com.bapppis.core.property.immunity;

import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class FreezeImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Freeze Immunity";
    }

    @Override
    public int getId() {
        return 3007;
    }

}

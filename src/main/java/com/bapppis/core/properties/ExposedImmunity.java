package com.bapppis.core.properties;

public class ExposedImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Exposed Immunity";
    }

}

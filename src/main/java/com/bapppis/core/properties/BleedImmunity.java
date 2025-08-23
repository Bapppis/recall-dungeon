package com.bapppis.core.properties;

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

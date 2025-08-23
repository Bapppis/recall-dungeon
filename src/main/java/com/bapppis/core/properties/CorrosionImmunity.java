package com.bapppis.core.properties;

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

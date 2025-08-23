package com.bapppis.core.properties;

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

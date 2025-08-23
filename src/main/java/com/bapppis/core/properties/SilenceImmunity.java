package com.bapppis.core.properties;

public class SilenceImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Silence Immunity";
    }

}

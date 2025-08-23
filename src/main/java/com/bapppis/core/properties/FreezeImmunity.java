package com.bapppis.core.properties;

public class FreezeImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Freeze Immunity";
    }

}

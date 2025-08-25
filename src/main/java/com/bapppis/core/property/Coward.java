package com.bapppis.core.property;

public class Coward implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.TRAIT;
    }

    @Override
    public String toString() {
        return "Coward";
    }
}

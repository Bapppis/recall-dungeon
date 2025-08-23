package com.bapppis.core.properties;

public class ConcussionImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Concussion Immunity";
    }

}

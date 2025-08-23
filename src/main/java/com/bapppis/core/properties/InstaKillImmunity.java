package com.bapppis.core.properties;

public class InstaKillImmunity implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.IMMUNITY;
    }

    @Override
    public String toString() {
        return "Insta-Kill Immunity";
    }

}

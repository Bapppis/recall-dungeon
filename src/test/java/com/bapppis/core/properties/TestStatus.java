package com.bapppis.core.properties;

public class TestStatus implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.TRAIT;
    }

    @Override
    public String toString() {
        return "Test Status Property";
    }
}

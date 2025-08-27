package com.bapppis.core.property.trait;

import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyType;

public class HumanAdaptability implements Property {

    @Override
    public PropertyType getType() {
        return PropertyType.TRAIT;
    }

    @Override
    public String toString() {
        return "Human Adaptability";
    }

    @Override
    public int getId() {
        return 4001;
    }

}

package com.bapppis.core.property;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyLoaderTest {

    @Test
    public void testLoadCowardPropertyPresent() {
        // Ensure properties are loaded
        PropertyLoader.loadProperties();

        Property p = PropertyLoader.getProperty(3666); // Coward
        assertNotNull(p, "Coward property (id 3666) should be present after loading properties");

        // basic sanity checks
        assertEquals(3666, p.getId());
        assertNotNull(p.getName(), "Coward should have a name");
    assertTrue(p instanceof Property, "Coward should deserialize to a Property instance");
    assertEquals(PropertyType.TRAIT, p.getType(), "Coward should have type TRAIT");
    }
}

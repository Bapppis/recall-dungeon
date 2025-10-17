package com.bapppis.core.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;

public class PropertyLookupEdgeCasesTest {

    @Test
    public void testNameLookupNormalization() {
        AllLoaders.loadAll();
        // Existing properties in assets include "Coward" and others
        Property byExact = PropertyLoader.getPropertyByName("Coward");
        assertNotNull(byExact);

        Property byCase = PropertyLoader.getPropertyByName("cOwArD");
        assertSame(byExact, byCase);

        // Simulate no-space variant lookup: e.g., "Human Adaptability" is known asset
        Property humanAdapt = PropertyLoader.getPropertyByName("Human Adaptability");
        if (humanAdapt != null) {
            Property byNoSpace = PropertyLoader.getPropertyByName("HumanAdaptability");
            assertSame(humanAdapt, byNoSpace);
        }
    }

    @Test
    public void testMissingNameReturnsNull() {
        AllLoaders.loadAll();
        assertNull(PropertyLoader.getPropertyByName(null));
        assertNull(PropertyLoader.getPropertyByName("   "));
        assertNull(PropertyLoader.getPropertyByName("this-does-not-exist-xyz"));
    }
}

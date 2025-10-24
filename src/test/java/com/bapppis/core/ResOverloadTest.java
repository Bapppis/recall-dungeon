package com.bapppis.core;

import static org.junit.jupiter.api.Assertions.*;

import com.bapppis.core.creature.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the resistance overload system: when buildup reaches 100%, a debuff
 * should be applied and buildup reset to 0.
 */
public class ResOverloadTest {

    private Player testCreature;

    @BeforeEach
    void setup() {
        // Load properties so that addProperty(id) can find Bleed1
        com.bapppis.core.property.PropertyLoader.loadProperties();
        testCreature = new Player();
    }

    @Test
    void testSlashingOverloadAppliesBleed() {
        // Initially no bleed debuff and buildup is 0
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.SLASHING));
        assertNull(testCreature.getDebuff(2334)); // Bleed1 ID

        // Increase slashing buildup to 100
        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 100);

        // After reaching 100, buildup should reset to 0 and Bleed1 should be applied
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.SLASHING),
                "Slashing buildup should reset to 0 after overload");
        assertNotNull(testCreature.getDebuff(2334), "Bleed1 (ID 2334) should be applied when slashing reaches 100%");
    }

    @Test
    void testOverloadDoesNotTriggerBelow100() {
        // Set buildup to 99
        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 99);

        // Should not trigger overload yet
        assertEquals(99, testCreature.getResBuildUp(ResBuildUp.SLASHING));
        assertNull(testCreature.getDebuff(2334), "Bleed1 should not be applied below 100%");
    }

    @Test
    void testOverloadTriggersExactlyAt100() {
        // Set to 50, then add 50 to reach exactly 100
        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 50);
        assertEquals(50, testCreature.getResBuildUp(ResBuildUp.SLASHING));
        assertNull(testCreature.getDebuff(2334));

        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 50);

        // Now it should overload
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.SLASHING),
                "Buildup should reset to 0 at exactly 100%");
        assertNotNull(testCreature.getDebuff(2334), "Bleed1 should be applied at exactly 100%");
    }

    @Test
    void testOverloadClampedAbove100() {
        // Add more than 100 in one go (should clamp to 100 first, then overload)
        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 150);

        // Should still reset to 0 and apply debuff
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.SLASHING),
                "Buildup should reset to 0 even when clamped above 100");
        assertNotNull(testCreature.getDebuff(2334), "Bleed1 should be applied when buildup exceeds 100%");
    }

    @Test
    void testMultipleOverloads() {
        // Trigger overload twice in succession
        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 100);
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.SLASHING));
        assertNotNull(testCreature.getDebuff(2334));

        // Trigger again
        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 100);
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.SLASHING),
                "Buildup should reset to 0 again after second overload");
        // Property should still be present (duration-based removal is separate)
        assertNotNull(testCreature.getDebuff(2334));
    }

    @Test
    void testImmuneDoesNotOverload() {
        // Set slashing buildup to immune (-1)
        testCreature.setResBuildUpAbsolute(ResBuildUp.SLASHING, -1);
        assertTrue(testCreature.isResBuildUpImmune(ResBuildUp.SLASHING));

        // Try to modify (should be ignored)
        testCreature.modifyResBuildUp(ResBuildUp.SLASHING, 100);

        // Should remain immune and no debuff applied
        assertEquals(-1, testCreature.getResBuildUp(ResBuildUp.SLASHING), "Immune buildup should not change");
        assertNull(testCreature.getDebuff(2334), "No debuff should be applied when immune");
    }

    @Test
    void testPlaceholderResistancesDoNotCrash() {
        // Test that resistances without a configured property ID don't crash
        // (they return -1 from getOverloadPropertyId and are skipped)
        testCreature.modifyResBuildUp(ResBuildUp.FIRE, 100);
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.FIRE),
                "Fire buildup should reset to 0 even without a configured property");

        testCreature.modifyResBuildUp(ResBuildUp.NATURE, 100);
        assertEquals(0, testCreature.getResBuildUp(ResBuildUp.NATURE),
                "Nature buildup should reset to 0 even without a configured property");
    }
}

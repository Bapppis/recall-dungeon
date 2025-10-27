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
    // Fire currently maps to a property (2336) — ensure it applies and resets
    testCreature.modifyResBuildUp(ResBuildUp.FIRE, 100);
    assertEquals(0, testCreature.getResBuildUp(ResBuildUp.FIRE),
        "Fire buildup should reset to 0 after overload");
    assertNotNull(testCreature.getDebuff(2336), "Fire overload should apply property ID 2336");

    // Nature currently has no configured overload property; it should reset but not apply a debuff
    testCreature.modifyResBuildUp(ResBuildUp.NATURE, 100);
    assertEquals(0, testCreature.getResBuildUp(ResBuildUp.NATURE),
        "Nature buildup should reset to 0 even without a configured property");
    }

    @Test
    void testAllResBuildUpOverloadMappings() {
        // Exhaustively test each ResBuildUp value: buildup reaches 100 -> resets to 0.
        // For known mappings, ensure the correct property is applied; otherwise no debuff.
        for (ResBuildUp rb : ResBuildUp.values()) {
            // Clear any previous debuffs for this id range by creating a fresh player
            Player c = new Player();
            // Apply 100 buildup
            c.modifyResBuildUp(rb, 100);
            assertEquals(0, c.getResBuildUp(rb), "Buildup for " + rb.name() + " should reset to 0 after overload");

            // Known overload mappings (kept in sync with ResistanceUtil.getOverloadPropertyId)
            if (rb == ResBuildUp.SLASHING) {
                assertNotNull(c.getDebuff(2334), "Slashing overload should apply Bleed1 (2334)");
            } else if (rb == ResBuildUp.FIRE) {
                assertNotNull(c.getDebuff(2336), "Fire overload should apply property 2336");
            } else if (rb == ResBuildUp.DARKNESS) {
                assertNotNull(c.getDebuff(2335), "Darkness overload should apply property 2335");
            } else {
                // Other types currently have no configured overload property — ensure nothing applied
                // We don't know exact IDs for future mappings, so assert that none of the two known IDs
                // were applied for these types.
                assertNull(c.getDebuff(2334), "No slashing debuff should be applied for " + rb.name());
                assertNull(c.getDebuff(2336), "No fire debuff should be applied for " + rb.name());
            }
        }
    }
}

package com.bapppis.core.Creature.species;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.Resistances;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.creature.creatureEnums.CreatureType;
import com.bapppis.core.creature.creatureEnums.Size;
import com.bapppis.core.creature.creatureEnums.Stats;

/**
 * Comprehensive test for the species modification system.
 * Tests that species and creature type modifications are applied additively
 * on top of JSON values, and validates all derived stat calculations.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpeciesModificationTest {

    @BeforeEach
    public void setup() {
        // Force reload all game data before each test to ensure test isolation
        // This prevents tests from affecting each other via cached creature instances
        // Using forceReload() instead of loadAll() to ensure caches are cleared
        AllLoaders.loadAll();
        CreatureLoader.forceReload();
    }

    @Test
    @Order(1)
    public void testBigglesTheTestSpeciesModifications() {
        // Load the test creature by ID
        Player biggles = CreatureLoader.getPlayerById(5002);

        assertNotNull(biggles, "BigglesTheTest should be loaded");

        // ===== VERIFY BASIC INFO =====
        assertEquals(5002, biggles.getId());
        assertEquals("Biggles The Test", biggles.getName());
        assertEquals(Size.SMALL, biggles.getSize(), "Size should be SMALL from TestGoblin");
        assertEquals(CreatureType.TESTHUMANOID, biggles.getCreatureType());

        // ===== CALCULATE EXPECTED STATS =====
        // JSON base values: all stats = 10, LUCK = 1
        // TestHumanoid modifications: STR +2, DEX -1, WIS +3
        // TestGoblin modifications: STR -3, DEX +5, INT +2, CHA -2, LUCK +1
        // TestTrait1 (from TestHumanoid): CON +1, INT -1
        // TestTrait2 (from TestGoblin): STR +2, WIS +1

        int expectedSTR = 10 + 2 - 3 + 2; // JSON + TestHumanoid + TestGoblin + TestTrait2 = 11
        int expectedDEX = 10 - 1 + 5; // JSON + TestHumanoid + TestGoblin = 14
        int expectedCON = 10 + 1; // JSON + TestTrait1 = 11
        int expectedINT = 10 + 2 - 1; // JSON + TestGoblin + TestTrait1 = 11
        int expectedWIS = 10 + 3 + 1; // JSON + TestHumanoid + TestTrait2 = 14
        int expectedCHA = 10 - 2; // JSON + TestGoblin = 8
        int expectedLUCK = 1 + 1; // JSON + TestGoblin = 2

        // ===== VERIFY STATS =====
        assertEquals(expectedSTR, biggles.getSTR(), "STR calculation failed");
        assertEquals(expectedDEX, biggles.getDEX(), "DEX calculation failed");
        assertEquals(expectedCON, biggles.getCON(), "CON calculation failed");
        assertEquals(expectedINT, biggles.getINT(), "INT calculation failed");
        assertEquals(expectedWIS, biggles.getWIS(), "WIS calculation failed");
        assertEquals(expectedCHA, biggles.getCHA(), "CHA calculation failed");
        assertEquals(expectedLUCK, biggles.getLUCK(), "LUCK calculation failed");

        // ===== VERIFY STAT BONUSES =====
        // Bonus formula: (stat - 10) for most stats, or raw value for LUCK
        int expectedSTRBonus = expectedSTR - 10; // 11 - 10 = 1
        int expectedDEXBonus = expectedDEX - 10; // 14 - 10 = 4
        int expectedCONBonus = expectedCON - 10; // 11 - 10 = 1
        int expectedINTBonus = expectedINT - 10; // 11 - 10 = 1
        int expectedWISBonus = expectedWIS - 10; // 14 - 10 = 4
        int expectedCHABonus = expectedCHA - 10; // 8 - 10 = -2
        int expectedLUCKBonus = expectedLUCK; // 2 (raw value for LUCK)

        assertEquals(expectedSTRBonus, biggles.getStatBonus(Stats.STRENGTH), "STR bonus calculation failed");
        assertEquals(expectedDEXBonus, biggles.getStatBonus(Stats.DEXTERITY), "DEX bonus calculation failed");
        assertEquals(expectedCONBonus, biggles.getStatBonus(Stats.CONSTITUTION), "CON bonus calculation failed");
        assertEquals(expectedINTBonus, biggles.getStatBonus(Stats.INTELLIGENCE), "INT bonus calculation failed");
        assertEquals(expectedWISBonus, biggles.getStatBonus(Stats.WISDOM), "WIS bonus calculation failed");
        assertEquals(expectedCHABonus, biggles.getStatBonus(Stats.CHARISMA), "CHA bonus calculation failed");
        assertEquals(expectedLUCKBonus, biggles.getStatBonus(Stats.LUCK), "LUCK bonus calculation failed");

        // ===== VERIFY RESISTANCES =====
        // JSON base: all 100
        // TestHumanoid: FIRE +20 (->120), ICE -30 (->70)
        // TestGoblin: DARKNESS +30 (->130), LIGHT -20 (->80), PIERCING +10 (->110)

        assertEquals(120, biggles.getResistance(Resistances.FIRE), "FIRE resistance calculation failed");
        assertEquals(100, biggles.getResistance(Resistances.WATER), "WATER resistance should remain 100");
        assertEquals(100, biggles.getResistance(Resistances.WIND), "WIND resistance should remain 100");
        assertEquals(70, biggles.getResistance(Resistances.ICE), "ICE resistance calculation failed");
        assertEquals(100, biggles.getResistance(Resistances.NATURE), "NATURE resistance should remain 100");
        assertEquals(100, biggles.getResistance(Resistances.LIGHTNING), "LIGHTNING resistance should remain 100");
        assertEquals(80, biggles.getResistance(Resistances.LIGHT), "LIGHT resistance calculation failed");
        assertEquals(130, biggles.getResistance(Resistances.DARKNESS), "DARKNESS resistance calculation failed");
        assertEquals(100, biggles.getResistance(Resistances.BLUDGEONING), "BLUDGEONING resistance should remain 100");
        assertEquals(110, biggles.getResistance(Resistances.PIERCING), "PIERCING resistance calculation failed");
        assertEquals(100, biggles.getResistance(Resistances.SLASHING), "SLASHING resistance should remain 100");
    // Note: default TRUE resistance changed to 50 by recent design updates
    assertEquals(50, biggles.getResistance(Resistances.TRUE), "TRUE resistance should remain 50");

        // ===== VERIFY DERIVED STATS =====
        // Base values from JSON: baseCrit=0, baseDodge=0, baseBlock=0,
        // baseMagicResist=0

        // Crit = baseCrit + (5 * LUCK bonus) + propertyCrit + equipmentCrit
        // baseCrit=0, LUCK bonus=2, propertyCrit=0, equipmentCrit=0
        float expectedCrit = 0.0f + (5.0f * expectedLUCKBonus); // 0 + 10 = 10.0%
        assertEquals(expectedCrit, biggles.getCrit(), 0.01f, "Crit calculation failed");

        // Dodge = baseDodge + (2.5 * DEX bonus) + propertyDodge + equipmentDodge
        // baseDodge=0, DEX bonus=4, propertyDodge=5 (from TestTrait2), equipmentDodge=0
        float expectedDodge = 0.0f + (2.5f * expectedDEXBonus) + 5.0f; // 0 + 10 + 5 = 15.0%
        assertEquals(expectedDodge, biggles.getDodge(), 0.01f, "Dodge calculation failed");

        // Block = baseBlock + equipmentBlock + propertyBlock
        // baseBlock=0, equipmentBlock=0, propertyBlock=0
        float expectedBlock = 0.0f;
        assertEquals(expectedBlock, biggles.getBlock(), 0.01f, "Block calculation failed");

        // MagicResist = baseMagicResist + (5 * WIS bonus) + (2.5 * CON bonus) +
        // propertyMagicResist + equipmentMagicResist
        // baseMagicResist=0, WIS bonus=4, CON bonus=1, propertyMagicResist=0,
        // equipmentMagicResist=0
        float expectedMagicResist = 0.0f + (5.0f * expectedWISBonus) + (2.5f * expectedCONBonus); // 0 + 20 + 2.5 =
                                                                                                  // 22.5%
        assertEquals(expectedMagicResist, biggles.getMagicResist(), 0.01f, "MagicResist calculation failed");

        // ===== VERIFY STAMINA REGEN =====
        // StaminaRegen = baseStaminaRegen + floor(2.5 * WIS bonus), minimum 1
        // baseStaminaRegen = maxStamina / 5 = 100 / 5 = 20 (set during setMaxStamina)
        // WIS bonus = 4
        int expectedBaseStaminaRegen = biggles.getMaxStamina() / 5; // Should be calculated from maxStamina
        int wisStaminaBonus = (int) Math.floor(2.5 * expectedWISBonus); // floor(2.5 * 4) = 10
        wisStaminaBonus = Math.max(1, wisStaminaBonus);
        int expectedStaminaRegen = expectedBaseStaminaRegen + wisStaminaBonus; // 20 + 10 = 30
        assertEquals(expectedStaminaRegen, biggles.getStaminaRegen(), "StaminaRegen calculation failed");

        // ===== VERIFY MAX HP =====
        // MaxHP depends on CON bonus and level
        // Formula in recalcMaxHp(): baseHp + (level * hpLvlBonus) + scaled by CON bonus
        // Since level=0: base calculation should be just baseHp (10) scaled by CON
        // CON bonus = 1, so factor = 1.1^1 = 1.1
        // maxHp = floor(10 * 1.1) = 11
        int expectedBaseHp = 10;
        int conBonus = expectedCONBonus;
        // MaxHP formula: baseHp + ((level + 1) * (hpDice + conBonus))
        int expectedMaxHp = expectedBaseHp + ((biggles.getLevel() + 1) * (biggles.getHpDice() + conBonus));
        // 10 + ((0 + 1) * (5 + 1)) = 10 + 6 = 16
        assertEquals(expectedMaxHp, biggles.getMaxHp(), "MaxHP calculation failed");

        // ===== VERIFY MAX MANA =====
        // MaxMana = baseMaxMana scaled by INT bonus
        // INT bonus = 1, so factor = 1.1^1 = 1.1
        // maxMana = floor(100 * 1.1) = 110, minimum 25
        int expectedBaseMaxMana = 100;
        int intBonus = expectedINTBonus;
        double intFactor = 1.0;
        if (intBonus < 0) {
            intFactor = Math.pow(0.9, -intBonus);
        } else if (intBonus > 0) {
            intFactor = Math.pow(1.1, intBonus);
        }
        int expectedMaxMana = (int) Math.floor(expectedBaseMaxMana * intFactor); // floor(100 * 1.1) = 110
        expectedMaxMana = Math.max(25, expectedMaxMana);
        assertEquals(expectedMaxMana, biggles.getMaxMana(), "MaxMana calculation failed");

        // ===== VERIFY MAX STAMINA =====
        // MaxStamina = floor(baseMaxStamina * conFactor) where conFactor = 1.1^conBonus
        // CON bonus = 1, so conFactor = 1.1^1 = 1.1
        // maxStamina = floor(100 * 1.1) = 110, minimum 25
        int expectedBaseMaxStamina = 100;
        double conFactor = conBonus > 0 ? Math.pow(1.1, conBonus) : (conBonus < 0 ? Math.pow(0.9, -conBonus) : 1.0);
        int expectedMaxStamina = (int) Math.floor(expectedBaseMaxStamina * conFactor); // floor(100 * 1.1) = 110
        expectedMaxStamina = Math.max(25, expectedMaxStamina);
        assertEquals(expectedMaxStamina, biggles.getMaxStamina(), "MaxStamina calculation failed");

        // ===== VERIFY PROPERTIES =====
        // Should have 3 traits: TestTrait1 (from TestHumanoid), TestTrait2 + TestTrait3
        // (from TestGoblin)
        assertEquals(3, biggles.getTraits().size(), "Should have exactly 3 traits");
        assertNotNull(biggles.getTrait(4001), "Should have TestTrait1");
        assertNotNull(biggles.getTrait(4002), "Should have TestTrait2");
        assertNotNull(biggles.getTrait(4003), "Should have TestTrait3");
        assertEquals(0, biggles.getBuffs().size(), "Should have no buffs");
        assertEquals(0, biggles.getDebuffs().size(), "Should have no debuffs");

        // ===== VERIFY VISION RANGE =====
        // JSON: visionRange=2
        // TestTrait3: visionRange+1
        // Expected: 2 + 1 = 3
        assertEquals(3, biggles.getVisionRange(), "VisionRange should be modified by TestTrait3");
    }

    @Test
    @Order(2)
    public void testNegativeStatBonusesAffectDerivedStats() {
        // Create a creature with intentionally low stats to test negative bonuses
        Player biggles = CreatureLoader.getPlayerById(5002);

        // Manually adjust stats to test negative bonuses
        biggles.setStat(Stats.DEXTERITY, 5); // DEX 5 -> bonus = -5
        biggles.setStat(Stats.WISDOM, 6); // WIS 6 -> bonus = -4
        biggles.setStat(Stats.LUCK, 0); // LUCK 0 -> bonus = 0 (raw value)

        // Verify negative DEX affects dodge negatively
        // Dodge = baseDodge + (2.5 * DEX bonus) = 0 + (2.5 * -5) = -12.5
        // But with TestTrait2's +5 dodge: -12.5 + 5 = -7.5
        int dexBonus = biggles.getStatBonus(Stats.DEXTERITY);
        assertEquals(-5, dexBonus, "DEX bonus should be -5");

        float expectedDodge = 0.0f + (2.5f * dexBonus) + 5.0f; // 0 + (-12.5) + 5 = -7.5
        assertEquals(expectedDodge, biggles.getDodge(), 0.01f, "Negative DEX should reduce dodge");

        // Verify negative WIS affects magic resist negatively
        int wisBonus = biggles.getStatBonus(Stats.WISDOM);
        int conBonus = biggles.getStatBonus(Stats.CONSTITUTION);
        assertEquals(-4, wisBonus, "WIS bonus should be -4");

        float expectedMagicResist = 0.0f + (5.0f * wisBonus) + (2.5f * conBonus);
        assertEquals(expectedMagicResist, biggles.getMagicResist(), 0.01f, "Negative WIS should reduce magic resist");

        // Verify zero LUCK gives zero crit bonus
        int luckBonus = biggles.getStatBonus(Stats.LUCK);
        assertEquals(0, luckBonus, "LUCK bonus should be 0");

        float expectedCrit = 0.0f + (5.0f * luckBonus); // 0 + 0 = 0
        assertEquals(expectedCrit, biggles.getCrit(), 0.01f, "Zero LUCK should give no crit bonus");
    }

    @Test
    @Order(3)
    public void testPropertyStatModifiersApplyCorrectly() {
        Player biggles = CreatureLoader.getPlayerById(5002);

        // Properties should already be applied from species modifications
        // Verify that removing a property correctly removes its stat modifiers

        // Remove TestTrait1 which has CON +1, INT -1
        boolean removed = biggles.removeProperty("TestTrait1");
        assertTrue(removed, "Should be able to remove TestTrait1");

        // CON should decrease by 1, INT should increase by 1
        int expectedCON = 11 - 1; // Was 11, loses +1 from TestTrait1
        int expectedINT = 11 + 1; // Was 11 (includes -1 from TestTrait1), removing the -1 adds 1

        assertEquals(expectedCON, biggles.getCON(), "CON should decrease after removing TestTrait1");
        assertEquals(expectedINT, biggles.getINT(), "INT should increase after removing TestTrait1");

        // Verify derived stats recalculated
        // MaxHP should change because CON changed
        int newConBonus = biggles.getStatBonus(Stats.CONSTITUTION);
        assertEquals(0, newConBonus, "CON bonus should now be 0");

        // MaxMana should change because INT changed
        int newIntBonus = biggles.getStatBonus(Stats.INTELLIGENCE);
        assertEquals(2, newIntBonus, "INT bonus should now be 2");
    }

    @Test
    @Order(4)
    public void testSpeciesModificationsAreAdditive() {
        Player biggles = CreatureLoader.getPlayerById(5002);

        // This test verifies the core requirement: species mods are ADDITIVE, not
        // fallback        // JSON specifies STR=10
        // TestHumanoid adds +2 STR
        // TestGoblin adds -3 STR
        // TestTrait2 adds +2 STR
        // Result should be: 10 + 2 - 3 + 2 = 11, NOT just using species defaults

        assertEquals(11, biggles.getSTR(), "Species modifications should be additive to JSON values");

        // Same for DEX: JSON=10, TestHumanoid=-1, TestGoblin=+5 -> 14
        assertEquals(14, biggles.getDEX(), "DEX should show additive modifications");

        // INT: JSON=10, TestGoblin=+2, TestTrait1=-1 -> 11
        assertEquals(11, biggles.getINT(), "INT should show additive modifications");
    }

    @Test
    @Order(5)
    public void testCreatureTypeAndSpeciesChaining() {
        Player biggles = CreatureLoader.getPlayerById(5002);

        // Verify that both TestHumanoid and TestGoblin modifications are applied
        // TestHumanoid sets size to LARGE
        // TestGoblin overrides to SMALL
        // Final should be SMALL (last one wins)
        assertEquals(Size.SMALL, biggles.getSize(), "TestGoblin should override size to SMALL");

        // TestHumanoid adds FIRE +20
        // TestGoblin doesn't modify FIRE
        // Final should be 120
        assertEquals(120, biggles.getResistance(Resistances.FIRE),
                "TestHumanoid's FIRE resistance should be preserved");

        // TestHumanoid modifies WIS +3
        // TestTrait2 (from TestGoblin) modifies WIS +1
        // Total WIS: 10 + 3 + 1 = 14
        assertEquals(14, biggles.getWIS(), "Both TestHumanoid and TestTrait2 WIS mods should apply");
    }
}

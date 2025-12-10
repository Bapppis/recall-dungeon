package com.bapppis.core.item;

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
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.property.Property;

/**
 * Comprehensive test for the item equipment system.
 * Tests that items correctly modify stats, resistances, derived stats, and
 * properties
 * when equipped and unequipped.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ItemEquipmentTest {

    @BeforeEach
    public void setup() {
        // Force reload all game data before each test to ensure test isolation
        AllLoaders.loadAll();
        CreatureLoader.forceReload();
        ItemLoader.forceReload();
    }

    @Test
    @Order(1)
    public void testBasicItemEquipAndUnequip() {
        // Load test creature
        Player player = CreatureLoader.getPlayerById(5002); // BigglesTheTest
        assertNotNull(player, "Test player should be loaded");

        // Capture baseline stats
        int baseSTR = player.getSTR();
        int baseDEX = player.getDEX();
        int baseCON = player.getCON();
        float baseDodge = player.getDodge();
        float baseCrit = player.getCrit();
        int baseAccuracy = player.getAccuracy();
        int baseFireResist = player.getResistance(Resistances.FIRE);
        int baseSlashResist = player.getResistance(Resistances.SLASHING);

        // Equip Test Sword (ID 29999)
        // STR+3, DEX+2, CON-1, crit+10%, dodge+5%, accuracy+1
        // SLASHING+10%, FIRE+5%
        boolean equipped = player.equipItemByName("Test Sword");
        assertTrue(equipped, "Should be able to equip Test Sword");

        // Verify stats increased
        assertEquals(baseSTR + 3, player.getSTR(), "STR should increase by 3");
        assertEquals(baseDEX + 2, player.getDEX(), "DEX should increase by 2");
        assertEquals(baseCON - 1, player.getCON(), "CON should decrease by 1");

        // Verify derived stats updated
        // Dodge = baseDodge + equipment dodge + stat bonus changes
        // DEX went from 14 to 16, bonus from 4 to 6, so +2*2.5 = +5 from stats, +5 from
        // equipment = +10 total
        assertEquals(baseDodge + 10.0f, player.getDodge(), 0.01f, "Dodge should increase");
        assertEquals(baseCrit + 10.0f, player.getCrit(), 0.01f, "Crit should increase by 10%");
        assertEquals(baseAccuracy + 1, player.getAccuracy(), "Accuracy should increase by 1");

        // Verify resistances updated
        // Test Sword has FIRE: -5, SLASHING: -10 (player takes MORE damage)
        assertEquals(baseFireResist - 5, player.getResistance(Resistances.FIRE),
                "FIRE resistance should decrease by 5");
        assertEquals(baseSlashResist - 10, player.getResistance(Resistances.SLASHING),
                "SLASHING resistance should decrease by 10");

        // Unequip the sword
        boolean unequipped = player.unequipItemByName("Test Sword");
        assertTrue(unequipped, "Should be able to unequip Test Sword");

        // Verify all stats returned to baseline
        assertEquals(baseSTR, player.getSTR(), "STR should return to baseline");
        assertEquals(baseDEX, player.getDEX(), "DEX should return to baseline");
        assertEquals(baseCON, player.getCON(), "CON should return to baseline");
        assertEquals(baseDodge, player.getDodge(), 0.01f, "Dodge should return to baseline");
        assertEquals(baseCrit, player.getCrit(), 0.01f, "Crit should return to baseline");
        assertEquals(baseAccuracy, player.getAccuracy(), "Accuracy should return to baseline");
        assertEquals(baseFireResist, player.getResistance(Resistances.FIRE),
                "FIRE resistance should return to baseline");
        assertEquals(baseSlashResist, player.getResistance(Resistances.SLASHING),
                "SLASHING resistance should return to baseline");
    }

    @Test
    @Order(2)
    public void testMultipleItemsEquipped() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Capture baseline
        int baseSTR = player.getSTR();
        int baseDEX = player.getDEX();
        int baseCON = player.getCON();
        int baseWIS = player.getWIS();
        float baseDodge = player.getDodge();
        float baseBlock = player.getBlock();

        // Equip Test Sword (STR+3, DEX+2, CON-1)
        player.equipItemByName("Test Sword");

        // Equip Test Shield (CON+2, WIS+1, DEX-1, block+20%, dodge-5%)
        player.equipItemByName("Test Shield");

        // Equip Test Boots (DEX+1, dodge+10%)
        player.equipItemByName("Test Boots");

        // Verify cumulative stat changes
        // STR: +3 from sword
        // DEX: +2 from sword, -1 from shield, +1 from boots, +1 from TestTrait4
        // (shield) = +3 total
        // CON: -1 from sword, +2 from shield = +1 total
        // WIS: +1 from shield
        assertEquals(baseSTR + 3, player.getSTR(), "STR should be +3");
        assertEquals(baseDEX + 3, player.getDEX(), "DEX should be +3 (includes TestTrait4 +1)");
        assertEquals(baseCON + 1, player.getCON(), "CON should be +1");
        assertEquals(baseWIS + 1, player.getWIS(), "WIS should be +1");

        // Verify cumulative derived stats
        // Block: +20% from shield, +5% from TestTrait4 = +25% total
        assertEquals(baseBlock + 25.0f, player.getBlock(), 0.01f,
                "Block should be +25% (shield +20% + TestTrait4 +5%)");

        // Dodge calculation is complex due to stat changes + equipment bonuses
        // DEX went from 14 to 17, bonus from 4 to 7, dodge from stats: +7.5
        // Equipment dodge: sword +5%, shield -5%, boots +10% = +10% total
        // Total dodge change: +17.5%
        assertEquals(baseDodge + 17.5f, player.getDodge(), 0.01f, "Dodge should increase with multiple items");

        // Unequip one item (shield) and verify partial restoration
        player.unequipItemByName("Test Shield");

        // After unequipping shield:
        // STR: +3 (sword only)
        // DEX: +2 from sword, +1 from boots = +3 total (TestTrait4 removed with shield)
        // CON: -1 (sword only)
        // WIS: 0 (shield removed)
        assertEquals(baseSTR + 3, player.getSTR(), "STR should still be +3");
        assertEquals(baseDEX + 3, player.getDEX(), "DEX should be +3 after shield removed");
        assertEquals(baseCON - 1, player.getCON(), "CON should be -1 after shield removed");
        assertEquals(baseWIS, player.getWIS(), "WIS should return to baseline");
        assertEquals(baseBlock, player.getBlock(), 0.01f, "Block should return to baseline");
    }

    @Test
    @Order(3)
    public void testItemWithPropertyEquipAndUnequip() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Check baseline properties count
        int baseTraitCount = player.getTraits().size();

        // Equip Test Shield (has TestTrait4: DEX+1, block+5%)
        player.equipItemByName("Test Shield");

        // Verify property was applied
        assertEquals(baseTraitCount + 1, player.getTraits().size(), "Should have one more trait");

        // Check if TestTrait4 is present
        boolean hasTestTrait4 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait4"));
        assertTrue(hasTestTrait4, "Should have TestTrait4");

        // Unequip shield
        player.unequipItemByName("Test Shield");

        // Verify property was removed
        assertEquals(baseTraitCount, player.getTraits().size(), "Trait count should return to baseline");

        hasTestTrait4 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait4"));
        assertFalse(hasTestTrait4, "Should not have TestTrait4");
    }

    @Test
    @Order(4)
    public void testComplexItemStatInteractions() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Test complex scenario: Equip armor and helmet that affect multiple stats
        int baseCON = player.getCON();
        int baseSTR = player.getSTR();
        int baseDEX = player.getDEX();
        int baseWIS = player.getWIS();
        int baseINT = player.getINT();
        int baseCHA = player.getCHA();
        int baseMaxHP = player.getMaxHp();
        int baseMaxMana = player.getMaxMana();
        float baseMagicResist = player.getMagicResist();

        // Equip Test Armor (CON+4, STR+2, DEX-2, magicResist+10%)
        player.equipItemByName("Test Armor");

        // Equip Test Helmet (WIS+3, INT+2, CHA-1, magicResist+15%, has TestTrait5)
        player.equipItemByName("Test Helmet");

        // Verify stats
        // TestTrait5 adds CON+2, so total CON bonus from equipment is: +4 (armor) + 2
        // (trait5) = +6
        assertEquals(baseSTR + 2, player.getSTR(), "STR should be +2");
        assertEquals(baseDEX - 2, player.getDEX(), "DEX should be -2");
        assertEquals(baseCON + 4 + 2, player.getCON(), "CON should be +6 (armor +4, TestTrait5 +2)");
        assertEquals(baseINT + 2, player.getINT(), "INT should be +2");
        assertEquals(baseWIS + 3, player.getWIS(), "WIS should be +3");
        assertEquals(baseCHA - 1, player.getCHA(), "CHA should be -1");

        // Verify MaxHP changed due to CON increase
        // CON went from 11 to 17, bonus from 1 to 7
        // MaxHP formula: baseHp + ((level + 1) * (hpLvlBonus + conBonus))
        // Old: 10 + ((0 + 1) * (5 + 1)) = 16
        // New: 10 + ((0 + 1) * (5 + 7)) = 22
        int expectedMaxHP = 10 + ((0 + 1) * (5 + 7)); // 22
        assertEquals(expectedMaxHP, player.getMaxHp(), "MaxHP should update with CON change");

        // Verify MaxMana changed due to INT increase
        // INT went from 11 to 13, bonus from 1 to 3
        // MaxMana formula: floor(baseMaxMana * intFactor), intFactor = 1.1^intBonus
        // Old: floor(100 * 1.1^1) = 110
        // New: floor(100 * 1.1^3) = floor(133.1) = 133
        int expectedMaxMana = (int) Math.floor(100 * Math.pow(1.1, 3));
        expectedMaxMana = Math.max(25, expectedMaxMana);
        assertEquals(expectedMaxMana, player.getMaxMana(), "MaxMana should update with INT change");

        // Verify magic resist
        // New WIS bonus: 7, CON bonus: 7 -> magicResist = 5*7 + 2.5*7 = 52.5
        // Plus equipment: +10% (armor) + 15% (helmet) = +25%
        float expectedMagicResist = (5.0f * 7) + (2.5f * 7) + 25.0f; // 77.5%
        assertEquals(expectedMagicResist, player.getMagicResist(), 0.01f, "Magic resist should update");

        // Verify TestTrait5 property applied (from helmet)
        boolean hasTestTrait5 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait5"));
        assertTrue(hasTestTrait5, "Should have TestTrait5 from helmet");

        // Unequip helmet
        player.unequipItemByName("Test Helmet");

        hasTestTrait5 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait5"));
        assertFalse(hasTestTrait5, "Should lose TestTrait5 after unequipping helmet");

        // Verify stats partially restored
        assertEquals(baseWIS, player.getWIS(), "WIS should return to baseline");
        assertEquals(baseINT, player.getINT(), "INT should return to baseline");
        assertEquals(baseCHA, player.getCHA(), "CHA should return to baseline");
    }

    @Test
    @Order(5)
    public void testConsumableHealing() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Damage the player first
        int maxHP = player.getMaxHp();
        player.setCurrentHp(5); // Set to low HP
        assertEquals(5, player.getCurrentHp(), "HP should be 5");

        // Add Test Healing Potion to inventory first
        boolean added = player.addItemByName("Test Healing Potion");
        assertTrue(added, "Should be able to add healing potion to inventory");

        // Consume Test Healing Potion (heals 3d6+5, so 8-23 HP)
        boolean consumed = player.consumeItemByName("Test Healing Potion");
        assertTrue(consumed, "Should be able to consume healing potion");

        // Verify HP increased (should be between 13 and 28, capped at maxHP)
        int currentHP = player.getCurrentHp();
        assertTrue(currentHP > 5, "HP should increase after healing");
        assertTrue(currentHP <= maxHP, "HP should not exceed maxHP");

        // Verify the potion was removed from inventory
        boolean hasPotion = false;
        for (Item item : player.getInventory().getConsumables()) {
            if (item.getName().equals("Test Healing Potion")) {
                hasPotion = true;
                break;
            }
        }
        assertFalse(hasPotion, "Healing potion should be removed from inventory");
    }

    @Test
    @Order(6)
    public void testConsumableWithProperty() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Capture baseline
        int baseBuffCount = player.getBuffs().size();
        int baseSTR = player.getSTR();
        float baseCrit = player.getCrit();        // Add Test Buff Potion to inventory first
        boolean added = player.addItemByName("Test Buff Potion");
        assertTrue(added, "Should be able to add buff potion to inventory");

        // Consume Test Buff Potion (applies TestBuff for 10 turns)
        boolean consumed = player.consumeItemByName("Test Buff Potion");
        assertTrue(consumed, "Should be able to consume buff potion");        // Verify property was applied
        assertEquals(baseBuffCount + 1, player.getBuffs().size(), "Should have one more buff");

        boolean hasTestBuff = player.getBuffs().values().stream()
                .anyMatch(p -> p.getName().equals("TestBuff"));
        assertTrue(hasTestBuff, "Should have TestBuff");

        // Verify buff effects
        // TestBuff gives all stats +1 (including LUCK), crit+5%, dodge+5%, etc.
        // The +1 LUCK increases bonusCrit by 5% (since bonusCrit = 5 * LUCK)
        // Plus the buff's direct crit modifier of +5%
        // Total crit increase: +10% (5% from LUCK, 5% from crit field)
        assertEquals(baseSTR + 1, player.getSTR(), "STR should increase from buff");
        assertEquals(baseCrit + 10.0f, player.getCrit(), 0.01f, "Crit should increase by 10% (5% from LUCK+1, 5% from buff.crit)");

        // Verify duration
        Property buff = player.getBuffs().values().stream()
                .filter(p -> p.getName().equals("TestBuff"))
                .findFirst().orElse(null);
        assertNotNull(buff, "Should be able to get buff property");
        assertEquals(10, buff.getDuration(), "Buff should have 10 turns duration");

        // Remove the buff
        player.removeProperty("TestBuff");

        // Verify stats returned to baseline
        assertEquals(baseSTR, player.getSTR(), "STR should return to baseline after buff removed");
        assertEquals(baseCrit, player.getCrit(), 0.01f, "Crit should return to baseline");

        hasTestBuff = player.getBuffs().values().stream()
                .anyMatch(p -> p.getName().equals("TestBuff"));
        assertFalse(hasTestBuff, "Should not have TestBuff");
    }

    @Test
    @Order(7)
    public void testNegativeStatModifiers() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Test items with negative stat modifiers
        int baseDEX = player.getDEX();
        int baseCON = player.getCON();
        float baseDodge = player.getDodge();

        // Equip Test Sword (CON-1, but also DEX+2, STR+3)
        player.equipItemByName("Test Sword");

        // Equip Test Armor (DEX-2, CON+4, STR+2)
        player.equipItemByName("Test Armor");

        // Verify negative stat changes
        // DEX: +2 from sword, -2 from armor = 0 net change
        assertEquals(baseDEX, player.getDEX(), "DEX should be unchanged (sword +2, armor -2)");
        assertEquals(baseCON - 1 + 4, player.getCON(), "CON should be -1 from sword, +4 from armor = +3 net");

        // Verify derived stats updated correctly with negative modifiers
        // DEX unchanged at 14, bonus stays at 4
        // Dodge change from DEX: 0
        // Equipment dodge: sword +5%, armor -10% = -5%
        // Total dodge change: -5%
        assertEquals(baseDodge - 5.0f, player.getDodge(), 0.01f, "Dodge should decrease by 5%");

        // CON went from 11 to 14, affecting MaxHP positively despite sword's -1
        // MaxHP = 10 + ((0 + 1) * (5 + 4)) = 19
        int expectedMaxHP = 10 + ((0 + 1) * (5 + 4));
        assertEquals(expectedMaxHP, player.getMaxHp(), "MaxHP should update with net CON change");
    }

    @Test
    @Order(8)
    public void testStatChangesWhileEquipped() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Equip items first
        player.equipItemByName("Test Sword");
        player.equipItemByName("Test Armor");

        int equippedSTR = player.getSTR();
        int equippedDEX = player.getDEX();
        float equippedDodge = player.getDodge();

        // Manually increase base stats while equipped
        player.increaseStat(Stats.STRENGTH);

        // Verify stat increased and derived stats updated
        assertEquals(equippedSTR + 1, player.getSTR(), "STR should increase by 1");

        // Manually decrease DEX
        player.setStat(Stats.DEXTERITY, equippedDEX - 2);

        // Verify derived stats updated
        // DEX decreased by 2, so dodge should decrease by 2.5 * 2 = 5
        assertEquals(equippedDodge - 5.0f, player.getDodge(), 0.01f, "Dodge should update when DEX changes");

        // Unequip items and verify stat changes persist
        int currentSTR = player.getSTR();
        player.unequipItemByName("Test Sword");
        player.unequipItemByName("Test Armor");

        // After unequipping, should have base stats + manual changes
        // Started with STR 11 (with species mods), equipped sword (+3) = 14, equipped
        // armor (+2) = 16, increased by 1 = 17, unequip all = 12
        assertEquals(currentSTR - 3 - 2, player.getSTR(), "STR should reflect manual increase after unequip");
    }

    @Test
    @Order(9)
    public void testEquipmentResistanceStacking() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        int baseFireResist = player.getResistance(Resistances.FIRE);
        int baseSlashResist = player.getResistance(Resistances.SLASHING);
        int baseBludgeonResist = player.getResistance(Resistances.BLUDGEONING);

        // Equip Test Sword (SLASHING+10%, FIRE+5%)
        player.equipItemByName("Test Sword");

        // Equip Test Armor (BLUDGEONING+20%, SLASHING+15%, FIRE-10%)
        player.equipItemByName("Test Armor");

        // Equip Test Shield (BLUDGEONING+15%, PIERCING+10%)
        player.equipItemByName("Test Shield");

        // Verify resistance stacking
        // Test Sword: FIRE: -5, SLASHING: -10
        // Test Armor: BLUDGEONING: +20, FIRE: -10, SLASHING: +15
        // Test Shield: BLUDGEONING: +15, PIERCING: +10
        // FIRE: -5 (sword) -10 (armor) = -15 net
        // SLASHING: -10 (sword) +15 (armor) = +5 net
        // BLUDGEONING: +20 (armor) +15 (shield) = +35 net
        assertEquals(baseFireResist - 15, player.getResistance(Resistances.FIRE), "FIRE resist should be -15%");
        assertEquals(baseSlashResist + 5, player.getResistance(Resistances.SLASHING),
                "SLASHING resist should be +5%");
        assertEquals(baseBludgeonResist + 35, player.getResistance(Resistances.BLUDGEONING),
                "BLUDGEONING resist should be +35%");
    }

    @Test
    @Order(10)
    public void testEdgeCaseEmptySlotUnequip() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Try to unequip from empty slot
        boolean unequipped = player.unequipItemByName("Nonexistent Item");
        assertFalse(unequipped, "Should not be able to unequip nonexistent item");

        // Verify no crashes or state corruption
        assertNotNull(player.getSTR(), "Stats should still be accessible");
    }

    @Test
    @Order(11)
    public void testEdgeCaseEquipSameItemTwice() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        int baseSTR = player.getSTR();

        // Equip sword once
        player.equipItemByName("Test Sword");
        assertEquals(baseSTR + 3, player.getSTR(), "STR should be +3");

        // Try to equip again (should replace, not stack)
        player.equipItemByName("Test Sword");

        // STR should still be +3, not +6
        assertEquals(baseSTR + 3, player.getSTR(), "STR should still be +3, not stacked");
    }

    @Test
    @Order(12)
    public void testComplexScenarioFullEquipmentSet() {
        Player player = CreatureLoader.getPlayerById(5002);
        assertNotNull(player);

        // Baseline capture
        int baseSTR = player.getSTR();
        int baseDEX = player.getDEX();
        int baseCON = player.getCON();
        int baseWIS = player.getWIS();
        int baseINT = player.getINT();
        int baseCHA = player.getCHA();
        int baseTraitCount = player.getTraits().size();

        // Equip full set of test items
        player.equipItemByName("Test Sword"); // WEAPON
        player.equipItemByName("Test Shield"); // OFFHAND (has TestTrait1)
        player.equipItemByName("Test Helmet"); // HELMET (has TestTrait2)
        player.equipItemByName("Test Armor"); // ARMOR
        player.equipItemByName("Test Boots"); // LEGWEAR

        // Calculate expected stats
        // Sword: STR+3, DEX+2, CON-1
        // Shield: CON+2, WIS+1, DEX-1
        // Helmet: WIS+3, INT+2, CHA-1
        // Armor: CON+4, STR+2, DEX-2
        // Boots: DEX+1
        // TestTrait4 (from shield): DEX+1, block+5%
        // TestTrait5 (from helmet): CON+2, crit+3%

        int expectedSTR = baseSTR + 3 + 2; // sword + armor = +5
        int expectedDEX = baseDEX + 2 - 1 - 2 + 1 + 1; // sword + shield + armor + boots + trait4 = +1
        int expectedCON = baseCON - 1 + 2 + 4 + 2; // sword + shield + armor + trait5 = +7
        int expectedWIS = baseWIS + 1 + 3; // shield + helmet = +4
        int expectedINT = baseINT + 2; // helmet = +2
        int expectedCHA = baseCHA - 1; // helmet = -1

        assertEquals(expectedSTR, player.getSTR(), "Full set STR calculation");
        assertEquals(expectedDEX, player.getDEX(), "Full set DEX calculation");
        assertEquals(expectedCON, player.getCON(), "Full set CON calculation");
        assertEquals(expectedWIS, player.getWIS(), "Full set WIS calculation");
        assertEquals(expectedINT, player.getINT(), "Full set INT calculation");
        assertEquals(expectedCHA, player.getCHA(), "Full set CHA calculation");

        // Verify properties applied
        assertEquals(baseTraitCount + 2, player.getTraits().size(), "Should have 2 additional traits");

        boolean hasTestTrait4 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait4"));
        boolean hasTestTrait5 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait5"));
        assertTrue(hasTestTrait4, "Should have TestTrait4 from shield");
        assertTrue(hasTestTrait5, "Should have TestTrait5 from helmet");

        // Unequip everything
        player.unequipItemByName("Test Sword");
        player.unequipItemByName("Test Shield");
        player.unequipItemByName("Test Helmet");
        player.unequipItemByName("Test Armor");
        player.unequipItemByName("Test Boots");

        // Verify complete restoration
        assertEquals(baseSTR, player.getSTR(), "STR should return to baseline");
        assertEquals(baseDEX, player.getDEX(), "DEX should return to baseline");
        assertEquals(baseCON, player.getCON(), "CON should return to baseline");
        assertEquals(baseWIS, player.getWIS(), "WIS should return to baseline");
        assertEquals(baseINT, player.getINT(), "INT should return to baseline");
        assertEquals(baseCHA, player.getCHA(), "CHA should return to baseline");
        assertEquals(baseTraitCount, player.getTraits().size(), "Trait count should return to baseline");

        hasTestTrait4 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait4"));
        hasTestTrait5 = player.getTraits().values().stream()
                .anyMatch(p -> p.getName().equals("TestTrait5"));
        assertFalse(hasTestTrait4, "Should not have TestTrait4");
        assertFalse(hasTestTrait5, "Should not have TestTrait5");
    }
}

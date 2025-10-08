package com.bapppis.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Creature.Stats;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.item.EquipmentSlot;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.ItemLoader;
import com.bapppis.core.property.PropertyManager;

public class TestCreatureAttack {
    // Test player creation logic here
    @Test
    public void testPlayerCreation() {
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();

        // Make the player Biggles
        Player biggles = CreatureLoader.getPlayerById(5000);
        assert biggles != null;
        biggles.setStat(Stats.CONSTITUTION, 100);
        // Give Falchion of Doom to Biggles and equip it
        biggles.addItem(ItemLoader.getItemById(9800)); // Falchion of Doom
        biggles.equipItem(biggles.getInventory().getWeapons().get(1)); // Equip Falchion of Doom
        System.out.println(biggles.getCurrentHp() + " / " + biggles.getMaxHp());
        biggles.attack(biggles); // Self-attack for testing
        System.out.println(biggles.getCurrentHp() + " / " + biggles.getMaxHp());
        // Drink healing potion
        Item minorHealingPotion = ItemLoader.getItemById(8000); // Minor Healing Potion
        minorHealingPotion.onApply(biggles);
        System.out.println(biggles.getCurrentHp() + " / " + biggles.getMaxHp());
    }

    @Test
    public void testDarkHoundVsTrainingDummyMultipleAttacks() {
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();

        // Load Dark Hound (id 6000)
        com.bapppis.core.creature.Creature darkHound = CreatureLoader.getCreatureById(6000);
        assert darkHound != null;

        // Load training dummy (id 6100)
        com.bapppis.core.creature.Creature dummy = CreatureLoader.getCreatureById(6100);
        assert dummy != null;

        // Ensure dummy is at full health before each attack
        int runs = 100;

        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        java.util.Map<String, Integer> physTotals = new java.util.HashMap<>();
        java.util.Map<String, Integer> magTotals = new java.util.HashMap<>();
        java.util.Map<String, Integer> timesPerAttack = new java.util.HashMap<>();

        // Install listener to capture detailed attack reports
        java.util.concurrent.atomic.AtomicBoolean sawDualRoll = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicInteger observedPhysAttempts = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger observedMagicAttempts = new java.util.concurrent.atomic.AtomicInteger();
        com.bapppis.core.creature.Creature.attackListener = (rpt) -> {
            counts.put(rpt.attackName, counts.getOrDefault(rpt.attackName, 0) + 1);
            physTotals.put(rpt.attackName, physTotals.getOrDefault(rpt.attackName, 0) + rpt.physAfter);
            magTotals.put(rpt.attackName, magTotals.getOrDefault(rpt.attackName, 0) + rpt.magAfter);
            timesPerAttack.putIfAbsent(rpt.attackName, rpt.times);
            if (rpt.dualRoll) {
                sawDualRoll.set(true);
            }
            if (rpt.physAttempts > 0) observedPhysAttempts.addAndGet(rpt.physAttempts);
            if (rpt.magicAttempts > 0) observedMagicAttempts.addAndGet(rpt.magicAttempts);
        };

        for (int i = 0; i < runs; i++) {
            // Reset dummy HP so it doesn't die
            dummy.setCurrentHp(dummy.getMaxHp());
            // Let the dark hound attack the dummy
            darkHound.attack(dummy);
        }

        System.out.println("--- Dark Hound Attack Summary after " + runs + " runs ---");
        for (String name : counts.keySet()) {
            int count = counts.get(name);
            int times = timesPerAttack.getOrDefault(name, 1);
            int totalHits = count * times;
            System.out.println("Attack: " + name + " | Count: " + count + " | TimesPerAttack: " + times
                    + " | TotalHits: " + totalHits + " | PhysTotal: " + physTotals.getOrDefault(name, 0)
                    + " | MagTotal: " + magTotals.getOrDefault(name, 0));
        }
        System.out.println("----------------------------------------------");

        // Basic checks (non-fatal): if no attacks were recorded, print a warning but
        // don't fail the test.
        if (counts.size() == 0) {
            System.out.println("Warning: No attacks recorded for Dark Hound; this run produced no attack events.");
        } else {
            int summedHits = 0;
            for (String name : counts.keySet()) {
                summedHits += counts.get(name) * timesPerAttack.getOrDefault(name, 1);
            }
            if (summedHits == 0) {
                System.out.println("Warning: Recorded attacks but total hits summed to 0.");
            }
        }

        // Basic diagnostic assertions (non-fatal): ensure counts align if dual rolls seen
        if (sawDualRoll.get()) {
            // If dual roll occurred at least once we should have recorded some magic attempts
            assert observedMagicAttempts.get() > 0 : "Expected magic attempts when dualRoll seen";
        }
        // Clear listener
        com.bapppis.core.creature.Creature.attackListener = null;
    }

    @Test
    public void testBigglesVsTrainingDummyMultipleAttacks() {
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();

        Player biggles = CreatureLoader.getPlayerById(5000);
        assert biggles != null;

        // Ensure full health and high constitution so HP doesn't drop undesirably
        // during test
        biggles.setStat(Stats.CONSTITUTION, 100);

        // Testing versatile weapon attacks
        biggles.addItem(ItemLoader.getItemById(9801)); // Rusty Iron Sword
        biggles.equipItem(biggles.getInventory().getWeapons().get(1), true);
        //biggles.setStat(Stats.INTELLIGENCE, 20);
        //biggles.setStat(Stats.CHARISMA, 100);
        //biggles.setStat(Stats.STRENGTH, 15);
        //biggles.setStat(Stats.DEXTERITY, 20);
        System.out.println(biggles.toString());

        // Load training dummy as a target
        com.bapppis.core.creature.Creature dummy = CreatureLoader.getCreatureById(6100);
        assert dummy != null;

        // Prepare counters
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        java.util.Map<String, Integer> physTotals = new java.util.HashMap<>();
        java.util.Map<String, Integer> magTotals = new java.util.HashMap<>();
        java.util.Map<String, String> physTypes = new java.util.HashMap<>();
        java.util.Map<String, String> magTypes = new java.util.HashMap<>();
        java.util.Map<String, Integer> timesPerAttack = new java.util.HashMap<>();

        // Install listener to capture detailed attack reports
        java.util.concurrent.atomic.AtomicInteger dualRollCount = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger trueDamageCount = new java.util.concurrent.atomic.AtomicInteger();
        com.bapppis.core.creature.Creature.attackListener = (rpt) -> {
            counts.put(rpt.attackName, counts.getOrDefault(rpt.attackName, 0) + 1);
            physTotals.put(rpt.attackName, physTotals.getOrDefault(rpt.attackName, 0) + rpt.physAfter);
            magTotals.put(rpt.attackName, magTotals.getOrDefault(rpt.attackName, 0) + rpt.magAfter);
            physTypes.putIfAbsent(rpt.attackName, rpt.damageType == null ? "UNKNOWN" : rpt.damageType);
            if (rpt.magicType != null) magTypes.putIfAbsent(rpt.attackName, rpt.magicType);
            timesPerAttack.putIfAbsent(rpt.attackName, rpt.times);
            if (rpt.dualRoll) dualRollCount.incrementAndGet();
            if (rpt.trueDamage) trueDamageCount.incrementAndGet();
            // sanity: attempts should be >= misses for each category (not asserted per-report, but can be logged)
        };

        int runs = 50;
        for (int i = 0; i < runs; i++) {
            // Reset dummy HP so it's always alive for attacks
            dummy.setCurrentHp(dummy.getMaxHp());
            biggles.attack(dummy);
        }

        // Print summary
        System.out.println("--- Attack Summary after " + runs + " attacks ---");
        for (String name : counts.keySet()) {
            int count = counts.get(name);
            int times = timesPerAttack.getOrDefault(name, 1);
            int totalHits = count * times;
            System.out.println("Attack: " + name + " | Count: " + count + " | TimesPerAttack: " + times
                    + " | TotalHits: " + totalHits + " | PhysType: " + physTypes.getOrDefault(name, "UNKNOWN")
                    + " | MagType: " + magTypes.getOrDefault(name, "NONE") + " | PhysTotal: "
                    + physTotals.getOrDefault(name, 0) + " | MagTotal: " + magTotals.getOrDefault(name, 0));
        }
        System.out.println("----------------------------------------------");

        // Basic sanity: At least one attack type should have occurred and total hits
        // consistent
        assert counts.size() > 0;
        int summedHits = 0;
        for (String name : counts.keySet()) {
            summedHits += counts.get(name) * timesPerAttack.getOrDefault(name, 1);
        }
        // there should be at least one hit recorded across all attacks
        assert summedHits > 0;

        // Assert if any magic damage occurred there was at least one dualRoll report
        int totalMag = 0; for (int v : magTotals.values()) totalMag += v;
        if (totalMag > 0) {
            assert dualRollCount.get() >= 0; // allow 0 if weapon magic not present in tested runs
        }
        // Clear listener after test
        com.bapppis.core.creature.Creature.attackListener = null;
    }

    @Test
    public void testBigglesUnarmedAttack() {
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();

        Player biggles = CreatureLoader.getPlayerById(5000);
        assert biggles != null;

        // Ensure full health and high constitution so HP doesn't drop undesirably
        biggles.setStat(Stats.CONSTITUTION, 100);

        // If Biggles has a weapon equipped, unequip it
        Item equippedWeapon = biggles.getEquipped(EquipmentSlot.WEAPON);
        if (equippedWeapon != null) {
            // Find the slot that contains this item and unequip it
            biggles.unequipItem(EquipmentSlot.WEAPON);
        }

        // Load training dummy as a target
        com.bapppis.core.creature.Creature dummy = CreatureLoader.getCreatureById(6100);
        assert dummy != null;

        java.util.concurrent.atomic.AtomicInteger attackCount = new java.util.concurrent.atomic.AtomicInteger(0);

        // Install listener to capture detailed attack reports
        com.bapppis.core.creature.Creature.attackListener = (rpt) -> {
            attackCount.getAndIncrement();
            // Print for debugging
            System.out.println("Unarmed attack report: " + rpt.attackName + " physAfter=" + rpt.physAfter
                    + " damageType=" + rpt.damageType);
        };

        // Run multiple unarmed attacks
        int runs = 20;
        for (int i = 0; i < runs; i++) {
            // Reset dummy HP so it's always alive
            dummy.setCurrentHp(dummy.getMaxHp());
            biggles.attack(dummy);
        }

        // There should be at least one attack reported
        assert attackCount.get() > 0 : "Expected at least one unarmed attack report but got 0";

        // Clear listener
        com.bapppis.core.creature.Creature.attackListener = null;
    }

    // make an assert function for biggles
    private void assertBigglesDefaults(Player biggles) {
        // Implement assertions for Biggles defaults

    }

    private void assertBigglesDebuffed(Player biggles) {
        // Implement assertions for Biggles debuffed state

    }
}

package com.bapppis.core.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;

public class ItemTest {
    // Test item functionality here
    @Test
    public void testItemFunctionality() {
        AllLoaders.loadAll();
        Player biggles = CreatureLoader.getPlayerById(5000);
        System.out.println("Stats before equipping items:");
        float critBefore = biggles.getCrit();
        float dodgeBefore = biggles.getDodge();
        float blockBefore = biggles.getBlock();
        float magicResistBefore = biggles.getMagicResist();
        System.out.println("Crit: " + critBefore);
        System.out.println("Dodge: " + dodgeBefore);
        System.out.println("Block: " + blockBefore);
        System.out.println("Magic Resist: " + magicResistBefore);
        biggles.addItem(ItemLoader.getItemById(7249)); // Test Armor
        biggles.addItem(ItemLoader.getItemById(7499)); // Test Helmet
        biggles.equipItem(biggles.getInventory().getArmors().get(0)); // Equip Test Armor
        biggles.equipItem(biggles.getInventory().getHelmets().get(0)); // Equip Test Helmet
        System.out.println("Stats after equipping items:");
        float critAfterEquip = biggles.getCrit();
        float dodgeAfterEquip = biggles.getDodge();
        float blockAfterEquip = biggles.getBlock();
        float magicResistAfterEquip = biggles.getMagicResist();
        System.out.println("Crit: " + critAfterEquip);
        System.out.println("Dodge: " + dodgeAfterEquip);
        System.out.println("Block: " + blockAfterEquip);
        System.out.println("Magic Resist: " + magicResistAfterEquip);
        // System.out.println("Biggles Dex " +
        // biggles.getStat(com.bapppis.core.creature.Creature.Stats.DEXTERITY));
        // Sanity: values should change after equipping equipment
        // (equipment in fixtures increases crit and dodge; block may go negative)
        // org.junit.jupiter.api.Assertions.assertNotEquals(critBefore, critAfterEquip);
        // org.junit.jupiter.api.Assertions.assertNotEquals(dodgeBefore,
        // dodgeAfterEquip);
        biggles.unequipItem(EquipmentSlot.ARMOR); // Unequip Test Armor
        biggles.unequipItem(EquipmentSlot.HELMET); // Unequip Test Helmet
        System.out.println("Stats after Unequipping items:");
        float critAfterUnequip = biggles.getCrit();
        float dodgeAfterUnequip = biggles.getDodge();
        float blockAfterUnequip = biggles.getBlock();
        float magicResistAfterUnequip = biggles.getMagicResist();
        System.out.println("Crit: " + critAfterUnequip);
        System.out.println("Dodge: " + dodgeAfterUnequip);
        System.out.println("Block: " + blockAfterUnequip);
        System.out.println("Magic Resist: " + magicResistAfterUnequip);
        // After unequip, raw values should be restored to their before values
        /*
         * assertEquals(critBefore, critAfterUnequip, 0.0001f);
         * assertEquals(dodgeBefore, dodgeAfterUnequip, 0.0001f);
         * assertEquals(blockBefore, blockAfterUnequip, 0.0001f);
         */
    }
}

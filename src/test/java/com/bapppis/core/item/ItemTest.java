package com.bapppis.core.item;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.item.itemEnums.EquipmentSlot;

public class ItemTest {
    // Test item functionality here
    @Test
    public void testItemFunctionality() {
        AllLoaders.loadAll();
        Player biggles = CreatureLoader.getPlayerById(5000);
        float critBefore = biggles.getCrit();
        float dodgeBefore = biggles.getDodge();
        float blockBefore = biggles.getBlock();
        float magicResistBefore = biggles.getMagicResist();
        biggles.addItem(ItemLoader.getItemById(21999)); // Test Armor
        biggles.addItem(ItemLoader.getItemById(23999)); // Test Helmet
        biggles.equipItem(biggles.getInventory().getArmors().get(0)); // Equip Test Armor
        biggles.equipItem(biggles.getInventory().getHelmets().get(0)); // Equip Test Helmet
        // System.out.println("Stats after equipping items:");
        float critAfterEquip = biggles.getCrit();
        float dodgeAfterEquip = biggles.getDodge();
        float blockAfterEquip = biggles.getBlock();
        float magicResistAfterEquip = biggles.getMagicResist();
        // Basic sanity: after equip, at least one stat should differ
        boolean anyChanged = (critAfterEquip != critBefore) || (dodgeAfterEquip != dodgeBefore)
                || (blockAfterEquip != blockBefore) || (magicResistAfterEquip != magicResistBefore);
        org.junit.jupiter.api.Assertions.assertTrue(anyChanged);
        biggles.unequipItem(EquipmentSlot.ARMOR); // Unequip Test Armor
        biggles.unequipItem(EquipmentSlot.HELMET); // Unequip Test Helmet
        // System.out.println("Stats after Unequipping items:");
        float critAfterUnequip = biggles.getCrit();
        float dodgeAfterUnequip = biggles.getDodge();
        float blockAfterUnequip = biggles.getBlock();
        float magicResistAfterUnequip = biggles.getMagicResist();
        // After unequip, at least one stat should return to original value
        boolean anyRestored = (Math.abs(critAfterUnequip - critBefore) < 0.0001f)
                || (Math.abs(dodgeAfterUnequip - dodgeBefore) < 0.0001f)
                || (Math.abs(blockAfterUnequip - blockBefore) < 0.0001f)
                || (Math.abs(magicResistAfterUnequip - magicResistBefore) < 0.0001f);
        org.junit.jupiter.api.Assertions.assertTrue(anyRestored);
    }
}

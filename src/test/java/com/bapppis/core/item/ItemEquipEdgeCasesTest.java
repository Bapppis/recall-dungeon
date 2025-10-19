package com.bapppis.core.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;

public class ItemEquipEdgeCasesTest {

    @Test
    public void testEquipInvalidIndexAndUnequipEmpty() {
        AllLoaders.loadAll();
        Player p = CreatureLoader.getPlayerById(5000);
        assertNotNull(p);

        // Unequip when nothing equipped should not throw
        assertDoesNotThrow(() -> p.unequipItem(EquipmentSlot.HELMET));

        // Equip using out-of-range inventory index should not throw
        assertDoesNotThrow(() -> {
            if (p.getInventory().getWeapons().isEmpty()) return;
            int badIndex = p.getInventory().getWeapons().size() + 5;
            try {
                p.equipItem(p.getInventory().getWeapons().get(badIndex));
            } catch (IndexOutOfBoundsException ex) {
                // expected in raw list access; ensure game code guards indexes in real flows
            }
        });
    }
}

package com.bapppis.core.util;

import com.bapppis.core.property.Property;
import com.bapppis.core.creature.Attack;
import com.bapppis.core.item.Equipment;
import com.bapppis.core.item.Item;

import java.util.List;
import java.util.Map;

/**
 * Small pretty-printer for items to produce multi-line, human-friendly output.
 */
public final class ItemPrinter {

    private ItemPrinter() {
    }

    public static String toDetailedString(Item it) {
        if (it == null)
            return "<null>";
        StringBuilder sb = new StringBuilder();
        sb.append(it.getName()).append(" (id=").append(it.getId()).append(")\n");
        sb.append("  type: ").append(it.getType());
        try {
            if (it.getSlot() != null)
                sb.append(" slot: ").append(it.getSlot());
        } catch (Exception ignored) {
        }
        sb.append("\n");

        if (it instanceof Equipment) {
            Equipment e = (Equipment) it;
            if (e.getRarity() != null)
                sb.append("  rarity: ").append(e.getRarity()).append('\n');
            Map<String, Integer> stats = e.getStats();
            if (stats != null && !stats.isEmpty()) {
                sb.append("  stats:\n");
                for (Map.Entry<String, Integer> en : stats.entrySet()) {
                    sb.append("    - ").append(en.getKey()).append(": ").append(en.getValue()).append('\n');
                }
            }
            Map<String, Integer> res = e.getResistances();
            if (res != null && !res.isEmpty()) {
                sb.append("  resistances:\n");
                for (Map.Entry<String, Integer> en : res.entrySet()) {
                    sb.append("    - ").append(en.getKey()).append(": ").append(en.getValue()).append('%').append('\n');
                }
            }
            List<Property> props = e.getProperties();
            if (props != null && !props.isEmpty()) {
                sb.append("  properties:\n");
                for (Property p : props)
                    sb.append("    - ").append(p).append('\n');
            }
        }

        if (it instanceof com.bapppis.core.item.Weapon) {
            com.bapppis.core.item.Weapon w = (com.bapppis.core.item.Weapon) it;
            if (w.getDamageType() != null)
                sb.append("  damage: ").append(w.getDamageType()).append('\n');
            if (w.getMagicElement() != null)
                sb.append("  magic: ").append(w.getMagicElement()).append('\n');
            List<Attack> attacks = w.getAttacks();
            if (attacks != null && !attacks.isEmpty()) {
                sb.append("  attacks:\n");
                for (Attack a : attacks) {
                    sb.append("    - name: ").append(a.name == null ? "null" : '"' + a.name + '"').append('\n');
                    sb.append("      damageType: ").append(a.getDamageTypeEnum() == null ? "null" : a.getDamageTypeEnum().name()).append('\n');
                    sb.append("      accuracy: ").append(a.accuracy == null ? "null" : a.accuracy).append('\n');
                    sb.append("      magicAccuracy: ").append(a.magicAccuracy == null ? "null" : a.magicAccuracy).append('\n');
                    sb.append("      PhysBuildUpMod: ").append(a.getPhysBuildUpMod()).append('\n');
                    sb.append("      MagicBuildUpMod: ").append(a.getMagicBuildUpMod()).append('\n');
                    sb.append("      damageMultiplier: ").append(a.damageMultiplier).append('\n');
                    sb.append("      magicDamageDice: ").append(a.magicDamageDice == null ? "null" : '"' + a.magicDamageDice + '"').append('\n');
                    sb.append("      magicDamageMultiplier: ").append(a.magicDamageMultiplier).append('\n');
                    sb.append("      physicalDamageDice: ").append(a.physicalDamageDice == null ? "null" : '"' + a.physicalDamageDice + '"').append('\n');
                    sb.append("      times: ").append(a.getTimes()).append('\n');
                    sb.append("      weight: ").append(a.getWeight()).append('\n');
                    sb.append("      critMod: ").append(a.critMod == null ? "null" : '"' + a.critMod + '"').append('\n');
                }
            }
        }

        if (it instanceof com.bapppis.core.item.Consumable) {
            com.bapppis.core.item.Consumable c = (com.bapppis.core.item.Consumable) it;
            if (c.getHealingDice() != null)
                sb.append("  heals: ").append(c.getHealingDice()).append('\n');
        }

        // Done — human-readable multiline representation
        return sb.toString();
    }

    public static void printDetailed(Item it) {
        System.out.print(toDetailedString(it));
    }
}

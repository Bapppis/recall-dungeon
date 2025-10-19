package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.Type;
import com.bapppis.core.item.EquipmentSlot;
import com.bapppis.core.item.Item;
import com.bapppis.core.util.LevelUtil;

public class Enemy extends Creature {
    private Integer enemyXp;

    public Enemy() {
        this.setType(Type.ENEMY);
    }

    public Integer getEnemyXp() {
        return enemyXp;
    }

    public void setEnemyXp(Integer enemyXp) {
        this.enemyXp = enemyXp;
    }

    @Override
    public String toString() {
        // Detailed enemy printout (combat-relevant info)
        StringBuilder sb = new StringBuilder();
        sb.append("Enemy: ").append(getName() == null ? "<unnamed>" : getName()).append(" (Id:").append(getId())
                .append(")\n");
        sb.append("Level: ").append(getLevel()).append(" ")
                .append(getSize() == null ? "" : getSize().name()).append(" ");

        // Print species if available (not base types)
        String species = getClass().getSimpleName();
        if (!species.equals("Player") && !species.equals("Enemy") && !species.equals("NPC")
                && !species.equals("CreatureType") && !species.equals("Creature")) {
            sb.append(species.toUpperCase());
        } else if (getCreatureType() != null) {
            sb.append(getCreatureType().name());
        }
        sb.append("\n");

        sb.append("XP Reward: ").append(enemyXp != null ? enemyXp : "N/A").append("\n");
        sb.append("HP: ").append(getCurrentHp()).append("/").append(getMaxHp()).append("\n");
        sb.append("Mana: ").append(getCurrentMana()).append("/").append(getMaxMana()).append("\n");
        sb.append("Stamina: ").append(getCurrentStamina()).append("/").append(getMaxStamina()).append("\n");
        sb.append("Stats: ")
                .append("STR ").append(getSTR()).append("  ")
                .append("DEX ").append(getDEX()).append("  ")
                .append("CON ").append(getCON()).append("  ")
                .append("INT ").append(getINT()).append("  ")
                .append("WIS ").append(getWIS()).append("  ")
                .append("CHA ").append(getCHA()).append("  ")
                .append("LUCK ").append(getLUCK()).append("\n");
        sb.append("Resists: ");
        boolean first = true;
        for (Resistances res : Resistances.values()) {
            if (!first)
                sb.append(", ");
            sb.append(res.name()).append("=").append(getResistance(res)).append("%");
            first = false;
        }
        sb.append("\n");
        sb.append("Crit: ").append(getCrit()).append("%  ")
                .append("Dodge: ").append(getDodge()).append("%  ")
                .append("Block: ").append(getBlock()).append("%  ")
                .append("MagicResist: ").append(getMagicResist()).append("%\n");
        sb.append("Accuracy: ").append(getAccuracy()).append("  ")
                .append("MagicAccuracy: ").append(getMagicAccuracy()).append("\n");
        sb.append("Equipment:\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item equipped = getEquipped(slot);
            sb.append("  ").append(slot.name()).append(": ");
            sb.append(equipped == null ? "Empty" : equipped.getName()).append("\n");
        }
        sb.append("Properties: ")
                .append("Buffs=").append(getBuffs().size()).append(" ")
                .append("Debuffs=").append(getDebuffs().size()).append(" ")
                .append("Traits=").append(getTraits().size()).append("\n");
        return sb.toString();
    }
}

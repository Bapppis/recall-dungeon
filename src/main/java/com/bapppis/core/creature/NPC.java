package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Type;

public class NPC extends Creature {
    public NPC() {
        this.setType(Type.NPC);
    }

    @Override
    public String toString() {
        // Basic NPC printout (less detail than player/enemy)
        StringBuilder sb = new StringBuilder();
        sb.append("NPC: ").append(getName() == null ? "<unnamed>" : getName()).append(" (Id:").append(getId())
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

        sb.append("HP: ").append(getCurrentHp()).append("/").append(getMaxHp()).append("\n");
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
        return sb.toString();
    }
}

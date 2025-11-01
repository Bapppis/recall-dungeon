package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.creature.creatureEnums.Type;
import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.item.Item;
import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.util.LevelUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Player extends Creature {
    private Coordinate position;
    private int statPoints = 0;

    // Player class system
    private Integer playerClassId; // ID of the player's class (60000-60999 range)
    private int talentPoints = 0; // Available talent points for spending
    private Set<String> learnedTalents = new HashSet<>(); // Talents the player has learned

    public Player() {
        this.setType(Type.PLAYER);
    }

    public static Player fromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(com.bapppis.core.Resistances.class,
                        new com.bapppis.core.util.ResistancesDeserializer())
                .create();
        return gson.fromJson(json, Player.class);
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public void setPosition(int x, int y) {
        this.position = new Coordinate(x, y);
    }

    public int getX() {
        return position != null ? position.getX() : -1;
    }

    public int getY() {
        return position != null ? position.getY() : -1;
    }

    public boolean addItem(Item item) {
        return getInventory().addItem(item);
    }

    public boolean removeItem(Item item) {
        return getInventory().removeItem(item);
    }

    public void addXp(int xp) {
        int currentXp = this.getXp() + xp;
        if (this.getLevel() >= LevelUtil.getMaxLevel()) {
            this.setLevel(LevelUtil.getMaxLevel());
            this.setXp(0);
            return;
        }
        if (currentXp >= LevelUtil.xpForNextLevel(this.getLevel())) {
            levelUp(currentXp);
            return;
        }
        this.setXp(currentXp);
    }

    public void levelUp(int xp) {
        xp -= LevelUtil.xpForNextLevel(this.getLevel());
        this.setLevel(this.getLevel() + 1);

        this.addStatPoint();
        this.addStatPoint();
        this.updateMaxHp();

        this.setXp(0);
        addXp(xp);
    }

    public int getStatPoints() {
        return statPoints;
    }

    public void setStatPoints(int statPoints) {
        this.statPoints = statPoints;
    }

    public void addStatPoint() {
        this.statPoints += 1;
    }

    public void removeStatPoint() {
        if (this.statPoints > 0) {
            this.statPoints -= 1;
        }
    }

    public void spendStatPoint(Stats stat) {
        if (this.statPoints > 0) {
            this.statPoints -= 1;
            increaseStat(stat);
        }
    }

    @Override
    public void finalizeAfterLoad() {
        super.finalizeAfterLoad();

        int tempXp = this.getXp() + LevelUtil.totalXpForLevel(this.getLevel());
        this.setLevel(0);
        this.setXp(0);
        this.addXp(tempXp);
    }

    @Override
    public String toString() {
        // Detailed player printout
        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(getName() == null ? "<unnamed>" : getName()).append(" (Id:").append(getId())
                .append(")\n");
        sb.append("Level: ").append(getLevel()).append(" (").append("XP: ").append(getXp()).append("/")
                .append(LevelUtil.xpForNextLevel(getLevel())).append(") ")
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

        sb.append("Stat Points: ").append(statPoints).append("\n");
        sb.append("Vision Range: ").append(getVisionRange()).append("\n");
        sb.append("HP: ").append(getCurrentHp()).append("/").append(getMaxHp()).append("  (HP regen: ")
                .append(getHpRegen())
                .append("/s)").append("\n");
        sb.append("Mana: ").append(getCurrentMana()).append("/").append(getMaxMana()).append("  (Mana regen: ")
                .append(getManaRegen()).append("/s)").append("\n");
        sb.append("Stamina: ").append(getCurrentStamina()).append("/").append(getMaxStamina())
                .append("  (Stamina regen: ")
                .append(getStaminaRegen()).append("/s)").append("\n");
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
        sb.append("Inventory counts: ")
                .append("Weapons=").append(getInventory().getWeapons().size()).append(", ")
                .append("Offhands=").append(getInventory().getOffhands().size()).append(", ")
                .append("Helmets=").append(getInventory().getHelmets().size()).append(", ")
                .append("Armor=").append(getInventory().getArmors().size()).append(", ")
                .append("Consumables=").append(getInventory().getConsumables().size()).append(", ")
                .append("Misc=").append(getInventory().getMisc().size()).append("\n");
        return sb.toString();
    }

    // Player class system methods

    public Integer getPlayerClassId() {
        return playerClassId;
    }

    public void setPlayerClassId(Integer playerClassId) {
        this.playerClassId = playerClassId;
    }

    public int getTalentPoints() {
        return talentPoints;
    }

    public void setTalentPoints(int talentPoints) {
        this.talentPoints = talentPoints;
    }

    public void addTalentPoint() {
        this.talentPoints++;
    }

    public void spendTalentPoint(String talentName) {
        if (this.talentPoints > 0 && !learnedTalents.contains(talentName)) {
            this.talentPoints--;
            learnedTalents.add(talentName);
        }
    }

    public Set<String> getLearnedTalents() {
        return new HashSet<>(learnedTalents);
    }

    public boolean hasTalent(String talentName) {
        return learnedTalents.contains(talentName);
    }
}

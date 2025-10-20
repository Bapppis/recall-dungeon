package com.bapppis.core.item;


import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.item.itemEnums.ItemType;
import com.bapppis.core.item.itemEnums.Rarity;
import com.bapppis.core.property.Property;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class Equipment implements Item {
    public void setEquipmentSlot(EquipmentSlot slot) {
        this.equipmentSlot = slot;
    }

    public EquipmentSlot getEquipmentSlot() {
        return this.equipmentSlot;
    }
    @Override
    public String toString() {
        return getName() + " (id=" + getId() + ")";
    }
    private int id;
    private String name;
    private String description;
    private Object tooltip;
    private ItemType itemType = ItemType.EQUIPMENT;
    private EquipmentSlot equipmentSlot;
    private Rarity rarity;
    private Map<String, Integer> stats;
    private Map<String, Integer> resistances;
    private Float crit;
    private Float dodge;
    private Float block;
    private Float magicResist;
    private Integer defense;
    private Integer accuracy;
    private Integer magicAccuracy;

    // Field to store property names/IDs from JSON
    @SerializedName("properties")
    private List<String> propertyNames;

    // Actual property objects resolved after loading
    private transient List<Property> properties;

    @Override
    public List<Property> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    @Override
    public void onApply(com.bapppis.core.creature.Creature creature) {
        List<Property> props = getProperties();
        if (props != null) {
            for (Property p : props) {
                if (p != null) {
                    Property equipmentProperty = p.copy();
                    equipmentProperty.setDuration(null);
                    creature.addProperty(equipmentProperty);
                }
            }
        }
    }

    @Override
    public void onRemove(com.bapppis.core.creature.Creature creature) {
        Item.super.onRemove(creature);
    }

    public Equipment() {
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTooltip() {
        return tooltip == null ? null : tooltip.toString();
    }

    @Override
    public ItemType getType() {
        return itemType;
    }

    @Override
    public EquipmentSlot getSlot() {
        return equipmentSlot;
    }

    @Override
    public void setSlot(EquipmentSlot slot) {
        this.equipmentSlot = slot;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    public void setStats(Map<String, Integer> stats) {
        this.stats = stats;
    }

    public Map<String, Integer> getResistances() {
        return resistances;
    }

    public void setResistances(Map<String, Integer> resistances) {
        this.resistances = resistances;
    }

    public Float getCrit() {
        return crit;
    }

    public void setCrit(Float crit) {
        this.crit = crit;
    }

    public Float getDodge() {
        return dodge;
    }

    public void setDodge(Float dodge) {
        this.dodge = dodge;
    }

    public Float getBlock() {
        return block;
    }

    public void setBlock(Float block) {
        this.block = block;
    }

    public Float getMagicResist() {
        return magicResist;
    }

    public void setMagicResist(Float magicResist) {
        this.magicResist = magicResist;
    }

    public Integer getDefense() {
        return defense;
    }

    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getMagicAccuracy() {
        return magicAccuracy;
    }

    public void setMagicAccuracy(Integer magicAccuracy) {
        this.magicAccuracy = magicAccuracy;
    }
}

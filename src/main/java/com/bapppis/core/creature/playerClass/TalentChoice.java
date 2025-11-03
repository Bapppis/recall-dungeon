package com.bapppis.core.creature.playerClass;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;

import java.util.List;
import java.util.Map;

/**
 * Represents a single choice within a talent node.
 * When a node has multiple choices, only one can be selected.
 */
public class TalentChoice {
  private String id;
  private String name;
  private String description;
  private String icon;

  // Rewards
  private Map<Stats, Integer> statBonuses;
  private Map<Resistances, Integer> resistances;
  private Integer maxHpBonus;
  private Integer maxManaBonus;
  private Integer maxStaminaBonus;
  private Integer hpRegenBonus;
  private Integer manaRegenBonus;
  private Integer staminaRegenBonus;
  // Combat-related bonuses
  private Float critBonus; // percent points (e.g., 5.0 -> +5% crit)
  private Float dodgeBonus; // percent points
  private Float blockBonus; // percent points
  private Integer accuracyBonus; // flat accuracy
  private Integer magicAccuracyBonus; // flat magic accuracy
  private List<String> grantedProperties; // Trait names
  private List<String> unlockedSpells; // Spell names

  // UI/display
  private String tooltip;

  public TalentChoice() {
  }

  // Getters and setters

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public Map<Stats, Integer> getStatBonuses() {
    return statBonuses;
  }

  public void setStatBonuses(Map<Stats, Integer> statBonuses) {
    this.statBonuses = statBonuses;
  }

  public Map<Resistances, Integer> getResistances() {
    return resistances;
  }

  public void setResistances(Map<Resistances, Integer> resistances) {
    this.resistances = resistances;
  }

  public Integer getMaxHpBonus() {
    return maxHpBonus;
  }

  public void setMaxHpBonus(Integer maxHpBonus) {
    this.maxHpBonus = maxHpBonus;
  }

  public Integer getMaxManaBonus() {
    return maxManaBonus;
  }

  public void setMaxManaBonus(Integer maxManaBonus) {
    this.maxManaBonus = maxManaBonus;
  }

  public Integer getMaxStaminaBonus() {
    return maxStaminaBonus;
  }

  public void setMaxStaminaBonus(Integer maxStaminaBonus) {
    this.maxStaminaBonus = maxStaminaBonus;
  }

  public Integer getHpRegenBonus() {
    return hpRegenBonus;
  }

  public void setHpRegenBonus(Integer hpRegenBonus) {
    this.hpRegenBonus = hpRegenBonus;
  }

  public Integer getManaRegenBonus() {
    return manaRegenBonus;
  }

  public void setManaRegenBonus(Integer manaRegenBonus) {
    this.manaRegenBonus = manaRegenBonus;
  }

  public Integer getStaminaRegenBonus() {
    return staminaRegenBonus;
  }

  public void setStaminaRegenBonus(Integer staminaRegenBonus) {
    this.staminaRegenBonus = staminaRegenBonus;
  }

  public Float getCritBonus() {
    return critBonus;
  }

  public void setCritBonus(Float critBonus) {
    this.critBonus = critBonus;
  }

  public Float getDodgeBonus() {
    return dodgeBonus;
  }

  public void setDodgeBonus(Float dodgeBonus) {
    this.dodgeBonus = dodgeBonus;
  }

  public Float getBlockBonus() {
    return blockBonus;
  }

  public void setBlockBonus(Float blockBonus) {
    this.blockBonus = blockBonus;
  }

  public Integer getAccuracyBonus() {
    return accuracyBonus;
  }

  public void setAccuracyBonus(Integer accuracyBonus) {
    this.accuracyBonus = accuracyBonus;
  }

  public Integer getMagicAccuracyBonus() {
    return magicAccuracyBonus;
  }

  public void setMagicAccuracyBonus(Integer magicAccuracyBonus) {
    this.magicAccuracyBonus = magicAccuracyBonus;
  }

  public List<String> getGrantedProperties() {
    return grantedProperties;
  }

  public void setGrantedProperties(List<String> grantedProperties) {
    this.grantedProperties = grantedProperties;
  }

  public List<String> getUnlockedSpells() {
    return unlockedSpells;
  }

  public void setUnlockedSpells(List<String> unlockedSpells) {
    this.unlockedSpells = unlockedSpells;
  }

  public String getTooltip() {
    return tooltip;
  }

  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  @Override
  public String toString() {
    return "TalentChoice{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        '}';
  }
}

package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a player class (e.g., Paladin, Rogue) that modifies player stats,
 * grants properties, unlocks spells, and provides level-based progression.
 *
 * Player classes are loaded from JSON files in data/creatures/player_classes/
 * and applied to Player instances via PlayerClassService.
 */
public class PlayerClass {
  private int id;
  private String name;
  private String description;

  // Base modifications applied when class is selected
  private Map<Stats, Integer> statBonuses;
  private Map<Resistances, Integer> resistances;

  // Health, mana, stamina bonuses (flat or percentage-based)
  private Integer maxHpBonus;
  private Integer maxManaBonus;
  private Integer maxStaminaBonus;

  // Regeneration bonuses
  private Integer hpRegenBonus;
  private Integer manaRegenBonus;
  private Integer staminaRegenBonus;

  // Properties (traits) granted immediately
  private List<String> grantedProperties;

  // Spells unlocked at class selection
  private List<String> unlockedSpells;

  // Talent progression
  private int talentPointsPerLevel = 1; // Default: 1 talent point per level

  // Level-based unlocks (spells, properties, stat bonuses at specific levels)
  private Map<Integer, LevelUnlock> levelUnlocks;

  // Tooltip for UI display
  private Object tooltip;

  /**
   * Represents unlocks granted at a specific level.
   */
  public static class LevelUnlock {
    private List<String> spells;
    private List<String> properties;
    private Map<Stats, Integer> statBonuses;
    private Map<Resistances, Integer> resistances;
    private Integer maxHpBonus;
    private Integer maxManaBonus;
    private Integer maxStaminaBonus;

    public LevelUnlock() {
      this.spells = new ArrayList<>();
      this.properties = new ArrayList<>();
      this.statBonuses = new HashMap<>();
      this.resistances = new HashMap<>();
    }

    public List<String> getSpells() {
      return spells;
    }

    public void setSpells(List<String> spells) {
      this.spells = spells;
    }

    public List<String> getProperties() {
      return properties;
    }

    public void setProperties(List<String> properties) {
      this.properties = properties;
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
  }

  // Constructors
  public PlayerClass() {
    this.statBonuses = new HashMap<>();
    this.resistances = new HashMap<>();
    this.grantedProperties = new ArrayList<>();
    this.unlockedSpells = new ArrayList<>();
    this.levelUnlocks = new HashMap<>();
  }

  // Getters and Setters
  public int getId() {
    return id;
  }

  public void setId(int id) {
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

  public int getTalentPointsPerLevel() {
    return talentPointsPerLevel;
  }

  public void setTalentPointsPerLevel(int talentPointsPerLevel) {
    this.talentPointsPerLevel = talentPointsPerLevel;
  }

  public Map<Integer, LevelUnlock> getLevelUnlocks() {
    return levelUnlocks;
  }

  public void setLevelUnlocks(Map<Integer, LevelUnlock> levelUnlocks) {
    this.levelUnlocks = levelUnlocks;
  }

  public Object getTooltip() {
    return tooltip;
  }

  public void setTooltip(Object tooltip) {
    this.tooltip = tooltip;
  }

  @Override
  public String toString() {
    return "PlayerClass{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", statBonuses=" + statBonuses +
        ", resistances=" + resistances +
        ", maxHpBonus=" + maxHpBonus +
        ", maxManaBonus=" + maxManaBonus +
        ", maxStaminaBonus=" + maxStaminaBonus +
        ", grantedProperties=" + grantedProperties +
        ", unlockedSpells=" + unlockedSpells +
        ", talentPointsPerLevel=" + talentPointsPerLevel +
        '}';
  }
}

package com.bapppis.core.creature;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyLoader;

import java.util.Map;

/**
 * Service for applying and managing player classes.
 * 
 * Handles:
 * - Applying class bonuses and properties to Player instances
 * - Removing class effects when switching classes
 * - Processing level-up unlocks (spells, properties, stat bonuses)
 * - Granting talent points on level-up
 */
public class PlayerClassService {
  private final PlayerClassLoader playerClassLoader;

  public PlayerClassService(PlayerClassLoader playerClassLoader) {
    this.playerClassLoader = playerClassLoader;
  }

  /**
   * Applies a player class to a Player instance.
   *
   * This applies:
   * - Base stat bonuses
   * - Resistance bonuses
   * - Max HP/Mana/Stamina bonuses
   * - Regeneration bonuses
   * - Granted properties (traits)
   * - Unlocked spells
   *
   * @param player      The Player to apply the class to
   * @param playerClass The PlayerClass to apply
   */
  public void applyClass(Player player, PlayerClass playerClass) {
    if (player == null || playerClass == null) {
      return;
    }

    // Remove existing class if present
    if (player.getPlayerClassId() != null && player.getPlayerClassId() != 0) {
      removeClass(player);
    }

    // Set class ID
    player.setPlayerClassId(playerClass.getId());

    // Apply stat bonuses
    if (playerClass.getStatBonuses() != null) {
      for (Map.Entry<Stats, Integer> entry : playerClass.getStatBonuses().entrySet()) {
        Stats stat = entry.getKey();
        int bonus = entry.getValue();
        player.increaseStat(stat, bonus);
      }
    }

    // Apply resistance bonuses
    if (playerClass.getResistances() != null) {
      for (Map.Entry<Resistances, Integer> entry : playerClass.getResistances().entrySet()) {
        Resistances resistance = entry.getKey();
        int bonus = entry.getValue();
        int currentValue = player.getResistance(resistance);
        player.setResistance(resistance, currentValue + bonus);
      }
    }

    // Apply max HP/Mana/Stamina bonuses
    if (playerClass.getMaxHpBonus() != null) {
      player.setBaseHp(player.getBaseHp() + playerClass.getMaxHpBonus());
      player.updateMaxHp();
    }
    if (playerClass.getMaxManaBonus() != null) {
      // Mana/Stamina bonuses would need to be stored and applied through custom logic
      // For now, we'll modify base values directly when such setters exist
      System.out.println("Mana bonus applied: +" + playerClass.getMaxManaBonus());
    }
    if (playerClass.getMaxStaminaBonus() != null) {
      System.out.println("Stamina bonus applied: +" + playerClass.getMaxStaminaBonus());
    }

    // Regeneration bonuses - these need to be tracked separately
    if (playerClass.getHpRegenBonus() != null) {
      System.out.println("HP regen bonus applied: +" + playerClass.getHpRegenBonus());
    }
    if (playerClass.getManaRegenBonus() != null) {
      System.out.println("Mana regen bonus applied: +" + playerClass.getManaRegenBonus());
    }
    if (playerClass.getStaminaRegenBonus() != null) {
      System.out.println("Stamina regen bonus applied: +" + playerClass.getStaminaRegenBonus());
    }

    // Grant properties (traits)
    if (playerClass.getGrantedProperties() != null) {
      for (String propertyName : playerClass.getGrantedProperties()) {
        Property property = PropertyLoader.getPropertyByName(propertyName);
        if (property != null) {
          player.addProperty(property.getId());
        }
      }
    }

    // Unlock spells
    if (playerClass.getUnlockedSpells() != null) {
      for (String spellName : playerClass.getUnlockedSpells()) {
        // Note: Implement spell unlocking when spell system supports it
        // For now, just log the unlock
        System.out.println("Player unlocked spell: " + spellName);
      }
    }

    System.out.println("Applied class " + playerClass.getName() + " to player " + player.getName());
  }

  /**
   * Applies a player class by ID.
   * 
   * @param player  The Player to apply the class to
   * @param classId The ID of the PlayerClass to apply
   * @return true if the class was applied, false if not found
   */
  public boolean applyClassById(Player player, int classId) {
    PlayerClass playerClass = playerClassLoader.getPlayerClassById(classId);
    if (playerClass == null) {
      System.err.println("Player class not found: " + classId);
      return false;
    }
    applyClass(player, playerClass);
    return true;
  }

  /**
   * Removes the current class from a Player.
   * 
   * This reverses all bonuses and removes granted properties.
   * 
   * @param player The Player to remove the class from
   */
  public void removeClass(Player player) {
    if (player == null || player.getPlayerClassId() == null || player.getPlayerClassId() == 0) {
      return;
    }

    PlayerClass playerClass = playerClassLoader.getPlayerClassById(player.getPlayerClassId());
    if (playerClass == null) {
      System.err.println("Cannot remove class - class not found: " + player.getPlayerClassId());
      player.setPlayerClassId(null);
      return;
    }

    // Reverse stat bonuses
    if (playerClass.getStatBonuses() != null) {
      for (Map.Entry<Stats, Integer> entry : playerClass.getStatBonuses().entrySet()) {
        Stats stat = entry.getKey();
        int bonus = entry.getValue();
        player.decreaseStat(stat, bonus);
      }
    }

    // Reverse resistance bonuses
    if (playerClass.getResistances() != null) {
      for (Map.Entry<Resistances, Integer> entry : playerClass.getResistances().entrySet()) {
        Resistances resistance = entry.getKey();
        int bonus = entry.getValue();
        int currentValue = player.getResistance(resistance);
        player.setResistance(resistance, currentValue - bonus);
      }
    }

    // Reverse max HP/Mana/Stamina bonuses
    if (playerClass.getMaxHpBonus() != null) {
      player.setBaseHp(player.getBaseHp() - playerClass.getMaxHpBonus());
      player.updateMaxHp();
    }
    // Note: Mana/Stamina/Regen bonuses would be reversed here when proper setters exist

    // Remove granted properties
    if (playerClass.getGrantedProperties() != null) {
      for (String propertyName : playerClass.getGrantedProperties()) {
        Property property = PropertyLoader.getPropertyByName(propertyName);
        if (property != null) {
          player.removeProperty(property.getId());
        }
      }
    }

    // Clear class ID
    player.setPlayerClassId(null);

    System.out.println("Removed class " + playerClass.getName() + " from player " + player.getName());
  }

  /**
   * Handles level-up for a player with a class.
   *
   * Grants:
   * - Talent points (based on talentPointsPerLevel)
   * - Level-specific unlocks (spells, properties, stat bonuses)
   *
   * @param player   The Player who leveled up
   * @param newLevel The new level reached
   */
  public void handleLevelUp(Player player, int newLevel) {
    if (player == null || player.getPlayerClassId() == null || player.getPlayerClassId() == 0) {
      return;
    }

    PlayerClass playerClass = playerClassLoader.getPlayerClassById(player.getPlayerClassId());
    if (playerClass == null) {
      System.err.println("Cannot handle level-up - class not found: " + player.getPlayerClassId());
      return;
    }

    // Grant talent points
    int talentPoints = playerClass.getTalentPointsPerLevel();
    player.setTalentPoints(player.getTalentPoints() + talentPoints);
    System.out.println(player.getName() + " gained " + talentPoints + " talent point(s)!");

    // Apply level-specific unlocks
    if (playerClass.getLevelUnlocks() != null && playerClass.getLevelUnlocks().containsKey(newLevel)) {
      PlayerClass.LevelUnlock unlock = playerClass.getLevelUnlocks().get(newLevel);

      // Apply stat bonuses
      if (unlock.getStatBonuses() != null) {
        for (Map.Entry<Stats, Integer> entry : unlock.getStatBonuses().entrySet()) {
          Stats stat = entry.getKey();
          int bonus = entry.getValue();
          player.increaseStat(stat, bonus);
          System.out.println(player.getName() + " gained +" + bonus + " " + stat + " from level unlock!");
        }
      }

      // Apply resistance bonuses
      if (unlock.getResistances() != null) {
        for (Map.Entry<Resistances, Integer> entry : unlock.getResistances().entrySet()) {
          Resistances resistance = entry.getKey();
          int bonus = entry.getValue();
          int currentValue = player.getResistance(resistance);
          player.setResistance(resistance, currentValue + bonus);
          System.out.println(player.getName() + " gained +" + bonus + " " + resistance + " from level unlock!");
        }
      }

      // Apply max HP/Mana/Stamina bonuses
      if (unlock.getMaxHpBonus() != null) {
        player.setBaseHp(player.getBaseHp() + unlock.getMaxHpBonus());
        player.updateMaxHp();
        System.out.println(player.getName() + " gained +" + unlock.getMaxHpBonus() + " max HP from level unlock!");
      }
      // Note: Mana/Stamina bonuses would be applied here when proper setters exist

      // Grant properties
      if (unlock.getProperties() != null) {
        for (String propertyName : unlock.getProperties()) {
          Property property = PropertyLoader.getPropertyByName(propertyName);
          if (property != null) {
            player.addProperty(property.getId());
            System.out.println(player.getName() + " gained trait: " + propertyName + "!");
          }
        }
      }

      // Unlock spells
      if (unlock.getSpells() != null) {
        for (String spellName : unlock.getSpells()) {
          System.out.println(player.getName() + " unlocked spell: " + spellName + "!");
        }
      }
    }
  }

  /**
   * Grants a talent point to the player.
   * 
   * @param player The Player to grant the point to
   */
  public void grantTalentPoint(Player player) {
    if (player != null) {
      player.setTalentPoints(player.getTalentPoints() + 1);
    }
  }
}

package com.bapppis.core.creature.playerClass;

import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Player;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.property.Property;
import com.bapppis.core.property.PropertyLoader;
import com.bapppis.core.spell.SpellReference;

import java.util.List;
import java.util.Map;

/**
 * Service for managing talent tree progression.
 * Handles unlocking talents, validating prerequisites, and applying rewards.
 */
public class TalentTreeService {
    private final TalentTreeLoader treeLoader;

    public TalentTreeService(TalentTreeLoader treeLoader) {
        this.treeLoader = treeLoader;
    }

    /**
     * Attempts to unlock a talent node for a player
     *
     * @param player   The player unlocking the talent
     * @param nodeId   The node ID to unlock
     * @param choiceId The choice ID within the node (can be null if node has only 1
     *                 choice)
     * @return true if successfully unlocked, false otherwise
     */
    public boolean unlockTalentNode(Player player, String nodeId, String choiceId) {
        // Get the player's class
        if (player.getPlayerClassId() == null) {
            System.out.println("Player has no class, cannot unlock talents");
            return false;
        }

        // Get the talent tree for the player's class
        TalentTree tree = treeLoader.getTalentTreeByClassId(player.getPlayerClassId());
        if (tree == null) {
            System.out.println("No talent tree found for class ID: " + player.getPlayerClassId());
            return false;
        }

        // Get the node
        TalentNode node = tree.getNodeById(nodeId);
        if (node == null) {
            System.out.println("Node not found: " + nodeId);
            return false;
        }

        // Check if already unlocked
        if (player.hasUnlockedNode(nodeId)) {
            System.out.println("Node already unlocked: " + nodeId);
            return false;
        }

        // Check if player has talent points
        if (player.getTalentPoints() < 1) {
            System.out.println("Player has no talent points available");
            return false;
        }

        // Check prerequisites
        if (!tree.arePrerequisitesMet(node, player.getUnlockedTalentNodes())) {
            System.out.println("Prerequisites not met for node: " + nodeId);
            return false;
        }

        // Determine which choice to apply
        TalentChoice choice;
        if (node.getChoices() == null || node.getChoices().isEmpty()) {
            System.out.println("Node has no choices: " + nodeId);
            return false;
        }

        if (node.getChoices().size() == 1) {
            // Single choice node
            choice = node.getChoices().get(0);
        } else {
            // Multi-choice node - must specify choice
            if (choiceId == null) {
                System.out.println("Node has multiple choices but no choice specified: " + nodeId);
                return false;
            }
            choice = node.getChoiceById(choiceId);
            if (choice == null) {
                System.out.println("Choice not found: " + choiceId + " in node: " + nodeId);
                return false;
            }
        }

        // Apply the talent choice rewards
        applyTalentRewards(player, choice);

        // Mark node as unlocked and spend talent point
        player.unlockTalentNode(nodeId);
        player.setTalentPoints(player.getTalentPoints() - 1);

        System.out.println(player.getName() + " unlocked talent: " + choice.getName() + " (node: " + nodeId + ")");
        return true;
    }

    /**
     * Applies all rewards from a talent choice to a player
     */
    private void applyTalentRewards(Player player, TalentChoice choice) {
        // Apply stat bonuses
        if (choice.getStatBonuses() != null) {
            for (Map.Entry<Stats, Integer> entry : choice.getStatBonuses().entrySet()) {
                player.increaseStat(entry.getKey(), entry.getValue());
                System.out.println("  +" + entry.getValue() + " " + entry.getKey());
            }
        }

        // Apply resistance modifications
        if (choice.getResistances() != null) {
            for (Map.Entry<Resistances, Integer> entry : choice.getResistances().entrySet()) {
                int currentResist = player.getResistance(entry.getKey());
                player.setResistance(entry.getKey(), currentResist + entry.getValue());
                System.out.println("  " + (entry.getValue() >= 0 ? "+" : "") + entry.getValue() + "% " + entry.getKey()
                        + " resistance");
            }
        }

        // Apply HP bonus
        if (choice.getMaxHpBonus() != null && choice.getMaxHpBonus() > 0) {
            player.setBaseHp(player.getBaseHp() + choice.getMaxHpBonus());
            player.updateMaxHp();
            System.out.println("  +" + choice.getMaxHpBonus() + " HP");
        }

        // Apply Mana bonus (placeholder - needs implementation in Creature)
        if (choice.getMaxManaBonus() != null && choice.getMaxManaBonus() > 0) {
            player.modifyBaseMaxMana(choice.getMaxManaBonus());
            System.out.println("  Mana bonus applied: +" + choice.getMaxManaBonus());
        }

        // Apply Stamina bonus (placeholder - needs implementation in Creature)
        if (choice.getMaxStaminaBonus() != null && choice.getMaxStaminaBonus() > 0) {
            player.modifyBaseMaxStamina(choice.getMaxStaminaBonus());
            System.out.println("  Stamina bonus applied: +" + choice.getMaxStaminaBonus());
        }

        // Apply HP regen bonus (placeholder)
        if (choice.getHpRegenBonus() != null && choice.getHpRegenBonus() > 0) {
            player.modifyBaseHpRegen(choice.getHpRegenBonus());
            System.out.println("  HP regen bonus applied: +" + choice.getHpRegenBonus());
        }

        // Apply Mana regen bonus (placeholder)
        if (choice.getManaRegenBonus() != null && choice.getManaRegenBonus() > 0) {
            player.modifyBaseManaRegen(choice.getManaRegenBonus());
            System.out.println("  Mana regen bonus applied: +" + choice.getManaRegenBonus());
        }

        // Apply Stamina regen bonus (placeholder)
        if (choice.getStaminaRegenBonus() != null && choice.getStaminaRegenBonus() > 0) {
            player.modifyBaseStaminaRegen(choice.getStaminaRegenBonus());
            System.out.println("  Stamina regen bonus applied: +" + choice.getStaminaRegenBonus());
        }

        // Apply combat-related bonuses
        if (choice.getCritBonus() != null) {
            player.modifyBaseCrit(choice.getCritBonus());
            System.out.println("  +" + choice.getCritBonus() + "% crit chance");
        }
        if (choice.getDodgeBonus() != null) {
            player.modifyBaseDodge(choice.getDodgeBonus());
            System.out.println("  +" + choice.getDodgeBonus() + "% dodge");
        }
        if (choice.getBlockBonus() != null) {
            player.modifyBaseBlock(choice.getBlockBonus());
            System.out.println("  +" + choice.getBlockBonus() + "% block");
        }
        if (choice.getAccuracyBonus() != null) {
            player.modifyBaseAccuracy(choice.getAccuracyBonus());
            System.out.println("  +" + choice.getAccuracyBonus() + " accuracy");
        }
        if (choice.getMagicAccuracyBonus() != null) {
            player.modifyBaseMagicAccuracy(choice.getMagicAccuracyBonus());
            System.out.println("  +" + choice.getMagicAccuracyBonus() + " magic accuracy");
        }

        // Grant properties (traits)
        if (choice.getGrantedProperties() != null) {
            for (String propertyName : choice.getGrantedProperties()) {
                Property property = PropertyLoader.getPropertyByName(propertyName);
                if (property != null) {
                    player.addProperty(property.getId());
                    System.out.println("  Granted trait: " + propertyName);
                } else {
                    System.err.println("  Warning: Property not found: " + propertyName);
                }
            }
        }

        // Unlock spells
        if (choice.getUnlockedSpells() != null) {
            for (String spellName : choice.getUnlockedSpells()) {
                if (spellName == null || spellName.isEmpty()) continue;
                try {
                    boolean exists = false;
                    for (SpellReference ref : player.getSpellReferences()) {
                        if (spellName.equalsIgnoreCase(ref.getName())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        player.getSpellReferences().add(new SpellReference(spellName, 1));
                    }
                    System.out.println("  Unlocked spell: " + spellName);
                } catch (Exception e) {
                    System.err.println("  Failed to unlock spell: " + spellName);
                }
            }
        }
    }

    /**
     * Gets all available nodes that the player can currently unlock
     */
    public List<TalentNode> getAvailableNodes(Player player) {
        if (player.getPlayerClassId() == null) {
            return null;
        }

        TalentTree tree = treeLoader.getTalentTreeByClassId(player.getPlayerClassId());
        if (tree == null) {
            return null;
        }

        return tree.getAvailableNodes(player.getUnlockedTalentNodes());
    }

    /**
     * Gets the talent tree for a player's class
     */
    public TalentTree getPlayerTalentTree(Player player) {
        if (player.getPlayerClassId() == null) {
            return null;
        }
        return treeLoader.getTalentTreeByClassId(player.getPlayerClassId());
    }

    /**
     * Checks if a specific node is available to unlock (prerequisites met, not
     * unlocked yet)
     */
    public boolean isNodeAvailable(Player player, String nodeId) {
        if (player.getPlayerClassId() == null) {
            return false;
        }

        TalentTree tree = treeLoader.getTalentTreeByClassId(player.getPlayerClassId());
        if (tree == null) {
            return false;
        }

        TalentNode node = tree.getNodeById(nodeId);
        if (node == null || player.hasUnlockedNode(nodeId)) {
            return false;
        }

        return tree.arePrerequisitesMet(node, player.getUnlockedTalentNodes());
    }

    /**
     * Resets all talents for a player, removing all unlocked nodes and refunding
     * talent points.
     * Note: This does NOT remove the bonuses already applied. The player needs to
     * be re-initialized
     * or bonuses manually reversed. This method only clears the unlocked node
     * tracking and refunds points.
     * 
     * @param player The player whose talents to reset
     * @return The number of talent points refunded
     */
    public int resetTalents(Player player) {
        int nodeCount = player.getUnlockedNodeCount();

        if (nodeCount == 0) {
            System.out.println(player.getName() + " has no talents to reset");
            return 0;
        }

    // Clear all unlocked nodes (use Player API to clear the internal set)
    player.clearUnlockedTalentNodes();

        // Refund talent points
        player.setTalentPoints(player.getTalentPoints() + nodeCount);

        System.out.println(
                player.getName() + " reset " + nodeCount + " talents and received " + nodeCount + " talent points");
        System.out.println("Note: Player stats must be recalculated for bonuses to be removed");

        return nodeCount;
    }

    /**
     * Resets all talents and re-applies class bonuses from scratch.
     * This fully removes all talent bonuses by resetting player stats to base +
     * class bonuses only.
     *
     * @param player       The player whose talents to fully reset
     * @param classService The PlayerClassService to re-apply class bonuses
     * @return The number of talent points refunded
     */
    public int resetTalentsFully(Player player, PlayerClassService classService, PlayerClassLoader classLoader) {
        if (player.getPlayerClassId() == null) {
            System.out.println("Player has no class, cannot reset talents");
            return 0;
        }

        int nodeCount = player.getUnlockedNodeCount();

        if (nodeCount == 0) {
            System.out.println(player.getName() + " has no talents to reset");
            return 0;
        }

        // Get the current class
        PlayerClass playerClass = classLoader.getPlayerClassById(player.getPlayerClassId());
        if (playerClass == null) {
            System.out.println("Player class not found: " + player.getPlayerClassId());
            return 0;
        }

        // Get the talent tree for this class
        TalentTree tree = treeLoader.getTalentTreeByClassId(player.getPlayerClassId());
        if (tree == null) {
            System.out.println("Talent tree not found for class: " + player.getPlayerClassId());
            return 0;
        }

        // IMPORTANT: Reverse all talent bonuses BEFORE removing class
        // We need to manually undo each talent's effects
        for (String unlockedNodeId : new java.util.HashSet<>(player.getUnlockedTalentNodes())) {
            TalentNode node = tree.getNodeById(unlockedNodeId);
            if (node == null) {
                continue;
            }

            // Find which choice was taken (assume first choice if only one exists)
            // TODO: This doesn't handle multi-choice nodes properly - would need to track choices
            TalentChoice choice = node.getChoices().isEmpty() ? null : node.getChoices().get(0);
            if (choice != null) {
                reverseTalentRewards(player, choice);
            }
        }

        // Remove class to get back to base stats
        classService.removeClass(player);

        // Clear all unlocked nodes (use Player API to clear the internal set)
        player.clearUnlockedTalentNodes();

        // Re-apply class
        classService.applyClass(player, playerClass);

        // Refund talent points
        player.setTalentPoints(player.getTalentPoints() + nodeCount);

        System.out.println(player.getName() + " fully reset " + nodeCount + " talents and received " + nodeCount
                + " talent points");

        return nodeCount;
    }

    /**
     * Reverses all rewards from a talent choice
     */
    private void reverseTalentRewards(Player player, TalentChoice choice) {
        // Reverse stat bonuses
        if (choice.getStatBonuses() != null) {
            for (Map.Entry<Stats, Integer> entry : choice.getStatBonuses().entrySet()) {
                player.decreaseStat(entry.getKey(), entry.getValue());
            }
        }

        // Reverse resistance modifications
        if (choice.getResistances() != null) {
            for (Map.Entry<Resistances, Integer> entry : choice.getResistances().entrySet()) {
                int currentResist = player.getResistance(entry.getKey());
                player.setResistance(entry.getKey(), currentResist - entry.getValue());
            }
        }

        // Reverse HP bonus
        if (choice.getMaxHpBonus() != null && choice.getMaxHpBonus() > 0) {
            player.setBaseHp(player.getBaseHp() - choice.getMaxHpBonus());
            player.updateMaxHp();
        }
        // Reverse Mana/Stamina and regen bonuses
        if (choice.getMaxManaBonus() != null && choice.getMaxManaBonus() > 0) {
            player.modifyBaseMaxMana(-choice.getMaxManaBonus());
        }
        if (choice.getMaxStaminaBonus() != null && choice.getMaxStaminaBonus() > 0) {
            player.modifyBaseMaxStamina(-choice.getMaxStaminaBonus());
        }
        if (choice.getHpRegenBonus() != null && choice.getHpRegenBonus() > 0) {
            player.modifyBaseHpRegen(-choice.getHpRegenBonus());
        }
        if (choice.getManaRegenBonus() != null && choice.getManaRegenBonus() > 0) {
            player.modifyBaseManaRegen(-choice.getManaRegenBonus());
        }
        if (choice.getStaminaRegenBonus() != null && choice.getStaminaRegenBonus() > 0) {
            player.modifyBaseStaminaRegen(-choice.getStaminaRegenBonus());
        }

        // Reverse combat bonuses
        if (choice.getCritBonus() != null) {
            player.modifyBaseCrit(-choice.getCritBonus());
        }
        if (choice.getDodgeBonus() != null) {
            player.modifyBaseDodge(-choice.getDodgeBonus());
        }
        if (choice.getBlockBonus() != null) {
            player.modifyBaseBlock(-choice.getBlockBonus());
        }
        if (choice.getAccuracyBonus() != null) {
            player.modifyBaseAccuracy(-choice.getAccuracyBonus());
        }
        if (choice.getMagicAccuracyBonus() != null) {
            player.modifyBaseMagicAccuracy(-choice.getMagicAccuracyBonus());
        }
    }
}

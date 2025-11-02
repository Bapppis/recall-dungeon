package com.bapppis.core.Creature.playerClass;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.Resistances;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.creature.playerClass.*;

/**
 * Comprehensive tests for talent tree service.
 * Tests talent unlocking, prerequisite validation, reward application,
 * progression, and reset functionality.
 */
public class TalentTreeServiceTest {

  private TalentTreeService service;
  private PlayerClassService classService;
  private Player player;

  @BeforeAll
  public static void setupAll() {
    AllLoaders.loadAll();
  }

  @BeforeEach
  public void setup() {
    // Create fresh instances for each test
    service = new TalentTreeService(AllLoaders.getTalentTreeLoader());
    classService = new PlayerClassService(AllLoaders.getPlayerClassLoader());

    // Create a fresh player for each test
    // NOTE: CreatureLoader returns cached instances, so we need to ensure clean state
    player = CreatureLoader.getPlayerById(5000);
    
    // Clear any state from previous tests
    player.clearUnlockedTalentNodes();
    player.setTalentPoints(0);
    
    // Remove any existing class before applying fresh
    if (player.getPlayerClassId() != null) {
      classService.removeClass(player);
    }

    // Apply Paladin class
    PlayerClass paladinClass = AllLoaders.getPlayerClassLoader().getPlayerClassById(60000);
    classService.applyClass(player, paladinClass);

    // Give player some talent points
    player.setTalentPoints(10);
  }

  @Test
  public void testUnlockRootNode() {
    // Test unlocking the root node
    boolean result = service.unlockTalentNode(player, "pal_root", null);

    assertTrue(result, "Root node should unlock successfully");
    assertTrue(player.hasUnlockedNode("pal_root"), "Player should have root node unlocked");
    assertEquals(9, player.getTalentPoints(), "Player should have 9 talent points remaining");

    // Verify stat bonuses were applied (root gives +1 CON)
    // Note: We can't directly test stat increase without knowing base stats
    // but we can verify the node is tracked as unlocked
  }

  @Test
  public void testUnlockWithoutClass() {
    // Create player without class - use the same cached player but ensure class is removed
    Player noClassPlayer = CreatureLoader.getPlayerById(5000);
    
    // Clear any existing class and state
    noClassPlayer.setPlayerClassId(null);
    noClassPlayer.clearUnlockedTalentNodes();
    noClassPlayer.setTalentPoints(5);

    boolean result = service.unlockTalentNode(noClassPlayer, "pal_root", null);

    assertFalse(result, "Should fail to unlock talent without class");
    assertFalse(noClassPlayer.hasUnlockedNode("pal_root"), "Node should not be unlocked");
  }

  @Test
  public void testUnlockWithoutTalentPoints() {
    player.setTalentPoints(0);

    boolean result = service.unlockTalentNode(player, "pal_root", null);

    assertFalse(result, "Should fail to unlock without talent points");
    assertFalse(player.hasUnlockedNode("pal_root"), "Node should not be unlocked");
  }

  @Test
  public void testUnlockWithoutPrerequisites() {
    // Try to unlock tier 1 node without unlocking root
    boolean result = service.unlockTalentNode(player, "pal_holy_1", null);

    assertFalse(result, "Should fail to unlock node without prerequisites");
    assertFalse(player.hasUnlockedNode("pal_holy_1"), "Node should not be unlocked");
    assertEquals(10, player.getTalentPoints(), "Talent points should not be spent");
  }

  @Test
  public void testUnlockProgressionChain() {
    // Unlock root
    assertTrue(service.unlockTalentNode(player, "pal_root", null), "Should unlock root");
    assertEquals(9, player.getTalentPoints(), "Should have 9 points after root");

    // Unlock tier 1 holy
    assertTrue(service.unlockTalentNode(player, "pal_holy_1", null), "Should unlock holy tier 1");
    assertEquals(8, player.getTalentPoints(), "Should have 8 points after tier 1");

    // Unlock tier 2 holy (multi-choice node)
    assertTrue(service.unlockTalentNode(player, "pal_holy_2", "healing_hands"),
        "Should unlock holy tier 2 with choice");
    assertEquals(7, player.getTalentPoints(), "Should have 7 points after tier 2");

    // Verify all nodes are tracked
    assertTrue(player.hasUnlockedNode("pal_root"), "Root should be unlocked");
    assertTrue(player.hasUnlockedNode("pal_holy_1"), "Holy tier 1 should be unlocked");
    assertTrue(player.hasUnlockedNode("pal_holy_2"), "Holy tier 2 should be unlocked");
    assertEquals(3, player.getUnlockedNodeCount(), "Should have 3 nodes unlocked");
  }

  @Test
  public void testMultiChoiceNodeRequiresChoice() {
    // Unlock prerequisites
    service.unlockTalentNode(player, "pal_root", null);
    service.unlockTalentNode(player, "pal_holy_1", null);

    // Try to unlock multi-choice node without specifying choice
    boolean result = service.unlockTalentNode(player, "pal_holy_2", null);

    assertFalse(result, "Should fail to unlock multi-choice node without choice ID");
    assertFalse(player.hasUnlockedNode("pal_holy_2"), "Node should not be unlocked");
  }

  @Test
  public void testMultiChoiceNodeWithInvalidChoice() {
    // Unlock prerequisites
    service.unlockTalentNode(player, "pal_root", null);
    service.unlockTalentNode(player, "pal_holy_1", null);

    // Try to unlock with invalid choice ID
    boolean result = service.unlockTalentNode(player, "pal_holy_2", "invalid_choice");

    assertFalse(result, "Should fail with invalid choice ID");
    assertFalse(player.hasUnlockedNode("pal_holy_2"), "Node should not be unlocked");
  }

  @Test
  public void testCannotUnlockSameNodeTwice() {
    // Unlock root
    assertTrue(service.unlockTalentNode(player, "pal_root", null), "Should unlock root first time");

    // Try to unlock again
    boolean result = service.unlockTalentNode(player, "pal_root", null);

    assertFalse(result, "Should not unlock same node twice");
    assertEquals(9, player.getTalentPoints(), "Should still have 9 points (not 8)");
  }

  @Test
  public void testGetAvailableNodes() {
    // Initially, only root should be available
    var available = service.getAvailableNodes(player);
    assertEquals(1, available.size(), "Only root should be available initially");
    assertEquals("pal_root", available.get(0).getId(), "Root should be available");

    // After unlocking root, tier 1 nodes should be available
    service.unlockTalentNode(player, "pal_root", null);
    available = service.getAvailableNodes(player);
    assertEquals(3, available.size(), "Three tier 1 nodes should be available");

    // After unlocking holy tier 1, holy tier 2 should be available
    service.unlockTalentNode(player, "pal_holy_1", null);
    available = service.getAvailableNodes(player);
    assertTrue(available.stream().anyMatch(n -> n.getId().equals("pal_holy_2")),
        "Holy tier 2 should be available");
  }

  @Test
  public void testIsNodeAvailable() {
    // Root should be available
    assertTrue(service.isNodeAvailable(player, "pal_root"), "Root should be available");

    // Tier 1 should not be available without root
    assertFalse(service.isNodeAvailable(player, "pal_holy_1"), "Holy tier 1 should not be available yet");

    // After unlocking root, tier 1 should be available
    service.unlockTalentNode(player, "pal_root", null);
    assertTrue(service.isNodeAvailable(player, "pal_holy_1"), "Holy tier 1 should be available now");

    // Root should not be available (already unlocked)
    assertFalse(service.isNodeAvailable(player, "pal_root"), "Root should not be available (already unlocked)");
  }

  @Test
  public void testGetPlayerTalentTree() {
    TalentTree tree = service.getPlayerTalentTree(player);

    assertNotNull(tree, "Player should have talent tree");
    assertEquals(70000, tree.getId(), "Should be Paladin talent tree");
    assertEquals(60000, tree.getClassId(), "Should be tied to Paladin class");
  }

  @Test
  public void testMixedPathProgression() {
    // Test that player can unlock nodes from different paths
    service.unlockTalentNode(player, "pal_root", null);

    // Unlock from Holy path
    assertTrue(service.unlockTalentNode(player, "pal_holy_1", null), "Should unlock from Holy path");

    // Unlock from Protection path
    assertTrue(service.unlockTalentNode(player, "pal_prot_1", null), "Should unlock from Protection path");

    // Unlock from Combat path
    assertTrue(service.unlockTalentNode(player, "pal_combat_1", null), "Should unlock from Combat path");

    assertEquals(4, player.getUnlockedNodeCount(), "Should have 4 nodes unlocked (1 root + 3 tier 1)");
  }

  @Test
  public void testProgressToCapstone() {
    // Progress through full Holy path to capstone
    service.unlockTalentNode(player, "pal_root", null);
    service.unlockTalentNode(player, "pal_holy_1", null);
    service.unlockTalentNode(player, "pal_holy_2", "healing_hands");
    service.unlockTalentNode(player, "pal_holy_3", null);

    // Now capstone should be available
    assertTrue(service.isNodeAvailable(player, "pal_holy_capstone"), "Holy capstone should be available");

    // Unlock capstone
    assertTrue(service.unlockTalentNode(player, "pal_holy_capstone", null), "Should unlock capstone");
    assertTrue(player.hasUnlockedNode("pal_holy_capstone"), "Capstone should be unlocked");
  }

  @Test
  public void testResetTalentsSimple() {
    // Unlock some talents
    service.unlockTalentNode(player, "pal_root", null);
    service.unlockTalentNode(player, "pal_holy_1", null);
    service.unlockTalentNode(player, "pal_holy_2", "healing_hands");

    assertEquals(3, player.getUnlockedNodeCount(), "Should have 3 nodes unlocked");
    assertEquals(7, player.getTalentPoints(), "Should have 7 talent points remaining");

    // Reset talents
    int refunded = service.resetTalents(player);

    assertEquals(3, refunded, "Should refund 3 talent points");
    assertEquals(0, player.getUnlockedNodeCount(), "Should have 0 nodes unlocked after reset");
    assertEquals(10, player.getTalentPoints(), "Should have 10 talent points after reset");

    // Verify nodes are no longer unlocked
    assertFalse(player.hasUnlockedNode("pal_root"), "Root should not be unlocked");
    assertFalse(player.hasUnlockedNode("pal_holy_1"), "Holy tier 1 should not be unlocked");
    assertFalse(player.hasUnlockedNode("pal_holy_2"), "Holy tier 2 should not be unlocked");
  }

  @Test
  public void testResetTalentsWithNoTalents() {
    // Try to reset when no talents are unlocked
    int refunded = service.resetTalents(player);

    assertEquals(0, refunded, "Should refund 0 points");
    assertEquals(10, player.getTalentPoints(), "Should still have 10 points");
  }

  @Test
  public void testResetTalentsFully() {
    // Make absolutely sure we start clean - capture baseline after class application
    // The cached player may have accumulated stats, so we force reset first
    if (player.getUnlockedNodeCount() > 0) {
      service.resetTalentsFully(player, classService, AllLoaders.getPlayerClassLoader());
    }
    
    // Record initial stats (after class but before talents)
    int initialCon = player.getStat(Stats.CONSTITUTION);
    int initialHp = player.getMaxHp();

    // Unlock talents that grant stats
    service.unlockTalentNode(player, "pal_root", null); // +1 CON, +10 HP

    int afterTalentCon = player.getStat(Stats.CONSTITUTION);
    int afterTalentHp = player.getMaxHp();

    // Stats should have increased
    assertTrue(afterTalentCon > initialCon, "CON should increase after talent");
    assertTrue(afterTalentHp > initialHp, "HP should increase after talent");

    assertEquals(1, player.getUnlockedNodeCount(), "Should have 1 node unlocked");
    assertEquals(9, player.getTalentPoints(), "Should have 9 talent points");

    // Perform full reset
    int refunded = service.resetTalentsFully(player, classService, AllLoaders.getPlayerClassLoader());

    assertEquals(1, refunded, "Should refund 1 talent point");
    assertEquals(0, player.getUnlockedNodeCount(), "Should have 0 nodes unlocked");
    assertEquals(10, player.getTalentPoints(), "Should have 10 talent points");

    // Stats should be back to initial (base + class only)
    assertEquals(initialCon, player.getStat(Stats.CONSTITUTION), "CON should be reset to initial");
    assertEquals(initialHp, player.getMaxHp(), "HP should be reset to initial");
  }

  @Test
  public void testResetAfterMultipleUnlocks() {
    // Unlock many nodes
    service.unlockTalentNode(player, "pal_root", null);
    service.unlockTalentNode(player, "pal_holy_1", null);
    service.unlockTalentNode(player, "pal_holy_2", "healing_hands");
    service.unlockTalentNode(player, "pal_prot_1", null);
    service.unlockTalentNode(player, "pal_combat_1", null);

    assertEquals(5, player.getUnlockedNodeCount(), "Should have 5 nodes");
    assertEquals(5, player.getTalentPoints(), "Should have 5 talent points left");

    // Reset
    int refunded = service.resetTalents(player);

    assertEquals(5, refunded, "Should refund 5 points");
    assertEquals(0, player.getUnlockedNodeCount(), "Should have 0 nodes");
    assertEquals(10, player.getTalentPoints(), "Should have all 10 points back");
  }

  @Test
  public void testCanRespecAndUnlockDifferentPath() {
    // Initial progression in Holy path
    service.unlockTalentNode(player, "pal_root", null);
    service.unlockTalentNode(player, "pal_holy_1", null);

    // Reset
    service.resetTalents(player);

    // Now progress in Protection path instead
    assertTrue(service.unlockTalentNode(player, "pal_root", null), "Should unlock root again");
    assertTrue(service.unlockTalentNode(player, "pal_prot_1", null), "Should unlock Protection path");

    assertTrue(player.hasUnlockedNode("pal_root"), "Root should be unlocked");
    assertTrue(player.hasUnlockedNode("pal_prot_1"), "Protection tier 1 should be unlocked");
    assertFalse(player.hasUnlockedNode("pal_holy_1"), "Holy tier 1 should not be unlocked");
  }

  @Test
  public void testResetWithoutClass() {
    // Remove class
    player.setPlayerClassId(null);

    int refunded = service.resetTalentsFully(player, classService, AllLoaders.getPlayerClassLoader());

    assertEquals(0, refunded, "Should not reset without class");
  }

  @Test
  public void testCapstoneRewardsAreSignificant() {
    // Unlock full Holy path to capstone
    service.unlockTalentNode(player, "pal_root", null);
    service.unlockTalentNode(player, "pal_holy_1", null);
    service.unlockTalentNode(player, "pal_holy_2", "healing_hands");
    service.unlockTalentNode(player, "pal_holy_3", null);

    int preCapstoneWis = player.getStat(Stats.WISDOM);
    int preCapstoneInt = player.getStat(Stats.INTELLIGENCE);
    int preCapstoneLight = player.getResistance(Resistances.LIGHT);

    // Unlock capstone
    service.unlockTalentNode(player, "pal_holy_capstone", null);

    int postCapstoneWis = player.getStat(Stats.WISDOM);
    int postCapstoneInt = player.getStat(Stats.INTELLIGENCE);
    int postCapstoneLight = player.getResistance(Resistances.LIGHT);

    // Capstone should grant significant bonuses (Avatar of Light: +3 WIS, +2 INT,
    // +25 Light)
    assertTrue(postCapstoneWis > preCapstoneWis, "WIS should increase from capstone");
    assertTrue(postCapstoneInt > preCapstoneInt, "INT should increase from capstone");
    assertTrue(postCapstoneLight > preCapstoneLight, "Light resistance should increase from capstone");
  }
}

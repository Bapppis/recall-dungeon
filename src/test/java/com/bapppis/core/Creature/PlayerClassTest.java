package com.bapppis.core.Creature;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.Resistances;
import com.bapppis.core.creature.Player;
import com.bapppis.core.creature.PlayerClass;
import com.bapppis.core.creature.PlayerClassLoader;
import com.bapppis.core.creature.PlayerClassService;
import com.bapppis.core.creature.creatureEnums.Stats;

/**
 * Tests for the PlayerClass system including loading, applying, removing,
 * and level-up functionality.
 */
public class PlayerClassTest {

  @BeforeAll
  public static void setup() {
    AllLoaders.loadAll();
  }

  @Test
  public void testPlayerClassLoaderExists() {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    assertNotNull(loader, "PlayerClassLoader should be initialized");
  }

  @Test
  public void testPaladinClassLoads() {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    PlayerClass paladin = loader.getPlayerClassById(60000);

    assertNotNull(paladin, "Paladin class should load");
    assertEquals(60000, paladin.getId());
    assertEquals("Paladin", paladin.getName());
    assertNotNull(paladin.getDescription());
  }

  @Test
  public void testPaladinStatBonuses() {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    PlayerClass paladin = loader.getPlayerClassById(60000);

    assertNotNull(paladin.getStatBonuses());
    assertEquals(2, paladin.getStatBonuses().get(Stats.STRENGTH));
    assertEquals(1, paladin.getStatBonuses().get(Stats.CONSTITUTION));
    assertEquals(1, paladin.getStatBonuses().get(Stats.WISDOM));
  }

  @Test
  public void testPaladinResistances() {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    PlayerClass paladin = loader.getPlayerClassById(60000);

    assertNotNull(paladin.getResistances());
    // Based on the JSON: "LIGHT": 10, "DARKNESS": -5
    assertEquals(10, paladin.getResistances().get(Resistances.LIGHT));
    assertEquals(-5, paladin.getResistances().get(Resistances.DARKNESS));
  }

  @Test
  public void testApplyPaladinClassToPlayer() {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    PlayerClass paladin = loader.getPlayerClassById(60000);
    PlayerClassService service = new PlayerClassService(loader);

    Player player = new Player();
    player.setName("Test Paladin");
    player.setLevel(1);

    // Record base stats
    int baseStr = player.getSTR();
    int baseCon = player.getCON();
    int baseWis = player.getWIS();
    int baseHp = player.getBaseHp();

    // Apply class
    service.applyClass(player, paladin);

    // Verify class ID is set
    assertEquals(60000, player.getPlayerClassId());

    // Verify stat bonuses applied
    assertEquals(baseStr + 2, player.getSTR(), "STR should increase by 2");
    assertEquals(baseCon + 1, player.getCON(), "CON should increase by 1");
    assertEquals(baseWis + 1, player.getWIS(), "WIS should increase by 1");

    // Verify HP bonus applied
    assertEquals(baseHp + 20, player.getBaseHp(), "Base HP should increase by 20");
  }

  @Test
  public void testRemoveClass() {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    PlayerClass paladin = loader.getPlayerClassById(60000);
    PlayerClassService service = new PlayerClassService(loader);

    Player player = new Player();
    player.setName("Test Paladin");
    player.setLevel(1);

    // Record base stats
    int baseStr = player.getSTR();
    int baseCon = player.getCON();
    int baseHp = player.getBaseHp();

    // Apply then remove class
    service.applyClass(player, paladin);
    service.removeClass(player);

    // Verify class ID is cleared
    assertNull(player.getPlayerClassId());

    // Verify stats returned to base
    assertEquals(baseStr, player.getSTR(), "STR should return to base");
    assertEquals(baseCon, player.getCON(), "CON should return to base");
    assertEquals(baseHp, player.getBaseHp(), "HP should return to base");
  }

  @Test
  public void testLevelUpGrantsTalentPoints() {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    PlayerClass paladin = loader.getPlayerClassById(60000);
    PlayerClassService service = new PlayerClassService(loader);

    Player player = new Player();
    player.setName("Test Paladin");
    player.setLevel(1);

    service.applyClass(player, paladin);

    int initialTalentPoints = player.getTalentPoints();

    // Simulate level up
    service.handleLevelUp(player, 2);

    assertEquals(initialTalentPoints + 1, player.getTalentPoints(),
        "Should gain 1 talent point per level");
  }

  @Test
  public void testTalentPointManagement() {
    Player player = new Player();
    player.setName("Test Player");

    assertEquals(0, player.getTalentPoints());

    player.addTalentPoint();
    assertEquals(1, player.getTalentPoints());

    player.spendTalentPoint("TestTalent1");
    assertEquals(0, player.getTalentPoints());
    assertTrue(player.hasTalent("TestTalent1"));

    // Can't spend the same talent twice
    player.addTalentPoint();
    player.spendTalentPoint("TestTalent1");
    assertEquals(1, player.getTalentPoints(), "Should not spend point on duplicate talent");
  }
}

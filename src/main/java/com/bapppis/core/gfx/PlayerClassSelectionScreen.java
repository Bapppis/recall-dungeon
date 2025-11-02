package com.bapppis.core.gfx;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import com.bapppis.core.AllLoaders;
import com.bapppis.core.creature.Player;
import com.bapppis.core.Resistances;
import com.bapppis.core.creature.creatureEnums.Stats;
import com.bapppis.core.creature.playerClass.PlayerClass;
import com.bapppis.core.creature.playerClass.PlayerClassLoader;
import com.bapppis.core.creature.playerClass.PlayerClassService;

import java.util.List;
import java.util.Map;

/**
 * Screen that allows the player to select a class for their character.
 * Shows available classes with their bonuses and allows selection.
 */
public class PlayerClassSelectionScreen {

  private final Stage stage;
  private final Player player;
  private final Runnable onComplete;
  private PlayerClass selectedClass;

  public PlayerClassSelectionScreen(Stage stage, Player player, Runnable onComplete) {
    this.stage = stage;
    this.player = player;
    this.onComplete = onComplete;
  }

  /**
   * Shows the class selection UI
   */
  public void show() {
    stage.clear();

        PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
        List<PlayerClass> classes = new java.util.ArrayList<>(loader.getAllClasses().values());    Table mainTable = new Table();
    mainTable.setFillParent(true);
    stage.addActor(mainTable);

    // Title
    VisLabel title = new VisLabel("Choose Your Class");
    title.setStyle(new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(
        title.getStyle().font, com.badlogic.gdx.graphics.Color.GOLD));
    mainTable.add(title).colspan(2).padBottom(20).row();

    // Left side: Class list
    Table leftPanel = new Table();
    final String[] classNames = classes.stream()
        .map(PlayerClass::getName)
        .toArray(String[]::new);

    final VisList<String> classList = new VisList<>();
    classList.setItems(classNames);
    classList.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        int idx = classList.getSelectedIndex();
        if (idx >= 0 && idx < classes.size()) {
          selectedClass = classes.get(idx);
          updateClassDetails(classes.get(idx));
        }
      }
    });

    VisScrollPane classScroll = new VisScrollPane(classList);
    leftPanel.add(new VisLabel("Available Classes:")).left().row();
    leftPanel.add(classScroll).width(200).height(300).padTop(10);

    mainTable.add(leftPanel).padRight(20);

    // Right side: Class details
    Table rightPanel = new Table();
    rightPanel.top();

    final VisLabel classNameLabel = new VisLabel("");
    final VisLabel descriptionLabel = new VisLabel("");
    descriptionLabel.setWrap(true);

    final VisLabel statsLabel = new VisLabel("");
    final VisLabel resistancesLabel = new VisLabel("");
    final VisLabel resourcesLabel = new VisLabel("");
    final VisLabel traitsLabel = new VisLabel("");
    final VisLabel spellsLabel = new VisLabel("");

    rightPanel.add(classNameLabel).left().row();
    rightPanel.add(descriptionLabel).width(300).left().padTop(10).row();
    rightPanel.add(new VisLabel("")).row(); // spacer
    rightPanel.add(statsLabel).left().row();
    rightPanel.add(resistancesLabel).left().row();
    rightPanel.add(resourcesLabel).left().row();
    rightPanel.add(traitsLabel).left().row();
    rightPanel.add(spellsLabel).left().row();

    mainTable.add(rightPanel).top().left().row();

    // Store references for updating
    mainTable.setUserObject(new Object[] {
        classNameLabel, descriptionLabel, statsLabel,
        resistancesLabel, resourcesLabel, traitsLabel, spellsLabel
    });

    // Buttons
    Table buttonTable = new Table();

    VisTextButton selectButton = new VisTextButton("Select Class");
    selectButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (selectedClass != null) {
          applyClassToPlayer(selectedClass);
          if (onComplete != null) {
            onComplete.run();
          }
        }
      }
    });

    VisTextButton skipButton = new VisTextButton("Skip (No Class)");
    skipButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        // Continue without a class
        if (onComplete != null) {
          onComplete.run();
        }
      }
    });

    buttonTable.add(selectButton).padRight(10);
    buttonTable.add(skipButton);

    mainTable.add(buttonTable).colspan(2).padTop(20);

    // Select first class by default
    if (classes.size() > 0) {
      classList.setSelectedIndex(0);
      selectedClass = classes.get(0);
      updateClassDetails(classes.get(0));
    }
  }

  /**
   * Updates the class details display
   */
  private void updateClassDetails(PlayerClass playerClass) {
    Object[] labels = (Object[]) stage.getRoot().getUserObject();
    if (labels == null || labels.length < 7)
      return;

    VisLabel classNameLabel = (VisLabel) labels[0];
    VisLabel descriptionLabel = (VisLabel) labels[1];
    VisLabel statsLabel = (VisLabel) labels[2];
    VisLabel resistancesLabel = (VisLabel) labels[3];
    VisLabel resourcesLabel = (VisLabel) labels[4];
    VisLabel traitsLabel = (VisLabel) labels[5];
    VisLabel spellsLabel = (VisLabel) labels[6];

    // Class name
    classNameLabel.setText(playerClass.getName());
    classNameLabel.setStyle(new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(
        classNameLabel.getStyle().font, com.badlogic.gdx.graphics.Color.CYAN));

    // Description
    String desc = playerClass.getDescription();
    if (desc == null || desc.isEmpty())
      desc = "A powerful class.";
    descriptionLabel.setText(desc);

    // Stat bonuses
    StringBuilder statsText = new StringBuilder("Stat Bonuses:\n");
    Map<Stats, Integer> statBonuses = playerClass.getStatBonuses();
    if (statBonuses != null && !statBonuses.isEmpty()) {
      for (Map.Entry<Stats, Integer> entry : statBonuses.entrySet()) {
        int value = entry.getValue();
        String sign = value >= 0 ? "+" : "";
        statsText.append("  ").append(entry.getKey().name()).append(": ")
            .append(sign).append(value).append("\n");
      }
    } else {
      statsText.append("  None\n");
    }
    statsLabel.setText(statsText.toString());

    // Resistances
    StringBuilder resistText = new StringBuilder("Resistances:\n");
    Map<Resistances, Integer> resistances = playerClass.getResistances();
    if (resistances != null && !resistances.isEmpty()) {
      for (Map.Entry<Resistances, Integer> entry : resistances.entrySet()) {
        int value = entry.getValue();
        String sign = value >= 0 ? "+" : "";
        resistText.append("  ").append(entry.getKey().name()).append(": ")
            .append(sign).append(value).append("%\n");
      }
    } else {
      resistText.append("  None\n");
    }
    resistancesLabel.setText(resistText.toString());

    // Resources (HP, Mana, Stamina, Regen)
    StringBuilder resourceText = new StringBuilder("Resources:\n");
    if (playerClass.getMaxHpBonus() > 0) {
      resourceText.append("  HP: +").append(playerClass.getMaxHpBonus()).append("\n");
    }
    if (playerClass.getMaxManaBonus() > 0) {
      resourceText.append("  Mana: +").append(playerClass.getMaxManaBonus()).append("\n");
    }
    if (playerClass.getMaxStaminaBonus() > 0) {
      resourceText.append("  Stamina: +").append(playerClass.getMaxStaminaBonus()).append("\n");
    }
    if (playerClass.getHpRegenBonus() > 0) {
      resourceText.append("  HP Regen: +").append(playerClass.getHpRegenBonus()).append("/s\n");
    }
    if (resourceText.length() == "Resources:\n".length()) {
      resourceText.append("  None\n");
    }
    resourcesLabel.setText(resourceText.toString());

    // Granted traits/properties
    StringBuilder traitsText = new StringBuilder("Traits:\n");
    List<String> grantedProps = playerClass.getGrantedProperties();
    if (grantedProps != null && !grantedProps.isEmpty()) {
      for (String prop : grantedProps) {
        traitsText.append("  - ").append(prop).append("\n");
      }
    } else {
      traitsText.append("  None\n");
    }
    traitsLabel.setText(traitsText.toString());

    // Unlocked spells
    StringBuilder spellsText = new StringBuilder("Starting Spells:\n");
    List<String> spells = playerClass.getUnlockedSpells();
    if (spells != null && !spells.isEmpty()) {
      for (String spell : spells) {
        spellsText.append("  - ").append(spell).append("\n");
      }
    } else {
      spellsText.append("  None\n");
    }
    spellsLabel.setText(spellsText.toString());
  }

  /**
   * Applies the selected class to the player
   */
  private void applyClassToPlayer(PlayerClass playerClass) {
    PlayerClassLoader loader = AllLoaders.getPlayerClassLoader();
    PlayerClassService service = new PlayerClassService(loader);

    service.applyClass(player, playerClass);

    com.badlogic.gdx.Gdx.app.log("PlayerClassSelection",
        "Applied class " + playerClass.getName() + " to player " + player.getName());
  }
}

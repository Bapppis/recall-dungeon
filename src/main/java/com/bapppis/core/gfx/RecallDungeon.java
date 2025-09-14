package com.bapppis.core.gfx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.scenes.scene2d.Stage;
// import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bapppis.core.game.Game;
import com.bapppis.core.item.ItemLoader;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.property.PropertyManager;

public class RecallDungeon extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont mapFont;
    private Stage stage;
    private com.bapppis.core.gfx.MapActor mapActor;
    // Left-side character info widgets
    private com.kotcrab.vis.ui.widget.VisLabel nameLabel;
    private com.kotcrab.vis.ui.widget.VisLabel hpLabel;
    private com.kotcrab.vis.ui.widget.VisLabel levelLabel;
    private com.kotcrab.vis.ui.widget.VisLabel statsLabel;
    private com.kotcrab.vis.ui.widget.VisLabel resistLabel;
    private com.kotcrab.vis.ui.widget.VisTextButton inventoryButton;
    private boolean showingInventory = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        // Use VisUI's built-in skin to avoid external skin dependency
        if (!VisUI.isLoaded())
            VisUI.load();
        // use VisUI runtime skin where needed via VisUI.getSkin()

        Gdx.input.setInputProcessor(stage);
        // Load properties and creatures once at startup so selection UI has data
        PropertyManager.loadProperties();
        CreatureLoader.loadCreatures();
        ItemLoader.loadItems();

        showMainMenu();
    }

    private void showMainMenu() {
        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        VisTextButton startButton = new VisTextButton("Start Game");
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Show character selection screen
                showCharacterSelect();
            }
        });
        table.add(startButton).fillX().uniformX();

        VisTextButton exitButton = new VisTextButton("Exit");
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.row().pad(10, 0, 0, 0); // Adds spacing before the next button
        table.add(exitButton).fillX().uniformX();
    }

    private void showCharacterSelect() {
        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Build a list of player names from loaded players
        java.util.List<Player> players = CreatureLoader.getAllPlayers();
        String[] names = players.stream().map(Player::getName).toArray(String[]::new);

        VisList<String> list = new VisList<>();
        list.setItems(names);
        VisScrollPane scroll = new VisScrollPane(list);
        table.add(scroll).width(300).height(200).row();

        VisTextButton play = new VisTextButton("Play");
        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int idx = list.getSelectedIndex();
                if (idx < 0 || idx >= players.size())
                    return;
                Player selected = players.get(idx);
                Gdx.app.log("RecallDungeon",
                        "Selected player index=" + idx + " id=" + selected.getId() + " name=" + selected.getName());
                // Initialize game and start its internal command loop (non-blocking)
                Gdx.app.log("RecallDungeon", "Selected player id=" + selected.getId() + " name=" + selected.getName());
                Game game = new Game(selected);
                game.initialize();
                // Store game instance in the stage's userObject so floor view can access it
                stage.setDebugAll(false);
                stage.getRoot().setUserObject(game);
                showFloorView();
            }
        });

        VisTextButton back = new VisTextButton("Back");
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMainMenu();
            }
        });

        table.row().pad(10, 0, 0, 0);
        table.add(play).uniformX().padRight(10);
        table.add(back).uniformX();
    }

    public void showFloorView() {
        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Center area: map label inside a scroll pane
        // Try to use a bundled bitmap font (prefer smaller/monospaced if present).
        BitmapFont chosen = null;
        try {
            if (Gdx.files.internal("font-small.fnt").exists()) {
                chosen = new BitmapFont(Gdx.files.internal("font-small.fnt"));
            } else if (Gdx.files.internal("default.fnt").exists()) {
                chosen = new BitmapFont(Gdx.files.internal("default.fnt"));
            }
        } catch (Exception e) {
            Gdx.app.log("RecallDungeon", "Error loading bitmap font for map", e);
        }
        if (chosen == null)
            chosen = font; // fallback to default
        // keep track so we can dispose only if it's different from the main `font`
        mapFont = chosen != font ? chosen : null;

        mapActor = new com.bapppis.core.gfx.MapActor(chosen);
        // Put the actor inside a container table cell and center it. Don't force fill
        // so actor size is used.
        com.badlogic.gdx.scenes.scene2d.ui.Container<com.bapppis.core.gfx.MapActor> container = new com.badlogic.gdx.scenes.scene2d.ui.Container<>(
                mapActor);
        container.center();

        // Left sidebar: character info
        com.kotcrab.vis.ui.widget.VisTable left = new com.kotcrab.vis.ui.widget.VisTable(true);
        nameLabel = new com.kotcrab.vis.ui.widget.VisLabel("Name: -");
        hpLabel = new com.kotcrab.vis.ui.widget.VisLabel("HP: -/-");
        levelLabel = new com.kotcrab.vis.ui.widget.VisLabel("Level: -");
        statsLabel = new com.kotcrab.vis.ui.widget.VisLabel("Stats: -");
        resistLabel = new com.kotcrab.vis.ui.widget.VisLabel("Resists: -");
        inventoryButton = new com.kotcrab.vis.ui.widget.VisTextButton("Inventory");
        inventoryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Toggle inventory view
                showingInventory = !showingInventory;
                if (showingInventory)
                    showInventory(left);
                else
                    showFloorView();
            }
        });

        left.add(nameLabel).left().row();
        left.add(hpLabel).left().row();
        left.add(levelLabel).left().row();
        left.add(statsLabel).left().padTop(8).row();
        left.add(resistLabel).left().padTop(8).row();
        left.add().expandY().row(); // spacer
        left.add(inventoryButton).width(140).padTop(8).row();

        // Add left sidebar and map container side-by-side
        table.add(left).width(300).fillY().top().pad(8);
        table.add(container).expand().fill().row();

        // Bottom area: Back button only (commands via keyboard)
        VisTextButton back = new VisTextButton("Back to Menu");
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // remove keyboard listener when leaving
                Gdx.input.setInputProcessor(stage);
                showMainMenu();
            }
        });

        table.row().pad(10, 0, 0, 0);
        table.add(back).colspan(2).fillX().uniformX();
        // Schedule initial refreshes to ensure UI shows the map after game initializes
        refreshMapDisplay();
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                refreshMapDisplay();
            }
        }, 0.12f);
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                refreshMapDisplay();
            }
        }, 0.35f);

        // Add keyboard input handling: map keys to game commands
        final com.badlogic.gdx.InputAdapter keyHandler = new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                Object userObj = stage.getRoot().getUserObject();
                if (!(userObj instanceof Game))
                    return false;
                Game g = (Game) userObj;
                String cmd = null;
                switch (keycode) {
                    case com.badlogic.gdx.Input.Keys.W:
                    case com.badlogic.gdx.Input.Keys.UP:
                        cmd = "move north";
                        break;
                    case com.badlogic.gdx.Input.Keys.S:
                    case com.badlogic.gdx.Input.Keys.DOWN:
                        cmd = "move south";
                        break;
                    case com.badlogic.gdx.Input.Keys.A:
                    case com.badlogic.gdx.Input.Keys.LEFT:
                        cmd = "move west";
                        break;
                    case com.badlogic.gdx.Input.Keys.D:
                    case com.badlogic.gdx.Input.Keys.RIGHT:
                        cmd = "move east";
                        break;
                    case com.badlogic.gdx.Input.Keys.COMMA:
                        cmd = "down"; // go down a floor
                        break;
                    case com.badlogic.gdx.Input.Keys.PERIOD:
                        cmd = "up"; // go up a floor
                        break;
                    case com.badlogic.gdx.Input.Keys.L:
                        cmd = "look";
                        break;
                }
                if (cmd != null) {
                    g.submitCommand(cmd);
                    // refresh map display now and shortly after to pick up async changes
                    refreshMapDisplay();
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            refreshMapDisplay();
                        }
                    }, 0.12f);
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            refreshMapDisplay();
                        }
                    }, 0.35f);
                    return true;
                }
                return false;
            }
        };

        // Use an InputMultiplexer so Stage still receives UI events
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(keyHandler);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
        batch.begin();
        // no title text drawn on floor view
        batch.end();
    }

    private void refreshMapDisplay() {
        if (mapActor == null)
            return;
        mapActor.refresh();
        // Update left sidebar info from current player
        try {
            com.bapppis.core.creature.player.Player p = com.bapppis.core.game.GameState.getPlayer();
            if (p != null) {
                if (nameLabel != null)
                    nameLabel.setText("Name: " + (p.getName() != null ? p.getName() : "-"));
                if (hpLabel != null)
                    hpLabel.setText("HP: " + p.getCurrentHp() + "/" + p.getMaxHp());
                if (levelLabel != null)
                    levelLabel.setText("Level: " + p.getLevel());
                if (statsLabel != null) {
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.creature.Creature.Stats s : com.bapppis.core.creature.Creature.Stats
                            .values()) {
                        if (s == com.bapppis.core.creature.Creature.Stats.LUCK)
                            continue; // skip luck
                        sb.append(s.name()).append(": ").append(p.getStat(s)).append('\n');
                    }
                    statsLabel.setText(sb.toString());
                }
                if (resistLabel != null) {
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.creature.Creature.Resistances r : com.bapppis.core.creature.Creature.Resistances
                            .values()) {
                        if (r == com.bapppis.core.creature.Creature.Resistances.TRUE)
                            continue; // hide TRUE
                        sb.append(r.name()).append(": ").append(p.getResistance(r)).append('%').append('\n');
                    }
                    resistLabel.setText(sb.toString());
                }
            }
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error updating player info", e);
        }
    }

    private void showInventory(com.kotcrab.vis.ui.widget.VisTable left) {
        left.clear();
        com.bapppis.core.creature.player.Player p = com.bapppis.core.game.GameState.getPlayer();
        if (p == null)
            return;
        // Equipped items
        left.add(new com.kotcrab.vis.ui.widget.VisLabel("Equipped:")).left().row();
        for (com.bapppis.core.item.EquipmentSlot slot : com.bapppis.core.item.EquipmentSlot.values()) {
            com.bapppis.core.item.Item eq = p.getEquipped(slot);
            String text = slot.name() + ": " + (eq != null ? eq.getName() : "Empty");
            left.add(new com.kotcrab.vis.ui.widget.VisLabel(text)).left().row();
        }
        left.add().padTop(8).row();
        left.add(new com.kotcrab.vis.ui.widget.VisLabel("Inventory (click to equip):")).left().row();

        com.kotcrab.vis.ui.widget.VisTextButton backBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Back");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showingInventory = false;
                showFloorView();
            }
        });

        // For each inventory group, add items with equip buttons
        com.bapppis.core.creature.Inventory inv = p.getInventory();
        java.util.function.BiConsumer<String, java.util.List<com.bapppis.core.item.Item>> addGroup = (label, items) -> {
            if (items == null || items.isEmpty())
                return;
            left.add(new com.kotcrab.vis.ui.widget.VisLabel(label)).left().padTop(6).row();
            for (com.bapppis.core.item.Item item : items) {
                com.kotcrab.vis.ui.widget.VisTable row = new com.kotcrab.vis.ui.widget.VisTable(true);
                row.add(new com.kotcrab.vis.ui.widget.VisLabel(item.getName())).left();
                com.kotcrab.vis.ui.widget.VisTextButton actionBtn;
                if (item.getType() == com.bapppis.core.item.ItemType.CONSUMABLE) {
                    actionBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Use");
                    actionBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            try {
                                item.onApply(p);
                                // remove consumable from inventory
                                p.getInventory().removeItem(item);
                                showInventory(left);
                                refreshMapDisplay();
                            } catch (Exception e) {
                                Gdx.app.error("RecallDungeon", "Error using consumable", e);
                            }
                        }
                    });
                } else {
                    actionBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Equip");
                    actionBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            try {
                                if (item instanceof com.bapppis.core.item.Equipment) {
                                    p.equipItem((com.bapppis.core.item.Equipment) item);
                                    // refresh inventory view and labels
                                    showInventory(left);
                                    refreshMapDisplay();
                                }
                            } catch (Exception e) {
                                Gdx.app.error("RecallDungeon", "Error equipping item", e);
                            }
                        }
                    });
                }
                row.add(actionBtn).padLeft(4).right();
                left.add(row).fillX().row();
            }
        };

        addGroup.accept("Weapons", inv.getWeapons());
        addGroup.accept("Offhands", inv.getOffhands());
        addGroup.accept("Helmets", inv.getHelmets());
        addGroup.accept("Armors", inv.getArmors());
        addGroup.accept("Legwear", inv.getLegwear());
        addGroup.accept("Consumables", inv.getConsumables());
        addGroup.accept("Misc", inv.getMisc());

        left.add().expandY().row();
        left.add(backBtn).width(140).row();
    }

    @Override
    public void dispose() {
        // Shutdown running Game loop if present
        try {
            Object userObj = stage != null ? stage.getRoot().getUserObject() : null;
            if (userObj instanceof Game) {
                ((Game) userObj).shutdown();
            }
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error shutting down game", e);
        }

        batch.dispose();
        font.dispose();
        if (mapFont != null)
            mapFont.dispose();
        stage.dispose();
        if (VisUI.isLoaded())
            VisUI.dispose();
    }
}

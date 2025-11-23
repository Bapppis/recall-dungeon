package com.bapppis.core.gfx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bapppis.core.game.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.bapppis.core.creature.CreatureLoader;
import com.bapppis.core.creature.Player;
import com.bapppis.core.AllLoaders;

public class RecallDungeon extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont mapFont;
    private Stage stage;
    private com.bapppis.core.gfx.MapActor mapActor;
    private com.badlogic.gdx.graphics.g2d.TextureAtlas spriteAtlas;
    private com.kotcrab.vis.ui.widget.VisLabel nameLabel;
    private com.kotcrab.vis.ui.widget.VisLabel hpLabel;
    private com.kotcrab.vis.ui.widget.VisLabel manaLabel;
    private com.kotcrab.vis.ui.widget.VisLabel staminaLabel;
    private com.kotcrab.vis.ui.widget.VisLabel levelLabel;
    private com.kotcrab.vis.ui.widget.VisLabel statsLabel;
    private com.kotcrab.vis.ui.widget.VisLabel resistLabel;
    private com.kotcrab.vis.ui.widget.VisTextButton inventoryButton;
    private com.kotcrab.vis.ui.widget.VisTextButton statsButton;
    private boolean showingInventory = false;
    private boolean awaitingInteractionDirection = false;
    private com.badlogic.gdx.scenes.scene2d.ui.Label interactionPromptLabel;
    private Game currentGame; // Store the Game instance to preserve it across view changes

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        if (!VisUI.isLoaded())
            VisUI.load();

        Gdx.input.setInputProcessor(stage);
        try {
            AllLoaders.loadAll();
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error loading assets at startup", e);
        }

        showMainMenu();
    }

    private void showInventoryDialog() {
        com.kotcrab.vis.ui.widget.VisTable table = new com.kotcrab.vis.ui.widget.VisTable(true);
        showInventory(table);
        com.kotcrab.vis.ui.widget.VisDialog dialog = new com.kotcrab.vis.ui.widget.VisDialog("Inventory");
        dialog.add(table).pad(8).row();
        dialog.button("Close", true);
        dialog.show(stage);
    }

    private void showStatsDialog() {
        com.kotcrab.vis.ui.widget.VisTable table = new com.kotcrab.vis.ui.widget.VisTable(true);
        table.add(statsLabel).left().row();
        table.add(resistLabel).left().row();
        com.kotcrab.vis.ui.widget.VisDialog dialog = new com.kotcrab.vis.ui.widget.VisDialog("Stats");
        dialog.add(table).pad(8).row();
        dialog.button("Close", true);
        dialog.show(stage);
    }

    private void showLootTransferDialog(com.bapppis.core.dungeon.Coordinate coord) {
        com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState.getCurrentFloor();
        com.bapppis.core.creature.Player player = com.bapppis.core.game.GameState.getPlayer();
        if (floor == null || player == null)
            return;

        com.bapppis.core.dungeon.Tile tile = floor.getTile(coord);
        if (tile == null || tile.getItems() == null || tile.getItems().isEmpty()) {
            System.out.println("There's nothing to loot here.");
            return;
        }

        com.kotcrab.vis.ui.widget.VisDialog dialog = new com.kotcrab.vis.ui.widget.VisDialog("Loot");
        com.kotcrab.vis.ui.widget.VisTable mainTable = new com.kotcrab.vis.ui.widget.VisTable(true);

        // Determine if this is a chest or ground loot
        boolean isChest = tile.getSymbol() == 'C';
        String containerLabel = isChest ? "Chest:" : "On Ground:";

        // Left side: Ground items
        com.kotcrab.vis.ui.widget.VisTable groundTable = new com.kotcrab.vis.ui.widget.VisTable(true);
        groundTable.add(new com.kotcrab.vis.ui.widget.VisLabel(containerLabel)).left().row();

        // Right side: Player inventory
        com.kotcrab.vis.ui.widget.VisTable invTable = new com.kotcrab.vis.ui.widget.VisTable(true);
        com.bapppis.core.creature.Inventory inv = player.getInventory();
        String capacityText = "Inventory (" + inv.getCurrentLoad() + "/" + inv.getMaxCapacity() + "):";
        invTable.add(new com.kotcrab.vis.ui.widget.VisLabel(capacityText)).left().row();

        Runnable refreshDialog = new Runnable() {
            @Override
            public void run() {
                dialog.hide();
                showLootTransferDialog(coord);
            }
        };

        // Add ground items with "Take" buttons
        java.util.List<com.bapppis.core.item.Item> groundItems = new java.util.ArrayList<>(tile.getItems());
        for (com.bapppis.core.item.Item item : groundItems) {
            com.kotcrab.vis.ui.widget.VisTable row = new com.kotcrab.vis.ui.widget.VisTable(true);
            row.add(new com.kotcrab.vis.ui.widget.VisLabel(item.getName())).left().width(200);
            com.kotcrab.vis.ui.widget.VisTextButton takeBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Take");
            takeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (player.getInventory().addItem(item)) {
                        tile.getItems().remove(item);

                        // Replace empty tile with floor ONLY if it's not a chest
                        if (tile.getItems().isEmpty()) {
                            boolean isChest = tile.getSymbol() == 'C';
                            if (!isChest) {
                                // Replace corpses/dropped items with floor when empty
                                com.bapppis.core.dungeon.TileType floorType = com.bapppis.core.dungeon.TileTypeLoader
                                        .getTileTypeByName("basicFloor");
                                if (floorType != null) {
                                    com.bapppis.core.dungeon.Tile floorTile = new com.bapppis.core.dungeon.Tile(coord,
                                            floorType);
                                    floorTile.setLeft(tile.getLeft());
                                    floorTile.setRight(tile.getRight());
                                    floorTile.setUp(tile.getUp());
                                    floorTile.setDown(tile.getDown());
                                    floorTile.setDiscovered(tile.isDiscovered());
                                    floor.addTile(coord, floorTile);

                                    if (floorTile.getLeft() != null)
                                        floorTile.getLeft().setRight(floorTile);
                                    if (floorTile.getRight() != null)
                                        floorTile.getRight().setLeft(floorTile);
                                    if (floorTile.getUp() != null)
                                        floorTile.getUp().setDown(floorTile);
                                    if (floorTile.getDown() != null)
                                        floorTile.getDown().setUp(floorTile);
                                }
                            }
                            dialog.hide();
                            refreshMapDisplay();
                        } else {
                            refreshDialog.run();
                        }
                    }
                }
            });
            row.add(takeBtn).padLeft(4);
            groundTable.add(row).fillX().row();
        }

        // Add inventory items (simplified view)
        java.util.List<com.bapppis.core.item.Item> allItems = new java.util.ArrayList<>();
        allItems.addAll(inv.getWeapons());
        allItems.addAll(inv.getOffhands());
        allItems.addAll(inv.getHelmets());
        allItems.addAll(inv.getArmors());
        allItems.addAll(inv.getLegwear());
        allItems.addAll(inv.getConsumables());
        allItems.addAll(inv.getMisc());

        for (com.bapppis.core.item.Item item : allItems) {
            invTable.add(new com.kotcrab.vis.ui.widget.VisLabel(item.getName())).left().row();
        }

        com.kotcrab.vis.ui.widget.VisScrollPane groundScroll = new com.kotcrab.vis.ui.widget.VisScrollPane(groundTable);
        groundScroll.setFadeScrollBars(false);
        com.kotcrab.vis.ui.widget.VisScrollPane invScroll = new com.kotcrab.vis.ui.widget.VisScrollPane(invTable);
        invScroll.setFadeScrollBars(false);

        mainTable.add(groundScroll).width(250).height(300).pad(4);
        mainTable.add(invScroll).width(250).height(300).pad(4);

        dialog.add(mainTable).row();
        dialog.button("Close");
        dialog.show(stage);
    }

    private void showMainMenu() {
        // Shutdown the game if it's running
        try {
            Object userObj = stage.getRoot().getUserObject();
            if (userObj instanceof Game) {
                ((Game) userObj).shutdown();
                stage.getRoot().setUserObject(null);
            }
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error shutting down game", e);
        }

        // Clean up any runtime view resources to avoid stale references or disposed
        // textures
        try {
            if (spriteAtlas != null) {
                try {
                    spriteAtlas.dispose();
                } catch (Exception ignored) {
                }
                spriteAtlas = null;
            }
        } catch (Exception ignored) {
        }
        // Drop reference to the map actor so any scheduled refreshes become no-ops
        mapActor = null;

        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        VisTextButton startButton = new VisTextButton("Start Game");
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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
        table.row().pad(10, 0, 0, 0);
        table.add(exitButton).fillX().uniformX();
    }

    private void showCharacterSelect() {
        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        java.util.List<Player> tmpPlayers = CreatureLoader.getAllPlayers();
        final java.util.List<Player> players = (tmpPlayers == null) ? java.util.Collections.emptyList() : tmpPlayers;
        final String[] names = players.stream().map(p -> p == null ? "<unknown>" : p.getName()).toArray(String[]::new);

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
                com.bapppis.core.game.Game.selectPlayer(selected);
                Gdx.app.log("RecallDungeon",
                        "Selected player index=" + idx + " id=" + selected.getId() + " name=" + selected.getName());

                // Show class selection screen before starting game
                showClassSelection(selected);
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

    private void showClassSelection(Player player) {
        PlayerClassSelectionScreen classSelection = new PlayerClassSelectionScreen(
                stage,
                player,
                () -> startGameWithPlayer(player));
        classSelection.show();
    }

    private void startGameWithPlayer(Player player) {
        currentGame = new Game(player);
        currentGame.initialize();
        stage.setDebugAll(false);
        stage.getRoot().setUserObject(currentGame);

        // Set the loot transfer callback
        com.bapppis.core.game.GameState.setLootTransferCallback(new com.bapppis.core.game.LootTransferCallback() {
            @Override
            public void showLootTransferDialog(com.bapppis.core.dungeon.Coordinate coord) {
                RecallDungeon.this.showLootTransferDialog(coord);
            }
        });

        showFloorView();
    }

    public void showFloorView() {
        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Restore the Game object to the new root
        if (currentGame != null) {
            stage.getRoot().setUserObject(currentGame);
        }
        BitmapFont chosen = font;
        try {
            if (Gdx.files.internal("default.fnt").exists()) {
                chosen = new BitmapFont(Gdx.files.internal("default.fnt"));
            }
        } catch (Exception e) {
            Gdx.app.log("RecallDungeon", "Error loading bitmap font for map", e);
        }

        if (spriteAtlas == null) {
            spriteAtlas = com.bapppis.core.gfx.AtlasBuilder.loadWithFallback();
        }

        if (spriteAtlas == null) {
            Gdx.app.log("RecallDungeon", "spriteAtlas is null (no atlas loaded)");
        }

        mapActor = new com.bapppis.core.gfx.MapActor(chosen, spriteAtlas);

        // Zoom the map 2x to make tiles larger
        float zoomScale = 2.0f;
        mapActor.setZoomFactor(zoomScale);

        com.badlogic.gdx.scenes.scene2d.ui.Container<com.bapppis.core.gfx.MapActor> container = new com.badlogic.gdx.scenes.scene2d.ui.Container<>(
                mapActor);
        container.center();
        container.setClip(true); // Clip to viewport size
        container.fillX();
        container.fillY();

        // Add map to main table centered
        table.add(container).expand().fill();

        // Create HUD overlay as a separate layer on the stage
        final com.badlogic.gdx.scenes.scene2d.ui.Table hudOverlay = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        hudOverlay.setFillParent(true);
        stage.addActor(hudOverlay);

        // Create HUD labels
        nameLabel = new com.kotcrab.vis.ui.widget.VisLabel("Name: -");
        hpLabel = new com.kotcrab.vis.ui.widget.VisLabel("HP: -/-");
        manaLabel = new com.kotcrab.vis.ui.widget.VisLabel("Mana: -/-");
        staminaLabel = new com.kotcrab.vis.ui.widget.VisLabel("Stamina: -/-");
        levelLabel = new com.kotcrab.vis.ui.widget.VisLabel("Level: -");
        statsLabel = new com.kotcrab.vis.ui.widget.VisLabel("Stats: -");
        resistLabel = new com.kotcrab.vis.ui.widget.VisLabel("Resists: -");

        // Create interaction prompt label (centered at bottom)
        interactionPromptLabel = new com.kotcrab.vis.ui.widget.VisLabel("");
        interactionPromptLabel.setColor(com.badlogic.gdx.graphics.Color.YELLOW);

        // Top-left HUD: name, hp, mana, stamina, level
        com.badlogic.gdx.scenes.scene2d.ui.Table topLeft = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        topLeft.add(nameLabel).left().pad(4).row();
        topLeft.add(hpLabel).left().pad(4).row();
        topLeft.add(manaLabel).left().pad(4).row();
        topLeft.add(staminaLabel).left().pad(4).row();
        topLeft.add(levelLabel).left().pad(4).row();

        // Bottom-right HUD: stats and inventory buttons
        statsButton = new com.kotcrab.vis.ui.widget.VisTextButton("Stats");
        inventoryButton = new com.kotcrab.vis.ui.widget.VisTextButton("Inventory");
        statsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showStatsDialog();
            }
        });
        inventoryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showInventoryDialog();
            }
        });

        com.badlogic.gdx.scenes.scene2d.ui.Table bottomRight = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        bottomRight.add(statsButton).pad(4);
        bottomRight.add(inventoryButton).pad(4);

        // Bottom-left: Back to Menu button
        VisTextButton back = new VisTextButton("Back to Menu");
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.setInputProcessor(stage);
                showMainMenu();
            }
        });

        VisTextButton reloadTextures = new VisTextButton("Reload Textures");
        reloadTextures.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("RecallDungeon", "Reloading sprite atlas...");
                try {
                    // Dispose old atlas if present
                    if (spriteAtlas != null) {
                        try {
                            spriteAtlas.dispose();
                        } catch (Exception ignored) {
                        }
                        spriteAtlas = null;
                    }
                    spriteAtlas = com.bapppis.core.gfx.AtlasBuilder.loadWithFallback();
                    if (spriteAtlas == null) {
                        Gdx.app.log("RecallDungeon", "Reload failed: atlas is null");
                    } else {
                        Gdx.app.log("RecallDungeon",
                                "Reloaded atlas with " + spriteAtlas.getRegions().size + " regions");
                        if (mapActor != null) {
                            mapActor.setAtlas(spriteAtlas);
                            refreshMapDisplay();
                        }
                    }
                } catch (Exception e) {
                    Gdx.app.error("RecallDungeon", "Error reloading sprite atlas", e);
                }
            }
        });

        com.badlogic.gdx.scenes.scene2d.ui.Table bottomLeft = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        bottomLeft.add(back).pad(4).row();
        bottomLeft.add(reloadTextures).pad(4);

        // Center bottom: interaction prompt
        com.badlogic.gdx.scenes.scene2d.ui.Table bottomCenter = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        bottomCenter.add(interactionPromptLabel).pad(12);

        // Layout HUD overlay: top-left, bottom-left, bottom-center, bottom-right
        hudOverlay.add(topLeft).expand().top().left().pad(12);
        hudOverlay.add().expand(); // spacer
        hudOverlay.row();
        hudOverlay.add(bottomLeft).expand().bottom().left().pad(12);
        hudOverlay.add(bottomCenter).expandX().bottom().center().pad(12);
        hudOverlay.add(bottomRight).expand().bottom().right().pad(12);
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

        final com.badlogic.gdx.InputAdapter keyHandler = new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                Object userObj = stage.getRoot().getUserObject();
                if (!(userObj instanceof Game)) {
                    return false;
                }
                Game g = (Game) userObj;
                String cmd = null;

                // Handle interaction mode
                if (awaitingInteractionDirection) {
                    String direction = null;
                    switch (keycode) {
                        case com.badlogic.gdx.Input.Keys.W:
                        case com.badlogic.gdx.Input.Keys.UP:
                            direction = "north";
                            break;
                        case com.badlogic.gdx.Input.Keys.S:
                        case com.badlogic.gdx.Input.Keys.DOWN:
                            direction = "south";
                            break;
                        case com.badlogic.gdx.Input.Keys.A:
                        case com.badlogic.gdx.Input.Keys.LEFT:
                            direction = "west";
                            break;
                        case com.badlogic.gdx.Input.Keys.D:
                        case com.badlogic.gdx.Input.Keys.RIGHT:
                            direction = "east";
                            break;
                        case com.badlogic.gdx.Input.Keys.ESCAPE:
                            // Cancel interaction mode
                            awaitingInteractionDirection = false;
                            if (interactionPromptLabel != null) {
                                interactionPromptLabel.setText("");
                            }
                            return true;
                    }

                    if (direction != null) {
                        awaitingInteractionDirection = false;
                        if (interactionPromptLabel != null) {
                            interactionPromptLabel.setText("");
                        }
                        cmd = "interact " + direction;
                        g.submitCommand(cmd);
                        refreshMapDisplay();
                        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                            @Override
                            public void run() {
                                refreshMapDisplay();
                            }
                        }, 0.12f);
                        return true;
                    }
                    return false;
                }

                switch (keycode) {
                    case com.badlogic.gdx.Input.Keys.I:
                        // Toggle inventory
                        if (showingInventory) {
                            showingInventory = false;
                            showFloorView();
                        } else {
                            showInventoryDialog();
                        }
                        return true;
                    case com.badlogic.gdx.Input.Keys.E:
                        // Enter interaction mode
                        awaitingInteractionDirection = true;
                        if (interactionPromptLabel != null) {
                            interactionPromptLabel.setText("Press a direction to interact with (or ESC to cancel)");
                        }
                        return true;
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
                        cmd = "down";
                        break;
                    case com.badlogic.gdx.Input.Keys.PERIOD:
                        cmd = "up";
                        break;
                    case com.badlogic.gdx.Input.Keys.L:
                        cmd = "look";
                        break;
                    case com.badlogic.gdx.Input.Keys.SPACE:
                        cmd = "wait";
                        break;
                    case com.badlogic.gdx.Input.Keys.R:
                        // Reveal entire floor
                        com.bapppis.core.dungeon.Floor cfR = com.bapppis.core.game.GameState.getCurrentFloor();
                        if (cfR != null) {
                            cfR.revealAll();
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
                        break;
                    case com.badlogic.gdx.Input.Keys.H:
                        // Hide entire floor
                        com.bapppis.core.dungeon.Floor cfH = com.bapppis.core.game.GameState.getCurrentFloor();
                        if (cfH != null) {
                            cfH.hideAll();
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
                        break;
                }
                if (cmd != null) {
                    g.submitCommand(cmd);
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

        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(keyHandler);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render() {
        // Check if combat has started and we need to show combat view
        if (com.bapppis.core.game.GameState.isInCombat()) {
            com.bapppis.core.creature.Enemy enemy = com.bapppis.core.game.GameState.getCombatEnemy();
            if (enemy != null && !(stage.getRoot().getUserObject() instanceof String
                    && "combat".equals(stage.getRoot().getUserObject()))) {
                showCombatView(enemy);
                stage.getRoot().setUserObject("combat"); // Mark that we're in combat view
            }
        } else {
            // If we were in combat but it ended, return to floor view
            if (stage.getRoot().getUserObject() instanceof String && "combat".equals(stage.getRoot().getUserObject())) {
                stage.getRoot().setUserObject(null);
                showFloorView();
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
        batch.begin();
        batch.end();
    }

    private void refreshMapDisplay() {
        if (mapActor == null)
            return;
        mapActor.refresh();
        // Update left sidebar info from current player
        try {
            com.bapppis.core.creature.Player p = com.bapppis.core.game.GameState.getPlayer();
            if (p != null) {
                if (nameLabel != null)
                    nameLabel.setText("Name: " + (p.getName() != null ? p.getName() : "-"));
                if (hpLabel != null)
                    hpLabel.setText("HP: " + p.getCurrentHp() + "/" + p.getMaxHp());
                if (manaLabel != null) {
                    try {
                        manaLabel.setText("Mana: " + p.getCurrentMana() + "/" + p.getMaxMana());
                    } catch (Exception ignored) {
                    }
                }
                if (staminaLabel != null) {
                    try {
                        staminaLabel.setText("Stamina: " + p.getCurrentStamina() + "/" + p.getMaxStamina());
                    } catch (Exception ignored) {
                    }
                }
                if (levelLabel != null)
                    levelLabel.setText("Level: " + p.getLevel());
                if (statsLabel != null) {
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.creature.creatureEnums.Stats s : com.bapppis.core.creature.creatureEnums.Stats
                            .values()) {
                        if (s == com.bapppis.core.creature.creatureEnums.Stats.LUCK)
                            continue;
                        sb.append(s.name()).append(": ").append(p.getStat(s)).append('\n');
                    }
                    statsLabel.setText(sb.toString());
                }
                if (resistLabel != null) {
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.Resistances r : com.bapppis.core.Resistances.values()) {
                        if (r == com.bapppis.core.Resistances.TRUE)
                            continue;
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
        com.bapppis.core.creature.Player p = com.bapppis.core.game.GameState.getPlayer();
        if (p == null)
            return;
        left.add(new com.kotcrab.vis.ui.widget.VisLabel("Equipped:")).left().row();
        for (com.bapppis.core.item.itemEnums.EquipmentSlot slot : com.bapppis.core.item.itemEnums.EquipmentSlot
                .values()) {
            com.bapppis.core.item.Item eq = p.getEquipped(slot);
            String text = slot.name() + ": " + (eq != null ? eq.getName() : "Empty");
            left.add(new com.kotcrab.vis.ui.widget.VisLabel(text)).left().row();
        }
        left.add().padTop(8).row();

        // Show capacity info
        com.bapppis.core.creature.Inventory inv = p.getInventory();
        String capacityText = "Inventory (" + inv.getCurrentLoad() + "/" + inv.getMaxCapacity() + "):";
        if (inv.isOverEncumbered()) {
            capacityText += " OVER-ENCUMBERED!";
        }
        left.add(new com.kotcrab.vis.ui.widget.VisLabel(capacityText)).left().row();

        com.kotcrab.vis.ui.widget.VisTextButton backBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Back");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showingInventory = false;
                showFloorView();
            }
        });

        java.util.function.BiConsumer<String, java.util.List<com.bapppis.core.item.Item>> addGroup = (label, items) -> {
            if (items == null || items.isEmpty())
                return;
            left.add(new com.kotcrab.vis.ui.widget.VisLabel(label)).left().padTop(6).row();
            for (com.bapppis.core.item.Item item : items) {
                com.kotcrab.vis.ui.widget.VisTable row = new com.kotcrab.vis.ui.widget.VisTable(true);
                row.add(new com.kotcrab.vis.ui.widget.VisLabel(item.getName())).left().expandX();

                // Add Drop button
                com.kotcrab.vis.ui.widget.VisTextButton dropBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Drop");
                dropBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        try {
                            p.getInventory().removeItem(item);

                            // Create a dropped item tile at player's position
                            com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState.getCurrentFloor();
                            if (floor != null && p.getPosition() != null) {
                                com.bapppis.core.dungeon.Tile currentTile = floor.getTile(p.getPosition());
                                if (currentTile != null) {
                                    // Check if tile already has items or is a corpse
                                    if (currentTile.getSymbol() == '*' || currentTile.getSymbol() == '%') {
                                        // Just add to existing items
                                        currentTile.getItems().add(item);
                                    } else {
                                        // Create a new dropped item tile
                                        // Use dropped_potion for consumables, dropped_item for everything else
                                        String droppedTileTypeName = "droppedItem";
                                        if (item.getType() == com.bapppis.core.item.itemEnums.ItemType.CONSUMABLE) {
                                            // Check if we have a droppedPotion tile type
                                            if (com.bapppis.core.dungeon.TileTypeLoader.hasTileType("droppedPotion")) {
                                                droppedTileTypeName = "droppedPotion";
                                            }
                                        }

                                        com.bapppis.core.dungeon.TileType droppedItemType = com.bapppis.core.dungeon.TileTypeLoader
                                                .getTileTypeByName(droppedTileTypeName);
                                        if (droppedItemType != null) {
                                            com.bapppis.core.dungeon.Tile droppedTile = new com.bapppis.core.dungeon.Tile(
                                                    currentTile.getCoordinate(), droppedItemType);
                                            droppedTile.getItems().add(item);

                                            // Copy navigation and discovery state
                                            droppedTile.setLeft(currentTile.getLeft());
                                            droppedTile.setRight(currentTile.getRight());
                                            droppedTile.setUp(currentTile.getUp());
                                            droppedTile.setDown(currentTile.getDown());
                                            droppedTile.setDiscovered(currentTile.isDiscovered());

                                            // Replace tile in floor
                                            floor.addTile(currentTile.getCoordinate(), droppedTile);

                                            // Update navigation references
                                            if (droppedTile.getLeft() != null)
                                                droppedTile.getLeft().setRight(droppedTile);
                                            if (droppedTile.getRight() != null)
                                                droppedTile.getRight().setLeft(droppedTile);
                                            if (droppedTile.getUp() != null)
                                                droppedTile.getUp().setDown(droppedTile);
                                            if (droppedTile.getDown() != null)
                                                droppedTile.getDown().setUp(droppedTile);
                                        }
                                    }
                                }
                            }

                            showInventory(left);
                            refreshMapDisplay();
                        } catch (Exception e) {
                            Gdx.app.error("RecallDungeon", "Error dropping item", e);
                        }
                    }
                });
                row.add(dropBtn).padLeft(4).width(60);

                com.kotcrab.vis.ui.widget.VisTextButton actionBtn;
                if (item.getType() == com.bapppis.core.item.itemEnums.ItemType.CONSUMABLE) {
                    actionBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Use");
                    actionBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            try {
                                item.onApply(p);
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
                                    showInventory(left);
                                    refreshMapDisplay();
                                }
                            } catch (Exception e) {
                                Gdx.app.error("RecallDungeon", "Error equipping item", e);
                            }
                        }
                    });
                }
                row.add(actionBtn).padLeft(4).width(60).right();
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

    public void showCombatView(com.bapppis.core.creature.Creature enemy) {
        // No need to preserve from stage - use instance variable

        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Mark as combat view (not Game object, to let render loop know we're in
        // combat)
        stage.getRoot().setUserObject("combat");

        com.kotcrab.vis.ui.widget.VisTable left = new com.kotcrab.vis.ui.widget.VisTable(true);
        com.bapppis.core.creature.Player p = com.bapppis.core.game.GameState.getPlayer();

        // Player sprite
        if (p != null && spriteAtlas != null) {
            String playerSprite = p.getSprite();
            if (playerSprite != null && !playerSprite.isEmpty()) {
                com.badlogic.gdx.graphics.g2d.TextureRegion playerReg = spriteAtlas.findRegion(playerSprite);
                if (playerReg != null) {
                    com.badlogic.gdx.scenes.scene2d.ui.Image playerImg = new com.badlogic.gdx.scenes.scene2d.ui.Image(
                            playerReg);
                    left.add(playerImg).size(48, 48).pad(8).row();
                }
            }
        }

        nameLabel = new com.kotcrab.vis.ui.widget.VisLabel("Name: -");
        hpLabel = new com.kotcrab.vis.ui.widget.VisLabel("HP: -/-");
        manaLabel = new com.kotcrab.vis.ui.widget.VisLabel("Mana: -/-");
        staminaLabel = new com.kotcrab.vis.ui.widget.VisLabel("Stamina: -/-");
        levelLabel = new com.kotcrab.vis.ui.widget.VisLabel("Level: -");
        statsLabel = new com.kotcrab.vis.ui.widget.VisLabel("Stats: -");
        resistLabel = new com.kotcrab.vis.ui.widget.VisLabel("Resists: -");
        if (p != null) {
            nameLabel.setText("Name: " + p.getName());
            hpLabel.setText("HP: " + p.getCurrentHp() + "/" + p.getMaxHp());
            manaLabel.setText("Mana: " + p.getCurrentMana() + "/" + p.getMaxMana());
            staminaLabel.setText("Stamina: " + p.getCurrentStamina() + "/" + p.getMaxStamina());
            levelLabel.setText("Level: " + p.getLevel());
            StringBuilder sb = new StringBuilder();
            for (com.bapppis.core.creature.creatureEnums.Stats s : com.bapppis.core.creature.creatureEnums.Stats
                    .values()) {
                if (s == com.bapppis.core.creature.creatureEnums.Stats.LUCK)
                    continue;
                sb.append(s.name()).append(": ").append(p.getStat(s)).append('\n');
            }
            statsLabel.setText(sb.toString());
            StringBuilder rs = new StringBuilder();
            for (com.bapppis.core.Resistances r : com.bapppis.core.Resistances.values()) {
                if (r == com.bapppis.core.Resistances.TRUE)
                    continue;
                rs.append(r.name()).append(": ").append(p.getResistance(r)).append('%').append('\n');
            }
            resistLabel.setText(rs.toString());
        }
        left.add(nameLabel).left().row();
        left.add(hpLabel).left().row();
        left.add(manaLabel).left().row();
        left.add(staminaLabel).left().row();
        left.add(levelLabel).left().row();
        left.add(statsLabel).left().padTop(8).row();
        left.add(resistLabel).left().padTop(8).row();

        com.kotcrab.vis.ui.widget.VisTable right = new com.kotcrab.vis.ui.widget.VisTable(true);

        // Enemy sprite
        if (enemy != null && spriteAtlas != null) {
            String enemySprite = enemy.getSprite();
            if (enemySprite != null && !enemySprite.isEmpty()) {
                com.badlogic.gdx.graphics.g2d.TextureRegion enemyReg = spriteAtlas.findRegion(enemySprite);
                if (enemyReg != null) {
                    com.badlogic.gdx.scenes.scene2d.ui.Image enemyImg = new com.badlogic.gdx.scenes.scene2d.ui.Image(
                            enemyReg);
                    right.add(enemyImg).size(48, 48).pad(8).row();
                }
            }
        }

        com.kotcrab.vis.ui.widget.VisLabel enemyName = new com.kotcrab.vis.ui.widget.VisLabel("Enemy: -");
        com.kotcrab.vis.ui.widget.VisLabel enemyHp = new com.kotcrab.vis.ui.widget.VisLabel("HP: -/-");
        com.kotcrab.vis.ui.widget.VisLabel enemyMana = new com.kotcrab.vis.ui.widget.VisLabel("Mana: -/-");
        com.kotcrab.vis.ui.widget.VisLabel enemyStamina = new com.kotcrab.vis.ui.widget.VisLabel("Stamina: -/-");
        com.kotcrab.vis.ui.widget.VisLabel enemyLevel = new com.kotcrab.vis.ui.widget.VisLabel("Level: -");
        com.kotcrab.vis.ui.widget.VisLabel enemyStats = new com.kotcrab.vis.ui.widget.VisLabel("");
        com.kotcrab.vis.ui.widget.VisLabel enemyResists = new com.kotcrab.vis.ui.widget.VisLabel("");
        if (enemy != null) {
            enemyName.setText("Enemy: " + enemy.getName());
            enemyHp.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
            enemyMana.setText("Mana: " + enemy.getCurrentMana() + "/" + enemy.getMaxMana());
            enemyStamina.setText("Stamina: " + enemy.getCurrentStamina() + "/" + enemy.getMaxStamina());
            enemyLevel.setText("Level: " + enemy.getLevel());
            StringBuilder sb = new StringBuilder();
            for (com.bapppis.core.creature.creatureEnums.Stats s : com.bapppis.core.creature.creatureEnums.Stats
                    .values()) {
                sb.append(s.name()).append(": ").append(enemy.getStat(s)).append('\n');
            }
            enemyStats.setText(sb.toString());
            StringBuilder rs = new StringBuilder();
            for (com.bapppis.core.Resistances r : com.bapppis.core.Resistances.values()) {
                rs.append(r.name()).append(": ").append(enemy.getResistance(r)).append('%').append('\n');
            }
            enemyResists.setText(rs.toString());
        }
        right.add(enemyName).right().row();
        right.add(enemyHp).right().row();
        right.add(enemyMana).right().row();
        right.add(enemyStamina).right().row();
        right.add(enemyLevel).right().row();
        right.add(enemyStats).right().padTop(8).row();
        right.add(enemyResists).right().padTop(8).row();

        com.kotcrab.vis.ui.widget.VisTable center = new com.kotcrab.vis.ui.widget.VisTable(true);
        com.kotcrab.vis.ui.widget.VisTextButton attackBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Attack");
        com.kotcrab.vis.ui.widget.VisTextButton inventoryBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Inventory");
        com.kotcrab.vis.ui.widget.VisTextButton spellsBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Spells");
        com.kotcrab.vis.ui.widget.VisTextButton useBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Use Item");
        com.kotcrab.vis.ui.widget.VisTextButton waitBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Wait");
        com.kotcrab.vis.ui.widget.VisTextButton fleeBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Flee");

        // Disable spells button if player has no spells
        if (p != null) {
            boolean hasSpells = p.getSpellReferences() != null && !p.getSpellReferences().isEmpty();
            spellsBtn.setDisabled(!hasSpells);
        }

        com.kotcrab.vis.ui.widget.VisLabel combatMsg = new com.kotcrab.vis.ui.widget.VisLabel("");
        combatMsg.setWrap(true);
        final Runnable refreshLabels = new Runnable() {
            @Override
            public void run() {
                com.bapppis.core.creature.Player pp = com.bapppis.core.game.GameState.getPlayer();
                if (pp != null) {
                    nameLabel.setText("Name: " + pp.getName());
                    hpLabel.setText("HP: " + pp.getCurrentHp() + "/" + pp.getMaxHp());
                    manaLabel.setText("Mana: " + pp.getCurrentMana() + "/" + pp.getMaxMana());
                    staminaLabel.setText("Stamina: " + pp.getCurrentStamina() + "/" + pp.getMaxStamina());
                    levelLabel.setText("Level: " + pp.getLevel());
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.creature.creatureEnums.Stats s : com.bapppis.core.creature.creatureEnums.Stats
                            .values()) {
                        if (s == com.bapppis.core.creature.creatureEnums.Stats.LUCK)
                            continue;
                        sb.append(s.name()).append(": ").append(pp.getStat(s)).append('\n');
                    }
                    statsLabel.setText(sb.toString());
                    StringBuilder rs = new StringBuilder();
                    for (com.bapppis.core.Resistances r : com.bapppis.core.Resistances.values()) {
                        if (r == com.bapppis.core.Resistances.TRUE)
                            continue;
                        rs.append(r.name()).append(": ").append(pp.getResistance(r)).append('%').append('\n');
                    }
                    resistLabel.setText(rs.toString());
                }
                if (enemy != null) {
                    enemyHp.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
                    enemyMana.setText("Mana: " + enemy.getCurrentMana() + "/" + enemy.getMaxMana());
                    enemyStamina.setText("Stamina: " + enemy.getCurrentStamina() + "/" + enemy.getMaxStamina());
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.creature.creatureEnums.Stats s : com.bapppis.core.creature.creatureEnums.Stats
                            .values()) {
                        sb.append(s.name()).append(": ").append(enemy.getStat(s)).append('\n');
                    }
                    enemyStats.setText(sb.toString());
                    StringBuilder rs2 = new StringBuilder();
                    for (com.bapppis.core.Resistances r : com.bapppis.core.Resistances.values()) {
                        rs2.append(r.name()).append(": ").append(enemy.getResistance(r)).append('%').append('\n');
                    }
                    enemyResists.setText(rs2.toString());
                }
            }
        };

        attackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    final com.bapppis.core.creature.Player pp = com.bapppis.core.game.GameState.getPlayer();
                    if (pp == null || enemy == null)
                        return;
                    attackBtn.setDisabled(true);
                    inventoryBtn.setDisabled(true);
                    spellsBtn.setDisabled(true);
                    useBtn.setDisabled(true);
                    waitBtn.setDisabled(true);
                    fleeBtn.setDisabled(true);

                    int beforeEnemyHp = enemy.getCurrentHp();
                    pp.attack(enemy);
                    int dealt = Math.max(0, beforeEnemyHp - enemy.getCurrentHp());
                    combatMsg.setText("You dealt " + dealt + " damage.");
                    refreshLabels.run();

                    if (enemy.getCurrentHp() <= 0) {
                        // Handle enemy defeat: award XP if applicable
                        if (pp instanceof com.bapppis.core.creature.Player
                                && enemy instanceof com.bapppis.core.creature.Enemy) {
                            Integer enemyXp = ((com.bapppis.core.creature.Enemy) enemy).getEnemyXp();
                            if (enemyXp != null && enemyXp > 0) {
                                ((com.bapppis.core.creature.Player) pp).addXp(enemyXp);
                            }
                        }
                        // Remove enemy from tile, create corpse, and clear its position
                        if (enemy instanceof com.bapppis.core.creature.Enemy) {
                            com.bapppis.core.creature.Enemy en = (com.bapppis.core.creature.Enemy) enemy;
                            if (en.getPosition() != null) {
                                com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState
                                        .getCurrentFloor();
                                if (floor != null) {
                                    com.bapppis.core.dungeon.Tile tile = floor.getTile(en.getPosition());
                                    if (tile != null) {
                                        tile.getOccupants().remove(enemy);
                                        // Create corpse tile with loot
                                        createCorpseTile(tile, en);
                                    }
                                }
                                // Clear the enemy's position to fully disconnect it from the map
                                en.setPosition((com.bapppis.core.dungeon.Coordinate) null);
                            }
                        }
                        // Show victory dialog and exit combat when dismissed
                        com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog("Victory");
                        dlg.text(enemy.getName() + " defeated!");
                        com.kotcrab.vis.ui.widget.VisTextButton okBtn = new com.kotcrab.vis.ui.widget.VisTextButton(
                                "OK");
                        okBtn.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                dlg.hide();
                                com.bapppis.core.game.GameState.setInCombat(false);
                                showFloorView();
                            }
                        });
                        dlg.button(okBtn);
                        dlg.show(stage);
                        return;
                    }

                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int beforePlayerHp = pp.getCurrentHp();
                                        enemy.attack(pp);
                                        int taken = Math.max(0, beforePlayerHp - pp.getCurrentHp());
                                        combatMsg.setText("Enemy dealt " + taken + " damage.");
                                        refreshLabels.run();
                                        if (pp.getCurrentHp() <= 0) {
                                            com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog(
                                                    "Defeat");
                                            dlg.text(pp.getName() + " has been defeated!");
                                            dlg.button("OK");
                                            dlg.show(stage);
                                        }
                                    } catch (Exception e) {
                                        Gdx.app.error("RecallDungeon", "Error during enemy retaliation", e);
                                    } finally {
                                        attackBtn.setDisabled(false);
                                        useBtn.setDisabled(false);
                                        waitBtn.setDisabled(false);
                                        fleeBtn.setDisabled(false);
                                    }
                                }
                            });
                        }
                    }, 0.9f);
                } catch (Exception e) {
                    Gdx.app.error("RecallDungeon", "Error during attack action", e);
                    com.bapppis.core.creature.Player p2 = com.bapppis.core.game.GameState.getPlayer();
                    attackBtn.setDisabled(false);
                    inventoryBtn.setDisabled(false);
                    boolean hasSpells2 = p2 != null && p2.getSpellReferences() != null
                            && !p2.getSpellReferences().isEmpty();
                    spellsBtn.setDisabled(!hasSpells2);
                    useBtn.setDisabled(false);
                    waitBtn.setDisabled(false);
                    fleeBtn.setDisabled(false);
                }
            }
        });

        waitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    final com.bapppis.core.creature.Player pp = com.bapppis.core.game.GameState.getPlayer();
                    if (pp == null || enemy == null)
                        return;
                    attackBtn.setDisabled(true);
                    inventoryBtn.setDisabled(true);
                    spellsBtn.setDisabled(true);
                    useBtn.setDisabled(true);
                    waitBtn.setDisabled(true);
                    fleeBtn.setDisabled(true);
                    combatMsg.setText("Waiting...");
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int beforePlayerHp = pp.getCurrentHp();
                                        enemy.attack(pp);
                                        int taken = Math.max(0, beforePlayerHp - pp.getCurrentHp());
                                        combatMsg.setText("Enemy dealt " + taken + " damage.");
                                        refreshLabels.run();
                                        if (pp.getCurrentHp() <= 0) {
                                            com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog(
                                                    "Defeat");
                                            dlg.text(pp.getName() + " has been defeated!");
                                            dlg.button("OK");
                                            dlg.show(stage);
                                        }
                                    } catch (Exception e) {
                                        Gdx.app.error("RecallDungeon", "Error during wait enemy attack", e);
                                    } finally {
                                        attackBtn.setDisabled(false);
                                        inventoryBtn.setDisabled(false);
                                        boolean hasSpells2 = pp.getSpellReferences() != null
                                                && !pp.getSpellReferences().isEmpty();
                                        spellsBtn.setDisabled(!hasSpells2);
                                        useBtn.setDisabled(false);
                                        waitBtn.setDisabled(false);
                                        fleeBtn.setDisabled(false);
                                    }
                                }
                            });
                        }
                    }, 0.9f);
                } catch (Exception e) {
                    Gdx.app.error("RecallDungeon", "Error during wait action", e);
                }
            }
        });

        fleeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                com.bapppis.core.game.GameState.setInCombat(false);
                com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog("Flee");
                dlg.text("You fled from combat.");
                dlg.button("OK", true);
                dlg.show(stage).addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        showFloorView();
                    }
                });
            }
        });

        inventoryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                com.bapppis.core.creature.Player pp = com.bapppis.core.game.GameState.getPlayer();
                if (pp == null)
                    return;

                com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog("Inventory");
                com.kotcrab.vis.ui.widget.VisTable invTable = new com.kotcrab.vis.ui.widget.VisTable(true);

                // Show equipped items
                invTable.add(new com.kotcrab.vis.ui.widget.VisLabel("Equipped Items:")).left().colspan(2).row();
                for (com.bapppis.core.item.itemEnums.EquipmentSlot slot : com.bapppis.core.item.itemEnums.EquipmentSlot
                        .values()) {
                    com.bapppis.core.item.Item eq = pp.getEquipped(slot);
                    String text = slot.name() + ": " + (eq != null ? eq.getName() : "Empty");
                    invTable.add(new com.kotcrab.vis.ui.widget.VisLabel(text)).left().colspan(2).row();
                }

                invTable.add().padTop(12).row();
                invTable.add(new com.kotcrab.vis.ui.widget.VisLabel("Inventory (click Equip to swap):")).left()
                        .colspan(2).row();

                // Show inventory items by category
                java.util.function.BiConsumer<String, java.util.List<com.bapppis.core.item.Item>> addInvGroup = (label,
                        items) -> {
                    if (items == null || items.isEmpty())
                        return;
                    invTable.add(new com.kotcrab.vis.ui.widget.VisLabel(label)).left().padTop(6).colspan(2).row();
                    for (com.bapppis.core.item.Item item : items) {
                        invTable.add(new com.kotcrab.vis.ui.widget.VisLabel(item.getName())).left();
                        com.kotcrab.vis.ui.widget.VisTextButton equipBtn = new com.kotcrab.vis.ui.widget.VisTextButton(
                                "Equip");
                        equipBtn.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                try {
                                    if (item instanceof com.bapppis.core.item.Equipment) {
                                        pp.equipItem((com.bapppis.core.item.Equipment) item);
                                        refreshLabels.run();
                                        dlg.hide();
                                        com.kotcrab.vis.ui.widget.VisDialog msg = new com.kotcrab.vis.ui.widget.VisDialog(
                                                "Success");
                                        msg.text("Equipped " + item.getName());
                                        msg.button("OK");
                                        msg.show(stage);
                                    }
                                } catch (Exception e) {
                                    Gdx.app.error("RecallDungeon", "Error equipping in combat", e);
                                }
                            }
                        });
                        invTable.add(equipBtn).padLeft(4).right().row();
                    }
                };

                com.bapppis.core.creature.Inventory inv = pp.getInventory();
                addInvGroup.accept("Weapons", inv.getWeapons());
                addInvGroup.accept("Offhands", inv.getOffhands());
                addInvGroup.accept("Helmets", inv.getHelmets());
                addInvGroup.accept("Armors", inv.getArmors());
                addInvGroup.accept("Legwear", inv.getLegwear());

                com.kotcrab.vis.ui.widget.VisScrollPane scrollPane = new com.kotcrab.vis.ui.widget.VisScrollPane(
                        invTable);
                scrollPane.setFadeScrollBars(false);
                dlg.getContentTable().add(scrollPane).width(400).height(400);
                dlg.button("Close");
                dlg.show(stage);
            }
        });

        spellsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                com.bapppis.core.creature.Player pp = com.bapppis.core.game.GameState.getPlayer();
                if (pp == null)
                    return;

                java.util.List<com.bapppis.core.spell.SpellReference> spells = pp.getSpellReferences();
                com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog("Cast Spell");

                if (spells == null || spells.isEmpty()) {
                    dlg.text("No spells available.");
                    dlg.button("OK");
                    dlg.show(stage);
                    return;
                }

                com.kotcrab.vis.ui.widget.VisTable spellTable = new com.kotcrab.vis.ui.widget.VisTable(true);
                for (com.bapppis.core.spell.SpellReference spellRef : spells) {
                    com.bapppis.core.spell.Spell spell = com.bapppis.core.spell.SpellLoader
                            .getSpellByName(spellRef.getName());
                    if (spell == null)
                        continue;

                    com.kotcrab.vis.ui.widget.VisLabel lbl = new com.kotcrab.vis.ui.widget.VisLabel(
                            spell.getName() + " (Mana: " + spell.getManaCost() + ")");
                    com.kotcrab.vis.ui.widget.VisTextButton castBtn = new com.kotcrab.vis.ui.widget.VisTextButton(
                            "Cast");

                    // Disable if not enough mana
                    if (pp.getCurrentMana() < spell.getManaCost()) {
                        castBtn.setDisabled(true);
                    }

                    castBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            try {
                                attackBtn.setDisabled(true);
                                inventoryBtn.setDisabled(true);
                                spellsBtn.setDisabled(true);
                                useBtn.setDisabled(true);
                                waitBtn.setDisabled(true);
                                fleeBtn.setDisabled(true);

                                com.bapppis.core.spell.SpellEngine.castSpell(pp, spell, enemy);
                                refreshLabels.run();
                                dlg.hide();
                                combatMsg.setText("You cast " + spell.getName() + "!");

                                if (enemy.getCurrentHp() <= 0) {
                                    // Handle enemy defeat: award XP if applicable
                                    if (pp instanceof com.bapppis.core.creature.Player
                                            && enemy instanceof com.bapppis.core.creature.Enemy) {
                                        Integer enemyXp = ((com.bapppis.core.creature.Enemy) enemy).getEnemyXp();
                                        if (enemyXp != null && enemyXp > 0) {
                                            ((com.bapppis.core.creature.Player) pp).addXp(enemyXp);
                                        }
                                    }
                                    // Remove enemy from tile and clear its position
                                    if (enemy instanceof com.bapppis.core.creature.Enemy) {
                                        com.bapppis.core.creature.Enemy en = (com.bapppis.core.creature.Enemy) enemy;
                                        if (en.getPosition() != null) {
                                            com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState
                                                    .getCurrentFloor();
                                            if (floor != null) {
                                                com.bapppis.core.dungeon.Tile tile = floor.getTile(en.getPosition());
                                                if (tile != null) {
                                                    tile.getOccupants().remove(enemy);
                                                }
                                            }
                                            // Clear the enemy's position to fully disconnect it from the map
                                            en.setPosition((com.bapppis.core.dungeon.Coordinate) null);
                                        }
                                    }
                                    // Show victory dialog and exit combat when dismissed
                                    com.kotcrab.vis.ui.widget.VisDialog victoryDlg = new com.kotcrab.vis.ui.widget.VisDialog(
                                            "Victory");
                                    victoryDlg.text(enemy.getName() + " defeated!");
                                    com.kotcrab.vis.ui.widget.VisTextButton spellOkBtn = new com.kotcrab.vis.ui.widget.VisTextButton(
                                            "OK");
                                    spellOkBtn.addListener(new ClickListener() {
                                        @Override
                                        public void clicked(InputEvent event, float x, float y) {
                                            victoryDlg.hide();
                                            com.bapppis.core.game.GameState.setInCombat(false);
                                            showFloorView();
                                        }
                                    });
                                    victoryDlg.button(spellOkBtn);
                                    victoryDlg.show(stage);
                                    return;
                                }

                                // Enemy counter-attack
                                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                    @Override
                                    public void run() {
                                        Gdx.app.postRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    int beforePlayerHp = pp.getCurrentHp();
                                                    enemy.attack(pp);
                                                    int taken = Math.max(0, beforePlayerHp - pp.getCurrentHp());
                                                    combatMsg.setText("Enemy dealt " + taken + " damage.");
                                                    refreshLabels.run();
                                                    if (pp.getCurrentHp() <= 0) {
                                                        com.kotcrab.vis.ui.widget.VisDialog defeatDlg = new com.kotcrab.vis.ui.widget.VisDialog(
                                                                "Defeat");
                                                        defeatDlg.text(pp.getName() + " has been defeated!");
                                                        defeatDlg.button("OK");
                                                        defeatDlg.show(stage);
                                                    }
                                                } catch (Exception e) {
                                                    Gdx.app.error("RecallDungeon",
                                                            "Error during spell enemy retaliation", e);
                                                } finally {
                                                    attackBtn.setDisabled(false);
                                                    inventoryBtn.setDisabled(false);
                                                    boolean hasSpells2 = pp.getSpellReferences() != null
                                                            && !pp.getSpellReferences().isEmpty();
                                                    spellsBtn.setDisabled(!hasSpells2);
                                                    useBtn.setDisabled(false);
                                                    waitBtn.setDisabled(false);
                                                    fleeBtn.setDisabled(false);
                                                }
                                            }
                                        });
                                    }
                                }, 0.9f);
                            } catch (Exception e) {
                                Gdx.app.error("RecallDungeon", "Error casting spell in combat", e);
                                attackBtn.setDisabled(false);
                                inventoryBtn.setDisabled(false);
                                boolean hasSpells2 = pp.getSpellReferences() != null
                                        && !pp.getSpellReferences().isEmpty();
                                spellsBtn.setDisabled(!hasSpells2);
                                useBtn.setDisabled(false);
                                waitBtn.setDisabled(false);
                                fleeBtn.setDisabled(false);
                            }
                        }
                    });
                    spellTable.add(lbl).left().padRight(8);
                    spellTable.add(castBtn).row();
                }

                dlg.getContentTable().add(spellTable);
                dlg.button("Close");
                dlg.show(stage);
            }
        });

        useBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                com.bapppis.core.creature.Player pp = com.bapppis.core.game.GameState.getPlayer();
                if (pp == null)
                    return;
                java.util.List<com.bapppis.core.item.Item> cons = pp.getInventory().getConsumables();
                com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog("Use Consumable");
                if (cons == null || cons.isEmpty()) {
                    dlg.text("No consumables available.");
                    dlg.button("OK");
                    dlg.show(stage);
                    return;
                }
                com.kotcrab.vis.ui.widget.VisTable listTable = new com.kotcrab.vis.ui.widget.VisTable(true);
                for (com.bapppis.core.item.Item item : cons) {
                    com.kotcrab.vis.ui.widget.VisLabel lbl = new com.kotcrab.vis.ui.widget.VisLabel(item.getName());
                    com.kotcrab.vis.ui.widget.VisTextButton btn = new com.kotcrab.vis.ui.widget.VisTextButton("Use");
                    btn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            try {
                                item.onApply(pp);
                                pp.getInventory().removeItem(item);
                                refreshLabels.run();
                                dlg.hide();
                                if (enemy != null && pp.getCurrentHp() > 0) {
                                    enemy.attack(pp);
                                    refreshLabels.run();
                                }
                            } catch (Exception e) {
                                Gdx.app.error("RecallDungeon", "Error using consumable in combat", e);
                            }
                        }
                    });
                    listTable.add(lbl).left().padRight(8);
                    listTable.add(btn).row();
                }
                dlg.getContentTable().add(listTable);
                dlg.button("Close");
                dlg.show(stage);
            }
        });

        center.add(combatMsg).width(300).padBottom(6).row();
        center.add(attackBtn).pad(6).row();
        center.add(inventoryBtn).pad(6).row();
        center.add(spellsBtn).pad(6).row();
        center.add(useBtn).pad(6).row();
        center.add(waitBtn).pad(6).row();
        center.add(fleeBtn).pad(6).row();

        com.kotcrab.vis.ui.widget.VisScrollPane leftCombatScroll = new com.kotcrab.vis.ui.widget.VisScrollPane(left);
        leftCombatScroll.setFadeScrollBars(false);
        table.add(leftCombatScroll).width(320).fillY().top().pad(8);
        table.add(center).expand().center().pad(8);
        com.kotcrab.vis.ui.widget.VisScrollPane rightCombatScroll = new com.kotcrab.vis.ui.widget.VisScrollPane(right);
        rightCombatScroll.setFadeScrollBars(false);
        table.add(rightCombatScroll).width(320).fillY().top().pad(8);
    }

    /**
     * Creates a corpse tile with loot when an enemy is defeated.
     * Spawns items from the enemy's loot pool and places them on the tile.
     */
    private void createCorpseTile(com.bapppis.core.dungeon.Tile tile, com.bapppis.core.creature.Enemy enemy) {
        try {
            Gdx.app.log("RecallDungeon",
                    "createCorpseTile called for enemy: " + enemy.getName() + " at " + tile.getCoordinate());

            // Get the death sprite from the enemy (should be "lootable_corpse")
            String deathSprite = enemy.getDeathSprite();
            Gdx.app.log("RecallDungeon", "Death sprite: " + deathSprite);
            if (deathSprite == null || deathSprite.isEmpty()) {
                Gdx.app.log("RecallDungeon", "No death sprite, skipping corpse creation");
                return; // No corpse sprite defined, skip corpse creation
            }

            // Load the lootable corpse tile type
            com.bapppis.core.dungeon.TileType corpseTileType = com.bapppis.core.dungeon.TileTypeLoader
                    .getTileTypeByName("lootableCorpse");
            if (corpseTileType == null) {
                Gdx.app.error("RecallDungeon", "Failed to load lootableCorpse tile type");
                return;
            }

            Gdx.app.log("RecallDungeon", "Loaded corpse tile type successfully");

            // Get the loot pool and sample it
            String lootPoolId = enemy.getLootPool();
            Gdx.app.log("RecallDungeon", "Enemy loot pool ID: " + lootPoolId);
            com.bapppis.core.loot.LootPool lootPool = null;
            if (lootPoolId != null && !lootPoolId.isEmpty()) {
                // Try to load pool by ID first, then by name
                lootPool = com.bapppis.core.loot.LootPoolLoader.getLootPoolById(lootPoolId);
                if (lootPool == null) {
                    lootPool = com.bapppis.core.loot.LootPoolLoader.getLootPoolByName(lootPoolId);
                }
                if (lootPool != null) {
                    Gdx.app.log("RecallDungeon", "Loaded loot pool: " + lootPool.name);
                } else {
                    Gdx.app.log("RecallDungeon", "Failed to load loot pool: " + lootPoolId);
                }
            } else {
                Gdx.app.log("RecallDungeon", "Enemy has no loot pool");
            }

            // Create a new corpse tile at the same coordinate
            com.bapppis.core.dungeon.Tile corpseTile = new com.bapppis.core.dungeon.Tile(
                    tile.getCoordinate(), corpseTileType);

            // Sample and spawn loot if we have a pool
            if (lootPool != null) {
                // Create a temporary LootManager to sample the pool
                com.bapppis.core.loot.LootManager tempManager = new com.bapppis.core.loot.LootManager();
                tempManager.loadDefaults();
                tempManager.registerPool(lootPool);

                // Sample using the pool's actual ID, not the reference name
                String poolIdToSample = lootPool.id;
                Gdx.app.log("RecallDungeon", "Sampling loot pool with ID: " + poolIdToSample);
                java.util.List<com.bapppis.core.loot.LootManager.Spawn> spawns = tempManager
                        .samplePool(poolIdToSample);
                Gdx.app.log("RecallDungeon", "Got " + (spawns != null ? spawns.size() : 0) + " spawns");
                if (spawns != null && !spawns.isEmpty()) {
                    // Spawn items from the loot pool
                    for (com.bapppis.core.loot.LootManager.Spawn spawn : spawns) {
                        Gdx.app.log("RecallDungeon", "Spawn type: " + spawn.type + ", id: " + spawn.id);
                        if ("item".equalsIgnoreCase(spawn.type)) {
                            com.bapppis.core.item.Item item = null;
                            if (spawn.id != null) {
                                // Try as integer ID first
                                try {
                                    int itemId = Integer.parseInt(spawn.id);
                                    item = com.bapppis.core.item.ItemLoader.getItemById(itemId);
                                } catch (NumberFormatException e) {
                                    // Not an int, try as name
                                    item = com.bapppis.core.item.ItemLoader.getItemByName(spawn.id);
                                }
                            }
                            if (item != null) {
                                corpseTile.getItems().add(item);
                                Gdx.app.log("RecallDungeon", "Added item to corpse: " + item.getName());
                            } else {
                                Gdx.app.log("RecallDungeon", "Failed to load item: " + spawn.id);
                            }
                        }
                    }
                }
                // Store the loot pool ID on the tile for reference
                corpseTile.setLootPoolId(lootPoolId);
            }

            // Copy over any navigation references from the old tile
            corpseTile.setLeft(tile.getLeft());
            corpseTile.setRight(tile.getRight());
            corpseTile.setUp(tile.getUp());
            corpseTile.setDown(tile.getDown());
            corpseTile.setDiscovered(tile.isDiscovered());

            // Replace the tile in the floor's tile map
            com.bapppis.core.dungeon.Floor floor = com.bapppis.core.game.GameState.getCurrentFloor();
            if (floor != null) {
                floor.addTile(tile.getCoordinate(), corpseTile);
                Gdx.app.log("RecallDungeon", "Replaced tile at " + tile.getCoordinate() + " with corpse tile");
                Gdx.app.log("RecallDungeon", "Corpse tile has " + corpseTile.getItems().size() + " items");

                // Update navigation references from neighboring tiles
                if (corpseTile.getLeft() != null) {
                    corpseTile.getLeft().setRight(corpseTile);
                }
                if (corpseTile.getRight() != null) {
                    corpseTile.getRight().setLeft(corpseTile);
                }
                if (corpseTile.getUp() != null) {
                    corpseTile.getUp().setDown(corpseTile);
                }
                if (corpseTile.getDown() != null) {
                    corpseTile.getDown().setUp(corpseTile);
                }
            }

        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error creating corpse tile", e);
        }
    }

    @Override
    public void dispose() {
        try {
            Object userObj = stage != null ? stage.getRoot().getUserObject() : null;
            if (userObj instanceof Game) {
                ((Game) userObj).shutdown();
            }
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error shutting down game", e);
        }

        if (batch != null)
            try {
                batch.dispose();
            } catch (Exception ignored) {
            }
        if (font != null)
            try {
                font.dispose();
            } catch (Exception ignored) {
            }
        if (mapFont != null)
            try {
                mapFont.dispose();
            } catch (Exception ignored) {
            }
        if (spriteAtlas != null)
            try {
                spriteAtlas.dispose();
            } catch (Exception ignored) {
            }
        // Note: spriteAtlas is shared with mapActor, so don't dispose it again via
        // reflection
        // The charTextureRegions map contains regions from spriteAtlas, not separate
        // textures to dispose
        if (stage != null)
            try {
                stage.dispose();
            } catch (Exception ignored) {
            }
        if (VisUI.isLoaded())
            VisUI.dispose();
    }

}

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
import com.bapppis.core.AllLoaders;

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
        // Load all assets once at startup so selection UI has data
        try {
            AllLoaders.loadAll();
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error loading assets at startup", e);
        }

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
    java.util.List<Player> tmpPlayers = CreatureLoader.getAllPlayers();
    final java.util.List<Player> players = (tmpPlayers == null)
        ? java.util.Collections.emptyList()
        : tmpPlayers;
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
                // Ensure global game state is set immediately so UI reflects selection
                com.bapppis.core.game.Game.selectPlayer(selected);
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
                // Spawn a goblin for quick combat testing (id 6400)
                /* try {
                    com.bapppis.core.creature.Creature goblin = com.bapppis.core.creature.CreatureLoader
                            .getCreatureById(6400);
                    if (goblin != null) {
                        showCombatView(goblin);
                    }
                } catch (Exception e) {
                    Gdx.app.error("RecallDungeon", "Error spawning test goblin", e);
                } */
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

        // Attempt to load a sprite atlas from packaged assets. If present, MapActor
        // will draw sprites for mapped characters, otherwise fall back to text.
        com.badlogic.gdx.graphics.g2d.TextureAtlas atlas = null;
        try {
            if (Gdx.files.internal("assets/sprites.atlas").exists()) {
                atlas = new com.badlogic.gdx.graphics.g2d.TextureAtlas(Gdx.files.internal("assets/sprites.atlas"));
                Gdx.app.log("RecallDungeon", "Loaded assets/sprites.atlas");
            } else if (Gdx.files.internal("sprites.atlas").exists()) {
                atlas = new com.badlogic.gdx.graphics.g2d.TextureAtlas(Gdx.files.internal("sprites.atlas"));
                Gdx.app.log("RecallDungeon", "Loaded sprites.atlas from project root");
            }
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error loading sprites atlas", e);
            atlas = null;
        }

        // load mapping from assets/tiles.json if present, otherwise fall back to defaults
        java.util.Map<Character, String> charToRegion = new java.util.HashMap<>();
        try {
            if (Gdx.files.internal("assets/tiles.json").exists()) {
                String json = Gdx.files.internal("assets/tiles.json").readString();
                com.badlogic.gdx.utils.JsonReader jr = new com.badlogic.gdx.utils.JsonReader();
                com.badlogic.gdx.utils.JsonValue root = jr.parse(json);
                com.badlogic.gdx.utils.JsonValue mappings = root.get("mappings");
                if (mappings != null) {
                    for (com.badlogic.gdx.utils.JsonValue entry = mappings.child; entry != null; entry = entry.next) {
                        String key = entry.name();
                        if (key != null && key.length() > 0) {
                            char ch = key.charAt(0);
                            String region = entry.asString();
                            charToRegion.put(ch, region);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("RecallDungeon", "Error parsing assets/tiles.json", e);
        }
        if (charToRegion.isEmpty()) {
            charToRegion.put('#', "wall");
            charToRegion.put('.', "floor");
            charToRegion.put('^', "stairs_up");
            charToRegion.put('v', "stairs_down");
            // default player sprite key: use a generic player_default region if available
            charToRegion.put('P', "player_default");
        }

        // If atlas missing, try to load individual PNGs named after region (e.g. floor.png)
        java.util.Map<Character, com.badlogic.gdx.graphics.g2d.TextureRegion> charTextureRegions = new java.util.HashMap<>();
        java.util.List<com.badlogic.gdx.graphics.Texture> createdTextures = new java.util.ArrayList<>();
        if (atlas == null) {
            for (java.util.Map.Entry<Character, String> e : charToRegion.entrySet()) {
                String fileName = e.getValue() + ".png"; // e.g. floor.png
                try {
                    if (Gdx.files.internal(fileName).exists()) {
                        com.badlogic.gdx.graphics.Texture t = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal(fileName));
                        createdTextures.add(t);
                        com.badlogic.gdx.graphics.g2d.TextureRegion tr = new com.badlogic.gdx.graphics.g2d.TextureRegion(t);
                        charTextureRegions.put(e.getKey(), tr);
                        Gdx.app.log("RecallDungeon", "Loaded texture for " + e.getKey() + " -> " + fileName);
                    } else if (Gdx.files.internal("assets/" + fileName).exists()) {
                        com.badlogic.gdx.graphics.Texture t = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("assets/" + fileName));
                        createdTextures.add(t);
                        com.badlogic.gdx.graphics.g2d.TextureRegion tr = new com.badlogic.gdx.graphics.g2d.TextureRegion(t);
                        charTextureRegions.put(e.getKey(), tr);
                        Gdx.app.log("RecallDungeon", "Loaded texture for " + e.getKey() + " -> assets/" + fileName);
                    }
                } catch (Exception ex) {
                    Gdx.app.error("RecallDungeon", "Error loading texture " + fileName, ex);
                }
            }
        }

        // --- Runtime override: prefer player-specific sprite for 'P' ---
        try {
            com.bapppis.core.creature.player.Player current = com.bapppis.core.game.GameState.getPlayer();
            if (current != null) {
                String spriteKey = current.getSprite();
                if (spriteKey != null && !spriteKey.trim().isEmpty()) {
                    // if atlas contains region, use it
                    if (atlas != null && atlas.findRegion(spriteKey) != null) {
                        charToRegion.put('P', spriteKey);
                        Gdx.app.log("RecallDungeon", "Player sprite mapping from creature: 'P' -> " + spriteKey + " (atlas)");
                    } else {
                        // try to load PNG from sprite_pngs or assets/sprite_pngs
                        String pngName = spriteKey + ".png";
                        boolean loaded = false;
                        try {
                            if (Gdx.files.internal("sprite_pngs/" + pngName).exists()) {
                                com.badlogic.gdx.graphics.Texture t = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("sprite_pngs/" + pngName));
                                createdTextures.add(t);
                                com.badlogic.gdx.graphics.g2d.TextureRegion tr = new com.badlogic.gdx.graphics.g2d.TextureRegion(t);
                                charTextureRegions.put('P', tr);
                                charToRegion.put('P', spriteKey);
                                loaded = true;
                                Gdx.app.log("RecallDungeon", "Loaded player PNG sprite for P -> sprite_pngs/" + pngName);
                            } else if (Gdx.files.internal("assets/sprite_pngs/" + pngName).exists()) {
                                com.badlogic.gdx.graphics.Texture t = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("assets/sprite_pngs/" + pngName));
                                createdTextures.add(t);
                                com.badlogic.gdx.graphics.g2d.TextureRegion tr = new com.badlogic.gdx.graphics.g2d.TextureRegion(t);
                                charTextureRegions.put('P', tr);
                                charToRegion.put('P', spriteKey);
                                loaded = true;
                                Gdx.app.log("RecallDungeon", "Loaded player PNG sprite for P -> assets/sprite_pngs/" + pngName);
                            }
                        } catch (Exception ex) {
                            Gdx.app.error("RecallDungeon", "Error loading player PNG sprite " + pngName, ex);
                        }
                        if (!loaded) {
                            // fallback: attempt to use atlas region if present, otherwise revert to 'player_default'
                            if (atlas != null && atlas.findRegion(spriteKey) != null) {
                                charToRegion.put('P', spriteKey);
                                Gdx.app.log("RecallDungeon", "Set player sprite mapping to 'P' -> " + spriteKey + " (atlas only)");
                            } else {
                                charToRegion.put('P', "player_default");
                                Gdx.app.log("RecallDungeon", "Player sprite unavailable, falling back to 'player_default' for 'P'");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Gdx.app.error("RecallDungeon", "Error assigning player sprite mapping", ex);
        }
        // --- end runtime override ---

        mapActor = new com.bapppis.core.gfx.MapActor(chosen, atlas, charToRegion, charTextureRegions);
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

        // Make the left sidebar scrollable and add side-by-side with the map container
        com.kotcrab.vis.ui.widget.VisScrollPane leftScroll = new com.kotcrab.vis.ui.widget.VisScrollPane(left);
        leftScroll.setFadeScrollBars(false);
        table.add(leftScroll).width(300).fillY().top().pad(8);
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

    public void showCombatView(com.bapppis.core.creature.Creature enemy) {
        stage.clear();
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Left: player info (reuse refreshMapDisplay values)
        com.kotcrab.vis.ui.widget.VisTable left = new com.kotcrab.vis.ui.widget.VisTable(true);
        com.bapppis.core.creature.player.Player p = com.bapppis.core.game.GameState.getPlayer();
        nameLabel = new com.kotcrab.vis.ui.widget.VisLabel("Name: -");
        hpLabel = new com.kotcrab.vis.ui.widget.VisLabel("HP: -/-");
        levelLabel = new com.kotcrab.vis.ui.widget.VisLabel("Level: -");
        statsLabel = new com.kotcrab.vis.ui.widget.VisLabel("Stats: -");
        resistLabel = new com.kotcrab.vis.ui.widget.VisLabel("Resists: -");
        if (p != null) {
            nameLabel.setText("Name: " + p.getName());
            hpLabel.setText("HP: " + p.getCurrentHp() + "/" + p.getMaxHp());
            levelLabel.setText("Level: " + p.getLevel());
            StringBuilder sb = new StringBuilder();
            for (com.bapppis.core.creature.Creature.Stats s : com.bapppis.core.creature.Creature.Stats.values()) {
                if (s == com.bapppis.core.creature.Creature.Stats.LUCK)
                    continue;
                sb.append(s.name()).append(": ").append(p.getStat(s)).append('\n');
            }
            statsLabel.setText(sb.toString());
            StringBuilder rs = new StringBuilder();
            for (com.bapppis.core.creature.Creature.Resistances r : com.bapppis.core.creature.Creature.Resistances
                    .values()) {
                if (r == com.bapppis.core.creature.Creature.Resistances.TRUE)
                    continue;
                rs.append(r.name()).append(": ").append(p.getResistance(r)).append('%').append('\n');
            }
            resistLabel.setText(rs.toString());
        }
        left.add(nameLabel).left().row();
        left.add(hpLabel).left().row();
        left.add(levelLabel).left().row();
        left.add(statsLabel).left().padTop(8).row();
        left.add(resistLabel).left().padTop(8).row();

        // Right: enemy info
        com.kotcrab.vis.ui.widget.VisTable right = new com.kotcrab.vis.ui.widget.VisTable(true);
        com.kotcrab.vis.ui.widget.VisLabel enemyName = new com.kotcrab.vis.ui.widget.VisLabel("Enemy: -");
        com.kotcrab.vis.ui.widget.VisLabel enemyHp = new com.kotcrab.vis.ui.widget.VisLabel("HP: -/-");
        com.kotcrab.vis.ui.widget.VisLabel enemyLevel = new com.kotcrab.vis.ui.widget.VisLabel("Level: -");
        com.kotcrab.vis.ui.widget.VisLabel enemyStats = new com.kotcrab.vis.ui.widget.VisLabel("");
        com.kotcrab.vis.ui.widget.VisLabel enemyResists = new com.kotcrab.vis.ui.widget.VisLabel("");
        if (enemy != null) {
            enemyName.setText("Enemy: " + enemy.getName());
            enemyHp.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
            enemyLevel.setText("Level: " + enemy.getLevel());
            StringBuilder sb = new StringBuilder();
            for (com.bapppis.core.creature.Creature.Stats s : com.bapppis.core.creature.Creature.Stats.values()) {
                sb.append(s.name()).append(": ").append(enemy.getStat(s)).append('\n');
            }
            enemyStats.setText(sb.toString());
            StringBuilder rs = new StringBuilder();
            for (com.bapppis.core.creature.Creature.Resistances r : com.bapppis.core.creature.Creature.Resistances
                    .values()) {
                rs.append(r.name()).append(": ").append(enemy.getResistance(r)).append('%').append('\n');
            }
            enemyResists.setText(rs.toString());
        }
        right.add(enemyName).right().row();
        right.add(enemyHp).right().row();
        right.add(enemyLevel).right().row();
        right.add(enemyStats).right().padTop(8).row();
        right.add(enemyResists).right().padTop(8).row();

        // Center: combat controls (Attack, Use, Wait, Flee)
        com.kotcrab.vis.ui.widget.VisTable center = new com.kotcrab.vis.ui.widget.VisTable(true);
        com.kotcrab.vis.ui.widget.VisTextButton attackBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Attack");
        com.kotcrab.vis.ui.widget.VisTextButton useBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Use");
        com.kotcrab.vis.ui.widget.VisTextButton waitBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Wait");
        com.kotcrab.vis.ui.widget.VisTextButton fleeBtn = new com.kotcrab.vis.ui.widget.VisTextButton("Flee");

        // combat message shown above controls
        com.kotcrab.vis.ui.widget.VisLabel combatMsg = new com.kotcrab.vis.ui.widget.VisLabel("");
        combatMsg.setWrap(true);
        // helper to refresh labels after actions
        final Runnable refreshLabels = new Runnable() {
            @Override
            public void run() {
                com.bapppis.core.creature.player.Player pp = com.bapppis.core.game.GameState.getPlayer();
                if (pp != null) {
                    nameLabel.setText("Name: " + pp.getName());
                    hpLabel.setText("HP: " + pp.getCurrentHp() + "/" + pp.getMaxHp());
                    levelLabel.setText("Level: " + pp.getLevel());
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.creature.Creature.Stats s : com.bapppis.core.creature.Creature.Stats
                            .values()) {
                        if (s == com.bapppis.core.creature.Creature.Stats.LUCK)
                            continue;
                        sb.append(s.name()).append(": ").append(pp.getStat(s)).append('\n');
                    }
                    statsLabel.setText(sb.toString());
                    StringBuilder rs = new StringBuilder();
                    for (com.bapppis.core.creature.Creature.Resistances r : com.bapppis.core.creature.Creature.Resistances
                            .values()) {
                        if (r == com.bapppis.core.creature.Creature.Resistances.TRUE)
                            continue;
                        rs.append(r.name()).append(": ").append(pp.getResistance(r)).append('%').append('\n');
                    }
                    resistLabel.setText(rs.toString());
                }
                if (enemy != null) {
                    enemyHp.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
                    StringBuilder sb = new StringBuilder();
                    for (com.bapppis.core.creature.Creature.Stats s : com.bapppis.core.creature.Creature.Stats
                            .values()) {
                        sb.append(s.name()).append(": ").append(enemy.getStat(s)).append('\n');
                    }
                    enemyStats.setText(sb.toString());
                    StringBuilder rs2 = new StringBuilder();
                    for (com.bapppis.core.creature.Creature.Resistances r : com.bapppis.core.creature.Creature.Resistances
                            .values()) {
                        rs2.append(r.name()).append(": ").append(enemy.getResistance(r)).append('%').append('\n');
                    }
                    enemyResists.setText(rs2.toString());
                }
            }
        };

        // Attack action: player attacks enemy, show damage, pause, then enemy
        // retaliates
        attackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    final com.bapppis.core.creature.player.Player pp = com.bapppis.core.game.GameState.getPlayer();
                    if (pp == null || enemy == null)
                        return;
                    // disable buttons while sequence runs
                    attackBtn.setDisabled(true);
                    useBtn.setDisabled(true);
                    waitBtn.setDisabled(true);
                    fleeBtn.setDisabled(true);

                    int beforeEnemyHp = enemy.getCurrentHp();
                    pp.attack(enemy);
                    int dealt = Math.max(0, beforeEnemyHp - enemy.getCurrentHp());
                    combatMsg.setText("You dealt " + dealt + " damage.");
                    refreshLabels.run();

                    if (enemy.getCurrentHp() <= 0) {
                        com.kotcrab.vis.ui.widget.VisDialog dlg = new com.kotcrab.vis.ui.widget.VisDialog("Victory");
                        dlg.text(enemy.getName() + " defeated!");
                        dlg.button("OK");
                        dlg.show(stage);
                        // leave controls disabled or re-enable to inspect
                        attackBtn.setDisabled(false);
                        useBtn.setDisabled(false);
                        waitBtn.setDisabled(false);
                        fleeBtn.setDisabled(false);
                        return;
                    }

                    // schedule enemy retaliation after a short pause
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
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
                                // re-enable controls for next action
                                attackBtn.setDisabled(false);
                                useBtn.setDisabled(false);
                                waitBtn.setDisabled(false);
                                fleeBtn.setDisabled(false);
                            }
                        }
                    }, 0.9f);
                } catch (Exception e) {
                    Gdx.app.error("RecallDungeon", "Error during attack action", e);
                    attackBtn.setDisabled(false);
                    useBtn.setDisabled(false);
                    waitBtn.setDisabled(false);
                    fleeBtn.setDisabled(false);
                }
            }
        });

        // Wait: skip player's action, show pause then enemy attacks and damage
        // displayed
        waitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    final com.bapppis.core.creature.player.Player pp = com.bapppis.core.game.GameState.getPlayer();
                    if (pp == null || enemy == null)
                        return;
                    attackBtn.setDisabled(true);
                    useBtn.setDisabled(true);
                    waitBtn.setDisabled(true);
                    fleeBtn.setDisabled(true);
                    combatMsg.setText("Waiting...");
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
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
                                useBtn.setDisabled(false);
                                waitBtn.setDisabled(false);
                                fleeBtn.setDisabled(false);
                            }
                        }
                    }, 0.9f);
                } catch (Exception e) {
                    Gdx.app.error("RecallDungeon", "Error during wait action", e);
                }
            }
        });

        // Flee: end combat and return to floor view
        fleeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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

        // Use: show dialog with consumables to pick
        useBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                com.bapppis.core.creature.player.Player pp = com.bapppis.core.game.GameState.getPlayer();
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
                                // enemy gets a free attack after use
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

        // layout center message and buttons
        center.add(combatMsg).width(300).padBottom(6).row();
        center.add(attackBtn).pad(6).row();
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

        if (batch != null) try { batch.dispose(); } catch (Exception ignored) {}
        if (font != null) try { font.dispose(); } catch (Exception ignored) {}
        if (mapFont != null) try { mapFont.dispose(); } catch (Exception ignored) {}
        // dispose atlas or any textures loaded when creating MapActor
        try {
            if (mapActor != null) {
                java.lang.reflect.Field fAtlas = com.bapppis.core.gfx.MapActor.class.getDeclaredField("atlas");
                fAtlas.setAccessible(true);
                Object a = fAtlas.get(mapActor);
                if (a instanceof com.badlogic.gdx.graphics.g2d.TextureAtlas) {
                    ((com.badlogic.gdx.graphics.g2d.TextureAtlas) a).dispose();
                }
                java.lang.reflect.Field fTexMap = com.bapppis.core.gfx.MapActor.class.getDeclaredField("charTextureRegions");
                fTexMap.setAccessible(true);
                Object tm = fTexMap.get(mapActor);
                if (tm instanceof java.util.Map) {
                    java.util.Map<?, ?> mm = (java.util.Map<?, ?>) tm;
                    for (Object v : mm.values()) {
                        if (v instanceof com.badlogic.gdx.graphics.g2d.TextureRegion) {
                            com.badlogic.gdx.graphics.g2d.TextureRegion tr = (com.badlogic.gdx.graphics.g2d.TextureRegion) v;
                            com.badlogic.gdx.graphics.Texture tex = tr.getTexture();
                            if (tex != null) try { tex.dispose(); } catch (Exception ex) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore - best effort dispose
        }
    if (stage != null) try { stage.dispose(); } catch (Exception ignored) {}
        if (VisUI.isLoaded())
            VisUI.dispose();
    }

    // simple name -> key sanitizer: lowercase, replace non-alnum with '_'
    private static String sanitize(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }
}

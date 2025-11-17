package com.bapppis.core.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.bapppis.core.creature.Player;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.Tile;
import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.game.GameState;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class MapActor extends Actor {
    private final BitmapFont font;
    private float cellWidth;
    private float lineHeight;
    private Floor floor;
    private int width;
    private int height;
    private TextureAtlas atlas;
    private float zoomFactor = 1.0f;

    public MapActor(BitmapFont font, TextureAtlas atlas) {
        if (font == null) {
            throw new IllegalArgumentException("MapActor requires a non-null BitmapFont");
        }
        this.font = font;
        this.lineHeight = font.getLineHeight();
        this.atlas = atlas;
        computeCellWidth();
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public void setZoomFactor(float zoom) {
        this.zoomFactor = zoom;
        computeCellWidth();
    }

    private void computeCellWidth() {
        char[] sample = new char[] { '#', 'P', '.', ' ' };
        float max = 0f;
        for (char c : sample) {
            try {
                com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph g = font.getData().getGlyph(c);
                if (g != null)
                    max = Math.max(max, g.xadvance);
                else {
                    GlyphLayout gl = new GlyphLayout(font, String.valueOf(c));
                    max = Math.max(max, gl.width);
                }
            } catch (Exception e) {
                GlyphLayout gl = new GlyphLayout(font, String.valueOf(c));
                max = Math.max(max, gl.width);
            }
        }
        if (max <= 0f) {
            GlyphLayout gl = new GlyphLayout(font, " ");
            if (gl.width > 0)
                max = gl.width;
            else
                max = 8f;
        }
        cellWidth = (max + 1f) * zoomFactor;
        lineHeight = font.getLineHeight() * zoomFactor;
    }

    public void refresh() {
        this.floor = GameState.getCurrentFloor();
        if (floor == null) {
            width = 0;
            height = 0;
            setSize(0, 0);
            return;
        }

        int maxY = 0;
        int maxX = 0;
        for (Coordinate c : floor.getTiles().keySet()) {
            if (c.getY() > maxY)
                maxY = c.getY();
            if (c.getX() > maxX)
                maxX = c.getX();
        }
        height = maxY + 1;
        width = maxX + 1;
        setSize(width * cellWidth, height * lineHeight);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (floor == null || width == 0 || height == 0)
            return;

        // Enable alpha blending for transparency
        batch.enableBlending();

        // Compute offsets so the entire map is centered in the viewport (fixed center,
        // do not follow player). If the map is larger than the viewport, offsets will
        // be negative so the central portion of the map is visible.
        float offsetX = 0f;
        float offsetY = 0f;
        float viewportWidth = getParent() != null ? getParent().getWidth() : 800;
        float viewportHeight = getParent() != null ? getParent().getHeight() : 600;
        float mapW = width * cellWidth;
        float mapH = height * lineHeight;
        offsetX = (viewportWidth - mapW) / 2f;
        offsetY = (viewportHeight - mapH) / 2f;

        float startX = getX() + offsetX;
        float startY = getY() + getHeight() - (lineHeight * 0.1f) + offsetY;

        Player player = GameState.getPlayer();
        int px = -1, py = -1;
        if (player != null && player.getPosition() != null) {
            px = player.getX();
            py = player.getY();
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float drawX = startX + x * cellWidth;
                float drawY = startY - y * lineHeight;

                Tile tile = floor.getTile(new Coordinate(x, y));
                String tileSpr = null;
                String overlaySprite = null;
                char tileSymbol = '.';

                if (tile == null) {
                    tileSpr = "basicFloor";
                    tileSymbol = '.';
                } else if (!tile.isDiscovered()) {
                    tileSpr = "undiscovered";
                    tileSymbol = ' ';
                } else {
                    // Get the base tile sprite
                    tileSpr = tile.getSprite();
                    tileSymbol = tile.getSymbol();
                    
                    // If there's loot, it goes on top of the tile
                    if (tile.getLoot() != null) {
                        overlaySprite = "common_treasure_chest";
                    }
                }

                // Draw the base tile
                boolean tileDrawn = false;
                if (atlas != null && tileSpr != null) {
                    TextureRegion reg = atlas.findRegion(tileSpr);
                    if (reg != null) {
                        float tileW = cellWidth;
                        float tileH = lineHeight * 0.9f;
                        float texY = drawY - lineHeight + (lineHeight * 0.1f);
                        batch.draw(reg, drawX, texY, tileW, tileH);
                        tileDrawn = true;
                    }
                }

                if (!tileDrawn) {
                    font.getData().setScale(zoomFactor);
                    font.draw(batch, String.valueOf(tileSymbol), drawX, drawY);
                    font.getData().setScale(1.0f);
                }
                
                // Draw overlay (chest, etc.) on top of tile if present
                if (overlaySprite != null && atlas != null) {
                    TextureRegion reg = atlas.findRegion(overlaySprite);
                    if (reg != null) {
                        float tileW = cellWidth;
                        float tileH = lineHeight * 0.9f;
                        float texY = drawY - lineHeight + (lineHeight * 0.1f);
                        batch.draw(reg, drawX, texY, tileW, tileH);
                    }
                }

                // Draw tile occupants (enemies, NPCs) before player - only on discovered tiles
                if (tile != null && tile.isDiscovered() && tile.getOccupants() != null && !tile.getOccupants().isEmpty()) {
                    for (com.bapppis.core.creature.Creature occupant : tile.getOccupants()) {
                        // Skip the player (we'll draw them last)
                        if (occupant == player) continue;

                        String spriteName = occupant.getSprite();
                        if (spriteName != null && !spriteName.isEmpty() && atlas != null) {
                            TextureRegion reg = atlas.findRegion(spriteName);
                            if (reg != null) {
                                float maxWidth = cellWidth;
                                float maxHeight = lineHeight * 0.9f;

                                float scale = Math.min(maxWidth / reg.getRegionWidth(), maxHeight / reg.getRegionHeight());
                                float finalW = reg.getRegionWidth() * scale;
                                float finalH = reg.getRegionHeight() * scale;

                                float texX = drawX + (maxWidth - finalW) / 2;
                                float texY = drawY - lineHeight + (lineHeight * 0.1f) + (maxHeight - finalH) / 2;

                                batch.draw(reg, texX, texY, finalW, finalH);
                            }
                        }
                    }
                }

                // Draw player on top if at this position (transparent parts will show tile
                // underneath)
                if (x == px && y == py) {
                    String spriteName = getPlayerSpriteName(player);

                    if (atlas != null && spriteName != null) {
                        TextureRegion reg = atlas.findRegion(spriteName);
                        if (reg != null) {
                            float maxWidth = cellWidth;
                            float maxHeight = lineHeight * 0.9f;

                            float scale = Math.min(maxWidth / reg.getRegionWidth(), maxHeight / reg.getRegionHeight());
                            float finalW = reg.getRegionWidth() * scale;
                            float finalH = reg.getRegionHeight() * scale;

                            float texX = drawX + (maxWidth - finalW) / 2;
                            float texY = drawY - lineHeight + (lineHeight * 0.1f) + (maxHeight - finalH) / 2;

                            batch.draw(reg, texX, texY, finalW, finalH);
                        } else {
                            // Fallback to text
                            font.getData().setScale(zoomFactor);
                            font.draw(batch, "P", drawX, drawY);
                            font.getData().setScale(1.0f);
                        }
                    }
                }
            }
        }
    }

    private String getPlayerSpriteName(Player player) {
        if (player == null)
            return "player_default";
        String spriteName = player.getSprite();
        if (spriteName == null || spriteName.isEmpty())
            return "player_default";

        // Return sprite name if it exists in atlas, otherwise default
        if (atlas != null && atlas.findRegion(spriteName) != null) {
            return spriteName;
        }
        return "player_default";
    }
}

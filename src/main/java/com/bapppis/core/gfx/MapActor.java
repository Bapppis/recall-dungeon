package com.bapppis.core.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.bapppis.core.dungeon.MapPrinter;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.creature.player.Player;
import com.bapppis.core.game.GameState;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class MapActor extends Actor {
    private final BitmapFont font;
    private float cellWidth;
    private float lineHeight;
    private String[] lines = new String[0];
    private final TextureAtlas atlas;
    private final java.util.Map<Character, String> charToRegion;
    private final java.util.Map<Character, TextureRegion> charTextureRegions;

    public MapActor(BitmapFont font) {
        this(font, null, null, null);
    }

    public MapActor(BitmapFont font, TextureAtlas atlas, java.util.Map<Character, String> charToRegion) {
        this(font, atlas, charToRegion, null);
    }

    /**
     * Create a MapActor which can render sprites from an atlas when available.
     * If atlas==null or charToRegion==null drawing falls back to text.
     */
    public MapActor(BitmapFont font, TextureAtlas atlas, java.util.Map<Character, String> charToRegion,
            java.util.Map<Character, TextureRegion> charTextureRegions) {
        if (font == null) {
            throw new IllegalArgumentException("MapActor requires a non-null BitmapFont");
        }
        this.font = font;
        this.lineHeight = font.getLineHeight();
        this.atlas = atlas;
        this.charToRegion = charToRegion;
        this.charTextureRegions = charTextureRegions;
        computeCellWidth();
    }

    private void computeCellWidth() {
        // Choose a fixed cell width by taking the max xadvance of a small set
        char[] sample = new char[]{'#', 'P', '.', ' '};
        float max = 0f;
        for (char c : sample) {
            try {
                com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph g = font.getData().getGlyph(c);
                if (g != null) max = Math.max(max, g.xadvance);
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
            if (gl.width > 0) max = gl.width;
            else max = 8f;
        }
        // Add a small padding so characters don't collide
        cellWidth = max + 1f;
    }

    public void refresh() {
        Floor floor = GameState.getCurrentFloor();
        Player player = GameState.getPlayer();
        String map = MapPrinter.renderWithPlayer(floor, player);
        if (map == null) {
            lines = new String[0];
        } else {
            lines = map.split("\\n");
        }
        // update actor size to fit content
        int maxLen = 0;
        for (String l : lines) if (l != null && l.length() > maxLen) maxLen = l.length();
        setSize(maxLen * cellWidth, Math.max(1, lines.length) * lineHeight);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (lines == null || lines.length == 0) return;
        float startX = getX();
        // Draw from top-left: compute baseline for first line
        float startY = getY() + getHeight() - (lineHeight * 0.1f);
        for (int row = 0; row < lines.length; row++) {
            String line = lines[row];
            if (line == null) line = "";
            float y = startY - row * lineHeight;
            for (int col = 0; col < line.length(); col++) {
                char ch = line.charAt(col);
                String s = String.valueOf(ch);
                float x = startX + col * cellWidth;
                boolean drawn = false;
                if (atlas != null && charToRegion != null) {
                    String regionName = charToRegion.get(ch);
                    if (regionName != null) {
                        TextureRegion reg = atlas.findRegion(regionName);
                        if (reg != null) {
                            // draw texture region sized to the cell
                            float tileW = cellWidth;
                            float tileH = lineHeight * 0.9f;
                            // compute bottom-left for texture so it visually aligns with text baseline
                            float texY = y - lineHeight + (lineHeight * 0.1f);
                            batch.draw(reg, x, texY, tileW, tileH);
                            drawn = true;
                        }
                    }
                }
                if (!drawn) {
                    font.draw(batch, s, x, y);
                }
            }
        }
    }
}

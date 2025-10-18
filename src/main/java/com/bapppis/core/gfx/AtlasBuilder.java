package com.bapppis.core.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to build a TextureAtlas from individual PNG files at runtime.
 */
public class AtlasBuilder {

    /**
     * Build a TextureAtlas from individual PNG files in the specified folder.
     * Each PNG file becomes a region with the filename (without extension) as the region name.
     * 
     * @param folderPath Path to folder containing PNG files (e.g., "sprite_pngs")
     * @return TextureAtlas containing all PNGs, or null if folder doesn't exist or is empty
     */
    public static TextureAtlas buildFromFolder(String folderPath) {
        try {
            FileHandle folder = Gdx.files.internal(folderPath);
            if (!folder.exists() || !folder.isDirectory()) {
                return null;
            }

            FileHandle[] files = folder.list(".png");
            if (files == null || files.length == 0) {
                return null;
            }

            TextureAtlas atlas = new TextureAtlas();
            
            for (FileHandle file : files) {
                try {
                    // Load texture from PNG
                    Texture texture = new Texture(file);
                    
                    // Get region name from filename (without extension)
                    String regionName = file.nameWithoutExtension();
                    
                    // Create texture region and add to atlas
                    TextureRegion region = new TextureRegion(texture);
                    atlas.addRegion(regionName, region);
                } catch (Exception e) {
                    Gdx.app.error("AtlasBuilder", "Error loading texture: " + file.name(), e);
                }
            }

            return atlas;
            
        } catch (Exception e) {
            Gdx.app.error("AtlasBuilder", "Error building atlas from folder: " + folderPath, e);
            return null;
        }
    }

    /**
     * Load atlas with fallback strategy:
     * 1. Try to build from individual PNGs in sprite_pngs folder
     * 2. Fall back to pre-built sprites.atlas
     * 3. Fall back to tiles.atlas
     * 
     * @return TextureAtlas or null if all methods fail
     */
    public static TextureAtlas loadWithFallback() {
        // Try building from individual PNGs first - try multiple possible paths
        String[] spriteFolders = {"sprite_pngs", "assets/sprite_pngs", "sprite_pngs/"};
        TextureAtlas atlas = null;
        
        for (String folder : spriteFolders) {
            atlas = buildFromFolder(folder);
            if (atlas != null && atlas.getRegions().size > 0) {
                return atlas;
            }
        }

        // Fall back to pre-built sprites.atlas
        String[] atlasFiles = {"sprites.atlas", "assets/sprites.atlas"};
        for (String atlasFile : atlasFiles) {
            try {
                if (Gdx.files.internal(atlasFile).exists()) {
                    return new TextureAtlas(Gdx.files.internal(atlasFile));
                }
            } catch (Exception ignored) {
            }
        }

        // Fall back to tiles.atlas
        for (String atlasFile : new String[]{"tiles.atlas", "assets/tiles.atlas"}) {
            try {
                if (Gdx.files.internal(atlasFile).exists()) {
                    return new TextureAtlas(Gdx.files.internal(atlasFile));
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}

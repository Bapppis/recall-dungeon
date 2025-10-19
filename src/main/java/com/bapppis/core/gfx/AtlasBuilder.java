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

public class AtlasBuilder {

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
                    Texture texture = new Texture(file);
                    String regionName = file.nameWithoutExtension();
                    
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

    public static TextureAtlas loadWithFallback() {
        String[] spriteFolders = {"sprite_pngs", "assets/sprite_pngs", "sprite_pngs/"};
        TextureAtlas atlas = null;
        
        for (String folder : spriteFolders) {
            atlas = buildFromFolder(folder);
            if (atlas != null && atlas.getRegions().size > 0) {
                return atlas;
            }
        }

        String[] atlasFiles = {"sprites.atlas", "assets/sprites.atlas"};
        for (String atlasFile : atlasFiles) {
            try {
                if (Gdx.files.internal(atlasFile).exists()) {
                    return new TextureAtlas(Gdx.files.internal(atlasFile));
                }
            } catch (Exception ignored) {
            }
        }

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

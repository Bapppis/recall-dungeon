package com.bapppis.core.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AtlasBuilder {

    public static TextureAtlas buildFromFolder(String folderPath) {
        try {
            FileHandle folder = Gdx.files.internal(folderPath);
            if (!folder.exists() || !folder.isDirectory()) {
                Gdx.app.log("AtlasBuilder", "Folder not found: " + folderPath);
                return null;
            }

            FileHandle[] files = folder.list(".png");
            if (files == null || files.length == 0) {
                Gdx.app.log("AtlasBuilder", "No PNG files in folder: " + folderPath);
                return null;
            }

            TextureAtlas atlas = new TextureAtlas();
            Gdx.app.log("AtlasBuilder", "Building atlas from folder: " + folderPath + " (" + files.length + " files)");

            for (FileHandle file : files) {
                try {
                    Texture texture = new Texture(file);
                    String regionName = file.nameWithoutExtension();

                    TextureRegion region = new TextureRegion(texture);
                    atlas.addRegion(regionName, region);
                    Gdx.app.log("AtlasBuilder", "  Added region: " + regionName);
                } catch (Exception e) {
                    Gdx.app.error("AtlasBuilder", "Error loading texture: " + file.name(), e);
                }
            }

            Gdx.app.log("AtlasBuilder", "Atlas built with " + atlas.getRegions().size + " regions");
            return atlas;

        } catch (Exception e) {
            Gdx.app.error("AtlasBuilder", "Error building atlas from folder: " + folderPath, e);
            return null;
        }
    }

    public static TextureAtlas loadWithFallback() {
        String[] spriteFolders = { "assets/sprite_pngs", "sprite_pngs", "sprite_pngs/" };
        TextureAtlas atlas = null;
        // Prefer a prebuilt atlas if available (this contains player sprites and other
        // packed regions)
        String[] atlasFiles = { "assets/sprites.atlas", "sprites.atlas", "assets/tiles.atlas", "tiles.atlas" };
        for (String atlasFile : atlasFiles) {
            try {
                if (Gdx.files.internal(atlasFile).exists()) {
                    atlas = new TextureAtlas(Gdx.files.internal(atlasFile));
                    Gdx.app.log("AtlasBuilder", "Loaded prebuilt atlas: " + atlasFile);
                    return atlas;
                }
            } catch (Exception e) {
                Gdx.app.error("AtlasBuilder", "Error loading atlas: " + atlasFile, e);
            }
        }

        Gdx.app.log("AtlasBuilder", "No prebuilt atlas found, building from PNGs...");

        // If no prebuilt atlas was present, try loading loose PNGs from known folders.
        for (String folder : spriteFolders) {
            atlas = buildFromFolder(folder);
            if (atlas != null && atlas.getRegions().size > 0) {
                return atlas;
            }
        }

        Gdx.app.error("AtlasBuilder", "Failed to build atlas from any folder");
        return null;
    }
}

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
        String[] spriteFolders = { "sprite_pngs", "assets/sprite_pngs", "sprite_pngs/" };
        TextureAtlas atlas = null;
        // Prefer a prebuilt atlas if available (this contains player sprites and other packed regions)
        String[] atlasFiles = { "sprites.atlas", "assets/sprites.atlas", "tiles.atlas", "assets/tiles.atlas" };
        for (String atlasFile : atlasFiles) {
            try {
                if (Gdx.files.internal(atlasFile).exists()) {
                    atlas = new TextureAtlas(Gdx.files.internal(atlasFile));
                    // Attempt to augment the loaded atlas with any standalone PNGs referenced in tiles.json
                    try {
                        TextureAtlas jsonAtlas = buildFromTilesJsonPngs();
                        if (jsonAtlas != null) {
                            for (com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion r : jsonAtlas.getRegions()) {
                                if (r == null || r.name == null) continue;
                                if (atlas.findRegion(r.name) == null) {
                                    atlas.addRegion(r.name, new com.badlogic.gdx.graphics.g2d.TextureRegion(r));
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    return atlas;
                }
            } catch (Exception ignored) {
            }
        }

        // If no prebuilt atlas was present, try loading loose PNGs from known folders first.
        for (String folder : spriteFolders) {
            atlas = buildFromFolder(folder);
            if (atlas != null && atlas.getRegions().size > 0) {
                return atlas;
            }
        }

        // Finally, try rebuilding from tiles.json mappings as a last resort.
        TextureAtlas jsonAtlas = buildFromTilesJsonPngs();
        if (jsonAtlas != null && jsonAtlas.getRegions().size > 0) {
            return jsonAtlas;
        }

        return null;
    }

    /**
     * Try loading region names from assets/tiles.json and load corresponding PNGs from
     * assets/sprite_pngs/<region>.png into a TextureAtlas. Useful when folder listing is
     * not available (packaged resources) but individual PNGs are present in the runtime
     * classpath.
     */
    private static TextureAtlas buildFromTilesJsonPngs() {
        try {
            if (!Gdx.files.internal("assets/tiles.json").exists()) return null;
            String json = Gdx.files.internal("assets/tiles.json").readString();
            com.badlogic.gdx.utils.JsonReader jr = new com.badlogic.gdx.utils.JsonReader();
            com.badlogic.gdx.utils.JsonValue root = jr.parse(json);
            com.badlogic.gdx.utils.JsonValue mappings = root.get("mappings");
            if (mappings == null) return null;

            TextureAtlas atlas = new TextureAtlas();
            boolean added = false;
            for (com.badlogic.gdx.utils.JsonValue entry = mappings.child; entry != null; entry = entry.next) {
                String region = entry.asString();
                if (region == null || region.isEmpty()) continue;
                String[] candidatePaths = new String[]{"assets/sprite_pngs/" + region + ".png", "sprite_pngs/" + region + ".png", region + ".png"};
                for (String p : candidatePaths) {
                    try {
                        if (Gdx.files.internal(p).exists()) {
                            com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal(p));
                            atlas.addRegion(region, new com.badlogic.gdx.graphics.g2d.TextureRegion(tex));
                            added = true;
                            break;
                        }
                    } catch (Exception e) {
                        // ignore and try next path
                    }
                }
            }
            if (added) return atlas;
        } catch (Exception ignored) {
        }
        return null;
    }
}

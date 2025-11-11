package com.bapppis.core.gfx;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Utility to pack sprite PNGs into a texture atlas using LibGDX's
 * TexturePacker.
 * Run this whenever you add/modify sprites in assets/sprite_pngs/.
 */
public class AtlasPacker {

  public static void main(String[] args) {
    // Configure texture packer settings
    TexturePacker.Settings settings = new TexturePacker.Settings();
    settings.maxWidth = 512;
    settings.maxHeight = 512;
    settings.paddingX = 2;
    settings.paddingY = 2;
    settings.duplicatePadding = false;
    settings.edgePadding = true;
    settings.rotation = false;
    settings.minWidth = 16;
    settings.minHeight = 16;
    settings.format = com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
    settings.alphaThreshold = 0;
    settings.filterMin = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
    settings.filterMag = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
    settings.stripWhitespaceX = false;
    settings.stripWhitespaceY = false;

    // Input: sprite_pngs folder
    // Output: assets folder (creates sprites.atlas and sprites.png)
    String inputDir = "src/main/resources/assets/sprite_pngs";
    String outputDir = "src/main/resources/assets";
    String packFileName = "sprites";

    System.out.println("Packing textures from: " + inputDir);
    System.out.println("Output to: " + outputDir);

    try {
      TexturePacker.process(settings, inputDir, outputDir, packFileName);
      System.out.println("✓ Atlas packed successfully!");
      System.out.println("  Created: " + outputDir + "/" + packFileName + ".atlas");
      System.out.println("  Created: " + outputDir + "/" + packFileName + ".png");
    } catch (Exception e) {
      System.err.println("✗ Error packing atlas:");
      e.printStackTrace();
      System.exit(1);
    }
  }
}

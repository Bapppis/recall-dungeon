#!/usr/bin/env python3
"""
Pack individual sprite PNGs into a single sprite sheet.
"""
from PIL import Image
import os

# Paths
SPRITE_DIR = "../src/main/resources/assets/sprite_pngs"
OUTPUT_PNG = "../src/main/resources/assets/sprites.png"

# Sprite list with their positions in the atlas (matching the .atlas file)
sprites = [
    ("basicWall.png", 2, 2),
    ("basicFloor.png", 20, 2),
    ("basicGenFloor.png", 38, 2),
    ("basicUpStairs.png", 56, 2),
    ("basicDownStairs.png", 74, 2),
    ("common_treasure_chest.png", 92, 2),
    ("undiscovered.png", 110, 2),
    ("player_default.png", 128, 2),
    ("player_biggles.png", 148, 2),
    ("player_voss.png", 168, 2),
    ("monster_goblin.png", 188, 2),
]

# Create sprite sheet
atlas = Image.new('RGBA', (256, 256), (0, 0, 0, 0))

script_dir = os.path.dirname(os.path.abspath(__file__))
sprite_dir = os.path.join(script_dir, SPRITE_DIR)
output_path = os.path.join(script_dir, OUTPUT_PNG)

for filename, x, y in sprites:
    sprite_path = os.path.join(sprite_dir, filename)
    if os.path.exists(sprite_path):
        sprite = Image.open(sprite_path)
        atlas.paste(sprite, (x, y))
        print(f"Added {filename} at ({x}, {y})")
    else:
        print(f"Warning: {filename} not found at {sprite_path}")

atlas.save(output_path)
print(f"\nSprite sheet saved to: {output_path}")

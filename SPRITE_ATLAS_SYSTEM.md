# Sprite Atlas System

## Overview
The game uses a flexible sprite loading system with automatic fallback and performance optimizations.

## Loading Strategy (in order of priority)

### 1. Runtime-Built Atlas from Individual PNGs **PRIMARY**
- **Location**: `src/main/resources/assets/sprite_pngs/`
- **How it works**: The `AtlasBuilder` class automatically scans this folder and creates a texture atlas from all `.png` files at runtime
- **Advantages**:
  - Easy to add/edit sprites - just drop PNG files in the folder
  - No need to rebuild atlas manually
  - Automatically uses filename (without extension) as region name

### 2. Pre-built sprites.atlas **FALLBACK 1** **CURRENTLY USED**
- **Location**: `src/main/resources/assets/sprites.atlas`
- **When used**: If sprite_pngs folder is empty or fails to load
- **Use case**: For production builds where you want optimized, pre-packed atlases
- **Note**: This is currently what your game uses (9 regions loaded)

### 3. Pre-built tiles.atlas **FALLBACK 2**
- **Location**: `src/main/resources/assets/tiles.atlas`
- **When used**: If both previous methods fail
- **Use case**: Legacy compatibility

## Performance Optimizations

### Atlas Caching
- The texture atlas is loaded **once** on the first call to `showFloorView()`
- Subsequent calls reuse the cached atlas
- Atlas is properly disposed when the game closes
- **Benefit**: No redundant file I/O or texture loading

### Quiet Loading
- All debug console prints have been removed
- Only critical errors are logged
- Clean console output for production

## Current Sprite Files in sprites.atlas
Based on the console output, these sprites are available:
- `floor` - Floor tiles
- `genfloor` - Generation floor tiles
- `monster_goblin` - Goblin enemy sprite
- `player_biggles` - Biggles character sprite
- `player_default` - Default player sprite
- `player_voss` - Voss character sprite
- `stairs_down` - Stairs going down
- `stairs_up` - Stairs going up
- `undiscovered` - Undiscovered/fog of war tile
- `wall` - Wall tiles

## Character to Region Mapping
In `RecallDungeon.java`, map characters are mapped to sprite regions:
```java
'#' → "wall"
'.' → "floor"
':' → "genfloor"
'^' → "stairs_up"
'v' → "stairs_down"
'P' → "player_default"
'?' → "undiscovered"
```

## How to Add New Sprites

### Option 1: Add to Pre-built Atlas (Recommended for Production)
1. Update your `sprites.png` and `sprites.atlas` files with the new region
2. Add mapping in `RecallDungeon.java`:
   ```java
   charToRegion.put('T', "treasure_chest");
   ```

### Option 2: Use Individual PNGs (Recommended for Development)
1. Create your sprite as a PNG file (e.g., `treasure_chest.png`)
2. Place it in `src/main/resources/assets/sprite_pngs/`
3. Add mapping in `RecallDungeon.java`:
   ```java
   charToRegion.put('T', "treasure_chest");
   ```
4. The game will automatically rebuild the atlas from the folder

## Classes
- **AtlasBuilder.java** - Handles runtime atlas building and fallback logic with caching
- **RecallDungeon.java** - Uses cached atlas for optimal performance
- **MapActor.java** - Renders sprites from the atlas, falls back to text if sprite missing

## Memory Management
- Atlas is cached in `RecallDungeon.spriteAtlas` field
- Properly disposed in `dispose()` method to prevent memory leaks
- No redundant atlas loading on scene transitions

## Technical Details
- Atlas building happens once at game start (when showFloorView() is called for the first time)
- Each PNG becomes a TextureRegion in the atlas
- Region names are derived from filenames (case-sensitive)
- If a character mapping points to a missing region, MapActor falls back to text rendering for that character
- The system tries multiple path variations to handle different deployment scenarios


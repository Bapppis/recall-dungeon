# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v0.1.12] - 2025-11-24

### Changed

- **Combat System**: Implemented d100-style damage scaling (Option 2) for larger numbers and variance
  - **All dice rolls now multiply by 5**: Damage ranges significantly increased (e.g., 2d6 becomes 10-60 instead of 2-12)
  - **Stat bonuses applied per-hit**: Formula changed from once-per-attack to `(statBonus × 5 × damageMultiplier)` per hit
  - **Physical attacks**: `(Dice.roll(dice) × 5) + (statBonus × 5 × damageMultiplier)` per hit
  - **Magic attacks**: `(Dice.roll(dice) × 5) + (magicStatBonus × 5 × magicMult)` per hit
  - **Secondary damage**: Dice × 5 (no stat bonus, same as before)
  - **Spell damage**: `(Dice.roll(dice) × 5) + (statBonus × 5)` then multiplied by damageMult
  - **Example changes**:
    - Captain Voss "Shoot" (2d4, STR 5): 27-33 → **35-65 damage**
    - Captain Voss "Barrage" (3×1d3, mult 0.32): 11-17 → **39-69 damage**
    - Spells (2d6, INT 8, mult 1.25): 52.5-90 → **62.5-125 damage**
- **Stat bonus calculation**: Creature.java now passes base stat value to AttackEngine (removed pre-multiplication by 5)
- **Test updates**: Updated SecondaryDamageAndBuildupTest expected values for new damage ranges
  - Added damageMultiplier = 1.0f to test attacks (required for stat bonus application)
  - Updated assertions: 1d1 with crit now expects 10 (was 2), 1d1 with stat bonus expects 55 (was 51)

### Fixed

- **Test failures**: Resolved damage calculation mismatches in combat tests due to scaling changes
- **Stat bonus application**: Fixed tests that created attacks without damageMultiplier (was defaulting to 0)

### Files Modified

- `src/main/java/com/bapppis/core/combat/AttackEngine.java` — physical/magic damage scaling by 5, stat bonus per-hit with multiplier
- `src/main/java/com/bapppis/core/combat/SpellEngine.java` — spell damage scaling by 5, stat bonus application updated
- `src/main/java/com/bapppis/core/creature/Creature.java` — removed stat bonus pre-multiplication, passes base value
- `src/test/java/com/bapppis/core/game/SecondaryDamageAndBuildupTest.java` — updated test expectations and added damageMultiplier
- `src/main/resources/SYSTEM_REFERENCE.md` — updated all damage formulas to reflect d100 scaling
- `README.md` — updated damage calculation documentation with d100 scaling examples

### Developer Notes

- **Damage ranges increased ~5x**: Enemy HP pools may need rebalancing for appropriate challenge levels
- **Stat bonuses now scale**: Characters with higher stats see much larger damage increases
- **Critical hits amplified**: Crits now double larger base numbers (e.g., 35-65 becomes 70-130)
- **More variance in combat**: Dice rolls have wider ranges, making combat less predictable



## [v0.1.11] - 2025-11-23

### Added

- **Interaction System**: E key + directional input for interacting with tiles
  - Press E to enter interaction mode (yellow prompt appears)
  - Press WASD/arrows to interact with tile in that direction
  - Works with chests, corpses, and dropped items
- **Loot Transfer Dialog**: Two-panel UI for looting items
  - Left panel shows container contents ("Chest:" or "On Ground:")
  - Right panel shows player inventory (read-only)
  - Click "Take" to transfer items to inventory
  - Dialog automatically refreshes when taking items
  - Auto-closes when all items are looted
- **Chest Spawning**: Treasure chests now properly spawn with loot from loot pools
  - Fixed loot pool sampling to use numeric IDs (40000, 40001, 40002)
  - Nested pool references now work correctly (Common Treasure Chest → Common Potions/Weapons)
  - Chests spawn with 1 guaranteed potion + chance for more potions/weapons
  - Chests remain on map after being emptied (can be looted multiple times if refilled)
- **Corpse Creation**: Defeated enemies leave lootable corpses
  - Enemy death creates corpse tile with loot from enemy's loot pool
  - Corpse sprite rendered at 1/3 scale with transparency
  - Corpses removed from map when all items are looted
- **Drop Item Functionality**: Players can drop items from inventory onto floor
  - Dropped consumables create droppedPotion tiles
  - Dropped items render at 1/3 scale with floor showing underneath
  - Dropped item tiles removed when all items are picked up
- **Inventory Shortcuts**: Press 'I' to toggle inventory dialog open/close
- **Tile Type System Expansion**: Added new tile types for loot system
  - lootableCorpse.json (symbol: '%', sprite: lootable_corpse)
  - droppedItem.json (symbol: '*', sprite: dropped_item)
  - droppedPotion.json (sprite: dropped_potion)
  - commonTreasureChest.json (symbol: 'C', sprite: common_treasure_chest)

### Changed

- **Sprite Atlas System**: PNG folder now prioritized over prebuilt atlas for development
  - AtlasBuilder tries multiple path variations for PNG folder
  - Falls back to prebuilt atlas if PNG folder not found
  - Enables rapid sprite iteration without atlas rebuilding
- **Map Rendering**: Enhanced transparency and sprite scaling
  - Floor tiles render underneath chests, corpses, and dropped items
  - Corpses and dropped items render at 0.33x scale (centered)
  - Chests render at full size on top of floor
  - All special tiles show floor underneath for visual clarity
- **Map Viewport**: Increased zoom from 2.0x to 2.5x for better map coverage
  - More tiles visible on screen at once
  - Reduces need for scrolling/panning during exploration
- **Loot System**: Complete refactor of loot pool loading and sampling
  - LootManager now calls loadDefaults() to load all nested pools
  - Fixed loot pool ID resolution (numeric IDs vs string names)
  - Added comprehensive debug logging for loot spawning pipeline

### Fixed

- **Tile Visibility**: Fixed tiles not appearing on map
  - TileTypeLoader now loads lootableCorpse, droppedItem, droppedPotion tile types
  - All tile symbols now render correctly (C, %, *, etc.)
- **Loot Pool Sampling**: Fixed nested pool references
  - Common Treasure Chest pool references now use numeric IDs ("40001", "40000")
  - LootManager properly resolves nested pools by ID and name
  - Fixed empty chest bug caused by unloaded nested pools
- **Sprite Loading**: Fixed sprite atlas loading priority
  - PNG folder sprites now load correctly
  - Fixed transparency issues with chest/corpse/item sprites
- **ConcurrentModificationException**: Fixed crash when iterating over tile items during looting
  - Now creates defensive copy of items list before iteration
- **Chest Persistence**: Chests no longer disappear after looting
  - Only corpses and dropped items are replaced with floor when empty
  - Chests remain on map (symbol 'C') even when empty
- **Dialog Labeling**: Loot dialog now shows correct container type
  - "Chest:" for treasure chests
  - "On Ground:" for corpses and dropped items

### Files Modified

- `src/main/java/com/bapppis/core/dungeon/TileTypeLoader.java` — added missing tile types to load list
- `src/main/java/com/bapppis/core/gfx/AtlasBuilder.java` — PNG folder priority, multiple path variations
- `src/main/java/com/bapppis/core/gfx/MapActor.java` — transparency, sprite scaling, increased zoom to 2.5x
- `src/main/java/com/bapppis/core/gfx/RecallDungeon.java` — loot transfer dialog, 'I' key handler, drop item, corpse creation
- `src/main/java/com/bapppis/core/game/CommandParser.java` — interaction mode, chest/corpse/item interaction
- `src/main/java/com/bapppis/core/game/GameState.java` — LootTransferCallback interface and static methods
- `src/main/java/com/bapppis/core/loot/LootManager.java` — loadDefaults() calls, debug logging, nested pool resolution
- `src/main/java/com/bapppis/core/dungeon/generator/BSPRoomGenerator.java` — chest tile creation instead of spawnTreasureChest()
- `src/main/resources/data/loot_pools/Common Treasure Chest.json` — fixed nested pool IDs to numeric format
- `src/main/resources/data/tile_types/lootableCorpse.json` (NEW)
- `src/main/resources/data/tile_types/droppedItem.json` (NEW)
- `src/main/resources/data/tile_types/droppedPotion.json` (NEW)

## [v0.1.10] - 2025-11-18

### Added

- **Combat System**: Proximity-based combat triggering and dedicated combat view
  - Enemy AI triggers combat when moving adjacent to player
  - Player movement triggers combat when moving adjacent to enemy
  - Dedicated combat UI with side-by-side player and enemy displays
  - Combat view features:
    - Player sprite (left) with stat panel showing HP, Mana, Stamina, Level
    - Enemy sprite (right) with stat panel showing HP and Level
    - Action buttons: Attack, Inventory, Spells, Use Item, Wait, Flee
    - Spells button shows available spells with mana costs and effects
    - Spells button grayed out when player has no learned spells
  - Turn-based combat with player action followed by enemy counter-attack
  - Victory handling: XP award, enemy removal from map, combat exit
  - Thread-safe UI updates using LibGDX main thread (Gdx.app.postRunnable)
  - Proper sprite sizing (48x48) for combat view display
  - Font loading fallback system (default.fnt)

### Changed

- Game object preservation: Added instance variable to maintain Game state across view transitions
- Input handling: Combat view and floor view use separate input processors
- Enemy cleanup: Defeated enemies properly removed from tiles via setPosition(null)

### Fixed

- Combat triggering: Now consistent whether player or enemy initiates proximity
- Movement after combat: Fixed Game object loss during stage clearing
- Victory dialog listeners: Proper button creation and click handling
- Combat exit timing: setInCombat(false) called after dialog dismissal
- Sprite rendering: Removed conflicting setScale calls
 - Spells unlocking: spells granted by classes, level unlocks, and talent choices are now added to the player's spell references so unlocked spells appear in combat

### Files Modified

- `src/main/java/com/bapppis/core/gfx/RecallDungeon.java` — added combat view, currentGame instance variable, victory handling
- `src/main/java/com/bapppis/core/game/CommandParser.java` — added adjacent enemy detection after player movement
- `src/main/java/com/bapppis/core/creature/Enemy.java` — proximity-based combat triggering in takeAITurn()
- `src/main/java/com/bapppis/core/game/GameState.java` — combat state management with inCombat flag and combatEnemy reference
 - `src/main/java/com/bapppis/core/creature/playerClass/PlayerClassService.java` — apply class & level unlocks now add unlocked spells to player's `spellReferences`
 - `src/main/java/com/bapppis/core/creature/playerClass/TalentTreeService.java` — talent choice unlocks now add unlocked spells to player's `spellReferences`

## [v0.1.09] - 2025-11-17

- Top-left in-map HUD: player name, HP, Mana, Stamina and Level overlay the floor view (screen-anchored).
- Bottom-right quick actions: `Stats` and `Inventory` buttons open lightweight dialogs.

### Changed

- Floor view map is now centered and uses a larger default zoom so the play area is more visible; HUD elements are anchored to screen corners instead of the map.
- `RecallDungeon` now cleanly shuts down any running `Game` instance when returning to the main menu to prevent stale threads and duplicate runs.

### Fixed

- Equipment swap bugs: fixes in `ItemManager` ensure two-handed weapons and offhand items are removed/returned to inventory correctly when swapping (e.g., dagger ↔ bow behavior).
- Spawn and LOS stability fixes carried over from earlier v0.1.08 changes (see continued section below).

### Files Modified

- `src/main/java/com/bapppis/core/gfx/RecallDungeon.java` — added HUD overlay, anchored HUD layout, centered map container, repositioned menu buttons.
- `src/main/java/com/bapppis/core/creature/ItemManager.java` — fixed equip/unequip handling for two-handed and offhand items so swapped items return to inventory.
- `README.md` — documented enemy AI and brief usage notes for HUD and controls.

## [v0.1.08] - 2025-11-16

### Added

- Basic enemy AI: `Enemy.takeAITurn()` implements a simple greedy one-step chase toward the player when the player is within vision range. Enemies now move one tile per turn and use a Manhattan-distance heuristic for target selection.
- `Coordinate.manhattanDistance()` helper for fast grid distance calculations used by AI and vision checks.
- `docs/ENEMY_AI.md` documentation describing the AI design, behavior, and extensibility.

### Changed

- Turn processing (`Game.passTurn()`) now triggers NPC/enemy AI after creature property ticks so creatures take their turns following the player.
- `BSPRoomGenerator` sets spawned enemy positions when placing monsters into tiles so newly spawned enemies have proper `position` fields and appear/move correctly.
- `Floor.hasLineOfSight(...)` gained an overloaded variant that can optionally consider occupied tiles as blockers. `Enemy` AI now checks line of sight (including occupied tiles) before pursuing the player so enemies cannot see through chests or other occupied tiles.
- UI flow: returning to the main menu now shuts down any running `Game` instance (`RecallDungeon.showMainMenu()`), preventing stale background threads and double-running game instances.
- Updated readme

### Fixed

- Fixed issue where spawned enemies never moved because their `position` field was not initialized when added to a tile.
- Fixed crash when returning to the main menu and selecting a character again by ensuring the previous `Game` is cleanly shut down before creating a new one.

### Files Modified

- `src/main/java/com/bapppis/core/creature/Enemy.java` — added position tracking, `takeAITurn()` AI method, LOS/vision checks.
- `src/main/java/com/bapppis/core/dungeon/Coordinate.java` — added `manhattanDistance()`.
- `src/main/java/com/bapppis/core/game/Game.java` — integrated AI execution into `passTurn()`.
- `src/main/java/com/bapppis/core/game/GameState.java` — added `inCombat` flag and accessors to pause AI during combat.
- `src/main/java/com/bapppis/core/dungeon/generator/BSPRoomGenerator.java` — ensure spawned monsters have their `position` set when placed into tiles.
- `src/main/java/com/bapppis/core/dungeon/Floor.java` — extended `hasLineOfSight()` to optionally treat occupied tiles as LOS blockers.
- `src/main/java/com/bapppis/core/gfx/RecallDungeon.java` — shutdown existing `Game` instance when returning to main menu to avoid double-run crashes.
- `README.md` — updated to document the enemy AI implementation and behavior.


## [v0.1.07] - 2025-11-14

### Added

- Runtime texture reload: "Reload Textures" button on floor view allows reloading the sprite atlas without restarting the game (useful for iterating on sprite changes).
- `MapActor.setAtlas()` method enables updating the texture atlas at runtime.

### Changed

- Undiscovered tiles now render using the `undiscovered.png` sprite (black tile) instead of wall texture, improving visual clarity of unexplored areas.
- Sprite atlas regenerated to include both `undiscovered` and `Unused_texture` sprites with proper naming.

### Fixed

- Wall tile discovery: walls at the edge of vision range are now properly discovered and display their wall texture (`basicWall.png`) instead of remaining as undiscovered tiles.
- Line-of-sight algorithm corrected to discover the destination tile before checking if it blocks vision, ensuring wall tiles render correctly when revealed.

### Files Modified

- `src/main/java/com/bapppis/core/gfx/MapActor.java` — made atlas non-final, added `setAtlas()` method, changed undiscovered tile sprite from `"basicWall"` to `"undiscovered"`.
- `src/main/java/com/bapppis/core/gfx/RecallDungeon.java` — added "Reload Textures" button with atlas disposal and reload logic.
- `src/main/java/com/bapppis/core/dungeon/Floor.java` — fixed `hasLineOfSight()` to check destination before blocking, ensuring walls are discovered.
- `src/main/resources/assets/sprites.atlas` — regenerated with proper sprite names.


## [v0.1.06] - 2025-11-13

### Added

- One-way stair choice: when the player first chooses a direction at floor 0, upstairs only allow going up and downstairs only allow going down on subsequent floors.
- Spawn-at-stair behavior on floor transition: using an upstairs now places the player at the corresponding downstairs tile on the next floor (and vice versa), preserving logical movement between floors.
- Final staircase connectivity safeguard: generator validates upstairs↔downstairs reachability after generation and will carve a connecting path if a staircase becomes isolated.

### Changed

- Dungeon openness tuning: reduced open-area creation from 8% to 3% to produce tighter layouts (corridor widening remains tuned to the current default).
- Generation randomness: the generator now seeds from the current time by default (per-run base seed) so floors vary between runs; each floor continues to derive a distinct seed from the base seed.
- Respawn behavior: floor-respawn logic was refactored so explicit stair-based spawns are preserved (we now reveal/reset fog manually after moving floors instead of calling the global respawn helper which previously overrode the position).

### Fixed

- Treasure chest blocking: chests spawned at runtime now mark tiles as occupied so players can no longer walk through occupied chests (`Tile.isOccupied()` now considers `loot != null`).
- Debug noise removal: removed or silenced development prints related to movement, blocking messages, and spawn debug output.

### Notes

- Build: local Maven package (skip tests) completed successfully after these changes.
- Follow-ups: make openness and seed behavior configurable at runtime; optionally add a deterministic seed option exposed in settings for reproducible runs.


## [v0.1.05] - 2025-11-12


### Added

- Maze generation for floor interiors: `BSPRoomGenerator` now includes a recursive backtracker maze generator for the inner area (carves winding corridors instead of a single open room).
- Connectivity guarantees: `carvePathIfNeeded()` ensures the player spawn can reach both upstairs and downstairs on floor 0 by carving floor tiles when necessary.
- Helper methods added to `BSPRoomGenerator`: `generateMaze()`, `ensureFloorAt()`, `isReachable()`, `carvePathIfNeeded()`, `wouldBlockPath()`, `spawnMonsters()`, and `spawnChests()` to support maze generation, spawn placement, and path validation.

### Changed

- Chest placement now validates candidate locations with `wouldBlockPath()` so chests do not block critical paths between spawn and stairs. Chest placement remains quadrant-aware but will fallback to non-blocking locations when needed.
- Monster and chest spawns are compatible with the new maze layout; quadrant-based spawning logic was preserved and adjusted to work on carved maze floors.

### Fixed

- Prevented chests from inadvertently blocking navigation to stairs on floor 0 by validating placements before committing them to tiles.

### Files modified

- `src/main/java/com/bapppis/core/dungeon/generator/BSPRoomGenerator.java` (UPDATED) — added maze generation, connectivity helpers, and chest non-blocking checks.

 - Turn system foundations: `Game.passTurn()` and a `WaitCommand` (bound to SPACE) so player waits and creatures tick their per-turn properties.
 - BSP content spawning: `BSPRoomGenerator` now spawns monsters on floor 0 (one per quadrant not containing the player) using the `monster_pools` definitions and places created creatures into `Tile.getOccupants()`.

### Changed

- Sprite pipeline: replaced the ad-hoc Python-only packing workflow with a LibGDX-integrated packer. The packer is wired into the build/dev flow and produces deterministic, non-overlapping atlas regions.
- `MapActor` rendering refactor: simplified rendering path, enabled alpha blending, always draws the base tile first and then overlays (for example, treasure chests) and actors on top. Player sprite lookup was simplified and duplicate mapping logic removed.
- `pom.xml` updated to include `com.badlogicgames.gdx:gdx-tools` so the packer runs on the project classpath.
 - Tile / occupancy semantics: `Tile` now keeps `occupants` (a list of creatures). `Tile.isOccupied()` returns `tileType.isOccupied || !occupants.isEmpty()`. `Player.setPosition(...)` and floor-change logic were updated to remove/add the player to tile occupants when moving between tiles and floors.
 - `MapActor` now respects fog-of-war for creatures: occupants (non-player creatures) are only rendered when their tile is discovered. This prevents enemies from appearing on undiscovered tiles.
 - `BSPRoomGenerator` updated to use weighted sampling from the "Floor 0 Enemies" monster pool and to retry placement within the intended quadrant, falling back to a deterministic scan if random attempts fail.
 - `AllLoaders` initialization order: now loads `monster_pools` (in addition to `loot_pools`) so monster pools are available to generators at startup.

### Fixed

- Resolved overlapping atlas regions that caused sprites to render twice (for example, a monster region bleeding into a player region). The new atlas is properly packed and no longer produces visual artifacts.
- Transparency fixes: sprite alpha now shows underlying tiles correctly for players and chest overlays.
 - Fixed monster pool loading bug: `monster_pools` are now loaded at startup (previously the lookup could return null, causing spawn failures).
 - Fixed spawned creatures invisibility / occlusion: creature JSON `sprite` fields are respected and `MapActor` rendering order plus fog-of-war fixes ensure creatures render correctly when tiles are discovered.
 - Cleaned up noisy debug logging introduced during development (BSP generator and chest placement messages removed).

### Removed

- `scripts/pack_sprites.py` is now removed in favour of the LibGDX `AtlasPacker`.

### How to repack

- To regenerate the atlas using the Java packer from the project root:

  mvn compile exec:java "-Dexec.mainClass=com.bapppis.core.gfx.AtlasPacker"

  The packer writes `src/main/resources/assets/sprites.atlas` and `src/main/resources/assets/sprites.png`.

### Files modified / added

- src/main/java/com/bapppis/core/gfx/AtlasPacker.java (NEW) — LibGDX TexturePacker-based packer utility
- pom.xml (UPDATED) — added `gdx-tools` dependency
- src/main/java/com/bapppis/core/gfx/MapActor.java (REFRACTORED) — rendering cleanup, blending, overlay support
- src/main/resources/assets/sprites.atlas (REGENERATED)
- src/main/resources/assets/sprites.png (REGENERATED)
- SPRITE_ATLAS_PACKING.md (NEW) — atlas packing docs and workflow
- scripts/pack_sprites.py (DEPRECATED) — left for reference, recommended to use `AtlasPacker`

### Notes

- Build and packer run were validated locally: the TexturePacker produced a non-overlapping atlas (256×128 in the validation run) and the project builds successfully with the new dependency. Use the Java packer for consistent, production-ready atlas files and reserve runtime PNG-based packing for quick development iteration only.

## [v0.1.04] - 2025-11-11

### Added

- LibGDX-based texture packing utility: `AtlasPacker.java` (uses `gdx-tools` TexturePacker) to produce a proper `sprites.atlas` + `sprites.png` from individual PNGs.
- `SPRITE_ATLAS_PACKING.md` documentation describing how and when to repack atlases and the recommended workflow for development vs production.

## [v0.1.03] - 2025-11-10

### Added

- **JSON-Based Tile System**: Complete refactor to data-driven tile architecture
  - `TileType.java`: Template class defining tile blueprints (name, symbol, sprite, flags, loot)
  - `TileTypeLoader.java`: Loads tile types from JSON files at startup
  - Six tile type JSONs: basicWall, basicFloor, basicGenFloor, basicUpStairs, basicDownStairs, commonTreasureChest
  - Tile types define: name, symbol, sprite name, isWall, isOccupied, isUpstairs, isDownstairs, lootPoolName
- **Tile.java Refactor**: Instance-based tile system with TileType references
  - Primary constructor: `Tile(Coordinate, TileType)` - uses TileType templates
  - Deprecated constructor: `Tile(Coordinate, char)` - for backward compatibility with MapParser
  - Delegates behavior to TileType: getSymbol(), getSprite(), isWall(), isOccupied()
  - Instance-specific state: coordinate, discovered, spawn, loot, occupants, items
- **Sprite Packing System**: Automated sprite atlas generation
  - `pack_sprites.py`: Python script to pack individual PNGs into sprite atlas
  - Generates `sprites.atlas` + `sprites.png` from `sprite_pngs/` folder
  - Atlas includes: basicWall, basicFloor, basicUpStairs, basicDownStairs, treasure chest, player sprites
  - Runtime atlas building from loose PNGs for development flexibility

### Changed

- **MapActor Rendering Optimization**: Eliminated character-to-sprite mapping duplication
  - Removed ~70 lines of character-to-sprite HashMap creation from RecallDungeon.java
  - MapActor now receives Floor object directly and calls `tile.getSprite()` for sprite names
  - Removed intermediate mapping layers (tiles.json, charToRegion mappings)
  - Sprite lookup now: TileType → sprite name → TextureAtlas lookup
- **BSPRoomGenerator**: Updated to use TileTypeLoader
  - Loads TileTypes at generation time: wallType, floorType, upStairsType, downStairsType
  - All tile creation uses `new Tile(coordinate, tileType)` pattern
  - Consistent tile creation across walls, floors, and stairs
- **AtlasBuilder Improvements**: Enhanced sprite loading with fallback system
  - Tries prebuilt atlas first (`assets/sprites.atlas`)
  - Falls back to building from `assets/sprite_pngs/` folder at runtime
  - Added comprehensive logging for atlas loading and sprite region detection
  - Proper UTF-8 encoding for resource streams
- **TileTypeLoader**: Classpath resource loading for packaged JARs
  - Uses `getResourceAsStream()` instead of file system paths
  - Loads from `data/tile_types/` in resources
  - Explicit file list for reliable loading
  - Works in both development and production builds

### Fixed

- **Tile System Initialization**: TileTypeLoader now loaded in AllLoaders.loadAll()
  - Loads after PropertyLoader, before SpellLoader
  - Prevents NullPointerException when BSPRoomGenerator tries to create tiles
  - All tile types properly cached before dungeon generation
- **Sprite Rendering**: Fixed sprites not displaying in game
  - Deleted old `sprites.atlas` with outdated sprite names (wall, floor, stairs_up, stairs_down)
  - Created new atlas with correct names matching TileType JSONs (basicWall, basicFloor, etc.)
  - Fixed atlas folder path priority (tries `assets/sprite_pngs` first)
  - Sprite names now consistent across: TileType JSONs → sprite PNGs → atlas regions

### Removed

- **tiles.json**: Eliminated redundant character-to-sprite mapping file
  - Sprite names now defined in TileType JSONs
  - Reduces maintenance burden from 5 files to 2 files per new tile type
- **buildFromTilesJsonPngs()**: Removed obsolete atlas building method from AtlasBuilder
  - Simplified loadWithFallback() to use either prebuilt atlas or PNG folder
- **Character Mappings**: Removed duplicate sprite name mappings from RecallDungeon
  - MapActor constructor simplified from 4 parameters to 2 (font, atlas)
  - All tile rendering now goes through tile.getSprite()

### Technical Details

- **Tile Creation Flow**: TileTypeLoader loads JSONs → BSPRoomGenerator gets TileTypes → creates Tile instances
- **Rendering Flow**: MapActor receives Floor → iterates tiles → tile.getSprite() → atlas.findRegion() → render
- **Sprite System**: Individual PNGs (16×16 or 18×18) → packed into single atlas → loaded at runtime
- **Extensibility**: Adding new tile type requires: 1) Create TileType JSON, 2) Add matching sprite PNG
- **Backward Compatibility**: Deprecated char constructor allows MapParser to continue working
- **ID Ranges**: Tile types use descriptive names (no numeric IDs needed for tiles)

### Files Modified

- src/main/java/com/bapppis/core/dungeon/Tile.java (REFACTORED)
- src/main/java/com/bapppis/core/dungeon/TileType.java (NEW)
- src/main/java/com/bapppis/core/dungeon/TileTypeLoader.java (NEW)
- src/main/java/com/bapppis/core/dungeon/generator/BSPRoomGenerator.java (UPDATED - uses TileTypeLoader)
- src/main/java/com/bapppis/core/gfx/MapActor.java (REFACTORED - direct Floor access)
- src/main/java/com/bapppis/core/gfx/RecallDungeon.java (SIMPLIFIED - removed char mappings)
- src/main/java/com/bapppis/core/gfx/AtlasBuilder.java (UPDATED - improved logging and paths)
- src/main/java/com/bapppis/core/AllLoaders.java (UPDATED - added TileTypeLoader)
- src/main/resources/data/tile_types/\*.json (NEW - 6 tile type definitions)
- src/main/resources/assets/sprites.atlas (REGENERATED)
- src/main/resources/assets/sprites.png (REGENERATED)
- scripts/pack_sprites.py (NEW - sprite atlas packing tool)

### Development Tools

- **pack_sprites.py**: Automated sprite sheet generation
  - Requires: Pillow (PIL) library
  - Input: Individual PNG files in sprite_pngs/
  - Output: sprites.atlas + sprites.png
  - Maintains consistent sprite positions for stable atlas file

### Notes

- Tile system now fully data-driven and JSON-configurable
- Adding new tiles is significantly easier (2 files instead of 5)
- Runtime sprite atlas building enables rapid iteration during development
- Prebuilt atlas can be used in production for faster loading
- System designed for future expansion (biomes, tile variants, animated tiles)

## [v0.1.02] - 2025-11-08

### Added

- **Loot Pool Sprite System**: Treasure chests now render with sprites

  - Added `sprite` field to `LootPool` class for visual representation
  - Common Treasure Chest displays `common_treasure_chest` sprite
  - `LootPoolLoader` now provides static lookup methods (`getLootPoolById`, `getLootPoolByName`)
  - Treasure chest sprite added to `tiles.json` for atlas loading

- **Treasure Chest Spawning**: Procedurally generated chests with loot

  - `BSPRoomGenerator` uses `spawnTreasureChest()` method to place Common Treasure Chest loot pools
  - Chests placed in random quadrants (avoiding stairs and spawn points)
  - `Tile.getLoot()` and `Tile.isOccupied()` getters added for chest detection

- **Movement Blocking System**: Occupied tiles prevent movement

  - Players cannot walk through treasure chests or other occupied tiles
  - `CommandParser` now checks `tile.isOccupied()` before allowing movement
  - Provides specific feedback about what blocks the way (e.g., "a treasure chest blocks your way")

- Procedural generation improvements:

  - `BSPRoomGenerator` divides the inner area into 4 quadrants and places upstairs/downstairs in distinct quadrants
  - Player spawn placement rules:
    - Floor 0: spawn is placed in a quadrant distinct from both stairs
    - Other floors: spawn is placed adjacent to the appropriate staircase (downstairs by default; falls back to upstairs if needed)
  - Random treasure chest placement in quadrants with sensible fallbacks

- Map / UI:
  - Floor view supports a configurable zoom factor and now defaults to 2× zoom
  - Map view centers on the player and no longer relies on scrollbars for the main floor viewport

### Fixed

- **Treasure Chest Rendering**: Fixed chests not appearing on map

  - `MapPrinter.renderWithPlayer()` now checks `tile.getLoot()` and renders 'C' for treasure chests
  - Added 'C' → "common_treasure_chest" sprite mapping in `RecallDungeon`
  - Treasure chests now properly visible in floor view

- Fixed crash when returning to main menu and selecting a different character (double-dispose of `spriteAtlas`)
- Fixed player sprite rendering by preferring prebuilt atlas files and augmenting with standalone PNGs referenced in `assets/tiles.json` at runtime

### Changed

- **LootPoolLoader Architecture**: Enhanced lookup system

  - Added static `HashMap` storage for loot pools (by ID and by name)
  - Name lookup is case-insensitive and space-insensitive
  - Loot pools now loaded and cached at startup for efficient retrieval

- `AtlasBuilder.loadWithFallback()` now prefers a prebuilt atlas (`sprites.atlas`/`tiles.atlas`) and merges any standalone PNG regions found via `assets/tiles.json` so runtime-built regions do not override missing prepacked regions
- `RecallDungeon` logs loaded atlas regions and attempts a best-effort region name match for the player's sprite (helps diagnose mismatches such as `player_biggles`)
- `MapActor` now exposes `setZoomFactor()` and computes cell sizes from the zoom so the scene2d layout reflects actual tile dimensions
- `MapPrinter` now consistently checks for loot in both `printWithPlayer()` and `renderWithPlayer()` methods

### Technical / Notes

- `BSPRoomGenerator` places stairs, spawn, and treasure chests according to quadrant rules and links tile neighbors as before
- Treasure chests use `spawnTreasureChest(LootPool)` method which sets `tile.loot` and `tile.isOccupied`
- The map rendering pipeline draws `TextureAtlas` regions when available and falls back to font glyphs; debug logging was added to surface missing region lookups at runtime
- Added debug logging to `BSPRoomGenerator` to track chest placement and loot pool loading
- These changes are intended to be backwards compatible with prebuilt atlases and existing map-rendering code, while providing better runtime robustness for packaged assets

## [v0.1.01] - 2025-11-05

### Added

- Procedural dungeon generation foundation:

  - `MapGenerator` interface (strategy contract for generators)
  - `BSPRoomGenerator` concrete generator (simple room/floor generator used as a placeholder)
  - Game now procedurally generates floors for depth -10 to +10 instead of relying on text files
    - Floors 0 to ±4 are generated at random sizes between 20x20 and 30x30
    - Floors ±5 to ±10 are generated at random sizes between 30x30 and 40x40
  - Two-tile-thick outer walls are applied to every generated floor
  - Every generated floor has an upstairs (^) and downstairs (v) placeholder tile

- Reveal / hide floor UI shortcuts in the floor view:

  - Press `R` to reveal the entire current floor (sets `discovered = true` for every tile)
  - Press `H` to hide the entire current floor (sets `discovered = false` for every tile)

- Unit and integration tests for the generator:

  - `BSPRoomGeneratorTest` (determinism, sizing, walls, stairs, neighbors, interior walkability)
  - `DungeonGenerationIntegrationTest` (basic end-to-end generation checks)

- Documentation: Added `PROCEDURAL_GENERATION.md` describing the generator architecture and how to add new generators.

### Changed

- The game's `loadDungeon()` no longer loads floor text files by default — text-file loading was commented out and replaced by procedural generation in `Game` (kept commented for reference).
- Generation uses a fixed seed for determinism by default (seed may be made configurable later).

### Files created / modified (high level)

- Created: `src/main/java/com/bapppis/core/dungeon/generator/MapGenerator.java`
- Created: `src/main/java/com/bapppis/core/dungeon/generator/BSPRoomGenerator.java`
- Modified: `src/main/java/com/bapppis/core/game/Game.java` (replaced map file loading with generator loop)
- Modified: `src/main/java/com/bapppis/core/dungeon/Floor.java` (added `revealAll()` and `hideAll()` helpers)
- Modified: `src/main/java/com/bapppis/core/gfx/RecallDungeon.java` (added `R` and `H` key handlers to reveal/hide floor and refresh UI)
- Created: `src/test/java/com/bapppis/core/dungeon/generator/BSPRoomGeneratorTest.java`
- Created: `src/test/java/com/bapppis/core/dungeon/generator/DungeonGenerationIntegrationTest.java`
- Created: `PROCEDURAL_GENERATION.md`

### Notes

- This is an initial, opinionated implementation intended as a minimal, testable replacement for text-file maps so other systems (UI, pathfinding, spawning) can work against a deterministic runtime-generated floor.
- The `BSPRoomGenerator` is intentionally simple (fills interior with floors and places placeholder stairs); future work should replace it with a more advanced algorithm (BSP rooms/corridors, cellular automata, or hybrid approaches) and add content placement (monsters, chests, events).
- Text-file map parsing is left in the repository (commented in code) so hand-authored maps remain available for future use.
  Text-file map parsing is left in the repository (commented in code) so hand-authored maps remain available for future use.

### Fixed

- Player class discovery bug: player classes were being loaded from nested JSONs under `data/creatures/player_classes/` (for example `talent_trees/*`). This caused entries like "Rogue Talents" to appear in the class selection list. The loader now only loads JSON files directly in `data/creatures/player_classes/` so talent tree files are not treated as classes.

### Changed

- UI: removed the "Skip (No Class)" option from the class selection screen so players must pick a class during character setup.

## [v0.1.00] - 2025-11-03

### Added - Rogue class

- Stealthy rogue, who is about quick strikes, bleeds, poisons and dodging

### Added - Spells

- Serpent's Fang, an ability that does nature damage and has a chance to poison the enemy

### Added - Build Up

- Nature build up now poisons enemy

### Added - Debuff

- Poisoned 1
- Poisoned 2

#### Changes

- Adjusted spells so they can use stamina too instead of mana

### Added - Spell stamina support & engine changes

- Spells can now declare a `staminaCost` (optional) in addition to or instead of `manaCost`.
- `Spell.java` and the spell engine now validate and deduct mana and/or stamina as required when casting.
- Existing spells without `staminaCost` remain unchanged.

### Added - Combat bonuses for classes & talents

- Classes (`PlayerClass`) and talent rewards (`TalentChoice`) may now grant combat modifiers: `crit`, `dodge`, `block`, `accuracy`, and `magicAccuracy` (plus existing stat/res/HP/resource bonuses).
- `PlayerClassService` and `TalentTreeService` apply and reverse these bonuses using creature base-modifier helpers so derived stats recalc correctly.

### Changed - ID index and tooling

- The ID generator (`scripts/generate_ids.py`) and `IDS.md` were updated to include missing entries discovered during a regeneration run (player classes, talent trees, loot pools, monster pools, and new property entries such as Poisoned).
- `src/main/resources/data/IDS.md` was merged with generated output to add the missing entries and a backup of the previous `IDS.md` was written.

## [v0.0.99] - 2025-11-02

### Added - Talent Tree System (Semi-Ready)

**Note**: Core architecture is complete and functional, but current content is placeholder and subject to change.

#### Overview

Implemented a Skyrim-style talent tree progression system that allows players to specialize their classes through branching paths. Each player class can have an associated talent tree with diverging paths leading to unique capstones. Players spend talent points earned through leveling to unlock nodes, making meaningful choices that permanently enhance their character.

**Architecture:**

- JSON-driven system (ID range: 70000-70999)
- POJOs: `TalentChoice` (individual options), `TalentNode` (prerequisites + choices), `TalentTree` (full tree tied to class)
- `TalentTreeLoader`: Loads trees, provides lookup by ID or by class ID
- `TalentTreeService`: Validates unlocks, applies rewards, handles resets
- Player integration: Tracks unlocked nodes, spends talent points per unlock

**How It Works:**

_Progression System:_

- Players earn talent points through leveling (configurable per class, default 1/level)
- Each node costs 1 talent point to unlock
- Nodes can have 1-3 mutually exclusive choices (most have 1, specializations have multiple)
- Prerequisites enforce tier progression (e.g., must unlock tier 1 before tier 2)
- Paths can be mixed if prerequisites allow (not restricted by path grouping)

_Reward Variety:_

- Same reward types as class system: stats, resistances, HP/Mana/Stamina bonuses
- Regeneration bonuses (HP/Mana/Stamina regen)
- Granted traits (properties automatically applied)
- Unlocked spells (granted when node unlocked)
- Rewards stack with class bonuses and are applied immediately

_Reset Functionality:_

- **Simple Reset**: Clears unlocked nodes and refunds talent points (stat bonuses remain until player reinitializes)
- **Full Reset**: Removes class, clears nodes, re-applies class to completely recalculate stats
- Useful for experimentation and respeccing builds

_UI-Ready Design:_

- Nodes have row/column positioning for visual layout
- Path grouping for logical organization (e.g., "Holy", "Protection", "Combat")
- Prerequisite arrows can be drawn between nodes
- Choice nodes show branching options visually

_Example Structure:_

- Paladin talent tree with 3 diverging paths (Holy, Protection, Combat)
- 13 total nodes: 1 root, 3 paths with 4 tiers each
- Each path has specialization choice nodes (3 options) and unique capstones
- Capstones provide major power spikes (high stat bonuses, powerful traits/spells)

#### Technical Integration

- **AllLoaders.java**: Added `TalentTreeLoader` initialization (loads after `PlayerClassLoader`)
- **Player.java**: Added `Set<String> unlockedTalentNodes` to track progression
- **IDS.md**: Added talent tree section (70000-70999 range)
- **generate_ids.py**: Added range validation for talent tree IDs
- **format_jsons.py**: Added canonical ordering for talent tree JSON files
- **TalentTreeServiceTest.java**: Comprehensive test coverage (unlock, prerequisites, reset, progression)

## [v0.0.98] - 2025-11-01

### Added - Player Class System

#### Core Implementation

- **PlayerClass.java**: Complete POJO representing player classes

  - Core fields: id, name, description, tooltip
  - Stat bonuses: Map<Stats, Integer> for permanent stat modifications
  - Resistances: Map<Resistances, Integer> for resistance modifications
  - Resource bonuses: maxHpBonus, maxManaBonus, maxStaminaBonus
  - Regeneration bonuses: hpRegenBonus, manaRegenBonus, staminaRegenBonus
  - Granted properties: List<String> of trait names automatically applied
  - Unlocked spells: List<String> of spell names granted at class selection
  - Talent system: talentPointsPerLevel (default 1)
  - Level progression: Map<Integer, LevelUnlock> for level-specific unlocks
  - LevelUnlock inner class: Contains spells, properties, stat bonuses, resistances, and resource bonuses

- **PlayerClassLoader.java**: JSON loader with Gson + ClassGraph

  - Loads from `data/creatures/player_classes/*.json`
  - Dual lookup: getPlayerClassById() and getPlayerClassByName() (case-insensitive)
  - Uses ResistancesDeserializer for proper resistance enum handling
  - Provides getAllClasses(), hasClass(), getClassCount() utilities

- **PlayerClassService.java**: Business logic for class management

  - applyClass(Player, PlayerClass): Applies all class bonuses to player
    - Stat bonuses via player.increaseStat()
    - Resistance modifications via player.setResistance()
    - HP/Mana/Stamina bonuses (HP fully implemented)
    - Granted properties via PropertyLoader.getPropertyByName() → player.addProperty()
    - Unlocked spells via player.learnSpell()
    - Sets playerClassId for persistence
  - removeClass(Player): Reverses all class bonuses
    - Stat bonuses removed via player.decreaseStat()
    - Resistances restored to original values
    - Properties removed via player.removeProperty()
    - Spells remain learned (design choice)
    - Clears playerClassId
  - handleLevelUp(Player, int): Processes level-based unlocks
    - Grants talent points based on talentPointsPerLevel
    - Applies level-specific unlocks from class definition
    - Recursive application of stat bonuses, traits, spells, resources

- **Player.java**: Extended with class system fields
  - playerClassId (Integer): Tracks assigned class ID for persistence
  - talentPoints (int): Accumulated points for talent spending
  - learnedTalents (Set<String>): Tracks spent talents (prevents duplicates)
  - addTalentPoint(): Increments available points
  - spendTalentPoint(String): Decrements points, adds to learned set
  - hasTalent(String): Checks if talent already learned
  - Getters/setters for all class-related fields

#### User Interface

- **PlayerClassSelectionScreen.java**: LibGDX Scene2D/VisUI class selection interface

  - Split layout: Class list (left) + Details panel (right)
  - Class list: Scrollable VisList with all available classes
  - Details panel displays:
    - Class name (highlighted in cyan)
    - Description text (wrapped, 300px width)
    - Stat bonuses with +/- formatting
    - Resistance modifications with %
    - Resource bonuses (HP, Mana, Stamina, Regen)
    - Granted traits listed
    - Starting spells listed
  - Action buttons:
    - "Select Class": Applies chosen class and continues
    - "Skip (No Class)": Continues without class selection
  - Dynamic updates: Details refresh when different class selected
  - Callback integration: onComplete() triggers game start after selection

- **RecallDungeon.java**: Integrated class selection into game flow
  - Modified character selection to call showClassSelection() after player pick
  - Added showClassSelection(Player): Creates and displays class selection screen
  - Added startGameWithPlayer(Player): Initializes game after class selection
  - Flow: Main Menu → Character Select → Class Select → Game Start

#### Data and Configuration

- **Paladin.json**: Example player class implementation

  - ID: 60000 (first in player class range)
  - Stat bonuses: STR +2, CON +1, WIS +1
  - Resistances: LIGHT +10, DARKNESS -5
  - Resources: HP +20, Mana +10, HP Regen +1
  - Granted traits: ["Bleed Immunity"]
  - Starting spells: ["Shield of Light"]
  - Level unlocks:
    - Level 3: Poison Immunity trait
    - Level 5: +1 STR, +10 HP
    - Level 7: Smite spell unlock
    - Level 10: +1 CON, +1 WIS, +5 Light resistance, +15 HP
  - Description: "A holy warrior devoted to protecting the innocent..."

- **IDS.md**: Added Player Classes section

  - Range: 60000-60999 (1000 classes allocated)
  - Description: "Player classes define character archetypes with stat bonuses, resistances, granted traits, unlocked spells, and level-based progression. Only Player creatures can have classes."
  - Entry: 60000 — Paladin — data/creatures/player_classes/Paladin.json

- **AllLoaders.java**: Integrated PlayerClassLoader into loading pipeline
  - Load order: PropertyLoader → SpellLoader → **PlayerClassLoader** → ItemLoader → LootPoolLoader → CreatureLoader
  - Added private static PlayerClassLoader playerClassLoader field
  - Added getPlayerClassLoader() static getter
  - Try-catch wrapping for safe initialization

#### Python Tooling

- **generate_ids.py**: Added player_classes range validation

  - Range rule: (("creatures", "player_classes"), (60000, 60999, "Player Classes (60000-60999)"))
  - Positioned after spells (50000-50999), before items (20000-29999)
  - Validates class IDs fall within allocated range

- **format_jsons.py**: Added player class JSON formatting support
  - CANON_PLAYERCLASS_ORDER: 16-field canonical ordering
    - Order: id, name, description, statBonuses, resistances, maxHpBonus, maxManaBonus, maxStaminaBonus, hpRegenBonus, manaRegenBonus, staminaRegenBonus, grantedProperties, unlockedSpells, talentPointsPerLevel, levelUnlocks, tooltip
  - Detection: Recognizes `creatures/player_classes/` path → kind='player_class'
  - Transform: Applies CANON_PLAYERCLASS_ORDER when formatting player class JSONs

#### Testing

- **PlayerClassTest.java**: Comprehensive test suite (8 passing tests)
  - testPlayerClassLoaderExists: Verifies loader initialization
  - testPaladinClassLoads: Loads Paladin by ID 60000, checks name and description
  - testPaladinStatBonuses: Validates STR +2, CON +1, WIS +1
  - testPaladinResistances: Validates LIGHT +10, DARKNESS -5
  - testApplyPaladinClassToPlayer: Tests full class application, stat increases
  - testRemoveClass: Validates class removal restores original stats
  - testLevelUpGrantsTalentPoints: Confirms talent point award on level-up
  - testTalentPointManagement: Tests add/spend/duplicate prevention logic
  - Test setup: @BeforeAll with AllLoaders.loadAll()
  - Location: src/test/java/com/bapppis/core/Creature/PlayerClassTest.java

#### Documentation

- **README.md**: Added "Player Class System" section under Features
  - Overview of JSON-driven class system
  - Stat bonuses, resistance modifications, resource bonuses
  - Granted traits and starting spells
  - Level-based progression system
  - Talent point customization
  - Class selection UI description
  - Paladin example mentioned

### Technical Details

- **ID Range**: 60000-60999 (1000 player classes allocated)
- **Data Location**: src/main/resources/data/creatures/player_classes/\*.json
- **Dependencies**: Uses PropertyLoader (for traits), SpellLoader (for spell grants)
- **Design Pattern**: POJO + Loader + Service (matches Spell/Property/Item patterns)
- **Known Limitations**:
  - Mana/Stamina bonus setters not yet implemented in Creature class (logged for now)
  - Regen bonus setters not yet implemented (logged for now)
  - Currently only HP bonus fully applies via setBaseHp() + updateMaxHp()

### Files Modified

- src/main/java/com/bapppis/core/creature/PlayerClass.java (NEW)
- src/main/java/com/bapppis/core/creature/PlayerClassLoader.java (NEW)
- src/main/java/com/bapppis/core/creature/PlayerClassService.java (NEW)
- src/main/java/com/bapppis/core/creature/Player.java (MODIFIED - added class fields)
- src/main/java/com/bapppis/core/gfx/PlayerClassSelectionScreen.java (NEW)
- src/main/java/com/bapppis/core/gfx/RecallDungeon.java (MODIFIED - integrated class selection)
- src/main/java/com/bapppis/core/AllLoaders.java (MODIFIED - added PlayerClassLoader)
- src/main/resources/data/creatures/player_classes/Paladin.json (NEW)
- src/main/resources/data/IDS.md (MODIFIED - added Player Classes section)
- src/test/java/com/bapppis/core/Creature/PlayerClassTest.java (NEW)
- scripts/generate_ids.py (MODIFIED - added player_classes range)
- scripts/format_jsons.py (MODIFIED - added CANON_PLAYERCLASS_ORDER)
- README.md (MODIFIED - added Player Class System documentation)

### Testing Results

- All 8 PlayerClass tests passed successfully
- Integration with existing systems verified
- Property granting confirmed working (Bleed Immunity applied)
- Stat bonuses confirmed (+2 STR, +1 CON, +1 WIS applied to player)
- Resistance modifications confirmed (+10 Light, -5 Darkness applied)
- HP bonus confirmed (+20 HP added to base HP)
- Class removal confirmed (stats restored to baseline)
- Talent point system confirmed working

## [v0.0.97] - 2025-10-31

### Added - Spell System Integration

#### Documentation

- **IDS.md**: Updated spell ID range from `0000-0999` to `50000-50999` (1000 IDs allocated)

  - Added three spell entries: Fireball (50000), Elemental Chaos (50001), Shield of Light (50002)
  - Updated ID range documentation block with correct spell range

- **SYSTEM_REFERENCE.md**: Added comprehensive "Spell System" section (~200+ lines)

  - Overview of spell architecture and mechanics
  - Detailed field documentation (core, damage, combat, property fields)
  - Casting mechanics: mana costs, stat bonus selection, to-hit resolution
  - Damage calculation and multi-element support
  - Buildup system integration
  - Property application (buff and onHit properties)
  - Spell types (damage-based, buff-only, hybrid)
  - Creature integration via SpellReference with weighted selection
  - Spell loading and testing hooks
  - Added to Table of Contents at position 2

- **README.md**: Added spell system to project documentation
  - Updated Creature System section with spell-related features
  - Added new "Spell System" section with 10+ feature highlights
  - Updated "Asset-driven Design" section to include spells
  - Updated SYSTEM_REFERENCE.md reference to mention spell mechanics

#### Python Scripts

- **generate_ids.py**: Added spell ID range validation

  - New range rule: `50000-50999` for spells
  - Validates that spell JSON files use IDs within the correct range

- **format_jsons.py**: Added spell support with canonical field ordering

  - New `CANON_SPELL_ORDER` list with 24 fields
  - Added spell file kind detection
  - Updated `transform()` function to handle 'spell' kind
  - Updated `ID_RANGES` from `(0, 999)` to `(50000, 50999)`

- **generate_tooltips.py**: Complete rewrite of spell tooltip generation
  - New comprehensive `generate_spell_tooltip()` function
  - Mana cost display (always shown first)
  - Damage calculation with stat bonuses (shows "X-Y + 5 \* STAT bonus damage")
  - Multi-element damage support (up to 4 damage types)
  - Accuracy, critical chance modifiers
  - Property application with detailed descriptions
  - Buff-only spell support
  - Damage multiplier display
  - Property name normalization (handles "Burning1" vs "Burning 1", "BleedImmunity" vs "Bleed Immunity")
  - Improved `load_property_by_name()` with fuzzy matching
  - Integrated into `--apply` workflow alongside weapons
  - Tooltips now match weapon tooltip style and comprehensiveness

#### Testing

- **SpellTest.java**: Created comprehensive test suite with 16 test methods
  - `testSpellLoading`: Verifies spell loading by ID and name
  - `testSpellManaCostChecking`: Tests mana deduction and insufficient mana handling
  - `testSpellStatBonusSelection`: Tests best stat selection from list
  - `testSpellDamageCalculation`: Verifies damage is dealt
  - `testMultiElementSpell`: Tests multi-damage-type spells
  - `testBuffOnlySpell`: Tests buff application without target
  - `testSpellPropertyApplication`: Tests onHitProperty application
  - `testCreatureSpellIntegration`: Tests spell loading in creatures
  - `testSpellDamageMultiplier`: Tests damage multiplier field
  - `testSpellCritMod`: Tests crit modifier parsing
  - `testSpellAccuracy`: Tests accuracy field
  - `testSpellBuildUpMods`: Tests all 4 buildup mod fields
  - `testSpellWithNoStatBonuses`: Tests edge case with empty stat list
  - `testSpellHasDamage`: Tests hasDamage() helper
  - `testSpellManaDeductionOnMiss`: Tests mana cost paid even on miss
  - `testSpellWeightInCreature`: Tests SpellReference weight loading
  - `testMultipleSpellCasts`: Tests casting same spell multiple times
  - `testSpellToString`: Tests toString() debug output

#### Spell Data Updates

- **Fireball.json**: Updated tooltip with comprehensive details

  - Shows mana cost (20 mana)
  - Displays damage range with stat bonuses (3-18 + 5 \* INT/WIS bonus fire damage)
  - Shows modifiers (chance to inflict Burning1, +10% crit, +5 accuracy)
  - Includes Burning 1 property description

- **Elemental Chaos.json**: Updated tooltip for multi-element spell

  - Shows mana cost (35 mana)
  - Displays all three damage types (fire, ice, lightning)
  - Shows stat bonus (INT) for all damage components
  - Includes +5% crit chance
  - Shows damage multiplier (0.8x)

- **Shield of Light.json**: Updated tooltip for buff-only spell
  - Shows mana cost (15 mana)
  - Displays buff effect (BleedImmunity)
  - Includes property description (Immune to Bleed condition)

### Changed

- Spell tooltips now follow the same comprehensive format as weapon tooltips
- Property lookups now support both exact names and normalized names (case-insensitive, space-insensitive)
- Spell system fully integrated with existing project conventions and tooling

### Technical Details

- **Testing Framework**: JUnit 5 with BeforeAll loader setup
- **API Patterns**: Uses `CreatureLoader.getCreatureById()`, `getBuff(id)`, `getDebuff(id)`
- **Field Ordering**: 24 canonical fields for spell JSON files
- **ID Allocation**: 3 spells created (50000-50002), 997 IDs available (50003-50999)
- **Stat Bonus Format**: "5 \* STAT bonus" matches weapon tooltip format
- **Property Matching**: Fuzzy matching handles naming inconsistencies (spaces, case)

### Known Issues

- Spell tooltip field in JSON is array format (matching weapons) but Spell.java expects String type
- Tests currently failing due to tooltip type mismatch - needs resolution in future update

---

## [v0.0.96] - 2025-10-30

### Added

- **Dizzy Debuff**: New condition that reduces block chance and dexterity
  - Dexterity loss indirectly reduces dodge and attacking if DEX is the attack stat
- **Property Application on Attacks**: Added `physicalOnHitProperty` and `magicalOnHitProperty`
  - Attacks can now apply debuffs to enemies on hit
  - Enables status effect buildup from weapon strikes

### Changed

- Updated README.md and SYSTEM_REFERENCE.md with attack property mechanics
- Tooltip and format generation scripts now support on-hit properties

### Fixed

- Updated tests to verify new on-hit property features

---

## [v0.0.95] - 2025-10-29

### Added

- **Dual Damage Types for Weapons**: Weapons and attacks now support secondary damage types
  - Added `damageType2` and `magicElement2` fields
  - Physical weapons can deal two types of physical damage (e.g., slashing + piercing)
  - Magic weapons can deal two types of elemental damage
- Supporting logic in weapon and attack Java classes

### Changed

- Updated `generate_tooltips.py` to display both damage types
- Updated SYSTEM_REFERENCE.md with dual damage type documentation

### Testing

- Added comprehensive tests for dual damage type functionality

---

## [v0.0.94] - 2025-10-28

### Added

- **Morningstar**: New piercing weapon with unique attack patterns

---

## [v0.0.93] - 2025-10-27

### Added

- **Kobold Species**: New creature species with racial traits
  - Updated folder structure for species organization
  - Added Kobold Warrior to enemy roster
- **Burning Debuff**: Fire damage now applies burning condition over time
- **Necrotic Plague**: Darkness damage buildup inflicts necrotic plague debuff

### Changed

- Added missing `physBuildUp` and `magicBuildUp` fields to creature JSONs
- Adjusted default true resistance to 50% (was 100%)

### Fixed

- Corrected folder structure and ID assignments for new species

---

## [v0.0.92] - 2025-10-26

### Added

- **Sword of Windfury**: New arcane weapon using INT/CHA for bonuses
- **Automated Tooltip Generation**: Python script generates weapon tooltips automatically
  - Calculates damage ranges with stat bonuses
  - Includes attack percentages based on weights
  - Displays modifiers and special properties

### Changed

- Arcane weapons now use Intelligence or Charisma as stat bonus (highest is selected)
- Adjusted weapon tooltips and stats for balance

### Fixed

- `format_jsons.py` now properly formats all weapon files

---

## [v0.0.91] - 2025-10-25

### Added

- **Steel Flanged Mace**: New blunt weapon for crushing damage
- **Staff of Flames**: New magical staff for fire-based spellcasting

---

## [v0.0.90] - 2025-10-24

### Added

- **Resistance Buildup System**: Comprehensive elemental resistance mechanics
  - `RESISTANCE_BUILDUP_CONSTANT` determines buildup rate (default: 20)
  - `addBuildUp()` method adds resistance buildup based on damage type
  - Buildup persists if actively receiving that damage type
  - Buildup decays by 10% per round when not receiving damage
- **Resistance Overload**: When buildup reaches 100%, applies debuff and resets
- **BuildUp Multipliers**: Per-attack buildup modifiers for fine-tuning
  - `physBuildUpMod` and `magicBuildUpMod` for each attack
  - Allows weapons to specialize in applying or avoiding status effects

### Changed

- Adjusted JSON formatting for consistency
- Refactored item printing into `ItemPrinter` utility class
- Default true resistance changed to 50%

### Testing

- Added comprehensive buildup and overload tests
- Updated SYSTEM_REFERENCE.md with combat mechanics documentation

---

## [v0.0.89] - 2025-10-23

### Added

- **Resistance Buildup Enum**: `ResBuildUp` tracks elemental exposure
  - Tracks buildup for each damage type (SLASHING, FIRE, ICE, etc.)
  - When buildup reaches 100%, applies corresponding debuff
  - Example: Slashing buildup → Bleed condition
- Helper functions to modify buildup values
- Automatic buildup decay: -10% per round × resistance value

### Testing

- Added BuildUpTest to verify functionality

---

## [v0.0.88] - 2025-10-22

### Added

- **SYSTEM_REFERENCE.md**: Comprehensive technical documentation
  - Documents how different game systems interact
  - Architecture and design decisions
  - Formula references for calculations
- **Utility Print Function**: `creature.printLnFields()` for debugging
  - Prints all creature fields for testing

### Changed

- **HP System Overhaul**: Renamed `hplv1Bonus` to `hpDice`
  - HP calculations increased ~5× for better scaling
  - Formula: `baseHp + (hpDice × level) + (CON bonus × level)`
- **Resistance Utility Helpers**: Quick resistance modification functions
  - `modifyFireResistance(creature, 50)` adds 50 to fire resistance
  - Similar helpers for all resistance types
- Updated all enemy creature JSONs to new HP system
- Added setters for base creature variables (used by CreatureType and Species)

### Refactoring

- Moved magic and melee weapons to separate sub-packages

---

## [v0.0.87] - 2025-10-21

### Added

- **Species and CreatureType Hooks**: Post-JSON-load modification system
  - Species can override JSON values after loading
  - CreatureType can apply class-specific modifications
- **Property Removal System**: JSONs can specify properties to remove
  - Useful when species/class grant properties that specific creatures shouldn't have

### Changed

- **CreatureLoader Priority**: Species and CreatureType now override JSON values
- Manual-test folder structure now mirrors main test structure

### Testing

- Updated ID tests for current layout
- Comprehensive creature hierarchy tests
- Added item loading and equipment tests

### Fixed

- Loader logic corrected for creature hierarchy (Creature ← Type ← Species)

---

## [v0.0.86] - 2025-10-20

### Added

- **Equipment Subclasses**: Separated armor into specialized types
  - `Helmet`: Head protection
  - `Armor`: Chest protection
  - `Legwear`: Leg protection
  - `Offhand`: Shields and secondary items

### Changed

- Reorganized file structure: enums moved to dedicated folder
- Updated Python helper scripts for new structure

---

## [v0.0.85] - 2025-10-20

### Added

- **Weapon Class Hierarchy**: Comprehensive weapon categorization
  - **Melee Weapons**: `PiercingWeapon`, `BluntWeapon`, `SlashingWeapon`
  - **Ranged Weapons**: `Bow`, `Crossbow`
  - **Magic Weapons**: `Staff`, `ArcaneWeapon`

### Changed

- Updated all weapon JSONs to specify weapon subclass
- Adjusted tests for weapon polymorphism and edge cases

---

## [v0.0.84] - 2025-10-19

### Added

- **EquipmentUtils**: Centralized equipment management utilities
  - Moved equipment printing from Creature class
- **Expanded Creature Types**: Significantly more variety in creature taxonomy

### Changed

- **IDS.md Overhaul**: Complete reorganization of ID system
- **Creature Inheritance Refactor**: New hierarchy system
  - `Creature` ← `ENEMY`/`NPC`/`PLAYER` ← `CreatureType` ← `Species`
  - Enables racial traits and class features
- Removed obsolete comments and cleaned codebase
- Updated README.md documentation

### Fixed

- Species loading bugs resolved
- Creature type hierarchy now works correctly

---

## [v0.0.83] - 2025-10-18

### Added

- Helper printer methods for equipped items
- Case-insensitive `unequipByName()` method (ignores spaces too)

### Testing

- Expanded equipment management tests

---

## [v0.0.82] - 2025-10-18

### Added

- **Properties for Items**: Items can now grant buffs/debuffs when used
  - Consumables can apply temporary effects
  - Equipment can grant passive bonuses
- **Name-Based References**: Items, creatures, and properties use names instead of IDs
  - Improves JSON readability
  - Easier content creation

### Changed

- **Equipment.java Split**: Separated into multiple item type classes
  - Better organization and maintainability
- **EquipmentManager → ItemManager**: Renamed to reflect broader scope
- **Sprite System**: AtlasBuilder.java generates sprite atlas from PNGs

### Testing

- Updated all tests for new item system
- Added edge case coverage

### Documentation

- Major README.md update with new systems

### Fixed

- Sprite loading bugs in map view
- Loader edge cases

---

## [v0.0.81] - 2025-10-17

### Added

- **Comprehensive Test Suite**: Massive expansion of unit tests
  - Edge case coverage
  - Integration tests
- **Manual Test Separation**: `manual-test` folder for interactive tests
  - Excluded from `mvn test` runs

### Changed

- Updated `pom.xml` build configuration
- Project structure optimization for faster compilation

### Documentation

- Updated README.md with testing information

### Fixed

- Various edge case bugs discovered through testing

---

## [v0.0.80] - 2025-10-17

### Added

- **Elemental Damage for Properties**: Properties can deal typed damage per turn
  - `damageType` field specifies element (Fire, Ice, etc.)
  - `damageDice` field defines damage roll (e.g., "1d6+2")
  - Damage respects creature resistances
- **Name-Based Entity Lookup**: Search by name in addition to ID
  - Space-insensitive: "NecroticPlague" matches "Necrotic Plague"
- **Necrotic Plague Debuff**: Example of damage-over-time property

### Changed

- **Loader Optimization**: All loaders run only once at startup
  - Significantly improved performance
- Removed test JSON files (using real data)

### Testing

- Added property damage tests with resistance checking

---

## [v0.0.79] - 2025-10-17

### Added

- **Resistance Deserializer**: Unified resistance parsing across all loaders
  - Consistent resistance handling for creatures, items, properties
- **ResistanceUtil**: Damage calculation after resistance
  - Formula: `finalDamage = baseDamage × (2 - resistance / 100)`

### Changed

- Attacks now use Resistance enums instead of strings
- Moved enums to dedicated package: `Resistances`, `Stats`, `Type`, `Size`, `CreatureType`
- Updated README.md file structure

### Refactoring

- Updated all code and tests for enum-based system
- Cleaner type safety and autocompletion

---

## [v0.0.78] - 2025-10-17

### Removed

- Leftover attack report code from Creature.java

### Documentation

- Updated README.md

---

## [v0.0.77] - 2025-10-15

### Added

- **AttackEngine**: Centralized combat mechanics
  - Moved attacking methods from Creature.java
  - Attack reports now generated by engine
- **RandomProvider**: Simplified random number generation utility
- **EquipmentManager**: Equipment handling separated from Creature
  - Equip/unequip logic moved for better separation of concerns
- **PropertyManager**: Centralized property (buff/debuff/trait) management
- **Accuracy System**: Fine-tune hit chances
  - `accuracy` and `magicAccuracy` for general bonuses
  - Per-attack accuracy modifiers

### Changed

- Creature.java significantly simplified (logic moved to managers)

### Documentation

- Updated README.md with new architecture

---

## [v0.0.76] - 2025-10-14

### Added

- **Expanded Property Effects**: Properties can now modify:
  - `staminaRegen`, `crit`, `dodge`, `block`, `magicResist`
  - `maxHp`, `maxStamina`, `maxMana`
  - Percentage-based HP/Stamina/Mana modifications
- **Test Properties**: Test Buff and Test Debuff for development

### Changed

- Python formatter now handles property files
- Updated IDS.md

---

## [v0.0.76] - 2025-10-13

### Added

- **Property ID System**: Multiple properties of same type can exist
  - IDs differentiate similar buffs (Health Regen 1, Health Regen 2)
- **Bleed Debuff**: Damage-over-time effect (negative `regenHp`)
- **Name Lookup System**: Reference entities by name or ID
  - Items, creatures, properties all searchable by name

### Changed

- **Property Refactor**: Unified Buff, Debuff, and Trait classes
  - Shared code consolidated
  - Cleaner inheritance structure

### Testing

- Added NameLookupTest for search functionality

---

## [v0.0.75] - 2025-10-12

### Added

- **Health Regen Buff**: Healing-over-time property system
  - Duration-based buffs
  - Automatic HP restoration per turn
- **Property Duration**: Time-limited effects
  - `onTick()` method for turn-based updates
- Property `toString()` methods for debugging

### Testing

- PropertyTest.java validates health regen mechanics

---

## [v0.0.74] - 2025-10-11

### Removed

- PropertyImpl.java (logic distributed to Property subclasses)

---

## [v0.0.73] - 2025-10-11

### Added

- **Three Property Types**: Separated into distinct implementations
  - `Buff`: Positive temporary effects
  - `Debuff`: Negative temporary effects
  - `Trait`: Permanent passive abilities
- **AllLoaders**: Single command loads all game data
- **PropertyIntegrationTest**: End-to-end property testing
- Debug logging system for AI assistance

### Changed

- **PropertyManager → PropertyLoader**: Naming consistency with other loaders
- **Immunity Consolidation**: Immunities are now buffs, not separate type
  - Updated ID ranges in IDS.md
  - Migrated immunity property IDs and types
- **Python Formatter**: Now handles Stats and Resistances formatting

### Fixed

- Player selection bugs
- Data loading edge cases

---

## [v0.0.72] - 2025-10-10

### Added

- **Level-Up System**: Stat point allocation
  - `statPoints` granted per level
  - Spend points to increase attributes
- **LevelUtil**: Level progression calculations
  - Experience requirements scale with level
- **Turn Passing**: Foundation for turn-based mechanics

### Changed

- Consistent method naming for HP, Stamina, and Mana
- Updated toString() to show XP progress (e.g., "1250/2000 XP to level 5")
- Minimum stat value enforced (can't go below 1)

### Documentation

- Updated README.md file structure

---

## [v0.0.71] - 2025-10-09

### Added

- **Resource Regeneration**: HP, Stamina, and Mana regen stats
  - `baseRegenHp`, `baseRegenStamina`, `baseRegenMana`
  - Equipment can modify regeneration rates
- **StatUtil**: Helper functions for stat manipulation
  - Simplifies level-up stat allocation

### Changed

- Regeneration mechanics prepared (implementation pending turn system)

### Documentation

- Updated README.md

---

## [v0.0.70] - 2025-10-08

### Added

- **Magic Resistance**: Chance to resist magical damage
  - `baseMagicResist` and `equipmentMagicResist` tracking
  - Equipment can grant magic resist
  - Separate from elemental resistances
- **ResistanceUtil**: Helper for resistance type categorization
  - Determines if resistance is "MAGICAL", "PHYSICAL", or "TRUE"

### Changed

- Minor JSON updates (added levels and magicResist to enemies)
- Attack reports now show physical vs magical damage separately
- Attack logic differentiates damage types for resistance application

### Refactoring

- Moved utilities out of Creature.java for cleaner code

---

## [v0.0.69] - 2025-10-07

### Added

- **Experience Rewards**: Enemies grant XP when defeated
  - `totalXp` field in creature JSONs
  - Combat.java transfers XP to victor

---

## [v0.0.68] - 2025-10-07

### Added

- **Magic Stat Bonuses**: Weapons can use multiple stats for magic damage
  - `magicStatBonus` array (e.g., ["INTELLIGENCE", "WISDOM"])
  - Best stat is automatically selected
- **Physical Stat Bonuses**: Multiple physical stats per weapon
  - `statBonuses` array for physical attacks
  - Enables Finesse (STR or DEX) weapons
- Item printing now shows all attacks and properties
- Attack.java `toString()` method for debugging

### Changed

- Removed "isVersatile" and "isFinesse" (now just "versatile" and "finesse")
- Updated weapon JSONs with new stat system
- Stat bonuses multiply damage more effectively (player stats matter more)
- Python formatter now sorts JSON fields

### Documentation

- Updated weapon tooltips to reflect stat bonuses

---

## [v0.0.67] - 2025-10-06

### Added

- **Versatile Weapon System**: Separate attack tables for one-hand/two-hand
  - `attacks`: Used when wielded one-handed
  - `versatileAttacks`: Used when wielded two-handed
  - Enables better balance (two-hand grants more damage)
- **Python JSON Sorter**: `format_jsons.py` maintains consistent field order
- **ID Automation**: Python script validates and maintains IDS.md
  - Detects duplicate IDs
  - Validates ID ranges
  - Generates `IDS_generated.md` with missing entries

### Changed

- Fixed Old Bow ID (9803 → 9600)
- Corrected "isVersatile" to "versatile" throughout codebase

### Testing

- Verified versatile weapon switching mechanics

---

## [v0.0.66] - 2025-10-05

### Added

- **Versatile Weapon Implementation**: Weapons can be wielded one or two-handed
  - Two-handed wielding grants damage bonuses
  - Automatically manages offhand slot

### Testing

- Added versatile weapon test cases

---

## [v0.0.65] - 2025-10-05

### Added

- **Stat-Based Attack Bonuses**: Stats now significantly affect combat
  - Attack rolls: `d20 + (stat bonus × 5)`
  - Makes character building more meaningful
  - Example: 15 STR = +5 modifier = +25 to attack
- **Combined Defense**: Dodge and block merged into single defense roll
  - Higher value determines defensive style (flavor text)
  - Simplifies combat calculations

### Changed

- Attack rolls now round to nearest integer
- Helper variable for stat bonus display (e.g., "Strength 15 +5")
- Creature.java uses helper for cleaner calculations
- Training Dummy updated to test defense mechanics

### Documentation

- Updated README.md with combat formulas

---

## [v0.0.64] - 2025-10-05

### Changed

- Adjusted creature JSON crit and luck stats for balance

---

## [v0.0.64] - 2025-10-04

### Added

- **Stamina System**: Resource for physical abilities
  - `baseMaxStamina` scales with Constitution
  - Stamina management (current/max tracking)
- **Luck-Based Crits**: Critical hit chance tied to Luck stat
  - Changing Luck adjusts crit chance dynamically

### Changed

- Lowered starting crit values for players (luck scaling compensates)

### Fixed

- Mana and stamina JSON values no longer overwritten by Creature.java

---

## [v0.0.63] - 2025-10-03

### Added

- **Mana System**: Resource for spellcasting
  - `currentMana` and `maxMana` tracking
  - `baseMaxMana` scales with Intelligence (10 mana per INT point)
  - Ratio preserved when max mana changes
- Default mana and stamina set to 100

### Changed

- Removed obsolete `defaultDamageType` variable
- Tidied Creature and CreatureLoader files
- Updated documentation files with current features

### Testing

- Added unarmed attack test in TestCreatureAttack.java

---

## [v0.0.62] - 2025-10-02

### Added

- **Stamina and Mana Foundations**: Added fields for future systems
  - `currentStamina` / `maxStamina`
  - `currentMana` / `maxMana`
  - Getters and setters implemented
  - `alterStamina()` and `alterMana()` preserve current/max ratio

---

## [v0.0.61] - 2025-10-01

### Added

- **Base Combat Stats**: Foundation for equipment calculations
  - `baseCrit`, `baseDodge`, `baseBlock`
  - Equipment bonuses calculated separately
- **DEX-Dodge Link**: Dexterity affects dodge chance
  - Each point above 10 DEX: +2.5% dodge
  - Each point below 10 DEX: -2.5% dodge
- **Equipment Stat Tracking**: Separate tracking for equipment bonuses
  - Clear separation between base and equipped stats

### Changed

- Level up logic moved to function in Creature.java
- Simplified `updateMaxHp()` method
- Commands no longer support diagonal movement (design decision for balance)
- Updated symbol system in CommandParser

### Fixed

- Equipment stat updates now work correctly

### Testing

- Tested movement and look commands on 50×50 map

---

## [v0.0.60] - 2025-09-30

### Added

- **Test Items**: Test Armor and Test Helmet for development
- Combat stat caps now properly enforced
  - Negative values stored correctly
  - Prevents stat manipulation exploits

### Changed

- Creature loader no longer loads duplicate entries

### Testing

- ItemTest validates edge values for crit, dodge, and block
- Updated IDS.md with missing entries

---

## [v0.0.59] - 2025-09-29

### Added

- **Dodge and Block Mechanics**: Active defense system
  - Attacker rolls against dodge first
  - If dodge fails, rolls against block
  - Both must fail for attack to land

### Changed

- Crit, block, and dodge converted to float for precision

---

## [v0.0.58] - 2025-09-29

### Changed

- File structure refinement: Player JSONs directly under `players/`
- Updated file reading at runtime

---

## [v0.0.58] - 2025-09-28

### Added

- **Monster Pools**: Random encounter system
  - Pool 1: Skeletons, Goblin, Dark Hound
  - Weighted random selection from pool

### Testing

- Verified spawn mechanics work as intended

---

## [v0.0.57] - 2025-09-26

### Added

- **Attack Definitions**: Creatures and weapons now have attack patterns
  - `magicDamageType` can be defined per attack
  - Weapon tooltips describe attack patterns
- **Healing Potion Tooltips**: Display healing amounts

### Changed

- Removed damage dice from weapons (moved to attacks)
- Goblin renamed to Goblin Berserker
- Attempted weapon and monster balance pass

### Fixed

- Updated IDS.md
- Corrected duplicate IDs (Dark Hound and Skeleton Spearman)

---

## [v0.0.56] - 2025-09-25

### Changed

- Removed weapon-level damage dice (attacks define damage)
- Added tooltips for item descriptions

### Testing

- Updated tests for attack-based damage system

---

## [v0.0.54] - 2025-09-24

### Added

- **Critical Hit System**: Damage multiplier on lucky hits
  - Critical hits double damage
  - `critMod` can modify crit chance per attack
  - Crits calculated per hit, not per attack
- **Training Dummy**: Test creature for combat mechanics

### Testing

- Added attack testing helper function in Creature class

---

## [v0.0.53] - 2025-09-24

### Added

- **Attack System**: Comprehensive attack customization
  - Attack.java class for flexible combat
  - Example: Dark Hound with multiple attack types
- **Combat Stats**: Crit, dodge, block chances
  - Equipment can modify these stats
- **DiceRoller**: Centralized dice rolling utility with unit tests

### Changed

- Equipment and Attack classes now integrated
- Updated README.md project structure

---

## [v0.0.52] - 2025-09-23

### Added

- **New Enemies**:
  - Skeleton Spearman
  - Skeleton Swordsman
  - Dark Hound
- **New Weapons**: Goblin and Skeleton equipment

### Changed

- Added missing creature folders
- Updated and fixed IDS.md

---

## [v0.0.51] - 2025-09-22

### Added

- **Guaranteed Loot Drops**: Loot pools can specify minimum drops
  - Enables boss loot and treasure chests
  - More granular drop chance control
- **Common Treasure Chest**: Example loot pool

### Testing

- TestIDSPaths.java validates ID assignments and file paths
- Updated loot pool tests for new mechanics

### Fixed

- Corrected Parrying Dagger name in IDS.md
- Added `data/` prefix to all item paths

---

## [v0.0.50] - 2025-09-22

### Added

- **Loot Pool System**: Random item drops
  - Search by name or ID
  - Weighted drop chances
- Loot pool section in IDS.md

### Removed

- Unused `userItemRarity` field

### Testing

- Added loot pool generation tests

---

## [v0.0.49] - 2025-09-21

### Added

- **Dice Modifiers**: Added support for "+" modifiers
  - Example: "1d6+2" (roll 1d6 and add 2)
  - Works for both attacking and healing (potions)

### Testing

- Updated attack tests for modifier support

---

## [v0.0.48] - 2025-09-20

### Added

- **Consumables**: Potion system
  - Healing functionality
  - Minor Healing Potion implemented
- Potion directory structure

---

## [v0.0.47] - 2025-09-20

### Added

- **Starting Equipment**: Players spawn with gear
  - Loaded from creature JSON
  - Character-specific starting items
- **Finesse Weapons**: DEX-based melee weapons
  - Fixed attack function (was mixing versatile and finesse)

### Changed

- Moved IDS.md to `data/` directory
- Updated IDS.md with new items
- Added starter gear for player characters

### Fixed

- Two-handed weapon unequip bug (ghost item in offhand)

### Testing

- Verified equipment loading and two-handed weapon mechanics

---

## [v0.0.46] - 2025-09-18

### Added

- **Loot and Monster Pool Templates**: Framework for random generation
- **LootPool.java**: Loot pool data structure
- **LootPoolLoader.java**: JSON deserialization for loot pools (GSON-based)
- Individual creature sprites by species
  - `sprite` field in creature JSONs
  - `getSprite()` method with fallback to "player_default"
- Asset attribution documentation

### Changed

- Moved personal assets to `resources/data` for clarity
- Increased player vision range by 1
- Updated README.md with sprites and project structure
- Updated `.gitattributes`

### Media

- Added GIF showcasing dungeon movement

---

## [v0.0.45] - 2025-09-16

### Added

- **Sprite System**: Visual representation for map symbols
  - Basic sprite rendering
  - One sprite per symbol type (expandable later)
- **Map Panning**: Camera follows player
- Sprite assets added to project

---

## [v0.0.44] - 2025-09-15

### Added

- **Combat View**: Dedicated combat interface
  - Displays player and enemy information
  - Combat action menu
  - Delayed damage display for readability

### Changed

- Increased window size to accommodate combat UI

---

## [v0.0.43] - 2025-09-14

### Added

- **Side Panel UI**: Character information display
  - Player stats in floor view
  - Inventory screen

### Documentation

- Updated README.md with UI features

---

## [v0.0.42] - 2025-09-13

### Added

- **Character Select Screen**: Choose your hero before adventuring
  - Integrated with game start sequence
- **Map Rendering**: Floor view with fog of war
  - Shows explored and unexplored areas
- **Keyboard Movement**: WASD or arrow key controls
- **Map Actor**: Keeps map display aligned and readable

### Changed

- Separated creature and player map tracking

---

## [v0.0.41] - 2025-09-13

### Added

- **LibGDX Framework**: Graphical UI foundation
- **VisUI**: Testing UI library
- **Basic Main Menu**:
  - Clickable Start button
  - Working Exit button

### Documentation

- Updated README.md and `.gitignore`
- Added Apache 2.0 license for VisUI

---

## [v0.0.40] - 2025-09-12

### Added

- **Combat Class**: Handles turn-based combat
  - Attack and flee commands
  - Combat ends on death or successful flee
- Unarmed damage for creatures

### Testing

- Prototype combat system functional

### Documentation

- Updated README.md with game progress

---

## [v0.0.39] - 2025-09-11

### Added

- **Resistance-Based Damage**: Attack function accounts for resistances
- **Magic Attack Detection**: Checks if weapon has magical damage
- **Unarmed Combat**: Default attack when no weapon equipped
- **HP Alteration**: `alterHp()` for damage and healing

### Features

- Creatures can attack with weapons or unarmed
- Damage and healing fully functional

---

## [v0.0.38] - 2025-09-11

### Added

- **Damage System Prototype**: Foundation for combat damage
- **Damage Dice Rolling**: Function to roll weapon damage dice

### Note

- Enemy resistances not yet factored in

---

## [v0.0.37] - 2025-09-11

### Added

- **Damage Types**: Physical and elemental damage categories
  - Default damage type for unarmed attacks
- **Attack Function**: Basic creature-vs-creature combat
- **Versatile Tag**: Weapons can be one or two-handed
- **WeaponClass Enum**: Categorizes weapons by type
- **Damage Dice**: Weapons define damage rolls
- **HP Ratio Tracking**: Maintains current/max HP relationship

### Changed

- Item printing now shows all combat-relevant stats

### Fixed

- `setCurrentHp()` function corrected

---

## [v0.0.36] - 2025-09-10

### Added

- **HP Scaling**: `hplv1Bonus` for level-based HP growth
  - Formula: `baseHp + hplv1Bonus + CON bonus`
  - Per level: `+ hplv1Bonus + CON bonus`
- Dynamic HP adjustment with Constitution changes

### Testing

- Added test for character "Biggles"

---

## [v0.0.35] - 2025-09-08

### Added

- **Darksight Trait**: Extended vision in darkness
  - Vision range system for creatures
  - Defaults to 1 if not specified
- **Fog of War**: Prototype exploration system
  - Tiles have `discovered` property
  - MapPrinter shows: '#' (undiscovered), symbol (discovered), '.' (unexplored)

---

## [v0.0.34] - 2025-09-08

### Added

- **Test Equipment**: Sword, helmet, legwear, shield
  - Verified different equipment slots work correctly
- **Spells Package**: Foundation for magic system (abstract class)

### Changed

- Organized armor into subfolders: armor, helmets, legwear, shields
- Organized weapons into subfolders: blunt, slash, piercing, ranged, magic
- Updated and future-proofed ID list

### Documentation

- Updated README.md

---

## [v0.0.33] - 2025-09-07

### Added

- **Inventory System**: Beginning of item management (Inventory.java)
  - Equip items directly from inventory
  - Equipped items don't consume inventory slots
- Equipping items now grants their stat bonuses

### Documentation

- Updated README.md file structure with equipment assets

---

## [v0.0.32] - 2025-09-06

### Changed

- Stats default to 10 if not provided in JSON
- Resistances default to 100 unless specified
- Property printing improved for readability

### Fixed

- Property application after stat changes
- Edge case with very low Constitution

---

## [v0.0.31] - 2025-09-05

### Fixed

- Base HP calculation when Constitution gives no bonus
- Creature loader no longer overrides level

---

## [v0.0.30] - 2025-09-04

### Added

- **EquipmentSlot Enum**: Standardized equipment slots
- **Item Rarity Enum**: Common, Uncommon, Rare, Epic, Legendary
- **ItemLoader**: JSON-based item loading (similar to CreatureLoader)
- Test armor for equipment system

### Changed

- Updated all creature JSONs for compatibility
- Sorted Creature.java methods

### Testing

- Verified item loading in Game.java

---

## [v0.0.29] - 2025-09-03

### Added

- **Leveling System**: XP and level progression
  - Max level: 30
  - XP automatically triggers level ups
  - Recursive XP overflow handling
- **Base HP**: Separate from stat-modified HP
  - Formula: `baseHp + (CON bonus × level)`
- `level` and `experience` fields for creatures

### Changed

- Current HP cannot exceed max HP
- HP and max HP now scale dynamically with Constitution
- Modified Creature `toString()` method
- Removed map printing from parsing

---

## [v0.0.28] - 2025-09-02

### Added

- **Multi-Floor Dungeons**: Floors -10 to +10 implemented
  - Test text maps for all floors
  - All floors loaded into dungeon entity
- Vertical movement (up/down stairs) functional

---

## [v0.0.27] - 2025-09-01

### Added

- **Look Command**: Examine nearby tiles
- Updated CommandParser symbols

### Changed

- **Removed Diagonal Movement**: Design decision for balance
  - Simpler grid-based combat
  - Easier to balance abilities and range

### Testing

- Tested movement and look commands on 50×50 map

---

## [v0.0.26] - 2025-09-01

### Added

- Expanded CommandParser for all cardinal directions

### Removed

- `occupied` boolean from Tile class (not needed)

---

## [v0.0.26] - 2025-08-31

### Changed

- Removed unnecessary files from structure
- Added item structure to assets

### Documentation

- Updated README.md project structure

---

## [v0.0.26] - 2025-08-30

### Added

- **Diagonal Movement Support**: 8-directional movement
  - Tile and Floor classes prepared for diagonal neighbors
- **Player Coordinates**: Track player position on map
- **GameState.java**: Current game state tracking
- **MapPrinter**: Renders current floor
- CommandParser: Select player and print map commands

### Changed

- Cleaned up map parsing
- Game.java more modular loading

---

## [v0.0.25] - 2025-08-30

### Added

- **Creature ID System**: Future-proofing for lookups
- **IDS.md**: Central registry for all IDs
  - Creatures, effects, items
- **Creature README**: JSON structure and ID conventions
- **CreatureLoader**: Loads all players and creatures from JSON

### Testing

- Player and creature creation tests
- ID usage verification
- Property management tests

### Changed

- Cleaned up test files

---

## [v0.0.24] - 2025-08-29

### Added

- **JSON Properties**: All properties now use JSON format
  - Stat modifications
  - Resistance changes

### Changed

- PropertyImpl accepts Stat and Resistance changes
- Creature supports new property system
- CharacterLoader works with new properties

---

## [v0.0.24] - 2025-08-28

### Added

- **GSON Library**: JSON parsing for game data
- **PropertyLoader**: Loads properties from JSON
  - HashMap-based ID lookup
- **JSON Creature Loading**: Modified Creature and Player for JSON (WIP)

### Changed

- PropertyManager (still broken)
- Test resource file structure reorganized
- Floor text files in subfolder
- Added asset folders: creatures, players, properties

### Testing

- PropertyLoader test files

### Deprecated

- Disabled old property system files

---

## [v0.0.23] - 2025-08-27

### Added

- **Item Stat Modification**: Items can modify creature stats
- **First Player Character**: Human player (WIP)
- **Licenses**: MIT (code), CC BY-NC-ND 4.0 (assets)

### Changed

- Human Adaptability trait doesn't affect luck

### Testing

- HumanPlayerTest.java for first character

---

## [v0.0.21] - 2025-08-27

### Added

- **Game Class**: Handles game initialization
- **Humanoid Subcategories**: Humans and Goblinoids
- **Property ID System**: Easy property manipulation
- **Property Slots**: Buffs, debuffs, immunities, traits
  - Properties modify creature on application/removal

### Changed

- Moved "Coward" trait to proper location

---

## [v0.0.2] - 2025-08-26

### Added

- Temporary map generation command
- Floor generation testing

### Changed

- Edited Tile class, fixed bugs

### Todo

- Code cleanup and modularization

---

## [v0.0.2] - 2025-08-25

### Added

- **CommandParser**: Foundation for game commands
- **Tile Symbols**: Early version with logic (WIP)
- README.md placeholder sections

---

## [v0.0.1] - 2025-08-25

### Added

- **Map Parser**: Improved parsing with tests
- **Game Class**: Core game loop foundation
- Test files for all major classes
- **Coordinate Class**: 2D position tracking
- **Tile Class**: Floor building blocks (placeholder variables)

### Changed

- File structure for test floors (text files)
- Categorized creatures by type
- Fixed test directory structure (mirroring main)

### Testing

- Two test floor files (one ready for testing)
- Expanded creature creation tests
- Fixed creature stat and resistance functions

---

## [v0.0.1] - 2025-08-24

### Added

- **MapParser Structure**: Basic parser implementation
- Dungeon and Floor abstracts
- Item and event placeholders in file structure
- **Property Types**: Immunity, Buff, Debuff, Trait
- Creature `description` field
- Creature HP, resistance, and stat modification methods
  - HP capped at maxHP

### Changed

- Component renamed to Property
- More file structure changes
- Set up Maven and project structure

### Testing

- Creature class tests
- Goblin class tests

### Documentation

- Added project license

---

## [v0.0.1] - 2025-08-23

### Added

- **Resistance Immunities**: All resistance types have immunity properties
- Proper Maven setup

---

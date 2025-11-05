# Procedural Map Generation Implementation

## Summary

Replaced static text-file-based map loading with procedural map generation system.

## Changes Made

### 1. Created MapGenerator Interface

**File:** `src/main/java/com/bapppis/core/dungeon/generator/MapGenerator.java`

Defines the contract for all map generation strategies:

```java
Floor generate(int width, int height, int floorNumber, long seed)
```

### 2. Implemented BSPRoomGenerator

**File:** `src/main/java/com/bapppis/core/dungeon/generator/BSPRoomGenerator.java`

Creates floors with:

- **2-tile thick outer walls** (as specified in requirement)
- **Floor tiles** filling the interior
- **Upstairs** (^) placed in top-left quadrant
- **Downstairs** (v) placed in bottom-right quadrant
- **Neighbor linking** for all tiles
- **Deterministic generation** using seeded Random

### 3. Updated Game.loadDungeon()

**File:** `src/main/java/com/bapppis/core/game/Game.java`

Commented out text file loading and replaced with:

- **Floors -10 to +10** generation
- **Size ranges based on depth:**
  - Floors 0 to ±4: random 20x20 to 30x30
  - Floors ±5 to ±10: random 30x30 to 40x40
- **Fixed seed** (12345L) with floor-specific variation (seed + floorNumber)
- Each floor gets a unique seed to ensure variety while maintaining determinism

### 4. Created Comprehensive Tests

**Files:**

- `src/test/java/com/bapppis/core/dungeon/generator/BSPRoomGeneratorTest.java` (7 tests)
- `src/test/java/com/bapppis/core/dungeon/generator/DungeonGenerationIntegrationTest.java` (2 tests)

**Test Coverage:**

- ✅ Correct floor size creation
- ✅ Deterministic generation (same seed = same floor)
- ✅ Two-layer wall borders
- ✅ Stair placement (both up and down)
- ✅ Walkable interior (>80% floor tiles)
- ✅ Neighbor linking (bidirectional)
- ✅ Full dungeon generation (-10 to +10)
- ✅ Size constraints per depth

## Results

- **All 9 new tests pass** ✅
- **Build succeeds** ✅
- **No breaking changes to existing code** ✅
- **Text file loading preserved in comments** for potential future reference

## Floor Specifications Met

✅ Floors 0 to ±4: 20x20-30x30 size  
✅ Floors ±5 to ±10: 30x30-40x40 size  
✅ Two outer layers are walls  
✅ Interior filled with floor tiles  
✅ Each floor has one upstair and one downstair  
✅ Fixed locations for stairs (placeholder as requested)

## Future Enhancements

The architecture supports easy addition of:

- More sophisticated generation algorithms (BSP room splitting, cellular automata, etc.)
- Configurable seed (command-line argument, save file, etc.)
- Room and corridor placement
- Door placement
- Treasure/enemy placement
- Special room types
- Biome-based generation

## Usage

The game now automatically generates all floors on startup using `BSPRoomGenerator`. No text files are needed.

To use a different seed, modify the `seed` variable in `Game.loadDungeon()`:

```java
long seed = 12345L; // Change this value for different dungeon layouts
```

To implement a new generator:

1. Create a class implementing `MapGenerator` interface
2. Implement the `generate()` method
3. Replace `BSPRoomGenerator` in `Game.loadDungeon()` with your new generator

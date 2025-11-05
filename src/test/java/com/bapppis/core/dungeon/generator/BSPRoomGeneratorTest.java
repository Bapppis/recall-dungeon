package com.bapppis.core.dungeon.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.Tile;

public class BSPRoomGeneratorTest {

    @Test
    public void testGeneratorCreatesFloorWithCorrectSize() {
        BSPRoomGenerator generator = new BSPRoomGenerator();
        Floor floor = generator.generate(25, 25, 0, 12345L);

        assertNotNull(floor, "Floor should not be null");
        assertNotNull(floor.getTiles(), "Floor tiles should not be null");

        // Verify we have tiles at expected coordinates
        Tile topLeft = floor.getTile(new Coordinate(0, 0));
        Tile bottomRight = floor.getTile(new Coordinate(24, 24));
        assertNotNull(topLeft, "Top-left tile should exist");
        assertNotNull(bottomRight, "Bottom-right tile should exist");
    }

    @Test
    public void testGeneratorIsDeterministic() {
        BSPRoomGenerator generator = new BSPRoomGenerator();
        long seed = 54321L;

        Floor floor1 = generator.generate(20, 20, 1, seed);
        Floor floor2 = generator.generate(20, 20, 1, seed);

        // Check that both floors have the same tiles at the same positions
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                Coordinate coord = new Coordinate(x, y);
                Tile tile1 = floor1.getTile(coord);
                Tile tile2 = floor2.getTile(coord);

                assertNotNull(tile1, "Tile should exist in floor1 at " + coord);
                assertNotNull(tile2, "Tile should exist in floor2 at " + coord);
                assertEquals(tile1.getSymbol(), tile2.getSymbol(),
                        "Tiles at " + coord + " should have the same symbol");
            }
        }
    }

    @Test
    public void testGeneratorCreatesTwoLayerWalls() {
        BSPRoomGenerator generator = new BSPRoomGenerator();
        Floor floor = generator.generate(20, 20, 0, 99999L);

        // Check outer two layers are walls
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                Tile tile = floor.getTile(new Coordinate(x, y));
                if (x < 2 || x >= 18 || y < 2 || y >= 18) {
                    assertTrue(tile.isWall(),
                            "Tile at (" + x + ", " + y + ") should be a wall");
                    assertEquals('#', tile.getSymbol(),
                            "Wall tile should have '#' symbol");
                }
            }
        }
    }

    @Test
    public void testGeneratorPlacesStairs() {
        BSPRoomGenerator generator = new BSPRoomGenerator();
        Floor floor = generator.generate(30, 30, -5, 77777L);

        boolean foundUpstairs = false;
        boolean foundDownstairs = false;

        // Search for stairs in the generated floor
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 30; y++) {
                Tile tile = floor.getTile(new Coordinate(x, y));
                if (tile != null) {
                    if (tile.getSymbol() == '^') {
                        foundUpstairs = true;
                    }
                    if (tile.getSymbol() == 'v') {
                        foundDownstairs = true;
                    }
                }
            }
        }

        assertTrue(foundUpstairs, "Floor should have at least one upstairs tile");
        assertTrue(foundDownstairs, "Floor should have at least one downstairs tile");
    }

    @Test
    public void testGeneratorCreatesWalkableInterior() {
        BSPRoomGenerator generator = new BSPRoomGenerator();
        Floor floor = generator.generate(25, 25, 3, 11111L);

        int floorTileCount = 0;

        // Count floor tiles in the interior (excluding 2-tile border)
        for (int x = 2; x < 23; x++) {
            for (int y = 2; y < 23; y++) {
                Tile tile = floor.getTile(new Coordinate(x, y));
                if (tile != null && !tile.isWall()) {
                    floorTileCount++;
                }
            }
        }

        // Interior should have many walkable tiles (at least 80% of interior area)
        int interiorArea = 21 * 21; // 23-2 = 21 per side
        assertTrue(floorTileCount > interiorArea * 0.8,
                "Interior should have mostly walkable floor tiles. Found " + floorTileCount +
                        " out of " + interiorArea + " interior tiles");
    }

    @Test
    public void testGeneratorLinksNeighbors() {
        BSPRoomGenerator generator = new BSPRoomGenerator();
        Floor floor = generator.generate(15, 15, 0, 33333L);

        // Check a tile in the middle has proper neighbors
        Tile centerTile = floor.getTile(new Coordinate(7, 7));
        assertNotNull(centerTile, "Center tile should exist");

        Tile left = centerTile.getLeft();
        Tile right = centerTile.getRight();
        Tile up = centerTile.getUp();
        Tile down = centerTile.getDown();

        assertNotNull(left, "Center tile should have left neighbor");
        assertNotNull(right, "Center tile should have right neighbor");
        assertNotNull(up, "Center tile should have up neighbor");
        assertNotNull(down, "Center tile should have down neighbor");

        // Verify bidirectional links
        assertEquals(centerTile, left.getRight(), "Left neighbor should link back to center");
        assertEquals(centerTile, right.getLeft(), "Right neighbor should link back to center");
        assertEquals(centerTile, up.getDown(), "Up neighbor should link back to center");
        assertEquals(centerTile, down.getUp(), "Down neighbor should link back to center");
    }
}

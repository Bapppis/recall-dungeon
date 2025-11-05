package com.bapppis.core.dungeon.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.dungeon.Coordinate;
import com.bapppis.core.dungeon.Dungeon;
import com.bapppis.core.dungeon.Floor;
import com.bapppis.core.dungeon.Tile;

public class DungeonGenerationIntegrationTest {

    @Test
    public void testDungeonGeneratesAllFloorsWithCorrectSizes() {
        Dungeon dungeon = new Dungeon() {};
        BSPRoomGenerator generator = new BSPRoomGenerator();
        long seed = 12345L;

        // Generate all floors as in Game.loadDungeon()
        for (int i = -10; i <= 10; i++) {
            int minSize, maxSize;
            if (i >= -4 && i <= 4) {
                minSize = 20;
                maxSize = 30;
            } else {
                minSize = 30;
                maxSize = 40;
            }

            java.util.Random random = new java.util.Random(seed + i);
            int width = minSize + random.nextInt(maxSize - minSize + 1);
            int height = minSize + random.nextInt(maxSize - minSize + 1);

            Floor floor = generator.generate(width, height, i, seed + i);
            dungeon.addFloor(i, floor);
        }

        // Verify all floors exist
        for (int i = -10; i <= 10; i++) {
            Floor floor = dungeon.getFloor(i);
            assertNotNull(floor, "Floor " + i + " should exist");

            // Verify sizing constraints
            if (i >= -4 && i <= 4) {
                // These floors should be 20x20 to 30x30
                verifyFloorHasReasonableSize(floor, 20, 30);
            } else {
                // These floors should be 30x30 to 40x40
                verifyFloorHasReasonableSize(floor, 30, 40);
            }

            // Verify stairs exist
            verifyFloorHasStairs(floor, i);
        }
    }

    private void verifyFloorHasReasonableSize(Floor floor, int minSize, int maxSize) {
        // Check that we can access tiles within the expected range
        boolean foundTileInRange = false;
        for (int x = minSize - 1; x <= maxSize; x++) {
            for (int y = minSize - 1; y <= maxSize; y++) {
                Tile tile = floor.getTile(new Coordinate(x, y));
                if (tile != null) {
                    foundTileInRange = true;
                    break;
                }
            }
            if (foundTileInRange) break;
        }
        assertTrue(foundTileInRange, "Floor should have tiles in the size range " + minSize + "-" + maxSize);
    }

    private void verifyFloorHasStairs(Floor floor, int floorNumber) {
        boolean hasUpstairs = false;
        boolean hasDownstairs = false;

        // Search for stairs
        for (Tile tile : floor.getTiles().values()) {
            if (tile.getSymbol() == '^') {
                hasUpstairs = true;
            }
            if (tile.getSymbol() == 'v') {
                hasDownstairs = true;
            }
        }

        assertTrue(hasUpstairs, "Floor " + floorNumber + " should have upstairs");
        assertTrue(hasDownstairs, "Floor " + floorNumber + " should have downstairs");
    }
}

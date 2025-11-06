package com.bapppis.core.dungeon.generator;

import com.bapppis.core.dungeon.Floor;

/**
 * Interface for map generation algorithms.
 * Implementations should use the provided seed for deterministic generation.
 */
public interface MapGenerator {
    /**
     * Generate a floor with the specified dimensions.
     *
     * @param width Floor width
     * @param height Floor height
     * @param floorNumber Floor number (for context, e.g., difficulty scaling)
     * @param seed Random seed for deterministic generation
     * @return Generated Floor
     */
    Floor generate(int width, int height, int floorNumber, long seed);
}

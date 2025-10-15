package com.bapppis.core.combat;

/**
 * RNG abstraction used by the combat engine to allow deterministic testing.
 */
public interface RandomProvider {
    /**
     * Returns a uniformly distributed float in [0.0, 1.0).
     */
    float nextFloat();
}

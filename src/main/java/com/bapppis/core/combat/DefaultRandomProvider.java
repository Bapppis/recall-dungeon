package com.bapppis.core.combat;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Production RandomProvider using ThreadLocalRandom.
 */
public final class DefaultRandomProvider implements RandomProvider {

    @Override
    public float nextFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }
}

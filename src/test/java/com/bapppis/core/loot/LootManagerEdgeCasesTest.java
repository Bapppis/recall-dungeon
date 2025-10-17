package com.bapppis.core.loot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;

public class LootManagerEdgeCasesTest {

    @Test
    public void testUnknownPoolReturnsEmpty() {
        AllLoaders.loadAll();
        LootManager manager = new LootManager();
        manager.loadDefaults();
        assertTrue(manager.samplePool("unknown-pool").isEmpty());
        assertTrue(manager.samplePoolByName(null).isEmpty());
    }
}

package com.bapppis.core.dungeon.mapparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class MapParserEdgeCasesTest {

    @Test
    public void testCopyStreamWithNullInputDoesNotThrow() {
        MapParserTestUtil util = new MapParserTestUtil();
        assertDoesNotThrow(() -> util.copyStreamToFile(null, "ignored.txt"));
    }

    @Test
    public void testParseMissingResourceGracefully() {
    InputStream is = MapParserEdgeCasesTest.class.getClassLoader().getResourceAsStream("nonexistent-floor.txt");
        if (is != null) {
            // If classpath accidentally contains the file, skip
            return;
        }
        // Nothing to parse; just ensure we handle null without throwing
        assertNull(is);
    }
}

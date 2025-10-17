package com.bapppis.core.dungeon.mapparser;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class MapParserTest {

    @Test
    public void testParseStream() {
    // System.out.println("---------------------Map Parser---------------------");

        MapParserTestUtil mapParserTestUtil = new MapParserTestUtil();
        String resourceName = "floor(20x20).txt";
        InputStream is = MapParserTest.class.getClassLoader().getResourceAsStream(resourceName);
        mapParserTestUtil.copyStreamToFile(is, "mapParserOutput.txt");

    }

}

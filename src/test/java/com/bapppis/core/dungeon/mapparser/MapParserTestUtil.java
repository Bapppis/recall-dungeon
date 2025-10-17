package com.bapppis.core.dungeon.mapparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;

public class MapParserTestUtil
 {
    public void copyStreamToFile(InputStream inputStream, String outputFilePath) {
        // System.out.println("Copying map symbols to output file...");
        if (inputStream == null) {
            // System.err.println("Input stream was null; resource not found. Aborting copy.");
            return;
        }
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))
        ) {
            int symbol;
            while ((symbol = reader.read()) != -1) {
                writer.write(symbol);
            }
            // System.out.println("Done! Output written to: " + outputFilePath);
        } catch (IOException e) {
            // System.err.println("Error during copy");
            // e.printStackTrace();
        }
    }
}
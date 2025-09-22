package com.bapppis.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import static org.junit.jupiter.api.Assertions.fail;

public class TestIDSPaths {

    private static final Path RES_ROOT = Paths.get("src", "main", "resources");

    @Test
    public void idsPathsShouldExist() throws IOException {
        Path ids = RES_ROOT.resolve(Paths.get("data", "IDS.md"));
        List<String> lines = Files.readAllLines(ids);

        // Pattern: lines that contain " — " followed by a path (we take everything after last ' — ')
        Pattern entryPattern = Pattern.compile(".*—\\s*(.+)$");

        List<String> missing = new ArrayList<>();
        List<String> checked = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;
            if (line.contains("[none indexed yet]")) continue;

            Matcher m = entryPattern.matcher(line);
            if (!m.find()) continue;

            String rawPath = m.group(1).trim(); // e.g. data/items/...
            rawPath = rawPath.replaceAll("[)\\]]+$", "").trim();

            if (!rawPath.contains("/") || !rawPath.toLowerCase().endsWith(".json")) continue;

            Path candidate = RES_ROOT.resolve(rawPath.replace('/', FileSystems.getDefault().getSeparator().charAt(0)));
            if (!Files.exists(candidate)) {
                missing.add(String.format("Line %d: %s -> %s", i + 1, line, candidate));
            } else {
                checked.add(candidate.toString());
            }
        }

        if (!missing.isEmpty()) {
            StringBuilder b = new StringBuilder();
            b.append("IDS.md refers to missing files:\n");
            missing.forEach(s -> b.append(s).append("\n"));
            b.append("\nChecked files (examples):\n");
            checked.stream().limit(10).forEach(s -> b.append(" - ").append(s).append("\n"));
            fail(b.toString());
        }
    }
}

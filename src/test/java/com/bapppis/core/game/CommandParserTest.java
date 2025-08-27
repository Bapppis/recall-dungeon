package com.bapppis.core.game;

import org.junit.jupiter.api.Test;

public class CommandParserTest {
    private CommandParser commandParser = new CommandParser();

    @Test
    public void testMoveCommand() {
        commandParser.parseAndExecute("move north");
        // Add assertions to verify the expected behavior
    }

    @Test
    public void testAttackCommand() {
        commandParser.parseAndExecute("attack goblin");
        // Add assertions to verify the expected behavior
    }

    @Test
    public void testUnknownCommand() {
        commandParser.parseAndExecute("jump");
        // Add assertions to verify the expected behavior
    }
}

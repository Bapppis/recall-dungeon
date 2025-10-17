package com.bapppis.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DiceEdgeCasesTest {

    @Test
    public void testZeroDiceAndZeroSidesInvalid() {
        assertEquals(0, Dice.roll("0d6"));
        assertEquals(0, Dice.roll("1d0"));
        assertEquals(0, Dice.roll("0d0"));
    }

    @Test
    public void testWhitespaceAndFormat() {
        assertEquals(0, Dice.roll(" d6"));
        assertEquals(0, Dice.roll("1 d6"));
        assertEquals(0, Dice.roll("1d 6"));
        assertEquals(0, Dice.roll("1d6 + 1"));
    }
}

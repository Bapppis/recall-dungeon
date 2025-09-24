package com.bapppis.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class DiceTest {

    @Test
    public void testSimpleRolls() {
        int r1 = Dice.roll("1d1");
        assertEquals(1, r1);

        int r2 = Dice.roll("2d1");
        assertEquals(2, r2);

        int r3 = Dice.roll("1d6");
        assertTrue(r3 >= 1 && r3 <= 6, "1d6 within 1-6");
    }

    @Test
    public void testModifierRolls() {
        int r = Dice.roll("2d1+3");
        assertEquals(5, r);

        int rNeg = Dice.roll("1d1-1");
        assertEquals(0, rNeg);
    }

    @Test
    public void testInvalidInputs() {
        assertEquals(0, Dice.roll(null));
        assertEquals(0, Dice.roll(""));
        assertEquals(0, Dice.roll("not a dice"));
        assertEquals(0, Dice.roll("d6"));
    }

    @RepeatedTest(5)
    public void testRandomRange() {
        int r = Dice.roll("3d4");
        assertTrue(r >= 3 && r <= 12);
    }

    @Test
    public void testPrintSampleRolls() {
        System.out.println("--- Sample Dice Rolls (visible in console) ---");
        int a = Dice.roll("1d6");
        System.out.println("1d6 -> " + a);
        assertTrue(a >= 1 && a <= 6);

        int b = Dice.roll("2d4+1");
        System.out.println("2d4+1 -> " + b);
        // 2d4 range is 2-8, plus 1 => 3-9
        assertTrue(b >= 3 && b <= 9);

        int c = Dice.roll("3d1");
        System.out.println("3d1 -> " + c);
        assertEquals(3, c);
        System.out.println("---------------------------------------------");
    }
}

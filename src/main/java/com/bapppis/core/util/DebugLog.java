package com.bapppis.core.util;

public final class DebugLog {
    // Toggle to enable/disable debug output globally during development.
    // Set to false for production or CI.
    public static boolean DEBUG = false;

    private DebugLog() {}

    public static void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}

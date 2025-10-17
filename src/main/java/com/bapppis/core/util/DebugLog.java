package com.bapppis.core.util;

public final class DebugLog {
    // Toggle to enable/disable debug output globally during development.
    // Set to false for production or CI.
    // To enable debug messages at runtime for local debugging, uncomment the
    // following line either here or in a test setup / main method:
    // DebugLog.DEBUG = true; // <-- uncomment to enable debug output
    public static boolean DEBUG = false;

    private DebugLog() {}

    public static void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}

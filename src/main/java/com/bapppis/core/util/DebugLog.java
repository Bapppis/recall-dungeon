package com.bapppis.core.util;

public final class DebugLog {
    public static boolean DEBUG = false;

    private DebugLog() {}

    public static void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}

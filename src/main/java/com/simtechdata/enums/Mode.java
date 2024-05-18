package com.simtechdata.enums;

public enum Mode {
    NORMAL,
    GRAAL,
    DEBUG;

    private static Mode mode = NORMAL;

    public static void setMode(Mode mode) {
        Mode.mode = mode;
    }

    public static boolean isDebug() {
        return mode.equals(DEBUG);
    }

    public static boolean isGraal() {
        return mode.equals(GRAAL);
    }

    public static boolean isForced() {
        return isDebug() || isGraal();
    }

}

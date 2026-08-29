package com.lyadev.mygame.debug;

import java.util.function.Consumer;

public final class DebugFeatures {
    private static boolean visionLinesVisible = false;
    private static boolean overlayVisible = false;
    private static Consumer<DebugLogEntry> externalLogSink;

    private DebugFeatures() {
        throw new UnsupportedOperationException();
    }

    public static boolean isVisionLinesVisible() {
        return visionLinesVisible;
    }

    public static void setVisionLinesVisible(boolean visible) {
        visionLinesVisible = visible;
    }

    public static void toggleVisionLines() {
        visionLinesVisible = !visionLinesVisible;
    }

    public static boolean isOverlayVisible() {
        return overlayVisible;
    }

    public static void setOverlayVisible(boolean visible) {
        overlayVisible = visible;
    }

    public static void toggleOverlay() {
        overlayVisible = !overlayVisible;
    }

    public static void setExternalLogSink(Consumer<DebugLogEntry> sink) {
        externalLogSink = sink;
    }

    public static void logExternal(DebugLogEntry entry) {
        if(externalLogSink != null && entry != null){
            externalLogSink.accept(entry);
        }
    }

    public static void logExternal(String level, String tag, String message) {
        logExternal(new DebugLogEntry(DebugLogEntry.Level.valueOf(level), tag, message));
    }
}

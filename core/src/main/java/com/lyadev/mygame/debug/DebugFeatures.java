package com.lyadev.mygame.debug;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

public final class DebugFeatures {
    @Getter
    @Setter
    private static boolean visionLinesVisible = false;
    @Getter
    @Setter
    private static boolean overlayVisible = false;
    private static Consumer<DebugLogEntry> externalLogSink;

    private DebugFeatures() {
        throw new UnsupportedOperationException();
    }

    public static void toggleVisionLines() {
        visionLinesVisible = !visionLinesVisible;
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

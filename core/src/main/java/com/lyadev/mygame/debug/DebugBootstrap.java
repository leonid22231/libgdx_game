package com.lyadev.mygame.debug;

public final class DebugBootstrap {
    private static Runnable onGameCreate = () -> {};
    private static Runnable onGameDispose = () -> {};

    private DebugBootstrap() {
        throw new UnsupportedOperationException();
    }

    public static void register(Runnable onCreate, Runnable onDispose) {
        onGameCreate = onCreate != null ? onCreate : () -> {};
        onGameDispose = onDispose != null ? onDispose : () -> {};
    }

    public static void onGameCreate() {
        onGameCreate.run();
    }

    public static void onGameDispose() {
        onGameDispose.run();
    }
}

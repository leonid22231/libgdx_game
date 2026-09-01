package com.lyadev.mygame.world;

/**
 * Virtual camera size in world pixels (tile space).
 * ~400×400 means about 25×25 of 16px tiles on screen; map can be larger — camera follows / pans.
 */
public final class WorldCameraSettings {
    public static final float WORLD_WIDTH = 400f;
    public static final float WORLD_HEIGHT = 400f;

    private WorldCameraSettings() {
        throw new UnsupportedOperationException();
    }
}

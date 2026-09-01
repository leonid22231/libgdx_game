package com.lyadev.mygame.worlds;

import com.lyadev.mygame.world.WorldController;

/**
 * Registers concrete playable worlds with {@link WorldController}.
 * Add new maps here — keep infrastructure in {@code com.lyadev.mygame.world}.
 */
public final class Worlds {
    private Worlds() {
        throw new UnsupportedOperationException();
    }

    /** Register all known worlds (order = debug list order). */
    public static void registerAll() {
        WorldController.register(new ForestLakeWorld());
        WorldController.register(new HouseInteriorWorld());
    }

    /** Default world id for {@link WorldController#start}. */
    public static String startWorldId() {
        return ForestLakeWorld.ID;
    }
}

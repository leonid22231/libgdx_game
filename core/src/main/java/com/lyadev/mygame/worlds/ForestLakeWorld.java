package com.lyadev.mygame.worlds;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.modules.door.DoorSettings;
import com.lyadev.mygame.world.JsonMapWorld;

/** Outdoor forest map — default start world. */
public final class ForestLakeWorld extends JsonMapWorld {
    public static final String ID = "forest_lake";
    public static final String MAP_PATH = "maps/forest_lake.json";

    public ForestLakeWorld() {
        super(ID, "Forest Lake", MAP_PATH, true);
    }

    @Override
    protected void installPortals() {
        placePortal("rocks_a", 163, 133, 26, 22, 8, 22,
                new DoorSettings(HouseInteriorWorld.ID, 6, 4));
        Gdx.app.log("ForestLakeWorld", "Portal @ [8,22] → " + HouseInteriorWorld.ID);
    }
}

package com.lyadev.mygame.worlds;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.modules.door.DoorSettings;
import com.lyadev.mygame.base.world.JsonMapWorld;

/** Small interior test house. */
public final class HouseInteriorWorld extends JsonMapWorld {
    public static final String ID = "house_interior";
    public static final String MAP_PATH = "maps/house_interior.json";

    public HouseInteriorWorld() {
        super(ID, "House Interior", MAP_PATH, false);
    }

    @Override
    protected void installPortals() {
        placePortal("mushroom_a", 136, 9, 16, 14, 6, 1,
                new DoorSettings(ForestLakeWorld.ID, 8, 20));
        Gdx.app.log("HouseInteriorWorld", "Portal @ [6,1] → " + ForestLakeWorld.ID);
    }
}

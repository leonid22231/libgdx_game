package com.lyadev.mygame.world.doors;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.modules.door.DoorModule;
import com.lyadev.mygame.modules.door.DoorSettings;
import com.lyadev.mygame.modules.prop.PropModule;
import com.lyadev.mygame.modules.prop.PropSettings;

/** Factory: decor Entity + PropModule + DoorModule. */
public final class DoorEntity {
    private DoorEntity() {
        throw new UnsupportedOperationException();
    }

    public static Entity create(PropSettings prop, DoorSettings door, int tileCol, int tileRow) {
        Entity entity = new Entity("Door:" + prop.getAssetId() + "@" + tileCol + "," + tileRow);
        PropModule propModule = new PropModule(prop);
        propModule.setTileCell(tileCol, tileRow);
        entity.registerModule(propModule);
        entity.registerModule(new DoorModule(door));
        entity.resolveModules();
        return entity;
    }
}

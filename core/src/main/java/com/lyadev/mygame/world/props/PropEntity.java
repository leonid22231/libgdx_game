package com.lyadev.mygame.world.props;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.modules.prop.PropModule;
import com.lyadev.mygame.modules.prop.PropSettings;

/** Factory: map decor as lean Entity + PropModule. */
public final class PropEntity {
    private PropEntity() {
        throw new UnsupportedOperationException();
    }

    public static Entity create(PropSettings settings, int tileCol, int tileRow) {
        Entity entity = new Entity("Prop:" + settings.getAssetId() + "@" + tileCol + "," + tileRow);
        PropModule prop = new PropModule(settings);
        prop.setTileCell(tileCol, tileRow);
        entity.registerModule(prop);
        entity.resolveModules();
        return entity;
    }
}

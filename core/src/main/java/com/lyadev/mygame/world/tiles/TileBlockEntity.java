package com.lyadev.mygame.world.tiles;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.modules.tile.TileBlockModule;
import com.lyadev.mygame.modules.vision.VisionModule;
import com.lyadev.mygame.modules.vision.VisionSettings;
import com.lyadev.mygame.world.topdown.OrthoTileset;

/** Factory: ground cell = Entity + TileBlockModule. */
public final class TileBlockEntity {
    private TileBlockEntity() {
        throw new UnsupportedOperationException();
    }

    public static Entity create(OrthoTileset tileset, int tileId, int col, int row, float drawSize) {
        Entity entity = new Entity("Tile:" + tileId + "@" + col + "," + row);
        entity.registerModule(new TileBlockModule(tileset, tileId, col, row, drawSize));
        entity.registerModule(new VisionModule(new VisionSettings(1f)));
        entity.resolveModules();
        entity.setVisible(true);
        return entity;
    }
}

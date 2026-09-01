package com.lyadev.mygame.modules.tile;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.world.topdown.OrthoTileset;

/**
 * One ground cell as Entity: tile id + grid coords + shared tileset region.
 */
public class TileBlockModule extends EntityModule {
    private final OrthoTileset tileset;
    private final float drawSize;
    private int tileId;
    private int tileCol;
    private int tileRow;

    public TileBlockModule(OrthoTileset tileset, int tileId, int tileCol, int tileRow, float drawSize) {
        this.tileset = tileset;
        this.tileId = tileId;
        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.drawSize = drawSize;
    }

    @Override
    public String getName() {
        return "tile_block_module";
    }

    @Override
    public void init() {
        getEntity().setSize(drawSize, drawSize);
        getEntity().setVisible(true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion region = tileset.get(tileId);
        if(region == null){
            return;
        }
        Entity entity = getEntity();
        batch.setColor(1f, 1f, 1f, parentAlpha);
        // Slight overlap kills subpixel seams between neighboring tile sprites.
        float overlap = 0.25f;
        batch.draw(region, entity.getX(), entity.getY(),
                entity.getWidth() + overlap, entity.getHeight() + overlap);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public int getTileId() {
        return tileId;
    }

    public void setTileId(int tileId) {
        this.tileId = tileId;
    }

    public int getTileCol() {
        return tileCol;
    }

    public int getTileRow() {
        return tileRow;
    }

    public void setTileCell(int col, int row) {
        this.tileCol = col;
        this.tileRow = row;
    }

    public static TileBlockModule from(Entity entity) {
        return entity.getModule(TileBlockModule.class);
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        panel.line("tileId", tileId);
        panel.line("cell", tileCol + "," + tileRow);
    }
}

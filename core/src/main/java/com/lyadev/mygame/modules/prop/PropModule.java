package com.lyadev.mygame.modules.prop;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;

/**
 * Draws a static atlas crop. Map decor = Entity + PropModule (not a ground tile cell).
 */
public class PropModule extends EntityModule {
    private final PropSettings settings;
    private TextureRegion region;
    private int tileCol;
    private int tileRow;

    public PropModule(PropSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "prop_module";
    }

    @Override
    public void init() {
        region = PropAtlasCache.region(
                settings.getAtlasPath(),
                settings.getSrcX(), settings.getSrcY(),
                settings.getSrcW(), settings.getSrcH());
        float w = settings.getSrcW() * settings.getScale();
        float h = settings.getSrcH() * settings.getScale();
        getEntity().setSize(w, h);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(region == null){
            return;
        }
        Entity entity = getEntity();
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(region, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public PropSettings getSettings() {
        return settings;
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

    public static PropModule from(Entity entity) {
        return entity.getModule(PropModule.class);
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        panel.line("assetId", settings.getAssetId());
        panel.line("tile", tileCol + "," + tileRow);
        panel.line("src", settings.getSrcW() + "x" + settings.getSrcH());
    }
}

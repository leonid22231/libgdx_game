package com.lyadev.mygame.world.topdown;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Orthogonal pixel tileset (e.g. 16×16). */
public final class OrthoTileset {
    private static final String TAG = "OrthoTileset";

    private final String path;
    private final int tileSize;
    private final int columns;
    private Texture texture;
    private TextureRegion[] regions;
    private int tileCount;

    public OrthoTileset(String path, int tileSize, int columns) {
        this.path = path;
        this.tileSize = tileSize;
        this.columns = columns;
    }

    public void load() {
        texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        int rows = texture.getHeight() / tileSize;
        tileCount = columns * rows;
        regions = new TextureRegion[tileCount];
        for(int i = 0; i < tileCount; i++){
            int col = i % columns;
            int row = i / columns;
            regions[i] = new TextureRegion(texture, col * tileSize, row * tileSize, tileSize, tileSize);
            // Inset half-texel so Nearest sampling never bleeds into neighbor / empty atlas.
            float u = regions[i].getU();
            float v = regions[i].getV();
            float u2 = regions[i].getU2();
            float v2 = regions[i].getV2();
            float du = (u2 - u) / tileSize * 0.5f;
            float dv = (v2 - v) / tileSize * 0.5f;
            regions[i].setU(u + du);
            regions[i].setV(v + dv);
            regions[i].setU2(u2 - du);
            regions[i].setV2(v2 - dv);
        }
        Gdx.app.log(TAG, "Loaded " + path + " tiles=" + tileCount + " size=" + tileSize);
    }

    public TextureRegion get(int localId) {
        if(regions == null || localId < 0 || localId >= regions.length){
            return null;
        }
        return regions[localId];
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getTileCount() {
        return tileCount;
    }

    public void dispose() {
        if(texture != null){
            texture.dispose();
            texture = null;
        }
        regions = null;
    }
}

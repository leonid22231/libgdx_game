package com.lyadev.mygame.base.world.topdown;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lyadev.mygame.services.MainService;

/** Draws an orthogonal tile layer + click marker. */
public final class OrthoMapActor extends Actor {
    private static final Color TILE_FILL = new Color(1f, 0.9f, 0.2f, 0.25f);
    private static final Color TILE_LINE = new Color(1f, 0.95f, 0.35f, 0.95f);
    private static final Color CLICK_POINT = new Color(1f, 0.25f, 0.2f, 1f);

    private final OrthoTileMap map;
    private final OrthoTileset tileset;
    private final float drawScale;
    private final float originX;
    private final float originY;

    private boolean hasClickMarker;
    private float clickWorldX;
    private float clickWorldY;
    private int clickCol = -1;
    private int clickRow = -1;

    private boolean drawTiles = true;

    public OrthoMapActor(OrthoTileMap map, OrthoTileset tileset, float drawScale, float originX, float originY) {
        this.map = map;
        this.tileset = tileset;
        this.drawScale = drawScale;
        this.originX = originX;
        this.originY = originY;

        float tile = tileSize();
        float mapW = map.getWidth() * tile;
        float mapH = map.getHeight() * tile;
        setSize(mapW, mapH);
        setPosition(originX - mapW * 0.5f, originY - mapH * 0.5f);
    }

    public void setDrawTiles(boolean drawTiles) {
        this.drawTiles = drawTiles;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float tile = tileSize();
        if(drawTiles){
            for(int row = 0; row < map.getHeight(); row++){
                for(int col = 0; col < map.getWidth(); col++){
                    int id = map.get(col, row);
                    if(id < 0){
                        continue;
                    }
                    TextureRegion region = tileset.get(id);
                    if(region == null){
                        continue;
                    }
                    float x = getX() + col * tile;
                    float y = getY() + row * tile;
                    batch.setColor(1f, 1f, 1f, parentAlpha);
                    batch.draw(region, x, y, tile, tile);
                }
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }
        if(hasClickMarker){
            drawClickMarker(batch, tile);
        }
    }

    public OrthoTileHit pickWorld(float worldX, float worldY) {
        float tile = tileSize();
        int col = (int) Math.floor((worldX - getX()) / tile);
        int row = (int) Math.floor((worldY - getY()) / tile);
        boolean inBounds = col >= 0 && row >= 0 && col < map.getWidth() && row < map.getHeight();
        int tileId = inBounds ? map.get(col, row) : -1;
        return new OrthoTileHit(worldX, worldY, col, row, tileId, inBounds);
    }

    public void setClickProjection(OrthoTileHit hit) {
        if(hit == null){
            clearClickMarker();
            return;
        }
        hasClickMarker = true;
        clickWorldX = hit.getWorldX();
        clickWorldY = hit.getWorldY();
        clickCol = hit.getCol();
        clickRow = hit.getRow();
    }

    public void clearClickMarker() {
        hasClickMarker = false;
        clickCol = -1;
        clickRow = -1;
    }

    /** World position of tile bottom-left. */
    public float tileWorldX(int col) {
        return getX() + col * tileSize();
    }

    public float tileWorldY(int row) {
        return getY() + row * tileSize();
    }

    public float tileSize() {
        return tileset.getTileSize() * drawScale;
    }

    /**
     * Keep world positions of existing cells stable after {@link OrthoTileMap#expandToInclude}.
     * Bottom-left moves by {@code -pad * tile}; size grows to the new grid.
     */
    public void applyGridExpand(int padLeft, int padBottom) {
        float tile = tileSize();
        if(padLeft != 0 || padBottom != 0){
            setPosition(getX() - padLeft * tile, getY() - padBottom * tile);
        }
        setSize(map.getWidth() * tile, map.getHeight() * tile);
    }

    public OrthoTileMap getMap() {
        return map;
    }

    public OrthoTileset getTileset() {
        return tileset;
    }

    private void drawClickMarker(Batch batch, float tile) {
        ShapeRenderer shape = MainService.getInstance().getShapeRenderer();
        batch.end();
        float x = tileWorldX(clickCol);
        float y = tileWorldY(clickRow);
        shape.begin(ShapeType.Filled);
        shape.setColor(TILE_FILL);
        shape.rect(x, y, tile, tile);
        shape.end();
        shape.begin(ShapeType.Line);
        shape.setColor(TILE_LINE);
        shape.rect(x, y, tile, tile);
        shape.setColor(CLICK_POINT);
        float arm = Math.max(3f, tile * 0.25f);
        shape.line(clickWorldX - arm, clickWorldY, clickWorldX + arm, clickWorldY);
        shape.line(clickWorldX, clickWorldY - arm, clickWorldX, clickWorldY + arm);
        shape.end();
        batch.begin();
    }
}

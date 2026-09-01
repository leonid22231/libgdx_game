package com.lyadev.mygame.base.world.topdown;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.debug.DebugEntityService;
import com.lyadev.mygame.modules.prop.PropAtlasCache;
import com.lyadev.mygame.modules.prop.PropModule;
import com.lyadev.mygame.modules.prop.PropSettings;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.base.world.GlobalWorld;
import com.lyadev.mygame.base.world.WorldCameraControl;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * In-game map editor with asset palette.
 * <p>
 * M toggle · 1 ground / 2 decor · LMB paint|place · RMB pick|delete · wheel cycle ·
 * Ctrl+wheel zoom · MMB pan · F5 save
 * <p>
 * Ground = Entity+TileBlockModule · Decor = Entity+PropModule · palette · F5 save
 */
public final class OrthoMapEditor {
    public enum Mode {
        GROUND,
        DECOR
    }

    private static final String TAG = "OrthoMapEditor";
    private static final Vector3 TMP = new Vector3();
    private static final Matrix4 SCREEN = new Matrix4();
    private static final Color HOVER = new Color(0.2f, 0.9f, 1f, 0.35f);
    private static final Color HOVER_LINE = new Color(0.2f, 0.95f, 1f, 0.95f);
    private static final Color PANEL_BG = new Color(0.08f, 0.09f, 0.11f, 0.92f);
    private static final Color HINT_BG = new Color(0.06f, 0.07f, 0.09f, 0.88f);
    private static final Color SELECT = new Color(1f, 0.85f, 0.2f, 1f);

    private static final float PANEL_HEIGHT = 88f;
    private static final float CELL = 40f;
    private static final float CELL_PAD = 4f;
    private static final float ZOOM_MIN = 0.35f;
    private static final float ZOOM_MAX = 3.5f;
    private static final float ZOOM_STEP = 0.12f;
    private static final String[] HINTS = {
            "M - exit editor",
            "1 / 2 - ground / decor",
            "LMB - paint / place (grows map)",
            "RMB - erase tile / delete decor",
            "Wheel - cycle brush",
            "Ctrl+Wheel - zoom",
            "MMB drag - pan camera",
            "F5 - save map"
    };

    private static boolean enabled;
    private static Mode mode = Mode.GROUND;
    private static int brushTileId;
    private static int brushPropIndex;
    private static int hoverCol = -1;
    private static int hoverRow = -1;
    private static boolean dirty;
    private static String status = "off";
    private static float savedZoom = 1f;

    private OrthoMapEditor() {
        throw new UnsupportedOperationException();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Mode getMode() {
        return mode;
    }

    public static boolean toggle() {
        enabled = !enabled;
        if(enabled){
            WorldCameraControl.pauseFollow();
            savedZoom = currentZoom();
            applyPlayableVisibility(false);
            status = "editing";
            Gdx.app.log(TAG, "Editor ON");
        } else {
            setZoom(savedZoom);
            applyPlayableVisibility(true);
            status = dirty ? "OFF *" : "OFF";
            Gdx.app.log(TAG, "Editor OFF");
        }
        return enabled;
    }

    /** Ctrl+wheel: amountY &lt; 0 (scroll up) zooms in. */
    public static void zoomByScroll(float amountY) {
        if(!enabled){
            return;
        }
        float next = currentZoom() + (amountY > 0f ? ZOOM_STEP : -ZOOM_STEP);
        setZoom(MathUtils.clamp(next, ZOOM_MIN, ZOOM_MAX));
        status = String.format("zoom=%.2f", currentZoom());
    }

    public static void setMode(Mode next) {
        mode = next;
        status = "mode=" + mode;
    }

    public static void cycleBrush(int delta) {
        if(mode == Mode.GROUND){
            OrthoTileset tileset = tileset();
            int count = tileset == null ? 1 : Math.max(1, tileset.getTileCount());
            brushTileId = Math.floorMod(brushTileId + delta, count);
            status = "tile=" + brushTileId;
            return;
        }
        AssetCatalog catalog = TopDownMapWorld.getCatalog();
        if(catalog == null || catalog.getProps().isEmpty()){
            return;
        }
        brushPropIndex = Math.floorMod(brushPropIndex + delta, catalog.getProps().size());
        status = "prop=" + catalog.getProps().get(brushPropIndex).id;
    }

    public static void updateHover(int screenX, int screenY) {
        if(!enabled || isOverPalette(screenY)){
            hoverCol = -1;
            hoverRow = -1;
            return;
        }
        OrthoTileHit hit = hit(screenX, screenY);
        if(hit == null){
            hoverCol = -1;
            hoverRow = -1;
            return;
        }
        // Show cell even outside current map — LMB will expand the grid.
        hoverCol = hit.getCol();
        hoverRow = hit.getRow();
    }

    /** @return true if click consumed by editor */
    public static boolean handleTouch(int screenX, int screenY, boolean leftButton) {
        if(!enabled){
            return false;
        }
        if(isOverPalette(screenY)){
            selectFromPalette(screenX, screenY);
            return true;
        }
        OrthoTileHit hit = hit(screenX, screenY);
        if(hit == null){
            return true;
        }
        int col = hit.getCol();
        int row = hit.getRow();
        if(mode == Mode.GROUND){
            if(leftButton){
                paintGround(col, row);
            } else {
                eraseGround(col, row);
            }
        } else {
            if(!hit.isInBounds()){
                int[] cell = TopDownMapWorld.ensureEditableCell(col, row);
                if(cell == null){
                    status = "map expand failed (max 256)";
                    return true;
                }
                col = cell[0];
                row = cell[1];
                dirty = true;
            }
            if(leftButton){
                placeDecor(col, row);
            } else {
                deleteDecor(col, row);
            }
        }
        hoverCol = col;
        hoverRow = row;
        return true;
    }

    public static boolean save() {
        OrthoMapData data = TopDownMapWorld.getMapData();
        OrthoTileMap map = map();
        String path = TopDownMapWorld.getMapPath();
        if(data == null || map == null || path == null){
            status = "save failed";
            return false;
        }

        JSONObject root = new JSONObject();
        root.put("name", data.getName());
        root.put("tileSize", data.getTileSize());
        root.put("tileset", data.getTilesetPath());
        root.put("tilesetColumns", data.getTilesetColumns());
        root.put("decorAtlas", data.getDecorAtlas());
        root.put("width", map.getWidth());
        root.put("height", map.getHeight());

        JSONObject spawn = new JSONObject();
        spawn.put("col", data.getSpawnCol());
        spawn.put("row", data.getSpawnRow());
        root.put("spawn", spawn);

        JSONArray ground = new JSONArray();
        for(int fileRow = 0; fileRow < map.getHeight(); fileRow++){
            int mapRow = map.getHeight() - 1 - fileRow;
            StringBuilder line = new StringBuilder();
            for(int col = 0; col < map.getWidth(); col++){
                if(col > 0){
                    line.append(',');
                }
                line.append(map.get(col, mapRow));
            }
            ground.put(line.toString());
        }
        root.put("ground", ground);

        // Decor from live Prop entities.
        JSONArray decor = new JSONArray();
        for(Entity entity : TopDownMapWorld.getPropEntities()){
            PropModule prop = PropModule.from(entity);
            if(prop == null){
                continue;
            }
            PropSettings s = prop.getSettings();
            JSONObject d = new JSONObject();
            d.put("asset", s.getAssetId());
            JSONArray src = new JSONArray();
            src.put(s.getSrcX());
            src.put(s.getSrcY());
            src.put(s.getSrcW());
            src.put(s.getSrcH());
            d.put("src", src);
            d.put("col", prop.getTileCol());
            d.put("row", prop.getTileRow());
            decor.put(d);
        }
        root.put("decor", decor);

        FileHandle out = Gdx.files.local(path);
        out.writeString(root.toString(2) + "\n", false, "UTF-8");
        dirty = false;
        status = "saved " + out.path() + " (props=" + decor.length() + ")";
        Gdx.app.log(TAG, "Saved " + out.file().getAbsolutePath());
        return true;
    }

    public static void drawOverlay(SpriteBatch batch, ShapeRenderer shape, BitmapFont font) {
        if(!enabled){
            return;
        }
        OrthoMapActor actor = TopDownMapWorld.getMapActor();
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(actor == null || viewport == null){
            return;
        }
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        shape.setProjectionMatrix(camera.combined);

        OrthoTileMap map = actor.getMap();
        float tile = actor.tileSize();
        float mapX = actor.tileWorldX(0);
        float mapY = actor.tileWorldY(0);
        float mapW = map.getWidth() * tile;
        float mapH = map.getHeight() * tile;
        shape.begin(ShapeType.Line);
        shape.setColor(0.25f, 0.85f, 1f, 0.9f);
        shape.rect(mapX, mapY, mapW, mapH);
        shape.end();

        if(hoverCol >= 0 && hoverRow >= 0){
            float x = actor.tileWorldX(hoverCol);
            float y = actor.tileWorldY(hoverRow);
            // Outside current grid: preview where expand will place the cell.
            if(hoverCol < 0 || hoverRow < 0
                    || hoverCol >= map.getWidth() || hoverRow >= map.getHeight()){
                x = mapX + hoverCol * tile;
                y = mapY + hoverRow * tile;
            }
            shape.begin(ShapeType.Filled);
            shape.setColor(HOVER);
            shape.rect(x, y, tile, tile);
            shape.end();
            shape.begin(ShapeType.Line);
            shape.setColor(HOVER_LINE);
            shape.rect(x, y, tile, tile);
            shape.end();
        }

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        SCREEN.setToOrtho2D(0, 0, screenW, screenH);
        shape.setProjectionMatrix(SCREEN);
        shape.begin(ShapeType.Filled);
        shape.setColor(PANEL_BG);
        shape.rect(0, 0, screenW, PANEL_HEIGHT);
        shape.end();

        batch.setProjectionMatrix(SCREEN);
        batch.begin();
        font.setColor(Color.WHITE);
        OrthoTileMap liveMap = map();
        String size = liveMap == null ? "?" : liveMap.getWidth() + "x" + liveMap.getHeight();
        String header = "EDIT " + mode + (dirty ? " *" : "") + " | map=" + size + " | " + status
                + String.format(" | zoom=%.2f", currentZoom());
        font.draw(batch, header, 8, PANEL_HEIGHT + 16);

        if(mode == Mode.GROUND){
            drawGroundPalette(batch, shape, font);
        } else {
            drawDecorPalette(batch, shape, font);
        }
        drawHints(batch, shape, font, screenW, screenH);
        batch.end();
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);
    }

    private static void drawHints(SpriteBatch batch, ShapeRenderer shape, BitmapFont font,
            float screenW, float screenH) {
        float lineH = 16f;
        float pad = 10f;
        float boxW = 220f;
        float boxH = pad * 2f + (HINTS.length + 1) * lineH;
        float boxX = screenW - boxW - 12f;
        float boxY = screenH - boxH - 12f;

        batch.end();
        shape.begin(ShapeType.Filled);
        shape.setColor(HINT_BG);
        shape.rect(boxX, boxY, boxW, boxH);
        shape.end();
        shape.begin(ShapeType.Line);
        shape.setColor(0.3f, 0.35f, 0.42f, 1f);
        shape.rect(boxX, boxY, boxW, boxH);
        shape.end();
        batch.begin();

        font.setColor(0.85f, 0.9f, 0.95f, 1f);
        float y = boxY + boxH - pad;
        font.draw(batch, "Controls", boxX + pad, y);
        y -= lineH;
        font.setColor(Color.WHITE);
        for(String hint : HINTS){
            font.draw(batch, hint, boxX + pad, y);
            y -= lineH;
        }
    }

    private static void applyPlayableVisibility(boolean show) {
        if(GlobalWorld.entities == null){
            return;
        }
        for(Entity entity : GlobalWorld.entities){
            if(DebugEntityService.isMapBlock(entity)){
                continue;
            }
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.updateVisibility();
                if(!show){
                    entity.setVisible(false);
                }
            } else {
                entity.setVisible(show);
            }
        }
    }

    private static float currentZoom() {
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(viewport == null || !(viewport.getCamera() instanceof OrthographicCamera)){
            return 1f;
        }
        return ((OrthographicCamera) viewport.getCamera()).zoom;
    }

    private static void setZoom(float zoom) {
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(viewport == null || !(viewport.getCamera() instanceof OrthographicCamera)){
            return;
        }
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        camera.zoom = MathUtils.clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        camera.update();
    }

    private static void drawGroundPalette(SpriteBatch batch, ShapeRenderer shape, BitmapFont font) {
        OrthoTileset tileset = tileset();
        if(tileset == null){
            return;
        }
        int maxShow = (int) ((Gdx.graphics.getWidth() - 16) / (CELL + CELL_PAD));
        int start = Math.max(0, brushTileId - maxShow / 2);
        int end = Math.min(tileset.getTileCount(), start + maxShow);
        float x = 8f;
        float y = 12f;
        batch.end();
        for(int id = start; id < end; id++){
            if(id == brushTileId){
                shape.begin(ShapeType.Line);
                shape.setColor(SELECT);
                shape.rect(x - 1, y - 1, CELL + 2, CELL + 2);
                shape.end();
            }
            x += CELL + CELL_PAD;
        }
        batch.begin();
        x = 8f;
        for(int id = start; id < end; id++){
            TextureRegion region = tileset.get(id);
            if(region != null){
                batch.draw(region, x, y, CELL, CELL);
            }
            font.getData().setScale(0.7f);
            font.draw(batch, String.valueOf(id), x + 2, y + CELL - 2);
            font.getData().setScale(1f);
            x += CELL + CELL_PAD;
        }
    }

    private static void drawDecorPalette(SpriteBatch batch, ShapeRenderer shape, BitmapFont font) {
        AssetCatalog catalog = TopDownMapWorld.getCatalog();
        if(catalog == null){
            return;
        }
        List<AssetCatalog.PropDef> props = catalog.getProps();
        float x = 8f;
        float y = 12f;
        batch.end();
        for(int i = 0; i < props.size(); i++){
            if(i == brushPropIndex){
                shape.begin(ShapeType.Line);
                shape.setColor(SELECT);
                shape.rect(x - 1, y - 1, CELL + 2, CELL + 2);
                shape.end();
            }
            x += CELL + CELL_PAD;
        }
        batch.begin();
        x = 8f;
        for(int i = 0; i < props.size(); i++){
            AssetCatalog.PropDef def = props.get(i);
            TextureRegion region = PropAtlasCache.region(def.atlasPath, def.srcX, def.srcY, def.srcW, def.srcH);
            batch.draw(region, x, y, CELL, CELL);
            x += CELL + CELL_PAD;
        }
    }

    private static void selectFromPalette(int screenX, int screenY) {
        float x = 8f;
        float cell = CELL + CELL_PAD;
        int index = (int) ((screenX - x) / cell);
        if(index < 0){
            return;
        }
        if(mode == Mode.GROUND){
            OrthoTileset tileset = tileset();
            if(tileset == null){
                return;
            }
            int maxShow = (int) ((Gdx.graphics.getWidth() - 16) / cell);
            int start = Math.max(0, brushTileId - maxShow / 2);
            int id = start + index;
            if(id >= 0 && id < tileset.getTileCount()){
                brushTileId = id;
                status = "tile=" + brushTileId;
            }
            return;
        }
        AssetCatalog catalog = TopDownMapWorld.getCatalog();
        if(catalog == null || index >= catalog.getProps().size()){
            return;
        }
        brushPropIndex = index;
        status = "prop=" + catalog.getProps().get(brushPropIndex).id;
    }

    private static void paintGround(int col, int row) {
        int[] cell = TopDownMapWorld.ensureEditableCell(col, row);
        if(cell == null){
            status = "map expand failed (max 256)";
            return;
        }
        col = cell[0];
        row = cell[1];
        OrthoTileMap map = map();
        if(map == null){
            return;
        }
        if(map.get(col, row) == brushTileId){
            return;
        }
        TopDownMapWorld.placeTileBlock(brushTileId, col, row);
        dirty = true;
        status = "tile [" + col + "," + row + "]=" + brushTileId
                + " map=" + map.getWidth() + "x" + map.getHeight();
    }

    private static void eraseGround(int col, int row) {
        OrthoTileMap map = map();
        if(map == null || col < 0 || row < 0
                || col >= map.getWidth() || row >= map.getHeight()){
            status = "erase outside map";
            return;
        }
        if(map.get(col, row) < 0){
            return;
        }
        TopDownMapWorld.removeTileBlockAt(col, row);
        dirty = true;
        status = "erase [" + col + "," + row + "]";
    }

    private static void placeDecor(int col, int row) {
        AssetCatalog catalog = TopDownMapWorld.getCatalog();
        if(catalog == null || catalog.getProps().isEmpty()){
            status = "no props in catalog";
            return;
        }
        TopDownMapWorld.removePropAt(col, row);
        AssetCatalog.PropDef def = catalog.getProps().get(brushPropIndex);
        PropSettings settings = catalog.toSettings(def, TopDownMapWorld.DRAW_SCALE);
        TopDownMapWorld.placeProp(settings, col, row);
        dirty = true;
        status = "place " + def.id + " @[" + col + "," + row + "]";
    }

    private static void deleteDecor(int col, int row) {
        if(TopDownMapWorld.removePropAt(col, row)){
            dirty = true;
            status = "delete prop @[" + col + "," + row + "]";
        }
    }

    private static boolean isOverPalette(int screenY) {
        // LibGDX input Y: 0 = top. Panel is at bottom of screen.
        return screenY >= Gdx.graphics.getHeight() - PANEL_HEIGHT;
    }

    private static OrthoTileHit hit(int screenX, int screenY) {
        OrthoMapActor actor = TopDownMapWorld.getMapActor();
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(actor == null || viewport == null){
            return null;
        }
        TMP.set(screenX, screenY, 0f);
        viewport.unproject(TMP);
        return actor.pickWorld(TMP.x, TMP.y);
    }

    private static OrthoTileMap map() {
        OrthoMapActor actor = TopDownMapWorld.getMapActor();
        return actor == null ? null : actor.getMap();
    }

    private static OrthoTileset tileset() {
        OrthoMapActor actor = TopDownMapWorld.getMapActor();
        return actor == null ? null : actor.getTileset();
    }
}

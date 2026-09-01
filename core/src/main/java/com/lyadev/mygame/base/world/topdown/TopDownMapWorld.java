package com.lyadev.mygame.base.world.topdown;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.modules.prop.PropAtlasCache;
import com.lyadev.mygame.modules.prop.PropSettings;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.base.world.JsonMapWorld;
import com.lyadev.mygame.base.world.WorldController;
import com.lyadev.mygame.base.world.WorldEntity;

/**
 * Facade over the active {@link JsonMapWorld} for editor / legacy callers.
 * Real state lives on {@link com.lyadev.mygame.base.world.WorldEntity} instances.
 */
public final class TopDownMapWorld {
    public static final String DEFAULT_MAP = "maps/forest_lake.json";
    public static final float DRAW_SCALE = JsonMapWorld.DRAW_SCALE;

    private static final Vector3 TMP = new Vector3();
    private static OrthoTileHit lastHit;

    private TopDownMapWorld() {
        throw new UnsupportedOperationException();
    }

    private static JsonMapWorld activeJson() {
        WorldEntity active = WorldController.getActive();
        return active instanceof JsonMapWorld ? (JsonMapWorld) active : null;
    }

    /** @deprecated Prefer {@link WorldController#start(String, Stage, float, float)}. */
    @Deprecated
    public static OrthoMapActor init(Stage stage, float worldCenterX, float worldCenterY) {
        return getMapActor();
    }

    public static Entity placeDoorEntity(Entity door, int col, int row) {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.placeDoorEntity(door, col, row);
    }

    public static void spawnMapEntities() {
        JsonMapWorld world = activeJson();
        if(world != null){
            world.spawnMapEntities();
        }
    }

    public static Entity placeTileBlock(int tileId, int col, int row) {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.placeTileBlock(tileId, col, row);
    }

    public static Entity placeTileBlock(int tileId, int col, int row, float size) {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.placeTileBlock(tileId, col, row, size);
    }

    public static int[] ensureEditableCell(int col, int row) {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.ensureEditableCell(col, row);
    }

    public static boolean removeTileBlockAt(int col, int row) {
        JsonMapWorld world = activeJson();
        return world != null && world.removeTileBlockAt(col, row);
    }

    public static Entity placeProp(PropSettings settings, int col, int row) {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.placeProp(settings, col, row);
    }

    public static boolean removePropAt(int col, int row) {
        JsonMapWorld world = activeJson();
        return world != null && world.removePropAt(col, row);
    }

    public static List<Entity> getTileEntities() {
        JsonMapWorld world = activeJson();
        return world == null ? Collections.emptyList() : world.getTileEntities();
    }

    public static List<Entity> getPropEntities() {
        JsonMapWorld world = activeJson();
        return world == null ? Collections.emptyList() : world.getPropEntities();
    }

    public static AssetCatalog getCatalog() {
        return AssetCatalog.ensureLoaded();
    }

    public static OrthoTileset getTileset() {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.getTileset();
    }

    public static void placeEntityOnTile(Entity entity, int col, int row) {
        JsonMapWorld world = activeJson();
        if(world != null){
            world.placeEntityOnTile(entity, col, row);
        }
    }

    public static void placeEntityAtSpawn(Entity entity) {
        JsonMapWorld world = activeJson();
        if(world != null){
            world.placeEntityAtSpawn(entity);
        }
    }

    public static OrthoTileHit projectScreenClick(int screenX, int screenY) {
        OrthoMapActor mapActor = getMapActor();
        if(mapActor == null){
            return null;
        }
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(viewport == null){
            return null;
        }
        TMP.set(screenX, screenY, 0f);
        viewport.unproject(TMP);
        OrthoTileHit hit = mapActor.pickWorld(TMP.x, TMP.y);
        mapActor.setClickProjection(hit);
        lastHit = hit;
        return hit;
    }

    public static OrthoMapData getMapData() {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.getMapData();
    }

    public static String getMapPath() {
        WorldEntity active = WorldController.getActive();
        return active == null ? null : active.getMapPath();
    }

    public static OrthoTileHit getLastHit() {
        return lastHit;
    }

    public static OrthoMapActor getMapActor() {
        JsonMapWorld world = activeJson();
        return world == null ? null : world.getMapActor();
    }

    public static boolean drawsSkyBackground() {
        WorldEntity active = WorldController.getActive();
        return active == null || active.drawsSkyBackground();
    }

    public static void dispose() {
        lastHit = null;
        PropAtlasCache.disposeAll();
        AssetCatalog.disposeShared();
        WorldController.dispose();
    }
}

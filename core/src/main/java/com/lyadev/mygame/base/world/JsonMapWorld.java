package com.lyadev.mygame.base.world;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.modules.door.DoorModule;
import com.lyadev.mygame.modules.door.DoorSettings;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.prop.PropModule;
import com.lyadev.mygame.modules.prop.PropSettings;
import com.lyadev.mygame.modules.tile.TileBlockModule;
import com.lyadev.mygame.base.world.doors.DoorEntity;
import com.lyadev.mygame.base.world.props.PropEntity;
import com.lyadev.mygame.base.world.tiles.TileBlockEntity;
import com.lyadev.mygame.base.world.topdown.AssetCatalog;
import com.lyadev.mygame.base.world.topdown.OrthoMapActor;
import com.lyadev.mygame.base.world.topdown.OrthoMapData;
import com.lyadev.mygame.base.world.topdown.OrthoMapLoader;
import com.lyadev.mygame.base.world.topdown.OrthoTileMap;
import com.lyadev.mygame.base.world.topdown.OrthoTileset;

/**
 * Default top-down world backed by {@code assets/maps/*.json}.
 * Owns tileset, map actor, tile/prop entities for this world only.
 * Concrete maps (forest, house, …) extend this from {@code com.lyadev.mygame.worlds}.
 */
public class JsonMapWorld extends WorldEntity {
    public static final float DRAW_SCALE = 1f;

    private final String mapPath;
    private final boolean skyBackground;

    private OrthoTileset tileset;
    private OrthoMapActor mapActor;
    private OrthoMapData mapData;
    private Entity[][] tileGrid;
    private final List<Entity> tileEntities = new ArrayList<>();
    private final List<Entity> propEntities = new ArrayList<>();
    private boolean mapContentSpawned;
    private boolean portalsInstalled;
    private Stage stageRef;
    private float centerX;
    private float centerY;

    public JsonMapWorld(String id, String displayName, String mapPath, boolean skyBackground) {
        super(id, displayName);
        this.mapPath = mapPath;
        this.skyBackground = skyBackground;
    }

    @Override
    public String getMapPath() {
        return mapPath;
    }

    @Override
    public boolean drawsSkyBackground() {
        return skyBackground;
    }

    @Override
    public OrthoMapActor getMapActor() {
        return mapActor;
    }

    @Override
    public OrthoMapData getMapData() {
        return mapData;
    }

    @Override
    public OrthoTileset getTileset() {
        return tileset;
    }

    public List<Entity> getTileEntities() {
        return tileEntities;
    }

    public List<Entity> getPropEntities() {
        return propEntities;
    }

    @Override
    protected void onLoad(Stage stage, float cx, float cy) {
        this.stageRef = stage;
        this.centerX = cx;
        this.centerY = cy;
        AssetCatalog.ensureLoaded();
        mapData = OrthoMapLoader.load(mapPath);
        tileset = new OrthoTileset(mapData.getTilesetPath(), mapData.getTileSize(), mapData.getTilesetColumns());
        tileset.load();
        mapActor = new OrthoMapActor(mapData.getMap(), tileset, DRAW_SCALE, centerX, centerY);
        mapActor.setDrawTiles(false);
        tileGrid = new Entity[mapData.getMap().getHeight()][mapData.getMap().getWidth()];
        Gdx.app.log("JsonMapWorld", "Loaded " + getId() + " " + mapData.getMap().getWidth()
                + "x" + mapData.getMap().getHeight());
    }

    @Override
    protected void onActivate(Entity traveler, Integer spawnCol, Integer spawnRow) {
        if(mapActor != null && mapActor.getStage() == null && stageRef != null){
            stageRef.addActor(mapActor);
            mapActor.toBack();
        }
        if(!mapContentSpawned){
            spawnMapEntities();
            mapContentSpawned = true;
        } else {
            showMapEntities();
        }
        for(Entity entity : getEntities()){
            if(isMapBlock(entity)){
                continue;
            }
            if(entity == traveler){
                continue;
            }
            showPlayable(entity);
        }
        if(traveler != null){
            WorldController.ensureTravelerOnStage(traveler);
            traveler.setVisible(true);
            int col = spawnCol != null ? spawnCol : mapData.getSpawnCol();
            int row = spawnRow != null ? spawnRow : mapData.getSpawnRow();
            placeEntityOnTile(traveler, col, row);
            MovementModule movement = MovementModule.from(traveler);
            if(movement != null){
                movement.resetControlState();
            }
        }
        if(!portalsInstalled){
            installPortals();
            portalsInstalled = true;
        }
        onActivated();
    }

    @Override
    protected void onDeactivate(Entity travelerLeaving) {
        for(Entity entity : new ArrayList<>(getEntities())){
            if(entity == travelerLeaving){
                continue;
            }
            if(isMapBlock(entity)){
                entity.remove();
                continue;
            }
            MovementModule movement = MovementModule.from(entity);
            if(movement != null){
                movement.stopMoving();
            }
            entity.remove();
            entity.setVisible(false);
        }
        if(mapActor != null){
            mapActor.remove();
        }
    }

    @Override
    protected void onDispose() {
        clearTileEntities(true);
        clearPropEntities(true);
        if(mapActor != null){
            mapActor.remove();
            mapActor = null;
        }
        if(tileset != null){
            tileset.dispose();
            tileset = null;
        }
        mapData = null;
        tileGrid = null;
        mapContentSpawned = false;
        portalsInstalled = false;
    }

    /** Override in concrete worlds to place doors. */
    protected void installPortals() {
    }

    @Override
    public void placeTraveler(Entity traveler, int col, int row) {
        if(traveler == null){
            return;
        }
        if(!contains(traveler)){
            addEntity(traveler);
        }
        WorldController.ensureTravelerOnStage(traveler);
        traveler.setVisible(true);
        placeEntityOnTile(traveler, col, row);
    }

    public void placeEntityOnTile(Entity entity, int col, int row) {
        if(mapActor == null || entity == null){
            return;
        }
        OrthoTileMap map = mapActor.getMap();
        col = Math.max(0, Math.min(col, map.getWidth() - 1));
        row = Math.max(0, Math.min(row, map.getHeight() - 1));
        float tile = mapActor.tileSize();
        float x = mapActor.tileWorldX(col) + (tile - entity.getWidth()) * 0.5f;
        float y = mapActor.tileWorldY(row);
        entity.setPosition(x, y);
    }

    public void placeEntityAtSpawn(Entity entity) {
        if(mapData == null){
            return;
        }
        placeEntityOnTile(entity, mapData.getSpawnCol(), mapData.getSpawnRow());
    }

    public void spawnMapEntities() {
        spawnGroundTiles();
        spawnDecorProps();
    }

    public void spawnGroundTiles() {
        if(mapData == null || mapActor == null || tileset == null){
            return;
        }
        clearTileEntities(false);
        OrthoTileMap map = mapData.getMap();
        float size = mapActor.tileSize();
        int count = 0;
        for(int row = 0; row < map.getHeight(); row++){
            for(int col = 0; col < map.getWidth(); col++){
                int id = map.get(col, row);
                if(id < 0){
                    continue;
                }
                placeTileBlock(id, col, row, size);
                count++;
            }
        }
        Gdx.app.log("JsonMapWorld", getId() + " tiles=" + count);
    }

    public void spawnDecorProps() {
        if(mapData == null || mapActor == null){
            return;
        }
        clearPropEntities(false);
        for(OrthoMapData.DecorSpec spec : mapData.getDecor()){
            PropSettings settings = new PropSettings(
                    spec.getAssetId(),
                    spec.getAtlasPath(),
                    spec.getSrcX(), spec.getSrcY(), spec.getSrcW(), spec.getSrcH(),
                    DRAW_SCALE);
            placeProp(settings, spec.getCol(), spec.getRow());
        }
        Gdx.app.log("JsonMapWorld", getId() + " props=" + propEntities.size());
    }

    public Entity placeTileBlock(int tileId, int col, int row) {
        return placeTileBlock(tileId, col, row, mapActor != null ? mapActor.tileSize() : 16f);
    }

    public Entity placeTileBlock(int tileId, int col, int row, float size) {
        if(mapActor == null || tileset == null || tileGrid == null){
            return null;
        }
        removeTileBlockAt(col, row);
        Entity entity = TileBlockEntity.create(tileset, tileId, col, row, size);
        entity.setPosition(mapActor.tileWorldX(col), mapActor.tileWorldY(row));
        entity.setVisible(isActive());
        entity.setZIndex(1);
        tileGrid[row][col] = entity;
        tileEntities.add(entity);
        mapActor.getMap().set(col, row, tileId);
        addEntity(entity);
        if(isActive()){
            GlobalWorld.addEntityToStage(entity);
        }
        return entity;
    }

    public boolean removeTileBlockAt(int col, int row) {
        if(tileGrid == null || row < 0 || col < 0
                || row >= tileGrid.length || col >= tileGrid[0].length){
            return false;
        }
        Entity existing = tileGrid[row][col];
        if(existing == null){
            if(mapActor != null){
                mapActor.getMap().set(col, row, -1);
            }
            return false;
        }
        tileGrid[row][col] = null;
        tileEntities.remove(existing);
        if(mapActor != null){
            mapActor.getMap().set(col, row, -1);
        }
        removeEntity(existing);
        existing.remove();
        existing.dispose();
        return true;
    }

    public Entity placeProp(PropSettings settings, int col, int row) {
        if(mapActor == null){
            return null;
        }
        Entity entity = PropEntity.create(settings, col, row);
        float tile = mapActor.tileSize();
        float x = mapActor.tileWorldX(col) + (tile - entity.getWidth()) * 0.5f;
        float y = mapActor.tileWorldY(row);
        entity.setPosition(x, y);
        entity.setVisible(isActive());
        propEntities.add(entity);
        addEntity(entity);
        if(isActive()){
            GlobalWorld.addEntityToStage(entity);
        }
        return entity;
    }

    public boolean removePropAt(int col, int row) {
        Entity found = null;
        for(Entity entity : propEntities){
            PropModule prop = PropModule.from(entity);
            if(prop != null && prop.getTileCol() == col && prop.getTileRow() == row){
                found = entity;
                break;
            }
        }
        if(found == null){
            return false;
        }
        propEntities.remove(found);
        removeEntity(found);
        found.remove();
        found.dispose();
        return true;
    }

    public Entity placeDoorEntity(Entity door, int col, int row) {
        if(mapActor == null || door == null){
            return null;
        }
        float tile = mapActor.tileSize();
        float x = mapActor.tileWorldX(col) + (tile - door.getWidth()) * 0.5f;
        float y = mapActor.tileWorldY(row);
        door.setPosition(x, y);
        door.setVisible(isActive());
        propEntities.add(door);
        addEntity(door);
        if(isActive()){
            GlobalWorld.addEntityToStage(door);
        }
        return door;
    }

    public void placePortal(
            String assetId,
            int srcX, int srcY, int srcW, int srcH,
            int col, int row,
            DoorSettings door) {
        String atlas = mapData != null
                ? mapData.getDecorAtlas()
                : "tiles/topdown/decor/Decorations.png";
        PropSettings prop = new PropSettings(assetId, atlas, srcX, srcY, srcW, srcH, DRAW_SCALE);
        Entity entity = DoorEntity.create(prop, door, col, row);
        placeDoorEntity(entity, col, row);
        DoorModule.armCooldown();
    }

    public int[] ensureEditableCell(int col, int row) {
        if(mapActor == null || mapData == null || tileGrid == null){
            return null;
        }
        OrthoTileMap map = mapActor.getMap();
        if(col >= 0 && row >= 0 && col < map.getWidth() && row < map.getHeight()){
            return new int[]{col, row};
        }
        final int maxSize = 256;
        int[] pad;
        try {
            pad = map.expandToInclude(col, row, maxSize);
        } catch(IllegalArgumentException error) {
            Gdx.app.error("JsonMapWorld", error.getMessage());
            return null;
        }
        int padLeft = pad[0];
        int padBottom = pad[1];
        mapActor.applyGridExpand(padLeft, padBottom);
        rebuildTileGridAfterExpand(padLeft, padBottom, map.getWidth(), map.getHeight());
        if(padLeft != 0 || padBottom != 0){
            mapData.shiftSpawn(padLeft, padBottom);
            shiftPropCells(padLeft, padBottom);
        }
        return new int[]{col + padLeft, row + padBottom};
    }

    private void rebuildTileGridAfterExpand(int padLeft, int padBottom, int newW, int newH) {
        Entity[][] next = new Entity[newH][newW];
        if(tileGrid != null){
            for(int r = 0; r < tileGrid.length; r++){
                for(int c = 0; c < tileGrid[r].length; c++){
                    Entity entity = tileGrid[r][c];
                    if(entity == null){
                        continue;
                    }
                    int nc = c + padLeft;
                    int nr = r + padBottom;
                    next[nr][nc] = entity;
                    TileBlockModule tile = TileBlockModule.from(entity);
                    if(tile != null){
                        tile.setTileCell(nc, nr);
                    }
                }
            }
        }
        tileGrid = next;
    }

    private void shiftPropCells(int padLeft, int padBottom) {
        for(Entity entity : propEntities){
            PropModule prop = PropModule.from(entity);
            if(prop != null){
                prop.setTileCell(prop.getTileCol() + padLeft, prop.getTileRow() + padBottom);
            }
        }
    }

    private void showMapEntities() {
        for(Entity entity : tileEntities){
            entity.setVisible(true);
            if(entity.getStage() == null){
                GlobalWorld.addEntityToStage(entity);
            }
        }
        for(Entity entity : propEntities){
            entity.setVisible(true);
            if(entity.getStage() == null){
                GlobalWorld.addEntityToStage(entity);
            }
        }
    }

    private void showPlayable(Entity entity) {
        entity.setVisible(true);
        if(entity.getStage() == null){
            GlobalWorld.addEntityToStage(entity);
        }
    }

    private void clearTileEntities(boolean disposeEntities) {
        for(Entity entity : new ArrayList<>(tileEntities)){
            removeEntity(entity);
            entity.remove();
            if(disposeEntities){
                entity.dispose();
            }
        }
        tileEntities.clear();
        if(tileGrid != null){
            for(int r = 0; r < tileGrid.length; r++){
                for(int c = 0; c < tileGrid[r].length; c++){
                    tileGrid[r][c] = null;
                }
            }
        }
    }

    private void clearPropEntities(boolean disposeEntities) {
        for(Entity entity : new ArrayList<>(propEntities)){
            removeEntity(entity);
            entity.remove();
            if(disposeEntities){
                entity.dispose();
            }
        }
        propEntities.clear();
    }

    private static boolean isMapBlock(Entity entity) {
        return TileBlockModule.from(entity) != null || PropModule.from(entity) != null;
    }
}

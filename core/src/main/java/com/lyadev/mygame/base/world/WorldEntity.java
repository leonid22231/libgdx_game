package com.lyadev.mygame.base.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.world.topdown.OrthoMapActor;
import com.lyadev.mygame.base.world.topdown.OrthoMapData;
import com.lyadev.mygame.base.world.topdown.OrthoTileset;

/**
 * One game world instance: own entity list + map state.
 * Subclass via {@link JsonMapWorld}; concrete maps live in {@code com.lyadev.mygame.worlds}.
 */
public abstract class WorldEntity {
    private final String id;
    private final String displayName;
    private final List<Entity> entities = new ArrayList<>();
    private boolean loaded;
    private boolean active;

    protected WorldEntity(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public final String getId() {
        return id;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final boolean isLoaded() {
        return loaded;
    }

    public final boolean isActive() {
        return active;
    }

    public final List<Entity> getEntities() {
        return entities;
    }

    public final List<Entity> getEntitiesView() {
        return Collections.unmodifiableList(entities);
    }

    public void addEntity(Entity entity) {
        if(entity != null && !entities.contains(entity)){
            entities.add(entity);
        }
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public boolean contains(Entity entity) {
        return entities.contains(entity);
    }

    /** Load map resources once (tileset, actor, spawn tiles/props). */
    public final void load(Stage stage, float centerX, float centerY) {
        if(loaded){
            return;
        }
        onLoad(stage, centerX, centerY);
        loaded = true;
    }

    /**
     * Make this world the live one on stage.
     * {@code traveler} may be moved in from another world; spawn cell optional.
     */
    public final void activate(Entity traveler, Integer spawnCol, Integer spawnRow) {
        if(traveler != null && !entities.contains(traveler)){
            entities.add(traveler);
        }
        active = true;
        onActivate(traveler, spawnCol, spawnRow);
    }

    /**
     * Leave stage but keep entity/map state.
     * {@code travelerLeaving} is removed from this world's list (moved by controller).
     */
    public final void deactivate(Entity travelerLeaving) {
        onDeactivate(travelerLeaving);
        if(travelerLeaving != null){
            entities.remove(travelerLeaving);
        }
        active = false;
    }

    public final void dispose() {
        onDispose();
        entities.clear();
        loaded = false;
        active = false;
    }

    public abstract String getMapPath();

    public abstract boolean drawsSkyBackground();

    public abstract OrthoMapActor getMapActor();

    public abstract OrthoMapData getMapData();

    public abstract OrthoTileset getTileset();

    public void placeTraveler(Entity traveler, int col, int row) {
        if(traveler != null && !contains(traveler)){
            addEntity(traveler);
        }
    }

    /** Called when this world becomes active — install portals, etc. */
    protected void onActivated() {
    }

    protected abstract void onLoad(Stage stage, float centerX, float centerY);

    protected abstract void onActivate(Entity traveler, Integer spawnCol, Integer spawnRow);

    protected abstract void onDeactivate(Entity travelerLeaving);

    protected abstract void onDispose();
}

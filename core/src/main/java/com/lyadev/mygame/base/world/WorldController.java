package com.lyadev.mygame.base.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.modules.door.DoorModule;
import com.lyadev.mygame.modules.movement.MovementModule;

/**
 * Registry + active world switch. Travel moves only the traveler between WorldEntity instances.
 */
public final class WorldController {
    private static final String TAG = "WorldController";

    private static final Map<String, WorldEntity> worlds = new LinkedHashMap<>();
    private static WorldEntity active;
    private static Stage stageRef;
    private static float centerX;
    private static float centerY;

    private WorldController() {
        throw new UnsupportedOperationException();
    }

    public static void register(WorldEntity world) {
        if(world == null){
            return;
        }
        worlds.put(world.getId(), world);
    }

    public static WorldEntity get(String id) {
        return worlds.get(id);
    }

    public static WorldEntity getActive() {
        return active;
    }

    public static Collection<WorldEntity> allWorlds() {
        return worlds.values();
    }

    public static List<Entity> allEntities() {
        List<Entity> all = new ArrayList<>();
        for(WorldEntity world : worlds.values()){
            all.addAll(world.getEntities());
        }
        return all;
    }

    public static Stage getStage() {
        return stageRef;
    }

    public static float getCenterX() {
        return centerX;
    }

    public static float getCenterY() {
        return centerY;
    }

    /** Load every registered world once, activate {@code startWorldId}. */
    public static void start(String startWorldId, Stage stage, float worldCenterX, float worldCenterY) {
        stageRef = stage;
        centerX = worldCenterX;
        centerY = worldCenterY;
        for(WorldEntity world : worlds.values()){
            world.load(stage, worldCenterX, worldCenterY);
        }
        WorldEntity start = worlds.get(startWorldId);
        if(start == null){
            throw new IllegalStateException("Unknown start world: " + startWorldId);
        }
        active = start;
        start.activate(null, null, null);
        GlobalWorld.syncEntitiesFromActive();
        Gdx.app.log(TAG, "Started world " + start.getId() + " (" + worlds.size() + " registered)");
    }

    public static void travel(String targetWorldId, Entity traveler, int spawnCol, int spawnRow) {
        if(traveler == null || targetWorldId == null){
            Gdx.app.error(TAG, "travel: missing traveler/target");
            return;
        }
        WorldEntity target = worlds.get(targetWorldId);
        if(target == null){
            Gdx.app.error(TAG, "Unknown world: " + targetWorldId);
            return;
        }
        WorldEntity from = active;
        if(from == target){
            target.placeTraveler(traveler, spawnCol, spawnRow);
            return;
        }
        DoorModule.armCooldown();
        if(from != null){
            from.deactivate(traveler);
        }
        active = target;
        target.activate(traveler, spawnCol, spawnRow);
        GlobalWorld.syncEntitiesFromActive();

        MovementModule movement = traveler.getModule(MovementModule.class);
        if(movement != null){
            movement.resetControlState();
        }
        GlobalWorld.setActivePlayer(traveler);
        GlobalWorld.resetVisionState();
        GlobalWorld.sortEntitiesByDepth();
        Gdx.app.log(TAG, "Travel " + traveler.getTag() + " → " + target.getId()
                + (from != null ? " from " + from.getId() : ""));
    }

    /**
     * Activate another world without moving any entity.
     * Residents of the previous world stay parked there (including the player).
     */
    public static void switchActive(String targetWorldId) {
        if(targetWorldId == null){
            Gdx.app.error(TAG, "switchActive: missing target");
            return;
        }
        WorldEntity target = worlds.get(targetWorldId);
        if(target == null){
            Gdx.app.error(TAG, "Unknown world: " + targetWorldId);
            return;
        }
        WorldEntity from = active;
        if(from == target){
            Gdx.app.log(TAG, "Already active: " + target.getId());
            return;
        }
        DoorModule.armCooldown();
        if(from != null){
            from.deactivate(null);
        }
        active = target;
        target.activate(null, null, null);
        GlobalWorld.syncEntitiesFromActive();
        GlobalWorld.bindPlayerToActiveWorld();
        GlobalWorld.resetVisionState();
        GlobalWorld.sortEntitiesByDepth();
        Gdx.app.log(TAG, "Switch active → " + target.getId()
                + (from != null ? " (left " + from.getId() + " parked)" : ""));
    }

    public static void dispose() {
        for(WorldEntity world : worlds.values()){
            world.dispose();
        }
        worlds.clear();
        active = null;
        stageRef = null;
    }

    /** Hook used by JsonMapWorld after traveler is attached. */
    static void ensureTravelerOnStage(Entity traveler) {
        if(traveler != null && traveler.getStage() == null && stageRef != null){
            stageRef.addActor(traveler);
        }
    }
}

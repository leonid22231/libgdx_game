package com.lyadev.mygame.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.prop.PropModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.tile.TileBlockModule;
import com.lyadev.mygame.modules.vision.VisionModule;
import com.lyadev.mygame.persons.cat.CatPerson;
import com.lyadev.mygame.persons.man.ManPerson;
import com.lyadev.mygame.persons.newgirl.NewgirlPerson;
import com.lyadev.mygame.persons.woomen.WoomenPerson;
import com.lyadev.mygame.world.topdown.OrthoMapActor;
import com.lyadev.mygame.world.topdown.OrthoMapData;
import com.lyadev.mygame.world.topdown.TopDownMapWorld;
import com.lyadev.mygame.worlds.Worlds;

import lombok.Getter;

public class GlobalWorld {
    /** Live view of the active {@link WorldEntity} entity list. */
    public static List<Entity> entities;
    public static Entity player;
    private static Stage worldStage;
    @Getter
    private static boolean ready = false;

    public static void init(Stage stage){
        worldStage = stage;
        entities = new ArrayList<>();

        float centerX = WorldCameraSettings.WORLD_WIDTH * 0.5f;
        float centerY = WorldCameraSettings.WORLD_HEIGHT * 0.5f;

        Worlds.registerAll();
        WorldController.start(Worlds.startWorldId(), stage, centerX, centerY);
        syncEntitiesFromActive();

        Entity cat = CatPerson.create();
        Entity man = ManPerson.createDefault();
        Entity woman = WoomenPerson.create();
        Entity newgirl = NewgirlPerson.create();

        addEntity(cat);
        addEntity(man);
        addEntity(woman);
        addEntity(newgirl);

        for(Entity entity : List.of(cat, man, woman, newgirl)){
            stage.addActor(entity);
        }

        TopDownMapWorld.placeEntityAtSpawn(man);
        OrthoMapData data = TopDownMapWorld.getMapData();
        int sc = data != null ? data.getSpawnCol() : 10;
        int sr = data != null ? data.getSpawnRow() : 5;
        TopDownMapWorld.placeEntityOnTile(cat, sc - 2, sr);
        TopDownMapWorld.placeEntityOnTile(woman, sc + 2, sr);
        TopDownMapWorld.placeEntityOnTile(newgirl, sc, sr - 2);

        setActivePlayer(man);
        resetVisionState();
        refreshEntityVisibility();
        sortEntitiesByDepth();
        ready = true;
        Gdx.app.debug("GlobalWorld", "World ready. Active="
                + (WorldController.getActive() != null ? WorldController.getActive().getId() : "?")
                + " entities=" + entities.size());
    }

    /** Point {@link #entities} at the active world's list. */
    public static void syncEntitiesFromActive() {
        WorldEntity active = WorldController.getActive();
        entities = active != null ? active.getEntities() : new ArrayList<>();
    }

    public static void sortEntitiesByDepth() {
        if(entities == null || entities.isEmpty()){
            return;
        }
        if(worldStage != null){
            for(Actor actor : worldStage.getActors()){
                if(actor instanceof OrthoMapActor){
                    actor.toBack();
                    break;
                }
            }
        }

        List<Entity> ground = new ArrayList<>();
        List<Entity> overlay = new ArrayList<>();
        for(Entity entity : entities){
            if(TileBlockModule.from(entity) != null){
                ground.add(entity);
            } else {
                overlay.add(entity);
            }
        }
        ground.sort(Comparator.comparingInt(e -> TileBlockModule.from(e).getTileRow()));
        overlay.sort(Comparator.comparingDouble((Entity e) -> (double) e.getY()).reversed());

        int z = 1;
        for(Entity entity : ground){
            entity.setZIndex(z++);
        }
        for(Entity entity : overlay){
            entity.setZIndex(z++);
        }
    }

    public static void drawModuleUi(Batch batch, float parentAlpha) {
        if(entities == null || batch == null){
            return;
        }
        for(Entity entity : entities){
            entity.drawUi(batch, parentAlpha);
        }
    }

    public static void addEntityToStage(Entity entity) {
        if(worldStage != null && entity != null && entity.getStage() == null){
            worldStage.addActor(entity);
        }
    }

    public static void setActivePlayer(Entity activePlayer) {
        if(entities != null){
            for(Entity entity : entities){
                SelectableModule selectable = SelectableModule.from(entity);
                if(selectable != null){
                    selectable.setActive(entity == activePlayer);
                }
                MovementModule movement = entity.getModule(MovementModule.class);
                if(movement != null){
                    movement.stopMoving();
                }
            }
        }
        player = activePlayer;
        WorldCameraControl.resumeFollow();
        refreshEntityVisibility();
    }

    /**
     * After a world switch without traveler: keep control only if player is in the active world,
     * otherwise pick first playable resident or clear.
     */
    public static void bindPlayerToActiveWorld() {
        if(player != null && entities != null && entities.contains(player)){
            setActivePlayer(player);
            return;
        }
        Entity resident = findFirstPlayable();
        if(resident != null){
            setActivePlayer(resident);
            return;
        }
        player = null;
        if(entities != null){
            for(Entity entity : entities){
                SelectableModule selectable = SelectableModule.from(entity);
                if(selectable != null){
                    selectable.setActive(false);
                }
            }
        }
        refreshEntityVisibility();
    }

    private static Entity findFirstPlayable() {
        if(entities == null){
            return null;
        }
        for(Entity entity : entities){
            if(TileBlockModule.from(entity) != null || PropModule.from(entity) != null){
                continue;
            }
            if(SelectableModule.from(entity) != null){
                return entity;
            }
        }
        return null;
    }

    public static void refreshEntityVisibility() {
        if(entities == null){
            return;
        }
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.updateVisibility();
            }
        }
    }

    public static void resetVisionState() {
        if(entities == null){
            return;
        }
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.setVisibleByVision(false);
            }
            VisionModule vision = VisionModule.from(entity);
            if(vision != null){
                vision.resetVisionTracking();
            }
        }
        refreshEntityVisibility();
    }

    public static void addEntity(Entity entity) {
        WorldEntity active = WorldController.getActive();
        if(active != null){
            active.addEntity(entity);
            syncEntitiesFromActive();
        } else if(entities != null){
            entities.add(entity);
        }
    }

    public static void removeEntity(Entity entity) {
        WorldEntity active = WorldController.getActive();
        if(active != null){
            active.removeEntity(entity);
        } else if(entities != null){
            entities.remove(entity);
        }
    }

    public static void dispose() {
        TopDownMapWorld.dispose();
        ready = false;
        entities = new ArrayList<>();
        player = null;
    }

    public static void updateEntityMouseInfo(float screenX, float screenY) {
        if(entities == null){
            return;
        }
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.mousePositionListener(screenX, screenY);
            }
        }
    }

    public static void setAllRandomPositions() {
        if(TopDownMapWorld.getMapActor() == null || entities == null){
            return;
        }
        int w = TopDownMapWorld.getMapActor().getMap().getWidth();
        int h = TopDownMapWorld.getMapActor().getMap().getHeight();
        java.util.Random random = new java.util.Random();
        for(Entity entity : entities){
            if(TileBlockModule.from(entity) != null || PropModule.from(entity) != null){
                continue;
            }
            TopDownMapWorld.placeEntityOnTile(entity, 2 + random.nextInt(Math.max(1, w - 4)),
                    2 + random.nextInt(Math.max(1, h - 4)));
        }
        resetVisionState();
        sortEntitiesByDepth();
    }

    public static void handleEntityClick() {
        if(!SelectableModule.isPickMode() || entities == null){
            return;
        }
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.clickEvent();
            }
        }
    }

    public static String activeWorldLabel() {
        WorldEntity active = WorldController.getActive();
        if(active == null){
            return "none";
        }
        return active.getId() + " (" + active.getDisplayName() + ")";
    }
}

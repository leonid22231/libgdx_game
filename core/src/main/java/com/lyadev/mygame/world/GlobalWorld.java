package com.lyadev.mygame.world;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.Assets;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.PlayableEntitySettings;
import com.lyadev.mygame.entity_modules.MovementModule;
import com.lyadev.mygame.entity_modules.PlayableModules;
import com.lyadev.mygame.entity_modules.SelectableModule;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.models.MoveEventSetting;
import com.lyadev.mygame.models.MoveSettings;
import com.lyadev.mygame.place.Place;
import com.lyadev.mygame.utils.Size;
import com.lyadev.mygame.utils.listeners.EntityListener;

public class GlobalWorld {
    public static List<Entity> entities;
    public static Entity player;
    public static List<EntityListener> listeners;
    private static boolean ready = false;

    public static boolean isReady() {
        return ready;
    }

    public static void init(Stage stage){
        initFields();

        MoveSettings moveSettings = new MoveSettings(
                new MoveEventSetting(MoveType.UP, 2),
                new MoveEventSetting(MoveType.DOWN, 4),
                new MoveEventSetting(MoveType.LEFT, 3),
                new MoveEventSetting(MoveType.RIGHT, 1));

        PlayableEntitySettings manSettings = new PlayableEntitySettings(
                "Man",
                Assets.PERSON_MAN,
                Size.ENTITY_DEFAULT,
                4,
                moveSettings,
                100,
                200,
                Gdx.graphics.getWidth() * 0.2f);
        Entity man = createPlayableEntity(manSettings);
        man.setRandomPositionInScreen();

        PlayableEntitySettings womanSettings = new PlayableEntitySettings(
                "Woomen",
                Assets.PERSON_WOMAN,
                Size.ENTITY_DEFAULT,
                4,
                moveSettings,
                100,
                200,
                100f);
        Entity woman = createPlayableEntity(womanSettings);
        woman.setRandomPositionInScreen();

        setActivePlayer(man);
        resetVisionState();

        Place place = new Place(new Size(1000, 1000));
        stage.addActor(place);
        for(Entity entity : entities){
            stage.addActor(entity);
        }
        refreshEntityVisibility();
        ready = true;
        Gdx.app.debug("GlobalWorld", "World ready. Entities: " + entities.size());
        for(Entity entity : entities){
            Gdx.app.debug("GlobalWorld", entity.getTAG()
                    + " active=" + entity.getStatus().isActive()
                    + " show=" + entity.getStatus().getIsShow()
                    + " actorVisible=" + entity.isVisible());
        }
    }

    public static void setActivePlayer(Entity activePlayer) {
        for(Entity entity : entities){
            entity.getStatus().setActive(entity == activePlayer);
            EntityModule movement = entity.getModule("movement_module");
            if(movement instanceof MovementModule){
                ((MovementModule) movement).stopMoving();
            }
        }
        player = activePlayer;
        refreshEntityVisibility();
    }

    public static void refreshEntityVisibility() {
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.updateVisibility();
            } else {
                entity.setVisible(false);
            }
        }
    }

    public static void resetVisionState() {
        for(Entity entity : entities){
            entity.getStatus().setIsShow(false);
            entity.getVision().resetVisionTracking();
        }
        refreshEntityVisibility();
    }

    private static Entity createPlayableEntity(PlayableEntitySettings settings) {
        Entity entity = new Entity(settings);
        PlayableModules.register(entity);
        entity.finishInit();
        addEntity(entity);
        return entity;
    }

    private static void initFields(){
        entities = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static void addEntity(Entity entity) {
        entities.add(entity);
    }

    public static void dispose() {
        for (Entity entity : entities) {
            entity.dispose();
        }
    }

    public static void updateEntityMouseInfo(float screenX, float screenY) {
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.mousePositionListener(screenX, screenY);
            }
        }
    }

    public static void setAllRandomPositions() {
        for(Entity entity : entities){
            entity.setRandomPositionInScreen();
        }
        resetVisionState();
    }

    public static void handleEntityClick() {
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.clearVisionVisibility();
            }
        }
        for(Entity entity : entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null){
                selectable.clickEvent();
            }
        }
    }
}

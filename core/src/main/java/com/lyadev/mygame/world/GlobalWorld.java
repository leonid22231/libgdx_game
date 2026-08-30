package com.lyadev.mygame.world;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.Assets;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.modules.ai.AiBrainModule;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.movement.MovementSettings;
import com.lyadev.mygame.modules.animation.AnimationClip;
import com.lyadev.mygame.modules.animation.AnimationSettings;
import com.lyadev.mygame.modules.playable.AnimatedPlayableBlueprint;
import com.lyadev.mygame.modules.playable.PlayableBlueprint;
import com.lyadev.mygame.modules.playable.PlayableModules;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.sprite.SpriteSettings;
import com.lyadev.mygame.modules.texture.TextureSettings;
import com.lyadev.mygame.modules.vision.VisionModule;
import com.lyadev.mygame.modules.vision.VisionSettings;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.models.MoveEventSetting;
import com.lyadev.mygame.models.MoveSettings;
import com.lyadev.mygame.place.Place;
import com.lyadev.mygame.utils.Size;

public class GlobalWorld {
    public static List<Entity> entities;
    public static Entity player;
    private static Stage worldStage;
    private static boolean ready = false;

    public static boolean isReady() {
        return ready;
    }

    public static void init(Stage stage){
        worldStage = stage;
        initFields();

        Entity man = createPlayableEntity(defaultManBlueprint(), new AiBrainModule());
        man.setRandomPositionInScreen();

        Entity woman = createPlayableEntity(defaultWomanBlueprint());
        woman.setRandomPositionInScreen();

        Entity newgirl = createAnimatedPlayableEntity(defaultNewgirlBlueprint());
        newgirl.setRandomPositionInScreen();

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
            SelectableModule selectable = SelectableModule.from(entity);
            Gdx.app.debug("GlobalWorld", entity.getTAG()
                    + " active=" + (selectable != null && selectable.isActive())
                    + " show=" + (selectable != null && selectable.isVisibleByVision())
                    + " actorVisible=" + entity.isVisible());
        }
    }

    public static PlayableBlueprint defaultManBlueprint() {
        return buildPlayableBlueprint(
                "Man",
                Assets.PERSON_MAN,
                Gdx.graphics.getWidth() * 0.2f);
    }

    public static PlayableBlueprint defaultWomanBlueprint() {
        return buildPlayableBlueprint(
                "Woomen",
                Assets.PERSON_WOMAN,
                100f);
    }

    public static AnimatedPlayableBlueprint defaultNewgirlBlueprint() {
        return buildAnimatedPlayableBlueprint(
                "Newgirl",
                defaultNewgirlAnimation(),
                120,
                220,
                Gdx.graphics.getWidth() * 0.2f);
    }

    public static AnimationSettings defaultNewgirlAnimation() {
        Size frame = new Size(64, 128);
        return new AnimationSettings(
                new AnimationClip("idle", Assets.NEWGIRL_IDLE, frame, 4, 8, 8f),
                new AnimationClip("walk", Assets.NEWGIRL_WALK, frame, 4, 10, 10f),
                new AnimationClip("run", Assets.NEWGIRL_RUN, frame, 4, 8, 12f),
                4);
    }

    public static AnimatedPlayableBlueprint buildAnimatedPlayableBlueprint(
            String tag,
            AnimationSettings animation,
            int walkSpeed,
            int sprintSpeed,
            float visibleRadius) {
        return new AnimatedPlayableBlueprint(
                tag,
                animation,
                new MovementSettings(walkSpeed, sprintSpeed),
                new VisionSettings(visibleRadius));
    }

    public static Entity spawnAnimatedPlayableEntity(AnimatedPlayableBlueprint blueprint) {
        Entity entity = new Entity(blueprint.getTag());
        PlayableModules.registerAnimated(entity, blueprint);
        addEntity(entity);
        if(worldStage != null){
            worldStage.addActor(entity);
        }
        refreshEntityVisibility();
        return entity;
    }

    public static PlayableBlueprint buildPlayableBlueprint(String tag, String texturePath, float visibleRadius) {
        MoveSettings moveSettings = defaultMoveSettings();
        return new PlayableBlueprint(
                tag,
                new TextureSettings(texturePath, Size.ENTITY_DEFAULT, 4),
                new SpriteSettings(moveSettings),
                new MovementSettings(100, 200),
                new VisionSettings(visibleRadius));
    }

    private static MoveSettings defaultMoveSettings() {
        return new MoveSettings(
                new MoveEventSetting(MoveType.UP, 2),
                new MoveEventSetting(MoveType.DOWN, 4),
                new MoveEventSetting(MoveType.LEFT, 3),
                new MoveEventSetting(MoveType.RIGHT, 1));
    }

    public static void setActivePlayer(Entity activePlayer) {
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

    private static Entity createAnimatedPlayableEntity(AnimatedPlayableBlueprint blueprint, EntityModule... extraModules) {
        Entity entity = new Entity(blueprint.getTag());
        PlayableModules.registerAnimatedModules(entity, blueprint);
        for(EntityModule extraModule : extraModules){
            entity.registerModule(extraModule);
        }
        entity.resolveModules();
        addEntity(entity);
        return entity;
    }

    private static Entity createPlayableEntity(PlayableBlueprint blueprint, EntityModule... extraModules) {
        Entity entity = new Entity(blueprint.getTag());
        PlayableModules.registerModules(entity, blueprint);
        for(EntityModule extraModule : extraModules){
            entity.registerModule(extraModule);
        }
        entity.resolveModules();
        addEntity(entity);
        return entity;
    }

    private static void initFields(){
        entities = new ArrayList<>();
    }

    public static void addEntity(Entity entity) {
        entities.add(entity);
    }

    public static Entity spawnPlayableEntity(PlayableBlueprint blueprint) {
        Entity entity = new Entity(blueprint.getTag());
        PlayableModules.register(entity, blueprint);
        addEntity(entity);
        if(worldStage != null){
            worldStage.addActor(entity);
        }
        refreshEntityVisibility();
        return entity;
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

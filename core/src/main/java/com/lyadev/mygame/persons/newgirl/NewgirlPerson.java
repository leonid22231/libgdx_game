package com.lyadev.mygame.persons.newgirl;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.modules.animation.AnimationClip;
import com.lyadev.mygame.modules.animation.AnimationModule;
import com.lyadev.mygame.modules.animation.AnimationSettings;
import com.lyadev.mygame.modules.camera.CameraFollowModule;
import com.lyadev.mygame.modules.live.LiveModule;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.playable.AnimatedPlayableBlueprint;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.texture.TextureModule;
import com.lyadev.mygame.modules.vision.VisionModule;
import com.lyadev.mygame.modules.visionmouse.VisionMouseModule;
import com.lyadev.mygame.persons.PersonBlueprints;
import com.lyadev.mygame.persons.PersonTags;
import com.lyadev.mygame.persons.PersonWorld;
import com.lyadev.mygame.utils.Size;
import com.lyadev.mygame.world.WorldController;

public final class NewgirlPerson {
    public static final String TAG = "Newgirl";
    public static final String IDLE_SHEET = "persons/newgirl/idle.png";
    public static final String WALK_SHEET = "persons/newgirl/walk.png";
    public static final String RUN_SHEET = "persons/newgirl/run.png";

    private static final int WALK_SPEED = 60;
    private static final int SPRINT_SPEED = 110;
    /** Sheet frames are 64×128; 0.25 → ~16×32 like man/woman on 16px tiles. */
    private static final float SCALE_FACTOR = 0.25f;

    private NewgirlPerson() {
        throw new UnsupportedOperationException();
    }

    public static AnimatedPlayableBlueprint blueprint() {
        return blueprint(TAG);
    }

    public static AnimatedPlayableBlueprint blueprint(String tag) {
        return PersonBlueprints.animated(
                tag,
                animation(),
                WALK_SPEED,
                SPRINT_SPEED,
                80f);
    }

    public static Entity spawn() {
        return spawn(create());
    }

    public static Entity spawnUnique() {
        String tag = PersonTags.unique(TAG, WorldController.allEntities());
        return spawn(create(blueprint(tag)));
    }

    public static Entity spawn(Entity entity) {
        return PersonWorld.adopt(entity);
    }

    public static Entity create() {
        return create(blueprint());
    }

    public static Entity create(AnimatedPlayableBlueprint blueprint) {
        Entity entity = new Entity(blueprint.getTag());
        registerModules(entity, blueprint);
        entity.resolveModules();
        return entity;
    }

    public static Entity create(AnimatedPlayableBlueprint blueprint, EntityModule... extraModules) {
        Entity entity = new Entity(blueprint.getTag());
        registerModules(entity, blueprint);
        for(EntityModule module : extraModules){
            entity.registerModule(module);
        }
        entity.resolveModules();
        return entity;
    }

    public static AnimationSettings animation() {
        Size frame = new Size(64, 128);
        return new AnimationSettings(
                new AnimationClip("idle", IDLE_SHEET, frame, 4, 8, 8f),
                new AnimationClip("walk", WALK_SHEET, frame, 4, 10, 10f),
                new AnimationClip("run", RUN_SHEET, frame, 4, 8, 12f),
                SCALE_FACTOR);
    }

    private static void registerModules(Entity entity, AnimatedPlayableBlueprint blueprint) {
        entity.registerModule(TextureModule.forAnimation(blueprint.getAnimation()));
        entity.registerModule(new AnimationModule(blueprint.getAnimation()));
        entity.registerModule(new MovementModule(blueprint.getMovement()));
        entity.registerModule(new VisionModule(blueprint.getVision()));
        entity.registerModule(new SelectableModule());
        entity.registerModule(new VisionMouseModule());
        entity.registerModule(new LiveModule());
        entity.registerModule(new CameraFollowModule());
    }
}

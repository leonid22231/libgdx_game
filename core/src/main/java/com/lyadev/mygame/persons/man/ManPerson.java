package com.lyadev.mygame.persons.man;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.modules.ai.AiBrainModule;
import com.lyadev.mygame.modules.camera.CameraFollowModule;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.playable.PlayableBlueprint;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.sprite.SpriteModule;
import com.lyadev.mygame.modules.texture.TextureModule;
import com.lyadev.mygame.modules.vision.VisionModule;
import com.lyadev.mygame.persons.PersonBlueprints;
import com.lyadev.mygame.persons.PersonTags;
import com.lyadev.mygame.persons.PersonWorld;
import com.lyadev.mygame.world.WorldController;

public final class ManPerson {
    public static final String TAG = "Man";
    public static final String TEXTURE = "persons/man/default.png";

    private ManPerson() {
        throw new UnsupportedOperationException();
    }

    public static PlayableBlueprint blueprint() {
        return blueprint(TAG);
    }

    public static PlayableBlueprint blueprint(String tag) {
        return PersonBlueprints.staticStrip(tag, TEXTURE, 80f);
    }

    /** Man + AiBrain (стартовый персонаж мира). */
    public static Entity spawnDefault() {
        return spawn(createDefault());
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

    public static Entity createDefault() {
        Entity entity = new Entity(TAG);
        registerModules(entity, blueprint());
        entity.registerModule(new AiBrainModule());
        entity.resolveModules();
        return entity;
    }

    public static Entity create() {
        return create(blueprint());
    }

    public static Entity create(PlayableBlueprint blueprint) {
        Entity entity = new Entity(blueprint.getTag());
        registerModules(entity, blueprint);
        entity.resolveModules();
        return entity;
    }

    public static Entity create(PlayableBlueprint blueprint, EntityModule... extraModules) {
        Entity entity = new Entity(blueprint.getTag());
        registerModules(entity, blueprint);
        for(EntityModule module : extraModules){
            entity.registerModule(module);
        }
        entity.resolveModules();
        return entity;
    }

    private static void registerModules(Entity entity, PlayableBlueprint blueprint) {
        entity.registerModule(TextureModule.forStatic(blueprint.getTexture()));
        entity.registerModule(new SpriteModule(blueprint.getSprite()));
        entity.registerModule(new MovementModule(blueprint.getMovement()));
        entity.registerModule(new VisionModule(blueprint.getVision()));
        entity.registerModule(new SelectableModule());
        entity.registerModule(new CameraFollowModule());
    }
}

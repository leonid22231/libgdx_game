package com.lyadev.mygame.modules.playable;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.modules.animation.AnimationModule;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.sprite.SpriteModule;
import com.lyadev.mygame.modules.texture.TextureModule;
import com.lyadev.mygame.modules.vision.VisionModule;

public final class PlayableModules {
    private PlayableModules() {
        throw new UnsupportedOperationException();
    }

    public static void register(Entity entity, PlayableBlueprint blueprint) {
        registerModules(entity, blueprint);
        entity.resolveModules();
    }

    public static void registerAnimated(Entity entity, AnimatedPlayableBlueprint blueprint) {
        registerAnimatedModules(entity, blueprint);
        entity.resolveModules();
    }

    /** Статичный strip + SpriteModule (man, woman, блоки). */
    public static void registerModules(Entity entity, PlayableBlueprint blueprint) {
        entity.registerModule(TextureModule.forStatic(blueprint.getTexture()));
        entity.registerModule(new SpriteModule(blueprint.getSprite()));
        entity.registerModule(new MovementModule(blueprint.getMovement()));
        entity.registerModule(new VisionModule(blueprint.getVision()));
        entity.registerModule(new SelectableModule());
    }

    /** Grid sheets idle/walk/run + AnimationModule (newgirl). */
    public static void registerAnimatedModules(Entity entity, AnimatedPlayableBlueprint blueprint) {
        entity.registerModule(TextureModule.forAnimation(blueprint.getAnimation()));
        entity.registerModule(new AnimationModule(blueprint.getAnimation()));
        entity.registerModule(new MovementModule(blueprint.getMovement()));
        entity.registerModule(new VisionModule(blueprint.getVision()));
        entity.registerModule(new SelectableModule());
    }
}

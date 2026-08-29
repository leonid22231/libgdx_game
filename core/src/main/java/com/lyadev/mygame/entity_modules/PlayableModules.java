package com.lyadev.mygame.entity_modules;

import com.lyadev.mygame.base.Entity;

public final class PlayableModules {
    private PlayableModules() {
        throw new UnsupportedOperationException();
    }

    public static void register(Entity entity) {
        entity.addModule(new TextureModule());
        entity.addModule(new SpriteModule());
        entity.addModule(new MovementModule());
        entity.addModule(new VisionModule());
        entity.addModule(new SelectableModule());
    }
}

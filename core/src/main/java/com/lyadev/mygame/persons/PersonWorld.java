package com.lyadev.mygame.persons;

import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.world.GlobalWorld;

/** Подключение entity, созданного персонажем, к {@link GlobalWorld}. */
public final class PersonWorld {
    private PersonWorld() {
        throw new UnsupportedOperationException();
    }

    public static Entity adopt(Entity entity) {
        GlobalWorld.addEntity(entity);
        GlobalWorld.addEntityToStage(entity);
        GlobalWorld.refreshEntityVisibility();
        return entity;
    }
}

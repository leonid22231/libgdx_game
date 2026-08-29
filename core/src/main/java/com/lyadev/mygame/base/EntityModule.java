package com.lyadev.mygame.base;

import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class EntityModule {
    private Entity entity;

    public final void attach(Entity entity) {
        this.entity = entity;
    }

    protected Entity getEntity() {
        return entity;
    }

    public abstract String getName();

    public void init() {
    }

    public void act(float delta) {
    }

    public void draw(Batch batch, float parentAlpha) {
    }

    public void dispose() {
    }
}

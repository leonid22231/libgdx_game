package com.lyadev.mygame.entity_modules;

import com.badlogic.gdx.graphics.g2d.Batch;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.EntityTexture;

public class TextureModule extends EntityModule {
    @Override
    public String getName() {
        return "texture_module";
    }

    @Override
    public void init() {
        Entity entity = getEntity();
        EntityTexture texture = entity.getTexture();
        texture.init(entity.getSettings());
        entity.setSize(texture.getTextureSize());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Entity entity = getEntity();
        SelectableModule selectable = SelectableModule.from(entity);
        if(selectable != null && !selectable.shouldRender()){
            return;
        }
        EntityTexture texture = entity.getTexture();
        if(texture.getAllTextureRegions().isEmpty()){
            return;
        }
        batch.draw(
                texture.getCurrentTexture(),
                entity.getPosition().getX(),
                entity.getPosition().getY(),
                entity.getSize().getWidth(),
                entity.getSize().getHeight());
    }
}

package com.lyadev.mygame.modules.texture;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.modules.animation.AnimationSettings;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.utils.Size;

import lombok.Getter;

public class TextureModule extends EntityModule {
    @Getter
    private final EntityTexture entityTexture = new EntityTexture();

    private final TextureSettings textureSettings;
    private final AnimationSettings animationSettings;

    private TextureModule(TextureSettings textureSettings, AnimationSettings animationSettings) {
        this.textureSettings = textureSettings;
        this.animationSettings = animationSettings;
    }

    public static TextureModule forStatic(TextureSettings settings) {
        return new TextureModule(settings, null);
    }

    public static TextureModule forAnimation(AnimationSettings settings) {
        return new TextureModule(null, settings);
    }

    @Override
    public String getName() {
        return "texture_module";
    }

    @Override
    public void init() {
        if(animationSettings != null){
            entityTexture.initAnimated(animationSettings);
        } else {
            entityTexture.init(textureSettings);
        }
        Size drawSize = entityTexture.getTextureSize();
        getEntity().setSize(drawSize.getWidth(), drawSize.getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Entity entity = getEntity();
        SelectableModule selectable = SelectableModule.from(entity);
        if(selectable != null && !selectable.shouldRender()){
            return;
        }
        TextureRegion frame = entityTexture.getCurrentTexture();
        if(frame == null){
            return;
        }
        batch.draw(frame, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    @Override
    public void dispose() {
        entityTexture.dispose();
    }

    public static TextureModule from(Entity entity) {
        return entity.getModule(TextureModule.class);
    }
}

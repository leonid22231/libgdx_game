package com.lyadev.mygame.modules.texture;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;
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
        float drawX = entity.getX();
        float drawY = entity.getY();
        float width = entity.getWidth();
        float height = entity.getHeight();

        if(entityTexture.isFlipHorizontal()){
            batch.draw(frame, drawX + width, drawY, -width, height);
        } else {
            batch.draw(frame, drawX, drawY, width, height);
        }
    }

    @Override
    public void dispose() {
        entityTexture.dispose();
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        if(!isEnabled()){
            return;
        }
        boolean animated = animationSettings != null;
        panel.line("mode", animated ? "animated" : "static");
        panel.line("drawSize", entityTexture.getTextureSize());
        if(animated){
            panel.line("activeClip", entityTexture.getActiveClipId());
            panel.line("activeRow", entityTexture.getActiveRow());
            panel.line("activeColumn", entityTexture.getActiveColumn());
            panel.line("flipHorizontal", entityTexture.isFlipHorizontal());
            panel.line("directionMode", animationSettings.getDirectionMode());
        } else {
            panel.line("regions", entityTexture.getAllTextureRegions().size());
            panel.line("spriteIndex", entityTexture.getCurrentSpriteIndex());
        }
    }

    public static TextureModule from(Entity entity) {
        return entity.getModule(TextureModule.class);
    }
}

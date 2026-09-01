package com.lyadev.mygame.modules.sprite;

import java.util.List;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.models.MoveEventSetting;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.texture.TextureModule;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpriteModule extends EntityModule {
    private final SpriteSettings settings;

    @Override
    public String getName() {
        return "sprite_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(TextureModule.class, MovementModule.class);
    }

    @Override
    public void init() {
        applySpriteIndex();
    }

    @Override
    public void act(float delta) {
        applySpriteIndex();
    }

    public int getCurrentSpriteIndex() {
        return require(TextureModule.class).getEntityTexture().getCurrentSpriteIndex();
    }

    private void applySpriteIndex() {
        TextureModule textureModule = require(TextureModule.class);
        if(textureModule.getEntityTexture().getAllTextureRegions().isEmpty()){
            return;
        }
        textureModule.getEntityTexture().setCurrentSpriteIndex(resolveSpriteIndex());
    }

    private int resolveSpriteIndex() {
        if(settings.getMoveSettings() == null){
            return 0;
        }
        MoveEventSetting[] moveEvents = settings.getMoveSettings().moveEvents;
        if(moveEvents == null || moveEvents.length == 0){
            return 0;
        }
        var facing = require(MovementModule.class).getFacingDirection();
        for(MoveEventSetting event : moveEvents){
            if(event.getType() == facing){
                return event.getSpriteNumber();
            }
        }
        return moveEvents[0].getSpriteNumber();
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        if(!isEnabled()){
            return;
        }
        panel.line("spriteIndex", getCurrentSpriteIndex());
        panel.line("facing", require(MovementModule.class).getFacingDirection());
        panel.line("regions", require(TextureModule.class).getEntityTexture().getAllTextureRegions().size());
    }

    public static SpriteModule from(Entity entity) {
        return entity.getModule(SpriteModule.class);
    }
}

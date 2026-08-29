package com.lyadev.mygame.entity_modules;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.PlayableEntitySettings;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.models.MoveEventSetting;

public class SpriteModule extends EntityModule {
    private MoveType lastMoveType = MoveType.DOWN;

    @Override
    public String getName() {
        return "sprite_module";
    }

    @Override
    public void init() {
        applySpriteIndex();
    }

    @Override
    public void act(float delta) {
        applySpriteIndex();
    }

    public void setLastMoveType(MoveType moveType) {
        if(moveType == null){
            return;
        }
        lastMoveType = moveType;
    }

    public MoveType getLastMoveType() {
        return lastMoveType;
    }

    private void applySpriteIndex() {
        Entity entity = getEntity();
        if(entity.getTexture().getAllTextureRegions().isEmpty()){
            return;
        }
        entity.getTexture().setCurrentSpriteIndex(resolveSpriteIndex());
    }

    private int resolveSpriteIndex() {
        PlayableEntitySettings settings = getEntity().getPlayableSettings();
        if(settings == null || settings.getMoveSettings() == null){
            return 0;
        }
        MoveEventSetting[] moveEvents = settings.getMoveSettings().moveEvents;
        if(moveEvents == null || moveEvents.length == 0){
            return 0;
        }
        for(MoveEventSetting event : moveEvents){
            if(event.getType() == lastMoveType){
                return event.getSpriteNumber();
            }
        }
        return moveEvents[0].getSpriteNumber();
    }

    public static SpriteModule from(Entity entity) {
        EntityModule module = entity.getModule("sprite_module");
        if(module instanceof SpriteModule){
            return (SpriteModule) module;
        }
        return null;
    }
}

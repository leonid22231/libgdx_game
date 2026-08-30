package com.lyadev.mygame.modules.animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.texture.TextureModule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnimationModule extends EntityModule {
    private final AnimationSettings settings;

    private AnimationState activeState = AnimationState.IDLE;
    @Getter
    private int frameIndex;
    private float frameTimer;

    @Override
    public String getName() {
        return "animation_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(TextureModule.class, MovementModule.class);
    }

    @Override
    public void init() {
        require(MovementModule.class);
        require(TextureModule.class).getEntityTexture().setAnimatedFrame(
                settings.getIdle().getId(),
                0,
                0);
    }

    @Override
    public void act(float delta) {
        MovementModule movement = require(MovementModule.class);
        TextureModule textureModule = require(TextureModule.class);

        AnimationState nextState = resolveState(movement);
        if(nextState != activeState){
            activeState = nextState;
            frameIndex = 0;
            frameTimer = 0f;
        }

        AnimationClip clip = clipForState(activeState);
        int row = rowForDirection(movement.getFacingDirection());
        advanceFrame(clip, delta);

        textureModule.getEntityTexture().setAnimatedFrame(clip.getId(), row, frameIndex);
    }

    public String getActiveClipId() {
        return clipForState(activeState).getId();
    }

    public static AnimationModule from(Entity entity) {
        return entity.getModule(AnimationModule.class);
    }

    private AnimationState resolveState(MovementModule movement) {
        if(!movement.isMoving()){
            return AnimationState.IDLE;
        }
        if(movement.isSprinting()){
            return AnimationState.RUN;
        }
        return AnimationState.WALK;
    }

    private AnimationClip clipForState(AnimationState state) {
        if(state == AnimationState.WALK){
            return settings.getWalk();
        }
        if(state == AnimationState.RUN){
            return settings.getRun();
        }
        return settings.getIdle();
    }

    private static int rowForDirection(MoveType direction) {
        if(direction == MoveType.RIGHT){
            return 1;
        }
        if(direction == MoveType.LEFT){
            return 2;
        }
        if(direction == MoveType.UP){
            return 3;
        }
        return 0;
    }

    private void advanceFrame(AnimationClip clip, float delta) {
        if(clip.getFps() <= 0f || clip.getColumns() <= 0){
            frameIndex = 0;
            return;
        }
        frameTimer += delta;
        float frameDuration = 1f / clip.getFps();
        while(frameTimer >= frameDuration){
            frameTimer -= frameDuration;
            frameIndex = (frameIndex + 1) % clip.getColumns();
        }
    }
}

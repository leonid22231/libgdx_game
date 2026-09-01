package com.lyadev.mygame.modules.animation;

import java.util.List;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.texture.EntityTexture;
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
        MovementModule movement = require(MovementModule.class);
        EntityTexture texture = require(TextureModule.class).getEntityTexture();
        MoveType facing = movement.getFacingDirection();
        texture.setFlipHorizontal(shouldFlipHorizontal(settings.getDirectionMode(), facing));
        texture.setAnimatedFrame(
                settings.getIdle().getId(),
                rowForFacing(settings.getDirectionMode(), facing),
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
        MoveType facing = movement.getFacingDirection();
        int row = rowForFacing(settings.getDirectionMode(), facing);
        boolean flip = shouldFlipHorizontal(settings.getDirectionMode(), facing);
        advanceFrame(clip, delta);

        EntityTexture entityTexture = textureModule.getEntityTexture();
        entityTexture.setFlipHorizontal(flip);
        entityTexture.setAnimatedFrame(clip.getId(), row, frameIndex);
    }

    public String getActiveClipId() {
        return clipForState(activeState).getId();
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        if(!isEnabled()){
            return;
        }
        MovementModule movement = require(MovementModule.class);
        panel.line("state", activeState);
        panel.line("clip", getActiveClipId());
        panel.line("frame", frameIndex);
        panel.line("facing", movement.getFacingDirection());
        panel.line("directionMode", settings.getDirectionMode());
        panel.line("flipHorizontal", require(TextureModule.class).getEntityTexture().isFlipHorizontal());
        panel.line("moving", movement.isMoving());
        panel.action("reset_animation", "Reset to idle");
    }

    @Override
    public String handleDebugAction(String actionId) {
        if("reset_animation".equals(actionId)){
            activeState = AnimationState.IDLE;
            frameIndex = 0;
            frameTimer = 0f;
            require(TextureModule.class).getEntityTexture().setFlipHorizontal(
                    shouldFlipHorizontal(settings.getDirectionMode(), require(MovementModule.class).getFacingDirection()));
            require(TextureModule.class).getEntityTexture().setAnimatedFrame(
                    settings.getIdle().getId(),
                    rowForFacing(settings.getDirectionMode(), require(MovementModule.class).getFacingDirection()),
                    0);
            return "Animation reset to idle";
        }
        return super.handleDebugAction(actionId);
    }

    public static AnimationModule from(Entity entity) {
        return entity.getModule(AnimationModule.class);
    }

    private static int rowForFacing(AnimationDirectionMode mode, MoveType direction) {
        if(mode == AnimationDirectionMode.MIRROR_HORIZONTAL){
            return 0;
        }
        return rowForDirection(direction);
    }

    private static boolean shouldFlipHorizontal(AnimationDirectionMode mode, MoveType direction) {
        return mode == AnimationDirectionMode.MIRROR_HORIZONTAL && direction == MoveType.LEFT;
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
            return 0;
        }
        if(direction == MoveType.DOWN){
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

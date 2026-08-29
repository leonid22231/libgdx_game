package com.lyadev.mygame.entity_modules;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.PlayableEntitySettings;
import com.lyadev.mygame.enums.MoveType;

public class MovementModule extends EntityModule {
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean isSprinting = false;

    @Override
    public String getName() {
        return "movement_module";
    }

    @Override
    public void act(float delta) {
        Entity entity = getEntity();
        if(!entity.getStatus().isActive()){
            return;
        }
        PlayableEntitySettings settings = entity.getPlayableSettings();
        if(settings == null){
            return;
        }
        float speed = isSprinting ? settings.getDefaultSprintSpeed() : settings.getDefaultWalkSpeed();
        SpriteModule spriteModule = getSpriteModule();

        if(moveLeft){
            entity.getPosition().setX(entity.getPosition().getX() - delta * speed);
            setMoveType(spriteModule, MoveType.LEFT);
        }
        if(moveRight){
            entity.getPosition().setX(entity.getPosition().getX() + delta * speed);
            setMoveType(spriteModule, MoveType.RIGHT);
        }
        if(moveUp){
            entity.getPosition().setY(entity.getPosition().getY() + delta * speed);
            setMoveType(spriteModule, MoveType.UP);
        }
        if(moveDown){
            entity.getPosition().setY(entity.getPosition().getY() - delta * speed);
            setMoveType(spriteModule, MoveType.DOWN);
        }
    }

    public void moveLeftToggle() {
        moveLeft = !moveLeft;
    }

    public void moveRightToggle() {
        moveRight = !moveRight;
    }

    public void moveUpToggle() {
        moveUp = !moveUp;
    }

    public void moveDownToggle() {
        moveDown = !moveDown;
    }

    public void sprintToggle() {
        isSprinting = !isSprinting;
    }

    public void stopMoving() {
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
        isSprinting = false;
    }

    public boolean isMoving() {
        return moveLeft || moveRight || moveUp || moveDown;
    }

    private SpriteModule getSpriteModule() {
        EntityModule module = getEntity().getModule("sprite_module");
        if(module instanceof SpriteModule){
            return (SpriteModule) module;
        }
        return null;
    }

    private void setMoveType(SpriteModule spriteModule, MoveType moveType) {
        if(spriteModule != null){
            spriteModule.setLastMoveType(moveType);
        }
    }
}

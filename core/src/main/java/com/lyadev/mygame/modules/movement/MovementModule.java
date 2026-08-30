package com.lyadev.mygame.modules.movement;

import java.util.Collections;
import java.util.List;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.modules.selectable.SelectableModule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovementModule extends EntityModule {
    private final MovementSettings settings;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean sprinting = false;

    @Getter
    private MoveType facingDirection = MoveType.DOWN;

    @Override
    public String getName() {
        return "movement_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return Collections.emptyList();
    }

    @Override
    public void act(float delta) {
        Entity entity = getEntity();
        SelectableModule selectable = SelectableModule.from(entity);
        if(selectable == null || !selectable.isActive()){
            return;
        }
        float speed = sprinting ? settings.getSprintSpeed() : settings.getWalkSpeed();

        if(moveLeft){
            entity.setX(entity.getX() - delta * speed);
            facingDirection = MoveType.LEFT;
        }
        if(moveRight){
            entity.setX(entity.getX() + delta * speed);
            facingDirection = MoveType.RIGHT;
        }
        if(moveUp){
            entity.setY(entity.getY() + delta * speed);
            facingDirection = MoveType.UP;
        }
        if(moveDown){
            entity.setY(entity.getY() - delta * speed);
            facingDirection = MoveType.DOWN;
        }
    }

    public boolean isMoving() {
        return moveLeft || moveRight || moveUp || moveDown;
    }

    public boolean isSprinting() {
        return sprinting && isMoving();
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
        sprinting = !sprinting;
    }

    public void rotate() {
        switch(facingDirection){
            case LEFT:
                facingDirection = MoveType.RIGHT;
                break;
            case RIGHT:
                facingDirection = MoveType.LEFT;
                break;
            case UP:
                facingDirection = MoveType.DOWN;
                break;
            case DOWN:
                facingDirection = MoveType.UP;
                break;
        }
    }

    public void stopMoving() {
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
        sprinting = false;
    }

    public static MovementModule from(Entity entity) {
        return entity.getModule(MovementModule.class);
    }
}
